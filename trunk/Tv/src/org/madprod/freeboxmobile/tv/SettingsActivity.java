package org.madprod.freeboxmobile.tv;

import org.madprod.freeboxmobile.tv.TvConstants;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.text.Layout;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
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
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Tv/SettingsTv");
		ListPreference lp = (ListPreference)findPreference(getString(R.string.key_fav1));
		CharSequence[] streamNames = new CharSequence[Chaine.STREAM_NAME.length];
		CharSequence[] streamTypes = new CharSequence[Chaine.STREAM_NAME.length];
	    Integer i = 0;
	    Log.d(TAG, "STREAM length : "+Chaine.STREAM_NAME.length);
	    while (i<Chaine.STREAM_NAME.length)
	    {
	    	streamNames[i] = Chaine.STREAM_NAME[i];
	    	streamTypes[i] = Chaine.STREAM_TYPE[i].toString();
	    	i++;
	    }
	    lp.setEntries(streamNames);
	    lp.setEntryValues(streamTypes);
	    lp.setDefaultValue(Chaine.STREAM_TYPE_MULTIPOSTE_SD);
   }

    @Override
    protected void onDestroy()
    {
    	tracker.stop();
    	super.onDestroy();
    }

	private void update(String key)
	{
		Object value = getPreferenceManager().getSharedPreferences().getAll().get(key);
		Log.d(TAG, "Value changed : "+value);
		if (value != null && value instanceof String)
			value = notEmpty(getPreferenceManager().getSharedPreferences().getString(key, null));

		if (key.equals("mevo_freq"))
		{
			if (value != null)
			{
				int time = Integer.parseInt((String)value);
//				MevoSync.changeTimer(time, this);
				Log.d(TAG, "Timer changed to : "+time);
			}
		}else if (key.equals(getResources().getString(R.string.key_icon))){
			if (value != null){
				boolean iconPresent = (Boolean)value;
				PackageManager pm = getApplicationContext().getPackageManager();
				if (iconPresent){
//					pm.setComponentEnabledSetting(new ComponentName(this, LaunchActivity.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}else{
//					pm.setComponentEnabledSetting(new ComponentName(this, LaunchActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
				
				final AlertDialog ad = new AlertDialog.Builder(this).create();
//				final View popup = LayoutInflater.from(this).inflate(R.layout.popuperreur,null);
				ad.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

//				ad.setView(popup);

//				TextView errorMessage = (TextView)popup.findViewById(R.id.errorMessage);
//				errorMessage.setText("La mise à jour ne sera peut être visible qu'après le redémarrage de votre téléphone");
//				ImageButton close = (ImageButton)popup.findViewById(R.id.close);
/*				close.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						ad.dismiss();
					}
				});
				try{
					ad.show();
				}catch (Exception e){}
				
*/				
			}
		}
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		update(key);
		
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
