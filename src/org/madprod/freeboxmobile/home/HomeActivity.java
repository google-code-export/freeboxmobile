package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.HttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class HomeActivity extends Activity implements HomeConstants
{
	private Activity homeActivity;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		Log.d(DEBUGTAG,"MainActivity Create "+getString(R.string.app_version));
        super.onCreate(savedInstanceState);

		// On teste si on est dans le cas d'un premier lancement pour cette version de l'appli
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);

		// Simulate first launch (for test only, these lines have to be commented for release)
/*		editor.remove(KEY_MEVO_PREFS_FREQ);
		editor.remove(KEY_SPLASH);
		editor.commit();
		editor = mgr.edit();
*/
		if (!mgr.getString(KEY_SPLASH, "0").equals(getString(R.string.app_version)))
		{
	        Log.d(DEBUGTAG,Environment.getExternalStorageDirectory().toString()+"/freeboxmobile");
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH, getString(R.string.app_version));
			editor.commit();
			displayAbout();
		}

        homeActivity = this;
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == false)
        	showSdCardError();
		setContentView(R.layout.home_main);
		HttpConnection.initVars(this);

        Button phoneButton = (Button) findViewById(R.id.phone);
        Button aboutButton = (Button) findViewById(R.id.about);
        Button pvrButton = (Button) findViewById(R.id.magneto);
		phoneButton.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
				    	Intent i = new Intent(homeActivity, org.madprod.freeboxmobile.mvv.MevoActivity.class);
				    	startActivity(i);
					}
				}
			);
		aboutButton.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
						displayAbout();
					}
				}
			);

		pvrButton.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
				    	Intent i = new Intent(homeActivity, org.madprod.freeboxmobile.pvr.PvrActivity.class);
				    	startActivity(i);
					}
				}
			);

    }

    @Override
    protected void onStart()
    {
    	Log.d(DEBUGTAG,"MainActivity Start");
    	super.onStart();
    }
    
    @Override
    protected void onStop()
	{
    	Log.d(DEBUGTAG,"MainActivity Stop");
		super.onStop();
//		this.mDbHelper.close();
	}

    @Override
    protected void onDestroy()
    {
    	Log.d(DEBUGTAG,"MainActivity Destroy");
    	super.onDestroy();
    }

    @Override
    protected void onPause()
    {
		Log.d(DEBUGTAG,"MainActivity Pause");
    	super.onPause();
		HttpConnection.closeDisplay();
    }
    
    @Override
    protected void onResume()
    {
		Log.d(DEBUGTAG,"MainActivity Resume");
    	super.onResume();
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, HOME_OPTION_COMPTES, 0, R.string.home_option_comptes).setIcon(android.R.drawable.ic_menu_myplaces);
        menu.add(0, HOME_OPTION_CONFIG, 1, R.string.home_option_config).setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, HOME_OPTION_SHARE, 1, R.string.home_option_share).setIcon(android.R.drawable.ic_menu_share);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	Intent i;

    	switch (item.getItemId())
    	{
    		case HOME_OPTION_COMPTES:
    			i = new Intent(this, ComptesActivity.class);
		    	startActivityForResult(i, ACTIVITY_COMPTES);
    			return true;
    		case HOME_OPTION_CONFIG:
		    	i = new Intent();
		    	i.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.Config");
		    	startActivity(i);
		    	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void displayAbout()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name));
		d.setMessage(
			"Freebox Mobile est une application "+
			"indépendante de Free.\n\nPlus de renseignements sur "+
			"http://code.google.com/p/freeboxmobile/\n\n"+
			"Version : "+getString(R.string.app_version)
		);
		d.setButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			}
		);
		d.show();
    }

    private void showSdCardError()
    {
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("SDCard Necessaire !");
		d.setMessage(
			"Une carte SD est nécessaire afin d'utiliser "+
			"cette application."
		);
		d.setButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			}
		);
		d.show();      
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode)
        {
        	case ACTIVITY_COMPTES:
            	if (HttpConnection.checkUpdated(
            			getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null),
            			getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_PASSWORD, null)
            			))
            	{
            		HttpConnection.initVars(homeActivity);
//            		new ConnectFree().execute();
            	}
        		break;
        }
    }
}