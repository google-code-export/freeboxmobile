package org.madprod.freeboxmobile.ligne;

import java.net.URI;
import java.util.Map;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.home.ComptesDbAdapter;
import org.madprod.freeboxmobile.mvv.MevoMessage;
import org.xmlrpc.android.XMLRPCClient;

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
	static String DSLAM_Info = "";
	static String DSLAM_Date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FBMHttpConnection.initVars(this, null);
        SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);

        new UpdateCompte().execute(new Payload(mgr.getString(KEY_NRA, "").equals(""), mgr.getString(KEY_TITLE, ""), mgr.getString(KEY_USER, ""), mgr.getString(KEY_PASSWORD, ""), mgr.getString(KEY_NRA, "")));
        setContentView(R.layout.ligne_info);
        setTitle(getString(R.string.app_name)+" - Info Ligne ADSL Freebox");
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
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
        text1 = "\tVotre ligne "+mgr.getString(KEY_USER, "")+" est connectée au central ADSL \"NRA\" "+mgr.getString(KEY_NRA, "")+" ("+DSLAM_Info+") ";
        text1 += "situé à "+mgr.getString(KEY_LINELENGTH, "0")+" mètres de votre Freebox.\n\n";
        text1 += "\tActuellement ("+DSLAM_Date+") les équipements dont vous dépendez ("+mgr.getString(KEY_DSLAM, "")+") fonctionnent correctement.";
        text2 = "Liste des tickets de "+mgr.getString(KEY_DSLAM, "")+" :";
        text3 = "Historique de l'état de "+mgr.getString(KEY_DSLAM, "")+" :";
        t1.setText(text1);
        t2.setText(text2);
        t3.setText(text3);    	
    }

    private void updateInfos(Payload p)
    {
    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
    	String nra = p.nra;
    	String loc = "";
    	try
    	{
			if (!nra.equals(""))
			{
				URI uri = URI.create(FBMHttpConnection.frimousseUrl);
				XMLRPCClient client = new XMLRPCClient(uri);
				Map<String, Object> map = (Map<String, Object>) client.call("getExchangeInfo", nra);
				loc = (String) map.get("localisation");
				Log.d(DEBUGTAG, "XMLRPC : "+map.get("commune")+" "+map.get("localisation"));
				DSLAM_Info = map.get("commune") + (loc.equals("") ? "" : " - "+loc);
				DSLAM_Date = MevoMessage.convertDateTimeHR((String) client.call("getLastDSLAMResultSetDate"));
			}
			else
			{
				Log.d(DEBUGTAG, "Pas de NRA");
			}
    	}
		catch (Exception e)
		{
			Log.e(DEBUGTAG, "updateInfos : " + e.getMessage());
			e.printStackTrace();
		}
    }

    private void saveState(Payload p)
    {
    	ContentValues v = p.result;

    	mDbHelper = new ComptesDbAdapter(this);
    	mDbHelper.open();
    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
    	String user = mgr.getString(KEY_USER, "");
    	Log.d(DEBUGTAG, "User:"+user);
    	Integer rowid = mDbHelper.getIdFromLogin(user);
    	if (rowid != null)
    	{
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
    	}
    	else
    	{
    		Log.d(DEBUGTAG, "saveState : pb : user not found !");
    	}
    }

    private static class Payload
    {
    	private String title;
        private String login;
        private String password;
        private String nra;
        private ContentValues result = null;
        private boolean refresh;
        
        public Payload(boolean refresh, String title, String login, String password, String nra)
        {
        	this.title = title;
        	this.login = login;
        	this.password = password;
        	this.nra = nra;
        	this.refresh = refresh;
        }
    }

    private class UpdateCompte extends AsyncTask<Payload, Void, Payload> implements Constants
    {
    	@Override
    	protected Payload doInBackground(Payload... payload)
    	{
    		if (payload[0].refresh)
    		{
    			payload[0].result = FBMHttpConnection.connectFreeCheck(payload[0].login, payload[0].password);
    			if (payload[0].result != null)
    			{
    				saveState(payload[0]);
    				payload[0].nra = (String) payload[0].result.get(KEY_NRA);
    			}
    		}
   			updateInfos(payload[0]);
    		return payload[0];
    	}

		@Override
    	protected void onPreExecute()
    	{
    		FBMHttpConnection.showProgressDialog2(LigneInfoActivity.this);
    	}

    	@Override
    	protected void onPostExecute(Payload payload)
    	{
    		if ((payload.refresh) && (payload.result != null))
    		{
    			if (payload.result != null)
    			{
//	   				saveState(payload);
	        		FBMHttpConnection.dismissPd();
    			}
    			else
    			{
            		FBMHttpConnection.dismissPd();
       				FBMHttpConnection.showError(LigneInfoActivity.this);    				
    			}
    		}
    		else
    		{
    			FBMHttpConnection.dismissPd();
    		}
			refreshView();
    	}
    }

}
