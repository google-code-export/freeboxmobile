package org.madprod.mevo.services;


import org.madprod.mevo.tools.Constants;
import org.madprod.mevo.tools.WakefullIntentService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id: OnMevoAlarmReceiver.java 65 2010-11-25 10:18:23Z clement $
* 
*/

public class OnMevoAlarmReceiver extends BroadcastReceiver implements Constants
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG,"onReceive Mevo Alarm");
		WakefullIntentService.acquireStaticLock(context);
		context.startService(new Intent(context, MevoSync.class));
	}
}
