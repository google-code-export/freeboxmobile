package org.madprod.freeboxmobile.fax;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class FaxActivity extends Activity implements FaxConstants {
	
	//Fichier selectionné
	private File selectedFile = null;
	
	private EditText numberBox;
	private CheckBox emailAckCheck;
	private CheckBox maskNumberCheck;

	/**
	 * Création de l'activité d'envoi d'un fax via le compte Free actuellement utilisé
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FBMHttpConnection.initVars(this,null);
		
		this.setContentView(R.layout.fax);

		numberBox = (EditText) findViewById(R.id.faxNumber);
		emailAckCheck = (CheckBox)findViewById(R.id.faxSendMailBox);
		maskNumberCheck = (CheckBox)findViewById(R.id.faxMaskMyNumberBox);
		
		final Button chooseFileButton = (Button) findViewById(R.id.chooseFileButton);
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
	}
	
	@Override
	protected void onDestroy() {
		FBMHttpConnection.closeDisplay();
		super.onDestroy();
	}
	
	/**
	 * Traitement de la demande de selection de fichier (clic sur le bouton "Choisir")
	 */
	private void fileSelection(){
		final Intent i = new Intent(this,FileChooserActivity.class);
    	startActivityForResult(i, FileChooserActivity.PICK_FILE_REQUEST);
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
		case FileChooserActivity.PICK_FILE_REQUEST:
			if(resultCode == 1 && data != null && data.getData()!=null){
				selectFile(new File(data.getData().getPath()));
			}else{
				Log.d(DEBUGTAG, "Ignoring FileChooserActivity result Intent "+data);
			}
		}
	}
	
	/*
	 * Changement de la selection de fichier courante
	 */
	private void selectFile(File file) {
		selectedFile = file;
		String message = getString(R.string.faxNoFile);
		if(selectedFile != null){
			message = getString(R.string.faxFileNamePrefix);
			if(selectedFile.getName().length()<13){
				message += selectedFile.getName();
			}else{
				message += selectedFile.getName().substring(0, 10)+getString(R.string.faxFileNameSuffix);
			}
		}
		EditText faxFileLabel = (EditText) findViewById(R.id.faxFilePath);
		faxFileLabel.setText(message);
	}

	/*
	 * Envoi du fichier actuellement selectionné
	 */
	private void send() {
		if (validate()) {
			new FaxFile(this).execute(selectedFile);
		}
	}

	/*
	 * Validation du formulaire avant envoi du fichier en POST
	 */
	private boolean validate() {
		return selectedFile != null
				&& !"".equals(numberBox.getText().toString().trim());
	}
	
	class FaxFile extends AsyncTask<File, Void, String> implements Constants
    {
		private FaxActivity owner;
		private ProgressDialog myProgressDialog;
		
		public FaxFile(FaxActivity owner) {
			this.owner = owner;
		}

		@Override
    	protected void onPreExecute()
    	{
    		myProgressDialog = ProgressDialog.show(owner, "FreeFax", getString(R.string.faxSendingFile),false,false);
    	}

		@Override
		protected String doInBackground(File... files) {
			return doPostFile(files[0]);
		}
		
		private String doPostFile(File file) {
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
			} catch (IOException e) {
				return null;
			}
		}
			
		@Override
    	protected void onPostExecute(String payload)
    	{
			myProgressDialog.dismiss();
			if(payload == null){
				Toast.makeText(owner, getString(R.string.faxError), Toast.LENGTH_LONG).show();
			}else if(!"".equals(payload.trim())){
				WebView resultView = new WebView(owner);
				resultView.loadData(payload+"", "text/html", "UTF-8");
				owner.setContentView(resultView);
			}else{
				Toast.makeText(owner, getString(R.string.faxSuccess), Toast.LENGTH_LONG).show();
			}
    	}
    }
}
