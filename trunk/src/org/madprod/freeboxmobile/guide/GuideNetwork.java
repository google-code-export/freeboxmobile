package org.madprod.freeboxmobile.guide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;

/**
 * $Id$
 */

public class GuideNetwork extends AsyncTask<Void, Integer, Integer> implements GuideConstants
{
	private Activity activity;
	private String datetime;
	private boolean getChaines;
	private boolean getProg;
	private boolean forceRefresh;
	
    protected void onPreExecute()
    {
    }

    protected Integer doInBackground(Void... arg0)
    {
    	return getData();
    }
    
    protected void onProgressUpdate(Integer... progress)
    {
        GuideActivity.showProgress(activity, progress[0]);
    }

    protected void onPostExecute(Integer statut)
    {
    	if (statut != DATA_NOT_DOWNLOADED)
    	{
    	}
    	else {
//    		ProgrammationActivity.afficherMsgErreur(activity.getString(R.string.pvrErreurTelechargementChaines), activity);
    	}
   		GuideActivity.dismissPd();
    }
    
    /**
     * GuideNetwork : refresh data for Guide
     * @param a : activity used to display progress bars
     * @param d : datetime to get programmes (if prog == true)
     * @param chaine : wants to get chaines lits ?
     * @param prog : wants to get programmes ?
     * @param force : force refresh programmes event if they are already present in database
     */
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

    public int getData()
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
			return DATA_FROM_CACHE;
		}
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("ajax","get_chaines"));
        param.add(new BasicNameValuePair("date", datetime));

        String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(MAGNETO_URL, param, true, true, "UTF8"));
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
						resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(MAGNETO_URL, param, true, true, "UTF8"));
					}
					else
					{
						db.close();
						return (DATA_NOT_DOWNLOADED);
					}
				}
				if (getProg)
				{
					jChannelsObject = jObject.getJSONObject("progs");
					max = jChannelsObject.length();
					GuideActivity.progressText = "Actualisation des programmes TV pour "+max+" chaînes choisies...";
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
							// TODO : sinon rafraichir ?
						}
					}
					db.createHistoGuide(datetime);
				}
				if (getChaines)
				{
					File file, filen;
					String image, canal;
					courant = 0;
		        	file = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES);
		        	file.mkdirs();
					jChannelsObject = jObject.getJSONObject("chaines");
					max = jChannelsObject.length();
					GuideActivity.progressText = "Actualisation des "+max+" chaînes du Guide...";
					publishProgress(0);
					GuideActivity.setPdMax(max);
					for (Iterator <String> it = jChannelsObject.keys() ; it.hasNext() ;)
					{
						publishProgress(courant++);
						String channelId = it.next();
						channel_id = Integer.decode(channelId);
						jChannelObject = jChannelsObject.getJSONObject(channelId);
						image = getString(jChannelObject, "image");
						canal = getString(jChannelObject, "canal");
						if (db.isGuideChainePresent(channel_id) == 0)
						{
							db.createGuideChaine(
								Integer.decode(getString(jChannelObject, "fbx_id")),
								channel_id,
								Integer.decode(canal),
								getString(jChannelObject, "name"),
								image
								);
						}
						// TODO : si chaine déjà présente -> update
						
						// On teste si on a le fichier qui correspond à la chaine
				        file = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES, image);
				        if (file.exists() == false)
				        {
							if (FBMHttpConnection.getFile(file, IMAGES_URL+image, null, false))
							{
						        filen = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES, canal+".png");
								try
								{
									FBMHttpConnection.FBMLog("Copy file "+image);
									Utils.copyFile(file, filen);
								}
								catch (IOException e)
								{
									FBMHttpConnection.FBMLog("Impossible de copier "+image+" "+canal);
									e.printStackTrace();
								}
							}
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
   		publishProgress(max);
    	db.close();
    	FBMHttpConnection.FBMLog("Guide Network end "+new Date());
    	if (ok)
    	{
	    	// On met à jour le timestamp du dernier refresh
/*
			SharedPreferences mgr = activity.getSharedPreferences(KEY_PREFS, activity.MODE_PRIVATE);
	    	Editor editor = mgr.edit();
	    	editor.putLong(KEY_LAST_REFRESH+FBMHttpConnection.getIdentifiant(), (new Date()).getTime());
	    	editor.commit();
*/
	    	return DATA_NEW_DATA;
    	}
        FBMHttpConnection.FBMLog("==> Impossible de télécharger les programmes");
        return DATA_NOT_DOWNLOADED;
    }
}
