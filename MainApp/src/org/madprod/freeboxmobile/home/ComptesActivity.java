package org.madprod.freeboxmobile.home;

import java.util.Date; 

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.guide.GuideCheck;

import static org.madprod.freeboxmobile.StaticConstants.TAG;
import static org.madprod.freeboxmobile.StaticConstants.KEY_PREFS;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class ComptesActivity extends ListActivity implements HomeConstants
{
    private static final int COMPTE_CREATE=0;
    private static final int COMPTE_EDIT=1;

	private ComptesDbAdapter mDbHelper = null;
	private Cursor mComptesCursor = null;
	private SimpleCursorAdapter comptesAdapter = null;

	GoogleAnalyticsTracker tracker;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
		
    	tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Home/ComptesList");
		
        Log.d(TAG, "onCreate");

    	FBMNetTask.register(this);
    	if (mDbHelper == null)
    	{
	        mDbHelper = new ComptesDbAdapter(this);
	        mDbHelper.open();
    	}
        setContentView(R.layout.comptes_list);
        registerForContextMenu(getListView());
        setTitle(getString(R.string.app_name)+" - Comptes Freebox");
        TextView t = (TextView) findViewById(R.id.Compte_Actif);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t = (TextView) findViewById(R.id.Compte_Liste);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        Button newButton = (Button) findViewById(R.id.comptes_button_new);
        newButton.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View view)
        	{
        		createCompte();
        	}
        });
        Button backButton = (Button) findViewById(R.id.comptes_button_retour);
        backButton.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View view)
        	{
        		finish();
        	}
        });
    }

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
    	AdapterContextMenuInfo info;

		super.onCreateContextMenu(menu, v, menuInfo);
		info = (AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle("Compte : "+mDbHelper.getValueFromId((int) info.id, KEY_TITLE));
        menu.add(0, COMPTES_OPTION_MODIFY, 0, R.string.comptes_option_modify);
        menu.add(0, COMPTES_OPTION_DELETE, 1, R.string.comptes_option_delete);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item)
    {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId())
		{
			case COMPTES_OPTION_MODIFY:
		        Intent i = new Intent(this, ComptesEditActivity.class);
		        i.putExtra(ComptesDbAdapter.KEY_ROWID, info.id);
		        startActivityForResult(i, COMPTE_EDIT);
				break;

    		case COMPTES_OPTION_DELETE:
    			String selected = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null);
    			if (mDbHelper.getComptesNumber() < 2)
    			{
                	Toast t = Toast.makeText(ComptesActivity.this, "Il doit rester au moins un compte !",Toast.LENGTH_LONG);
                	t.show();
    			}
    			else
    			{
    				if (mDbHelper.getValueFromId((int) info.id, KEY_USER).equals(selected))
	    			{
	                	Toast t = Toast.makeText(ComptesActivity.this, "Impossible de supprimer le compte actif !",Toast.LENGTH_LONG);
	                	t.show();
	    			}
	    			else
	    			{
		    			mDbHelper.deleteCompte(info.id);
		    			fillData();
	    			}
    			}
    			return true;
		}
		return super.onContextItemSelected(item);
	}

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, ComptesEditActivity.class);
        i.putExtra(ComptesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, COMPTE_EDIT);
    }

    @Override
    protected void onStart()
    {
    	super.onStart();
    }
    
    @Override
    protected void onStop()
	{
    	if (mComptesCursor != null)
    	{
    		mComptesCursor.close();
    		mComptesCursor = null;
    	}
    	super.onStop();
	}

    @Override
    protected void onDestroy()
    {
    	if (mDbHelper != null)
    	{
    		mDbHelper.close();
    		mDbHelper = null;
    	}
    	FBMNetTask.unregister(this);
    	tracker.stop();
    	super.onDestroy();
    }

    @Override
    protected void onPause()
    {
    	super.onPause();
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
        fillData();
    }

    private void createCompte()
    {
        startActivityForResult(new Intent(this, ComptesEditActivity.class), COMPTE_CREATE);
    }

    private void fillData()
    {
    	Log.d(TAG,"FILLDATA");
        mComptesCursor = mDbHelper.fetchAllComptes();
        String[] from = new String[]{KEY_TITLE};
        int[] to = new int[]{R.id.comptes_liste_row};
        comptesAdapter = new SimpleCursorAdapter(this, R.layout.comptes_row, mComptesCursor, from, to);
        setListAdapter(comptesAdapter);

        Spinner s = (Spinner) findViewById(R.id.Spinner01);
        ArrayAdapter<String> liste = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        liste.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(liste);
        int titleColumnIndex = mComptesCursor.getColumnIndexOrThrow(KEY_TITLE);
        int loginColumnIndex = mComptesCursor.getColumnIndexOrThrow(KEY_USER);
        int pos = 0;
        String selected = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null);
        if (selected == null)
        {
        	liste.add("-- pas de compte selectionné --");
        }

        if (mComptesCursor.moveToFirst())
        {
        	do
        	{
        		liste.add(mComptesCursor.getString(titleColumnIndex));
        		if (mComptesCursor.getString(loginColumnIndex).equals(selected))
        		{
        			s.setSelection(pos, true);
        		}
        		pos++;
        	}
       		while (mComptesCursor.moveToNext());
        }
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int i, long l)
			{
				Cursor c = mDbHelper.fetchFromTitle(parent.getSelectedItem().toString());
				if ((c.getCount() > 0) && !c.getString(c.getColumnIndexOrThrow(KEY_USER)).equals(FBMHttpConnection.getIdentifiant()))
				{
					if (c.getCount() > 0)
					{
						SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
						Long duree = (new Date()).getTime() - getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getLong(KEY_LAST_REFRESH+c.getString(c.getColumnIndexOrThrow(KEY_USER)), 0);
						Log.d(TAG,"TEMPS : "+c.getString(c.getColumnIndexOrThrow(KEY_USER))+" - "+getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getLong(KEY_LAST_REFRESH+c.getString(c.getColumnIndexOrThrow(KEY_USER)), 0)+" "+duree);
						
						// Si ca fait + de 30 jours on met à jour (2592000000 )
						if (duree > 2592000000L)
						{
							updatePrefs(mgr.edit(), c);
					        new ManageCompte(new ComptePayload(
					        		mgr.getString(KEY_TITLE, ""),
					        		mgr.getString(KEY_USER, ""),
					        		mgr.getString(KEY_PASSWORD, ""),
					        		/*(mgr.getString(KEY_LINETYPE, "0").equals(LINE_TYPE_FBXOPTIQUE) ? COMPTES_TYPE_FO : COMPTES_TYPE_ADSL),*/
						        	null, true)).execute();
						}
						else
						{
							updatePrefs(mgr.edit(), c);
						}
					}
					else
					{
	                	Toast t = Toast.makeText(ComptesActivity.this, "Impossible de selectionner ce compte",Toast.LENGTH_LONG);
	                	t.show();
					}
				}
				c.close();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
        });
    }
    
    private void updatePrefs(Editor editor, Cursor c)
    {
		editor.putString(KEY_USER, c.getString(c.getColumnIndexOrThrow(KEY_USER)));
		editor.putString(KEY_PASSWORD, c.getString(c.getColumnIndexOrThrow(KEY_PASSWORD)));
		editor.putString(KEY_TITLE, c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
		editor.putString(KEY_NRA, c.getString(c.getColumnIndexOrThrow(KEY_NRA)));
		editor.putString(KEY_DSLAM, c.getString(c.getColumnIndexOrThrow(KEY_DSLAM)));
		editor.putString(KEY_IP, c.getString(c.getColumnIndexOrThrow(KEY_IP)));
		editor.putString(KEY_TEL, c.getString(c.getColumnIndexOrThrow(KEY_TEL)));
		editor.putString(KEY_LINELENGTH, c.getString(c.getColumnIndexOrThrow(KEY_LINELENGTH)));
		editor.putString(KEY_ATTN, c.getString(c.getColumnIndexOrThrow(KEY_ATTN)));
		editor.putString(KEY_LINETYPE, c.getString(c.getColumnIndexOrThrow(KEY_LINETYPE)));
		editor.putString(KEY_FBMVERSION, c.getString(c.getColumnIndexOrThrow(KEY_FBMVERSION)));
		editor.commit();
		FBMHttpConnection.initCompte(ComptesActivity.this);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        
        Log.d(TAG, "onActivityResult");
        FBMNetTask.register(this);
        // Si il n'y avait pas de compte défini et qu'un compte vient d'être créé, on le selectionne par défaut
        // et on installe les timers
        if ((getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null) == null) && (resultCode != 0))
        {
       		Long id = intent.getLongExtra(ComptesDbAdapter.KEY_ROWID, 0);
        	if (id != 0)
        	{
        		ComptesDbAdapter mDb = new ComptesDbAdapter(this).open();
				Cursor c = mDb.fetchCompte(id);
				if (c.getCount() > 0)
				{
					SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
					updatePrefs(mgr.edit(), c);
	            	Toast t = Toast.makeText(ComptesActivity.this, "Compte "+c.getString(c.getColumnIndexOrThrow(KEY_TITLE))+" selectionné",Toast.LENGTH_LONG);
	            	t.show();
				}
				c.close();
				mDb.close();

				String ms = getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_MEVO_PREFS_FREQ, "-1");
				if (!ms.equals("0")) // Si "0" : l'utilisateur ne veut pas de relève périodique
				{
//					if (!ms.equals("-1"))  // Si une valeur était mise
//					{
//						MevoSync.changeTimer(Integer.parseInt(ms), this);
//					}
//					else // Si pas configuré : valeur par défaut
//					{
//						MevoSync.changeTimer(DEFAULT_MEVO_FREQ, this);
//					}
				}
				GuideCheck.setTimer(this);
        	}
        }
        else
        {
        	FBMHttpConnection.initCompte(ComptesActivity.this);
        }
    }
}
