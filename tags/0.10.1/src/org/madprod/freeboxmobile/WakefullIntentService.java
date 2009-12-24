package org.madprod.freeboxmobile;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class WakefullIntentService extends IntentService implements Constants
{
	public static final String LOCK_NAME_STATIC = "org.madprod.freeboxmobile.AppService.static";
	public static final String LOCK_NAME_LOCAL = "org.madprod.freeboxmobile.AppService.local";
	private static PowerManager.WakeLock lockStatic = null;
	private PowerManager.WakeLock lockLocal = null;

	public static void acquireStaticLock(Context context)
	{
		Log.i(DEBUGTAG,"acquireStaticLock ");
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
		getLock(this).release();
	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		lockLocal.release();
	}
}
