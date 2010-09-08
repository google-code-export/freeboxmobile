package org.madprod.freeboxmobile.guide;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.WakefullIntentService;
import org.madprod.freeboxmobile.home.HomeListActivity;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideCheck extends WakefullIntentService implements Constants
{
	static NotificationManager mNotificationManager = null;
	
	public GuideCheck()
	{
		super("GuideCheck");
		Log.i(TAG,"GuideCheck constructor");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.i(TAG,"GuideCheck onHandleIntent ");

		File log = new File(Environment.getExternalStorageDirectory()+DIR_FBM, file_log);
		try
		{
			BufferedWriter out = new BufferedWriter (new FileWriter(log.getAbsolutePath(), true));
			out.write("guide_start : ");
			out.write(new Date().toString());
			out.write("\n");
			out.close();
		}
		catch (IOException e)
		{
			Log.e(TAG, "Exception appending to log file ",e);
		}

		GuideUtils.makeCalDates();
		
		String dateToGet = GuideUtils.calDates.get((GuideUtils.calDates.size() - 1));
		Log.d(TAG, "last date : "+dateToGet);
		new GuideNetwork(this, dateToGet, 24, false, false, true).getData();
		dateToGet = GuideUtils.calDates.get((GuideUtils.calDates.size() - 2));
		new GuideNetwork(this, dateToGet, 24, false, false, true).getData();
		int nbJours = GuideUtils.calDates.size() - 3;
		while (nbJours >= 0)
		{
			dateToGet = GuideUtils.calDates.get((nbJours--));
			new GuideNetwork(this, dateToGet, 24, false, false, false).getData();			
		}

		try
		{
			BufferedWriter out = new BufferedWriter (new FileWriter(log.getAbsolutePath(), true));
			out.write("date : "+dateToGet+"\n");
			out.write("guide_end : ");
			out.write(new Date().toString());
			out.write("\n");
			out.close();
		}
		catch (IOException e)
		{
			Log.e(TAG, "Exception appending to log file ",e);
		}

		super.onHandleIntent(intent);
	}

	/**
	 * set periodical guide download timer
	 * @param c : context
	 */
	public static void setTimer(Context c)
	{
		AlarmManager amgr = (AlarmManager) c.getSystemService(HomeListActivity.ALARM_SERVICE);
		Intent i = new Intent(c, OnGuideAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(c, 0, i, 0);
		amgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, /*System.currentTimeMillis(),*/SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_DAY, pi);
		Log.i(TAG, "GuideTimer set");
	}
}
