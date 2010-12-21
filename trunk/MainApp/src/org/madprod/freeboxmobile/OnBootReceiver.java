package org.madprod.freeboxmobile;

import org.madprod.freeboxmobile.guide.GuideCheck; 

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

public class OnBootReceiver extends BroadcastReceiver implements Constants
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "On Boot Receiver ! ");

		String ms = context.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_MEVO_PREFS_FREQ, "-1");
		if (!ms.equals("0")) // Si "0" : l'utilisateur ne veut pas de relève périodique
		{
			if (!ms.equals("-1"))  // Si une valeur était mise
			{
//				MevoSync.changeTimer(Integer.parseInt(ms), context);
			}
			else // Si pas configuré : valeur par défaut
			{
//				MevoSync.changeTimer(DEFAULT_MEVO_FREQ, context);
			}
		}

		GuideCheck.setTimer(context);
	}
}
