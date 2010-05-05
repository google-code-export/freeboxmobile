package org.madprod.freeboxmobile;

import java.nio.channels.FileChannel;
import java.io.*;

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

public class Utils implements Constants
{
	private static String FBMVersion = null;
	
	public static void copyFile(File in, File out) throws IOException 
    {
	    FileChannel inChannel = new FileInputStream(in).getChannel();
	    FileChannel outChannel = new FileOutputStream(out).getChannel();
	    try
	    {
	    	inChannel.transferTo(0, inChannel.size(), outChannel);
	    } 
	    catch (IOException e)
	    {
	        throw e;
	    }
	    finally
	    {
	    	if (inChannel != null) inChannel.close();
	    	if (outChannel != null) outChannel.close();
	    }
	}

	public static String getFBMVersion(Context c)
	{
		if ((FBMVersion == null) && (c != null))
		{
		    PackageInfo pInfo = null; 
		    try
		    {
		    	pInfo = c.getPackageManager().getPackageInfo ("org.madprod.freeboxmobile",PackageManager.GET_META_DATA);
		    	FBMVersion = ""+pInfo.versionName;
		    }
		    catch (NameNotFoundException e)
		    {
	            pInfo = null;
	            Log.d(TAG, "getFBMVersion ERROR");
		    }
		}
		return FBMVersion;
	}
}
