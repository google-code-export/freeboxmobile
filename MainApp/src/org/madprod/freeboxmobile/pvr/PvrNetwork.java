package org.madprod.freeboxmobile.pvr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

/**
 * télécharge la liste des chaines et disques
 * $Id$
 */

public class PvrNetwork extends FBMNetTask implements PvrConstants // AsyncTask<Void, Integer, Boolean> implements PvrConstants
{
	private boolean getChaines;
	private boolean getDisques;
	
	/**
	 * PvrNetwork
	 * @param getChaines : want to get Chaines list ?
	 * @param getDisques : want to get Logo list ?
	 */
    public PvrNetwork(boolean getChaines, boolean getDisques)
    {
    	this.getChaines = getChaines;
    	this.getDisques = getDisques;
    }

    public boolean getData()
    {
    	JSONObject jObject;
    	JSONObject jDiskObject;
    	JSONObject jChaineObject;
    	JSONObject jServiceObject;
    	JSONArray jServicesArray;
//    	JSONArray jChainesArray;
    	JSONArray jArray;
    	String mode;
    	int pvrmode;
    	Long boitier = new Long(0);
    	int nbBoitiers = 0;
    	int i,j;
    	Long chaineId;
		ChainesDbAdapter db;
		int max = 0;
		int size = 0;
		int ok = 0;
		
		Activity a = getActivity();
		if (a == null)
			return false;
		db = new ChainesDbAdapter(a);
		if (db == null)
		{
			return (false);
		}
		db.open();

    	dProgressSet("Importation", "", R.drawable.fm_magnetoscope);

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
	
	        String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthXmlRequest(MAGNETO_URL, param, true, true, "ISO8859_1"));
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
							resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthXmlRequest(MAGNETO_URL, param, true, true, "ISO8859_1"));
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
						
						// On récupère les favoris
						jArray = jObject.getJSONArray("chaines");
						size = jArray.length();
			    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    		String datetime = sdf.format(new Date());

						for (i=0 ; i < size ; i++)
						{
//							Log.d(TAG, "fav : "+jArray.getString(i) );
							db.updateFavoris(Integer.parseInt(jArray.getString(i)), datetime);
						}
						db.flushFavoris(datetime);
					}
					if (getDisques)
					{
						// ON RECUPERE LES DISQUES
						jArray = jObject.getJSONArray("disks");
						size = jArray.length();
						for (i=0 ; i < size ; i++)
						{
							// En insérant en base comme cela, si un boitier n'a pas de disque
							// il ne sera pas référencé (ce qui est voulu)
							jDiskObject = jArray.getJSONObject(i);
					    	db.createBoitierDisque(
					    			"Freebox HD "+(boitier+1),
					    			boitier,
					    			jDiskObject.getInt("free_size"),
					    			jDiskObject.getInt("total_size"),
					    			jDiskObject.getInt("id"),
					    			getJSONBoolean(jDiskObject,"nomedia"),
					    			getJSONBoolean(jDiskObject,"dirty"),
					    			getJSONBoolean(jDiskObject,"readonly"),
					    			getJSONBoolean(jDiskObject,"busy"),
					    			jDiskObject.getString("mount_point"),
					    			jDiskObject.getString("label")
					    			);
						}
						Log.d(TAG,"GET BOITIER "+boitier+" - NB DISQUES : "+i);
					}
					if (getChaines)
					{
						// ON RECUPERE LA LISTE DES CHAINES DE CE BOITIER
						jArray = jObject.getJSONArray("services");
						max = jArray.length();
						dProgressMessage("Actualisation de la liste des "+max+" chaînes pour le boitier "+(int)(boitier+1)+" ("+nbBoitiers+" boitier"+(nbBoitiers >1?"s":"")+" en tout)...", max);
						publishProgress(0);
						Log.d(TAG,"Récupération chaines boitier "+boitier+" - nb chaines = "+max);
						db.mDb.beginTransaction();
						try
						{
							for (i=0 ; i < max ; i++)
							{
								publishProgress(i);
								jChaineObject = jArray.getJSONObject(i);
								chaineId = new Long(jChaineObject.getInt("id"));
								db.createRawChaine(
										jChaineObject.getString("name").replace("'", " "),
										chaineId,
										boitier
										);
	
								jServicesArray = jChaineObject.getJSONArray("service");
								size = jServicesArray.length();
								for (j=0 ; j < size ; j++)
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
									
									db.createRawService(
											chaineId,
											boitier,
											jServiceObject.getString("desc"),
											jServiceObject.getInt("id"),
											pvrmode);
								}
							}
							Log.d(TAG, "Transaction success");
							db.mDb.setTransactionSuccessful();
						}
						finally
						{
							db.mDb.endTransaction();
						}
					}
					ok = 1;
				}
	        	catch (JSONException e)
	        	{
					Log.e(TAG,"JSONException ! "+e.getMessage());
					Log.d(TAG,"Page:"+resultat);
					e.printStackTrace();
					ok = -1;
					break;
				}
	        }
	        else
	        {
	        	// TODO : Si on a un boitier débranché, on tombe ici mais il ne faudrait pas
	        	// sortir comme ca (ca empeche la maj de tous les autres boitiers)
	        	Log.d(TAG, "IMPOSSIBLE DE RAFRAICHIR");
	        	ok = -1;
	        	break;
	        }
	        boitier++;
    	} while (boitier < nbBoitiers);
    	Log.i(TAG,"FIN :"+new Date());

    	if (getChaines)
    		publishProgress(max);
    	publishProgress(-1);
    	if (ok > 0)
    	{
        	doSwap(db);
    		Log.d(TAG, "C FINI ! "+db.getChainesNb());
	    	// On met à jour le timestamp du dernier refresh
        	// TODO : Peut etre mettre mgr dans FBMNetTask afin qu'il soit toujours accessible et qu'on puisse toujours faire l'update ci-dessous
        	a = getActivity();
        	if (a != null)
        	{
				SharedPreferences mgr = getActivity().getSharedPreferences(KEY_PREFS, Activity.MODE_PRIVATE);
		    	Editor editor = mgr.edit();
		    	editor.putLong(KEY_LAST_REFRESH+FBMHttpConnection.getIdentifiant(), (new Date()).getTime());
		    	editor.commit();
        	}
	    	db.close();
	    	return true;
    	}
        Log.d(TAG,"==> Impossible de télécharger le json des chaines/disques");
    	db.close();
        return false;
    }

	private void doSwap(ChainesDbAdapter db)
	{
		if (getChaines)
			db.swapChaines();
		if (getDisques)
			db.swapBoitiersDisques();
	}
}
