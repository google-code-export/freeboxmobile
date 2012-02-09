package org.madprod.freeboxmobile.tv;

import org.madprod.freeboxmobile.tv.TvConstants;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class SettingsActivity extends PreferenceActivity implements TvConstants, OnSharedPreferenceChangeListener
{
	private GoogleAnalyticsTracker tracker;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.tv_settings);
        SharedPreferences pref = getPreferenceManager().getSharedPreferences();
        pref.registerOnSharedPreferenceChangeListener(this);
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Tv/SettingsTv");
//		clearConfig();
		setupLists(null);
    }

    @Override
    protected void onDestroy()
    {
    	tracker.stopSession();
    	super.onDestroy();
    }

	private void clearConfig()
    {
		Editor e = getPreferenceManager().getSharedPreferences().edit();
		e.clear();
		e.commit();    	
    }
    
	private void setupLists(String key)
    {
		// Si on ajoute des listes ici, il faut ajouter dans lists, dans defaultValues et dans tv_settings.xml
		
		String[] selected = new String[listStreamsKeys.length];
		Integer i = 0;
		Integer j;

		// cont passe à false dès qu'on a un choix de la liste grisé. Ainsi, tous les choix suivants seront grisés
		boolean cont = true;
		while (i < listStreamsKeys.length)
		{
			Object value = getPreferenceManager().getSharedPreferences().getAll().get(listStreamsKeys[i]);
			value = notEmpty(getPreferenceManager().getSharedPreferences().getString(listStreamsKeys[i], null));
			
			selected[i] = (value != null ? value.toString() : null);
			ListPreference lp = (ListPreference)findPreference(listStreamsKeys[i]);

			if (((i != 0) && (selected[i-1] == null)) || (cont == false))
			{
				lp.setEnabled(false);
				lp.setSummary("");
				cont = false;
			}
			else
			{
				CharSequence[] streamNames = new CharSequence[Chaine.STREAM_NAME.length - i];
				CharSequence[] streamTypes = new CharSequence[Chaine.STREAM_NAME.length - i];
				// on check si on a paas un doublon de choix
				// (si on revient sur Choix1 pour choisir pareil que Choix2...)
				j = 0;
				while (j < i)
				{
					if ((selected[i] != null) && (selected[j].equals(selected[i])))
					{
						Editor editor = getPreferenceManager().getSharedPreferences().edit();
						editor.putString(listStreamsKeys[i], null);
						editor.commit();
						selected[i] = null;
					}
					j++;
				}
			    j = 0;
			    // Indice dans la liste qui s'affiche à l'écran
			    Integer selectListNumber = 0;
			    while (j<Chaine.STREAM_NAME.length)
			    {
			    	// Pour supprimer les flux sélectionnés dans les choix précédants
			    	Integer k = 0;
			    	Boolean found = false;
			    	while (k < i)
			    	{
			    		if ((selected[k] != null) && (selected[k].equals(Chaine.STREAM_TYPE[j].toString())))
			    		{
			    			found = true;
			    			break;
			    		}
			    		k++;
			    	}
			    	if (!found)
			    	{
			    		Log.d(TAG, "i:"+i+" selectListNumber:"+selectListNumber+" j:"+j);
			    		if (streamNames.length > selectListNumber)
			    		{
					    	streamNames[selectListNumber] = Chaine.STREAM_NAME[j];
					    	streamTypes[selectListNumber] = Chaine.STREAM_TYPE[j].toString();
			    		}
				    	selectListNumber++;
			    	}
			    	j++;
			    }
			    // Si on est sur le dernier choix (un seul choix possible)
			    // et si on a modifié l'avant dernier choix (lists[lists.length - 2])
			    // on le selectionne le seul choix possible pour le dernier
			    if ((i != 0) && (selectListNumber == 1) && (key != null) && (key.equals(listStreamsKeys[listStreamsKeys.length - 2])))
			    {
					Editor editor = getPreferenceManager().getSharedPreferences().edit();
					editor.putString(listStreamsKeys[i], streamTypes[0].toString());
					editor.commit();
					selected[i] = streamTypes[0].toString();
			    }
			    lp.setEntries(streamNames);
			    lp.setEntryValues(streamTypes);
			    lp.setDefaultValue(defaultValues[i]);
			    lp.setEnabled(true);
			    if (i == 0)
			    {
			    	lp.setSummary("Flux utilisé en priorité : "+ (selected[i] != null ? Chaine.getStreamName(Integer.parseInt(selected[i])) : "non séléctionné"));
			    }
			    else
			    {
			    	lp.setSummary("Flux utilisé si le "+ (selected != null ? Chaine.getStreamName(Integer.parseInt(selected[i-1])) : " flux du Choix "+i)+" n'est pas disponible : "+ (selected[i] != null ? Chaine.getStreamName(Integer.parseInt(selected[i])) : "non séléctionné"));
			    }
			}
			i++;
		}
    }

	private void update(String key)
	{
		Object value = getPreferenceManager().getSharedPreferences().getAll().get(key);
		Log.d(TAG, "Value changed : "+value);
		if (value != null && value instanceof String)
			value = notEmpty(getPreferenceManager().getSharedPreferences().getString(key, null));
		if (value == null)
		{
			return;
		}

		if (key.equals(getResources().getString(R.string.key_icon)))
		{
			if (value != null)
			{
				boolean iconPresent = (Boolean)value;
				PackageManager pm = getApplicationContext().getPackageManager();
				if (iconPresent)
				{
					pm.setComponentEnabledSetting(new ComponentName(this, LaunchActivity.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}
				else
				{
					pm.setComponentEnabledSetting(new ComponentName(this, LaunchActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
				
				final AlertDialog ad = new AlertDialog.Builder(this).create();
				final View popup = LayoutInflater.from(this).inflate(R.layout.popuperreur,null);
				ad.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

				ad.setView(popup);

				TextView errorMessage = (TextView)popup.findViewById(R.id.errorMessage);
				errorMessage.setText("La mise à jour ne sera peut être visible qu'après le redémarrage de votre téléphone");
				ImageButton close = (ImageButton)popup.findViewById(R.id.close);
				close.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						ad.dismiss();
					}
				});
				try
				{
					ad.show();
				}
				catch (Exception e)
				{}
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		update(key);
		setupLists(key);
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
