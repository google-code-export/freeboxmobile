package org.madprod.freeboxmobile.fax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;

import static org.madprod.freeboxmobile.StaticConstants.TAG;
import static org.madprod.freeboxmobile.StaticConstants.KEY_PREFS;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts.Phones;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.Toast;

public class FaxActivity extends Activity implements FaxConstants {
	public static final int PICK_FILE 		= 1;
	public static final int PICK_CONTACT 	= 2;
	
	private EditText numberBox;
	private CheckBox emailAckCheck;
	private CheckBox maskNumberCheck;
	private EditText faxFileLabel;
	private EditText filePathText;

	private File selectedFile = null;
	
	GoogleAnalyticsTracker tracker;
	
	/**
	 * Création de l'activité d'envoi d'un fax via le compte Free actuellement utilisé
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Fax/HomeFax");
		FBMNetTask.register(this);
		this.setContentView(R.layout.fax);

		numberBox = (EditText) findViewById(R.id.faxNumber);
		emailAckCheck = (CheckBox)findViewById(R.id.faxSendMailBox);
		maskNumberCheck = (CheckBox)findViewById(R.id.faxMaskMyNumberBox);
		faxFileLabel = (EditText) findViewById(R.id.faxFilePath);
		filePathText = (EditText)findViewById(R.id.faxFilePath);
		ImageButton faxNumberPicker=(ImageButton)findViewById(R.id.faxNumberPicker);
		faxNumberPicker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				faxNumberSelection();
			}
		});
		
		filterIntent();
		
		final ImageButton chooseFileButton = (ImageButton) findViewById(R.id.chooseFileButton);
		chooseFileButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				fileSelection();
			}
		});
		final Button sendFaxButton = (Button) findViewById(R.id.sendFaxButton);
		sendFaxButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				send();
			}
		});
		
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		if (!mgr.getString(KEY_SPLASH_FAX, "0").equals(Utils.getFBMVersion(this)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_FAX, Utils.getFBMVersion(this));
			editor.commit();
			displayInfos();
		}
	}

	/**
	 * Filtre l'action demandée par une autre application si elle existe
	 */
	private final void filterIntent(){
		if(getIntent()!=null && getIntent().getAction() != null){
			if(getIntent().getAction().equals(Intent.ACTION_SEND)){
				if("text/plain".equals(getIntent().getType())){
					//Cas de l'envoi d'un texte saisi par l'utilisateur ou copié depuis une application
					final String text = (String) getIntent().getExtras().get(Intent.EXTRA_TEXT);
					Log.d(TAG,"Fax du texte : "+text);
					try {
						final File tempFile = File.createTempFile("fax", "txt");
						final FileWriter writer = new FileWriter(tempFile);
						writer.write(text);
						writer.close();
						selectFile(tempFile);
					} catch (IOException e) {
						Log.e(TAG,"Erreur pendant le fax de "+text+" ["+e.getLocalizedMessage()+"]");
						e.printStackTrace();
					}
				}else{
					final Uri uri = (Uri)getIntent().getExtras().get(Intent.EXTRA_STREAM);
					if(uri!=null){
						filterUriIntent(uri);
					}
				}
			}
		}
	}
	
	/**
	 * Traitement d'une URI fournie avec dans l'EXTRA_STREAM d'un Intent SEND 
	 * (pointeur vers un fichier à faxer)
	 * @param uri
	 */
	private void filterUriIntent(Uri uri){
		//OI File Manager send action || media Image 
		Cursor openIntentFileCursor = getContentResolver().query(uri, null, null, null, null);
		if (openIntentFileCursor.moveToFirst()) {
			String filePath = openIntentFileCursor.getString(openIntentFileCursor.getColumnIndex(Images.Media.DATA));
			selectFile(new File(filePath));
		}
	}
	
	@Override
	protected void onDestroy() {
		FBMNetTask.unregister(this);
		tracker.stop();
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("selectedFile", (selectedFile!=null)?selectedFile.getAbsolutePath():null);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		final String previouslySelectedFile = savedInstanceState.getString("selectedFile");
		selectedFile = null;
		if(previouslySelectedFile!=null){
			selectFile(new File(previouslySelectedFile));
		}
	}
	
	/**
	 * Traitement de la demande de selection de fichier (clic sur le bouton "Choisir")
	 */
	private void fileSelection(){
		final Intent i = new Intent(this,FileChooserActivity.class);
    	startActivityForResult(i, PICK_FILE);
	}
	
	private void faxNumberSelection(){
		Intent i = new Intent(Intent.ACTION_PICK,Phones.CONTENT_URI);
    	startActivityForResult(i, PICK_CONTACT);
	}

    private void displayInfos()
    {
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name)+" Fax");
		d.setIcon(R.drawable.icon_fbm);
    	d.setMessage(
			"Le module Fax de Freebox Mobile vous permet d'envoyer des Fax en utilisant votre compte Freebox (donc gratuit vers les numéros de fixe).\n\n"+
			"Vous devez choisir ou saisir un numéro de fax ainsi qu'un fichier présent sur votre carte mémoire. Les formats supportés sont :\n"+
			"- pdf\n"+
			"- jpeg / jpg / png\n"+
			"- doc / docx / xls / xlsx / odt / ods\n"+
			"- txt\n\n"+
			"Grâce à Freebox Mobile vous pouvez aussi envoyer des fax directement à partir d'autres applications comme :\n"+
			"- OI File Manage\n"+
			"- Chrome (Navigateur)\n"+
			"- K9 Mail\n"+
			"- Astro... \n"+
			"Consultez www.freeboxmobile.org pour plus d'informations.\n "
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
		d.show();
    }
    
	/*
	 * Traitement du retour de l'activité de selection d'un fichier sur la carte SD
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case PICK_FILE:
			if(resultCode == Activity.RESULT_OK && data != null && data.getData()!=null){
				selectFile(new File(data.getData().getPath()));
			}else{
				Log.d(TAG, "Ignoring FileChooserActivity result Intent "+data);
			}
			break;
		case PICK_CONTACT:
			if(resultCode == Activity.RESULT_OK && data != null && data.getData()!=null){
				final Uri contactData = data.getData();
		        final Cursor c =  managedQuery(contactData, null, null, null, null);
			        if (c.moveToFirst()) {
			        	final String originalNumber = c.getString(c.getColumnIndexOrThrow(Phones.NUMBER));
			        	numberBox.setText(originalNumber.trim().replace(" ", ""));
			        }
			    break;
			}else{
				Log.d(TAG, "Ignoring Fax Number pick activity result Intent "+data);
			}
		}
	}
	
	/*
	 * Changement de la selection de fichier courante
	 */
	private void selectFile(File file) {
		selectedFile = file;
		
		File selectedFile = this.selectedFile;
		String message = getString(R.string.faxNoFile);
		if(selectedFile != null){
			message = "";
			faxFileLabel.setError(null);
			if(selectedFile.getName().length()<13){
				message += selectedFile.getAbsolutePath();
			}else{
				message += selectedFile.getAbsolutePath().substring(0, 10)+getString(R.string.faxFileNameSuffix);
			}
		}
		faxFileLabel.setText(message);
	}

	/*
	 * Envoi du fichier actuellement selectionn�
	 */
	private void send() {
		if (validate()) {
			final File selectedFile = this.selectedFile;
			new FaxFileNotification(this).execute(selectedFile);
			Toast.makeText(this, getString(R.string.faxDemandOK,selectedFile.getName()), 2000).show();
			setResult(Activity.RESULT_OK, new Intent(Intent.ACTION_SEND, Uri.fromFile(selectedFile)));
			finish();
		}
	}

	/*
	 * Validation du formulaire avant envoi du fichier en POST
	 */
	private boolean validate() {
		boolean isValid = true;
		if(selectedFile == null){
			isValid = false;
			filePathText.setError(getString(R.string.faxErrorNoFile));
		}
		if("".equals(numberBox.getText().toString().trim())){
			isValid = false;
			numberBox.setError(getString(R.string.faxErrorNumberLabel));
		}
		return isValid;
	}
	
	private class FaxFileNotification extends AsyncTask<File, Integer, String> implements Constants
    {
		private FaxActivity owner;
		private RemoteViews views;
		private Notification notification;
		private NotificationManager mNotificationManager;
		private String fileName;
		
		public FaxFileNotification(FaxActivity owner) {
			this.owner = owner;
		}

		@Override
    	protected void onPreExecute()
    	{
			final String ns = Context.NOTIFICATION_SERVICE;
			mNotificationManager = (NotificationManager) getSystemService(ns);
			final int icon = R.drawable.icon_fbm;
			final CharSequence tickerText = getString(R.string.faxStartNotificationTicker);
			final long when = System.currentTimeMillis();
			notification = new Notification(icon, tickerText, when);
			
			final Intent notificationIntent = new Intent(owner, FaxActivity.class);
			final PendingIntent contentIntent = PendingIntent.getActivity(owner, 0, notificationIntent, 0);

			views = new RemoteViews(owner.getPackageName(), R.layout.fax_notification_layout);
			views.setProgressBar(R.id.notificationProgress, 100, 0, false);
			views.setTextViewText(R.id.notificationText, "Fax en cours");
			
			notification.contentIntent = contentIntent;
			notification.contentView = views;
			mNotificationManager.notify(R.layout.fax_notification_layout, notification);
    	}

		@Override
		protected String doInBackground(File... files) {
			return doPostFile(files[0]);
		}
		
		private String doPostFile(File file) {
			fileName = file.getName();
			views.setTextViewText(R.id.notificationText, getString(R.string.faxSendingFile,fileName,numberBox.getText().toString().trim()));
			notification.contentView = views;
			mNotificationManager.notify(R.layout.fax_notification_layout, notification);
			final List<NameValuePair> params = new ArrayList<NameValuePair>();
			if(maskNumberCheck.isChecked()){
				params.add(new BasicNameValuePair("masque","Y"));
			}
			if(emailAckCheck.isChecked()){
				params.add(new BasicNameValuePair("email_ack","1"));
			}
			params.add(new BasicNameValuePair("destinataire",numberBox.getText().toString().trim()));
			try {
				return FBMHttpConnection.postFileAuthRequest(UPLOAD_FAX_URL,params,file,HttpURLConnection.HTTP_MOVED_TEMP,true);
			} catch (FileNotFoundException fileNotFound){
				return null;
			} catch (IOException e) {
				return null;
			}
		}
		
		private void notifyResult(String message){
			final int icon = R.drawable.icon_fbm;
			final long when = System.currentTimeMillis();
			final Notification notification = new Notification(icon, message, when);
			final Intent notificationIntent = new Intent(owner, FaxActivity.class);
			final PendingIntent contentIntent = PendingIntent.getActivity(owner, 0, notificationIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(), "Fax Freebox",message , contentIntent);
			mNotificationManager.notify(R.layout.fax_notification_layout, notification);
		}
			
		@Override
    	protected void onPostExecute(String payload)
    	{
			views.setViewVisibility(R.id.notificationProgressBarContainer,View.INVISIBLE);
			if(payload == null){
				notifyResult(getString(R.string.faxError,fileName));
			}else if(!"".equals(payload.trim())){
				notifyResult(getString(R.string.faxError,fileName));
			}else{
				notifyResult(getString(R.string.faxSuccess,fileName));
			}
    	}
    }
}
