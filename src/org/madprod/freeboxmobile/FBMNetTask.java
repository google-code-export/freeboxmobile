package org.madprod.freeboxmobile;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

public class FBMNetTask extends AsyncTask<Void, Integer, Integer> implements Constants
{
	static private Activity activity = null;
	static private AlertDialog alertDialog = null;
	static private ProgressDialog iProgressDialog = null;
	
	static private ProgressDialog dProgressDialog = null;
	static private String dProgressText = "";
	static private String dProgressTitle = "";
	static private int dProgressIcon = 0;
	static private int dProgressMax = 0;

	@Override
	protected Integer doInBackground(Void... params)
	{
		return null;
	}
	
	protected void onProgressUpdate(Integer... progress)
    {
		if (progress[0] != -1)
		{
			dProgressUpdate(progress[0]);
		}
		else
		{
			dProgressDialogDismiss();
		}
    }
	
	public static void register(Activity a)
	{
		Log.d(TAG, "FBMNetTask : REGISTER");
		activity = a;
		FBMHttpConnection.initVars(a, null);
		if (iProgressDialog != null)
		{
			iProgressDialog.show();
		}
		if (dProgressDialog != null)
		{
			dProgressDialog.show();
		}
		if (alertDialog != null)
		{
			alertDialog.show();
		}
	}
	
	public static void unregister(Activity a)
	{
		Log.d(TAG, "FBMNetTask : UNREGISTER");
		if (activity == a)
		{
			if (dProgressDialog != null)
				dProgressDialog.dismiss();
			if (iProgressDialog != null)
				iProgressDialog.dismiss();
			if (alertDialog != null)
				alertDialog.dismiss();
			activity = null;
		}
	}
	
	protected Activity getActivity()
	{
		return activity;
	}
	
	protected void dismissAll()
	{
		dProgressDialogDismiss();
		iProgressDialogDismiss();
		alertDialogDismiss();
	}
	
	protected static void alertDialogDismiss()
	{
		if (alertDialog != null)
		{
			alertDialog.dismiss();
			alertDialog = null;
		}
	}
	
	public static void iProgressDialogDismiss()
	{
		if (iProgressDialog != null)
		{
			iProgressDialog.dismiss();
			iProgressDialog = null;
		}
	}

	public static void dProgressDialogDismiss()
	{
		if (dProgressDialog != null)
		{
			dProgressDialog.dismiss();
			dProgressDialog = null;
		}
	}
	
	public static void iProgressShow(String title, String message, int icon)
	{
		if (iProgressDialog == null)
		{
			iProgressDialog = new ProgressDialog(activity);
		}
		iProgressDialog.setIcon(icon);
		iProgressDialog.setTitle(title);
		iProgressDialog.setCancelable(false);
		iProgressDialog.setMessage(message);
//		iProgressDialog.setMessage("Veuillez patienter,\n\nChargement / rafraichissement des données en cours...");
		iProgressDialog.show();
//		httpProgressDialog = ProgressDialog.show(a, "Mon Compte Free", "Connexion en cours ...", true,false);
	}

	public static void dProgressSet(String title, String message, int icon)
	{
		dProgressTitle = title;
		dProgressText = message;
		dProgressIcon = icon;
	}
	
	public static void dProgressShow()
    {
		if (activity != null)
		{
	    	if (dProgressDialog == null)
	    	{
	    		dProgressDialog = new ProgressDialog(activity);
	    	}
			dProgressDialog.setIcon(dProgressIcon);
			dProgressDialog.setTitle(dProgressTitle);
			dProgressDialog.setMessage(dProgressText);
			dProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dProgressDialog.setCancelable(false);
			dProgressDialog.setMax(dProgressMax);
			dProgressDialog.show();
		}
    }

	protected static void dProgressMessage(String m, int max)
	{
		dProgressText = m;
		dProgressMax = max;
	}

	protected static void dProgressMax2(int max)
	{
		dProgressMax = max;
	}

    protected static void dProgressUpdate(int progress)
    {
    	if (activity != null)
    	{
	    	if ((dProgressDialog == null) || (progress == 0))
	    		dProgressShow();
	    	dProgressDialog.setProgress(progress);
    	}
    }
    
    /**
     * alertDialogShow : display an alert dialog with handle of screen rotation
     * @param title
     * @param message
     * @param icon
     */
    public static void alertDialogShow(String title, String message, int icon)
    {
    	if (alertDialog == null)
    	{
    		alertDialog = new AlertDialog.Builder(activity).create();
    	}
		alertDialog.setTitle(title);
		alertDialog.setIcon(icon);
		alertDialog.setMessage(message);
		alertDialog.setButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					alertDialogDismiss();
				}
			}
		);
		alertDialog.show();
    }
    
	protected int getJSONBoolean(JSONObject o, String key)
	{
		if (o.has(key))
		{
			try
			{
				return (o.getBoolean(key)?1:0);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	protected int getJSONInt(JSONObject o, String key)
	{
		if (o.has(key))
		{
			try
			{
				return o.getInt(key);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return 0;
	}

	protected String getJSONString(JSONObject o, String key)
	{
		if (o.has(key))
		{
			try
			{
				return o.getString(key);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return "";
	}
}
