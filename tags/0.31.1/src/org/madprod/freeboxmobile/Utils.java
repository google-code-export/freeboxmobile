package org.madprod.freeboxmobile;

import java.nio.channels.FileChannel;
import java.io.*;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class Utils
{
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
}
