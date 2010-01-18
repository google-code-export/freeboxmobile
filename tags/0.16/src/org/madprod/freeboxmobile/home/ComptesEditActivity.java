package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class ComptesEditActivity extends Activity implements Constants
{
	private EditText mTitleText;
    private EditText mUserText;
    private EditText mPasswordText;
    private Long mRowId;
    private ComptesDbAdapter mDbHelper;
    private static int exit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	Log.d(DEBUGTAG,"EditActivity Create ");
        super.onCreate(savedInstanceState);
        
        FBMHttpConnection.initVars(ComptesEditActivity.this);

        mDbHelper = new ComptesDbAdapter(this);
        mDbHelper.open();
        exit = RESULT_CANCELED;

        setContentView(R.layout.comptes_edit);
        setTitle(getString(R.string.app_name)+" - Edition Compte Freebox");

        mTitleText = (EditText) findViewById(R.id.comptes_edit_nom);
        mUserText = (EditText) findViewById(R.id.comptes_edit_user);
        mPasswordText = (EditText) findViewById(R.id.comptes_edit_password);

        Button confirmButton = (Button) findViewById(R.id.comptes_button_ok);
        Button cancelButton = (Button) findViewById(R.id.comptes_button_cancel);

        mRowId = savedInstanceState != null ? savedInstanceState.getLong(ComptesDbAdapter.KEY_ROWID) : null;
        Log.d(DEBUGTAG,"rowid1 "+mRowId);
        if (mRowId == null)
        {
			Bundle extras = getIntent().getExtras();            
			mRowId = extras != null ? extras.getLong(ComptesDbAdapter.KEY_ROWID) : null;
        }
        if (mRowId != null && mRowId == -1)
        	mRowId = null;
        Log.d(DEBUGTAG,"rowid2 "+mRowId);
        populateFields();

        cancelButton.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View view)
        	{
        		finish();
        	}
        });
        confirmButton.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View view)
            {
                Bundle bundle = new Bundle();

                String title = mTitleText.getText().toString();
                String user = mUserText.getText().toString();
                String password = mPasswordText.getText().toString();
                Log.d(DEBUGTAG, "ici <"+title+">");

                if (!title.equals("") && !user.equals("") && !password.equals(""))
                {
                	if (mDbHelper.isValuePresent(ComptesDbAdapter.KEY_TITLE, title) &&
                		!mDbHelper.isMatch(mRowId, ComptesDbAdapter.KEY_TITLE, title))
                	{
                    	Toast t = Toast.makeText(ComptesEditActivity.this, "Un compte avec ce nom existe déjà !",Toast.LENGTH_LONG);
                    	t.show();
                	}
                	else if (mDbHelper.isValuePresent(ComptesDbAdapter.KEY_USER, user) &&
                			!mDbHelper.isMatch(mRowId, ComptesDbAdapter.KEY_USER, user))
                	{
                    	Toast t = Toast.makeText(ComptesEditActivity.this, "Un compte avec cet identifiant existe déjà !",Toast.LENGTH_LONG);
                    	t.show();                		
                	}
                	else
                	{
		                bundle.putString(ComptesDbAdapter.KEY_TITLE, title);
		                bundle.putString(ComptesDbAdapter.KEY_USER, user);
		                bundle.putString(ComptesDbAdapter.KEY_PASSWORD, password);
		                if (mRowId != null)
		                {
		                    bundle.putLong(ComptesDbAdapter.KEY_ROWID, mRowId);
		                }
		                // TODO : A QUOI SERT LE INTENT ?
		                Intent mIntent = new Intent();
		                mIntent.putExtras(bundle);
		                setResult(RESULT_OK, mIntent);
		                new CheckFree().execute(new Payload(user, password, mIntent));
                	}
                }
                else
                {
                	Toast t = Toast.makeText(ComptesEditActivity.this, "Veuillez remplir tous les champs !",Toast.LENGTH_LONG);
                	t.show();
                }
            }
        });
    }
    
    private void populateFields()
    {
    	Log.d(DEBUGTAG,"EditActivity PopulateFields "+mRowId+" "+mDbHelper);
        if (mRowId != null)
        {
            Cursor compte = mDbHelper.fetchCompte(mRowId);
            startManagingCursor(compte);
            mTitleText.setText(compte.getString(compte.getColumnIndexOrThrow(ComptesDbAdapter.KEY_TITLE)));
            mUserText.setText(compte.getString(compte.getColumnIndexOrThrow(ComptesDbAdapter.KEY_USER)));
            mPasswordText.setText(compte.getString(compte.getColumnIndexOrThrow(ComptesDbAdapter.KEY_PASSWORD)));
//            stopManagingCursor(compte);
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	Log.d(DEBUGTAG,"EditActivity SaveInstanceState "+ComptesDbAdapter.KEY_ROWID+" "+mRowId);
        super.onSaveInstanceState(outState);
        if (mRowId != null)
        {
        	outState.putLong(ComptesDbAdapter.KEY_ROWID, mRowId);
        }
        else
        {
        	outState.putLong(ComptesDbAdapter.KEY_ROWID, -1);
        }
    }
    
    @Override
    protected void onPause()
    {
    	Log.d(DEBUGTAG,"EditActivity Pause ");
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume()
    {
    	Log.d(DEBUGTAG,"EditActivity Resume ");
        super.onResume();
        populateFields();
    }
    
    @Override
    protected void onDestroy()
    {
    	Log.d(DEBUGTAG,"EditActivity Destroy ");
    	mDbHelper.close();
    	FBMHttpConnection.closeDisplay();
        super.onDestroy();
    }

    private void saveState()
    {
    	if (exit == RESULT_OK)
    	{
	        String title = mTitleText.getText().toString();
	        String login = mUserText.getText().toString();
	        String password = mPasswordText.getText().toString();
	
	        if (mRowId == null)
	        {
	            long id = mDbHelper.createCompte(title, login, password);
	            if (id > 0)
	            {
	                mRowId = id;
	            }
	        }
	        else
	        {
	            mDbHelper.updateCompte(mRowId, title, login, password);
	        }
    	}
    }

    public static class Payload
    {
        public String login;
        public String password;
        public Intent intent;
        public int result;
        
        public Payload(String login, String password, Intent intent)
        {
        	this.login = login;
        	this.password = password;
        	this.intent = intent;
        }
    }

    private class CheckFree extends AsyncTask<Payload, Void, Payload> implements Constants
    {
    	@Override
    	protected Payload doInBackground(Payload... payload)
    	{
    		payload[0].result = FBMHttpConnection.connectionFree(payload[0].login, payload[0].password, true);
    		return payload[0];
    	}

		@Override
    	protected void onPreExecute()
    	{
    		Log.d(DEBUGTAG,"onPreExecute");
    		FBMHttpConnection.showProgressDialog(ComptesEditActivity.this);
    	}

    	@Override
    	protected void onPostExecute(Payload payload)
    	{
    		Log.d(DEBUGTAG,"onPostExecute");
    		FBMHttpConnection.dismissPd();
    		switch (payload.result)
    		{
    			case CONNECT_CONNECTED:
    				ComptesEditActivity.exit = RESULT_OK;
    				finish();
    			break;
    			default:
    				FBMHttpConnection.showError(ComptesEditActivity.this);
    				ComptesEditActivity.exit = RESULT_CANCELED;
    			break;
    		}
    	}
    }
}
