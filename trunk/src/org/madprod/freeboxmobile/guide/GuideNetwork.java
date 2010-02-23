package org.madprod.freeboxmobile.guide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.app.Activity;
import android.os.AsyncTask;

/**
 * $Id$
 */

public class GuideNetwork extends AsyncTask<Void, Integer, Boolean> implements Constants
{
	private Activity activity;
	private String datetime;
	private boolean getChaines;
	private boolean getProg;
	private boolean forceRefresh;
	
    protected void onPreExecute()
    {
    }

    protected Boolean doInBackground(Void... arg0)
    {
    	return getData();
    }
    
    protected void onProgressUpdate(Integer... progress)
    {
        GuideActivity.showProgress(activity, progress[0]);
    }

    protected void onPostExecute(Boolean telechargementOk) {
    	if (telechargementOk == Boolean.TRUE) {
    	}
    	else {
//    		ProgrammationActivity.afficherMsgErreur(activity.getString(R.string.pvrErreurTelechargementChaines), activity);
    	}
    	if (activity != null)
    		GuideActivity.dismissPd();
    }
    
    public GuideNetwork(Activity a, String d, boolean chaine, boolean prog, boolean force)
    {
    	activity = a;
    	if (d != null)
    	{
    		datetime = d;
    	}
    	else
    	{
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
    		datetime = sdf.format(new Date());
    	}
    	getChaines = chaine;
    	getProg = prog;
    	forceRefresh = force;
    	FBMHttpConnection.FBMLog("GUIDENETWORK START "+d+" "+chaine+" "+prog);
    }
    
	private int getBoolean(JSONObject o, String key)
	{
		if (o.has(key))
		{
			try
			{
				return (o.getBoolean(key)?1:0);
			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	private int getInt(JSONObject o, String key)
	{
		if (o.has(key))
		{
			try
			{
				return o.getInt(key);
			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	private String getString(JSONObject o, String key)
	{
		if (o.has(key))
		{
			try
			{
				return o.getString(key);
			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";
	}

    public boolean getData()
    {
    	JSONObject jObject;
    	JSONObject jChannelsObject;
    	JSONObject jChannelObject;
    	JSONObject jHorairesObject;
    	JSONObject jHoraireObject;
    	int channel_id;
		ChainesDbAdapter db;
		int courant=0;
		int max = 0;
		boolean ok = true;

		GuideActivity.progressText = "Actualisation des données du guide";
		publishProgress(0);
		db = new ChainesDbAdapter(activity);
		db.open();

		if ((forceRefresh == false) && (db.isHistoGuidePresent(datetime) > 0) && (getChaines == false))
		{
			// On a déjà les données, on les charge donc pas
			FBMHttpConnection.FBMLog("ON A DEJA LES DONNEES");
			db.close();
			return true;
		}
    	String url = "http://adsl.free.fr/admin/magneto.pl";
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("ajax","get_chaines"));
        param.add(new BasicNameValuePair("date", datetime));

        String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(url, param, true, true, "UTF8"));
        if (resultat != null)
        {
        	FBMHttpConnection.FBMLog("Guide Network start "+new Date());
        	try
        	{
				jObject = new JSONObject(resultat);
				if (jObject.has("redirect"))
				{
					if (FBMHttpConnection.connect() == CONNECT_CONNECTED)
					{
						resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(url, param, true, true, "UTF8"));
					}
					else
					{
						db.close();
						return (false);
					}
				}
				if (getProg)
				{
					jChannelsObject = jObject.getJSONObject("progs");
					max = jChannelsObject.length();
					GuideActivity.progressText = "Actualisation des programmes TV";
					GuideActivity.setPdMax(max);
					for (Iterator <String> it = jChannelsObject.keys() ; it.hasNext() ;)
					{
						publishProgress(courant++);
						String channelId = it.next();
						jHorairesObject = jChannelsObject.getJSONObject(channelId);
						for (Iterator <String> jt = jHorairesObject.keys(); jt.hasNext() ;)
						{
							channel_id = Integer.decode(channelId);
							String datetime_deb = jt.next();
							// Je ne sais pas à quoi correspond le channel_id 173 mais les programmes sont vides
							// et il n'y a pas de chaine qui corresponde dans la liste des chaines
							// donc...
							if ((db.isProgrammePresent(channel_id, datetime_deb) == 0) && (channel_id != 173))
							{
								jHoraireObject = jHorairesObject.getJSONObject(datetime_deb);
								db.createProgramme(
									Integer.decode(getString(jHoraireObject, "genre_id")),
									channel_id,
									getString(jHoraireObject,"resum_s"),
									getString(jHoraireObject,"resum_l"),
									getString(jHoraireObject,"title"),
									Integer.decode(getString(jHoraireObject,"duree")),
									datetime_deb,
									getString(jHoraireObject,"datetime_fin")
									);
							}
						}
					}
					db.createHistoGuide(datetime);
				}
				if (getChaines)
				{
					courant = 0;
					jChannelsObject = jObject.getJSONObject("chaines");
					max = jChannelsObject.length();
					GuideActivity.progressText = "Actualisation des chaînes du Guide";
					publishProgress(0);
					GuideActivity.setPdMax(max);
					for (Iterator <String> it = jChannelsObject.keys() ; it.hasNext() ;)
					{
						publishProgress(courant++);
						String channelId = it.next();
						channel_id = Integer.decode(channelId);
						jChannelObject = jChannelsObject.getJSONObject(channelId);
						if (db.isGuideChainePresent(channel_id) == 0)
						{
							db.createGuideChaine(
								Integer.decode(getString(jChannelObject, "fbx_id")),
								channel_id,
								Integer.decode(getString(jChannelObject, "canal")),
								getString(jChannelObject, "name"),
								getString(jChannelObject, "image")
								);
						}
					}
				}
			}
        	catch (JSONException e)
        	{
				FBMHttpConnection.FBMLog("JSONException ! "+e.getMessage());
				FBMHttpConnection.FBMLog(resultat);
				e.printStackTrace();
				ok = false;
			}
        }
        else
        {
        	ok = false;
        }
    	FBMHttpConnection.FBMLog("Guide Network end "+new Date());
   		publishProgress(max);
    	db.close();
    	if (ok)
    	{
	    	// On met à jour le timestamp du dernier refresh
/*
			SharedPreferences mgr = activity.getSharedPreferences(KEY_PREFS, activity.MODE_PRIVATE);
	    	Editor editor = mgr.edit();
	    	editor.putLong(KEY_LAST_REFRESH+FBMHttpConnection.getIdentifiant(), (new Date()).getTime());
	    	editor.commit();
*/
	    	return true;
    	}
        FBMHttpConnection.FBMLog("==> Impossible de télécharger les programmes");
        return false;
    }
}
