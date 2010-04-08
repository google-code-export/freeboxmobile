package org.madprod.freeboxmobile.ligne;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.home.ComptesDbAdapter;
import org.madprod.freeboxmobile.mvv.MevoMessage;
import org.xmlrpc.android.XMLRPCClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class LigneInfoActivity extends Activity implements LigneInfoConstants
{
	private static String DSLAM_Info = "";
	private static String DSLAM_Date = "";
	private static boolean DSLAM_ok = false;
	private static Object[] DSLAM_Histo = null;
	private static String lineType = "";
	private static String lastUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FBMNetTask.register(this);
        Log.i(TAG,"LINEINFO CREATE");
        
        setContentView(R.layout.ligne_info);
        setTitle(getString(R.string.app_name)+" - Info Ligne ADSL Freebox");
    }
    
    @Override
    protected void onDestroy()
    {
    	FBMNetTask.unregister(this);
    	super.onDestroy();
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
        SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        // Si l'utilisateur est différent de celui qui a lancé l'activity la dernière fois (changement de compte)
        if (!lastUser.equals(mgr.getString(KEY_TITLE, "")))
        {
        	DSLAM_Info = "";
        	lastUser = mgr.getString(KEY_TITLE, "");
        }
        lineType = mgr.getString(KEY_LINETYPE, "");
        Log.i(TAG,"LineType : "+lineType);
        if (DSLAM_Info.equals(""))
        	new UpdateCompte().execute(new Payload(mgr.getString(KEY_NRA, "").equals(""), mgr.getString(KEY_TITLE, ""), mgr.getString(KEY_USER, ""), mgr.getString(KEY_PASSWORD, ""), mgr.getString(KEY_NRA, "")));
        else
        	refreshView();
        NotificationManager mNotif = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotif.cancel(NOTIF_INFOADSL);
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
    	Log.i(TAG,"Fetchticket : "+l);
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
		c.close();
		mDb.close();
    }

    private void refreshView()
    {
    	String text1_1;
    	String text1_2;
    	String text2;
    	String text3;
    	Cursor mTicketCursor;

    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
//        TextView t0_0 = (TextView) findViewById(R.id.infoLigne0_0);
        TextView t_compte = (TextView) findViewById(R.id.infoLigne_compte);
//        TextView t0_1 = (TextView) findViewById(R.id.infoLigne0_1);
//        TextView t0_2 = (TextView) findViewById(R.id.infoLigne0_2);
        TextView t1_1 = (TextView) findViewById(R.id.infoLigne1_1);
        TextView t1_2 = (TextView) findViewById(R.id.infoLigne1_2);
        TextView t2 = (TextView) findViewById(R.id.infoLigne2);
        TextView t3 = (TextView) findViewById(R.id.infoLigne3);
        text1_1 = "\tVotre ligne "+mgr.getString(KEY_USER, "")+" est connectée au central ADSL \"NRA\" "+mgr.getString(KEY_NRA, "")+" ";
        if (!DSLAM_Info.equals(""))
        {
        	text1_1 += "("+DSLAM_Info+") ";
        }
        text1_1 += "situé à "+mgr.getString(KEY_LINELENGTH, "0")+" mètres de votre Freebox.";

        if (lineType.equals("1"))
        {
	        text1_2 = "\tActuellement ("+DSLAM_Date+") les équipements dont vous dépendez ("+mgr.getString(KEY_DSLAM, "")+") ";
	        if (DSLAM_ok)
	        {
	        	text1_2 += "fonctionnent correctement.";
	        	t1_2.setTextColor(0xff000000);
	        }
	        else
	        {
	        	text1_2 += "sont en interruption de service.";
	        	t1_2.setTextColor(0xffff0000);
	        }
	        text2 = "Liste des tickets de "+mgr.getString(KEY_DSLAM, "")+" :";
	        text3 = "Historique de l'état de "+mgr.getString(KEY_DSLAM, "")+" :";
	        
	        LinearLayout lt = (LinearLayout) findViewById(R.id.LinearLayoutTickets);
	        lt.removeAllViews();
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
	        
	        if (DSLAM_Histo != null)
	        {
		    	LinearLayout.LayoutParams imgParam = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		    	imgParam.setMargins(1,5,1,1);
		    	imgParam.weight = 1;
	
		    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		    	SimpleDateFormat formatCourt = new SimpleDateFormat("dd/MM");
		    	GregorianCalendar calendar = new java.util.GregorianCalendar();
		    	calendar.setTime(new Date());
		    	int j = 0;
		        LinearLayout layoutH = (LinearLayout) findViewById(R.id.LinearLayoutHistory);
		        layoutH.removeAllViews();
		    	while (j++ < 7)
		    	{
		    		String f = format.format(calendar.getTime());
		    		int l = DSLAM_Histo.length - 1;
		    		boolean trouve = false;
		    		while (l >=0 && !trouve)
		    		{
		    			Map<String, Object> map = (Map<String, Object>) DSLAM_Histo[l];
			        	String h = (String) map.get("date");
			        	if (h.equals(f))
			        	{
			        		trouve = true;
			        	}
			        	else
			        		l = l-1;
		    		}
			        if (l >= 0)
					{
			        	LinearLayout layoutHistory = new LinearLayout(this);
			        	LinearLayout cellDate = new LinearLayout(this);
			        	cellDate.setOrientation(LinearLayout.VERTICAL);
				    	LinearLayout.LayoutParams cellDateParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			        	cellDate.setLayoutParams(cellDateParams);
			        	
				    	TextView tdate = new TextView(this);
				    	tdate.setText(formatCourt.format(calendar.getTime()));
				    	tdate.setPadding(0,0,5,0);
				    	tdate.setInputType(InputType.TYPE_CLASS_TEXT);
	
			        	LinearLayout ligneImages = new LinearLayout(this);
			        	ligneImages.setOrientation(LinearLayout.HORIZONTAL);
				    	LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				    	ligneImages.setLayoutParams(llp);

			        	Map<String, Object> map = (Map<String, Object>) DSLAM_Histo[l];
			        	String h = (String) map.get("summary");
				        for (int i = 0; i <48; i++)
				        {	
				        	ImageView img = new ImageView(LigneInfoActivity.this);
				        	img.setLayoutParams(imgParam);
				        	try
				        	{
					        	if (h.charAt(i) == '1')
					        		img.setBackgroundColor(0xff00ff00);
					        	else
					        		img.setBackgroundColor(0xffff0000);
				        	}
				        	catch (Exception e)
				        	{
				        		// on laisse l'image transparente
				        	}
				        	img.setMinimumHeight(10);
				        	ligneImages.addView(img);
				        }
				    	cellDate.addView(tdate);
			        	layoutHistory.addView(cellDate);
			        	layoutHistory.addView(ligneImages);
			        	layoutH.addView(layoutHistory);
					}
		    		calendar.add(Calendar.DATE, -1);
		    	}
	        }
	        mTicketCursor.close();
	        mDb.close();
        }
        else
        {
        	text1_2 = "\tInformations non disponibles pour votre type de ligne.";
	        text2 = "";
	        text3 = "";
	        LinearLayout l = (LinearLayout) findViewById(R.id.LinearLayoutHistoLigne);
	        l.setVisibility(View.GONE);
        }
        t_compte.setText(FBMHttpConnection.getTitle());
        t1_1.setText(text1_1);
        t1_2.setText(text1_2);
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
				DSLAM_Info = map.get("commune") + (loc.equals("") ? "" : " - "+(String) map.get("localisation"));
				DSLAM_Date = MevoMessage.convertDateTimeHR((String) client.call("getLastDSLAMResultSetDate"));
				DSLAM_ok = (Boolean) client.call("getDSLAMStatus", mgr.getString(KEY_DSLAM, ""));
				Object[] response = (Object[]) client.call("getTicketListForDSLAM", mgr.getString(KEY_DSLAM, ""));
//				Object[] response = (Object[]) client.call("getTicketListForDSLAM", "bas33-1");
				int i = response.length;
				LigneInfoDbAdapter mDb = new LigneInfoDbAdapter(LigneInfoActivity.this);
				mDb.open();

				while (--i >= 0)
				{
					if (!mDb.isTicketPresent((Integer)response[i]))
					{
						Map<String, Object> ticket = (Map<String, Object>) client.call("getTicketInfo", response[i]);
						long result = mDb.createTicket((Integer)response[i], (String)ticket.get("title"), (String)ticket.get("description"), (String)ticket.get("start"), (String)ticket.get("end"));
						Log.d(TAG, "Liste tickets db : "+result);
					}
				}
				mDb.close();
				
				DSLAM_Histo = (Object[]) client.call("getDSLAMStatusHistory", mgr.getString(KEY_DSLAM, ""));
				Log.d(TAG,"Liste histo:"+DSLAM_Histo.length);
			}
			else
			{
				Log.d(TAG,"Pas de NRA");
			}
    	}
		catch (Exception e)
		{
			Log.e(TAG, "updateInfos : " + e.getMessage());
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
    	Log.d(TAG, "User:"+user);
    	Long rowid = mDbHelper.getIdFromLogin(user);
    	if (rowid != null)
    	{
	    	mDbHelper.updateCompte(rowid, p.title, p.login, p.password, (String) v.get(KEY_NRA),
	    			(String) v.get(KEY_DSLAM), (String) v.get(KEY_IP), (String) v.get(KEY_LINELENGTH),
	    			(String) v.get(KEY_ATTN), (String) v.get(KEY_TEL), (String) v.get(KEY_LINETYPE),
	    			(String) v.get(KEY_FBMVERSION));
			Editor editor = mgr.edit();
			editor.putString(KEY_NRA, (String) v.get(KEY_NRA));
			editor.putString(KEY_DSLAM, (String) v.get(KEY_DSLAM));
			editor.putString(KEY_IP, (String) v.get(KEY_IP));
			editor.putString(KEY_TEL, (String) v.get(KEY_TEL));
			editor.putString(KEY_LINELENGTH, (String) v.get(KEY_LINELENGTH));
			editor.putString(KEY_ATTN, (String) v.get(KEY_ATTN));
			editor.putString(KEY_LINETYPE, (String) v.get(KEY_LINETYPE));
			editor.putString(KEY_FBMVERSION, (String) v.get(KEY_FBMVERSION));
			editor.commit();
    	}
    	else
    	{
    		Log.d(TAG, "saveState : pb : user not found !");
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
        // refresh est demandé si NRA n'existe pas dans les prefs, donc si on est dans le cas d'un compte
		// configuré avec Freeboxmobile <= 0.16 
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

    // TODO : Nettoyer plus bas les lignes de code commentées
    private class UpdateCompte extends AsyncTask<Payload, Void, Payload> implements Constants
    {
    	@Override
    	protected Payload doInBackground(Payload... payload)
    	{
    		// TODO : Remove tout ca
    		// refresh est demandé si NRA n'existe pas dans les prefs, donc si on est dans le cas d'un compte
    		// configuré avec Freeboxmobile <= 0.16 
//    		if (payload[0].refresh)
//    		{
//    			payload[0].result = FBMHttpConnection.connectFreeCheck(payload[0].login, payload[0].password);
//    			if (payload[0].result != null)
//    			{
//    				saveState(payload[0]);
//    				payload[0].nra = (String) payload[0].result.get(KEY_NRA);
//    			}
//    		}
   			updateInfos(payload[0]);
    		return payload[0];
    	}

		@Override
    	protected void onPreExecute()
    	{
			FBMNetTask.iProgressShow(
					"Mise à jour des données",
					"Connexion en cours...",
					R.drawable.fm_infos_adsl);
    	}

    	@Override
    	protected void onPostExecute(Payload payload)
    	{
    		FBMNetTask.iProgressDialogDismiss();
    		if ((payload.refresh) && (payload.result != null))
    		{
    			if (payload.result == null)
    			{
    				FBMNetTask.alertDialogShow(
    						"Connexion impossible",
    						"Impossible de se connecter au portail de Free.\n"+
    						"Vérifiez votre identifiant, " +
    						"votre mot de passe et votre "+	
    						"connexion à Internet (Wifi, 3G...).",
    						R.drawable.fm_infos_adsl);
    			}
    		}
			refreshView();
    	}
    }

}
