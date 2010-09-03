package org.madprod.freeboxmobile;

import org.madprod.freeboxmobile.ligne.OnInfoAdslAlarmReceiver;
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

public class OnBootReceiver extends BroadcastReceiver implements Constants
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String ms = context.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_MEVO_PREFS_FREQ, "0");
		Log.d(TAG, "On Boot Receiver mevo ! "+ms);
		if (!ms.equals("0"))
		{
			AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, OnMevoAlarmReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
			mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), Integer.parseInt(ms), pi);
		}
		// InfoADSL ne fonctionne plus vraiment (frimousse.org) du coup pour l'instant ceci est désactivé
//		ms = context.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_INFOADSL_PREFS_FREQ, "0");
//		Log.d(DEBUGTAG, "On Boot Receiver infoadsl ! "+ms);
//		if (!ms.equals("0"))
//		{
//			AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//			Intent i = new Intent(context, OnInfoAdslAlarmReceiver.class);
//			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
//			mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), Integer.parseInt(ms), pi);
//		}
	}
}
