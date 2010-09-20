package org.madprod.freeboxmobile;

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

public class OnExternalApp extends BroadcastReceiver implements Constants
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "=====================> ON EXTERNAL APP !"+intent.toString());
	}
}
