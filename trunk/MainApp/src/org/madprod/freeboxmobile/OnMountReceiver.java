package org.madprod.freeboxmobile;



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
* $Id: OnBootReceiver.java 46 2010-09-30 11:02:16Z clement $
* 
*/

public class OnMountReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i("FBM","Application montee");
	}
}
