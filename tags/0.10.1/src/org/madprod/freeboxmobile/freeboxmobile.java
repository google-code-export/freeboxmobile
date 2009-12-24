package org.madprod.freeboxmobile;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class freeboxmobile extends Activity implements Constants
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		Log.d(DEBUGTAG,"MainActivity Create "+getString(R.string.app_version));
        super.onCreate(savedInstanceState);

        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == false)
        	showSdCardError();
		setContentView(R.layout.main);
		HttpConnection.setActivity(this);
        Button phoneButton = (Button) findViewById(R.id.phone);
        Button configButton = (Button) findViewById(R.id.config);
        Button aboutButton = (Button) findViewById(R.id.about);
        configButton.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
				    	Intent i = new Intent();
				    	i.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.Config");
//				    	startActivityForResult(i, ACTIVITY_CONFIG);
				    	startActivity(i);
				    }
				}
			);
		phoneButton.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
				    	Intent i = new Intent();
				    	i.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.mvv.FreeboxMobileMevo");
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
		// On teste si on est dans le cas d'un premier lancement pour cette version de l'appli
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);

		// Simulate first launch (for test only, these lines have to be commented for release)
/*		Editor e = mgr.edit();
		e.remove(KEY_SPLASH);
		e.commit();
*/	
		if (!mgr.getString(KEY_SPLASH, "0").equals(getString(R.string.app_version)))
		{
	        Log.d(DEBUGTAG,Environment.getExternalStorageDirectory().toString()+"/freeboxmobile");

	        File file = new File(Environment.getExternalStorageDirectory().toString()+DIR_MEVO);
	        file.mkdirs();

			HttpConnection.refresh();

			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH, this.getString(R.string.app_version));
			editor.commit();
			displayAbout();
		}
    }

    @Override
    protected void onStart()
    {
    	Log.d(DEBUGTAG,"MainActivity Start");
    	super.onStart();
    	HttpConnection.setActivity(this);
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
    	HttpConnection.setActivity(null);
    }

    @Override
    protected void onPause()
    {
		Log.d(DEBUGTAG,"MainActivity Pause");
    	super.onPause();
    }
    
    @Override
    protected void onResume()
    {
		Log.d(DEBUGTAG,"MainActivity Resume");
    	super.onPause();
    }
    

    private void displayAbout()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name));
		d.setMessage(
			"Freebox Mobile est une application "+
			"indépendante de Free.\n\nPlus de renseignements sur "+
			"http://code.google.com/p/freeboxmobile/"
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
        	case ACTIVITY_CONFIG:
        		break;
        }
    }
}