package org.madprod.freeboxmobile.guide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.ServiceUpdateUIListener;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideChoixChainesActivity extends GuideUtils implements GuideConstants
{
	private static ChainesDbAdapter mDbHelper;
	// -1 si une chaine a été enlevée / 1 si une chaine a été ajoutée / 0 si pas bougé
	// si suppression et ajout : 1
	private int activityResult = 0; 
	private int itemSelected = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FBMNetTask.register(this);
        Log.i(TAG,"GUIDECHOIXCHAINES CREATE");
        setContentView(R.layout.guide_choix_chaines);
        mDbHelper = new ChainesDbAdapter(this);
        mDbHelper.open();
        setTitle(getString(R.string.app_name)+" Favoris - "+FBMHttpConnection.getTitle());
        displayFavoris(this, 
           	new View.OnClickListener()
        	{
	    		public void onClick(View view)
	    		{
	    			displaySuppr((Integer)view.getTag());
	    		}
	    	},
		R.id.ChoixSelectedLinearLayout, itemSelected);

//        getFavoris();

		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		if (!mgr.getString(KEY_SPLASH_FAVORIS, "0").equals(Utils.getFBMVersion(this)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_FAVORIS, Utils.getFBMVersion(this));
			editor.commit();
			displayAboutFavoris();
		}
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    }

    @Override
    public void onDestroy()
    {
    	FBMNetTask.unregister(this);
        mDbHelper.close();
    	super.onDestroy();
    }

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id)
	{
		super.onListItemClick(l, v, pos, id);
		itemSelected = pos;
		Log.d(TAG,"ITEM SELECTED : "+itemSelected);
		TextView chaine_id = (TextView) v.findViewById(R.id.HiddenTextView); 
		displayAdd(Integer.parseInt((String)chaine_id.getText()));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	        Log.d(this.getClass().getName(), "back button pressed");

	        if (activityResult == 1)
	        {
		    	AlertDialog d = new AlertDialog.Builder(this).create();
				d.setTitle(getString(R.string.app_name)+" - GuideTV");
				d.setIcon(R.drawable.fm_guide_tv);
				d.setMessage(
					"Les programmes du guide sont mis à jour automatiquement en tâche de fond toutes les 24 heures.\n\n"+
					"Des favoris viennent d'être ajoutés. Voulez-vous mettre à jour les programmes du guide maintenant (opération longue) ?");
				d.setButton(DialogInterface.BUTTON_POSITIVE, "Oui", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
			            	GuideCheck.setActivity(GuideChoixChainesActivity.this);
			               	GuideCheck.setUpdateListener(
			               			new ServiceUpdateUIListener()
			            	    	{
			            				@Override
			            				public void updateUI()
			            				{
			            				}
			            	    	});
			    			GuideCheck.refresh(null);
							dialog.dismiss();
							GuideChoixChainesActivity.this.finish();
						}
					});
				d.setButton(DialogInterface.BUTTON_NEGATIVE, "Non", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						GuideChoixChainesActivity.this.finish();
					}
				}
					);
				d.show();
	        }
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
    private void displayAdd(final Integer id)
    {
    	displayDialog(id, FAVORIS_COMMAND_ADD, "Ajouter cette chaîne aux favoris ?");
    }

    private void displaySuppr(final Integer id)
    {
    	displayDialog(id, FAVORIS_COMMAND_SUPPR, "Supprimer cette chaîne des favoris ?");
    }
    
    private void displayDialog(final Integer id, final int command, String msg)
    {
		Cursor chaineCursor = mDbHelper.getGuideChaine(id);
		chaineCursor.moveToFirst();
		String image = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE));
		String name = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_NAME));
		chaineCursor.close();
		String filepath = Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+image;
		Drawable draw = Drawable.createFromPath(filepath);
		
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(name);
		d.setIcon(draw);
    	d.setMessage(msg);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Oui", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					new GuideFavorisActivityNetwork(command, id).execute();
					dialog.dismiss();
				}
			});
		d.setButton(DialogInterface.BUTTON_NEGATIVE, "Non", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		d.show();
    }
    
	private void displayAboutFavoris()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name)+" - Favoris");
		d.setIcon(R.drawable.fm_guide_tv);
		d.setMessage(
			"Utilisez la liste horizontale d'icônes pour voir les favoris actuels.\n"+
			"Cliquez sur un icône de chaîne pour supprimer un favori.\n\n"+
			"Utilisez la liste verticale (icônes + nom) pour ajouter des favoris."
			);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			}
			);
		d.show();
    }
	
    public void showError()
    {
    	AlertDialog d = new AlertDialog.Builder(this).create();
    	d.setIcon(R.drawable.fm_guide_tv);
		d.setTitle("Erreur");
    	d.setMessage("Problème réseau, essayez à nouveau.");
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
		d.show();
    }
        
    private class GuideFavorisActivityNetwork extends AsyncTask<Void, Integer, Boolean>
    {
    	private int command;
    	private int param;
    	
        protected void onPreExecute()
        {
        	switch (command)
        	{
	        	case FAVORIS_COMMAND_RESET:
	        		FBMNetTask.iProgressShow("Réinitialisation","",R.drawable.fm_guide_tv);
	            	break;
	        	case FAVORIS_COMMAND_ADD:
	        		FBMNetTask.iProgressShow("Ajout","",R.drawable.fm_guide_tv);
	            	break;
	        	case FAVORIS_COMMAND_SUPPR:
	        		FBMNetTask.iProgressShow("Suppression","",R.drawable.fm_guide_tv);
	            	break;
        	}
        }

        protected Boolean doInBackground(Void... arg0)
        {
        	Boolean result = false;
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        	switch (command)
        	{
	        	case FAVORIS_COMMAND_RESET:
	                params.add(new BasicNameValuePair("ajax","raz_chaine"));
	            	break;
	        	case FAVORIS_COMMAND_ADD:
	                params.add(new BasicNameValuePair("ajax","add_chaine"));
	            	break;
	        	case FAVORIS_COMMAND_SUPPR:
	                params.add(new BasicNameValuePair("ajax","del_chaine"));
	            	break;
        	}
            params.add(new BasicNameValuePair("chaine", ((Integer)param).toString()));
        	result = Action(params);
        	switch (command)
        	{
	        	case FAVORIS_COMMAND_RESET:
	            	break;
	        	case FAVORIS_COMMAND_ADD:
	                mDbHelper.clearHistorique();
//	                new GuideNetwork(GuideChoixChainesActivity.this, null, false, true, param, true).getData();
	            	break;
	        	case FAVORIS_COMMAND_SUPPR:
	            	break;
        	}
        	return result;
        }
        
        protected void onPostExecute(Boolean result)
        {
        	FBMNetTask.iProgressDialogDismiss();
            displayFavoris(GuideChoixChainesActivity.this, 
                   	new View.OnClickListener()
                	{
        	    		public void onClick(View view)
        	    		{
        	    			displaySuppr((Integer)view.getTag());
        	    		}
        	    	},
        		R.id.ChoixSelectedLinearLayout, itemSelected);
        	switch (command)
        	{
	        	case FAVORIS_COMMAND_RESET:
	        		activityResult = 1;
	            	break;
	        	case FAVORIS_COMMAND_ADD:
	        		activityResult = 1;
	            	break;
	        	case FAVORIS_COMMAND_SUPPR:
	        		if (activityResult == 0)
	        		{
	        			activityResult = -1;
	        		}
	            	break;
        	}
            setResult(activityResult);
       		if (!result)
       		{
       			showError();
       		}
        }
        
        public GuideFavorisActivityNetwork(int command, int param)
        {
        	this.command = command;
        	this.param = param;
        	Log.d(TAG,"GUIDEFAVORISACTIVITYNETWORK START "+command+" "+param);
        }
        
        private boolean Action(ArrayList<NameValuePair> params)
        {
			Log.d(TAG, "===============> ACTION !");
        	String res = FBMHttpConnection.getPage(FBMHttpConnection.getAuthXmlRequest(MAGNETO_URL, params, true, true, "UTF8"));
            if (res != null)
            {
            	try
            	{
            		JSONObject jObject = new JSONObject(res);
    				if (jObject.has("redirect"))
    				{
    					Log.d(TAG, "===============> REDIRECT !");
    					if (FBMHttpConnection.connect() == CONNECT_CONNECTED)
    					{
        					Log.d(TAG, "===============> CONNECTED !");
    						res = FBMHttpConnection.getPage(FBMHttpConnection.getAuthXmlRequest(MAGNETO_URL, params, true, true, "UTF8"));
    						if (res != null)
    						{
            					Log.d(TAG, "===============> NOT  NULL !");
	    						try
	    						{
	    							jObject = new JSONObject(res);
	    		    				if (jObject.has("redirect"))
	    		    				{
	    		    					Log.d(TAG, "===============> REDIRECT ! "+res);
	    		    					return (false);
	    		    				}
	    						}
	    		            	catch (JSONException e)
	    		            	{
	    		    				Log.e(TAG,"JSONException ! "+e.getMessage());
	    		    				Log.e(TAG,res);
	    		    				e.printStackTrace();
	    		    				return false;
	    		    			}
    						}
    						else
    						{
            					Log.d(TAG, "===============> NULL !");
    							return false;
    						}
    					}
    					else
    					{
        					Log.d(TAG, "===============> NOT CONNECTED !");
    						return (false);
    					}
    				}
					Log.d(TAG, "CHECKING FAV "+res );
					JSONArray jArray = jObject.getJSONArray("new_chaines");
					int size = jArray.length();
		    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    		String datetime = sdf.format(new Date());
		    		ChainesDbAdapter db = new ChainesDbAdapter(GuideChoixChainesActivity.this);
		    		if (db == null)
		    		{
		    			return (false);
		    		}
		    		db.open();

					for (int i=0 ; i < size ; i++)
					{
//						Log.d(TAG, "fav : "+jArray.getString(i) );
						db.updateFavoris(Integer.parseInt(jArray.getString(i)), datetime);
					}
					db.flushFavoris(datetime);
					db.close();
            	}
            	catch (JSONException e)
            	{
    				Log.e(TAG,"JSONException ! "+e.getMessage());
    				Log.e(TAG,res);
    				e.printStackTrace();
    				return false;
    			}
//            	Log.d(TAG,"ACTION : "+res);
                return true;
            }
            else
            {
            	Log.d(TAG, "===============> RES NULL !");
            }
            return false;
        }
    }

	@Override
	protected boolean getFromDb()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
