package org.madprod.freeboxmobile.pvr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.FBMHttpConnection;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

/**
 * télécharge la liste des chaines et disques
 * $Id$
 */

public class PvrNetwork extends AsyncTask<Void, Integer, Boolean> implements PvrConstants
{
	private Activity activity;
	private boolean getChaines;
	private boolean getDisques;
	private int max;
	
    protected void onPreExecute()
    {
    }

    protected Boolean doInBackground(Void... arg0)
    {
    	return getData();
    }
    
    protected void onProgressUpdate(Integer... progress)
    {
        ProgrammationActivity.showProgress(activity, progress[0], max);
    }
    
    protected void onPostExecute(Boolean telechargementOk)
    {
   		ProgrammationActivity.dismissPd();
    }
    
    public PvrNetwork(Activity a, boolean getChaines, boolean getDisques)
    {
    	activity = a;
    	this.getChaines = getChaines;
    	this.getDisques = getDisques;
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
				e.printStackTrace();
			}
		}
		return 0;
	}

    public boolean getData()
    {
    	JSONObject jObject;
    	JSONObject jDiskObject;
    	JSONObject jChaineObject;
    	JSONObject jServiceObject;
    	JSONArray jServicesArray;
    	JSONArray jChainesArray;
    	JSONArray jDisksArray;
    	String mode;
    	int pvrmode;
    	int boitier = 0;
    	int nbBoitiers = 0;
    	int i,j;
    	int chaineId;
		ChainesDbAdapter db;
		int courant=0;
		int max = 0;
		boolean ok = true;
		
		db = new ChainesDbAdapter(activity);
		db.open();

    	String url = "http://adsl.free.fr/admin/magneto.pl";
    	
    	if (getDisques)
    	{
			db.makeBoitiersDisques();    		
    	}
    	if (getChaines)
    	{
			db.makeChaines();    		
    	}
    	do
    	{
	        List<NameValuePair> param = new ArrayList<NameValuePair>();
	        param.add(new BasicNameValuePair("ajax","listes"));
	        param.add(new BasicNameValuePair("box", ""+boitier));
	
	        String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(url, param, true, true, "ISO8859_1"));
	    	Log.i(TAG,"DEBUT :"+new Date());
	        if (resultat != null)
	        {
	        	try
	        	{
					jObject = new JSONObject(resultat);
					if (jObject.has("redirect"))
					{
						if (FBMHttpConnection.connect() == CONNECT_CONNECTED)
						{
							resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(url, param, true, true, "ISO8859_1"));
						}
						else
						{
							db.close();
							return (false);
						}
					}

					// ON RECUPERE LE NB DE BOITIERS (UNE FOIS)
					if (nbBoitiers == 0)
					{
						nbBoitiers = jObject.getInt("boxes");
					}
					if (getDisques)
					{
						// ON RECUPERE LES DISQUES
						jDisksArray = jObject.getJSONArray("disks");
						for (i=0 ; i < jDisksArray.length() ; i++)
						{
							// En insérant en base comme cela, si un boitier n'a pas de disque
							// il ne sera paas référencé (ce qui est voulu)
							jDiskObject = jDisksArray.getJSONObject(i);
					    	db.createBoitierDisque(
					    			"Freebox HD "+(boitier+1),
					    			boitier,
					    			jDiskObject.getInt("free_size"),
					    			jDiskObject.getInt("total_size"),
					    			jDiskObject.getInt("id"),
					    			getBoolean(jDiskObject,"nomedia"),
					    			getBoolean(jDiskObject,"dirty"),
					    			getBoolean(jDiskObject,"readonly"),
					    			getBoolean(jDiskObject,"busy"),
					    			jDiskObject.getString("mount_point"),
					    			jDiskObject.getString("label")
					    			);
						}
						Log.d(TAG,"GET BOITIER "+boitier+" NB DISQUES : "+i);
					}
					if (getChaines)
					{
						// ON RECUPERE LA LISTE DES CHAINES DE CE BOITIER
						jChainesArray = jObject.getJSONArray("services");
						max = jChainesArray.length();
						ProgrammationActivity.progressText = "Actualisation de la liste des "+max+" chaînes pour le boitier "+(int)(boitier+1)+" ("+nbBoitiers+" boitier"+(nbBoitiers >1?"s":"")+" en tout)...";
						this.max = max;
						courant = 0;
						publishProgress(0);
						Log.d(TAG,"Récupération chaines boitier "+boitier+" - nb chaines = "+max);
						for (i=0 ; i < jChainesArray.length() ; i++)
						{
							courant ++;
							publishProgress(courant);
							jChaineObject = jChainesArray.getJSONObject(i);
							chaineId = jChaineObject.getInt("id");
							db.createChaine(
									jChaineObject.getString("name"),
									chaineId,
									boitier
									);
							jServicesArray = jChaineObject.getJSONArray("service");
							for (j=0 ; j < jServicesArray.length() ; j++)
							{
								jServiceObject = jServicesArray.getJSONObject(j);
								mode = jServiceObject.getString("pvr_mode");
								if (mode.equals("public"))
								{
									pvrmode = PVR_MODE_PUBLIC;
								} else if (mode.equals("private"))
								{
									pvrmode = PVR_MODE_PRIVATE;
								} else
								{
									pvrmode = PVR_MODE_DISABLED;
								}
								
								db.createService(
										chaineId,
										boitier,
										jServiceObject.getString("desc"),
										jServiceObject.getInt("id"),
										pvrmode);
	//					    	Log.d(TAG,"GET DATA DIRECT SERVICE "+id);
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
					break;
				}
	        }
	        else
	        {
	        	ok = false;
	        	break;
	        }
	        boitier++;
    	} while (boitier < nbBoitiers);
    	Log.i(TAG,"FIN :"+new Date());

    	if (getChaines)
    		publishProgress(max);
    	db.close();
    	if (ok)
    	{
        	doSwap();
	    	// On met à jour le timestamp du dernier refresh
			SharedPreferences mgr = activity.getSharedPreferences(KEY_PREFS, activity.MODE_PRIVATE);
	    	Editor editor = mgr.edit();
	    	editor.putLong(KEY_LAST_REFRESH+FBMHttpConnection.getIdentifiant(), (new Date()).getTime());
	    	editor.commit();
	    	return true;
    	}
        Log.d(TAG,"==> Impossible de télécharger le json des chaines/disques");
        return false;
    }

	private void doSwap()
	{
		ChainesDbAdapter db;

		db = new ChainesDbAdapter(activity);
		db.open();
		if (getChaines)
			db.swapChaines();
		if (getDisques)
			db.swapBoitiersDisques();
		db.close();
	}
}
