package org.madprod.freeboxmobile.pvr;

import java.io.File;
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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

/**
 * Activité Enregistrements
 * Gère l'onglet enregistrement avec la liste des programmation d'enregistrements
 * 
 * Sous-classe: ListeEnregistrements
 * 
 * @author bduffez
 * *$Id$
 * 
 */

public class EnregistrementsActivity extends ExpandableListActivity {
	private boolean succesChargement;
    private static ListeEnregistrements listeEnregistrements = null;
    public static EnregistrementsActivity enrAct = null;
    private AsyncTask<Void, Integer, Boolean> task = null;

	static final int MENU_UPDATE = 0;
	static final int MENU_ADD = 1;
	
	static final int CMENU_VOIR = 0;
	static final int CMENU_MODIF = 1;
	static final int CMENU_SUPPR = 2;

	static final int ACTIVITY_ENREGISTREMENT = 1;
	static final int ACTIVITY_PROGRAMMATION = 2;
	
	static final int RESULT_SUPPRESSION_OK = 1;
	static final int RESULT_PROG_OK = 2;
	static final int RESULT_PROG_NOK = 3;
	
	static final String DIR_PVR = "/pvr/";
	
	ProgressDialog progressDialog = null;
	
	static String curId = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.pvr);
        FBMHttpConnection.initVars(this, null);
        FBMHttpConnection.FBMLog("ENREGISTREMENTSACTIVITY CREATE");

        File old_db = getDatabasePath("freeboxmobile"+FBMHttpConnection.getIdentifiant());
        if (old_db.exists()) {
        	FBMHttpConnection.FBMLog("PVR: Ancien nom de bdd sqlite, renommage en pvr_");
        	if (old_db.renameTo(getDatabasePath(EnregistrementsDbAdapter.DATABASE_NAME))) {
        		FBMHttpConnection.FBMLog("OK ");
        	} else {
        		FBMHttpConnection.FBMLog("KO");
        	}
        }
        if (!curId.equals(FBMHttpConnection.getIdentifiant())) {
        	curId = FBMHttpConnection.getIdentifiant();
        	reset();
        }
        else {
        	listeEnregistrements = new ListeEnregistrements();
        }
        succesChargement = false;
        enrAct = this;
        
        registerForContextMenu(getExpandableListView());

        setTheme(android.R.style.Theme_Light);
        setTitle(getString(R.string.app_name) + " " + getString(R.string.pvrPVR)
        		+ " - "+FBMHttpConnection.getTitle());
        
        ((Button) findViewById(R.id.pvrBtnProg)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        		ajouterNouvelEnregistrement();
        	}
        });
    }
    
    @Override
	protected void onDestroy()
    {
    	super.onDestroy();
    	FBMHttpConnection.closeDisplay();
    	enrAct = null;
    }
	
	@Override
	protected void onPause() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		super.onPause();
	}
    
	@Override
	protected void onStart() {
		super.onStart();
		if (listeEnregistrements != null)
		{
			updateEnregistrementsFromDb();
			afficherEnregistrements();
		}
		else
		{
			listeEnregistrements = new ListeEnregistrements();
			updateEnregistrementsFromDb();
			afficherEnregistrements();
			updaterEnregistrements(true);
		}
	}
	
    private void erreur(String msgErreur)
    {
    	if (enrAct != null)
    	{
	    	AlertDialog d = new AlertDialog.Builder(this).create();
			d.setTitle(getString(R.string.pvrErreur));
			d.setMessage(msgErreur);
			d.setIcon(R.drawable.fm_magnetoscope);
			d.setButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					finish();
				}
			});
			d.show();
    	}
    }
    
    public static void reset() {
    	if (listeEnregistrements != null) {
    		listeEnregistrements.vider();
    		listeEnregistrements = null;
    	}
    }
    
    public void updaterEnregistrements(boolean updateFromConsole)
    {
    	task = new UpdateEnregistrementsTask(updateFromConsole).execute();
    }

	/**
	 * télécharge la liste des enregistrements, et l'affiche
	 * @author bduffez
	 *
	 */
    class UpdateEnregistrementsTask extends AsyncTask<Void, Integer, Boolean> {
    	Boolean updateFromConsole = false;
    	
    	UpdateEnregistrementsTask(boolean ufc) {
    		updateFromConsole = ufc;
    	}

        protected void onPreExecute() {
    		setProgressBarIndeterminateVisibility(true);
        }
    	
        protected Boolean doInBackground(Void... arg0) {
        	if (listeEnregistrements != null) {
        		listeEnregistrements.vider();
        	} else {
        		listeEnregistrements = new ListeEnregistrements();
        	}
            
        	if (updateFromConsole) {
        		listeEnregistrements.vider();
        		succesChargement = EnregistrementsNetwork.updateEnregistrementsFromConsole(enrAct);
        		return succesChargement;
        	}

			return Boolean.TRUE;
        }
        
        protected void onPostExecute(Boolean succes) {
        	if (succes == Boolean.TRUE) {
	            updateEnregistrementsFromDb();
	            afficherEnregistrements();
        	}
        	else {
            	erreur("Impossible de se connecter à la console Free\n");
        	}
        	
    		setProgressBarIndeterminateVisibility(false);
        	if (updateFromConsole && progressDialog != null) {
        		progressDialog.dismiss();
        	}
        	
            progressDialog = null;
        }
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
        		EnregistrementsDbAdapter.KEY_NOM,
        		EnregistrementsDbAdapter.KEY_BOITIER_ID,
        		},
        		EnregistrementsDbAdapter.KEY_DATE + " ASC, "
        		+ EnregistrementsDbAdapter.KEY_HEURE + " ASC, "
        		+ EnregistrementsDbAdapter.KEY_MIN + " ASC");

		if (listCursor != null && listCursor.moveToFirst()) {
			succesChargement = true;
			listeEnregistrements.vider();
            do {
     			String item = listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_NOM))+
     			" [" + listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE)) +
     			" "+ listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_HEURE)) + "]";
     			
     			List<String> details = new ArrayList<String>();
     			details.add("Chaîne");
     			details.add(listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE)));
//     			details.add("Date");
//     			details.add(listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE)));
//     			details.add("Heure");
//     			details.add(listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_HEURE)));
     			details.add("Durée");
     			details.add(listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_DUREE)));
//     			details.add("Nom");
//     			details.add(listCursor.getString(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_NOM)));
     			details.add("Boitier");
     			details.add("Boitier "+(listCursor.getInt(listCursor.getColumnIndex(EnregistrementsDbAdapter.KEY_BOITIER_ID))+1));
     			
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
    
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
    	super.onChildClick(parent, v, groupPosition, childPosition, id);
    	
    	if (succesChargement == false) {
    		return false;
    	}
    	
    	afficherEnregistrementActivity(new Intent(this, EnregistrementActivity.class), groupPosition, ACTIVITY_ENREGISTREMENT);
    	
		return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.add(0, CMENU_VOIR, 0, getString(R.string.pvrCMenuVoir));
    	menu.add(0, CMENU_MODIF, 0, getString(R.string.pvrCMenuModif));
    	menu.add(0, CMENU_SUPPR, 0, getString(R.string.pvrCMenuSuppr));
    }

    public boolean onContextItemSelected(MenuItem item)
    {
    	final ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
    	int itemId = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		switch (item.getItemId()) {
			case CMENU_VOIR:
				afficherEnregistrementActivity(new Intent(this, EnregistrementActivity.class), itemId, ACTIVITY_ENREGISTREMENT);
				return true;
			case CMENU_MODIF:
				afficherEnregistrementActivity(new Intent(this, ProgrammationActivity.class), itemId, ACTIVITY_PROGRAMMATION);
				return true;
			case CMENU_SUPPR:
    			EnregistrementActivity.SupprimerEnregistrement(enrAct, false, getRowIdFromItemId(itemId));
				return true;
			default:
				return super.onContextItemSelected(item);
		}
    }
    private long getRowIdFromItemId(int itemId) {
    	// Récupération de l'id
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
        db.open();
        Cursor c = db.fetchAllEnregistrements(new String[] { EnregistrementsDbAdapter.KEY_ROWID });
        c.moveToPosition(itemId);
        long rowId = c.getLong(c.getColumnIndex(EnregistrementsDbAdapter.KEY_ROWID));
        c.close();
        db.close();
        return rowId;
    }
    private void afficherEnregistrementActivity(Intent i, int itemId, int action) {        
        // Lancement de l'activité
        i.putExtra(EnregistrementsDbAdapter.KEY_ROWID, getRowIdFromItemId(itemId));
        startActivityForResult(i, action);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode)
    	{
	    	case ACTIVITY_ENREGISTREMENT:
	    		updaterEnregistrements(false);
	    		
	    		if (resultCode == RESULT_SUPPRESSION_OK) {
	    			new UpdateEnregistrementsTask(true).execute();
	    			Toast.makeText(this, getString(R.string.pvrModificationsEnregistrees),
	    					Toast.LENGTH_LONG).show();
	    		}
	    	break;
	    	case ACTIVITY_PROGRAMMATION:
	    		if (resultCode != 0)
	    			updaterEnregistrements(false);
	    	break;
    	}
    }
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_UPDATE, 0, "Mettre à jour la liste").setIcon(android.R.drawable.ic_menu_rotate);
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
    	startActivityForResult(i, ACTIVITY_PROGRAMMATION);
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
