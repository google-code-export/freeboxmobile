package org.madprod.freeboxmobile.mvv;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.WakefullIntentService;
import org.madprod.freeboxmobile.mvv.MevoSync;

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
