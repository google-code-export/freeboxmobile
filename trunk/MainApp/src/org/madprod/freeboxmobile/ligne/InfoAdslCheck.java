package org.madprod.freeboxmobile.ligne;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.WakefullIntentService;
import org.madprod.freeboxmobile.home.HomeListActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class InfoAdslCheck extends WakefullIntentService implements Constants
{
	static NotificationManager mNotificationManager = null;
	
	public InfoAdslCheck()
	{
		super("InfoAdslCheck");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{

		Log.i(TAG,"InfoAdslCheck onHandleIntent ");

		File log = new File(Environment.getExternalStorageDirectory()+DIR_FBM, file_log);
		try
		{
			BufferedWriter out = new BufferedWriter (new FileWriter(log.getAbsolutePath(), true));
			out.write("infoadsl_start: ");
			out.write(new Date().toString());
			out.write("\n");
			out.close();
		}
		catch (IOException e)
		{
			Log.e(TAG, "Exception appending to log file ",e);
		}

		/*
		try
		{
			SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
			String DSLAM = mgr.getString(KEY_DSLAM, "");
			String LastDSLAMCheck = mgr.getString(KEY_LAST_DSLAM_CHECK, "");
			if (!DSLAM.equals(""))
			{
				URI uri = URI.create(FBMHttpConnection.frimousseUrl);
				XMLRPCClient client = new XMLRPCClient(uri);
				boolean DSLAM_ok = (Boolean) client.call("getDSLAMStatus", DSLAM);
				if (!DSLAM_ok)
				{
					if (!LastDSLAMCheck.equals("0"))
					{
						_initNotif();
						Editor editor = mgr.edit();
						editor.putString(KEY_LAST_DSLAM_CHECK, "0");
						editor.commit();
					}
				}
				else
				{
					if (!LastDSLAMCheck.equals("1"))
					{
						Editor editor = mgr.edit();
						editor.putString(KEY_LAST_DSLAM_CHECK, "1");
						editor.commit();
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "InfoAdslCheck : " + e.getMessage());
			e.printStackTrace();
		}
*/
		try
		{
			BufferedWriter out = new BufferedWriter (new FileWriter(log.getAbsolutePath(), true));
			out.write("infoadsl_end: ");
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
	
	private void _initNotif()
	{
		int icon = R.drawable.icon_fbm;
		CharSequence tickerText;
		CharSequence contentText;

		if (mNotificationManager == null)
		{
			mNotificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
		
		tickerText = getString(R.string.app_name)+" : Problème réseau ADSL";
		contentText = "Votre DSLAM ne répond pas.";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = getString(R.string.app_name);

		Intent notificationIntent = new Intent(this, LigneInfoActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.defaults |= Notification.DEFAULT_SOUND;
		mNotificationManager.notify(NOTIF_INFOADSL, notification);
	}
	
	/**
	 * Change timer to ms value (or cancel if ms == 0)
	 * @param ms : timer value in ms
	 * @param a : activity
	 */
	public static void changeTimer(int ms, Activity a)
	{
		AlarmManager amgr = (AlarmManager) a.getSystemService(HomeListActivity.ALARM_SERVICE);
		Intent i = new Intent(a, OnInfoAdslAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(a, 0, i, 0);
		if (ms != 0)
		{
			amgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), ms, pi);
			Log.i(TAG, "InfoAdslTimer changed to "+ms);
		}
		else
		{
			amgr.cancel(pi);
			Log.i(TAG, "InfoAdslTimer canceled");			
		}
	}
}
