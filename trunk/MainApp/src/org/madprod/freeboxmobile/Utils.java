package org.madprod.freeboxmobile;

import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
	private static int FBMCode = 0;

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

	private static void initVersions(Context c)
	{
		if ((FBMVersion == null) && (c != null))
		{
		    PackageInfo pInfo = null; 
		    try
		    {
		    	pInfo = c.getPackageManager().getPackageInfo ("org.madprod.freeboxmobile", PackageManager.GET_META_DATA);
		    	FBMVersion = ""+pInfo.versionName;
		    	FBMCode = pInfo.versionCode;
		    }
		    catch (NameNotFoundException e)
		    {
	            pInfo = null;
	            Log.d(TAG, "getFBMVersion ERROR");
		    }
		}
	}

	public static String getFBMVersion(Context c)
	{
		if (FBMVersion == null)
		{
			initVersions(c);
		}
		return FBMVersion;
	}
	
	public static int getFBMCode(Context c)
	{
		if (FBMCode == 0)
		{
			initVersions(c);
		}
		return FBMCode;
	}

	public static void unzipFile(String pathZipFile, String path){
		try {
			final int BUFFER = 2048;
			byte data[] = new byte[BUFFER];
			BufferedOutputStream dest = null;
			FileInputStream zipFile = new FileInputStream(pathZipFile);
			BufferedInputStream buffZip = new BufferedInputStream(zipFile);
			ZipInputStream zis = new ZipInputStream(buffZip);
			ZipEntry entree;
			int count;
			while((entree = zis.getNextEntry()) != null) {
				File f = new File(path+"/"+entree.getName());
				if (entree.isDirectory()){
					if (!f.exists()){
						f.mkdir();
					}
				}else{
					FileOutputStream fos = new FileOutputStream(f);
					dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = zis.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
				}
			}
			

			zis.close();

		} catch (IOException e2) {
			e2.printStackTrace();
		}

	}
	
	public static void deleteFile(File path) {
		if (path.exists()){
			if (!path.delete()){
				File[] files = path.listFiles(); 
				for(int i=0; i<files.length; i++) { 
					if(files[i].isDirectory()) { 
						deleteFile(files[i]); 
					} 
					else { 
						files[i].delete(); 
					} 
				} 
			}
		}
	}

	
}
