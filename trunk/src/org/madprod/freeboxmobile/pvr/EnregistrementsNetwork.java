package org.madprod.freeboxmobile.pvr;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;

import android.content.Context;
import android.util.Log;

/**
 * télécharge la liste des enregistrements et les stocke en base
 * $Id$
 */

public class EnregistrementsNetwork implements Constants
{
	private static String tableEnregistrements;

    /**
     * HTML --> DB
     * Télécharge la page HTML de l'interface, et stocke la liste des enregistrements dans
     * la base sqlite (via la fonction recupererEnregistrements)
     */
    public static boolean updateEnregistrementsFromConsole(Context c)
    {		
		String url;
		int boitier = 0;
		int nbBoitiers = 0;
		int bNum;
		boolean succesChargement = false;
		
        // Recup if tv
        String contenu = null;
    	url = "https://adsls.free.fr/admin/magneto.pl";
    	List<NameValuePair> param;
    	do
    	{
	    	param = new ArrayList<NameValuePair>();
	    	param.add(new BasicNameValuePair("sommaire","television"));
	    	param.add(new BasicNameValuePair("box", ""+boitier));
	    	contenu = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(url, param, true, true, "ISO8859_1"));
	    	if (contenu == null)
	    	{
	    		// TODO : Dans le cas de plusieurs boitiers, le fait qu'un des boitiers
	    		// soit null (donc eteind) ne doit pas tout interrompre
	    		return false;
	    	}
			if (boitier == 0)
			{
	        	// Plusieurs boitiers HD ?
	        	int posDebut = contenu.indexOf("box=");
	        	if (posDebut > 0)
	        	{
	        		int d, f;
	        		String boitiers;
	        		boitiers = contenu.substring(posDebut);
	        		// On compte le nombre de boitiers
	        		do {
	        			d = boitiers.indexOf("box=");
	        			if (d == -1) {
		        			break;
	        			}
			        	boitiers = boitiers.substring(d);
			        	f = boitiers.indexOf("\"");
			        	Log.d(TAG,"Boitier parse : "+boitiers.substring(4, f));
			        	bNum = Integer.parseInt(boitiers.substring(4, f));
			        	d = boitiers.indexOf("Boitier HD");
			        	boitiers = boitiers.substring(d);

	        			f = boitiers.indexOf("</");
//	        			bName = boitiers.substring(0, f);
	        			boitiers = boitiers.substring(f);
	        			nbBoitiers++;
	        			Log.d(TAG,"Boitier : "+bNum);
	        		} while (true);
	        	}
	        	else
	        	{
//	        		bName = "Freebox HD";
	        		bNum = 0;
	        	}
			}
			// Pour chaque boitier, on récupère la liste des enregistrements
	    	int debut = contenu.indexOf("<div class=\"table block\">") + 25;
	    	int fin = contenu.indexOf("<div class=\"clearer\"></div>");
	
	    	if (debut > 25 && fin > 0)
	    	{
	    		tableEnregistrements = contenu.substring(debut, fin);
	    		succesChargement = true;
	    		recupererEnregistrements(c, boitier);
	    	}
	    	boitier++;
    	} while (boitier < nbBoitiers);	
    	return succesChargement;
    }

    /**
     * Récupère les enregistrements depuis la table HTML de la console correspondant
     * à  la liste des enregistrements programmés
     * Stocke cette liste dans la base sqlite
     */
    private static boolean recupererEnregistrements(Context c, int bId)
    {
    	int debut;
    	String chaine, date, heure, duree, nom, ide, chaine_id, service_id;
    	String h, min, dur, name, where_id, repeat_a;
        
        // SQLite
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(c);
        db.open();
        db.cleanEnregistrements(bId);
		do
		{
			debut = tableEnregistrements.indexOf(" <form id=\"");
			
			if (debut > 0)
			{
				tableEnregistrements = tableEnregistrements.substring(debut);
	        	
	        	// Récupération des infos
				chaine =		recupererChamp("<strong>", "<");
				date =			recupererChamp("<strong>", "<");
				heure =			recupererChamp("<strong>", "<");
				duree =			recupererChamp("<strong>", "<");
				nom =			recupererChamp("<strong>", "<");
				ide =			recupererChamp("value=\"", "\"");
				chaine_id =		recupererChamp("value=\"", "\"");
				service_id =	recupererChamp("value=\"", "\"");
				date =			recupererChamp("value=\"", "\"");
				h =				recupererChamp("value=\"", "\"");
				min =			recupererChamp("value=\"", "\"");
				dur =			recupererChamp("value=\"", "\"");
				name =			recupererChamp("value=\"", "\"");
				where_id =		recupererChamp("value=\"", "\"");
				repeat_a =		recupererChamp("value=\"", "\"");
				
				if (db.isEnregistrementPresent(Integer.parseInt(ide)) > 0)
				{
					db.updateEnregistrement(chaine, "", date, heure, duree, nom, ide,
							chaine_id, service_id, h, min, dur, name, where_id, repeat_a);	
				}
				else
				{
				db.createEnregistrement(chaine, "", date, heure, duree, nom, ide,
						chaine_id, service_id, bId, h, min, dur, name, where_id, repeat_a);
				}
				debut = tableEnregistrements.indexOf(" <form id=");
			}
			else
			{
				break;
			}
		} while (true);
		db.close();
		
		return true;
    }
    
    /**
     * Récupère un "champ" (date, chaine...) pour un enregistrement programmé
     * @param debut	identificateur du début du champ
     * @param fin	identificateur de fin du champ
     * @return		le texte compris entre "debut" et "fin"
     */
    private static String recupererChamp(String debut, String fin)
    {
    	String champ;
    	int pos;
    	
    	// On se place au début
    	pos = tableEnregistrements.indexOf(debut);    	
    	if (pos <= 0 || pos + debut.length() > tableEnregistrements.length())
    	{
    		return "";
    	}
    	champ = tableEnregistrements.substring(pos + debut.length());
    	tableEnregistrements = tableEnregistrements.substring(pos + debut.length());
    	
    	// On coupe après la fin
    	pos = champ.indexOf(fin);
    	if (pos <= 0 || pos > champ.length() || pos > tableEnregistrements.length())
    	{
    		return "";
    	}
    	champ = champ.substring(0, pos);
    	tableEnregistrements = tableEnregistrements.substring(pos);
    	
    	return champ;
    }
}
