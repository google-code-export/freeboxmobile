package org.madprod.freeboxmobile;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class Config extends PreferenceActivity implements OnSharedPreferenceChangeListener, Constants
{
    private boolean inflateOK = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.prefs);
        Log.d(DEBUGTAG,Environment.getExternalStorageDirectory().toString()+"/freeboxmobile");

        File file = new File(Environment.getExternalStorageDirectory().toString()+DIR_MEVO);
        file.mkdirs();

        // Initialiser d'apres les prefs actuelles
        updateLibelle(KEY_USER);
        updateLibelle(KEY_PASSWORD);
        // Pas beau sur les prefs suite bug : http://code.google.com/p/android/issues/detail?id=922
        // TODO : try workaround évoqué dans le rapport de bug
        //setTheme(android.R.style.Theme_Light);
    }

    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	Log.d(DEBUGTAG,"PREFS onDestroy");
    	HttpConnection.checkUpdated();
    }

    private SharedPreferences getSharedPreferences()
    {
        return super.getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode)
    {
		return getSharedPreferences();
	}

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
    	updateLibelle(key);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	if (!inflateOK)
    	{
    		MenuInflater inflater = getMenuInflater();
    		inflater.inflate(R.menu.menu, menu);
    		inflateOK = true;
    	}
    	return true;
    }

    private void updateLibelle(String key)
    {
    	String summary = null;
    	String value = notEmpty(getSharedPreferences().getString(key, null));
    	if (KEY_USER.equals(key))
    	{
    		summary = "Actuellement : "+(value==null?"Non renseigné":value);
        }
    	else
    	{
			if (KEY_PASSWORD.equals(key))
			{
				summary = "Acuellement : "+(value==null?"Non renseigné":"Renseigné");
			}
    	}
    	if (summary!=null)
    	{
    		PreferenceScreen ps = getPreferenceScreen();
    		if (ps!=null)
    		{
    			Preference p = ps.findPreference(key);
    			if (p!=null)
    			{
    				p.setSummary(summary);
    			} 
    		}
    	}
    }

    private String notEmpty(String s)
    {
    	if (s!=null && s.trim().length()==0)
    	{
    		return null;
        }
        return s;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
		if (item.getItemId() == R.id.aide)
		{
			AlertDialog d = new AlertDialog.Builder(this).create();
			d.setTitle(null);
			d.setMessage(
				"Pour avoir accès à votre messagerie vocale Freebox "+
				"veuillez saisir votre identifiant Free "+
				"ainsi que votre mot de passe."
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
			return true;
		}
    	return super.onMenuItemSelected(featureId, item);
	}
}
