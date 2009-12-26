package org.madprod.freeboxmobile;

import org.madprod.freeboxmobile.mvv.MevoConstants;
import org.madprod.freeboxmobile.mvv.OnMevoAlarmReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class OnBootReceiver extends BroadcastReceiver implements MevoConstants
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(DEBUGTAG,"onReceive Boot");
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, OnMevoAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), PERIOD, pi);
	}
}
