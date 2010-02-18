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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.app.Activity;
import android.os.AsyncTask;

/**
 * télécharge la liste des chaines et disques
 * @author 
 * $Id$
 */

public class PvrNetwork extends AsyncTask<Void, Integer, Boolean> implements PvrConstants
{
	private Activity activity;
	private boolean getChaines;
	private boolean getDisques;
	
        protected void onPreExecute() {
//        	if (activity != null)
//        		if (!getDisques)
//        			ProgrammationActivity.showPatientezChaines(activity);
//        			ProgrammationActivity.showProgress(activity, "test","chargement des chaines",0);
//        		else
//        			ProgrammationActivity.showPatientezDonnees(activity);
        }

        protected Boolean doInBackground(Void... arg0) {
        	return getData();
        }
        
        protected void onProgressUpdate(Integer... progress)
        {
            ProgrammationActivity.showProgress(activity, progress[0]);
        }
        
        protected void onPostExecute(Boolean telechargementOk) {
        	FBMHttpConnection.FBMLog("onPostExecute : "+activity);
        	if (telechargementOk == Boolean.TRUE) {
        	}
        	else {
        		ProgrammationActivity.afficherMsgErreur(activity.getString(R.string.pvrErreurTelechargementChaines), activity);
        	}
        	if (activity != null)
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

		db = new ChainesDbAdapter(activity);
		db.open();

    	String url = "http://adsl.free.fr/admin/magneto.pl";
    		//?id=2536851&idt=ffabaa06377c9a2a&ajax=listes
    	do
    	{
	        List<NameValuePair> param = new ArrayList<NameValuePair>();
	        param.add(new BasicNameValuePair("ajax","listes"));
	        param.add(new BasicNameValuePair("box", ""+boitier));
	
	        String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequestISR(url, param, true, true));
	        if (resultat != null)
	        {
	        	try
	        	{
					jObject = new JSONObject(resultat);
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
						// ON RECUPERE LA LISTE DES CHAINES DE CE BOITIER
						jChainesArray = jObject.getJSONArray("services");
						if (boitier == 0)
						{
							ProgrammationActivity.progressText = "Actualisation de la liste des chaînes pour "+nbBoitiers+" boitier"+(nbBoitiers >1?"s...":"...");
							publishProgress(0);
							max = nbBoitiers * jChainesArray.length();
							ProgrammationActivity.setPdMax(max);
						}
	
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

    /**
     * 
     * @return true en cas de succès, false sinon
     */
    public boolean getData2() {
    	int boitier = 0;
    	int nbBoitiers = 0;
    	int bNum = 0;
    	boolean ok = true;
    	List<String> mBoitiersName;
    	List<Integer> mBoitiersNb;

        String url = "http://adsl.free.fr/admin/magneto.pl";

    	// Récupérer chaines et disques durs et boitiers
		mBoitiersName = new ArrayList<String>();
		mBoitiersNb = new ArrayList<Integer>();
    	do
    	{
	        List<NameValuePair> param = new ArrayList<NameValuePair>();
	        param.add(new BasicNameValuePair("detail","1"));
	        param.add(new BasicNameValuePair("box", ""+boitier));
	
	        String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequestISR(url, param, true, true));
	        if (resultat != null) {
				FBMHttpConnection.FBMLog("telechargerEtParser not null - boitier "+boitier+" / "+nbBoitiers);
	    		
		        int posChaines = resultat.indexOf("var serv_a = [");
		        int posDisques = resultat.indexOf("var disk_a = [");

		        if (posChaines > 0 && posDisques > 0) {
		    		FBMHttpConnection.FBMLog("telechargerEtParser posChaines > 0 && posDisques > 0");
		    		if (boitier == 0)
		    		{
			        	// Plusieurs boitiers HD ?
			        	int posDebut = resultat.indexOf("box=");
			        	if (posDebut > 0)
			        	{
			        		int d, f;
			        		String boitiers;
			        		boitiers = resultat.substring(posDebut);
		
			        		do {
			        			d = boitiers.indexOf("box=");
			        			if (d == -1) {
				        			break;
			        			}
					        	boitiers = boitiers.substring(d);
					        	f = boitiers.indexOf("\"");
					        	FBMHttpConnection.FBMLog("Boitier parse : "+boitiers.substring(4, f));
					        	bNum = Integer.parseInt(boitiers.substring(4, f));
					        	mBoitiersNb.add(bNum);
					        	d = boitiers.indexOf("Boitier HD");
					        	boitiers = boitiers.substring(d);
			        			
			        			f = boitiers.indexOf("</");
			        			String bname = boitiers.substring(0, f);
			        			mBoitiersName.add(bname);
			        			boitiers = boitiers.substring(f);
			        			nbBoitiers++;
			        			FBMHttpConnection.FBMLog("Boitier : "+bname+" n="+bNum);
			        		} while (true);
			        	}
			        	else
			        	{
			        		mBoitiersName.add("Freebox HD");
			        		mBoitiersNb.add(0);
			        	}
		    		}
		        	// Pour chaque boitier, récupération du javascript correspondant à la liste des chaines
		        	
		        	// Conversion JSON -> objet
		        	if (getChaines)
		        	{
			        	String strChaines = resultat.substring(posChaines+14, posDisques);
			        	strChaines = strChaines.substring(0, strChaines.lastIndexOf("}")+1);
		        		getListeChaines(strChaines, mBoitiersNb.get(boitier));
		        	}

		        	if (getDisques)
		        	{
			        	// Pour chaque boitier, on récupère la liste des disques
			        	String strDisques = resultat.substring(posDisques+14);
			        	strDisques = strDisques.substring(0, strDisques.lastIndexOf("}];")+1);
			        	getListeDisques(strDisques, mBoitiersName.get(boitier), mBoitiersNb.get(boitier));
		        	}
		        }
		        else {
		    		FBMHttpConnection.FBMLog("telechargerEtParser impossible de trouver le json dans le html");
		    		FBMHttpConnection.FBMLog(resultat);
		    		ok = false;
		        }
	        }
	        else {
	        	FBMHttpConnection.FBMLog("telechargerEtParser null");
	        	ok = false;
	        }
	        boitier++;
    	} while (boitier < nbBoitiers);
    	// On échange les tables temporaires avec les vraies ou on efface les tables temp
    	doSwap(ok);
    	if (ok)
    	{
	    	// On met à jour le timestamp du dernier refresh
			SharedPreferences mgr = activity.getSharedPreferences(KEY_PREFS, activity.MODE_PRIVATE);
	    	Editor editor = mgr.edit();
	    	editor.putLong(KEY_LAST_REFRESH+FBMHttpConnection.getIdentifiant(), (new Date()).getTime());
	    	editor.commit();
	    	// On met à jour une variable du PVR pour que le PVR rafraichisse son affichage
	    	return true;
    	}
        FBMHttpConnection.FBMLog("==> Impossible de télécharger le json des chaines/disques");
    	return false;
    }
    
	// Fonctions pour parser le javascript listant les chaines et les disques durs
	private void getListeChaines(String strChaines, int bNumber) {
		getListe(strChaines, "}]},{\"name\"", 3, null, bNumber);
	}

	private void getListeDisques(String strDisques, String bName, int bNumber) {
		getListe(strDisques, "}", 1, bName, bNumber);
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
		else
		{
			if (getChaines)
				db.cleanTempChaines();
			if (getDisques)
				db.cleanTempBoitiersDisques();
		}
		db.close();
	}
	/**
	 * Crée la liste des chaines ou des disques (selon le boolean)
	 * @param strSource:	l'array de JSON (commence par { sinon crash)
	 * @param sep:			ce qui sépare deux objets JSON
	 * @param shift:		le nombre d'octets inutiles entre deux objets JSON
	 * @param isChaines:	true si c'est la liste des chaines, false si c'est celle des disques
	 */
	private void getListe(String strSource, String sep, int shift, String bName, int bNumber) {
		String str;
		int pos;
		ChainesDbAdapter db;

		db = new ChainesDbAdapter(activity);
		db.open();
		
		// Loop
		do {
			// pos contient la position de la fin du string JSON
			pos = strSource.indexOf(sep) + shift;
			if (pos <= 10) {
				pos = strSource.lastIndexOf("}") + 1;
				
				if (pos <= 10) {
					break;
				}
			}
			
			// Récupération du string JSON
			str = strSource.substring(0, pos);
			
			if (bName == null)
			{
				Chaine chaine = new Chaine(str, bNumber);
				chaine.storeDb(db);
			}
			else
			{
				Disque disque = new Disque(str);
				disque.storeDb(db, bName, bNumber);
			}
			// Préparation du prochain item JSON
			if (strSource.length() > pos+1) {
				strSource = strSource.substring(pos+1);
			}
			else {
				break;
			}
		} while(true);
		db.close();
    }
}
