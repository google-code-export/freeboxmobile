package org.madprod.freeboxmobile.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

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
	private ProgressDialog progressDialog = null;
	private List< Map<String,Object> > chainesToSelect;
	// -1 si une chaine a été enlevée / 1 si une chaine a été ajoutée / 0 si pas bougé
	// si suppression et ajout : 1
	private int activityResult = 0; 
	private int itemSelected = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FBMHttpConnection.initVars(this, null);
        FBMHttpConnection.FBMLog("GUIDECHOIXCHAINES CREATE");
        setContentView(R.layout.guide_choix_chaines);
        mDbHelper = new ChainesDbAdapter(this);
        mDbHelper.open();
        setTitle(getString(R.string.app_name)+" Favoris - "+FBMHttpConnection.getTitle());
        getFavoris();
        
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		if (!mgr.getString(KEY_SPLASH_FAVORIS, "0").equals(this.getString(R.string.app_version)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_FAVORIS, this.getString(R.string.app_version));
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
        mDbHelper.close();
    	super.onDestroy();
    }

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id)
	{
		super.onListItemClick(l, v, pos, id);
		itemSelected = pos;
		FBMHttpConnection.FBMLog("ITEM SELECTED : "+itemSelected);
		TextView chaine_id = (TextView) v.findViewById(R.id.HiddenTextView); 
		displayAdd(Integer.decode((String)chaine_id.getText()));
	}

    private void refresh()
    {
    	getFavoris();
    }
    
    private void getFavoris()
    {
		FBMHttpConnection.FBMLog("getFavoris");

		listeFavoris.clear();
		// On commence par récupérer la liste des chaines favorites
    	Cursor chainesIds = mDbHelper.getChainesProg();
    	startManagingCursor (chainesIds);
        if (chainesIds != null)
		{
			Favoris f;
			if (chainesIds.moveToFirst())
			{
				Cursor chaineCursor;
				boolean nochaine = false;
				
				int CI_progchannel_id = chainesIds.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_CHANNEL_ID);
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
					for (Iterator<Favoris> it = listeFavoris.iterator(); it.hasNext();)
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

			// On créé l'horizontal scrollview en haut avec la liste des chaines favorites
			Iterator<Favoris> it = listeFavoris.iterator();
			String filepath;
			Bitmap bmp;
	    	LinearLayout csly = (LinearLayout) findViewById(R.id.ChoixSelectedLinearLayout);
	    	csly.removeAllViews();
	    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	params.setMargins(5,5,5,5);
			while(it.hasNext())
			{
				f = it.next();
		        filepath = Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+f.image;
				bmp = BitmapFactory.decodeFile(filepath);
				ImageView i = new ImageView(this);
				i.setImageBitmap(bmp);
				i.setLayoutParams(params);
				i.setTag((Integer)f.guidechaine_id);
		        i.setOnClickListener(
		            	new View.OnClickListener()
		            	{
		            		public void onClick(View view)
		            		{
		            			displaySuppr((Integer)view.getTag());
		            		}
		            	}
		            );
				csly.addView(i);
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
    	FBMHttpConnection.FBMLog("displayDialog : "+id);
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
    
	public void showProgressDialog(String title)
	{
		progressDialog = new ProgressDialog(this);
		progressDialog.setIcon(R.drawable.icon_fbm_reverse);
		progressDialog.setTitle(title);
		progressDialog.setCancelable(false);
		progressDialog.setMessage("En cours...");
		progressDialog.show();
	}

    public void dismissPd()
    {
    	if (progressDialog != null)
    	{
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    }

    private class Favoris implements Comparable<Favoris>
    {
    	public int guidechaine_id;
    	public int canal;
    	public String name;
    	public String image;
    	
		@Override
		public int compareTo(Favoris another)
		{
			return (canal - another.canal);
		}
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
	            	showProgressDialog("Réinitialisation");
	            	break;
	        	case FAVORIS_COMMAND_ADD:
	            	showProgressDialog("Ajout");
	            	break;
	        	case FAVORIS_COMMAND_SUPPR:
	            	showProgressDialog("Suppression");
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
//        	if (result)
//        	{
	        	switch (command)
	        	{
		        	case FAVORIS_COMMAND_RESET:
		            	break;
		        	case FAVORIS_COMMAND_ADD:
		                mDbHelper.clearHistorique();
		                new GuideNetwork(GuideChoixChainesActivity.this, null, false, true, param, true).getData();
		            	break;
		        	case FAVORIS_COMMAND_SUPPR:
		                mDbHelper.deleteProgsChaine(param);
		            	break;
	        	}
//        	}
        	return result;
        }
        
        protected void onPostExecute(Boolean result)
        {
        	GuideActivity.dismissPd();
       		dismissPd();
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
        	FBMHttpConnection.FBMLog("GUIDEFAVORISACTIVITYNETWORK START "+command+" "+param);
        }
        
        private boolean Action(ArrayList<NameValuePair> params)
        {
        	String res = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(MAGNETO_URL, params, true, true, "UTF8"));
            if (res != null)
            {
            	try
            	{
            		JSONObject jObject = new JSONObject(res);
    				if (jObject.has("redirect"))
    				{
    					if (FBMHttpConnection.connect() == CONNECT_CONNECTED)
    					{
    						res = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(MAGNETO_URL, params, true, true, "UTF8"));
    						if (res != null)
    						{
	    						try
	    						{
	    							jObject = new JSONObject(res);
	    		    				if (jObject.has("redirect"))
	    		    					return (false);
	    						}
	    		            	catch (JSONException e)
	    		            	{
	    		    				FBMHttpConnection.FBMLog("JSONException ! "+e.getMessage());
	    		    				FBMHttpConnection.FBMLog(res);
	    		    				e.printStackTrace();
	    		    				return false;
	    		    			}    						
    						}
    						else
    							return false;
    					}
    					else
    					{
    						return (false);
    					}
    				}
            	}
            	catch (JSONException e)
            	{
    				FBMHttpConnection.FBMLog("JSONException ! "+e.getMessage());
    				FBMHttpConnection.FBMLog(res);
    				e.printStackTrace();
    				return false;
    			}
            	FBMHttpConnection.FBMLog("ACTION : "+res);
                return true;
            }
            return false;
        }
    }
}