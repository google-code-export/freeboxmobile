package org.madprod.mevo.services;



import org.madprod.freeboxmobile.services.IMevo;
import org.madprod.freeboxmobile.services.IRemoteControlServiceCallback;
import org.madprod.mevo.HomeActivity;
import org.madprod.mevo.R;
import org.madprod.mevo.tools.Constants;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 *
 * @author Clément Beslon
 * $Id: MevoSync.java 71 2010-12-07 08:41:46Z clement $
 * 
 */

public class MevoSync extends IntentService implements Constants
{

	static NotificationManager mNotificationManager = null;


	//	/**
	//	 * Change timer to ms value (or cancel if ms == 0)
	//	 * @param ms : timer value in ms
	//	 * @param a : activity
	//	 */
	public static void changeTimer(int minute,  Context c)
	{
		
				AlarmManager amgr = (AlarmManager) c.getSystemService(HomeActivity.ALARM_SERVICE);
				Intent i = new Intent(c, OnMevoAlarmReceiver.class);
				int ms = minute * 60000;
				PendingIntent pi = PendingIntent.getBroadcast(c, 0, i, 0);
				if (ms != 0)
				{
					amgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), ms, pi);
					Log.i(TAG,"MevoTimer  changed to "+ms);
				}
				else
				{
					amgr.cancel(pi);
					Log.i(TAG,"MevoTimer canceled");			
				}
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
			mNotificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}

		tickerText = getString(R.string.app_name)+" : "+newmsg+" nouveaux messages";
		contentText = newmsg+" nouveaux messages";


		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = getString(R.string.app_name);

		Intent notificationIntent = new Intent(this, HomeActivity.class);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;
		
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.key_notifson), context.getResources().getBoolean(R.bool.default_notifson)))
			notification.defaults |= Notification.DEFAULT_SOUND;


		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.key_notifvisu), context.getResources().getBoolean(R.bool.default_notifvisu)))
			mNotificationManager.notify(NOTIF_MEVO, notification);
		
	}









	private static final String TAG = "MevoSync";

	public static final String EXTRA_STATUS_RECEIVER =
		"org.madprod.mevo.extra.STATUS_RECEIVER";

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;




	public MevoSync() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");

		final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		if (receiver != null) receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		try{
			final long startRemote = System.currentTimeMillis();
			int nbNews = 0;
			
			
			if (HomeActivity.mMevo == null){
				try{
					if (!bindService(new Intent("org.madprod.freeboxmobile.services.MevoService"), mMevoConnection, Context.BIND_AUTO_CREATE)){
					}
				}catch(SecurityException e){
					e.printStackTrace();
				}
			}
			
			
			try {
				nbNews = HomeActivity.mMevo.checkMessages();
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			if (nbNews > 0)
			{
				_initNotif(nbNews);
			}

			Log.d(TAG, "remote sync took " + (System.currentTimeMillis() - startRemote) + "ms");

		} catch (Exception e) {
			Log.e(TAG, "Problem while syncing", e);

			if (receiver != null) {
				// Pass back error to surface listener
				final Bundle bundle = new Bundle();
				bundle.putString(Intent.EXTRA_TEXT, e.toString());
				receiver.send(STATUS_ERROR, bundle);
			}
		}

		Log.d(TAG, "sync finished");
		if (receiver != null) receiver.send(STATUS_FINISHED, Bundle.EMPTY);
	}


	public static void resetActivity(){
		
	}
	public static void setActivity(Activity a){
		
	}
	public static void setUpdateListener(Object o){
		
	}
	
	private ServiceConnection mMevoConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e("REMOTE", "Deconnecte");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.e("REMOTE", "Connecte");
			HomeActivity.mMevo = IMevo.Stub.asInterface(service);
			try { 
				HomeActivity.mMevo.registerCallback(callback); 
			}catch (RemoteException e) {
				e.printStackTrace();
			} 
		}

	};
	
	final IRemoteControlServiceCallback callback = new IRemoteControlServiceCallback.Stub() {

		@Override
		public void dataChanged(int status, String message)
		throws RemoteException {
			if (status == 1){
				mHandler.sendMessage(mHandler.obtainMessage(0,"Erreur "+message));
			}

		} 

	}; 



	private Handler mHandler = new Handler() {
		@Override public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				break;
			default:
				super.handleMessage(msg);
			}
		}

	};

}
