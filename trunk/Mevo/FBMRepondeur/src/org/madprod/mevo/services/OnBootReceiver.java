package org.madprod.mevo.services;


import org.madprod.mevo.R; 
import org.madprod.mevo.tools.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id: OnBootReceiver.java 71 2010-12-07 08:41:46Z clement $
* 
*/

public class OnBootReceiver extends BroadcastReceiver implements Constants
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		
		int time = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_refresh), context.getString(R.integer.default_refresh)));
		Log.d(TAG, "OnBootReceiver (Mevo) : time set to "+time+" minutes");
		MevoSync.changeTimer(time, context);
	}
}

