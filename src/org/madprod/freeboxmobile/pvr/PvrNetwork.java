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
import org.madprod.freeboxmobile.R;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.app.Activity;
import android.os.AsyncTask;

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
    	FBMHttpConnection.FBMLog("PVRNETWORK START");
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
    	long id;
		ChainesDbAdapter db;
		int courant=0;
		int max = 0;
		boolean ok = true;
		ContentValues chainesValues = new ContentValues(3);
		ContentValues servicesValues = new ContentValues(5);
		
		db = new ChainesDbAdapter(activity);
		db.open();
//		SQLiteDatabase mDb = db.getDb();

    	String url = "http://adsl.free.fr/admin/magneto.pl";
    	
    	// Copie en variables locales pour optimiser la vitesse
    	final String KEY_CHAINE_ID = ChainesDbAdapter.KEY_CHAINE_ID;
    	final String KEY_NAME = ChainesDbAdapter.KEY_CHAINE_NAME;
    	final String KEY_CHAINE_BOITIER = ChainesDbAdapter.KEY_CHAINE_BOITIER;
    	final String DATABASE_TABLE_CHAINESTEMP = ChainesDbAdapter.DATABASE_TABLE_CHAINESTEMP;
    	final String KEY_SERVICE_DESC = ChainesDbAdapter. KEY_SERVICE_DESC;
    	final String KEY_SERVICE_ID = ChainesDbAdapter. KEY_SERVICE_ID;
    	final String KEY_PVR_MODE = ChainesDbAdapter. KEY_PVR_MODE;
    	final String DATABASE_TABLE_SERVICESTEMP = ChainesDbAdapter.DATABASE_TABLE_SERVICESTEMP;
    	
    	do
    	{
	        List<NameValuePair> param = new ArrayList<NameValuePair>();
	        param.add(new BasicNameValuePair("ajax","listes"));
	        param.add(new BasicNameValuePair("box", ""+boitier));
	
	        String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(url, param, true, true, "ISO8859_1"));
	    	FBMHttpConnection.FBMLog("DEBUT :"+new Date());
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
						db.makeBoitiersDisques();
						// ON RECUPERE LES DISQUES
						jDisksArray = jObject.getJSONArray("disks");
						for (i=0 ; i < jDisksArray.length() ; i++)
						{
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
	//						FBMHttpConnection.FBMLog("GET DATA DIRECT BOITIER "+boitier+" DISQUE SAVED : "+id);
						}
					}
					if (getChaines)
					{
						db.makeChaines();
						// ON RECUPERE LA LISTE DES CHAINES DE CE BOITIER
						jChainesArray = jObject.getJSONArray("services");
						if (boitier == 0)
						{
							ProgrammationActivity.progressText = "Actualisation de la liste des chaînes pour "+nbBoitiers+" boitier"+(nbBoitiers >1?"s...":"...");
							publishProgress(0);
							max = nbBoitiers * jChainesArray.length();
							//ProgrammationActivity.setPdMax(max);
							this.max = max; 
						}
	
						for (i=0 ; i < jChainesArray.length() ; i++)
						{
							courant ++;
							publishProgress(courant);
							jChaineObject = jChainesArray.getJSONObject(i);
							chaineId = jChaineObject.getInt("id");
							
/*							chainesValues.put(KEY_NAME, jChaineObject.getString("name"));
					        chainesValues.put(KEY_CHAINE_ID, chaineId);
					        chainesValues.put(KEY_CHAINE_BOITIER, boitier);
							mDb.insert(DATABASE_TABLE_CHAINESTEMP, null, chainesValues);
*/
							db.createChaine(
									jChaineObject.getString("name"),
									chaineId,
									boitier
									);
		//			    	FBMHttpConnection.FBMLog("GET DATA DIRECT CHAINE "+id);
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
								
/*								servicesValues.put(KEY_CHAINE_ID, chaineId);
							    servicesValues.put(KEY_CHAINE_BOITIER, boitier);
							    servicesValues.put(KEY_SERVICE_DESC, jServiceObject.getString("desc"));
							    servicesValues.put(KEY_SERVICE_ID, jServiceObject.getInt("id"));
							    servicesValues.put(KEY_PVR_MODE, pvrmode);        
							    mDb.insert(DATABASE_TABLE_SERVICESTEMP, null, servicesValues);
*/
								db.createService(
										chaineId,
										boitier,
										jServiceObject.getString("desc"),
										jServiceObject.getInt("id"),
										pvrmode);
	//					    	FBMHttpConnection.FBMLog("GET DATA DIRECT SERVICE "+id);
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
    	FBMHttpConnection.FBMLog("FIN :"+new Date());

    	if (getChaines)
    		publishProgress(max);
    	db.close();
    	doSwap(ok);
    	if (ok)
    	{
	    	// On met à jour le timestamp du dernier refresh
			SharedPreferences mgr = activity.getSharedPreferences(KEY_PREFS, activity.MODE_PRIVATE);
	    	Editor editor = mgr.edit();
	    	editor.putLong(KEY_LAST_REFRESH+FBMHttpConnection.getIdentifiant(), (new Date()).getTime());
	    	editor.commit();
	    	return true;
    	}
        FBMHttpConnection.FBMLog("==> Impossible de télécharger le json des chaines/disques");
        return false;
    }

	private void doSwap(boolean ok)
	{
		ChainesDbAdapter db;

		db = new ChainesDbAdapter(activity);
		db.open();
		if (ok)
		{
			if (getChaines)
				db.swapChaines();
			if (getDisques)
				db.swapBoitiersDisques();
		}
		db.close();
	}
}
