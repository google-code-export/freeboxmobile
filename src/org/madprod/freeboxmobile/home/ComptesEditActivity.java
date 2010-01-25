package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
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
        super.onCreate(savedInstanceState);
        
        FBMHttpConnection.initVars(ComptesEditActivity.this, null);

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

        if (mRowId == null)
        {
			Bundle extras = getIntent().getExtras();            
			mRowId = extras != null ? extras.getLong(ComptesDbAdapter.KEY_ROWID) : null;
        }
        if (mRowId != null && mRowId == -1)
        {
        	mRowId = null;
        	populateFieldsFromSaved(savedInstanceState);
        }
        else
        {
        	populateFieldsFromDb();
        }

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
		                new CheckFree().execute(new Payload(title, user, password));
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
    
    private void populateFieldsFromSaved(Bundle b)
    {
        if (b != null)
        {
            mTitleText.setText(b.getString(ComptesDbAdapter.KEY_TITLE));
            mUserText.setText(b.getString(ComptesDbAdapter.KEY_USER));
            mPasswordText.setText(b.getString(ComptesDbAdapter.KEY_PASSWORD));
        }
    }

    private void populateFieldsFromDb()
    {
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
        super.onSaveInstanceState(outState);
        if (mRowId != null)
        {
        	outState.putLong(ComptesDbAdapter.KEY_ROWID, mRowId);
        }
        else
        {
        	outState.putLong(ComptesDbAdapter.KEY_ROWID, -1);
        }
    	outState.putString(ComptesDbAdapter.KEY_TITLE, mTitleText.getText().toString());
    	outState.putString(ComptesDbAdapter.KEY_USER, mUserText.getText().toString());
    	outState.putString(ComptesDbAdapter.KEY_PASSWORD, mPasswordText.getText().toString());
    }

    @Override
    protected void onPause()
    {
        super.onPause();
//        saveState();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
    	mDbHelper.close();
    	FBMHttpConnection.closeDisplay();
        super.onDestroy();
    }

    private void saveState(Payload p)
    {
        Bundle bundle = new Bundle();
        Intent mIntent = new Intent();

    	ContentValues v = p.result;
 
    	if (ComptesEditActivity.exit == RESULT_OK)
    	{
        	Log.d(DEBUGTAG, "ComptesEditActivity RESULT_OK");

	        if (mRowId == null)
	        {
	            long id = mDbHelper.createCompte(p.title, p.login, p.password, (String) v.get(KEY_NRA),
	            		(String) v.get(KEY_DSLAM), (String) v.get(KEY_IP), (String) v.get(KEY_LINELENGTH),
	            		(String) v.get(KEY_ATTN), (String) v.get(KEY_TEL));
	            if (id > 0)
	            {
	                mRowId = id;
	            }
	        }
	        else
	        {
	            mDbHelper.updateCompte(mRowId, p.title, p.login, p.password, (String) v.get(KEY_NRA),
	            		(String) v.get(KEY_DSLAM), (String) v.get(KEY_IP), (String) v.get(KEY_LINELENGTH),
	            		(String) v.get(KEY_ATTN), (String) v.get(KEY_TEL));
	        }

            bundle.putLong(ComptesDbAdapter.KEY_ROWID, mRowId);
/* NOT USED FOR NOW
 	        bundle.putString(ComptesDbAdapter.KEY_TITLE, title);
	        bundle.putString(ComptesDbAdapter.KEY_USER, login);
	        bundle.putString(ComptesDbAdapter.KEY_PASSWORD, password);
*/
    	}
    	else
    	{
/* NOT USED FOR NOW
	        bundle.putString(ComptesDbAdapter.KEY_TITLE, null);
	        bundle.putString(ComptesDbAdapter.KEY_USER, null);
	        bundle.putString(ComptesDbAdapter.KEY_PASSWORD, null);
*/
            bundle.putLong(ComptesDbAdapter.KEY_ROWID, 0);
    	}
        mIntent.putExtras(bundle);
        setResult(ComptesEditActivity.exit, mIntent);
    }

    public static class Payload
    {
    	public String title;
        public String login;
        public String password;
//        public Intent intent;
        public ContentValues result;
        
        public Payload(String title, String login, String password)
        {
        	this.title = title;
        	this.login = login;
        	this.password = password;
        }
    }

    private class CheckFree extends AsyncTask<Payload, Void, Payload> implements Constants
    {
    	@Override
    	protected Payload doInBackground(Payload... payload)
    	{
    		payload[0].result = FBMHttpConnection.connectFreeCheck(payload[0].login, payload[0].password);
    		return payload[0];
    	}

		@Override
    	protected void onPreExecute()
    	{
    		FBMHttpConnection.showProgressDialog(ComptesEditActivity.this);
    	}

    	@Override
    	protected void onPostExecute(Payload payload)
    	{
    		FBMHttpConnection.dismissPd();
    		if (payload.result != null)
    		{
   				ComptesEditActivity.exit = RESULT_OK;
   				saveState(payload);
   				finish();
    		}
    		else
    		{
   				FBMHttpConnection.showError(ComptesEditActivity.this);
   				ComptesEditActivity.exit = RESULT_CANCELED;
    		}
    	}
    }
}
