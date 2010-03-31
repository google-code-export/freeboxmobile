package org.madprod.freeboxmobile.pvr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.pvr.PvrNetwork;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 
 * $Id$
 *
 */
public class ProgrammationActivity extends Activity implements PvrConstants
{
	static final int MENU_UPDATE_HD = 0;
	static final int MENU_UPDATE_CHAINES = 1;
	static final int MENU_UPDATE_ALL = 2;

	// id utilisé pour les modifs d'enregistrements
	private Integer ide = 0;
	
	// etat de ProgressBarIndeterminateVisibility
	static private boolean pbiv = false;
	
	private boolean fromListe = false;
	private boolean fromGuide = false;
	
	// Id du boitier séléctionné
	private static int mBoitierHD = 0;
	// Nom du boitier sélectionné
	private static String mBoitierHDName = null;
	// Curseur sur les boitiers
	private static Cursor boitiersCursor = null;
	SimpleCursorAdapter boitiersSpinnerAdapter = null;
	
	private boolean plusieursBoitiersHD;
	
	// Curseur sur la liste des chaines
	private static Cursor chainesCursor = null;
	SimpleCursorAdapter chainesSpinnerAdapter = null;
	// id de la chaine selectionnée
	private static Integer mChaineID = 0;

	// Curseur sur les disques
	private static Cursor disquesCursor = null;

	// Curseur sur les services de la chaine selectionnée
	private static Cursor servicesCursor = null;

	private long mRowId = -1;
	static Activity progAct = null;
	private boolean nomEmissionSaisi = false;
	private boolean[] joursChoisis = { false, false, false, false, false, false, false };

	static AlertDialog alertDialog = null;
	TextView nomEmission = null;
	TextView dureeEmission = null;
	Spinner chainesSpinner = null;
	Spinner boitierHDSpinner = null;
	Spinner disqueSpinner = null;

	// identifiant du dernier user à être entré dans l'activité
	public static String lastUser = "";
    private Button buttonRecur, ButtonDateDeb, ButtonTimeDeb, ButtonDateFin, ButtonTimeFin;
    CheckBox lendi, mordi, credi, joudi, dredi, sadi, gromanche;
    public static String progressText = ""; // Text des progressDialog avec bar
    private AsyncTask<Void, Integer, Boolean> progNetwork = null;
    
    private  int choosen_year_deb = 0;
    private  int choosen_month_deb = 0;
    private  int choosen_day_deb = 0;
    private  int choosen_hour_deb = -1;
    private  int choosen_minute_deb = -1;

    private  int choosen_year_fin = 0;
    private  int choosen_month_fin = 0;
    private  int choosen_day_fin = 0;
    private  int choosen_hour_fin = -1;
    private  int choosen_minute_fin = -1;

    private  final int DIALOG_DATE_DEB = 0;
    private  final int DIALOG_TIME_DEB = 1;
    private  final int DIALOG_DATE_FIN = 2;
    private  final int DIALOG_TIME_FIN = 3;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.pvr_programmation2);
        FBMNetTask.register(this);
        Log.i(TAG,"PROGRAMMATIONACTIVITY CREATE");

        resetJours();

        setTitle(getString(R.string.app_name) + " " + getString(R.string.pvrPVR)
        		+ " - "+FBMHttpConnection.getTitle());

    	mRowId = savedInstanceState != null ?
    			savedInstanceState.getLong(EnregistrementsDbAdapter.KEY_ROWID)
    			: 0;
    	
    	Bundle extras = getIntent().getExtras();
    	if (mRowId == 0)
    	{
    		mRowId = extras != null ? extras.getLong(EnregistrementsDbAdapter.KEY_ROWID) : 0;
    	}
		if (mRowId != 0)
			fromListe = true;
    	if (extras != null)
    		fromGuide = extras.getString(ChainesDbAdapter.KEY_PROG_TITLE) != null;
    	
    	Log.d(TAG, "from liste : "+fromListe+" - from guide : "+fromGuide);
        // Mode 24h
        ((TimePicker) findViewById(R.id.pvrPrgHeure)).setIs24HourView(true);
        
        progAct = this;
        
        // Chargement des ressources
        ButtonDateDeb = (Button) findViewById(R.id.ButtonDateDeb);
        ButtonTimeDeb = (Button) findViewById(R.id.ButtonTimeDeb);
        ButtonDateFin = (Button) findViewById(R.id.ButtonDateFin);
        ButtonTimeFin = (Button) findViewById(R.id.ButtonTimeFin);
        buttonRecur = (Button) findViewById(R.id.pvrPrgBtnRecur);
        nomEmission = (TextView) findViewById(R.id.pvrPrgNom);
        dureeEmission = (TextView) findViewById(R.id.pvrPrgDuree);
        chainesSpinner = (Spinner) findViewById(R.id.pvrPrgChaine);
        boitierHDSpinner = (Spinner) findViewById(R.id.pvrPrgBoitier);
        disqueSpinner = (Spinner) findViewById(R.id.pvrPrgDisque);
        
        if (choosen_year_deb == 0)
        {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MINUTE, 5);
	        choosen_year_deb = c.get(Calendar.YEAR);
	        choosen_month_deb = c.get(Calendar.MONTH);
	        choosen_day_deb = c.get(Calendar.DAY_OF_MONTH);
	        choosen_hour_deb = c.get(Calendar.HOUR_OF_DAY);
	        choosen_minute_deb = c.get(Calendar.MINUTE);
        }
        ButtonDateDeb.setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View arg0)
					{
						showDialog(DIALOG_DATE_DEB);
					}
				}
			);
        ButtonTimeDeb.setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View arg0)
					{
						showDialog(DIALOG_TIME_DEB);
					}
				}
			);
        setFin();
        ButtonDateFin.setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View arg0)
					{
						showDialog(DIALOG_DATE_FIN);
					}
				}
			);
        ButtonTimeFin.setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View arg0)
					{
						showDialog(DIALOG_TIME_FIN);
					}
				}
			);

        // Vider le champ quand on le clique
        nomEmission.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean b) {
            	if (b == true) {
            		if (nomEmissionSaisi == false) {
            			nomEmission.setText("");
            			nomEmissionSaisi = true;
            		}
            	}
            }
        });

        dureeEmission.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            public void onFocusChange(View v, boolean b)
            {
            	if (b == false)
            	{
            		setFin();
            		if (getDuree() < 1)
            		{
            			afficherMsgErreur("Vous ne pouvez pas utiliser cette durée !");
            		}
            	}
            }
        });

        // Qualité
        chainesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		chainesCursor.moveToPosition(arg2);
        		mChaineID = chainesCursor.getInt(chainesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_CHAINE_ID));
				remplirSpinner(R.id.pvrPrgQualite);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        });

        // Récurrence
        if (buttonRecur != null) {
	        buttonRecur.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					proposerRecurrence();
				}
	        });
        }

        // Boitier HD
        boitierHDSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				Log.d(TAG, "Boitier HD SPinner "+position+" "+id);
				if (mBoitierHD != position)
				{
					Log.d(TAG, "Boitier HD SPinner into"+position+" "+id);
	        		boitiersCursor.moveToPosition(position);
	        		mBoitierHD = boitiersCursor.getInt(boitiersCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_BOITIER_ID));
	        		mBoitierHDName = boitiersCursor.getString(boitiersCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_BOITIER_NAME));
	        		remplirSpinner(R.id.pvrPrgDisque);
	        		remplirSpinner(R.id.pvrPrgChaine);
					afficherInfosDisque();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        });
    }
    
    @Override
    protected void onStart()
    {
    	super.onStart();
        Log.i(TAG,"PROGRAMMATIONACTIVITY START");

		setProgressBarIndeterminateVisibility(pbiv);
    	if (lastUser.equals(FBMHttpConnection.getIdentifiant()))
    	{
    		preparerActivite();
    	}
    	else
    	{
    		preparerActivite();
    		lastUser = FBMHttpConnection.getIdentifiant();
    		// Si bdd des chaines vide, on propose de la remplir
			if (chainesCursor.getCount()==0)
			{
				SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
	        	// S'il s'agit du premier lancement de cette version, on rafraichi pas mal d'infos
	        	if (!mgr.getString(KEY_SPLASH_PVR, "0").equals(Utils.getFBMVersion(this)))
	        	{
					runProgNetwork(true, true);
	        		Log.d(TAG,"PVR: on rafraichi les chaines");
	        		Editor editor = mgr.edit();
					editor.putString(KEY_SPLASH_PVR, Utils.getFBMVersion(this));
					editor.commit();
	        		afficherMsgErreur("La liste des chaînes est vide, elle va être actualisée dans quelques secondes.");
	        	}
	        	else
	        	{
	        		showPasDeChaine();
	        	}
			}
			else // Sinon on met quand même à jour la liste des disques
			{
				runProgNetwork(false, true);
			}
    	}
    }
    
    private boolean runProgNetwork(final boolean chaines, final boolean disques)
    {
    	if (
    			((progNetwork != null) && (progNetwork.getStatus() == AsyncTask.Status.FINISHED)) ||
    			(progNetwork == null)
    		)
    	{
    		progNetwork = new ProgNetwork(chaines, disques).execute((Void[])null);
    		return true;
    	}
    	else
    	{
	    	alertDialog = new AlertDialog.Builder(this).create();
	    	alertDialog.setTitle("Rafraichissement en cours");
	    	alertDialog.setIcon(R.drawable.icon_fbm_reverse);
	    	alertDialog.setMessage(
				"Un rafraichissement est déjà en cours.\n\n"+
				"Réessayez lorsqu'il sera terminé."
			);
	    	alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Réessayer", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dismissAd();
					runProgNetwork(chaines, disques);
				}
			});
			
	    	alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dismissAd();
					}
				});
	    	alertDialog.show();
    		return false;
    	}
    }
    
    @Override
	protected void onDestroy()
    {
//    	FBMHttpConnection.closeDisplay();
    	FBMNetTask.unregister(this);
    	super.onDestroy();
    }

	@Override
	protected void onResume()
	{
		super.onResume();
		if (alertDialog != null)
		{
			alertDialog.show();
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (alertDialog != null)
		{
			alertDialog.dismiss();
		}
	}
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_UPDATE_HD, 1, "Mettre à jour disques").setIcon(android.R.drawable.ic_menu_rotate);
        menu.add(0, MENU_UPDATE_CHAINES, 2, "Mettre à jour chaînes").setIcon(android.R.drawable.ic_menu_rotate);
        menu.add(0, MENU_UPDATE_CHAINES, 0, "Tout mettre à jour").setIcon(android.R.drawable.ic_menu_rotate);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_UPDATE_HD:
        	runProgNetwork(false, true);
            return true;
        case MENU_UPDATE_CHAINES:
        	runProgNetwork(true, false);
            return true;
        case MENU_UPDATE_ALL:
        	runProgNetwork(true, true);
            return true;
        }
        return false;
    }

	@Override    
    protected Dialog onCreateDialog(int id) 
    {
		Log.d(TAG,"ON CREATE FIN : "+choosen_year_fin+" "+choosen_month_fin+" "+choosen_day_fin+" "+choosen_hour_fin+" "+choosen_minute_fin);

        switch (id) {
            case DIALOG_DATE_DEB:
            	DatePickerDialog dpdd = new DatePickerDialog(this, mDateSetListenerDeb, choosen_year_deb, choosen_month_deb, choosen_day_deb);
            	dpdd.setIcon(R.drawable.fm_magnetoscope);
            	dpdd.setTitle(getString(R.string.pvrDialogDateDebut));
            	return dpdd;
		    case DIALOG_TIME_DEB:
	    		TimePickerDialog tpdd = new TimePickerDialog(this, mTimeSetListenerDeb, choosen_hour_deb, choosen_minute_deb, true);
	    		tpdd.setIcon(R.drawable.fm_magnetoscope);
	    		tpdd.setTitle(getString(R.string.pvrDialogHeureDebut));
		        return tpdd;
            case DIALOG_DATE_FIN: 
            	DatePickerDialog dpdf = new DatePickerDialog(this, mDateSetListenerFin, choosen_year_fin, choosen_month_fin, choosen_day_fin);
            	dpdf.setIcon(R.drawable.fm_magnetoscope);
            	dpdf.setTitle(getString(R.string.pvrDialogDateFin));
            	return dpdf;
		    case DIALOG_TIME_FIN:
	    		TimePickerDialog tpdf = new TimePickerDialog(this, mTimeSetListenerFin, choosen_hour_fin, choosen_minute_fin, true);
	    		tpdf.setIcon(R.drawable.fm_magnetoscope);
	    		tpdf.setTitle(getString(R.string.pvrDialogHeureFin));
		        return tpdf;
		}
        return null;    
    }

	private DatePickerDialog.OnDateSetListener mDateSetListenerDeb =
	    new DatePickerDialog.OnDateSetListener() 
	    {        
	        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
	        {
	        	choosen_year_deb = year;
	        	choosen_month_deb = monthOfYear;
	        	choosen_day_deb = dayOfMonth;        		
        		ButtonDateDeb.setText(makeDate(year, monthOfYear, dayOfMonth));

        		setFin();
	        }
	    };

	private DatePickerDialog.OnDateSetListener mDateSetListenerFin =
	    new DatePickerDialog.OnDateSetListener() 
	    {        
	        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
	        {
	        	choosen_year_fin = year;
	        	choosen_month_fin = monthOfYear;
	        	choosen_day_fin = dayOfMonth;
        		ButtonDateDeb.setText(makeDate(year, monthOfYear, dayOfMonth));
        		
        		setDuree();
	        }
	    };

	private String makeDateForPost(int y, int m, int d)
	{
		String cdate = "";
		
		cdate = d < 10 ? "0" : "";
		cdate += d;
		cdate += "/";
		cdate += m+1 < 10 ? "0" : "";
		cdate += m+1;
		cdate += "/";
		cdate += y;
		return cdate;
	}
	
	private String makeDate(int y, int m, int d)
	{
		String cdate = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(sdf.parse(y+"-"+m+"-"+d));
			c.setFirstDayOfWeek(Calendar.MONDAY);
			cdate = jours[c.get(Calendar.DAY_OF_WEEK)]+" ";
			cdate += d < 10 ? "0" : "";
			cdate += d;
			cdate += " "+mois[c.get(Calendar.MONTH)+1];
		} catch (ParseException e) 
		{
			e.printStackTrace();
			cdate = makeDateForPost(y, m, d);
		}
		return cdate;
	}

	private TimePickerDialog.OnTimeSetListener mTimeSetListenerDeb =
        new TimePickerDialog.OnTimeSetListener() 
        {        
            public void onTimeSet(TimePicker view, int h, int m) 
            {
            	choosen_hour_deb = h;
            	choosen_minute_deb = m;
        		ButtonTimeDeb.setText(makeTime(h,m));
        		setFin();
            }
        };

    private TimePickerDialog.OnTimeSetListener mTimeSetListenerFin =
        new TimePickerDialog.OnTimeSetListener() 
        {        
            public void onTimeSet(TimePicker view, int h, int m) 
            {
            	choosen_hour_fin = h;
            	choosen_minute_fin = m;
        		ButtonTimeFin.setText(makeTime(h,m));
        		setDuree();
            }
        };

    void setFin()
    {
        Calendar c = Calendar.getInstance();
        c.set(choosen_year_deb, choosen_month_deb, choosen_day_deb, choosen_hour_deb, choosen_minute_deb);
        c.add(Calendar.MINUTE, getDuree());
        choosen_year_fin = c.get(Calendar.YEAR);
        choosen_month_fin = c.get(Calendar.MONTH);
        choosen_day_fin = c.get(Calendar.DAY_OF_MONTH);
        choosen_hour_fin = c.get(Calendar.HOUR_OF_DAY);
        choosen_minute_fin = c.get(Calendar.MINUTE);
		ButtonDateFin.setText(makeDate(choosen_year_fin, choosen_month_fin, choosen_day_fin));
		ButtonTimeFin.setText(makeTime(choosen_hour_fin, choosen_minute_fin));
//		Log.d(TAG,"FIN : "+choosen_year_fin+" "+choosen_month_fin+" "+choosen_day_fin+" "+choosen_hour_fin+" "+choosen_minute_fin);
    }

    int getDuree()
    {
    	return Integer.parseInt(dureeEmission.getText().toString());
    }

    void setDuree()
    {
        Calendar cd = Calendar.getInstance();
        cd.set(choosen_year_deb, choosen_month_deb, choosen_day_deb, choosen_hour_deb, choosen_minute_deb);
        Calendar cf = Calendar.getInstance();
        cf.set(choosen_year_fin, choosen_month_fin, choosen_day_fin, choosen_hour_fin, choosen_minute_fin);
		Long duree = (long) ((cf.getTime().getTime() - cd.getTime().getTime())/(1000*60));
    	dureeEmission.setText(duree.toString());
    }

    private String makeTime(int h, int m)
    {
		return (h<10?"0"+h:""+h)+":"+(m<10?"0"+m:""+m);
    }
    
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EnregistrementsDbAdapter.KEY_ROWID, mRowId);
        outState.putString("nomEmission", nomEmission.getText().toString());
        outState.putString("da", dureeEmission.getText().toString());
        outState.putInt("choosen_year_deb", choosen_year_deb);
        outState.putInt("choosen_month_deb", choosen_month_deb);
        outState.putInt("choosen_day_deb", choosen_day_deb);
        outState.putInt("choosen_hour_deb", choosen_hour_deb);
        outState.putInt("choosen_minute_deb", choosen_minute_deb);
        outState.putBoolean("nomEmissionSaisi",nomEmissionSaisi);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
            super.onRestoreInstanceState(savedInstanceState);
            dureeEmission.setText(savedInstanceState.getString("da"));
            nomEmission.setText(savedInstanceState.getString("nomEmission"));
            nomEmissionSaisi = savedInstanceState.getBoolean("nomEmissionSaisi");
            choosen_year_deb = savedInstanceState.getInt("choosen_year_deb");
            choosen_month_deb = savedInstanceState.getInt("choosen_month_deb");
            choosen_day_deb = savedInstanceState.getInt("choosen_day_deb");
            choosen_hour_deb = savedInstanceState.getInt("choosen_hour_deb");
            choosen_minute_deb = savedInstanceState.getInt("choosen_minute_deb");
            
            setFin();
            refreshDateTimeButtons();
    }
    
    class SelectionRecurrenceListener implements OnCheckedChangeListener {
    	int jourId;
    	SelectionRecurrenceListener(int id) {
    		jourId = id;
    	}
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			joursChoisis[jourId] = isChecked;
		}
    }
    
    void resetJours() {
    	for (int i = 0; i < 7; i++) {
    		joursChoisis[i] = false;
    	}
    }
    
    private void proposerRecurrence() {
		final CharSequence[] jours = { getString(R.string.pvrLundi), getString(R.string.pvrMardi),
				getString(R.string.pvrMercredi), getString(R.string.pvrJeudi), getString(R.string.pvrVendredi),
				getString(R.string.pvrSamedi), getString(R.string.pvrDimanche) };

    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Récurrence");
    	alertDialog.setIcon(R.drawable.fm_magnetoscope);
		alertDialog.setMessage(getString(R.string.pvrTxtRecurrenceInfo));
		alertDialog.setButton("Continuer", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dismissAd();
				setTheme(android.R.style.Theme_Black);
				alertDialog = new AlertDialog.Builder(progAct)
					.setMultiChoiceItems(jours,
							null,
							new DialogInterface.OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which, boolean what) {
									joursChoisis[which] = what;						
								}
							})
					.setPositiveButton(getString(R.string.OK),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									progAct.setTheme(android.R.style.Theme_Light);
									dismissAd();
								}
							}) 
		            .setNegativeButton(getString(R.string.Annuler),
		            		new DialogInterface.OnClickListener() { 
		            			public void onClick(DialogInterface dialog, int whichButton) {
									progAct.setTheme(android.R.style.Theme_Light);
		            				resetJours();
		            				dialog.cancel();
									dismissAd();
		            			} 
		            		})
		        	.setTitle(getString(R.string.pvrChoixJours))
		            .setIcon(R.drawable.pvr_date)
		            .create();
				alertDialog.show();
			}
		});
		alertDialog.show();
    }
    
    public void showPasDeChaine()
    {
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("La liste des chaînes du magnétoscope est vide");
    	alertDialog.setIcon(R.drawable.icon_fbm_reverse);
    	alertDialog.setMessage(
			"Ceci est peut être dû à un problème réseau lors du téléchargement.\n"+
			"Vous pouvez rafraichir la liste des chaînes en utilisant le bouton MENU de votre téléphone "+
			"ou en cliquant ci-dessous."
		);
    	alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Rafraichir", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dismissAd();
					runProgNetwork(true, true);
				}
			}); 
    	alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Plus tard", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
    	alertDialog.show();
    }
    
    private static void dismissAd()
    {
    	if (alertDialog != null)
    	{
    		alertDialog.dismiss();
    		alertDialog = null;
    	}
    }

    private void refreshDateTimeButtons()
    {
        ButtonDateDeb.setText(makeDate(choosen_year_deb, choosen_month_deb, choosen_day_deb));
        ButtonTimeDeb.setText(makeTime(choosen_hour_deb,choosen_minute_deb));
        ButtonDateFin.setText(makeDate(choosen_year_fin, choosen_month_fin, choosen_day_fin));
        ButtonTimeFin.setText(makeTime(choosen_hour_fin,choosen_minute_fin));    	
    }
    
    private void preparerActivite()
    {
        // Dans tous les cas on remplit le spinner du boitier (même si on l'affiche pas)
    	// car son init remplit certaines variables nécessaires aux disques (chaque disque dépend d'un boitier)
    	remplirSpinner(R.id.pvrPrgBoitier);
    	// Suppression du layout de sélection si on n'a qu'un boitier HD
		if (!plusieursBoitiersHD)
		{
    		findViewById(R.id.pvrPrgLayoutBoitier).setVisibility(View.GONE);
    	}

		// Remplissage des spinners
    	remplirSpinner(R.id.pvrPrgChaine);
    	remplirSpinner(R.id.pvrPrgDisque);
    	
    	disqueSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				Log.d(TAG,"DISQUE SELECTED : "+parent.getSelectedItem().toString());
				afficherInfosDisque();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
    	});
    	// S'il s'agit d'une modification d'un enregistrement existant, remplir le formulaire
    	if (chainesCursor.getCount() > 0)
    	{
    		if (fromListe)
    			remplirFicheFromEnr();
    		if (fromGuide)
    			remplirFicheFromGuide();
    	}
        setFin();
        refreshDateTimeButtons();

        // Activation d'un listener sur le bouton OK
        final Button button = (Button) findViewById(R.id.pvrPrgBtnOK);
        button.setOnClickListener(new View.OnClickListener()
        {
            class TraiterFormulaireTask extends AsyncTask<Void, Integer, String>
            {
                protected void onPreExecute()
                {
                	FBMNetTask.iProgressShow(
                			getString(R.string.pvrPatientez),
                			getString(fromListe ? R.string.pvrModificationEnCours: R.string.pvrProgrammationEnCours),
                			R.drawable.fm_magnetoscope
                	);
                }
            	
                protected String doInBackground(Void... arg0)
                {
                	String errMsg = doAction();
                	if (errMsg == null)
                	{
                		EnregistrementsNetwork.updateEnregistrementsFromConsole(progAct);
                	}
                	else
                	{
                		Log.d(TAG,"RETOUR CONSOLE ENREGISTREMENT : "+errMsg);
                	}
    	        	return errMsg;
                }
                
                protected void onPostExecute(String errMsg)
                {
                	FBMNetTask.iProgressDialogDismiss();

                    if (errMsg != null)
                    {
						setResult(EnregistrementsActivity.RESULT_PROG_NOK);
                        afficherMsgErreur(errMsg);
	                }
                    else
                    {
						setResult(EnregistrementsActivity.RESULT_PROG_OK);
                    	Toast.makeText(progAct, getString(R.string.pvrModificationsEnregistrees),
                    			Toast.LENGTH_LONG).show();
                    	finish();
                    }
                }
            }
            
            public void onClick(View v)
            {
            	new TraiterFormulaireTask().execute((Void[])null);
            }
            
            /**
             * doAction: traite le formulaire, et envoie la requete à la console de free
             * @return : String le message d'erreur, le cas échéant, null sinon
             */
            private String doAction()
            {
        		List<NameValuePair> postVars = new ArrayList<NameValuePair>();
        		Integer service, duree, where_id;
        		int h, m;
        		String date, emission, heure, minutes;

        		// On refait des query ici, parceque des operations asynchrones peuvent toucher aux
        		// cursor du reste de l'appli
                ChainesDbAdapter db = new ChainesDbAdapter(progAct);
                db.open();
                
        		Log.d (TAG, "DoAction");
        		// Duree, emission, nom
        		duree = Integer.parseInt(dureeEmission.getText().toString());
        		emission = nomEmission.getText().toString();
        		emission = emission.replaceAll("&","-");
        		
        		if (emission.length() == 0)
        		{
        			db.close();
        			return getString(R.string.pvrErreurNomEmission);
        		}

        		// Service
        		Spinner spinnerQualite = (Spinner) findViewById(R.id.pvrPrgQualite);
        		// TODO : créer une requete plutot que de chercher dans le cursor
        		Cursor mServicesCursor = db.fetchServicesChaine(mChaineID, mBoitierHD);
        		mServicesCursor.moveToPosition(spinnerQualite.getSelectedItemPosition());
        		Log.d(TAG,"SERVICE : "+mServicesCursor.getString(mServicesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_SERVICE_DESC)));
        		service = mServicesCursor.getInt(mServicesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_SERVICE_ID));
        		mServicesCursor.close();

        		// Date et heure
   	       		date = makeDateForPost(choosen_year_deb, choosen_month_deb, choosen_day_deb);
        		h = choosen_hour_deb;
        		m = choosen_minute_deb;
        		
        		if (h < 10) {	heure = "0" + h; }
        		else { 			heure = "" + h; }
        		if (m < 10) {	minutes = "0" + m; }
        		else { 			minutes = "" + m; }
        		
        		// Disque
        		int disqueId = disqueSpinner.getSelectedItemPosition();
        		// TODO : Ici, si l'abonné n'a pas de disque dur dans sa Freebox : problème !

        		// TODO : créer une requete plutot que de chercher dans le cursor
        		Cursor mDisquesCursor = db.getListeDisques(mBoitierHD);
       			mDisquesCursor.moveToPosition(disqueId);
       			where_id = mDisquesCursor.getInt(mDisquesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_DISQUE_ID));
       			mDisquesCursor.close();

        		// Creation des variables POST
        		postVars.add(new BasicNameValuePair("chaine", mChaineID.toString()));
        		postVars.add(new BasicNameValuePair("service", service.toString()));
        		postVars.add(new BasicNameValuePair("date", date));
        		postVars.add(new BasicNameValuePair("heure", heure));
        		postVars.add(new BasicNameValuePair("minutes", minutes));
        		postVars.add(new BasicNameValuePair("duree", duree.toString()));
        		postVars.add(new BasicNameValuePair("emission", emission));
        		postVars.add(new BasicNameValuePair("where_id", where_id.toString()));
        		
        		// Récurrence
        		String repeat_a = "";
            	for (int i = 0; i < 7; i++) {
            		if (joursChoisis[i]) {
            			postVars.add(new BasicNameValuePair("period", ""+(i+1)));
            			repeat_a += "1";
            		} else {
            			repeat_a += "0";
            		}
            	}

            	if (fromListe)
            	{
            		postVars.add(new BasicNameValuePair("submit", "MODIFIER+L%27ENREGISTREMENT"));
            		postVars.add(new BasicNameValuePair("ide", ide.toString()));
            	}
            	else
            	{
            		postVars.add(new BasicNameValuePair("submit", "PROGRAMMER L'ENREGISTREMENT"));
            	}

        		// Requete HTTP
        		String url = "https://adsls.free.fr/admin/magneto.pl";
       			postVars.add(new BasicNameValuePair("box", ""+mBoitierHD));
        		String resultat = FBMHttpConnection.getPage(FBMHttpConnection.postAuthRequest(url, postVars, true, true));

        		int erreurPos = resultat.indexOf("erreurs");
        		if (erreurPos > 0)
        		{
        			int debutErr, finErr;
        			String msgErreur;
        			
        			msgErreur = resultat.substring(erreurPos);            			
        			debutErr = msgErreur.indexOf("<span style=\"color: #cc0000\">") + 29;
        			finErr = msgErreur.substring(debutErr).indexOf("<");
        			msgErreur = msgErreur.substring(debutErr, debutErr+finErr);
        			
        			if (msgErreur.indexOf("Erreur interne 1") >= 0)
        			{
        				msgErreur += "\n" + getString(R.string.pvrErreurInterne1);
        			}
        			db.close();
        			return getString(R.string.pvrErreurConsole) + "\n" + msgErreur;
        		}
        		else
        		{
        			// On enregistre le total de minutes d'enregistrement effectués avec l'application
        			// plus tard on pourra l'afficher à l'utilisateur
        			SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        			int total = mgr.getInt(KEY_TOTAL_ENR, 0);
        			total += duree;
        			Editor editor = mgr.edit();
    				editor.putInt(KEY_TOTAL_ENR, total);
    				editor.commit();
        		}
    			db.close();
        		return null;
            }
        });
    }
	
	void afficherMsgErreur(String msg)
	{	
		if (progAct != null)
		{
	    	alertDialog = new AlertDialog.Builder(progAct).create();
	    	alertDialog.setTitle("Erreur !");
	    	alertDialog.setIcon(R.drawable.fm_magnetoscope);
	    	alertDialog.setMessage(msg);
	    	alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dismissAd();
				}
			});
			alertDialog.show();
		}
    }
    
	private boolean remplirFicheFromGuide()
	{
		Bundle bundle = getIntent().getExtras();	        
        if (bundle == null)
        {
        	return false;
        }
        if (bundle.getString(ChainesDbAdapter.KEY_PROG_TITLE) != null)
        {
			nomEmission.setText(bundle.getString(ChainesDbAdapter.KEY_PROG_TITLE));
			nomEmissionSaisi = true;
			chainesCursor.moveToFirst();
			int chaine = bundle.getInt(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL);
			do
			{
				if (chainesCursor.getInt(chainesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_CHAINE_ID)) == chaine)
				{
					break;
				}
			} while (chainesCursor.moveToNext());
	        chainesSpinner.setSelection(chainesCursor.getPosition());
	        
	        String datetimedeb = bundle.getString(ChainesDbAdapter.KEY_PROG_DATETIME_DEB);
	        String datetimefin = bundle.getString(ChainesDbAdapter.KEY_PROG_DATETIME_FIN);
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        try
	        {
				Date dtdeb = sdf.parse(datetimedeb);
				boolean immediat = false;
		        if (dtdeb.before(new Date()))
		        {
		        	dtdeb = new Date();
		        	immediat = true;
		        }
		        choosen_year_deb = dtdeb.getYear() + 1900;
		        choosen_month_deb = dtdeb.getMonth();
		        choosen_day_deb = dtdeb.getDate();
		        choosen_hour_deb = dtdeb.getHours();
		        choosen_minute_deb = dtdeb.getMinutes();
		        if (immediat)
		        {
		            Calendar c = Calendar.getInstance();
		            c.set(choosen_year_deb, choosen_month_deb, choosen_day_deb, choosen_hour_deb, choosen_minute_deb);
		            c.add(Calendar.MINUTE, 2);
		            choosen_year_deb = c.get(Calendar.YEAR);
		            choosen_month_deb = c.get(Calendar.MONTH);
		            choosen_day_deb = c.get(Calendar.DAY_OF_MONTH);
		            choosen_hour_deb = c.get(Calendar.HOUR_OF_DAY);
		            choosen_minute_deb = c.get(Calendar.MINUTE);
		        }
			}
	        catch (ParseException e)
	        {
	        	Log.e(TAG,"Problème conversion de date deb ! "+e.getMessage());
				e.printStackTrace();
				showErreurDate("fin");
			}
	        try
	        {
				Date dtfin = sdf.parse(datetimefin);
		        choosen_year_fin = dtfin.getYear() + 1900;
		        choosen_month_fin = dtfin.getMonth();
		        choosen_day_fin = dtfin.getDate();
		        choosen_hour_fin = dtfin.getHours();
		        choosen_minute_fin = dtfin.getMinutes();
			}
	        catch (ParseException e)
	        {
	        	Log.e(TAG,"Problème conversion de date fin ! "+e.getMessage());
				e.printStackTrace();
				showErreurDate("début");
			}
	        setDuree();
	        refreshDateTimeButtons();
	        afficherInfosDisque();
	        fromGuide = false;
	        return true;
        }
        return false;
	}

	private void showErreurDate(String q)
	{
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Problème");
    	alertDialog.setIcon(R.drawable.icon_fbm_reverse);
    	alertDialog.setMessage(
			"Une erreur est survenue lors de la récupération de la date.\n\n"+
			"Veuillez vérifier la date de "+q+" de votre enregistrement."
		);
    	alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dismissAd();
				}
			});
    	alertDialog.show();
	}
	
	/**
	 * S'il s'agit d'une modification, remplir le formulaire
	 * @returnCursor l'enregistrement si c'est une modification d'un enregistrement existant
	 * null sinon
	 */
    private boolean remplirFicheFromEnr()
    {
		// Est-ce un ajout ou une modification d'un enregistrement ?
		Bundle bundle = getIntent().getExtras();	        
        if (bundle == null)
        {
        	return false;
        }

        if (mRowId >= 0)
        {
	        // Récupération des infos concernant cet enregistrement
	        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
	        db.open();
	        Cursor c = db.fetchEnregistrement(mRowId);
	        startManagingCursor(c);
	        db.close();
	        
	        // Y'a qqn ?
	        if (c.getCount() <= 0 || c.moveToFirst() == false)
	        {
	        	return false;
	        }
	        
	        // Views
//	        DatePicker date = (DatePicker) findViewById(R.id.pvrPrgDate);
//	        TimePicker heure = (TimePicker) findViewById(R.id.pvrPrgHeure);
	        
	        // Remplissage
			chainesCursor.moveToFirst();
			int chaine = c.getInt(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE_ID));//bundle.getInt(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL);
			do
			{
				if (chainesCursor.getInt(chainesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_CHAINE_ID)) == chaine)
				{
					break;
				}
			} while (chainesCursor.moveToNext());
	        chainesSpinner.setSelection(chainesCursor.getPosition());

//	        chainesSpinner.setSelection(c.getInt(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE_ID)));
	        disqueSpinner.setSelection(getDisqueSpinnerId(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_WHERE_ID))));
	        
	        String strDate = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE));
	        int day = Integer.parseInt(strDate.substring(0,2));
	        int month = Integer.parseInt(strDate.substring(3, 5));
	        month -= 1; //month = 0..11
	        int year = Integer.parseInt(strDate.substring(6));
	        //date.updateDate(year, month-1, day);//month = 0..11
	        
	        String strHeure = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_HEURE));
//	        heure.setCurrentHour(Integer.parseInt(strHeure.substring(0,2)));
//	        heure.setCurrentMinute(Integer.parseInt(strHeure.substring(3)));
	        
	        choosen_year_deb = year;
	        choosen_month_deb = month;
	        choosen_day_deb = day;
	        choosen_hour_deb = Integer.parseInt(strHeure.substring(0,2));
	        choosen_minute_deb = Integer.parseInt(strHeure.substring(3));

	        dureeEmission.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DUREE)));
	        nomEmission.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_NOM)));
	        nomEmissionSaisi = true;
	        int disqueId = Integer.parseInt(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_WHERE_ID)));
	        disqueSpinner.setSelection(disqueId);
	        ide = c.getInt(c.getColumnIndex(EnregistrementsDbAdapter.KEY_IDE));
	        refreshDateTimeButtons();
	        afficherInfosDisque();
	        return true;
        }
        
        afficherInfosDisque();
        return false;
    }

    private int getGiga(int size)
    {
    	return size / 1048576;
    }
    
    private void afficherInfosDisque()
    {
    	if ((disquesCursor != null) && (disquesCursor.getCount() > 0))
    	{
	    	int disqueId = disqueSpinner.getSelectedItemPosition();
	    	// Au démarrage de l'activity, rien n'est encore selectionné
	    	if (disqueId < 0)
	    		disqueId = 0;
			disquesCursor.moveToPosition(disqueId);
	    	int t1 = disquesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_DISQUE_ID);
			disqueId = disquesCursor.getInt(t1);
	    	ChainesDbAdapter db = new ChainesDbAdapter(this);
	    	db.open();
	//    	Log.d(TAG,"afficherInfosDisque : "+disqueId + " "+mBoitierHDName);
	    	Cursor c = db.fetchDisque(disqueId, mBoitierHDName);
	    	startManagingCursor(c);
	        if (c.moveToFirst())
	        {
	        	int gigaFree = getGiga(c.getInt(c.getColumnIndex(ChainesDbAdapter.KEY_DISQUE_FREE_SIZE)));
	        	int gigaTotal = getGiga(c.getInt(c.getColumnIndex(ChainesDbAdapter.KEY_DISQUE_TOTAL_SIZE)));
	
	        	((TextView) findViewById(R.id.pvrPrgInfosDisque1))
	    			.setText(getString(R.string.pvrInfosDisque1).replace("#nom", c.getString(c.getColumnIndex(ChainesDbAdapter.KEY_DISQUE_LABEL))));
	        	((TextView) findViewById(R.id.pvrPrgInfosDisque2))
	    			.setText(getString(R.string.pvrInfosDisque2).replace("#libre", ""+gigaFree));
	        	((TextView) findViewById(R.id.pvrPrgInfosDisque3))
	    			.setText(getString(R.string.pvrInfosDisque3).replace("#total", ""+gigaTotal));
              	((TextView) findViewById(R.id.pvrPrgInfosDisque4))
        			.setText(getString(R.string.pvrInfosDisque4_2).replace("#mount", c.getString(c.getColumnIndex(ChainesDbAdapter.KEY_DISQUE_MOUNT))));
	        	if (gigaFree < 4) {
	        		((TextView) findViewById(R.id.pvrPrgInfosDisqueEspaceFaible))
	        			.setText(getString(R.string.pvrInfosDisqueEspaceFaible));
	        	}
	        	else
	        	{
	        		((TextView) findViewById(R.id.pvrPrgInfosDisqueEspaceFaible))
	    			.setText("");        		
	        	}
	
	        	ProgressBar pb = (ProgressBar) findViewById(R.id.pvrPrgDisquePB);
	        	pb.setMax(gigaTotal);
	        	pb.setProgress(gigaTotal-gigaFree);
	        }
	        db.close();
    	}
    }
    
    private void remplirSpinner(int id) {
		Spinner spinner = (Spinner) findViewById(id);
		List<String> liste = new ArrayList<String>();
		
		ChainesDbAdapter db = new ChainesDbAdapter(this);
		db.open();
		
		int selection = spinner.getSelectedItemPosition();
		switch (id) {
			case R.id.pvrPrgChaine:
				chainesCursor = db.fetchAllChaines(mBoitierHD);
				if (chainesCursor != null)
				{
					startManagingCursor(chainesCursor);
					chainesCursor.moveToFirst();
					String [] chaines = new String [] {ChainesDbAdapter.KEY_CHAINE_NAME};
					int [] to = new int[] {android.R.id.text1};
					chainesSpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, chainesCursor, chaines, to);
					chainesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinner.setAdapter(chainesSpinnerAdapter);
					// TODO : Modifier pour ajouter le numéro de chaîne + le logo
				}
				break;
				
			case R.id.pvrPrgDisque:
//				disquesCursor = db.getListeDisques(mBoitierHDName);
				disquesCursor = db.getListeDisques(mBoitierHD);
		        if (disquesCursor.moveToFirst())
		        {
					startManagingCursor(disquesCursor);
		            int disqueNameIndex = disquesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_DISQUE_LABEL);
		            int disqueFreeIndex = disquesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_DISQUE_FREE_SIZE);
		            int disqueTotalIndex = disquesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_DISQUE_TOTAL_SIZE);
					String disqueName;
		        	do
		        	{
		        		disqueName = disquesCursor.getString(disqueNameIndex);
		        		disqueName += " (" + getGiga(disquesCursor.getInt(disqueFreeIndex)) + "/" + getGiga(disquesCursor.getInt(disqueTotalIndex));
						disqueName += " " + getString(R.string.pvrGoLibres) + ")";
		        		liste.add(disqueName);
		        	}
		       		while (disquesCursor.moveToNext());
		        }
				afficherInfosDisque();
				break;

			case R.id.pvrPrgQualite:
				servicesCursor = db.fetchServicesChaine(mChaineID, mBoitierHD);
				if (servicesCursor.moveToFirst())
				{
					startManagingCursor(servicesCursor);
					String serviceName;
					int serviceDescIndex = servicesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_SERVICE_DESC);
					int servicePvrModeIndex = servicesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PVR_MODE);
					do
					{
						if (mChaineID == 0)
						{
							liste.add(getString(R.string.pvrNonEnregistrable));
						}
						else
						{
							serviceName = servicesCursor.getString(serviceDescIndex);
							if (serviceName.length() == 0) {
								serviceName = getString(R.string.pvrTxtQualiteParDefaut);
							}
							if (servicesCursor.getInt(servicePvrModeIndex) != PVR_MODE_PUBLIC) {
								serviceName += " *";
							}
							liste.add(serviceName);
						}						
					} while (servicesCursor.moveToNext());
				}
			break;
			
			case R.id.pvrPrgBoitier:
				int i = 0;
				boitiersCursor = db.fetchBoitiers();
		        if (boitiersCursor.moveToFirst())
		        {
		        	startManagingCursor(boitiersCursor);
		            int boitierNameIndex = boitiersCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_BOITIER_NAME);
					mBoitierHD = boitiersCursor.getInt(boitiersCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_BOITIER_ID));
					mBoitierHDName = boitiersCursor.getString(boitierNameIndex);
		        	do
		        	{
		        		liste.add(boitiersCursor.getString(boitierNameIndex));
		        		i++;
		        	}
		       		while (boitiersCursor.moveToNext());
		        	plusieursBoitiersHD = (i > 1);
		        	Log.d(TAG,"PLUSIEURS BOITIERS HD : "+i);
		        }
			break;
		}
		
		if ((id != R.id.pvrPrgChaine))
		{
			ArrayAdapter<String> adapter= new ArrayAdapter<String>(
					this, android.R.layout.simple_spinner_item, liste);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
		}
		if (selection != -1)
		{
			spinner.setSelection(selection, true);
		}
		db.close();
    }
    
	private int getDisqueSpinnerId(String disque) {
		int i = 0;
        if (disquesCursor.moveToFirst())
        {
            int disqueNameIndex = disquesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_DISQUE_LABEL);
        	do
        	{
        		if (disquesCursor.getString(disqueNameIndex).equals(disque))
        			return i;
        		i++;
        	}
       		while (disquesCursor.moveToNext());
        }
		return -1;
	}
	
	private class ProgNetwork extends AsyncTask<Void, Integer, Boolean>
	{
		private boolean getChaines;
		private boolean getDisques;

        protected void onPreExecute()
        {
        		setProgressBarIndeterminateVisibility(true);
        		pbiv = true;
        }

        protected Boolean doInBackground(Void... arg0)
        {
        	return new PvrNetwork(getChaines, getDisques).getData();
        }

        protected void onPostExecute(Boolean telechargementOk)
        {
        	preparerActivite();
        	if (telechargementOk == Boolean.TRUE)
        	{
        	}
        	else
        	{
    			afficherMsgErreur(progAct.getString(R.string.pvrErreurTelechargementDonnees));
        	}
        	// progAct important ici / en cas de screenrotation, il est essentiel
        	// (sinon le setProgressBar se fait sur l'ancienne activity que connait l'asynctask) 
    		progAct.setProgressBarIndeterminateVisibility(false);
    		pbiv = false;
        }
        
        public ProgNetwork(boolean getChaines, boolean getDisques)
        {
        	Log.d(TAG,"ProgNetwork START "+getChaines + " "+getDisques);
        	this.getChaines = getChaines;
        	this.getDisques = getDisques;
        }
	}
}