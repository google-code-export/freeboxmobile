package org.madprod.freeboxmobile.guide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideMenuActivity extends Activity implements GuideConstants
{
	private ArrayList<Favoris> listeFavoris = new ArrayList<Favoris>();
	private ChainesDbAdapter mDbHelper;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FBMNetTask.register(this);
        Log.i(TAG,"GUIDE MENU CREATE");
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        setContentView(R.layout.guide_menu);

        mDbHelper = new ChainesDbAdapter(this);
        mDbHelper.open();
        Log.d(TAG,"Nettoyage des anciens programmes effacés : "+mDbHelper.deleteOldProgs());
        Log.d(TAG,"Nettoyage de l'ancienne historique : "+mDbHelper.deleteOldHisto());

    	if (mDbHelper.getNbChaines() == 0)
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
        getFavoris();
        final Spinner datesSpinner = (Spinner) findViewById(R.id.SpinnerDate);
        final Spinner heuresSpinner = (Spinner) findViewById(R.id.SpinnerHeure);
        Spinner jourChaine = (Spinner) findViewById(R.id.SpinnerJourChaine);
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
	
	// TODO : factoriser avec celui de GuideChoixChainesActivity et mettre dans GuideUtils
	private void getFavoris()
    {
		Log.d(TAG,"getFavoris");

		listeFavoris.clear();

    	Cursor chainesIds = mDbHelper.getChainesProg();
    	startManagingCursor (chainesIds);
        if (chainesIds != null)
		{
			Favoris f;
			if (chainesIds.moveToFirst())
			{
				Cursor chaineCursor;
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
					listeFavoris.add(f);
				} while (chainesIds.moveToNext());
			}
			// Ici on trie pour avoir les listes de programmes dans l'ordre des numéros de chaine
			Collections.sort(listeFavoris);

			// On créé l'horizontal scrollview en haut avec la liste des chaines favorites
			Iterator<Favoris> it = listeFavoris.iterator();
			String filepath;
			Bitmap bmp;
	    	LinearLayout csly = (LinearLayout) findViewById(R.id.ChoixSelectedLinearLayout);
	    	csly.removeAllViews();
	    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	csly.setGravity(Gravity.CENTER);
	    	params.gravity = (Gravity.CENTER);
	    	params.setMargins(5,5,5,5);
			while(it.hasNext())
			{
				LinearLayout il = new LinearLayout(this);
				il.setOrientation(LinearLayout.VERTICAL);
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
		            			Log.d(TAG, "click "+(Integer)view.getTag());
		            			Toast.makeText(GuideMenuActivity.this, "Fonctionnalité non disponible pour l'instant",
		    	    					Toast.LENGTH_SHORT).show();
		            		}
		            	}
		            );
		        il.addView(i);
				TextView t = new TextView(this);
				t.setText(f.name);
				t.setTextSize(8);
				// TODO : Terminer (mettre le nom des chaînes sous le logo)
//				il.addView(t);
				il.setGravity(Gravity.CENTER);
				csly.addView(il);
			}
		}
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
	
    private class GuideMenuActivityNetwork extends AsyncTask<Void, Integer, Integer>
    {
        protected void onPreExecute()
        {
        }

        protected Integer doInBackground(Void... arg0)
        {
        	return new GuideNetwork(GuideMenuActivity.this, null, true, true, 0, false).getData();
        }

        protected void onPostExecute(Integer result)
        {
        	if (result != DATA_NOT_DOWNLOADED)
        	{
        		getFavoris();
        	}
        }

        /**
         * GuideactivityNetwork
         * @param d : datetime début
         * @param chaine : récupérer liste des chaines (+ logos) ?
         * @param prog : récupérer liste des programmes ?
         * @param force : forcer la récupération des programmes (ne pas tenir compte du cache) ?
         * @param refreshactivity : pour rafraichir toute l'activité après un onActivityResult du choix des favoris
         */
        public GuideMenuActivityNetwork()
        {
        	Log.d(TAG,"GUIDEMENUACTIVITYNETWORK START");
        }
    }

}
