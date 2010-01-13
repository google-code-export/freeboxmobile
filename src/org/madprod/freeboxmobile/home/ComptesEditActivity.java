package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.HttpConnection;
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
    private EditText mLoginText;
    private EditText mPasswordText;
    private Long mRowId;
    private ComptesDbAdapter mDbHelper;
    private static int exit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new ComptesDbAdapter(this);
        mDbHelper.open();
        exit = RESULT_CANCELED;
        
        setContentView(R.layout.comptes_edit);
       
        mTitleText = (EditText) findViewById(R.id.comptes_edit_nom);
        mLoginText = (EditText) findViewById(R.id.comptes_edit_login);
        mPasswordText = (EditText) findViewById(R.id.comptes_edit_password);

        Button confirmButton = (Button) findViewById(R.id.comptes_button_ok);
        Button cancelButton = (Button) findViewById(R.id.comptes_button_cancel);
       
        mRowId = savedInstanceState != null ? savedInstanceState.getLong(ComptesDbAdapter.KEY_ROWID) : null;
        if (mRowId == null)
        {
			Bundle extras = getIntent().getExtras();            
			mRowId = extras != null ? extras.getLong(ComptesDbAdapter.KEY_ROWID) : null;
        }

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
                String login = mLoginText.getText().toString();
                String password = mPasswordText.getText().toString();
                Log.d(DEBUGTAG, "ici <"+title+">");

                if (!title.equals("") && !login.equals("") && !password.equals(""))
                {
	                bundle.putString(ComptesDbAdapter.KEY_TITLE, mTitleText.getText().toString());
	                bundle.putString(ComptesDbAdapter.KEY_LOGIN, mLoginText.getText().toString());
	                bundle.putString(ComptesDbAdapter.KEY_PASSWORD, mPasswordText.getText().toString());
	                if (mRowId != null)
	                {
	                    bundle.putLong(ComptesDbAdapter.KEY_ROWID, mRowId);
	                }
	                // TODO : A QUOI SERT LE INTENT ?
	                Intent mIntent = new Intent();
	                mIntent.putExtras(bundle);
	                setResult(RESULT_OK, mIntent);
	                new CheckFree().execute(new Payload(login, password, mIntent));
                }
                else
                {
                	Toast t;
                	t = Toast.makeText(ComptesEditActivity.this, "Veuillez remplir tous les champs !",Toast.LENGTH_LONG);
                	t.show();
                }
            }
        });
    }
    
    private void populateFields()
    {
        if (mRowId != null)
        {
            Cursor compte = mDbHelper.fetchCompte(mRowId);
            startManagingCursor(compte);
            mTitleText.setText(compte.getString(compte.getColumnIndexOrThrow(ComptesDbAdapter.KEY_TITLE)));
            mLoginText.setText(compte.getString(compte.getColumnIndexOrThrow(ComptesDbAdapter.KEY_LOGIN)));
            mPasswordText.setText(compte.getString(compte.getColumnIndexOrThrow(ComptesDbAdapter.KEY_PASSWORD)));
//            stopManagingCursor(compte);
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(ComptesDbAdapter.KEY_ROWID, mRowId);
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        populateFields();
    }
    
    @Override
    protected void onDestroy()
    {
    	mDbHelper.close();
        super.onDestroy();
    }

    private void saveState()
    {
    	if (exit == RESULT_OK)
    	{
	        String title = mTitleText.getText().toString();
	        String login = mLoginText.getText().toString();
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
    		payload[0].result = HttpConnection.connectionFree(payload[0].login, payload[0].password, true);
    		return payload[0];
    	}

		@Override
    	protected void onPreExecute()
    	{
    		Log.d(DEBUGTAG,"onPreExecute");
    		HttpConnection.showProgressDialog(ComptesEditActivity.this);
    	}

    	@Override
    	protected void onPostExecute(Payload payload)
    	{
    		Log.d(DEBUGTAG,"onPostExecute");
    		HttpConnection.dismissPd();
    		switch (payload.result)
    		{
    			case CONNECT_CONNECTED:
    				ComptesEditActivity.exit = RESULT_OK;
    				finish();
    			break;
    			default:
    				HttpConnection.showError(ComptesEditActivity.this);
    				ComptesEditActivity.exit = RESULT_CANCELED;
    			break;
    		}
    	}
    }
}
