package org.madprod.freeboxmobile.pvr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.madprod.freeboxmobile.HttpConnection;

public class EnregistrementsActivity extends ListActivity {
	private static String urlConsole = "";
	private String tableEnregistrements;
	private Cursor listCursor;
	private boolean succesChargement;
	List<String> listeEnregistrements;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        HttpConnection.initVars(this);
        
		this.listeEnregistrements = new ArrayList<String>();
        this.succesChargement = false;
        
        updateEnregistrements();
        afficherEnregistrements();
    }

	public static String getUrlConsole() {
		return EnregistrementsActivity.urlConsole;
	}
    
    private void afficherEnregistrements() {
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
        
        db.open();
        listCursor = db.fetchAllEnregistrements(new String[] {
        		EnregistrementsDbAdapter.KEY_ROWID,
        		EnregistrementsDbAdapter.KEY_CHAINE,
        		EnregistrementsDbAdapter.KEY_DATE});

		if (listCursor != null && listCursor.moveToFirst()) {
            do {
     			int colChaine = listCursor.getColumnIndex("chaine");
     			int colDate = listCursor.getColumnIndex("date");
     			String item = listCursor.getString(colChaine);
     			item += " (" + listCursor.getString(colDate) + ")";
     			
     			listeEnregistrements.add(item);
     			
            } while (listCursor.moveToNext());
		}
        
        db.close();
		
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, listeEnregistrements));
    }
    
    private boolean login() {
    	return HttpConnection.connectFreeUI(this) == HttpConnection.CONNECT_CONNECTED;
    }
    
    private void erreur(String msgErreur) {
		listeEnregistrements.add(msgErreur);
		
		succesChargement = false;

        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, listeEnregistrements));
    }
    
    private void updateEnregistrements() {
    	// On se log sur l'if free
        if (login() == false) {
        	erreur("Impossible de se connecter à la console Free");
        	return;
        }
        
		String url;
		
        // Recup if tv
        String contenu = null;
    	url  = "http://adsl.free.fr/admin/magneto.pl?id=";
    	url += HttpConnection.getId()+"&idt="+HttpConnection.getIdt();
    	url += "&sommaire=television";
        	
    	contenu = HttpConnection.getPage(HttpConnection.getRequest(url, true));
    	if (contenu == null) {
    		erreur("Impossible de télécharger la liste des enregistrements");
    		return;
    	}
    	
    	int debut = contenu.indexOf("<div class=\"table block\">") + 25;
    	int fin = contenu.indexOf("<div class=\"clearer\"></div>");

    	if (debut > 25 && fin > 0) {
    		tableEnregistrements = contenu.substring(debut, fin);
    		succesChargement = true;
    		recupererEnregistrements();
    	}
    	else {
    		erreur("Impossible de télécharger la liste des enregistrements");
		}
    }

    private void recupererEnregistrements() {
    	int debut;
        
        // SQLite
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
        db.open();
        db.deleteAllEnregistrements();
    	
		do {
			debut = tableEnregistrements.indexOf(" <form id=\"");
			
			if (debut > 0) {
				tableEnregistrements = tableEnregistrements.substring(debut);
				
	        	String chaine, date, heure, duree, nom, ide, chaine_id, service_id;
	        	String h, min, dur, name, where_id, repeat_a;
	        	
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
				repeat_a =		recupererChamp("value=\"", "\"") + " ";
				
				// EnregistrementActivity
				db.createEnregistrement(chaine, date, heure, duree, nom, ide,
						chaine_id, service_id, h, min, dur, name, where_id, repeat_a);
				
				debut = tableEnregistrements.indexOf(" <form id=");
			}
			else {
				break;
			}
		} while (true);
		
		db.close();
    }
    
    private String recupererChamp(String debut, String fin) {
    	String champ;
    	int pos;
    	
    	// On se place au début
    	pos = tableEnregistrements.indexOf(debut);    	
    	if (pos <= 0 || pos + debut.length() > tableEnregistrements.length()) {
    		return null;
    	}
    	champ = tableEnregistrements.substring(pos + debut.length());
    	tableEnregistrements = tableEnregistrements.substring(pos + debut.length());
    	
    	// On coupe après la fin
    	pos = champ.indexOf(fin);
    	if (pos <= 0 || pos > champ.length() || pos > tableEnregistrements.length()) {
    		return null;
    	}
    	champ = champ.substring(0, pos);
    	tableEnregistrements = tableEnregistrements.substring(pos);
    	
    	return champ;
    }
    
    //@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
    	if (succesChargement == false) {
    		return;
    	}
    	
        Cursor c = listCursor;
        c.moveToPosition(position);
        Intent i = new Intent(this, EnregistrementActivity.class);
        i.putExtra(EnregistrementsDbAdapter.KEY_ROWID, c.getLong(c.getColumnIndex(EnregistrementsDbAdapter.KEY_ROWID)));
        startActivity(i);
        c.close();
    }
}
