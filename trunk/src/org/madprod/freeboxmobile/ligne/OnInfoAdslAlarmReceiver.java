package org.madprod.freeboxmobile.ligne;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.WakefullIntentService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class OnInfoAdslAlarmReceiver extends BroadcastReceiver implements Constants
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG,"onReceive InfoAdsl Alarm");
		WakefullIntentService.acquireStaticLock(context);
		context.startService(new Intent(context, InfoAdslCheck.class));
	}
}
