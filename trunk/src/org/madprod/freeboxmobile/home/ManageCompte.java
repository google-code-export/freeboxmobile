package org.madprod.freeboxmobile.home;

import java.util.Date;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.guide.GuideActivity;
import org.madprod.freeboxmobile.guide.GuideNetwork;
import org.madprod.freeboxmobile.pvr.ProgrammationActivity;
import org.madprod.freeboxmobile.pvr.PvrNetwork;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class ManageCompte extends FBMNetTask implements Constants// AsyncTask<ComptePayload, Void, ComptePayload> implements Constants
{
	ComptePayload payload;

	@Override
	protected Integer doInBackground(Void...params)
	{
		payload.result = FBMHttpConnection.connectFreeCheck(payload.login, payload.password);
		if ((payload.result != null) && (payload.refresh))
		{
	    	dProgressSet("Importation", "", R.drawable.fm_magnetoscope);
			new PvrNetwork(true, true).getData();
	    	ProgrammationActivity.lastUser = "";
			new GuideNetwork(getActivity(), null, true, true, 0, false).getData();
		}
		return (0);
	}

	@Override
	protected void onPreExecute()
	{
		iProgressShow("Mon compte Freebox", "Veuillez patienter,\n\nChargement / rafraichissement des données en cours...", R.drawable.icon_fbm_reverse);
	}

	@Override
	protected void onPostExecute(Integer i)
	{
		dismissAll();
		if (payload.result != null)
		{
				payload.exit = Activity.RESULT_OK;
				saveState(payload);

				if (!payload.refresh)
				{
					getActivity().finish();
				}
		}
		else
		{
			alertDialogShow("Connexion impossible",
					"Impossible de se connecter au portail de Free.\n"+
					"Vérifiez votre identifiant, " +
					"votre mot de passe et votre "+	
					"connexion à Internet (Wifi, 3G...).",
					R.drawable.icon_fbm_reverse);
				payload.exit = Activity.RESULT_CANCELED;
		}
	}

	public ManageCompte(ComptePayload p)
	{
		payload = p;
	}

    private void saveState(ComptePayload p)
    {
        Bundle bundle = new Bundle();
        long id=0;
 
        ComptesDbAdapter mDbHelper = new ComptesDbAdapter(getActivity());
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
	            id = mDbHelper.createCompte(p.title, p.login, p.password, (String) v.get(KEY_NRA),
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
	        	id = p.rowid;
	        }

            bundle.putLong(ComptesDbAdapter.KEY_ROWID, p.rowid);
    	}
    	else
    	{
            bundle.putLong(ComptesDbAdapter.KEY_ROWID, 0);
    	}
    	if (!p.refresh)
    	{
            Intent mIntent = new Intent();
	        mIntent.putExtras(bundle);
	        getActivity().setResult(p.exit, mIntent);
    	}
		Cursor c = mDbHelper.fetchCompte(p.rowid);
		// On met à jour les parametres de conf avec les nouvelles données
    	if ((p.refresh) || (c.getString(c.getColumnIndexOrThrow(KEY_USER)).equals(FBMHttpConnection.getIdentifiant())))
    	{
    		Log.d(TAG,"REFRESH !");
    		SharedPreferences mgr = getActivity().getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE);
    		Editor editor = mgr.edit();
    		editor.putString(KEY_USER, c.getString(c.getColumnIndexOrThrow(KEY_USER)));
    		editor.putString(KEY_PASSWORD, c.getString(c.getColumnIndexOrThrow(KEY_PASSWORD)));
    		editor.putString(KEY_TITLE, c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
    		editor.putString(KEY_NRA, c.getString(c.getColumnIndexOrThrow(KEY_NRA)));
    		editor.putString(KEY_DSLAM, c.getString(c.getColumnIndexOrThrow(KEY_DSLAM)));
    		editor.putString(KEY_IP, c.getString(c.getColumnIndexOrThrow(KEY_IP)));
    		editor.putString(KEY_TEL, c.getString(c.getColumnIndexOrThrow(KEY_TEL)));
    		editor.putString(KEY_LINELENGTH, c.getString(c.getColumnIndexOrThrow(KEY_LINELENGTH)));
    		editor.putString(KEY_ATTN, c.getString(c.getColumnIndexOrThrow(KEY_ATTN)));
    		editor.putString(KEY_LINETYPE, c.getString(c.getColumnIndexOrThrow(KEY_LINETYPE)));
    		editor.putString(KEY_FBMVERSION, c.getString(c.getColumnIndexOrThrow(KEY_FBMVERSION)));
    		editor.commit();
    	}
		c.close();
    	mDbHelper.close();
    }
}
