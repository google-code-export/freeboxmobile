package org.madprod.freeboxmobile.guide;


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

public class OnGuideAlarmReceiver extends BroadcastReceiver implements Constants
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG,"onReceive Guide Alarm");
		WakefullIntentService.acquireStaticLock(context);
		context.startService(new Intent(context, GuideCheck.class));
	}
}
