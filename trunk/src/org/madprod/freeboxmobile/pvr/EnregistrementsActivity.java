package org.madprod.freeboxmobile.pvr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;

import org.madprod.freeboxmobile.HttpConnection;
import org.madprod.freeboxmobile.R;

public class EnregistrementsActivity extends ExpandableListActivity {
	private String tableEnregistrements;
	private boolean succesChargement;
	ProgressDialog prog = null;
    final Handler mHandler = new Handler();
    ListeEnregistrements listeEnregistrements;
	
	static final int MENU_UPDATE = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        HttpConnection.initVars(this);

		this.listeEnregistrements = new ListeEnregistrements();
        this.succesChargement = false;

        updateEnregistrementsFromConsole();
        updateEnregistrementsFromDb();
        afficherEnregistrements();
    }
    
    private void updateEnregistrementsFromDb() {
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
        
        db.open();
        Cursor listCursor = db.fetchAllEnregistrements(new String[] {
        		EnregistrementsDbAdapter.KEY_ROWID,
        		EnregistrementsDbAdapter.KEY_CHAINE,
        		EnregistrementsDbAdapter.KEY_DATE,
        		EnregistrementsDbAdapter.KEY_HEURE,
        		EnregistrementsDbAdapter.KEY_DUREE,
        		EnregistrementsDbAdapter.KEY_NOM
        		});

		if (listCursor != null && listCursor.moveToFirst()) {
			succesChargement = true;
			
            do {
     			int colChaine = listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE);
     			int colDate = listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE);
     			String item = listCursor.getString(colChaine);
     			item += " (" + listCursor.getString(colDate) + ")";
     			
     			List<String> details = new ArrayList<String>();
     			details.add("Chaîne");
     			details.add(listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE)));
     			details.add("Date");
     			details.add(listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE)));
     			details.add("Heure");
     			details.add(listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_HEURE)));
     			details.add("Durée");
     			details.add(listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_DUREE)));
     			details.add("Nom");
     			details.add(listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_NOM)));
     			
     			listeEnregistrements.ajouter(item, details);
     			
            } while (listCursor.moveToNext());
		}
		
		listCursor.close();
        
        db.close();
    }
    
    private boolean login() {
    	return HttpConnection.connectFreeUI(this) == HttpConnection.CONNECT_CONNECTED;
    }
    
    private void erreur(String msgErreur) {
    	listeEnregistrements.ajouter(msgErreur);
		succesChargement = false;
		afficherEnregistrements();
    }
    
    private void doUpdateEnregistrements() {		
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
    
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
    	super.onChildClick(parent, v, groupPosition, childPosition, id);
    	
    	if (succesChargement == false) {
    		return false;
    	}

    	// Récupération de l'id
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
        db.open();
        Cursor c = db.fetchAllEnregistrements(new String[] { EnregistrementsDbAdapter.KEY_ROWID });
        c.moveToPosition(groupPosition);
        long rowId = c.getLong(c.getColumnIndex(EnregistrementsDbAdapter.KEY_ROWID));
        c.close();
        db.close();
        
        // Lancement de l'activité
        Intent i = new Intent(this, EnregistrementActivity.class);
        i.putExtra(EnregistrementsDbAdapter.KEY_ROWID, rowId);
        startActivity(i);
    	
		return true;
    }
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_UPDATE, 0, "Mettre à jour");
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_UPDATE:
            updateEnregistrementsFromConsole();
            updateEnregistrementsFromDb();
            afficherEnregistrements();
            return true;
        }
        return false;
    }

    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            afficherEnregistrements();
        }
    };
    
    protected void updateEnregistrementsFromConsole() {
    	prog = ProgressDialog.show(this, "Veuillez patienter", "Mise à jour...", true,false);
        Thread t = new Thread() {
            public void run() {
            	listeEnregistrements.vider();
	        	doUpdateEnregistrements();

				if (prog != null) {
					prog.dismiss();
					prog = null;
				}
				
                mHandler.post(mUpdateResults);
            }
        };
        t.start();
    }

	private class ListeEnregistrements {
		private List<String> listeEnregistrements = null;
		private List<List<String>> detailsEnregistrements = null;
		
		ListeEnregistrements() {
			listeEnregistrements = new ArrayList<String>();
			detailsEnregistrements = new ArrayList<List<String>>();
		}
		
	    public void vider() {
	    	listeEnregistrements.clear();
	    	detailsEnregistrements.clear();
	    }
	    
	    // Ajout d'un enregistrement à la liste, avec détails
	    public void ajouter(String nom, List<String> details) {
	    	listeEnregistrements.add(nom);
	    	detailsEnregistrements.add(details);
	    }
	    // Ajout d'un enregistrement à la liste, sans détails == erreur
	    public void ajouter(String message) {
	    	listeEnregistrements.add("erreur");
	    	ArrayList<String> details = new ArrayList<String>();
	    	details.add(message);
	    	detailsEnregistrements.add(details);
	    }
	    
	    // Crée la liste des enregistrements
		public List<HashMap<String, String>> createGroupList() {
			ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
			for( int i = 0 ; i < listeEnregistrements.size() ; ++i ) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("enregistrement", listeEnregistrements.get(i));
				result.add(map);
			}
			return result;
		}
		// Crée la liste des détails pour chaque enregistrement
		public List<ArrayList<HashMap<String, String>>> createChildList() {
			ArrayList<ArrayList<HashMap<String, String>>> result = new ArrayList<ArrayList<HashMap<String, String>>>();

			for( int i = 0 ; i < detailsEnregistrements.size() ; ++i ) {
				ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();
				
				for( int n = 0 ;
						detailsEnregistrements.get(i) != null && n < detailsEnregistrements.get(i).size();
						n += 2) {
					HashMap<String, String> detail = new HashMap<String, String>();
					detail.put("key", detailsEnregistrements.get(i).get(n));
					detail.put("value", detailsEnregistrements.get(i).get(n+1));
					secList.add(detail);
				}
				result.add(secList);
			}
			return result;
		}
	}
    
    private void afficherEnregistrements() {
		SimpleExpandableListAdapter expListAdapter =
			new SimpleExpandableListAdapter(
				this,
				// Group: liste des enregistrements
				listeEnregistrements.createGroupList(),
				R.layout.pvr_enregistrements_liste,
				new String[] { "enregistrement" },
				new int[] { R.id.pvr_enr_list_item },
				
				// Child: liste des détails pour chaque enregistrement
				listeEnregistrements.createChildList(),
				R.layout.pvr_enregistrements_liste,
				new String[] { "key", "value" },
				new int[] { R.id.pvr_enr_list_key, R.id.pvr_enr_list_value }
			);
		setListAdapter( expListAdapter );
    }
}
