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
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class LigneInfoActivity extends Activity implements LigneInfoConstants
{
	String DSLAM_Info = "";
	String DSLAM_Date = "";
	boolean DSLAM_ok = false;
	Cursor mTicketCursor; 

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

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, LIGNEINFO_OPTION_REFRESH, 0, R.string.mevo_option_update).setIcon(android.R.drawable.ic_menu_rotate);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
	        case LIGNEINFO_OPTION_REFRESH:
	        	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
	        	new UpdateCompte().execute(new Payload(mgr.getString(KEY_NRA, "").equals(""), mgr.getString(KEY_TITLE, ""), mgr.getString(KEY_USER, ""), mgr.getString(KEY_PASSWORD, ""), mgr.getString(KEY_NRA, "")));
	        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayTicket(long l)
    {
    	Log.d(DEBUGTAG, "Fetchticket : "+l);
    	LigneInfoDbAdapter mDb = new LigneInfoDbAdapter(LigneInfoActivity.this);
		mDb.open();
    	Cursor c = mDb.fetchTicket(l);
    	startManagingCursor(c);
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
    	d.setMessage("Début : "+MevoMessage.convertDateTimeHR(c.getString(c.getColumnIndexOrThrow(KEY_START)))+"\n"+
    			"Fin : "+MevoMessage.convertDateTimeHR(c.getString(c.getColumnIndexOrThrow(KEY_END)))+"\n\n"+
    			c.getString(c.getColumnIndexOrThrow(KEY_DESC)));
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
		d.show();
		mDb.close();
    }

    private void refreshView()
    {
    	String text1_1;
    	String text1_2;
    	String text2;
    	String text3;

    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        TextView t0_0 = (TextView) findViewById(R.id.infoLigne0_0);
        TextView t_compte = (TextView) findViewById(R.id.infoLigne_compte);
        TextView t0_1 = (TextView) findViewById(R.id.infoLigne0_1);
        TextView t0_2 = (TextView) findViewById(R.id.infoLigne0_2);
        TextView t1_1 = (TextView) findViewById(R.id.infoLigne1_1);
        TextView t1_2 = (TextView) findViewById(R.id.infoLigne1_2);
        TextView t2 = (TextView) findViewById(R.id.infoLigne2);
        TextView t3 = (TextView) findViewById(R.id.infoLigne3);
        TextView t3_1 = (TextView) findViewById(R.id.infoLigne3_1);
        text1_1 = "\tVotre ligne "+mgr.getString(KEY_USER, "")+" est connectée au central ADSL \"NRA\" "+mgr.getString(KEY_NRA, "")+" ("+DSLAM_Info+") ";
        text1_1 += "situé à "+mgr.getString(KEY_LINELENGTH, "0")+" mètres de votre Freebox.";
        text1_2 = "\tActuellement ("+DSLAM_Date+") les équipements dont vous dépendez ("+mgr.getString(KEY_DSLAM, "")+") ";
        if (DSLAM_ok)
        {
        	text1_2 += "fonctionnent correctement.";
        }
        else
        {
        	text1_2 += "sont en interruption de service.";
        	t1_2.setTextColor(0xffff0000);
        }
        text2 = "Liste des tickets de "+mgr.getString(KEY_DSLAM, "")+" :";
        text3 = "Historique de l'état de "+mgr.getString(KEY_DSLAM, "")+" :";
        t_compte.setText(FBMHttpConnection.getTitle());
        t1_1.setText(text1_1);
        t1_2.setText(text1_2);
        t2.setText(text2);
        t3.setText(text3);  
        
        LinearLayout lt = (LinearLayout) findViewById(R.id.LinearLayoutTickets);
        LigneInfoDbAdapter mDb = new LigneInfoDbAdapter(LigneInfoActivity.this);
		mDb.open();
        mTicketCursor = mDb.fetchAllTickets();
        startManagingCursor(mTicketCursor);
        if (mTicketCursor.moveToFirst())
        {
        	do
        	{
        		Button t = new Button(this);
        		t.setText(mTicketCursor.getString(mTicketCursor.getColumnIndexOrThrow(KEY_TITLE)));
        		t.setTextSize(14);
        		final Long id = mTicketCursor.getLong(mTicketCursor.getColumnIndexOrThrow(KEY_ROWID));
        		t.setOnClickListener(
    					new View.OnClickListener()
    					{
    						public void onClick(View view)
    						{
    							displayTicket(id);
    						}
    					}
    				);
        		lt.addView(t);
        	}
       		while (mTicketCursor.moveToNext());
        }
        else
        {
        	TextView t = new TextView(this);
    		t.setText("Pas de ticket pour cet équipement.");
    		t.setTextSize(16);
    		t.setPadding(10, 0, 0, 0);
    		lt.addView(t);        	
        }
        mDb.close();
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
				DSLAM_Info = map.get("commune") + (loc.equals("") ? "" : " - "+(String) map.get("localisation"));
				DSLAM_Date = MevoMessage.convertDateTimeHR((String) client.call("getLastDSLAMResultSetDate"));
				DSLAM_ok = (Boolean) client.call("getDSLAMStatus", mgr.getString(KEY_DSLAM, ""));
				Log.d(DEBUGTAG, "Liste tickets:");
				Object[] response = (Object[]) client.call("getTicketListForDSLAM", mgr.getString(KEY_DSLAM, ""));
//				Object[] response = (Object[]) client.call("getTicketListForDSLAM", "bas33-1");
				Log.d(DEBUGTAG, "Liste tickets:"+response.length);
				int i = response.length;
				LigneInfoDbAdapter mDb = new LigneInfoDbAdapter(LigneInfoActivity.this);
				mDb.open();

				while (--i >= 0)
				{
					if (!mDb.isTicketPresent((Integer)response[i]))
					{
						Map<String, Object> ticket = (Map<String, Object>) client.call("getTicketInfo", response[i]);
						Log.d(DEBUGTAG, "Liste tickets boucle : "+ticket.get("title"));
						Log.d(DEBUGTAG, "Liste tickets boucle : "+ticket.get("description"));
						Log.d(DEBUGTAG, "Liste tickets boucle : "+ticket.get("start"));
						Log.d(DEBUGTAG, "Liste tickets boucle : "+ticket.get("end"));
						long result = mDb.createTicket((Integer)response[i], (String)ticket.get("title"), (String)ticket.get("description"), (String)ticket.get("start"), (String)ticket.get("end"));
						Log.d(DEBUGTAG, "Liste tickets db : "+result);
					}
				}
				mDb.close();
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

    	ComptesDbAdapter mDbHelper = new ComptesDbAdapter(this);
    	mDbHelper.open();
    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
    	String user = mgr.getString(KEY_USER, "");
    	Log.d(DEBUGTAG, "User:"+user);
    	Integer rowid = mDbHelper.getIdFromLogin(user);
    	if (rowid != null)
    	{
	    	mDbHelper.updateCompte(rowid, p.title, p.login, p.password, (String) v.get(KEY_NRA),
	    			(String) v.get(KEY_DSLAM), (String) v.get(KEY_IP), (String) v.get(KEY_LINELENGTH),
	    			(String) v.get(KEY_ATTN), (String) v.get(KEY_TEL), (String) v.get(KEY_LINETYPE));
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
    	mDbHelper.close();
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
    		// refresh est demandé si NRA n'existe pas dans les prefs, donc si on est dans le cas d'un compte
    		// configuré avec Freeboxmobile <= 0.16
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
