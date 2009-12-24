package org.madprod.freeboxmobile;

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

public class OnAlarmReceiver extends BroadcastReceiver implements Constants
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(DEBUGTAG,"onReceive Alarm");
		WakefullIntentService.acquireStaticLock(context);
		context.startService(new Intent(context, HttpConnection.class));
	}
}
