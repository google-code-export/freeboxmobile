package org.madprod.mevo.services;



import org.madprod.freeboxmobile.services.IMevo; 
import org.madprod.mevo.HomeActivity;
import org.madprod.mevo.tools.Constants;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;

/**
 *
 * @author Clément Beslon
 * $Id: MevoSync.java 71 2010-12-07 08:41:46Z clement $
 * 
 */

public class MevoSync extends IntentService implements Constants
{



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

	









	private static final String TAG = "MevoSync";

	public static final String EXTRA_STATUS_RECEIVER =
		"org.madprod.mevo.extra.STATUS_RECEIVER";

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;
	public static ResultReceiver receiver = null;
	private boolean binded = false;


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

		receiver = intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		if (receiver != null) receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		try{
			final long startRemote = System.currentTimeMillis();
			
			if (HomeActivity.mMevo == null){
				try{
					Intent serviceIntent = new Intent("org.madprod.freeboxmobile.services.MevoService");
					if (!(binded = bindService(serviceIntent , mMevoConnection, Context.BIND_AUTO_CREATE))){
						return;
					}
				}catch(SecurityException e){
					e.printStackTrace();
				}
			}
			
			
			try {
				HomeActivity.mMevo.checkMessages();
			} catch (RemoteException e) {
				e.printStackTrace();
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
		}

	};

	public void onDestroy() {
		if (mMevoConnection != null && binded){
			unbindService(mMevoConnection);
			mMevoConnection = null;
		}
	};

}
