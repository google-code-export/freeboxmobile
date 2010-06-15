package org.madprod.freeboxmobile.guide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;
import org.madprod.freeboxmobile.pvr.ProgrammationActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideChaineActivity extends GuideUtils implements GuideConstants
{
	private Spinner datesSpinner;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		Integer i;
		
		ArrayAdapter<String> spinnerAdapter;
        super.onCreate(savedInstanceState);

        FBMNetTask.register(this);
        Log.i(TAG,"GUIDE CHAINE CREATE");
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		mode_reduit = mgr.getBoolean(KEY_MODE, false);
		selectedChaine = 0;

		setContentView(R.layout.guide_chaine);
        registerForContextMenu(getListView());
        categories = new ArrayList<Categorie>();
        
        for (i = 0; i < genres.length; i++)
        {
        	if (genres[i].length() > 0)
        	{
	        	Categorie c = new Categorie();
	        	c.name = genres[i];
	        	c.id = i; 
	        	c.checked = true;
	        	categories.add(c);
        	}
        }
        Collections.sort(categories);
        categoriesDialog = new String[categories.size()];
        for (i=0; i < categories.size(); i++)
        {
        	categoriesDialog[i] = categories.get(i).name;
        }

        mDbHelper = new ChainesDbAdapter(this);
        mDbHelper.open();
       
        final Bundle extras = getIntent().getExtras();
        datesSpinner = (Spinner) findViewById(R.id.DatesSpinner);

        makeCalDates();
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);

    	// Selections
    	selectedDate = GuideUtils.calDates.get(0);

		spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, GuideUtils.dates);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		datesSpinner.setAdapter(spinnerAdapter);

		Button buttonCategories = (Button) findViewById(R.id.ButtonCategories);
        buttonCategories.setOnClickListener(
        	new View.OnClickListener()
        	{
        		public void onClick(View view)
        		{
        			chooseCategories();
        		}
        	}
        );

    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
    	String dt;
        if (extras != null)
        {
	        dt = extras.getString("DATE");
	        String mDate = dt;
	        for (i=0 ; i<GuideUtils.calDates.size() ; i++)
	        {
	        	if (GuideUtils.calDates.get(i).equals(mDate))
	        	{
	        		datesSpinner.setSelection(i, true);
	        		selectedDate = mDate;
	        		break;
	        	}
	        }
	        selectedChaine = extras.getInt("CHAINE");
        }
        else
        {
        	dt = sdf.format(new Date());
        }
    	Log.i(TAG, "Selected chaine : "+selectedChaine);
		setFinDateHeure();
//        boolean nochaine = getFromDb();
   		new GuideChaineActivityNetwork(dt, false, false, true).execute((Void[])null);

        displayFavoris(this,
               	new View.OnClickListener()
            	{
    	    		public void onClick(View view)
    	    		{
            			selectedDate = GuideUtils.calDates.get(datesSpinner.getSelectedItemPosition());
        				setFinDateHeure();
    	    			selectedChaine = (Integer)view.getTag();
    					new GuideChaineActivityNetwork(selectedDate, false, false, true).execute((Void[])null);
    	    		}
    	    	},
    		R.id.ChoixSelectedLinearLayout, -1);

    	setTitle(getString(R.string.app_name)+" Guide TV - "+FBMHttpConnection.getTitle());
		if (!mgr.getString(KEY_SPLASH_GUIDE, "0").equals(Utils.getFBMVersion(this)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_GUIDE, Utils.getFBMVersion(this));
			editor.commit();
			displayHelp();
		}
    }
	
    @Override
    public void onStart()
    {
    	super.onStart();
    	Log.i(TAG,"GUIDE CHAINE onStart");
    }

    @Override
    public void onDestroy()
    {
    	FBMNetTask.unregister(this);
        mDbHelper.close();
    	super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	menu.removeItem(GUIDE_OPTION_MODE);
        if (mode_reduit)
        {
        	menu.add(0, GUIDE_OPTION_MODE, 2, R.string.guide_option_mode_etendu).setIcon(android.R.drawable.ic_menu_view);
        }
        else
        {
        	menu.add(0, GUIDE_OPTION_MODE, 2, R.string.guide_option_mode_reduit).setIcon(android.R.drawable.ic_menu_view);
        }
        return true;
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, GUIDE_OPTION_REFRESH, 0, R.string.guide_option_refresh).setIcon(android.R.drawable.ic_menu_rotate);
        menu.add(0, GUIDE_OPTION_SELECT, 1, R.string.guide_option_select).setIcon(android.R.drawable.ic_menu_add);
        if (mode_reduit)
        	menu.add(0, GUIDE_OPTION_MODE, 2, R.string.guide_option_mode_etendu).setIcon(android.R.drawable.ic_menu_view);
        else
        	menu.add(0, GUIDE_OPTION_MODE, 2, R.string.guide_option_mode_reduit).setIcon(android.R.drawable.ic_menu_view);
        menu.add(0, GUIDE_OPTION_FILTER, 1, R.string.guide_option_filter).setIcon(android.R.drawable.ic_menu_more);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    		case GUIDE_OPTION_SELECT:
    			startActivityForResult(new Intent(this, GuideChoixChainesActivity.class),0);
    			return true;
    		case GUIDE_OPTION_REFRESH:
    	   		new GuideChaineActivityNetwork(selectedDate, false, true, true).execute((Void[])null);
    			return true;
    		case GUIDE_OPTION_MODE:
    			mode_reduit = (mode_reduit ? false : true);
    			Editor editor = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).edit();
    			editor.putBoolean(KEY_MODE, mode_reduit);
    			editor.commit();
    	    	adapter.notifyDataSetChanged();
    			return true;
    		case GUIDE_OPTION_FILTER:
    			chooseCategories();
    			return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id)
	{
		super.onListItemClick(l, v, pos, id);
		launchActivity(GuideDetailsActivity.class, pos);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{
		AdapterContextMenuInfo info;

		super.onCreateContextMenu(menu, view, menuInfo);
		info = (AdapterContextMenuInfo) menuInfo;
		Programme p = (Programme) adapter.getItem((int)info.id);
	    menu.setHeaderTitle(p.titre);
	    menu.add(0, GUIDE_CONTEXT_ENREGISTRER, 0, "Enregistrer");
	    menu.add(0, GUIDE_CONTEXT_DETAILS, 1, "Détails");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	switch (item.getItemId())
    	{
			case GUIDE_CONTEXT_ENREGISTRER:
				launchActivity(ProgrammationActivity.class, (int)info.id);
			break;
			case GUIDE_CONTEXT_DETAILS:
				launchActivity(GuideDetailsActivity.class, (int)info.id);
			break;
    	}
    	return super.onContextItemSelected(item);
	}

	@Override
    protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString("selectedDate", selectedDate);
        outState.putInt("spinnerDate", datesSpinner.getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
    	Log.i(TAG,"onRestore !");
/*
            dureeEmission.setText(savedInstanceState.getString("da"));
            nomEmission.setText(savedInstanceState.getString("nomEmission"));
            nomEmissionSaisi = savedInstanceState.getBoolean("nomEmissionSaisi");
            choosen_year_deb = savedInstanceState.getInt("choosen_year_deb");
            choosen_month_deb = savedInstanceState.getInt("choosen_month_deb");
            choosen_day_deb = savedInstanceState.getInt("choosen_day_deb");
            choosen_hour_deb = savedInstanceState.getInt("choosen_hour_deb");
            choosen_minute_deb = savedInstanceState.getInt("choosen_minute_deb");
*/    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	Log.i(TAG,"ON ACTIVITY RESULT : "+resultCode);
    	switch (resultCode)
    	{
    		case 0 :
    			break;
    		case -1 :
    			mDbHelper.close();
    	        mDbHelper = new ChainesDbAdapter(this);
    	        mDbHelper.open();
    		case 1 :
    	    	Log.d(TAG,"RESULT ADD/SUPPR");
    	        displayFavoris(this,
    	               	new View.OnClickListener()
    	            	{
    	    	    		public void onClick(View view)
    	    	    		{
    	            			selectedDate = GuideUtils.calDates.get(datesSpinner.getSelectedItemPosition());
    	        				setFinDateHeure();
    	    	    			selectedChaine = (Integer)view.getTag();
    	    					new GuideChaineActivityNetwork(selectedDate, false, false, true).execute((Void[])null);
    	    	    		}
    	    	    	},
    	    		R.id.ChoixSelectedLinearLayout, -1);
//    			getFromDb();
    			break;
    	}
    }

    private void setFinDateHeure()
    {
            long datefin = datesSpinner.getSelectedItemId();
            String sdatefin = GuideUtils.calDates.get((int) (datefin+1));
            finDateHeure = sdatefin+" 00:00:00";
	}

	/**
	 * getFromDb
	 * @return true if there is no chaine in db
	 */
	public boolean getFromDb()
	{
		boolean nochaine = false;

		Log.d(TAG,"getFromDb");
	    adapter = new SectionedAdapter()
	    {
	    	protected View getHeaderView(String caption, Bitmap bmp, int index, View convertView, ViewGroup parent)
	    	{
	    		LinearLayout header = (LinearLayout) convertView;
	    		if (convertView == null)
	    		{
					header = (LinearLayout)getLayoutInflater().inflate(R.layout.guide_list_header, null);
	    		}
	    		TextView result = (TextView)header.findViewById(R.id.TextViewHeader);
	    		ImageView logo = (ImageView)header.findViewById(R.id.ImageViewHeader);
				if (bmp != null)
				{
					logo.setImageBitmap(bmp);
				}
				else
				{
					logo.setVisibility(View.GONE);
				}
	    		result.setText(caption);
	    		return(header);
	    	}    	
	    };
	    listesChaines = new ArrayList<ListeChaines>();
//    	Cursor chainesIds = mDbHelper.getChaineFavoris();
 //   	startManagingCursor (chainesIds);
 //       if (chainesIds != null)
//		{
//			if (chainesIds.moveToFirst())
//			{
				Cursor chaineCursor;
				ListeChaines l;
//				int columnIndex = chainesIds.getColumnIndexOrThrow(ChainesDbAdapter.KEY_FAVORIS_ID);
//				do
//				{
					l = new ListeChaines();
					l.chaine_id = selectedChaine;//chainesIds.getInt(columnIndex);
					chaineCursor = mDbHelper.getGuideChaine(l.chaine_id);
					if ((chaineCursor != null))
					{
						if (chaineCursor.moveToFirst())
						{
							l.canal = chaineCursor.getInt(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL));
							l.name = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_NAME));
							l.image = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE));
							l.guidechaine_id = chaineCursor.getInt(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_ID));
							chaineCursor.close();
						}
						else
						{
							nochaine = true;
						}
					}
					else
						nochaine = true;

			        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			        String datetoget = selectedDate+" 00:00:00";//+selectedHeure;
			        try
			        {
						Date dselect = sdf.parse(datetoget);
				        if (dselect.before(new Date()))
				        {
				        	datetoget = sdf.format(new Date());
				        	selectedDate = datetoget.split(" ")[0];
				        }
					}
			        catch (ParseException e)
			        {
			        	Log.e(TAG,"GUIDEACTIVITY : pb parse date "+e.getMessage());
						e.printStackTrace();
					}
					l.programmes = mDbHelper.getProgrammes(l.chaine_id, datetoget, finDateHeure, categories);
					startManagingCursor(l.programmes);
					listesChaines.add(l);
//				} while (chainesIds.moveToNext());
				
				// TODO : si nochaine == true, il manque une chaine, lancer un téléchargement des chaines du guide
				if (nochaine == true)
				{
					Log.d(TAG,"IL MANQUE AU MOINS UNE CHAINE");
				}
				// Ici on trie pour avoir les listes de programmes dans l'ordre des numéros de chaine
				Collections.sort(listesChaines);
				
				// Puis on créé les différentes sous-listes (une par chaine)
				ga = new ArrayList<GuideAdapter>();
				Iterator<ListeChaines> it = listesChaines.iterator();
				String filepath;
				Bitmap bmp;
				while(it.hasNext())
				{
					l = it.next();
					GuideAdapter g = new GuideAdapter(this, l);
					ga.add(g);
			        filepath = Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+l.image;
					bmp = BitmapFactory.decodeFile(filepath);
					adapter.addSection(l.name+" ("+((Integer)l.canal).toString()+")",bmp,g);
				}
//			}
//		}
        setListAdapter(adapter);
        return nochaine;
	}
	
    private class GuideChaineActivityNetwork extends FBMNetTask
    {
    	private String debdatetime;
    	private boolean getChaines;
    	private boolean forceRefresh;
    	private boolean refreshActivity;
    	
        protected void onPreExecute()
        {
        }

        protected Integer doInBackground(Void... arg0)
        {
        	return new GuideNetwork(GuideChaineActivity.this, debdatetime, 24, getChaines, true, forceRefresh).getData();
        }

        protected void onPostExecute(Integer result)
        {
        	if (((result != DATA_NOT_DOWNLOADED) && (getChaines)) || (refreshActivity))
        	{
        		getFromDb();
        	}
        	if (result == DATA_NOT_DOWNLOADED)
        	{
        		displayError();
        	}
       		if ((!refreshActivity) && (!getChaines))
       		{
       			refresh();
       		}
        }

        /**
         * GuideactivityNetwork
         * @param d : datetime début
         * @param chaine : récupérer liste de toutes les chaines (+ logos) ?
         * @param prog : récupérer liste des programmes des chaines favorites ?
         * @param force : forcer la récupération des programmes (ne pas tenir compte du cache) ?
         * @param refreshactivity : pour rafraichir toute l'activité après un onActivityResult du choix des favoris
         */
        public GuideChaineActivityNetwork(String d, boolean chaine, boolean force, boolean refreshactivity)
        {
       		debdatetime = d;
        	getChaines = chaine;
        	forceRefresh = force;
        	refreshActivity = refreshactivity;
        	Log.d(TAG,"GUIDECHAINEACTIVITYNETWORK START "+d+" "+chaine);
        }
    }
}
