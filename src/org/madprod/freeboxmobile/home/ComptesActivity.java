package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
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

	private ComptesDbAdapter mDbHelper;
    private Cursor mComptesCursor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.comptes_list);
        mDbHelper = new ComptesDbAdapter(this);
        mDbHelper.open();
        registerForContextMenu(getListView());
        setTitle(getString(R.string.app_name)+" - Comptes Freebox");
        Button newButton = (Button) findViewById(R.id.comptes_button_new);
        newButton.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View view)
        	{
        		createCompte();
        	}
        });
    }

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
		super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, COMPTES_OPTION_DELETE, 0, R.string.comptes_option_delete);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item)
    {
		switch(item.getItemId())
		{
    		case COMPTES_OPTION_DELETE:
    			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    			String selected = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null);
    			if (mDbHelper.getComptesNumber() < 2)
    			{
                	Toast t = Toast.makeText(ComptesActivity.this, "Il doit rester au moins un compte !",Toast.LENGTH_LONG);
                	t.show();
    			}
    			else
    			{
    				if (mDbHelper.getLoginFromId((int) info.id).equals(selected))
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
        fillData();
    }
    
    @Override
    protected void onStop()
	{
    	super.onStop();
	}

    @Override
    protected void onDestroy()
    {
    	mDbHelper.close();
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
    }

    private void createCompte()
    {
        Intent i = new Intent(this, ComptesEditActivity.class);
        startActivityForResult(i, COMPTE_CREATE);
    }

    private void fillData()
    {
        mComptesCursor = mDbHelper.fetchAllComptes();
        startManagingCursor(mComptesCursor);
        String[] from = new String[]{KEY_TITLE};
        int[] to = new int[]{R.id.comptes_liste_row};
        SimpleCursorAdapter comptes = new SimpleCursorAdapter(this, R.layout.comptes_row, mComptesCursor, from, to);
        setListAdapter(comptes);

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
        			s.setSelection(pos);
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
				startManagingCursor(c);
				if (c.getCount() > 0)
				{
					SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
					Editor editor = mgr.edit();
					editor.putString(KEY_USER, c.getString(c.getColumnIndexOrThrow(KEY_USER)));
					editor.putString(KEY_PASSWORD, c.getString(c.getColumnIndexOrThrow(KEY_PASSWORD)));
					editor.putString(KEY_TITLE, c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
					editor.putString(KEY_NRA, c.getString(c.getColumnIndexOrThrow(KEY_NRA)));
					editor.putString(KEY_DSLAM, c.getString(c.getColumnIndexOrThrow(KEY_DSLAM)));
					editor.putString(KEY_IP, c.getString(c.getColumnIndexOrThrow(KEY_IP)));
					editor.putString(KEY_TEL, c.getString(c.getColumnIndexOrThrow(KEY_TEL)));
					editor.putString(KEY_LENGTH, c.getString(c.getColumnIndexOrThrow(KEY_LENGTH)));
					editor.putString(KEY_ATTN, c.getString(c.getColumnIndexOrThrow(KEY_ATTN)));
					editor.commit();
					FBMHttpConnection.initCompte(ComptesActivity.this);
				}
				else
				{
                	Toast t = Toast.makeText(ComptesActivity.this, "Impossible de selectionner ce compte",Toast.LENGTH_LONG);
                	t.show();
				}
				c.close();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// TODO Auto-generated method stub	
			}
        });
        //mComptesCursor.close();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        // Si il n'y avait pas de compte définit et qu'un compte vient d'être créé, on le selectionne par défaut
        if ((getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null) == null) && (resultCode != 0))
        {
       		Long id = intent.getLongExtra(ComptesDbAdapter.KEY_ROWID, 0);
        	if (id != 0)
        	{
				Cursor c = mDbHelper.fetchCompte(id);
				startManagingCursor(c);
				if (c.getCount() > 0)
				{
					SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
					Editor editor = mgr.edit();
					editor.putString(KEY_USER, c.getString(c.getColumnIndexOrThrow(KEY_USER)));
					editor.putString(KEY_PASSWORD,  c.getString(c.getColumnIndexOrThrow(KEY_PASSWORD)));
					editor.putString(KEY_TITLE, c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
					editor.putString(KEY_NRA, c.getString(c.getColumnIndexOrThrow(KEY_NRA)));
					editor.putString(KEY_DSLAM, c.getString(c.getColumnIndexOrThrow(KEY_DSLAM)));
					editor.putString(KEY_IP, c.getString(c.getColumnIndexOrThrow(KEY_IP)));
					editor.putString(KEY_TEL, c.getString(c.getColumnIndexOrThrow(KEY_TEL)));
					editor.putString(KEY_LENGTH, c.getString(c.getColumnIndexOrThrow(KEY_LENGTH)));
					editor.putString(KEY_ATTN, c.getString(c.getColumnIndexOrThrow(KEY_ATTN)));
					editor.commit();
					FBMHttpConnection.initCompte(this);
	            	Toast t = Toast.makeText(ComptesActivity.this, "Compte "+c.getString(c.getColumnIndexOrThrow(KEY_TITLE))+" selectionné",Toast.LENGTH_LONG);
	            	t.show();
				}
				c.close();
        	}
        }
        fillData();
    }
}
