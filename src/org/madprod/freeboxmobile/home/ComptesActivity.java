package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.R;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

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
    
//	private int mCompteNumber = 1;
	private ComptesDbAdapter mDbHelper;
    private Cursor mComptesCursor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	Log.d(DEBUGTAG,"Compte onCreate");
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.comptes_list);
        mDbHelper = new ComptesDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, COMPTES_OPTION_NEW, 0, R.string.comptes_option_new);
        return result;
    }

    // TODO : GERE LE DELETE
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
		super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, COMPTES_OPTION_DELETE, 0, R.string.comptes_option_delete);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
	    	case COMPTES_OPTION_NEW:
	    		createCompte();
	            return true;
        }
        return super.onOptionsItemSelected(item);
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
    	Log.d(DEBUGTAG,"Compte onStop");
    	super.onStop();
	}

    @Override
    protected void onDestroy()
    {
    	Log.d(DEBUGTAG,"Compte onDestroy");
    	mDbHelper.close();
    	super.onDestroy();
    }

    @Override
    protected void onPause()
    {
    	Log.d(DEBUGTAG,"Compte onPause");
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
        String[] from = new String[]{ComptesDbAdapter.KEY_TITLE};
        int[] to = new int[]{R.id.text1};
        SimpleCursorAdapter comptes = new SimpleCursorAdapter(this, R.layout.comptes_row, mComptesCursor, from, to);
        setListAdapter(comptes);
//        stopManagingCursor(mComptesCursor);
    }
}
