package org.madprod.freeboxmobile.pvr;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * télécharge la liste des chaines et disques
 * @author 
 * $Id :
 */

public class PvrNetwork extends AsyncTask<Void, Integer, Boolean>
{
	public static Activity activity;
	private List<Chaine> mChaines;
	private List<Disque> mDisques;
	private static List<String> mBoitiers = null;
	private boolean plusieursBoitiersHD = false;
	
        protected void onPreExecute() {
//        	ProgrammationActivity.showPatientez(a);
        }

        protected Boolean doInBackground(Void... arg0) {
        	return telechargerEtParser();
        }
        
        protected void onPostExecute(Boolean telechargementOk) {
        	if (telechargementOk == Boolean.TRUE) {
//        		preparerActivite();
        	}
        	else {
//        		afficherMsgErreur(getString(R.string.pvrErreurTelechargementChaines));
//        		boutonOK.setEnabled(false);
        	}
//            ProgrammationActivity.dismissPd();
        }
    
    /**
     * 
     * @return true en cas de succès, false sinon
     */
    private boolean telechargerEtParser() {

        // Récupérer chaines et disques durs        
        String url = "http://adsl.free.fr/admin/magneto.pl";
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("detail","1"));
//        param.add(new BasicNameValuePair("box", ""+mBoitierHD));

        String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequestISR(url, param, true, true));
        if (resultat != null) {
			FBMHttpConnection.FBMLog("telechargerEtParser not null");
    		
	        int posChaines = resultat.indexOf("var serv_a = [");
	        int posDisques = resultat.indexOf("var disk_a = [");
	        
	        if (posChaines > 0 && posDisques > 0) {
	    		FBMHttpConnection.FBMLog("telechargerEtParser posChaines > 0 && posDisques > 0");
	        	// Récupération du javascript correspondant à la liste des chaines
	        	String strChaines = resultat.substring(posChaines+14, posDisques);
	        	int finChaines = strChaines.lastIndexOf("}");
	        	strChaines = strChaines.substring(0, finChaines+1);
	        	
	        	// Récupération du javascript correspondant à la liste des disques durs
	        	String strDisques = resultat.substring(posDisques+14);
	        	int fin = strDisques.lastIndexOf("}];")+1;
	        	strDisques = strDisques.substring(0, fin);
	        	
	        	// Conversion JSON -> objet dans la RAM
	        	getListeChaines(strChaines);
	        	getListeDisques(strDisques);
	        	
	        	// Deux boitiers HD ?
	        	int posDebut = resultat.indexOf("Boitier HD");
	        	if (posDebut > 0) {
	        		int d, f;
	        		String boitiers;
	        		boitiers = resultat.substring(posDebut);
	        		mBoitiers = new ArrayList<String>();
	        		plusieursBoitiersHD = true;
	        		
	        		do {
	        			d = boitiers.indexOf("Boitier HD");
	        			if (d == -1) {
		        			break;
	        			}
	        			
	        			f = d + boitiers.substring(d).indexOf("</");
	        			mBoitiers.add(boitiers.substring(d, f));
	        			boitiers = boitiers.substring(f);
	        		} while (true);
	        	}
	        	
	        	return true;
	        }
	        else {
	    		FBMHttpConnection.FBMLog("telechargerEtParser impossible de trouver le json dans le html");
	    		FBMHttpConnection.FBMLog(resultat);
	        }
        }
        else {
        	FBMHttpConnection.FBMLog("telechargerEtParser null");
        }
        FBMHttpConnection.FBMLog("==> Impossible de télécharger le json des chaines/disques");
    	return false;
    }
    
	// Fonctions pour parser le javascript listant les chaines et les disques durs
	private void getListeChaines(String strChaines) {
		getListe(strChaines, "}]},{\"name\"", 3, true);
	}
	
	private void getListeDisques(String strDisques) {
		getListe(strDisques, "}", 1, false);
	}

	/**
	 * Crée la liste des chaines ou des disques (selon le boolean)
	 * @param strSource:	l'array de JSON (commence par { sinon crash)
	 * @param sep:			ce qui sépare deux objets JSON
	 * @param shift:		le nombre d'octets inutiles entre deux objets JSON
	 * @param isChaines:	true si c'est la liste des chaines, false si c'est celle des disques
	 */
	private void getListe(String strSource, String sep, int shift, boolean isChaines) {
		String str;
		int pos;
		String chaineName;
		ChainesDbAdapter db;
		int chaineId;
		int i;

		db = new ChainesDbAdapter(activity);
		db.open();
		// Init
		if (isChaines) {
			mChaines = new ArrayList<Chaine>();
		} else  {
			mDisques = new ArrayList<Disque>();
		}
		
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
			
			// Ajout à la liste
			if (isChaines) {
				Chaine chaine = new Chaine(str);
				List<Chaine.Service> s = chaine.getServices();
				mChaines.add(chaine);
				chaineName = chaine.getName();
				chaineId = chaine.getChaineId();
				FBMHttpConnection.FBMLog("CHAINE : "+chaineName+" "+chaineId+" "+s.size());
				for (i = 0; i < s.size(); i++) {
					FBMHttpConnection.FBMLog("   SERVICE : "+s.get(i).getServiceId()+" "+s.get(i).getPvrMode()+" "+s.get(i).getDesc()+" "+
					+db.createChaine(chaineName, chaineId, s.get(i).getDesc(), s.get(i).getServiceId(), s.get(i).getPvrMode()));
				}
			} else {
				mDisques.add(new Disque(str));
			}
			
			// Préparation du prochain item JSON
			if (strSource.length() > pos+1) {
				strSource = strSource.substring(pos+1);
			}
			else {
				break;
			}
		} while(true);
		if (isChaines) {
			db.swapChaines();
		}
		db.close();
    }
}
