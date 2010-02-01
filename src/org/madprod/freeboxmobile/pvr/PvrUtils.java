package org.madprod.freeboxmobile.pvr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.madprod.freeboxmobile.FBMHttpConnection;

import android.util.Log;

class PvrUtils {
	private final static String DEBUGTAG = "FreeboxMobileDebug";
	
	public static String make02d(int a) {
		if (a < 10) {
			return "0"+a;
		}
		else {
			return ""+a;
		}
	}
	/**
	 * BufferedReader to String conversion
	 * @param	BufferedReader
	 * @return	String
	 * @throws	IOException
	 */
	public static String getPage2(InputStream is) {
		FBMHttpConnection.FBMLog("getPage start");
		if (is == null) {
			FBMHttpConnection.FBMLog("getPage is null");
			return null;
		}
		try {
			FBMHttpConnection.FBMLog("getPage try");
			return getPage2(new InputStreamReader(is, "ISO8859_1"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FBMHttpConnection.FBMLog("getPage is end null");
		return null;
	}
	public static String getPage2(InputStreamReader isr) {
		if (isr == null) {
			return null;
		}
		
		BufferedReader reader = new BufferedReader(isr); 
		StringBuilder sb = new StringBuilder();
		
		if (reader == null) {
			return null;
		}

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
		          sb.append(line+"\n");
		    }
		}
		catch (IOException e) {
			Log.e(DEBUGTAG, "getPage: "+e);
			return null;
		}		
		return sb.toString();
	}
}