package org.madprod.mevo.services;


import org.madprod.mevo.LaunchActivity;
import org.madprod.mevo.R;
import org.madprod.mevo.tools.Constants;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id: OnUpgradeReceiver.java 584 2010-09-07 16:03:18Z olivier.rosello34 $
* 
*/

public class OnUpgradeReceiver extends BroadcastReceiver implements Constants
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "On Upgrade Receiver (Mevo) ! ");

		
		
		int time = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_refresh), context.getString(R.integer.default_refresh)));
		Log.d(TAG, "OnUpgradeReceiver (Mevo) : time set to "+time+" minutes ");
		MevoSync.changeTimer(time, context);

		Boolean icon = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.key_icon), Boolean.parseBoolean(context.getString(R.bool.default_icon)));
		PackageManager pm = context.getPackageManager();
		if (icon){
			pm.setComponentEnabledSetting(new ComponentName(context, LaunchActivity.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			
		}else{
			pm.setComponentEnabledSetting(new ComponentName(context, LaunchActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
			
		}

		

	}

}
