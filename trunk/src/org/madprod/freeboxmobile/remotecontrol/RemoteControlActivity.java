package org.madprod.freeboxmobile.remotecontrol;

import java.io.BufferedInputStream;
import java.io.DataInputStream;  
import java.io.File; 
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;

import dalvik.system.DexClassLoader;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabContentFactory;

/**
 *
 * @author Clement Beslon
 * $Id: RemoteControlActivity.java 2010-05-04 $
 * 
 */

public class RemoteControlActivity extends TabActivity implements Constants, RemoteControlActivityConstants
{

	static final CommandManager cm = CommandManager.getCommandManager();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"RemoteControlActivity create");
		super.onCreate(savedInstanceState);
		FBMNetTask.register(this);


		new RefreshBoxes().execute();
		setContentView(R.layout.remotecontrol);

		new BroadCastManager(getApplicationContext());
		loadUI();


//		intent = new Intent().setClass(this, SchedulerActivity.class);
//		spec = tabHost.newTabSpec("scheduler").setIndicator("Organiseur",
//		res.getDrawable(R.drawable.bouton_pause))
//		.setContent(intent);
//		tabHost.addTab(spec); 

	}	




	@Override
	public void onStart()
	{
		super.onStart();
		Log.i(TAG,"RemoteControlActivity Start");
	}


	@Override
	public void onStop()
	{
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		FBMNetTask.unregister(this);
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		Log.i(TAG,"onSaveInstanceState called");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		Log.i(TAG,"onRestoreInstanceState called");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0, SEARCHBOXES, 0, "Rechercher les boitiers");
		menu.add(0, BOXESMANAGER, 0, "Gestion des boitiers");
		menu.add(0, LAYOUTMANAGER, 0, "Changer le skin");
		menu.add(0, LAYOUTDOWNLOAD, 0, "Telecharger les skins par defaut");
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case SEARCHBOXES:
			new RefreshBoxes().execute();
			break;
		case BOXESMANAGER:
			startActivity(new Intent(this, RemoteControlPreferences.class));
			break;
		case LAYOUTMANAGER:
			startActivityForResult(new Intent(this, RemoteControlChooseLayout.class), CHOOSELAYOUTREQUESTCODE);
			break;
		case LAYOUTDOWNLOAD:
			new DownloadLayout().execute();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHOOSELAYOUTREQUESTCODE && resultCode == SAVE){
			TabHost tabHost = getTabHost();
			tabHost.invalidate();
			finish();	
			Toast.makeText(getApplicationContext(), "Nouveau skin installé", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onResume()
	{
		Log.i(TAG,"onResume() start");
		super.onResume();
	}

	@Override
	public void onPause()
	{
		Log.i(TAG,"RemoteControlActivity pause");
		super.onPause();
	}

	private View createMosaicViewForTab(String pathHorizontal, String pathVertical){
		View v = null;
		try {
			String path = setPathWithOrientation(pathHorizontal, pathVertical);
			File dirView = new File(path);
			String[] apks = dirView.list(new FilenameFilter() {								
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".apk");
				}
			});

			File info = new File(dirView.getAbsolutePath()+"/info");
			DataInputStream dis = new DataInputStream(new FileInputStream(info));
			String clazzName = dis.readLine();
			String apk = "";
			for (int i=0; i<apks.length; i++){
				apk += dirView.getAbsolutePath()+"/"+apks[i]+":";
			}			
			DexClassLoader dcl = new DexClassLoader(apk, getFilesDir().getAbsolutePath(), null, ClassLoader.getSystemClassLoader());							
			Class<?> clazz = dcl.loadClass(clazzName);

			Object o = clazz.getConstructor(Context.class).newInstance(getApplicationContext());			

			clazz.getMethod("setMosaicInfo", HashMap.class).invoke(o, new ChaineDbAsker(getApplicationContext()).getAllChainesInfo());
			clazz.getMethod("setPathView", String.class).invoke(o, path);			

			v = (ViewGroup)clazz.getMethod("getViewGroup", (Class [])null).invoke(o, (Object[])null);			
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}catch(Exception e){
			e.printStackTrace();
		}
		if (v == null) {
			v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.remotecontrol_nolayout, null);
			TextView aide = (TextView)v.findViewById(R.id.aideExplication);
			aide.setText(afficheAide());
		}
		return v;

	}


	private View createRemoteViewForTab(String pathHorizontal, String pathVertical){
		View v = null;
		try {
			String path = setPathWithOrientation(pathHorizontal, pathVertical);

			File dirView = new File(path);
			String[] apks = dirView.list(new FilenameFilter() {								
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".apk");
				}
			});

			File info = new File(dirView.getAbsolutePath()+"/info");
			DataInputStream dis = new DataInputStream(new FileInputStream(info));
			String clazzName = dis.readLine();
			String apk = "";
			for (int i=0; i<apks.length; i++){
				apk += dirView.getAbsolutePath()+"/"+apks[i]+":";
			}			
			DexClassLoader dcl = new DexClassLoader(apk, getFilesDir().getAbsolutePath(), null, ClassLoader.getSystemClassLoader());							
			Class<?> clazz = dcl.loadClass(clazzName);

			Object o = clazz.getConstructor(Context.class).newInstance(getApplicationContext());			
			clazz.getMethod("setPathView", String.class).invoke(o, path);			

			v = (ViewGroup)clazz.getMethod("getViewGroup", (Class [])null).invoke(o, (Object[])null);			
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}catch(Exception e){
			e.printStackTrace();
		}
		if (v == null) {
			v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.remotecontrol_nolayout, null);
			TextView aide = (TextView)v.findViewById(R.id.aideExplication);
			aide.setText(afficheAide());
		}
		return v;

	}

	private String setPathWithOrientation(String pathHorizontal, String pathVertical){
		String path;
		int orientation = getResources().getConfiguration().orientation;
		if (new File(pathHorizontal).exists() && new File(pathVertical).exists()){
			if (orientation == Configuration.ORIENTATION_LANDSCAPE){
				path = pathHorizontal;
			}else{
				path = pathVertical;
			}
		}else if (new File(pathHorizontal).exists()){
			path = pathHorizontal;			
		}else{
			path = pathVertical;			
		}
		return path;
	}

	private class RefreshBoxes extends AsyncTask<Void, Void, Void> 
	{

		@Override
		protected Void doInBackground(Void... arg0) {
			cm.refreshAdresses();
			return null;
		}

		@Override
		protected void onPreExecute()
		{
			FBMNetTask.iProgressShow(
					"Recherche des boitiers",
					"Connexion en cours...",
					R.drawable.fm_infos_adsl);
		}


		@Override
		protected void onPostExecute(Void result) {
			FBMNetTask.iProgressDialogDismiss();
			if (!CommandManager.getAdresses().containsValue(true)){
				finish();
				Toast.makeText(getApplicationContext(), "Aucun boitier connecte\nVeuillez verifier vos parametres wifi", Toast.LENGTH_LONG).show();
			}
		}		
	}



	private class DownloadLayout extends AsyncTask<Void, Void, Void> 
	{

		@Override
		protected Void doInBackground(Void... arg0) {


			HashMap<String, String> files = new HashMap<String, String>();
			files.put("SkinMosaicSlideView.zip", "mosaic");
			files.put("SkinMosaicGridView.zip", "mosaic");
			files.put("SkinRemote1.zip", "remote");
			files.put("SkinRemote2.zip", "remote");

			Iterator<Entry<String, String>> it = files.entrySet().iterator();
			while (it.hasNext()){
				
				Entry<String, String> entry = it.next();
				String pathDest;
				String repDest; 
				if (entry.getValue().compareTo("remote") == 0){
					repDest = Environment.getExternalStorageDirectory().getAbsolutePath()+DIR_FBM+PATHREMOTESDCARD;
					pathDest = repDest+"/"+entry.getKey();
				}else{
					repDest = Environment.getExternalStorageDirectory().getAbsolutePath()+DIR_FBM+PATHMOSAICSDCARD;
					pathDest = repDest+"/"+entry.getKey();
				}
				String urlPath = "http://freeboxmobile.googlecode.com/files/"+entry.getKey();
				Log.d(TAG, "Fichier src = "+urlPath);
				Log.d(TAG, "Fichier dst = "+pathDest);
				try {

					URL url = new URL(urlPath);
					URLConnection urlConn = url.openConnection(); 
					urlConn.setDoInput(true); 
					urlConn.setUseCaches(false);
					int length = urlConn.getContentLength();
					if(length == -1){
						throw new IOException("Fichier vide");
					}
					InputStream is = new BufferedInputStream(urlConn.getInputStream());
					byte[] data = new byte[length];
					int currentBit = 0;
					int deplacement = 0;
					while(deplacement < length){
						currentBit = is.read(data, deplacement, data.length-deplacement); 
						if(currentBit == -1)break; 
						deplacement += currentBit;
					}

					is.close();
					if(deplacement != length){
						throw new IOException("Le fichier n'a pas été lu en entier (seulement "
								+ deplacement + " sur " + length + ")");
					}  
					File fileRepDest = new File(repDest);
					if (!fileRepDest.exists()) fileRepDest.mkdirs();
					File fileDest = new File(pathDest);
					if (!fileDest.exists()) fileDest.createNewFile();
					FileOutputStream destinationFile = new FileOutputStream(fileDest); 
					destinationFile.write(data);
					destinationFile.flush();
					destinationFile.close();

				} catch (MalformedURLException e) { 
					e.printStackTrace();
				} catch (IOException e) { 
					e.printStackTrace();
				}   
			}

			return null;
		}

		@Override
		protected void onPreExecute()
		{
			FBMNetTask.iProgressShow(
					"Telechargement des skins",
					"Veuillez patienter svp...",
					R.drawable.fm_infos_adsl);
		}


		@Override
		protected void onPostExecute(Void result) {
			FBMNetTask.iProgressDialogDismiss();
		}		
	}

	private void loadUI(){
		Resources res = getResources(); 
		TabHost.TabSpec spec;  
		TabHost tabHost = getTabHost();  
		spec = tabHost.newTabSpec("mosaic").setIndicator("Mosaique",
				res.getDrawable(R.drawable.bouton_pause))
				.setContent(new TabContentFactory() {

					@Override
					public View createTabContent(String tag) {
						return createMosaicViewForTab(getFilesDir()+PATHMOSAICHORIZONTAL, getFilesDir()+ PATHMOSAICVERTICAL);
					}
				});


		tabHost.addTab(spec); 


		spec = tabHost.newTabSpec("telecommande").setIndicator("Telecommande",
				res.getDrawable(R.drawable.bouton_pause))
				.setContent(new TabContentFactory() {

					@Override
					public View createTabContent(String tag) {
						return createRemoteViewForTab(getFilesDir()+PATHREMOTEHORIZONTAL, getFilesDir()+PATHREMOTEVERTICAL);
					}
				});


		tabHost.addTab(spec); 

	}

	private String afficheAide(){
		return "Aucun skin n est selectionne pour cette vue\n\n"+
		 "Pour telecharger les skins par defaut, cliquer sur menu -> Telecharger les layouts par defaut\n"+
		 "Pour selectionner les skins, cliquer sur menu -> Changer le skin\n"+
		 "Pour les reglages des boitiers (codes, activations), cliquer sur menu -> Gestion des boitiers\n";
	}

}