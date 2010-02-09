package org.madprod.freeboxmobile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* Cette classe sert à logguer les infos de debug et les exceptions
* Ce log peut être envoyé par l'utilisateur sur la mailing liste de dev
* (option "Log erreur" de la home)
* 
*/

public class FMLog
{
	private static final String FBMTAG = "FreeboxMobile";
	private static String fbmlog = "";

	public static void d(String s)
	{
		Log.d(FBMTAG, s);
		fbmlog += "d: "+s+"\n";
	}

	public static void i(String s)
	{
		Log.i(FBMTAG, s);
		fbmlog += "i: "+s+"\n";
	}

	public static void e(String s, Throwable throwable)
	{
		Log.e(FBMTAG, s);
		Log.e(FBMTAG, throwable.getMessage());
		throwable.printStackTrace();
		fbmlog += "e: "+s+"\n";
		fbmlog += "e: "+throwable.getMessage()+"\n";
		fbmlog += "e: "+getStackTrace(throwable)+"\n";
	}

	private static String getStackTrace(Throwable throwable)
	{
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		throwable.printStackTrace(printWriter);
		return writer.toString();
	}
	
	public static String getLog()
	{
		return fbmlog;
	}
}
