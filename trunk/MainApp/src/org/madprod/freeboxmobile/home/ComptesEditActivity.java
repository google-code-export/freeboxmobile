package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;

import static org.madprod.freeboxmobile.StaticConstants.TAG;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

	GoogleAnalyticsTracker tracker;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Home/CompteEdit");
		
        FBMNetTask.register(this);

    	mDbHelper = new ComptesDbAdapter(ComptesEditActivity.this);
    	mDbHelper.open();

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
                String user;
                String password = mPasswordText.getText().toString();
              	user = mUserText.getText().toString();

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
                		// TODO : peut être ne pas appeler ManageCompte si le compte n'est pas le premier
		                new ManageCompte(new ComptePayload(title, user, password, /*type,*/ mRowId, false)).execute(); // COMPTES_TYPE_ADSL
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
            mTitleText.setText(compte.getString(compte.getColumnIndexOrThrow(ComptesDbAdapter.KEY_TITLE)));
            mUserText.setText(compte.getString(compte.getColumnIndexOrThrow(ComptesDbAdapter.KEY_USER)));
            mPasswordText.setText(compte.getString(compte.getColumnIndexOrThrow(ComptesDbAdapter.KEY_PASSWORD)));            
            String type =  compte.getString(compte.getColumnIndexOrThrow(ComptesDbAdapter.KEY_LINETYPE));
            compte.close();
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
        // TODO : checker si on fait un screen rotation, est-ce que ca efface les données ?
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
    	FBMNetTask.unregister(this);
    	mDbHelper.close();
    	tracker.stop();
        super.onDestroy();
    }
}
