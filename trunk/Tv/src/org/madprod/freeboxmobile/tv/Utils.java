package org.madprod.freeboxmobile.tv;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class Utils implements TvConstants
{
	private static String MyVersion = null;
	private static int MyCode = 0;

	private static void initVersions(Context c)
	{
		if ((MyVersion == null) && (c != null))
		{
		    PackageInfo pInfo = null; 
		    try
		    {
		    	pInfo = c.getPackageManager().getPackageInfo ("org.madprod.freeboxmobile.tv", PackageManager.GET_META_DATA);
		    	MyVersion = ""+pInfo.versionName;
		    	MyCode = pInfo.versionCode;
		    }
		    catch (NameNotFoundException e)
		    {
	            pInfo = null;
	            Log.d(TAG, "getFBMVersion ERROR");
		    }
		}
	}

	public static String getMyVersion(Context c)
	{
		if (MyVersion == null)
		{
			initVersions(c);
		}
		return MyVersion;
	}
	
	public static int getMyCode(Context c)
	{
		if (MyCode == 0)
		{
			initVersions(c);
		}
		return MyCode;
	}
}
