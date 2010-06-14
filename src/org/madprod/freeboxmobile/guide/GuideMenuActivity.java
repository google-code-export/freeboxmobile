package org.madprod.freeboxmobile.guide;

import java.util.Calendar;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;
import org.madprod.freeboxmobile.pvr.PvrNetwork;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

public class GuideMenuActivity extends Activity implements GuideConstants
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
    }
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		Log.i(TAG,"GUIDE MENU START");
		FBMNetTask.register(this);

		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);

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
        GuideUtils.makeCalDates();
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

	   	setTitle(getString(R.string.app_name)+" Menu Guide TV - "+FBMHttpConnection.getTitle());
		if (!mgr.getString(KEY_SPLASH_GUIDE, "0").equals(Utils.getFBMVersion(this)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_GUIDE, Utils.getFBMVersion(this));
			editor.commit();
			displayAboutGuide();
		}
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
    	GuideUtils.displayFavoris(this, 
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

	private void displayAboutGuide()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name)+" - GuideTV");
		d.setIcon(R.drawable.fm_guide_tv);
		d.setMessage(
			"Le guide est en version beta.\n\n"+
			"Ses fonctionnalités, son look et ses performances seront améliorés "+
			"dans les prochaines semaines.\n\n"+
			"Mais il est déjà pratique comme cela :)"
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
            	GuideUtils.displayFavoris(GuideMenuActivity.this, 
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
}
