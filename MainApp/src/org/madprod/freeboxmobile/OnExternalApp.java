package org.madprod.freeboxmobile;

import org.madprod.freeboxmobile.guide.GuideCheck; 

import static org.madprod.freeboxmobile.StaticConstants.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 *
 * @author Olivier Rosello
 * $Id$
 * 
 */

public class OnExternalApp extends BroadcastReceiver
{
	String[] packages = new String[]{"org.madprod.infofreenautes", "org.madprod.mevo"};

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "=====================> ON EXTERNAL APP !"+intent.toString());		
		Intent i = new Intent("org.madprod.freeboxmobile.action.MOUNTMODULE");
		context.sendBroadcast(i);


		GuideCheck.setTimer(context);
	}
}
