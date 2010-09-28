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
import org.madprod.freeboxmobile.ServiceUpdateUIListener;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;
import org.madprod.freeboxmobile.pvr.ProgrammationActivity;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideChaineActivity extends GuideUtils implements GuideConstants
{
	private Spinner datesSpinner;
	GoogleAnalyticsTracker tracker;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		Integer i;
		
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Guide/GuideChaine");
		
		ArrayAdapter<String> spinnerAdapter;
        super.onCreate(savedInstanceState);

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

        datesSpinner = (Spinner) findViewById(R.id.DatesSpinner);
		spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, GuideUtils.dates);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		datesSpinner.setAdapter(spinnerAdapter);

        final Bundle extras = getIntent().getExtras();
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

        makeCalDates();
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);

		datesSpinner.setOnItemSelectedListener(
			new OnItemSelectedListener()
			{
				public void onItemSelected(AdapterView<?> view, View arg1, int arg2, long arg3)
	    		{
	    			selectedDate = GuideUtils.calDates.get(arg2);
					setFinDateHeure();
	    			getFromDb();
	    		}

				@Override
				public void onNothingSelected(AdapterView<?> arg0)
				{
				}
			});

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

		setFinDateHeure();
        getFromDb();
//   		new GuideChaineActivityNetwork(dt, false, false, true).execute((Void[])null);

        displayFavoris(this,
               	new View.OnClickListener()
            	{
    	    		public void onClick(View view)
    	    		{
            			selectedDate = GuideUtils.calDates.get(datesSpinner.getSelectedItemPosition());
        				setFinDateHeure();
    	    			selectedChaine = (Integer)view.getTag();
    	    			getFromDb();
//    					new GuideChaineActivityNetwork(selectedDate, false, false, true).execute((Void[])null);
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
    public void onResume()
    {
    	super.onResume();
    	Log.i(TAG,"GUIDE CHAINE onResume");
    	GuideCheck.setActivity(this);
       	GuideCheck.setUpdateListener(
       			new ServiceUpdateUIListener()
    	    	{
    				@Override
    				public void updateUI()
    				{
    					Log.i(TAG,"updateUI");
    					runOnUiThread(
    						new Runnable()
    						{
    							public void run()
    							{
    								getFromDb();
    							}
    						});
    				}
    	    	});    	
    }
    
    @Override
    public void onDestroy()
    {
        mDbHelper.close();
        tracker.stop();
    	super.onDestroy();
    }

	@Override
	public void onPause()
	{
		Log.i(TAG,"GuideChaineActivity pause");
		super.onPause();
		GuideCheck.setActivity(null);
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
//    	   		new GuideChaineActivityNetwork(selectedDate, false, true, true).execute((Void[])null);
            	Log.d(TAG, "Refresh from point 2");
    			GuideCheck.refresh(selectedDate);
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
    	    	Log.d(TAG,"RESULT SUPPR");
    			mDbHelper.close();
    	        mDbHelper = new ChainesDbAdapter(this);
    	        mDbHelper.open();
    		case 1 :
    			if (resultCode == 1)
    			{
	    	    	Log.d(TAG,"RESULT ADD");
	    	    	// TODO : Enlever refresh auto ici
	    	    	// Ajouter boite de dialogue qui propose de rafraichir tous les jours
	    	    	// en sortant de la config des favoris
//	    	    	displayMAJ(this);
	    	    	
/*	            	Log.d(TAG, "Refresh from point 3");
	            	GuideCheck.setActivity(this);
	               	GuideCheck.setUpdateListener(
	               			new ServiceUpdateUIListener()
	            	    	{
	            				@Override
	            				public void updateUI()
	            				{
	            					Log.i(TAG,"updateUI");
	            					runOnUiThread(
	            						new Runnable()
	            						{
	            							public void run()
	            							{
	            								getFromDb();
	            							}
	            						});
	            				}
	            	    	});
	    			GuideCheck.refresh(GuideUtils.calDates.get(datesSpinner.getSelectedItemPosition()));
	    			*/
    			}
    			else
    			{
    				getFromDb();
    			}
    	        displayFavoris(this,
	               	new View.OnClickListener()
	            	{
	    	    		public void onClick(View view)
	    	    		{
	            			selectedDate = GuideUtils.calDates.get(datesSpinner.getSelectedItemPosition());
	        				setFinDateHeure();
	    	    			selectedChaine = (Integer)view.getTag();
	    	    			getFromDb();
//    	    					new GuideChaineActivityNetwork(selectedDate, false, false, true).execute((Void[])null);
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
		Cursor chaineCursor;
		ListeChaines l;
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
			}
			else
			{
				Log.d(TAG, "nochaine 1");
				nochaine = true;
			}
			chaineCursor.close();
		}
		else
		{
			Log.d(TAG, "nochaine 2");
			nochaine = true;
		}

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetoget = selectedDate+" 00:00:00";
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
        setListAdapter(adapter);
        if (nochaine == true)
        {
        	Log.d(TAG, "Refresh from point 1");
        	GuideCheck.refresh(GuideUtils.calDates.get(datesSpinner.getSelectedItemPosition()));
        }
        return nochaine;
	}
}
