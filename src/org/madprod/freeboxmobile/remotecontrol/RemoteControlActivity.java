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
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import dalvik.system.DexClassLoader;

import android.app.Activity;
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
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;

/**
 *
 * @author Clement Beslon
 * $Id: RemoteControlActivity.java 2010-05-04 $
 * 
 */

public class RemoteControlActivity extends Activity implements Constants, RemoteControlActivityConstants
{

	static final CommandManager cm = CommandManager.getCommandManager();
	private static TabHost th;
	private static boolean fullscreen;
	private static String currentTag;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"RemoteControlActivity create");
		super.onCreate(savedInstanceState);
		FBMNetTask.register(this);


		new RefreshBoxes().execute();

		new BroadCastManager(getApplicationContext());



		Log.e(TAG, "th = "+th);
		loadUI();
		if (currentTag != null){
			Log.i(TAG, "On remet le tabhost sur l onglet "+currentTag);
			th.setCurrentTabByTag(currentTag);
		}
		th.setOnTabChangedListener(new OnTabChangeListener(){

			@Override
			public void onTabChanged(String tag) {
				currentTag = tag;
			}
			
		});
		chooseView();
	}	

	



	private void chooseView() {
		if (fullscreen){
			
			if (currentTag.compareTo("mosaic") == 0){
				setContentView(createMosaicViewForTab(getFilesDir()+PATHMOSAICHORIZONTAL, getFilesDir()+ PATHMOSAICVERTICAL));					
			}else{
				setContentView(createRemoteViewForTab(getFilesDir()+PATHREMOTEHORIZONTAL, getFilesDir()+ PATHREMOTEVERTICAL));										
			}
		}else{
			setContentView(th);		
			if (currentTag != null){
				th.setCurrentTabByTag(currentTag);
			}
		}
		Log.e(TAG, "view : "+th.getCurrentTabTag());
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
		menu.add(0, FULLSCREEN, 0, "Mode plein ecran");
		menu.add(0, SEARCHBOXES, 0, "Rechercher les boitiers");
		menu.add(0, BOXESMANAGER, 0, "Gestion des boitiers");
		menu.add(0, LAYOUTMANAGER, 0, "Changer le skin");
		menu.add(0, LAYOUTDOWNLOAD, 0, "Telecharger les skins par defaut");
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.e(TAG, "current Tab = "+th.getCurrentTabTag());
		if (th.getCurrentTabTag().compareTo("remote") != 0 && th.getCurrentTabTag().compareTo("mosaic") != 0 ){
			menu.findItem(FULLSCREEN).setEnabled(false);
			menu.findItem(FULLSCREEN).setTitle("Mode plein ecran");
		}else{
			menu.findItem(FULLSCREEN).setEnabled(true);

			if (fullscreen){
				menu.findItem(FULLSCREEN).setTitle("Quittez mode plein ecran");
			}else{
				menu.findItem(FULLSCREEN).setTitle("Mode plein ecran");
			}
		}
		return super.onPrepareOptionsMenu(menu);
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
		case FULLSCREEN:
			fullscreen = !fullscreen;
			chooseView();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHOOSELAYOUTREQUESTCODE && resultCode == SAVE){

			
			loadUI();
			if (currentTag != null){
				th.setCurrentTabByTag(currentTag);
			}
//			TabHost tabHost = getTabHost();
//			finish();	
//			Toast.makeText(getApplicationContext(), "Nouveau skin installé", Toast.LENGTH_SHORT).show();
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

			File f = null;
			try {
				f = File.createTempFile("tmp", "fbm");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			copyRemoteFile("http://freeboxmobile.googlecode.com/files/skins.xml", f.getPath());
		
		SAXParserFactory fabrique = SAXParserFactory.newInstance();
			List<Skin> skins = null;

			try {

				SAXParser parseur = fabrique.newSAXParser();
				XMLReader xr = parseur.getXMLReader();
				SkinHandler gestionnaire = new SkinHandler();
				xr.setContentHandler(gestionnaire);
				xr.parse(new InputSource(new FileInputStream(f)));
				skins = gestionnaire.getSkins();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

			if (skins != null){
				for (Skin s:skins){
					Log.e(TAG, "Skins = "+s);

					downloadSkin(s);
				}
			}else{
				Log.e(TAG, "Liste des skins vide");
			}

			
			f.delete();

			return null;
		}


		public void downloadSkin(Skin s){
			String pathDest;
			String repDest; 
			if (s.getType().compareTo("remote") == 0){
				repDest = Environment.getExternalStorageDirectory().getAbsolutePath()+DIR_FBM+PATHREMOTESDCARD;
			}else{
				repDest = Environment.getExternalStorageDirectory().getAbsolutePath()+DIR_FBM+PATHMOSAICSDCARD;
			}
			pathDest = repDest+"/"+s.getName();
			String urlPath = s.getPath();
			Log.d(TAG, "Fichier src = "+urlPath);
			Log.d(TAG, "Fichier dst = "+pathDest);

			copyRemoteFile(urlPath, pathDest);
		}

		private void copyRemoteFile(String remoteFile, String localfile){
			try {

				URL url = new URL(remoteFile);
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
				
				
				File fileRepDest = new File(localfile.substring(0, localfile.lastIndexOf("/")));
				
				if (!fileRepDest.exists()) fileRepDest.mkdirs();
				File fileDest = new File(localfile);
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
		
		
		th = (TabHost)LayoutInflater.from(getApplication()).inflate(R.layout.remotecontrol, null);
		th.setup();

		spec = th.newTabSpec("mosaic").setIndicator("Mosaique",
				res.getDrawable(R.drawable.bouton_pause))
				.setContent(new TabContentFactory() {

					@Override
					public View createTabContent(String tag) {
						return createMosaicViewForTab(getFilesDir()+PATHMOSAICHORIZONTAL, getFilesDir()+ PATHMOSAICVERTICAL);
					}
				});


		th.addTab(spec); 


		spec = th.newTabSpec("remote").setIndicator("Telecommande",
				res.getDrawable(R.drawable.bouton_pause))
				.setContent(new TabContentFactory() {

					@Override
					public View createTabContent(String tag) {
						return createRemoteViewForTab(getFilesDir()+PATHREMOTEHORIZONTAL, getFilesDir()+PATHREMOTEVERTICAL);
					}
				});


		th.addTab(spec); 

	}

	private String afficheAide(){
		return "Aucun skin n est selectionne pour cette vue\n\n"+
		"Pour telecharger les skins par defaut, cliquer sur menu -> Telecharger les layouts par defaut\n"+
		"Pour selectionner les skins, cliquer sur menu -> Changer le skin\n"+
		"Pour les reglages des boitiers (codes, activations), cliquer sur menu -> Gestion des boitiers\n";
	}
	

}