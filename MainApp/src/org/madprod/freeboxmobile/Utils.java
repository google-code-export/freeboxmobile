package org.madprod.freeboxmobile;

import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.*;

import static org.madprod.freeboxmobile.StaticConstants.TAG;

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

public class Utils
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

	public static int getPlatformVersion()
	{
		try
		{
			Field verField = Class.forName("android.os.Build$VERSION").getField("SDK_INT");
			int ver = verField.getInt(verField);
			return ver;
		}
		catch (Exception e)
		{
			// android.os.Build$VERSION is not there on Cupcake
			return 3;
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

	public static String convertDateTimeHR(String org)
	{
		String ret = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			Date deb = sdf.parse(org);
			Date fin = new Date();//sdf.parse(new Date());
			long diff = fin.getTime() - deb.getTime();
			int nbJours = (int) (diff / 86400000);
			String[] datetime = org.split(" ");
//			if (nbJours == 0)
//				ret = datetime[1];
//			else if (nbJours == 1)
//				ret = "Hier "+datetime[1];
//			else
			String[] date = datetime[0].split("-");
			if (nbJours <366)
			{
				ret = date[2]+"/"+date[1]+" "+datetime[1];
			}
			else
			{
				ret = date[2]+"/"+date[1]+"/"+date[0]+" "+datetime[1];				
			}
		}
		catch (ParseException e)
		{
			Log.e(TAG,"PARSE DATETIME HR "+e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}
	

	
}
