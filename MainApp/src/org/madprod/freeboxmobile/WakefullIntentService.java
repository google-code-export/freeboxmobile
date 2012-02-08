package org.madprod.freeboxmobile;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import static org.madprod.freeboxmobile.StaticConstants.TAG;

/**
*
* @author Olivier Rosello
* $Id: WakefullIntentService.java 874 2011-04-06 10:15:56Z olivier@rosello.eu $
* 
*/

public class WakefullIntentService extends IntentService
{
	public static final String LOCK_NAME_STATIC = "org.madprod.freeboxmobile.mevo.AppService.static";
	public static final String LOCK_NAME_LOCAL = "org.madprod.freeboxmobile.mevo.AppService.local";
	private static PowerManager.WakeLock lockStatic = null;
	private PowerManager.WakeLock lockLocal = null;

	public static void acquireStaticLock(Context context)
	{
		Log.i(TAG,"acquireStaticLock");
		getLock(context).acquire();
	}

	synchronized private static PowerManager.WakeLock getLock(Context context)
	{
		if (lockStatic == null)
		{
			PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
			
			lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}
		return (lockStatic);
	}

	public WakefullIntentService(String name)
	{
		super(name);
	}

	public void onCreate()
	{
		super.onCreate();
		PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);
		lockLocal = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_LOCAL);
		lockLocal.setReferenceCounted(true);
	}

	@Override
	public void onStart(Intent intent, final int startId)
	{
		lockLocal.acquire();
		super.onStart(intent, startId);
		WakeLock mWakeLock = getLock(this);
		if (mWakeLock.isHeld())
			mWakeLock.release();
		if (getLock(this).isHeld())
		{
			Log.i(TAG,"releaseStaticLock");
			getLock(this).release();
		}
	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		lockLocal.release();
	}
}
