package org.madprod.mevo.services;



import org.madprod.freeboxmobile.services.IMevo;   
import org.madprod.mevo.R;
import org.madprod.mevo.tools.Constants;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
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


	//	/**
	//	 * Change timer to ms value (or cancel if ms == 0)
	//	 * @param ms : timer value in ms
	//	 * @param a : activity
	//	 */
	public static void changeTimer(int minute,  Context c)
	{

		AlarmManager amgr = (AlarmManager) c.getSystemService(ALARM_SERVICE);
		Intent i = new Intent(c, OnMevoAlarmReceiver.class);
		int ms = minute * 60000;
		PendingIntent pi = PendingIntent.getBroadcast(c, 0, i, 0);
		if (ms != 0)
		{
			long last = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(c).getString(c.getString(R.string.last_refresh), c.getString(R.string.default_last_refresh)));
			long periodInMilliSeconds = (System.currentTimeMillis()-last);
			long periodInSeconds = periodInMilliSeconds/1000;
			int periodMinutes = (int)(periodInSeconds/60);
			long periodSeconds = periodInSeconds - (periodMinutes*60);
			Log.d(TAG, "MevoTimer last success sync : "+periodInSeconds+" seconds (~ "+periodMinutes+" minutes "+periodSeconds+" seconds)");
			Log.d(TAG, "MevoTimer last : "+last +" period : "+periodInSeconds + " ms : "+ms);

			//			if (periodInMilliSeconds > ms){
			//				Log.d(TAG, "MevoTimer set repeating now ");
			//				amgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ms, pi);
			//			}
			//			else{
			//				Log.d(TAG, "MevoTimer set repeating in "+(last+ms)+" ms");
			amgr.setRepeating(AlarmManager.RTC_WAKEUP, last+ms, ms, pi);
			//			}		
			Log.i(TAG,"MevoTimer  changed to "+ms);
		}
		else
		{
			amgr.cancel(pi);
			Log.i(TAG,"MevoTimer canceled");			
		}
	}

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

		ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

		if (cm == null || cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected()){
			Log.d(TAG, "airplane mode");
			if (receiver != null) receiver.send(STATUS_ERROR, Bundle.EMPTY);
			
			
			return;
		}



		try{
			Intent serviceIntent = new Intent("org.madprod.freeboxmobile.services.MevoService");
			if (!(binded = bindService(serviceIntent , mMevoConnection, Context.BIND_AUTO_CREATE))){
				Log.d(TAG, "Serice not binded");
				sendError("Service not binded");
				return;
			}
		}catch(SecurityException e){
			e.printStackTrace();
		}


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
			Log.e(TAG, "Deconnecte");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "Connecte");
			try{
				mMevo = IMevo.Stub.asInterface(service);
				final long startRemote = System.currentTimeMillis();
				Log.d(TAG, "Check message");
				try {
					mMevo.checkMessages();
				} catch (RemoteException e) {
					Log.d(TAG, "Remote exception");
					e.printStackTrace();
				}

				Log.d(TAG, "Write end date");
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MevoSync.this);
				Editor e = prefs.edit();
				e.putString(getString(R.string.last_refresh), ""+System.currentTimeMillis());
				e.commit();
				if (receiver != null) {
					// Pass back error to surface listener
					final Bundle bundle = new Bundle();
					receiver.send(STATUS_FINISHED, bundle);
				}

				Log.d(TAG, "remote sync took " + (System.currentTimeMillis() - startRemote) + "ms");

			} catch (Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				sendError(e.toString());
			}

			Log.d(TAG, "sync finished");

		}

	};

	public void onDestroy() {
		if (mMevoConnection != null && binded){
			unbindService(mMevoConnection);
			mMevoConnection = null;
		}
	};


	public static IMevo mMevo = null;


	private void sendError(String message){
		if (receiver != null) {
			// Pass back error to surface listener
			final Bundle bundle = new Bundle();
			bundle.putString(Intent.EXTRA_TEXT, message);
			receiver.send(STATUS_ERROR, bundle);
		}

	}


}
