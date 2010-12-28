package org.madprod.mevo.services;


import org.madprod.mevo.HomeActivity; 
import org.madprod.mevo.R;
import org.madprod.mevo.tools.Constants;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 *
 * @author Olivier Rosello
 * $Id: OnBootReceiver.java 71 2010-12-07 08:41:46Z clement $
 * 
 */

public class OnMessageReceiver extends BroadcastReceiver implements Constants
{

	static NotificationManager mNotificationManager = null;
	private Context context;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		final int nbNews = intent.getIntExtra("NEW", 0);
		this.context = context;
		//getTruc

		if (nbNews > 0)
		{
			_initNotif(nbNews);
		}

		Log.d(TAG, "sync finished");
		
		if (MevoSync.receiver != null) MevoSync.receiver.send(MevoSync.STATUS_FINISHED, Bundle.EMPTY);

	}


	public static void cancelNotif(int id)
	{
		if (mNotificationManager != null)
			mNotificationManager.cancel(id);
	}

	private void _initNotif(int newmsg)
	{
		int icon = R.drawable.fm_repondeur;
		CharSequence tickerText;
		CharSequence contentText;

		if (mNotificationManager == null)
		{
			mNotificationManager= (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
		}

		tickerText = context.getString(R.string.app_name)+" : "+newmsg+" nouveaux messages";
		contentText = newmsg+" nouveaux messages";


		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		
		CharSequence contentTitle = context.getString(R.string.app_name);

		Intent notificationIntent = new Intent(context, HomeActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;

		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.key_notifson), context.getResources().getBoolean(R.bool.default_notifson)))
			notification.defaults |= Notification.DEFAULT_SOUND;


		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.key_notifvisu), context.getResources().getBoolean(R.bool.default_notifvisu)))
			mNotificationManager.notify(NOTIF_MEVO, notification);

	}


}

