package org.madprod.freeboxmobile.remotecontrol;

import java.io.BufferedInputStream; 
import java.io.BufferedOutputStream;
import java.io.File;  
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 *
 * @author Clement Beslon
 * $Id: RemoteControlActivity.java 2010-05-04 $
 * 
 */

public class RemoteControlChooseLayout extends Activity implements Constants, RemoteControlActivityConstants
{

	private SharedPreferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		FBMNetTask.register(this);
		setContentView(R.layout.remotecontrol_chooselayout);
		setTitle(getString(R.string.app_name)+" - Gestion des skins");
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		String[] mosaicFiles = listFiles(Environment.getExternalStorageDirectory()+DIR_FBM+".skins/mosaic");
		Spinner spin = null;
		if (mosaicFiles != null){
			ArrayAdapter<String> aa = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,mosaicFiles);
			aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			spin = (Spinner)findViewById(R.id.MosaicLayoutHorizontalSpinner);
			spin.setAdapter(aa);
			String mosaichorizontal = preferences.getString("mosaicHorizontal", "no");
			int position;
			if ((position = Arrays.asList(mosaicFiles).indexOf(mosaichorizontal)) != -1){
				spin.setSelection(position);
			}					



			spin = (Spinner)findViewById(R.id.MosaicLayoutVerticalSpinner);		
			spin.setAdapter(aa);
			String mosaicvertical = preferences.getString("mosaicVertical", "no");
			if ((position = Arrays.asList(mosaicFiles).indexOf(mosaicvertical)) != -1){
				spin.setSelection(position);
			}					



		}
		String[] telecommandeFiles = listFiles(Environment.getExternalStorageDirectory()+DIR_FBM+".skins/remote");
		if (telecommandeFiles != null){

			ArrayAdapter<String> aa = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,telecommandeFiles);
			aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			spin = (Spinner)findViewById(R.id.TelecommandeLayoutHorizontalSpinner);		
			spin.setAdapter(aa);
			String telechorizontal = preferences.getString("telecHorizontal", "no");
			int position;
			if ((position = Arrays.asList(telecommandeFiles).indexOf(telechorizontal)) != -1){
				spin.setSelection(position);
			}					


			spin = (Spinner)findViewById(R.id.TelecommandeLayoutVerticalSpinner);		
			spin.setAdapter(aa);
			String telecvertical = preferences.getString("telecVertical", "no");
			if ((position = Arrays.asList(telecommandeFiles).indexOf(telecvertical)) != -1){
				spin.setSelection(position);
			}					
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SAVE, 0, "Sauvegarder");
		menu.add(0, CANCEL, 0, "Annuler");
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case SAVE:

			Editor editor = preferences.edit();
			File dirSkins = new File(getFilesDir()+"/skins");
			if (!dirSkins.exists())	dirSkins.mkdirs();

			File skin;

			Spinner spin = (Spinner)findViewById(R.id.MosaicLayoutHorizontalSpinner);		
			skin = new File(getFilesDir()+PATHMOSAICHORIZONTAL);
			if (skin.exists())  deleteFile(skin);
			if (spin.getSelectedItem() != null){
				skin.mkdirs();
				new UnzipFile().execute(Environment.getExternalStorageDirectory()+DIR_FBM+".skins/mosaic/"+spin.getSelectedItem(), skin.getAbsolutePath());
				editor.putString("mosaicHorizontal", ""+spin.getSelectedItem());
			}else{
				editor.remove("mosaicHorizontal");
			}


			spin = (Spinner)findViewById(R.id.MosaicLayoutVerticalSpinner);		
			skin = new File(getFilesDir()+PATHMOSAICVERTICAL);
			if (skin.exists())  deleteFile(skin);
			if (spin.getSelectedItem() != null){
				skin.mkdirs();
				new UnzipFile().execute(Environment.getExternalStorageDirectory()+DIR_FBM+".skins/mosaic/"+spin.getSelectedItem(), skin.getAbsolutePath());
				editor.putString("mosaicVertical", ""+spin.getSelectedItem());
			}else{
				editor.remove("mosaicVertical");
			}

			spin = (Spinner)findViewById(R.id.TelecommandeLayoutHorizontalSpinner);		
			skin = new File(getFilesDir()+PATHREMOTEHORIZONTAL);
			if (skin.exists())  deleteFile(skin);
			if (spin.getSelectedItem() != null){
				skin.mkdirs();
				new UnzipFile().execute(Environment.getExternalStorageDirectory()+DIR_FBM+".skins/remote/"+spin.getSelectedItem(), skin.getAbsolutePath());
				editor.putString("telecHorizontal", ""+spin.getSelectedItem());
			}else{
				editor.remove("telecHorizontal");
			}

			spin = (Spinner)findViewById(R.id.TelecommandeLayoutVerticalSpinner);		
			skin = new File(getFilesDir()+PATHREMOTEVERTICAL);
			if (skin.exists())  deleteFile(skin);
			if (spin.getSelectedItem() != null){
				skin.mkdirs();
				new UnzipFile().execute(Environment.getExternalStorageDirectory()+DIR_FBM+".skins/remote/"+spin.getSelectedItem(), skin.getAbsolutePath());
				editor.putString("telecVertical", ""+spin.getSelectedItem());
			}else{
				editor.remove("telecVertical");
			}
			editor.commit();

			setResult(SAVE);
			break;
		case CANCEL:
			setResult(CANCEL);
			break;

		}
		finish();
		return super.onOptionsItemSelected(item);
	}




	private void deleteFile(File path) {
		if (path.exists()){
			if (!path.delete()){
				File[] files = path.listFiles(); 
				for(int i=0; i<files.length; i++) { 
					if(files[i].isDirectory()) { 
						deleteFile(files[i]); 
					} 
					else { 
						files[i].delete(); 
					} 
				} 
			}
		}
	}


	@Override
	public void onDestroy()
	{
		FBMNetTask.unregister(this);
		super.onDestroy();
	}


	public String[] listFiles(String path){
		File rep = new File(path);
		Log.e(TAG, "path = "+path);
		if (!(rep.exists() && rep.isDirectory())){
			if (!rep.mkdirs()){
				Toast.makeText(getApplicationContext(), "Impossible de creer le repertoire "+path+" sur la carte sd", Toast.LENGTH_LONG).show();
				finish();
			}
		}
		return rep.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".zip"))
					return true;
				return false;
			}
		});


	}




	private class UnzipFile extends AsyncTask<String, Void, Void> 
	{


		@Override
		protected Void doInBackground(String... params) {
			if (params.length == 2){
				unzipFile(params[0], params[1]);
			}
			return null;
		}

		@Override
		protected void onPreExecute()
		{
			FBMNetTask.iProgressShow(
					"Installation du skin",
					"Installation en cours...",
					R.drawable.rc_zip);
		}

		@Override
		protected void onPostExecute(Void result) {
    		FBMNetTask.iProgressDialogDismiss();
		}		


		public void unzipFile(String pathZipFile, String path){
			try {
				final int BUFFER = 2048;
				byte data[] = new byte[BUFFER];
				BufferedOutputStream dest = null;
				FileInputStream zipFile = new FileInputStream(pathZipFile);
				BufferedInputStream buffZip = new BufferedInputStream(zipFile);
				ZipInputStream zis = new ZipInputStream(buffZip);
				ZipEntry entree;
				int count;
				while((entree = zis.getNextEntry()) != null) {
					Log.e(TAG, "entree = "+entree);
					File f = new File(path+"/"+entree.getName());
					if (entree.isDirectory()){
						if (!f.exists()){
							f.mkdir();
						}
					}else{
						FileOutputStream fos = new FileOutputStream(f);
						dest = new BufferedOutputStream(fos, BUFFER);
						while ((count = zis.read(data, 0, BUFFER)) != -1) {
							dest.write(data, 0, count);
						}
						dest.flush();
						dest.close();
					}
				}

				zis.close();

			} catch (IOException e2) {
				e2.printStackTrace();
			}


		}

	}
}