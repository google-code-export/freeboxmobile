package org.madprod.freeboxmobile.guide;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;
import org.madprod.freeboxmobile.pvr.PvrNetwork;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideMenuActivity extends GuideUtils implements GuideConstants
{
	private ChainesDbAdapter mDbHelper;
	private Spinner jourChaine;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FBMNetTask.register(this);
        Log.i(TAG,"GUIDE MENU CREATE");
        setContentView(R.layout.guide_menu);

        mDbHelper = new ChainesDbAdapter(this);
        mDbHelper.open();
        Log.d(TAG,"Nettoyage des anciens programmes effacés : "+mDbHelper.deleteOldProgs());
        Log.d(TAG,"Nettoyage de l'ancienne historique : "+mDbHelper.deleteOldHisto());

        /*
		if(getIntent()!=null && getIntent().getAction() != null)
		{
			Log.d(TAG, "TYPE : "+getIntent().getType());
			Log.d(TAG, "ACTION : "+getIntent().getAction());
			Log.d(TAG, "SCHEME : "+getIntent().getScheme());
			Log.d(TAG, "PACKAGE : "+getIntent().getPackage());
			Log.d(TAG, "TOSTRING : "+getIntent().toString());
			Log.d(TAG, "FLAGS : "+ getIntent().getFlags());
			Log.d(TAG, "URI : "+ getIntent().toURI());
			Log.d(TAG, "DESCRIBE CONTENTS : "+ getIntent().describeContents());
			Log.d(TAG, "RESOLVE : "+ getIntent().resolveType(this));
			Log.d(TAG, "DATA STRING : "+ getIntent().getDataString());
			Log.d(TAG, "CATEGORIES : "+ getIntent().getCategories());
			
			if (getIntent().getExtras() != null)
			{
				Log.d(TAG, "EXTRA_TEXT : "+(String) getIntent().getExtras().get(Intent.EXTRA_TEXT));
				Log.d(TAG, "EXTRA STREAM : "+ getIntent().getExtras().get(Intent.EXTRA_STREAM));				
				Log.d(TAG, "SIZE : "+ getIntent().getExtras().size());
				Log.d(TAG, "TO STRING : "+ getIntent().getExtras().toString());
			}
			else
			{
				Log.d(TAG, "NO EXTRA !");
			}
			if (getIntent().getData() != null)
			{
				Log.d(TAG, "DATA : "+ getIntent().getData().toString());
			}
			else
			{
				Log.d(TAG, "NO DATA !");
			}
		}
*/
    }
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		Log.i(TAG,"GUIDE MENU START");
		FBMNetTask.register(this);

    	if (mDbHelper.getNbFavoris() == 0)
    	{
    		new GuideMenuActivityNetwork().execute((Void[])null);    		
    	}

        Button enCours = (Button) findViewById(R.id.ButtonEnCours);
        enCours.setOnClickListener(
            	new View.OnClickListener()
            	{
            		public void onClick(View view)
            		{
            			startActivity(new Intent(GuideMenuActivity.this, GuideActivity.class));
            		}
            	}
            );
        Button favoris = (Button) findViewById(R.id.ButtonFavoris);
        favoris.setOnClickListener(
            	new View.OnClickListener()
            	{
            		public void onClick(View view)
            		{
            			startActivity(new Intent(GuideMenuActivity.this, GuideChoixChainesActivity.class));
            		}
            	}
            );
        Button ceSoir = (Button) findViewById(R.id.ButtonCeSoir);
        ceSoir.setOnClickListener(
            	new View.OnClickListener()
            	{
            		public void onClick(View view)
            		{
				        Calendar c = Calendar.getInstance();
			        	Integer mois = c.get(Calendar.MONTH)+1;
			        	Integer jour = c.get(Calendar.DAY_OF_MONTH);
			        	String date = (c.get(Calendar.YEAR)+"-"+(mois<10?"0":"")+mois.toString()+"-"+(jour<10?"0":"")+jour.toString());
			        	date += " 20:00:00";
						Intent i = new Intent(GuideMenuActivity.this, GuideActivity.class);
						i.putExtra("DATE", date);
						i.putExtra("CHAINE", 0);
				        startActivity(i);
            		}
            	}
            );
        makeCalDates();
        final Spinner datesSpinner = (Spinner) findViewById(R.id.SpinnerDate);
        final Spinner heuresSpinner = (Spinner) findViewById(R.id.SpinnerHeure);
        jourChaine = (Spinner) findViewById(R.id.SpinnerJourChaine);
		ArrayAdapter<String> spinnerAdapter;
		spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, GuideUtils.dates);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		datesSpinner.setAdapter(spinnerAdapter);
		jourChaine.setAdapter(spinnerAdapter);

        datesSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				Calendar cal = Calendar.getInstance();
				String sdate = GuideUtils.calDates.get(arg2).split("-")[2];
				int jour = Integer.parseInt(sdate);
				if (cal.get(Calendar.DAY_OF_MONTH) == jour)
				{
					GuideUtils.remplirHeuresSpinner(GuideMenuActivity.this, cal.get(Calendar.HOUR_OF_DAY), R.id.SpinnerHeure);
				}
				else
				{
					GuideUtils.remplirHeuresSpinner(GuideMenuActivity.this, 0, R.id.SpinnerHeure);	
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
        });

		Button buttonDate = (Button) findViewById(R.id.ButtonDate);
        buttonDate.setOnClickListener(
        	new View.OnClickListener()
        	{
        		public void onClick(View view)
        		{
        			String selectedDate = GuideUtils.calDates.get(datesSpinner.getSelectedItemPosition());
        			String s = (String) heuresSpinner.getSelectedItem();
        			String selectedHeure = s.split("h")[0];
        			if (selectedHeure.length() == 1)
        			{
        				selectedHeure = "0"+selectedHeure;
        			}
        			selectedHeure += ":00:00";
					Intent i = new Intent(GuideMenuActivity.this, GuideActivity.class);
					i.putExtra("DATE", selectedDate+" "+selectedHeure);
					i.putExtra("CHAINE", 0);
			        startActivity(i);
        		}
        	}
        );
    }
	
	@Override
	protected void onDestroy()
	{
		mDbHelper.close();
		FBMNetTask.unregister(this);
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
    	displayFavoris(this, 
        		new View.OnClickListener()
    			{
    				public void onClick(View view)
    				{
    					Log.d(TAG, "click "+(Integer)view.getTag());
    	    			String selectedDate = GuideUtils.calDates.get(jourChaine.getSelectedItemPosition());
    					Intent i = new Intent(GuideMenuActivity.this, GuideChaineActivity.class);
    					i.putExtra("DATE", selectedDate);
    					i.putExtra("CHAINE", (Integer)view.getTag());
    			        startActivity(i);
    				}
    			},
    			R.id.ChoixSelectedLinearLayout, -1);
	}

	private class GuideMenuActivityNetwork extends FBMNetTask //AsyncTask<Void, Integer, Integer>
    {
        protected void onPreExecute()
        {
    		iProgressShow("Mon compte Freebox", "Veuillez patienter,\n\nChargement / rafraichissement des données en cours...", R.drawable.icon_fbm_reverse);
        }

        protected Integer doInBackground(Void... arg0)
        {
        	new GuideNetwork(GuideMenuActivity.this, null, 4, true, true, false).getData(); // To get chaines logos
        	// TODO : handle return type
        	new PvrNetwork(false, false).getData(); // to get favoris list
        	return 1;
        }

        protected void onPostExecute(Integer result)
        {
    		setProgressBarIndeterminateVisibility(false);
        	if (result != DATA_NOT_DOWNLOADED)
        	{
            	displayFavoris(GuideMenuActivity.this, 
                		new View.OnClickListener()
            			{
            				public void onClick(View view)
            				{
            					Log.d(TAG, "click "+(Integer)view.getTag());
            	    			String selectedDate = GuideUtils.calDates.get(jourChaine.getSelectedItemPosition());
            					Intent i = new Intent(GuideMenuActivity.this, GuideChaineActivity.class);
            					i.putExtra("DATE", selectedDate);
            					i.putExtra("CHAINE", (Integer)view.getTag());
            			        startActivity(i);
            				}
            			},
            			R.id.ChoixSelectedLinearLayout, -1);
        	}
        	dismissAll();
        }

        /**
         * GuideactivityNetwork
         */
        public GuideMenuActivityNetwork()
        {
        	Log.d(TAG,"GUIDEMENUACTIVITYNETWORK START");
        }
    }

	@Override
	protected boolean getFromDb()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
