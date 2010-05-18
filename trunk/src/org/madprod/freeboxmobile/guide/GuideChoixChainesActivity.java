package org.madprod.freeboxmobile.guide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideChoixChainesActivity extends ListActivity implements GuideConstants
{
	private static ChainesDbAdapter mDbHelper;
	private ArrayList<Favoris> listeFavoris = new ArrayList<Favoris>();
	private List< Map<String,Object> > chainesToSelect;
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
        getFavoris();

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

    private void refresh()
    {
    	getFavoris();
    }
    
    private void getFavoris()
    {
		Log.d(TAG,"getFavoris");

		listeFavoris.clear();
		
		// On commence par récupérer la liste des chaines favorites
    	Cursor chainesIds = mDbHelper.getFavoris();
    	startManagingCursor (chainesIds);
        if (chainesIds != null)
		{
			Favoris f;
			if (chainesIds.moveToFirst())
			{
				Cursor chaineCursor;
				boolean nochaine = false;
				
				int CI_progchannel_id = chainesIds.getColumnIndexOrThrow(ChainesDbAdapter.KEY_FAVORIS_ID);
				do
				{
					f = new Favoris();
					f.guidechaine_id = chainesIds.getInt(CI_progchannel_id);
					chaineCursor = mDbHelper.getGuideChaine(f.guidechaine_id);
					if ((chaineCursor != null) && (chaineCursor.moveToFirst()))
					{
						f.canal = chaineCursor.getInt(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL));
						f.name = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_NAME));
						f.image = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE));
						chaineCursor.close();
					}
					else
						nochaine = true;
					listeFavoris.add(f);
				} while (chainesIds.moveToNext());
			}
			// Ici on trie pour avoir les listes de programmes dans l'ordre des numéros de chaine
			Collections.sort(listeFavoris);

			// On créé l'horizontal scrollview en haut avec la liste des chaines favorites
			Iterator<Favoris> it = listeFavoris.iterator();
	    	LinearLayout csly = (LinearLayout) findViewById(R.id.ChoixSelectedLinearLayout);
	    	csly.removeAllViews();
			while(it.hasNext())
			{
				f = it.next();
				csly.addView(GuideUtils.addVisuelChaine(f.image, f.name, (Integer)f.guidechaine_id,
	            	new View.OnClickListener()
	            	{
	            		public void onClick(View view)
	            		{
	            			displaySuppr((Integer)view.getTag());
	            		}
	            	},
	            	this));
			}

			// Ici on récupère la liste des chaines disponibles pour le guide auxquelles ont enlève celles
			// déjà sélectionnées
			chainesToSelect = new ArrayList< Map<String,Object> >();
			Cursor allChaines = mDbHelper.getListChaines();
			if (allChaines != null)
			{
				if (allChaines.moveToFirst())
				{
					final int CI_image = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE);
					final int CI_canal = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL);
					final int CI_name = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_NAME);
					final int CI_id = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_ID);
					String image;
					Map<String,Object> map;
					for (it = listeFavoris.iterator(); it.hasNext();)
					{
						f = it.next();
						while ((!allChaines.isAfterLast()) &&
								(allChaines.getInt(CI_canal) != f.canal))
						{
							image = allChaines.getString(CI_image);
							map = new HashMap<String,Object>();
							if (image.length() > 0)
							{
								map.put(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+image);
							}
							else
							{
								map.put(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, R.drawable.chaine_vide);
							}
							map.put(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL, allChaines.getInt(CI_canal));
							map.put(ChainesDbAdapter.KEY_GUIDECHAINE_NAME, allChaines.getString(CI_name));
							map.put(ChainesDbAdapter.KEY_GUIDECHAINE_ID, allChaines.getInt(CI_id));
							chainesToSelect.add(map);
							allChaines.moveToNext();
						}
						allChaines.moveToNext();
					}
					// On doit refaire un tour pour les chaines non sélectionnées
					// dont le numéro est > au numéro de la dernière chaine des favoris
					while (!allChaines.isAfterLast())
					{
						image = allChaines.getString(CI_image);
						map = new HashMap<String,Object>();
						if (image.length() > 0)
						{
							map.put(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+image);
						}
						else
						{
							map.put(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, R.drawable.chaine_vide);
						}
						map.put(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL, allChaines.getInt(CI_canal));
						map.put(ChainesDbAdapter.KEY_GUIDECHAINE_NAME, allChaines.getString(CI_name));
						map.put(ChainesDbAdapter.KEY_GUIDECHAINE_ID, allChaines.getInt(CI_id));
						chainesToSelect.add(map);
						allChaines.moveToNext();							
					}
			        SimpleAdapter mList = new SimpleAdapter(
			        		this, chainesToSelect, R.layout.guide_favoris_row, 
			        		new String[] {ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, ChainesDbAdapter.KEY_GUIDECHAINE_NAME, ChainesDbAdapter.KEY_GUIDECHAINE_ID},
			        		new int[] {R.id.ImageViewFavoris, R.id.TextViewFavoris, R.id.HiddenTextView});
			        setListAdapter(mList);
			        if (itemSelected > 0)
			        {
			        	setSelection(itemSelected);
			        }
				}
				allChaines.close();
			}
		}
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
			"Utilisez la liste vericale (icônes + nom) pour ajouter des favoris."
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
	        		// TODO : check and remove this line
//	                mDbHelper.deleteProgsChaine(param);
	            	break;
        	}
        	return result;
        }
        
        protected void onPostExecute(Boolean result)
        {
        	FBMNetTask.iProgressDialogDismiss();
   			refresh();
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
        	String res = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(MAGNETO_URL, params, true, true, "UTF8"));
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
    						res = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(MAGNETO_URL, params, true, true, "UTF8"));
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
}
