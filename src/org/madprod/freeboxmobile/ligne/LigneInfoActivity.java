package org.madprod.freeboxmobile.ligne;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.home.ComptesDbAdapter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class LigneInfoActivity extends Activity implements Constants
{
	private ComptesDbAdapter mDbHelper;
	//static Activity a;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //a = this;
        FBMHttpConnection.initVars(this, null);
        SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        
        if (mgr.getString(KEY_NRA, "").equals(""))
        {
        	Log.d(DEBUGTAG, "REFRESH");
//        	mDbHelper = new ComptesDbAdapter(this);
//            mDbHelper.open();
            new UpdateCompte().execute(new Payload(mgr.getString(KEY_TITLE, ""), mgr.getString(KEY_USER, ""), mgr.getString(KEY_PASSWORD, "")));
		}
        setContentView(R.layout.ligne_info);
        setTitle(getString(R.string.app_name)+" - Info Ligne ADSL Freebox");
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	refreshView();
    }

    private void refreshView()
    {
    	String text1;
    	String text2;
    	String text3;

    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        TextView t1 = (TextView) findViewById(R.id.infoLigne1);
        TextView t2 = (TextView) findViewById(R.id.infoLigne2);
        TextView t2_1 = (TextView) findViewById(R.id.infoLigne2_1);
        TextView t3 = (TextView) findViewById(R.id.infoLigne3);
        TextView t3_1 = (TextView) findViewById(R.id.infoLigne3_1);
        text1 = "\tVotre ligne "+mgr.getString(KEY_USER, "")+" est connectée au central ADSL \"NRA\" "+mgr.getString(KEY_NRA, "")+" (Pérols) ";
        text1 += "situé à "+mgr.getString(KEY_LINELENGTH, "0")+" mètres de votre Freebox.\n\n";
        text1 += "\tActuellement (2010-01-25 19:00:00) les équipements dont vous dépendez ("+mgr.getString(KEY_DSLAM, "")+") fonctionnent correctement.";
        text2 = "Liste des tickets de "+mgr.getString(KEY_DSLAM, "")+" :";
        text3 = "Historique de l'état de "+mgr.getString(KEY_DSLAM, "")+" :";
        t1.setText(text1);
        t2.setText(text2);
        t3.setText(text3);    	
    }

    private void saveState(Payload p)
    {
    	ContentValues v = p.result;

    	mDbHelper = new ComptesDbAdapter(this);
    	mDbHelper.open();
    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
    	int rowid = mDbHelper.getIdFromLogin(mgr.getString(KEY_USER, ""));
    	mDbHelper.updateCompte(rowid, p.title, p.login, p.password, (String) v.get(KEY_NRA),
    			(String) v.get(KEY_DSLAM), (String) v.get(KEY_IP), (String) v.get(KEY_LINELENGTH),
    			(String) v.get(KEY_ATTN), (String) v.get(KEY_TEL));
		Editor editor = mgr.edit();
		editor.putString(KEY_NRA, (String) v.get(KEY_NRA));
		editor.putString(KEY_DSLAM, (String) v.get(KEY_DSLAM));
		editor.putString(KEY_IP, (String) v.get(KEY_IP));
		editor.putString(KEY_TEL, (String) v.get(KEY_TEL));
		editor.putString(KEY_LINELENGTH, (String) v.get(KEY_LINELENGTH));
		editor.putString(KEY_ATTN, (String) v.get(KEY_ATTN));
		editor.commit();
		refreshView();
    }

    public static class Payload
    {
    	public String title;
        public String login;
        public String password;
        public ContentValues result;
        
        public Payload(String title, String login, String password)
        {
        	this.title = title;
        	this.login = login;
        	this.password = password;
        }
    }

    private class UpdateCompte extends AsyncTask<Payload, Void, Payload> implements Constants
    {
    	@Override
    	protected Payload doInBackground(Payload... payload)
    	{
    		payload[0].result = FBMHttpConnection.connectFreeCheck(payload[0].login, payload[0].password);
    		return payload[0];
    	}

		@Override
    	protected void onPreExecute()
    	{
    		FBMHttpConnection.showProgressDialog(LigneInfoActivity.this);
    	}

    	@Override
    	protected void onPostExecute(Payload payload)
    	{
    		FBMHttpConnection.dismissPd();
    		if (payload.result != null)
    		{
   				saveState(payload);
    		}
    		else
    		{
   				FBMHttpConnection.showError(LigneInfoActivity.this);
    		}
    	}
    }

}
