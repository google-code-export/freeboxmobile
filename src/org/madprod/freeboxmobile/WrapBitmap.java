package org.madprod.freeboxmobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class WrapBitmap
{
	private Bitmap mInstance = null;
	
	static
	{
		try
		{
			Class.forName("android.graphics.Bitmap");
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/* calling here forces class initialization */
	public static void checkAvailable() {}

	public WrapBitmap(String filepath) 
	{
		mInstance = BitmapFactory.decodeFile(filepath);
	}
	
	public void setDensity(int density)
	{
		if (mInstance != null)
		{
			mInstance.setDensity(density);
		}
	}
	
	public Bitmap getBitmap()
	{
		return mInstance;
	}
}
