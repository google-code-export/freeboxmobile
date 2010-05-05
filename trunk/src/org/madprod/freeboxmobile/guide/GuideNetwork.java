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
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

/**
 * $Id$
 */

public class GuideNetwork extends FBMNetTask implements GuideConstants
{
	private Activity activity;
	private String datetime;
	private boolean getChaines;
	private boolean getProg;
	private boolean forceRefresh;
	private int pnb;

    /**
     * GuideNetwork : refresh data for Guide
     * @param a : activity used to display progress bars
     * @param d : datetime to get programmes (if prog == true)
     * @param chaine : wants to get chaines lits ?
     * @param prog : wants to get programs ?
     * @param pnb : wants to get this particular program ?
     * @param force : force refresh programs event if they are already present in database
     */
    public GuideNetwork(Activity a, String d, boolean chaine, boolean prog, int pnb, boolean force)
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
    	this.pnb = pnb;
    	Log.d(TAG,"GUIDENETWORK START "+d+" "+chaine+" "+prog+" "+force);
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

		if (pnb == 0)
		{
			dProgressSet("Importation", "Actualisation des données du guide", R.drawable.fm_guide_tv);
			publishProgress(0);
		}
		db = new ChainesDbAdapter(activity);
		db.open();

		if ((pnb == 0) &&
			(forceRefresh == false) &&
			(db.isHistoGuidePresent(datetime) > 0) &&
			(getChaines == false))
		{
			// On a déjà les données, on les charge donc pas
			Log.d(TAG,"ON A DEJA LES DONNEES");
			db.close();
			publishProgress(-1);
			return DATA_FROM_CACHE;
		}
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("ajax","get_chaines"));
        param.add(new BasicNameValuePair("date", datetime));

        String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(MAGNETO_URL, param, true, true, "UTF8"));
        if (resultat != null)
        {
        	Log.d(TAG,"Guide Network start "+new Date());
        	try
        	{
				jObject = new JSONObject(resultat);
				if (jObject.has("redirect"))
				{
					Log.d(TAG,"Authentification expirée");
					if (FBMHttpConnection.connect() == CONNECT_CONNECTED)
					{
						resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(MAGNETO_URL, param, true, true, "UTF8"));
						jObject = new JSONObject(resultat);
					}
					else
					{
						db.close();
						publishProgress(-1);
						return (DATA_NOT_DOWNLOADED);
					}
				}
				if (getProg)
				{
					jChannelsObject = jObject.getJSONObject("progs");
					max = jChannelsObject.length();
					if (pnb == 0)
					{
						dProgressMessage("Actualisation des programmes TV pour "+max+" chaînes favorites...",max);
					}
					String channelId;
					for (Iterator <String> it = jChannelsObject.keys() ; it.hasNext() ;)
					{
						if (pnb != 0)
						{
							channelId = ((Integer)pnb).toString();
						}
						else
						{
							publishProgress(courant++);
							channelId = it.next();
						}
						jHorairesObject = jChannelsObject.getJSONObject(channelId);
						for (Iterator <String> jt = jHorairesObject.keys(); jt.hasNext() ;)
						{
							channel_id = Integer.parseInt(channelId);
							String datetime_deb = jt.next();
							// Je ne sais pas à quoi correspond le channel_id 173 mais les programmes sont vides
							// et il n'y a pas de chaine qui corresponde dans la liste des chaines
							// donc...
							if ((db.isProgrammePresent(channel_id, datetime_deb) == 0) && (channel_id != 173))
							{
								jHoraireObject = jHorairesObject.getJSONObject(datetime_deb);
								db.createProgramme(
									Integer.parseInt(getJSONString(jHoraireObject, "genre_id")),
									channel_id,
									getJSONString(jHoraireObject,"resum_s"),
									getJSONString(jHoraireObject,"resum_l"),
									getJSONString(jHoraireObject,"title").replaceAll("&amp;","&"),
									Integer.parseInt(getJSONString(jHoraireObject,"duree")),
									datetime_deb,
									getJSONString(jHoraireObject,"datetime_fin")
									);
							}
							// TODO : sinon rafraichir ?
						}
						if (pnb != 0)
						{
							break;
						}
					}
					if (pnb == 0)
					{
						db.createHistoGuide(datetime);
					}
				}
				if (getChaines)
				{
					File file, filen;
					String image, canal;
					courant = 0;
					jChannelsObject = jObject.getJSONObject("chaines");
					max = jChannelsObject.length();
					dProgressMessage("Actualisation de la liste des "+max+" chaînes disponibles pour le Guide...",max);
					publishProgress(0);
					for (Iterator <String> it = jChannelsObject.keys() ; it.hasNext() ;)
					{
						publishProgress(courant++);
						String channelId = it.next();
						if (channelId.length() > 0)
						{
							channel_id = Integer.parseInt(channelId);
						}
						else
						{
							Log.d(TAG,"ChannelId == -1");
							channel_id = -1;
						}
						jChannelObject = jChannelsObject.getJSONObject(channelId);
						image = getJSONString(jChannelObject, "image");
						canal = getJSONString(jChannelObject, "canal");
						// TODO : si chaine déjà présente -> update
						
						// On teste si on a le fichier qui correspond à la chaine
				        file = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES, image);
				        if ((file.exists() == true) && (file.length() == 0))
				        {
				        	Log.d(TAG, "File size == 0, deleting... "+image);
				        	file.delete();
				        }
				        if (file.exists() == false)
				        {
							if (FBMHttpConnection.getFile(file, IMAGES_URL+image, null, false))
							{
								Log.d(TAG, "Downloading logo : "+image);
						        filen = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES, canal+".png");
								try
								{
									Log.d(TAG,"Copy file "+image);
									Utils.copyFile(file, filen);
								}
								catch (IOException e)
								{
									Log.d(TAG,"Impossible de copier "+image+" "+canal);
									e.printStackTrace();
								}
							}
				        }
				        
						if (db.isGuideChainePresent(channel_id) == 0)
						{
							db.createGuideChaine(
								Integer.parseInt(getJSONString(jChannelObject, "fbx_id")),
								channel_id,
								Integer.parseInt(canal),
								getJSONString(jChannelObject, "name"),
								image
								);
						}				        
					}
				}
			}
        	catch (JSONException e)
        	{
				Log.e(TAG,"JSONException ! "+e.getMessage());
				Log.d(TAG,resultat);
				e.printStackTrace();
				ok = false;
			}
        }
        else
        {
        	ok = false;
        }
   		publishProgress(max);
    	Log.d(TAG,"Guide Network end "+new Date());
    	db.close();
    	publishProgress(-1);
    	if (ok)
    	{
	    	return DATA_NEW_DATA;
    	}
        Log.d(TAG,"==> Impossible de télécharger les programmes");
        return DATA_NOT_DOWNLOADED;
    }
}
