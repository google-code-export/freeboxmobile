package org.madprod.freeboxmobile.pvr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

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
	static final int CMENU_SHARE = 3;

	static final int ACTIVITY_ENREGISTREMENT = 1;
	static final int ACTIVITY_PROGRAMMATION = 2;
	
	static final int RESULT_SUPPRESSION_OK = 1;
	static final int RESULT_PROG_OK = 2;
	static final int RESULT_PROG_NOK = 3;
	
	static final String DIR_PVR = "/pvr/";
	
	static ProgressDialog progressDialog = null;
	
	static String curId = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.pvr);
        FBMHttpConnection.initVars(this, null);
        FBMHttpConnection.FBMLog("ENREGISTREMENTSACTIVITY CREATE");

        File old_db = getDatabasePath("freeboxmobile"+FBMHttpConnection.getIdentifiant());
        if (old_db.exists())
        {
        	FBMHttpConnection.FBMLog("PVR: Ancien nom de bdd sqlite, renommage en pvr_");
        	if (old_db.renameTo(getDatabasePath(EnregistrementsDbAdapter.DATABASE_NAME)))
        	{
        		FBMHttpConnection.FBMLog("OK ");
        	} else
        	{
        		FBMHttpConnection.FBMLog("KO");
        	}
        }
        if (!curId.equals(FBMHttpConnection.getIdentifiant()))
        {
        	curId = FBMHttpConnection.getIdentifiant();
        	reset();
        }
        else
        {
        	listeEnregistrements = new ListeEnregistrements();
        }
        succesChargement = false;
        enrAct = this;
        
        registerForContextMenu(getExpandableListView());

        setTheme(android.R.style.Theme_Light);
        setTitle(getString(R.string.app_name) + " " + getString(R.string.pvrPVR)
        		+ " - "+FBMHttpConnection.getTitle());
        
        ((Button) findViewById(R.id.pvrBtnProg)).setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
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
	protected void onPause()
	{
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		super.onPause();
	}
    
	@Override
	protected void onStart()
	{
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
    	if (listeEnregistrements != null)
    	{
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
        	if (listeEnregistrements != null)
        	{
        		listeEnregistrements.vider();
        	} else
        	{
        		listeEnregistrements = new ListeEnregistrements();
        	}
            
        	if (updateFromConsole)
        	{
        		listeEnregistrements.vider();
        		succesChargement = EnregistrementsNetwork.updateEnregistrementsFromConsole(enrAct);
        		return succesChargement;
        	}
			return Boolean.TRUE;
        }
        
        protected void onPostExecute(Boolean succes) {
        	if (succes == Boolean.TRUE)
        	{
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

		if (listCursor != null && listCursor.moveToFirst())
		{
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
    private void afficherEnregistrements()
    {
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;

    	menu.setHeaderTitle(listeEnregistrements.get((int)info.id));
    	menu.add(0, CMENU_MODIF, 0, getString(R.string.pvrCMenuModif));
    	menu.add(0, CMENU_SUPPR, 0, getString(R.string.pvrCMenuSuppr));
    	menu.add(0, CMENU_SHARE, 0, "Partager");
    }

    public boolean onContextItemSelected(MenuItem item)
    {
    	final ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
    	int itemId = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		switch (item.getItemId()) {
			case CMENU_MODIF:
				afficherEnregistrementActivity(new Intent(this, ProgrammationActivity.class), itemId, ACTIVITY_PROGRAMMATION);
				return true;
			case CMENU_SUPPR:
    			SupprimerEnregistrement(enrAct, getRowIdFromItemId(itemId));
				return true;
			case CMENU_SHARE:
				PartagerEnregistrement(itemId);
			default:
				return super.onContextItemSelected(item);
		}
    }
    
    private void PartagerEnregistrement(int itemId)
    {
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
        db.open();
        Cursor c = db.fetchAllEnregistrements(new String[] {
        		EnregistrementsDbAdapter.KEY_ROWID ,
        		EnregistrementsDbAdapter.KEY_IDE,
        		EnregistrementsDbAdapter.KEY_CHAINE,
        		EnregistrementsDbAdapter.KEY_DATE,
        		EnregistrementsDbAdapter.KEY_HEURE,
        		EnregistrementsDbAdapter.KEY_NOM
        },
        		EnregistrementsDbAdapter.KEY_DATE + " ASC, "
        		+ EnregistrementsDbAdapter.KEY_HEURE + " ASC, "
        		+ EnregistrementsDbAdapter.KEY_MIN + " ASC");
        c.moveToPosition(itemId);
        long rowId = c.getLong(c.getColumnIndex(EnregistrementsDbAdapter.KEY_ROWID));
        FBMHttpConnection.FBMLog("ROWID : "+rowId+" ITEMID:"+itemId+
        "IDE : "+c.getLong(c.getColumnIndex(EnregistrementsDbAdapter.KEY_IDE))
        );
		String text = "J'enregistre sur ma Freebox '"+ 
				c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_NOM))+"' "+
				"sur "+c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE))+
				" le "+c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE))+
				" à "+c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_HEURE))+
				"\n\nPartagé par FreeboxMobile pour Android.";
        Intent i = new Intent(Intent.ACTION_SEND)
		.putExtra(Intent.EXTRA_TEXT, text).setType("text/plain")
		.putExtra(Intent.EXTRA_SUBJECT, "EnregistrementTV partagé par FreeboxMobile")
//			    		.addCategory(Intent.CATEGORY_DEFAULT)
		;
        c.close();
        db.close();
    	startActivityForResult(Intent.createChooser(i, "Partagez ce programme avec"),0);
    }
    
    private long getRowIdFromItemId(int itemId)
    {
    	// Récupération de l'id
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
        db.open();
        Cursor c = db.fetchAllEnregistrements(new String[] { EnregistrementsDbAdapter.KEY_ROWID , EnregistrementsDbAdapter.KEY_IDE},
        		EnregistrementsDbAdapter.KEY_DATE + " ASC, "
        		+ EnregistrementsDbAdapter.KEY_HEURE + " ASC, "
        		+ EnregistrementsDbAdapter.KEY_MIN + " ASC");
        c.moveToPosition(itemId);
        long rowId = c.getLong(c.getColumnIndex(EnregistrementsDbAdapter.KEY_ROWID));
        FBMHttpConnection.FBMLog("ROWID : "+rowId+" ITEMID:"+itemId+
        "IDE : "+c.getLong(c.getColumnIndex(EnregistrementsDbAdapter.KEY_IDE))
        );
        c.close();
        db.close();
        return rowId;
    }

    private void afficherEnregistrementActivity(Intent i, int itemId, int action)
    {
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
	private class ListeEnregistrements
	{
		private List<String> listeEnregistrements = null;
		private List<List<String>> detailsEnregistrements = null;
		
		ListeEnregistrements()
		{
			listeEnregistrements = new ArrayList<String>();
			detailsEnregistrements = new ArrayList<List<String>>();
		}
		
		public String get(int i)
		{
			if (listeEnregistrements.size() > i)
			{
				return listeEnregistrements.get(i);
			}
			else
			{
				return "";
			}
		}
		
	    public void vider()
	    {
	    	listeEnregistrements.clear();
	    	detailsEnregistrements.clear();
	    }
	    
	    // Ajout d'un enregistrement à la liste, avec détails
	    public void ajouter(String nom, List<String> details)
	    {
	    	listeEnregistrements.add(nom);
	    	detailsEnregistrements.add(details);
	    }
	    
	    // Crée la liste des enregistrements
		public List<HashMap<String, String>> createGroupList()
		{
			ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
			for( int i = 0 ; i < listeEnregistrements.size() ; ++i )
			{
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("enregistrement", listeEnregistrements.get(i));
				result.add(map);
			}
			return result;
		}
		
		// Crée la liste des détails pour chaque enregistrement
		public List<ArrayList<HashMap<String, String>>> createChildList()
		{
			ArrayList<ArrayList<HashMap<String, String>>> result = new ArrayList<ArrayList<HashMap<String, String>>>();

			for( int i = 0 ; i < detailsEnregistrements.size() ; ++i )
			{
				ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();
				for( int n = 0 ;
					detailsEnregistrements.get(i) != null && n < detailsEnregistrements.get(i).size();
					n += 2)
				{
					HashMap<String, String> detail = new HashMap<String, String>();
					detail.put("key", detailsEnregistrements.get(i).get(n));
					if (detailsEnregistrements.get(i).size() > n+1)
					{
						detail.put("value", detailsEnregistrements.get(i).get(n+1));
					}
					secList.add(detail);
				}
				result.add(secList);
			}
			return result;
		}
	}
	
	private static void doSuppression(Activity activity, long rowId)
	{
        // Vars
		List<NameValuePair> postVars = new ArrayList<NameValuePair>();
		String ide, chaine_id, service_id, date, h, min, dur, name, where_id, repeat_a;
		    			
		// Post vars pour suppression
    	// ide=11&chaine_id=6&service_id=0&date=31%2F12%2F2009&h=23
    	// &min=09&dur=15&name=titre&where_id=0&repeat_a=&supp=Supprimer
		
		// DB
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(activity);
        db.open();
        Cursor c = db.fetchEnregistrement(rowId);
        db.close();
        
        if (c == null || c.moveToFirst() == false) {
        	return;
        }

		ide = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_IDE));
		chaine_id = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE_ID));
		service_id = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_SERVICE_ID));
		date = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE));
		h = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_H));
		min = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_MIN));
		dur = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DUR));
		name = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_NAME));
		where_id = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_WHERE_ID));
		repeat_a = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_REPEAT_A));
		
		c.close();

		// Creation des variables POST
		postVars.add(new BasicNameValuePair("ide", ide));
		postVars.add(new BasicNameValuePair("chaine_id", chaine_id));
		postVars.add(new BasicNameValuePair("service_id", service_id));
		postVars.add(new BasicNameValuePair("date", date));
		postVars.add(new BasicNameValuePair("h", h));
		postVars.add(new BasicNameValuePair("min", min));
		postVars.add(new BasicNameValuePair("dur", dur));
		postVars.add(new BasicNameValuePair("name", name));
		postVars.add(new BasicNameValuePair("where_id", where_id));
		postVars.add(new BasicNameValuePair("repeat_a", repeat_a));

		postVars.add(new BasicNameValuePair("supp", "Supprimer"));
		
		// Requete HTTP
		String url = "http://adsl.free.fr/admin/magneto.pl";
		FBMHttpConnection.postAuthRequest(url, postVars, true, false);
	}

	public static void SupprimerEnregistrement (final Activity activity, final long rowId)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(activity.getString(R.string.pvrConfirmationSuppression))
		       .setCancelable(false)
		       .setPositiveButton(R.string.oui, new DialogInterface.OnClickListener()
		       {
		           public void onClick(DialogInterface dialog, int id)
		           {
		    			new DeleteEnregistrementTask(activity).execute(rowId);
		           }
		       })
		       .setNegativeButton(R.string.non, new DialogInterface.OnClickListener()
		       {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	static class DeleteEnregistrementTask extends AsyncTask<Long, Integer, Void> {
		Activity activity = null;
		long rowId;
		
		DeleteEnregistrementTask(Activity a)
		{
			activity = a;
		}

		protected void onPreExecute() {
	    	progressDialog = ProgressDialog.show(activity, activity.getString(R.string.pvrPatientez),
	    			activity.getString(R.string.pvrSuppressionEnCours), true, false);
		}

		@Override
		protected Void doInBackground(Long... params)
		{
			rowId = params[0];
			doSuppression(activity, rowId);
			
			// TODO: vérifier la réponse de free, si la suppression a bien
			// été faite!
			
			EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(activity);
			db.open();
            db.deleteEnregistrement(rowId);
            db.close();

			return null;
		}
		
		protected void onPostExecute(Void v)
		{
			progressDialog.dismiss();
			progressDialog = null;
			((EnregistrementsActivity) activity).updaterEnregistrements(false);
		}
	}
}
