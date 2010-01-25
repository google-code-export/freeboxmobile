package org.madprod.freeboxmobile.pvr;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

/**
 * Activité Enregistrements
 * Gère l'onglet enregistrement avec la liste des programmation d'enregistrements
 * 
 * Sous-classe: ListeEnregistrements
 * 
 * @author bduffez
 *
 */
public class EnregistrementsActivity extends ExpandableListActivity {
	private String tableEnregistrements;
	private boolean succesChargement;
    ListeEnregistrements listeEnregistrements;
    public static EnregistrementsActivity enrAct = null;

	static final int MENU_UPDATE = 0;
	static final int MENU_ADD = 1;

	static final int ACTIVITY_ENREGISTREMENT = 1;
	static final int ACTIVITY_PROGRAMMATION = 2;
	
	static final int RESULT_SUPPRESSION_OK = 1;
	static final int RESULT_PROG_OK = 2;
	static final int RESULT_PROG_NOK = 3;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pvr);
        FBMHttpConnection.initVars(this, null);

		this.listeEnregistrements = new ListeEnregistrements();
        this.succesChargement = false;
        enrAct = this;

        setTheme(android.R.style.Theme_Light);
        setTitle(getString(R.string.app_name) + " - Magnétoscope numérique" + "("+FBMHttpConnection.getIdentifiant()+")");
        
        ((Button) findViewById(R.id.pvrBtnProg)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        		ajouterNouvelEnregistrement();
        	}
        });

        updaterEnregistrements(true);
    }
    
    @Override
	protected void onDestroy() {
    	super.onDestroy();
    	FBMHttpConnection.closeDisplay();
    }
    
    private void erreur(String msgErreur) {
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("Erreur!");
		d.setMessage(msgErreur);
		d.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		d.show();
    }
    
    public void updaterEnregistrements(boolean updateFromConsole) {
    	new UpdateEnregistrementsTask(updateFromConsole).execute();
    }

	/**
	 * télécharge la liste des enregistrements, et l'affiche
	 * @author bduffez
	 *
	 */
    class UpdateEnregistrementsTask extends AsyncTask<Void, Integer, Boolean> {
    	ProgressDialog progressDialog = null;
    	Boolean updateFromConsole = false;
    	
    	UpdateEnregistrementsTask(boolean ufc) {
    		updateFromConsole = ufc;
    	}

        protected void onPreExecute() {
        	if (updateFromConsole) {
            	progressDialog = ProgressDialog.show(enrAct, "Enregistrements", "Mise à jour...", true, false);
        	}
        }
    	
        protected Boolean doInBackground(Void... arg0) {        	
        	listeEnregistrements.vider();
            
        	if (updateFromConsole) {
        		return doUpdateEnregistrements();
        	}
        	
			return Boolean.TRUE;
        }
        
        protected void onPostExecute(Boolean succes) {
        	if (succes == Boolean.TRUE) {
	            updateEnregistrementsFromDb();
	            afficherEnregistrements();
        	}
        	else {
            	erreur("Impossible de se connecter à la console Free\n"
            			+ "Avez-vous renseigné votre identifiant et mot de passe "
            			+ "dans la configuration ?");
        	}
        	
        	if (updateFromConsole) {
        		progressDialog.dismiss();
        	}
        	
            progressDialog = null;
        }
    }
    
    /**
     * HTML --> DB
     * Télécharge la page HTML de l'interface, et stocke la liste des enregistrements dans
     * la base sqlite (via la fonction recupererEnregistrements)
     */
    private boolean doUpdateEnregistrements() {		
		String url;
		
        // Recup if tv
        String contenu = null;
    	url = "http://adsl.free.fr/admin/magneto.pl";
    	List<NameValuePair> param = new ArrayList<NameValuePair>();
    	param.add(new BasicNameValuePair("sommaire","television"));
    	contenu = PvrUtils.getPage(FBMHttpConnection.getAuthRequest(url, param, true, true));
    	if (contenu == null) {
    		return false;
    	}

    	int debut = contenu.indexOf("<div class=\"table block\">") + 25;
    	int fin = contenu.indexOf("<div class=\"clearer\"></div>");

    	if (debut > 25 && fin > 0) {
    		tableEnregistrements = contenu.substring(debut, fin);
    		succesChargement = true;
    		return recupererEnregistrements();
    	}
    	
    	return false;
    }
    
    /**
     * DB --> RAM
     * Se connecte à sqlite, récupère le contenu et stocke ça dans l'objet listeEnregistrements
     */
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

    /**
     * RAM --> ECRAN
     * Affiche la liste des enregistrements depuis l'objet ListeEnregistrements
     */
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
				R.layout.pvr_enregistrements_details,
				new String[] { "key", "value" },
				new int[] { R.id.pvr_enr_list_key, R.id.pvr_enr_list_value }
			);
		setListAdapter(expListAdapter);
    }
    
    /**
     * Récupère les enregistrements depuis la table HTML de la console correspondant
     * à la liste des enregistrements programmés
     * Stocke cette liste dans la base sqlite
     */
    private boolean recupererEnregistrements() {
    	int debut;
    	String chaine, date, heure, duree, nom, ide, chaine_id, service_id;
    	String h, min, dur, name, where_id, repeat_a;
        
        // SQLite
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
        db.open();
        db.deleteAllEnregistrements();
    	
		do {
			debut = tableEnregistrements.indexOf(" <form id=\"");
			
			if (debut > 0) {
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
		
		return true;
    }
    
    /**
     * Récupère un "champ" (date, chaine...) pour un enregistrement programmé
     * @param debut	identificateur du début du champ
     * @param fin	identificateur de fin du champ
     * @return		le texte compris entre "debut" et "fin"
     */
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
        startActivityForResult(i, ACTIVITY_ENREGISTREMENT);
    	
		return true;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == ACTIVITY_ENREGISTREMENT) {
    		updaterEnregistrements(false);
    		
    		if (resultCode == RESULT_SUPPRESSION_OK) {
    			Toast.makeText(this, "Modifications enregistrées!", Toast.LENGTH_LONG).show();
    		}
    	}
    }
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_UPDATE, 0, "Mettre à jour").setIcon(android.R.drawable.ic_menu_rotate);
        menu.add(0, MENU_ADD, 1, "Ajouter").setIcon(android.R.drawable.ic_menu_add);
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_UPDATE:
            updaterEnregistrements(true);
            return true;
        case MENU_ADD:
        	ajouterNouvelEnregistrement();
            return true;
        }
        return false;
    }
    
    void ajouterNouvelEnregistrement() {
    	Intent i = new Intent();
    	i.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.pvr.ProgrammationActivity");
    	startActivity(i);
    }

    /**
     * classe de stockage de données, avec la liste des enregistrements programmés
     * ainsi que des détails sur ceux-ci (date, heure...)
     * 
     * @author bduffez
     *
     */
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
	    	listeEnregistrements.add("Erreur");
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
					if (detailsEnregistrements.get(i).size() > n+1) {
						detail.put("value", detailsEnregistrements.get(i).get(n+1));
					}
					secList.add(detail);
				}
				result.add(secList);
			}
			return result;
		}
	}
}
