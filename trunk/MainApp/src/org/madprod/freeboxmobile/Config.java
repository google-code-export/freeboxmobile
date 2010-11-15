package org.madprod.freeboxmobile;

import org.madprod.freeboxmobile.ligne.InfoAdslCheck; 
import org.madprod.freeboxmobile.mvv.MevoSync;
import org.madprod.freeboxmobile.remotecontrol.FindCodesActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

/**
 *
 * @author Olivier Rosello
 * $Id$
 * 
 */

public class Config extends PreferenceActivity implements OnSharedPreferenceChangeListener, Constants
{
	private static AlertDialog myAlertDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.config_prefs);

		getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		FBMHttpConnection.initVars(this, null);
		// Pas beau sur les prefs suite bug : http://code.google.com/p/android/issues/detail?id=922
		// TODO : try workaround évoqué dans le rapport de bug
		//setTheme(android.R.style.Theme_Light);


		if (myAlertDialog != null)
		{
			myAlertDialog.show();
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference != null){
			String key = preference.getKey();
			if (key != null){
				if (key.compareTo("findCodes") == 0){
					startActivity(new Intent(this, FindCodesActivity.class));
				}
			}
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}


	@Override
	protected void onDestroy()
	{
		Log.i(TAG,"PREFS onDestroy");
		if (myAlertDialog != null)
		{
			myAlertDialog.dismiss();
		}
		super.onDestroy();
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


	private void updateLibelle(String key)
	{
		String summary = null;
		Object value = getSharedPreferences().getAll().get(key);
		if (value != null && value instanceof String)
			value = notEmpty(getSharedPreferences().getString(key, null));

		if (KEY_USER.equals(key))
		{
			summary = "Actuellement : "+(value==null?"Non renseigné":value);
		}
		else if (KEY_PASSWORD.equals(key))
		{
			summary = "Acuellement : "+(value==null?"Non renseigné":"Renseigné");
		}
		else if (BOITIER1_CODE.equals(key))
		{
			summary = "Acuellement : "+(value==null?"Non renseigné":"Renseigné");
		}
		else if (BOITIER1_STATE.equals(key))
		{
			summary = "Acuellement : "+(value==null?"Non renseigné":"Renseigné");
		}
		else if (BOITIER2_CODE.equals(key))
		{
			summary = "Acuellement : "+(value==null?"Non renseigné":"Renseigné");
		}
		else if (BOITIER2_STATE.equals(key))
		{
			summary = "Acuellement : "+(value==null?"Non renseigné":"Renseigné");
		}
		else if (KEY_MEVO_PREFS_FREQ.equals(key))
		{
			if (value != null)
			{
				MevoSync.changeTimer(Integer.parseInt((String)value), this);
			}
		}
		else if (KEY_INFOADSL_PREFS_FREQ.equals(key))
		{
			if (value != null)
			{
				InfoAdslCheck.changeTimer(Integer.parseInt((String)value), this);
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



}
