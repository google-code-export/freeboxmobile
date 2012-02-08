package org.madprod.freeboxmobile.guide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.ServiceUpdateUIListener;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import static org.madprod.freeboxmobile.StaticConstants.TAG;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
	GoogleAnalyticsTracker tracker;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Guide/HomeGuide");
        Log.i(TAG,"GUIDE MENU CREATE");
        setContentView(R.layout.guide_menu);

        mDbHelper = new ChainesDbAdapter(this);
        mDbHelper.openRead();
    }

	@Override
	protected void onStart()
	{
		super.onStart();

		Log.i(TAG,"GUIDE MENU START");
		FBMNetTask.register(this);

        Button enCours = (Button) findViewById(R.id.ButtonEnCours);
        enCours.setOnClickListener(
            	new View.OnClickListener()
            	{
            		public void onClick(View view)
            		{
            			startActivity(new Intent(GuideMenuActivity.this, GuideNowActivity.class));
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
			        	date += " 21:00:00";
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
		tracker.stop();
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

    	GuideCheck.setActivity(this);
       	GuideCheck.setUpdateListener(
       			new ServiceUpdateUIListener()
    	    	{
    				@Override
    				public void updateUI()
    				{
    					runOnUiThread(
    							new Runnable()
    							{
    								public void run()
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
    							});
    				}
    	    	});
    	if (mDbHelper.getNbFavoris() == 0)
    	{
	    	AlertDialog d = new AlertDialog.Builder(this).create();
			d.setTitle(getString(R.string.app_name)+" - GuideTV");
			d.setIcon(R.drawable.fm_guide_tv);
			d.setMessage(
				"Les données du guide sont mis à jour automatiquement en tâche de fond toutes les 24 heures.\n\n"+
				"Comme il s'agit du premier lancement, les données doivent être téléchargées. Voulez-vous le faire maintenant (opération longue) ?");
			d.setButton(DialogInterface.BUTTON_POSITIVE, "Oui", new DialogInterface.OnClickListener()
				{
					public void onClick(final DialogInterface dialog, int which)
					{
						dialog.dismiss();
		    			GuideCheck.refresh(null);
					}
				});
			d.setButton(DialogInterface.BUTTON_NEGATIVE, "Non", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					GuideMenuActivity.this.finish();
				}
			});
			d.show();
    	}
    }

	@Override
	public void onPause()
	{
		super.onPause();
		GuideCheck.setActivity(null);
	}

	@Override
	protected boolean getFromDb()
	{
		return false;
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, GUIDE_OPTION_REFRESH_WEEK, 0, R.string.guide_option_refresh_all).setIcon(android.R.drawable.ic_menu_rotate);
        menu.add(0, GUIDE_OPTION_REFRESH, 1, R.string.guide_option_refresh_day).setIcon(android.R.drawable.ic_menu_rotate);
        menu.add(0, GUIDE_OPTION_RESET, 2, "RAZ du guide").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    		case GUIDE_OPTION_REFRESH:
            	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        	String selectedDate = sdf.format(new Date());
    			GuideCheck.refresh(selectedDate);
    			return true;
    		case GUIDE_OPTION_REFRESH_WEEK:
    			GuideCheck.refresh(null);
    			return true;
    		case GUIDE_OPTION_RESET:
    			mDbHelper.cleanGuideChaine();
    			GuideCheck.refresh(null);
    			return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
