package org.madprod.freeboxmobile.remotecontrol;

import java.io.BufferedInputStream; 
import java.io.File; 
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.Config;
import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.guide.GuideConstants;
import org.madprod.freeboxmobile.home.HomeConstants;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import dalvik.system.DexClassLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;

/**
 *
 * @author Clement Beslon
 * $Id: RemoteControlActivity.java 2010-05-04 $
 * 
 */

public class RemoteControlActivity extends Activity implements GuideConstants, HomeConstants, Constants, RemoteControlActivityConstants
{

	static final CommandManager cm = CommandManager.getCommandManager();
	private TabHost th;
	private boolean fullscreen;
	private View currentRemoteView;
	private View currentMosaicView;
	private String currentTag = "mosaic";
	private BroadCastManager bcm;
	private BroadcastReceiver viewCommandBcr ;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"RemoteControlActivity create");
		super.onCreate(savedInstanceState);

		FBMNetTask.register(this);


		if (savedInstanceState != null){

			currentTag = savedInstanceState.getString("currentTag");
			if (currentTag == null){
				currentTag="mosaic";
			}
			fullscreen = savedInstanceState.getBoolean("fullscreen");
			Log.i(TAG,"Restore savedInstanceState");
			Log.i(TAG,"currentTag = "+currentTag);
			Log.i(TAG,"fullscreen = "+fullscreen);

		}


		bcm = new BroadCastManager(this);
		viewCommandBcr = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "Receiver VIEWCOMMAND");
				if (intent.getExtras() != null){
					String view = intent.getStringExtra("view");
					if (view.compareTo("mosaic") == 0){
						currentTag="mosaic";
					}else{
						currentTag="remote";						
					}

					chooseView();

				}else{
					Log.d(TAG, "Pas de parametre passes a l intent");
				}
			}
		};

		registerReceiver(viewCommandBcr, new IntentFilter("VIEWCOMMAND"));		

	}	


	@Override
	protected void onStop() {
		getWindow().setCallback(null);
		super.onStop();
	}


	@Override
	protected void onPause() {
		this.getWindow().setCallback(null);
		super.onPause();
	}

	private void chooseView() {
		Log.d(TAG, "fullscreen = "+fullscreen);
		Log.d(TAG, "currentTag = "+currentTag);
		liberateView(currentMosaicView);
		liberateView(currentRemoteView);
		liberateView(th);

		if (fullscreen && currentTag != null){

			if (currentTag.compareTo("mosaic") == 0){
				currentMosaicView = createView(getFilesDir()+PATHMOSAICHORIZONTAL, getFilesDir()+ PATHMOSAICVERTICAL, true);
				setContentView(currentMosaicView);					
			}else{
				currentRemoteView = createView(getFilesDir()+PATHREMOTEHORIZONTAL, getFilesDir()+ PATHREMOTEVERTICAL, false);
				setContentView(currentRemoteView);					
			}
		}else{

			loadUI();
			setContentView(th);		

			if (currentTag != null){
				th.setCurrentTabByTag(currentTag);
			}

			th.setOnTabChangedListener(new OnTabChangeListener(){

				@Override
				public void onTabChanged(String tag) {
					currentTag = tag;
				}

			});
			Log.d(TAG, "view : "+th.getCurrentTabTag());

		}
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i(TAG,"onConfigurationChanged Start");
		chooseView();
		super.onConfigurationChanged(newConfig);
	}


	@Override
	public void onStart()
	{
		boolean confOk = false;
		super.onStart();
		Log.i(TAG,"RemoteControlActivity Start");
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);




		if (!isWifiConnected() || getNbCodes()==0){

			AlertDialog d = new AlertDialog.Builder(this).create();
			d.setTitle(getString(R.string.app_name)+" - Telecommande");
			d.setIcon(R.drawable.fm_telecommande);
			d.setMessage(
					"Avant d'utiliser la télécommande, vous devez paramétrer les éléments suivants :\n\n"+
					((!isWifiConnected())?"- Connectez vous en wifi a votre Freebox\n":"")+
					((getNbCodes() == 0)?"- Paramétrer le(s) code(s) du(des) boitier(s)\n":"")+
					"\nQue souhaitez vous faire ?"+
					"\n\n"
			);


			if (!isWifiConnected()){
				d.setButton(DialogInterface.BUTTON_POSITIVE, "Configurer Wifi", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						dialog.dismiss();
					}
				}
				);
			}
			if (getNbCodes() == 0){
				d.setButton(DialogInterface.BUTTON_NEUTRAL, "Configurer Télécommande", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						startActivity(new Intent(getApplicationContext(), Config.class));
					}
				}
				);
			}
			d.setButton(DialogInterface.BUTTON_NEGATIVE, "Retour à l'accueil", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					finish();
				}
			}
			);
			d.setCancelable(false);
			d.show();

		}else{
			confOk = true;
		}



		if (!mgr.getString(KEY_SPLASH_REMOTE, "0").equals(Utils.getFBMVersion(this)))
		{
			displayAboutRemote();
			new DownloadLayout().execute();
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_REMOTE, Utils.getFBMVersion(this));
			editor.commit();
		}


		if (confOk)
		{
			chooseView();
		}		

	}



	protected void onSaveInstanceState(Bundle outState)
	{
		Log.i(TAG,"onSaveInstanceState called");
		outState.putBoolean("fullscreen", fullscreen);
		outState.putString("currentTag", currentTag);
		super.onSaveInstanceState(outState);			
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0, FULLSCREEN, 0, "Mode plein ecran");
		//			menu.add(0, HOME_OPTION_CONFIG, 1, R.string.home_option_config).setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean ever = false;
		if (th != null){
			if (th.getCurrentTabTag().compareTo("remote") != 0 && th.getCurrentTabTag().compareTo("mosaic") != 0 ){
				Log.d(TAG, "current Tab = "+th.getCurrentTabTag());
				menu.findItem(FULLSCREEN).setEnabled(false);
				menu.findItem(FULLSCREEN).setTitle("Mode plein ecran");
				ever = true;
			}
		}
		if (!ever)
		{
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
		case HOME_OPTION_CONFIG:
			startActivity(new Intent(this, Config.class));
			break;
		case FULLSCREEN:
			fullscreen = !fullscreen;
			chooseView();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	//	@Override
	//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	////		if (requestCode == CHOOSELAYOUTREQUESTCODE && resultCode == SAVE){
	//		if (requestCode == CHOOSELAYOUTREQUESTCODE){
	//			loadUI();
	//			if (currentTag != null){
	//				th.setCurrentTabByTag(currentTag);
	//			}
	//		}
	//	}
	//

	private View createView(String pathHorizontal, String pathVertical, boolean mosaicView){

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


			SAXParserFactory fabrique = SAXParserFactory.newInstance();
			String clazzName = null;
			try {

				SAXParser parseur = fabrique.newSAXParser();
				XMLReader xr = parseur.getXMLReader();
				InfoSkinHandler gestionnaire = new InfoSkinHandler();
				xr.setContentHandler(gestionnaire);
				xr.parse(new InputSource(new FileInputStream(info)));
				InfoSkin infoSkin = gestionnaire.getInfoSkin();
				if (info != null)
					clazzName = infoSkin.getClassName();

			} catch (Exception e) {
				e.printStackTrace();
			} 

			Log.d(TAG, "class = "+clazzName);

			String apk = "";
			for (int i=0; i<apks.length; i++){
				apk += dirView.getAbsolutePath()+"/"+apks[i]+":";
			}			

			DexClassLoader dcl = new DexClassLoader(apk, getFilesDir().getAbsolutePath(), null, ClassLoader.getSystemClassLoader());							
			Class<?> clazz = dcl.loadClass(clazzName);

			Object o = clazz.getConstructor(Context.class).newInstance(this);			
			if (mosaicView){
				if ((new ChaineDbAsker(getApplicationContext()).getAllChainesInfo().get("imgs").isEmpty()))
					new UpdateChannels().execute();


				clazz.getMethod("setMosaicInfo", HashMap.class).invoke(o, new ChaineDbAsker(getApplicationContext()).getAllChainesInfo());

			}

			clazz.getMethod("setPathView", String.class).invoke(o, path);			
			v = (ViewGroup)clazz.getMethod("getViewGroup", (Class [])null).invoke(o, (Object[])null);			
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}catch(Exception e){
			e.printStackTrace();
		}

		return v;

	}

	private String setPathWithOrientation(String pathHorizontal, String pathVertical){
		String path;
		Log.d(TAG, "Chemin horizontal = "+pathHorizontal);
		Log.d(TAG, "Chemin vertical = "+pathVertical);

		int orientation = getResources().getConfiguration().orientation;
		Log.d(TAG, "Orientation = "+orientation);
		if (new File(pathHorizontal).exists() && new File(pathVertical).exists()){
			if (orientation == Configuration.ORIENTATION_LANDSCAPE){
				Log.d(TAG, "Orientation = landscape");
				path = pathHorizontal;
			}else{
				Log.d(TAG, "Orientation = portrait");
				path = pathVertical;
			}
		}else if (new File(pathHorizontal).exists()){
			Log.d(TAG, "path = horizontal");
			path = pathHorizontal;			
		}else{
			Log.d(TAG, "path = vertical");
			path = pathVertical;			
		}
		return path;
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

			copyRemoteFile("http://freeboxmobile.googlecode.com/files/skins-v1.xml", f.getPath());
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
					Log.d(TAG, "Skins = "+s);

					String pathInMemory = null;

					String path = downloadSkin(s);
					String parameter = null;
					if (s.isDefaultHorizontal()){
						if (s.getType().compareTo("remote") == 0) {
							pathInMemory = getFilesDir()+PATHREMOTEHORIZONTAL;	
							parameter = "telecHorizontal";
						}else if (s.getType().compareTo("mosaic") == 0) {
							pathInMemory = getFilesDir()+PATHMOSAICHORIZONTAL;							
							parameter = "mosaicHorizontal";

						}
						save(pathInMemory, path, parameter);
					}

					if (s.isDefaultVertical()){
						if (s.getType().compareTo("remote") == 0) {
							pathInMemory = getFilesDir()+PATHREMOTEVERTICAL;							
							parameter = "telecVertical";
						}else if (s.getType().compareTo("mosaic") == 0) {
							pathInMemory = getFilesDir()+PATHMOSAICVERTICAL;							
							parameter = "mosaicVertical";
						}
						save(pathInMemory, path, parameter);
					}

				}
			}else{
				Log.d(TAG, "Liste des skins vide");
			}


			f.delete();
			startActivityForResult(new Intent(getApplicationContext(), RemoteControlChooseLayout.class), 0);
			return null;
		}


		public String downloadSkin(Skin s){
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
			return pathDest;
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


		th = (TabHost)LayoutInflater.from(this).inflate(R.layout.remotecontrol, null);

		if (th.getCurrentTabTag() != null){
			th.clearAllTabs();
		}

		th.setup();




		spec = th.newTabSpec("mosaic").setIndicator("Mosaique",
				res.getDrawable(R.drawable.bouton_pause))
				.setContent(new TabContentFactory() {

					@Override
					public View createTabContent(String tag) {

						currentMosaicView = createView(getFilesDir()+PATHMOSAICHORIZONTAL, getFilesDir()+ PATHMOSAICVERTICAL, true);
						return currentMosaicView;
					}
				});


		th.addTab(spec); 


		spec = th.newTabSpec("remote").setIndicator("Telecommande",
				res.getDrawable(R.drawable.fm_telecommande))
				.setContent(new TabContentFactory() {

					@Override
					public View createTabContent(String tag) {
						currentRemoteView = createView(getFilesDir()+PATHREMOTEHORIZONTAL, getFilesDir()+PATHREMOTEVERTICAL, false);
						return currentRemoteView;
					}
				});


		th.addTab(spec); 

	}

	private void displayAboutRemote()
	{	
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name)+" - Telecommande");
		d.setIcon(R.drawable.fm_telecommande);
		d.setMessage(
				"La telecommande est en version beta.\n\n"+
				"Vous pouvez personnaliser la télécommande comme bon vous semble grâce à l'utilisation de skins."+
				"\n\n"+
				"Pour plus d'infos sur les skins : http://code.google.com/p/freeboxmobile/ (Rubrique Wiki)"+
				"\n\n"+
				"Enjoy :)"
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		}
		);
		d.show();

	}

	private boolean isWifiConnected(){
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()){
			WifiInfo infos = wifi.getConnectionInfo();
			return (infos.getSSID()==null)?false:true;
		}

		return false;
	}

	private int getNbCodes() {

		String boitier1_code = getApplicationContext().getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(BOITIER1_CODE, null);
		Log.d(TAG, "boitier1_code = "+boitier1_code);
		Boolean boitier1_state = getApplicationContext().getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getBoolean(BOITIER1_STATE, false);
		Log.d(TAG, "boitier1_state = "+boitier1_state);
		String boitier2_code = getApplicationContext().getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(BOITIER2_CODE, null);
		Log.d(TAG, "boitier2_code = "+boitier2_code);
		Boolean boitier2_state = getApplicationContext().getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getBoolean(BOITIER2_STATE, false);
		Log.d(TAG, "boitier2_state = "+boitier2_state);
		int nb = 0;
		if (boitier1_state && boitier1_code != null)
			nb++;
		if (boitier2_state && boitier2_code != null)
			nb++;

		return nb;
	}


	private void save(String pathInMemory, String pathInSd, String parameter){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor editor = preferences.edit();

		File skin = new File(pathInMemory);
		File zip = new File(pathInSd);
		if (skin.exists())  Utils.deleteFile(skin);
		skin.mkdirs();
		Utils.unzipFile(pathInSd, skin.getAbsolutePath());
		editor.putString(parameter, zip.getName());

		editor.commit();

	}




	private class UpdateChannels extends FBMNetTask 
	{

		@Override
		protected Integer doInBackground(Void... arg0) {
			updateChaine();
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			FBMNetTask.iProgressDialogDismiss();
			chooseView();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute()
		{
			FBMNetTask.iProgressShow(
					"Mise à jour des chaines",
					"Veuillez patienter svp...",
					R.drawable.fm_infos_adsl);
		}

		private void updateChaine(){
			File file, filen;
			String image, canal;
			int courant = 0;
			ChainesDbAdapter db = new ChainesDbAdapter(RemoteControlActivity.this);
			db.open();

			List<NameValuePair> param = new ArrayList<NameValuePair>();
			param.add(new BasicNameValuePair("ajax","get_chaines"));
			param.add(new BasicNameValuePair("date", new SimpleDateFormat("yyyy-MM-dd HH:00:00").format(new Date())));

			String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(MAGNETO_URL, param, true, true, "UTF8"));
			try {
				JSONObject jObject = new JSONObject(resultat);
				JSONObject jChannelsObject = jObject.getJSONObject("chaines");

				int max = jChannelsObject.length();
				dProgressMessage("Actualisation de la liste des "+max+" chaînes disponibles pour le Guide...",max);
				publishProgress(0);
				for (Iterator <String> it = jChannelsObject.keys() ; it.hasNext() ;)
				{
					publishProgress(courant++);
					int channel_id;
					String channelId = it.next();
					if (channelId.length() > 0)
					{
						channel_id = Integer.parseInt(channelId);
					}
					else
					{
						Log.d(TAG,"ChannelId == -1");
						channel_id = -1;
					}
					JSONObject jChannelObject = jChannelsObject.getJSONObject(channelId);
					image = getJSONString(jChannelObject, "image");
					canal = getJSONString(jChannelObject, "canal");
					// On teste si on a le fichier qui correspond à  la chaine
					file = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES, image);
					if ((file.exists() == true) && (file.length() == 0))
					{
						Log.d(TAG, "File size == 0, deleting... "+image);
						file.delete();
					}
					if (file.exists() == false)
					{
						if (FBMHttpConnection.getFile(file, IMAGES_URL+image, null, false))
						{
							Log.d(TAG, "Downloading logo : "+image);
							filen = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES, canal+".png");
							try
							{
								Log.d(TAG,"Copy file "+image);
								Utils.copyFile(file, filen);
							}
							catch (IOException e)
							{
								Log.d(TAG,"Impossible de copier "+image+" "+canal);
								e.printStackTrace();
							}
						}
					}

					if (db.isGuideChainePresent(channel_id) == 0)
					{
						db.createGuideChaine(
								Integer.parseInt(getJSONString(jChannelObject, "fbx_id")),
								channel_id,
								Integer.parseInt(canal),
								getJSONString(jChannelObject, "name"),
								image
						);
					}				        

				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			publishProgress(-1);

			db.close();

		}



	}

	@Override
	protected void onDestroy() {
		liberateView(currentMosaicView);
		liberateView(currentRemoteView);
		if (bcm != null){
			bcm.destroy();
		}
		unregisterReceiver(viewCommandBcr);

		System.gc();
		super.onDestroy();
	}


	private void liberateView(View v){
		if (v == null) return ;
		if (v instanceof ViewGroup) {
			ViewGroup tmp = (ViewGroup) v;
			int childCount = tmp.getChildCount();
			for (int i=0; i<childCount; i++){
				liberateView(tmp.getChildAt(i));
			}

		}

		v.clearAnimation();
		v.refreshDrawableState();

		if (v instanceof ImageView){
			ImageView ivTmp = (ImageView) v;
			Drawable d = ivTmp.getDrawable();
			if (d != null){
				d.setCallback(null);
				d = null;
			}
		}
		v = null;

	}


}