package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.pvr.ProgrammationActivity;
import org.madprod.freeboxmobile.pvr.PvrNetwork;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class ManageCompte extends AsyncTask<ComptePayload, Void, ComptePayload> implements Constants
{
	public static Activity activity;

	@Override
	protected ComptePayload doInBackground(ComptePayload... payload)
	{
		payload[0].result = FBMHttpConnection.connectFreeCheck(payload[0].login, payload[0].password);
		if ((payload[0].result != null) && (payload[0].refresh))
		{
			new PvrNetwork(activity, true, true).getData();
	    	ProgrammationActivity.lastUser = "";
		}
		return payload[0];
	}

	@Override
	protected void onPreExecute()
	{
		FBMHttpConnection.showProgressDialog(activity);
	}

	@Override
	protected void onPostExecute(ComptePayload payload)
	{
		FBMHttpConnection.dismissPd();
		if (payload.result != null)
		{
				payload.exit = Activity.RESULT_OK;
				saveState(payload);

				if (!payload.refresh)
				{
					activity.finish();
				}
		}
		else
		{
				FBMHttpConnection.showError(activity);
				payload.exit = Activity.RESULT_CANCELED;
		}
	}

	public ManageCompte(Activity a)
	{
		activity = a;
	}

    private void saveState(ComptePayload p)
    {
        Bundle bundle = new Bundle();
 
        ComptesDbAdapter mDbHelper = new ComptesDbAdapter(activity);
    	mDbHelper.open();
    	ContentValues v = p.result;
 
    	if (p.rowid == null)
    	{
    		p.rowid = mDbHelper.getIdFromLogin(p.login);
    	}
    	if (p.exit == Activity.RESULT_OK)
    	{
	        if (p.rowid == null)
	        {
	            long id = mDbHelper.createCompte(p.title, p.login, p.password, (String) v.get(KEY_NRA),
	            		(String) v.get(KEY_DSLAM), (String) v.get(KEY_IP), (String) v.get(KEY_LINELENGTH),
	            		(String) v.get(KEY_ATTN), (String) v.get(KEY_TEL), (String) v.get(KEY_LINETYPE),
	            		(String) v.get(KEY_FBMVERSION));
	            if (id > 0)
	            {
	                p.rowid = id;
	            }
	        }
	        else
	        {
	            mDbHelper.updateCompte(p.rowid, p.title, p.login, p.password, (String) v.get(KEY_NRA),
	            		(String) v.get(KEY_DSLAM), (String) v.get(KEY_IP), (String) v.get(KEY_LINELENGTH),
	            		(String) v.get(KEY_ATTN), (String) v.get(KEY_TEL), (String) v.get(KEY_LINETYPE),
	            		(String) v.get(KEY_FBMVERSION));
	        }

            bundle.putLong(ComptesDbAdapter.KEY_ROWID, p.rowid);
    	}
    	else
    	{
            bundle.putLong(ComptesDbAdapter.KEY_ROWID, 0);
    	}
    	mDbHelper.close();
    	if (!p.refresh)
    	{
            Intent mIntent = new Intent();
	        mIntent.putExtras(bundle);
	        activity.setResult(p.exit, mIntent);
    	}
    }
}
