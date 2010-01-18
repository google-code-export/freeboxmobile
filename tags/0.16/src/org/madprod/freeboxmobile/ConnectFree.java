package org.madprod.freeboxmobile;

import android.app.Activity;
import android.os.AsyncTask;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class ConnectFree extends AsyncTask<Void, Void, Integer> implements Constants
{
	private static Activity curActivity;

	@Override
	protected Integer doInBackground(Void... params)
	{
		return FBMHttpConnection.connectFree();
	}

	@Override
	protected void onPreExecute()
	{
		FBMHttpConnection.showProgressDialog(curActivity);
	}

	@Override
	protected void onPostExecute(Integer result)
	{
		FBMHttpConnection.dismissPd();
		if (result == CONNECT_LOGIN_FAILED)
		{
			FBMHttpConnection.showError(curActivity);
		}
	}
	
	public static void setActivity(Activity a)
	{
		curActivity = a;
	}
}