package org.madprod.freeboxmobile.remotecontrol;

import java.io.BufferedInputStream; 
import java.io.BufferedOutputStream;
import java.io.File;  
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

/**
 *
 * @author Clement Beslon
 * $Id: RemoteControlActivity.java 2010-05-04 $
 * 
 */

public class RemoteControlListLayout extends ListActivity implements Constants, RemoteControlActivityConstants
{

	private SharedPreferences preferences;
	private String pathInSd = null;
	private String pathInInternalMemory = null;
	private String preferenceName = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		FBMNetTask.register(this);
		setContentView(R.layout.remotecontrol_listlayout);
		setTitle(getString(R.string.app_name)+" - Gestion des skins");

		

		
		
		
		if (getIntent().getExtras() != null){
			String type = getIntent().getStringExtra("type");			
			String sens = getIntent().getStringExtra("sens");
			
			if (type.compareTo("remote") == 0){
				pathInSd = Environment.getExternalStorageDirectory()+DIR_FBM+PATHREMOTESDCARD;
				if (sens.compareTo("horizontal") == 0){
					pathInInternalMemory = getFilesDir()+PATHREMOTEHORIZONTAL;
					preferenceName = "telecHorizontal";					
				}else{
					pathInInternalMemory = getFilesDir()+PATHREMOTEVERTICAL;
					preferenceName = "telecVertical";
				}
				
			}else{
				pathInSd = Environment.getExternalStorageDirectory()+DIR_FBM+PATHMOSAICSDCARD;
				if (sens.compareTo("horizontal") == 0){
					pathInInternalMemory = getFilesDir()+PATHMOSAICHORIZONTAL;
					preferenceName = "mosaicHorizontal";					
				}else{
					pathInInternalMemory = getFilesDir()+PATHMOSAICVERTICAL;
					preferenceName = "mosaicVertical";
				}
			}
		}
		
		
		
		
		
		setListAdapter(new myAdapter(pathInSd, this));

		
		
		//		
		//
		//		String[] mosaicFiles = listFiles(Environment.getExternalStorageDirectory()+DIR_FBM+".skins/mosaic");
		//		Spinner spin = null;
		//		if (mosaicFiles != null){
		//			ArrayAdapter<String> aa = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,mosaicFiles);
		//			aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//
		//			spin = (Spinner)findViewById(R.id.MosaicLayoutHorizontalSpinner);
		//			spin.setAdapter(aa);
		//			String mosaichorizontal = preferences.getString("mosaicHorizontal", "no");
		//			int position;
		//			if ((position = Arrays.asList(mosaicFiles).indexOf(mosaichorizontal)) != -1){
		//				spin.setSelection(position);
		//			}					
		//
		//
		//
		//			spin = (Spinner)findViewById(R.id.MosaicLayoutVerticalSpinner);		
		//			spin.setAdapter(aa);
		//			String mosaicvertical = preferences.getString("mosaicVertical", "no");
		//			if ((position = Arrays.asList(mosaicFiles).indexOf(mosaicvertical)) != -1){
		//				spin.setSelection(position);
		//			}					
		//
		//
		//
		//		}
		//		String[] telecommandeFiles = listFiles(Environment.getExternalStorageDirectory()+DIR_FBM+".skins/remote");
		//		if (telecommandeFiles != null){
		//
		//			ArrayAdapter<String> aa = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,telecommandeFiles);
		//			aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//
		//			spin = (Spinner)findViewById(R.id.TelecommandeLayoutHorizontalSpinner);		
		//			spin.setAdapter(aa);
		//			String telechorizontal = preferences.getString("telecHorizontal", "no");
		//			int position;
		//			if ((position = Arrays.asList(telecommandeFiles).indexOf(telechorizontal)) != -1){
		//				spin.setSelection(position);
		//			}					
		//
		//
		//			spin = (Spinner)findViewById(R.id.TelecommandeLayoutVerticalSpinner);		
		//			spin.setAdapter(aa);
		//			String telecvertical = preferences.getString("telecVertical", "no");
		//			if ((position = Arrays.asList(telecommandeFiles).indexOf(telecvertical)) != -1){
		//				spin.setSelection(position);
		//			}					
		//		}
		//		
		//		Button cancel = (Button)findViewById(R.id.layout_button_cancel);
		//		cancel.setOnClickListener(new OnClickListener(){
		//			@Override
		//			public void onClick(View arg0) {
		//				cancel();
		//			}
		//		});
		//
		//		
		//		Button valider = (Button)findViewById(R.id.layout_button_ok);
		//		valider.setOnClickListener(new OnClickListener(){
		//			@Override
		//			public void onClick(View arg0) {
		//				save();
		//			}
		//		});

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		displayDialog(v);
		
		
	}

	
    private void displayDialog(View v)
    {
		
    	AlertDialog d = new AlertDialog.Builder(this).create();
    	final String name = ""+((TextView)v.findViewById(R.id.listLayoutName)).getText();
    	
    	d.setTitle("Skin : "+name);
		d.setIcon(R.drawable.fm_telecommande);
    	d.setMessage("Souhaitez vous installer le skin selectionné ?");
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Oui", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					save(pathInInternalMemory, pathInSd,name, preferenceName);
					finish();
				}
			});
		d.setButton(DialogInterface.BUTTON_NEGATIVE, "Non", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		d.show();
    }





	@Override
	public void onDestroy()
	{
		FBMNetTask.unregister(this);
		super.onDestroy();
	}


	private void save(String pathInMemory, String pathInSd, String file, String parameter){
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = preferences.edit();

		File skin = new File(pathInMemory);
		if (skin.exists())  Utils.deleteFile(skin);
		if (file != null){
			skin.mkdirs();
			new UnzipFile().execute(pathInSd+"/"+file, skin.getAbsolutePath());
			editor.putString(parameter, file);
		}else{
			editor.remove(parameter);
		}

		editor.commit();

		setResult(SAVE);
		finish();
	}





	public class UnzipFile extends AsyncTask<String, Void, Void> 
	{


		@Override
		protected Void doInBackground(String... params) {
			if (params.length == 2){
				Utils.unzipFile(params[0], params[1]);
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




	}




	class myAdapter extends BaseAdapter{

		private final File[] files ;
		private final Context context;

		public myAdapter(String path, Context c) {
			super();
			files = listFiles(path);
			context = c;
		}		

		@Override
		public int getCount() {
			return files.length;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			if (convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.remotecontrol_listlayoutline, null);

			}
			ImageView iv = (ImageView)convertView.findViewById(R.id.listLayoutIV);
			iv.setAdjustViewBounds(true);
			iv.setMaxHeight(80);
			iv.setMaxWidth(80);
			iv.setScaleType(ScaleType.FIT_CENTER);

			final int BUFFER = 2048;
			byte data[] = new byte[BUFFER];
			BufferedOutputStream dest = null;

			iv.setImageResource(R.drawable.freebox_big_claire);
			((TextView)convertView.findViewById(R.id.listLayoutSize)).setText(files[position].length()+" bytes");
			((TextView)convertView.findViewById(R.id.listLayoutName)).setText(files[position].getName());
		

			try{
				
				FileInputStream zipFile = new FileInputStream(files[position]);
				BufferedInputStream buffZip = new BufferedInputStream(zipFile);
				ZipInputStream zis = new ZipInputStream(buffZip);
				ZipEntry entree;
				int count;
				while((entree = zis.getNextEntry()) != null ) {
					Log.d(TAG, "entree = "+entree);
					File f = File.createTempFile("tmp", "FBM");
					FileOutputStream fos = new FileOutputStream(f);
					dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = zis.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
					
					if (entree.getName().compareTo("skin.png") == 0){
						iv.setImageDrawable(Drawable.createFromPath(f.getAbsolutePath()));
					}else if (entree.getName().compareTo("info") == 0){
						SAXParserFactory fabrique = SAXParserFactory.newInstance();

						try {

							SAXParser parseur = fabrique.newSAXParser();
							XMLReader xr = parseur.getXMLReader();
							InfoSkinHandler gestionnaire = new InfoSkinHandler();
							xr.setContentHandler(gestionnaire);
							xr.parse(new InputSource(new FileInputStream(f)));
							InfoSkin info = gestionnaire.getInfoSkin();
							if (info != null)
							((TextView)convertView.findViewById(R.id.listLayoutDesc)).setText(info.getDescription());
												
						} catch (Exception e) {
							e.printStackTrace();
						} 

					}
					
					f.delete();
				}
				zis.close();

			} catch (IOException e2) {
				e2.printStackTrace();
			}
			return convertView;
		}


		public File[] listFiles(String path){
			File rep = new File(path);
			Log.d(TAG, "path = "+path);
			if (!(rep.exists() && rep.isDirectory())){
				if (!rep.mkdirs()){
					Toast.makeText(context, "Impossible de creer le repertoire "+path+" sur la carte sd", Toast.LENGTH_LONG).show();
					finish();
				}
			}
			return rep.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					if (filename.endsWith(".zip"))
						return true;
					return false;
				}
			});
		}


	}

}



