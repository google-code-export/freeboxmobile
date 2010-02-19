package org.madprod.freeboxmobile.pvr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.pvr.PvrNetwork;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 
 * @author bduffez
 * $Id$
 *
 */
public class ProgrammationActivity extends Activity implements PvrConstants {
	static final int MENU_UPDATE_HD = 0;
	static final int MENU_UPDATE_CHAINES = 1;
	static final int MENU_UPDATE_ALL = 2;

	// Id du boitier séléctionné
	private static int mBoitierHD = 0;
	// Nom du boitier sélectionné
	private static String mBoitierHDName = null;
	// Curseur sur les boitiers
	private static Cursor boitiersCursor = null;
	SimpleCursorAdapter boitiersSpinnerAdapter = null;
	private static boolean plusieursBoitiersHD;
	
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
	Activity progAct = null;
	private boolean nomEmissionSaisi = false;
	private boolean[] joursChoisis = { false, false, false, false, false, false, false };
	static ProgressDialog progressDialog = null;
	static AlertDialog alertDialog = null;
	TextView nomEmission = null;
	TextView dureeEmission = null;
	Spinner chainesSpinner = null;
	Spinner dureeSpinner = null;
	Spinner boitierHDSpinner = null;
	Spinner disqueSpinner = null;

	private final int LAYOUT_BENOIT = 1;
	private final int LAYOUT_OLIVIER = 2;
	private int selectedLayout = LAYOUT_OLIVIER;

	// identifiant du dernier user à être entré dans l'activité
	public static String lastUser = "";
	boolean orientationPortrait = false;
	int positionEcran = 0;
	int nbEcrans = 3;
    private Button suivant, precedent, boutonOK, buttonRecur, ButtonDateDeb, ButtonTimeDeb, ButtonDateFin, ButtonTimeFin;
	private GestureDetector gestureDetector;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
    private Animation slideRightOut;
    CheckBox lendi, mordi, credi, joudi, dredi, sadi, gromanche;
    ViewFlipper viewFlipper;
    public static String progressText = ""; // Text des progressDialog avec bar
    private AsyncTask<Void, Integer, Boolean> progNetwork = null;
    
    /*
    private static int choosen_year_deb = 0;
    private static int choosen_month_deb = 0;
    private static int choosen_day_deb = 0;
    private static int choosen_hour_deb = -1;
    private static int choosen_minute_deb = -1;

    private static int choosen_year_fin = 0;
    private static int choosen_month_fin = 0;
    private static int choosen_day_fin = 0;
    private static int choosen_hour_fin = -1;
    private static int choosen_minute_fin = -1;
*/
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.pvr_programmation2);
        FBMHttpConnection.initVars(this, null);
        resetJours();

        setTitle(getString(R.string.app_name) + " " + getString(R.string.pvrPVR)
        		+ " - "+FBMHttpConnection.getTitle());

    	mRowId = savedInstanceState != null ?
    			savedInstanceState.getLong(EnregistrementsDbAdapter.KEY_ROWID)
    			: -1;
    			
    	if (mRowId < 0) {
    		Bundle extras = getIntent().getExtras();
    		mRowId = extras != null ? extras.getLong(EnregistrementsDbAdapter.KEY_ROWID) : -1;
    	}
        
        // Mode 24h
        ((TimePicker) findViewById(R.id.pvrPrgHeure)).setIs24HourView(true);
        
        progAct = this;
        
        // Chargement des ressources
        ButtonDateDeb = (Button) findViewById(R.id.ButtonDateDeb);
        ButtonTimeDeb = (Button) findViewById(R.id.ButtonTimeDeb);
        ButtonDateFin = (Button) findViewById(R.id.ButtonDateFin);
        ButtonTimeFin = (Button) findViewById(R.id.ButtonTimeFin);
        boutonOK = (Button) findViewById(R.id.pvrPrgBtnOK);
        suivant = (Button) findViewById(R.id.pvrPrgBtnSuivant);
        precedent = (Button) findViewById(R.id.pvrPrgBtnPrecedent);
        buttonRecur = (Button) findViewById(R.id.pvrPrgBtnRecur);
        nomEmission = (TextView) findViewById(R.id.pvrPrgNom);
        dureeEmission = (TextView) findViewById(R.id.pvrPrgDuree);
        chainesSpinner = (Spinner) findViewById(R.id.pvrPrgChaine);
        dureeSpinner = (Spinner) findViewById(R.id.pvrPrgDurees);
        boitierHDSpinner = (Spinner) findViewById(R.id.pvrPrgBoitier);
        disqueSpinner = (Spinner) findViewById(R.id.pvrPrgDisque);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        lendi = (CheckBox) findViewById(R.id.pvrPrgJour0);
        mordi = (CheckBox) findViewById(R.id.pvrPrgJour1);
        credi = (CheckBox) findViewById(R.id.pvrPrgJour2);
        joudi = (CheckBox) findViewById(R.id.pvrPrgJour3);
        dredi = (CheckBox) findViewById(R.id.pvrPrgJour4);
        sadi = (CheckBox) findViewById(R.id.pvrPrgJour5);
        gromanche = (CheckBox) findViewById(R.id.pvrPrgJour6);
        viewFlipper = (ViewFlipper) findViewById(R.id.pvrFlipper);
        
        
        if (selectedLayout == LAYOUT_OLIVIER) {
        	orientationPortrait = false;
        }
        else {
        	orientationPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        }
        
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

        dureeEmission.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean b) {
            	if (b == false) {
            		setFin();
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
        
        // Flipper
        if (orientationPortrait && selectedLayout == LAYOUT_BENOIT) {
            // Le choix du boitier HD est masqué par défaut, on fera showPrevious si on détecte 2 boitiers HD
    		viewFlipper.showNext();
    		
    		// Listeners sur les boutons
	        suivant.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					swipeRight();
				}
	        });
	        precedent.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					swipeLeft();
				}
	        });
	        gestureDetector = new GestureDetector(new MyGestureDetector());

	        // Callbacks pour gérer la récurrence en mode portrait
	        lendi.setOnCheckedChangeListener(new SelectionRecurrenceListener(0));
	        mordi.setOnCheckedChangeListener(new SelectionRecurrenceListener(1));
	        credi.setOnCheckedChangeListener(new SelectionRecurrenceListener(2));
	        joudi.setOnCheckedChangeListener(new SelectionRecurrenceListener(3));
	        dredi.setOnCheckedChangeListener(new SelectionRecurrenceListener(4));
	        sadi.setOnCheckedChangeListener(new SelectionRecurrenceListener(5));
	        gromanche.setOnCheckedChangeListener(new SelectionRecurrenceListener(6));
        }
        
        // Boitier HD
        boitierHDSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (mBoitierHD != position) {
	        		boitiersCursor.moveToPosition(position);
	        		mBoitierHD = boitiersCursor.getInt(boitiersCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_BOITIER_ID));
	        		mBoitierHDName = boitiersCursor.getString(boitiersCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_BOITIER_NAME));
//	        		FBMHttpConnection.FBMLog("BOITIER : "+mBoitierHD+" "+mBoitierHDName);
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
    protected void onStart() {
    	super.onStart();
    	
    	if (lastUser.equals(FBMHttpConnection.getIdentifiant())) {
    		preparerActivite();
    	}
    	else {
    		preparerActivite();
    		lastUser = FBMHttpConnection.getIdentifiant();
    		// Si bdd des chaines vide, on propose de la remplir
			if (chainesCursor.getCount()==0)
			{
				showPasDeChaine();
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
	protected void onDestroy() {
    	super.onDestroy();
    	FBMHttpConnection.closeDisplay();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (orientationPortrait) {
        	return gestureDetector.onTouchEvent(event);
        }
	    return false;
    }
	
	@Override
	protected void onResume() {
		super.onResume();

		if (progressDialog != null) {
			progressDialog.show();
		}
		if (alertDialog != null) {
			alertDialog.show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
    	
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		if (alertDialog != null) {
			alertDialog.dismiss();
		}
		// TODO: enregistrer l'état du formulaire qqpart pour
		// le récupérer quand on revient sur l'activité
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
//        	new ProgNetwork(false, true).execute((Void[])null);
            return true;
        case MENU_UPDATE_CHAINES:
        	runProgNetwork(true, false);
//        	new ProgNetwork(true, false).execute((Void[])null);
            return true;
        case MENU_UPDATE_ALL:
        	runProgNetwork(true, true);
//        	new ProgNetwork(true, true).execute((Void[])null);
            return true;
        }
        return false;
    }

	@Override    
    protected Dialog onCreateDialog(int id) 
    {
		FBMHttpConnection.FBMLog("ON CREATE FIN : "+choosen_year_fin+" "+choosen_month_fin+" "+choosen_day_fin+" "+choosen_hour_fin+" "+choosen_minute_fin);

        switch (id) {
            case DIALOG_DATE_DEB: 
            	DatePickerDialog dpdd = new DatePickerDialog(this, mDateSetListenerDeb, choosen_year_deb, choosen_month_deb, choosen_day_deb);
            	dpdd.setIcon(R.drawable.fm_magnetoscope);
            	// TODO : Migrer dans strings.xml
            	dpdd.setTitle("Date de début de l'enregistrement");
            	return dpdd;
		    case DIALOG_TIME_DEB:
	    		TimePickerDialog tpdd = new TimePickerDialog(this, mTimeSetListenerDeb, choosen_hour_deb, choosen_minute_deb, true);
	    		tpdd.setIcon(R.drawable.fm_magnetoscope);
            	// TODO : Migrer dans strings.xml
	    		tpdd.setTitle("Heure de début de l'enregistrement");
		        return tpdd;
            case DIALOG_DATE_FIN: 
            	DatePickerDialog dpdf = new DatePickerDialog(this, mDateSetListenerFin, choosen_year_fin, choosen_month_fin, choosen_day_fin);
            	dpdf.setIcon(R.drawable.fm_magnetoscope);
            	// TODO : Migrer dans strings.xml
            	dpdf.setTitle("Date de fin de l'enregistrement");
            	return dpdf;
		    case DIALOG_TIME_FIN:
	    		TimePickerDialog tpdf = new TimePickerDialog(this, mTimeSetListenerFin, choosen_hour_fin, choosen_minute_fin, true);
	    		tpdf.setIcon(R.drawable.fm_magnetoscope);
            	// TODO : Migrer dans strings.xml
	    		tpdf.setTitle("Heure de fin de l'enregistrement");
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

	private String makeDate(int y, int m, int d)
	{
		String cdate;
		
		cdate  = d < 10 ? "0" : "";
		cdate += d;
		cdate += "/";
		cdate += m+1 < 10 ? "0" : "";
		cdate += m+1;
		cdate += "/";
		cdate += y;
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
//		FBMHttpConnection.FBMLog("FIN : "+choosen_year_fin+" "+choosen_month_fin+" "+choosen_day_fin+" "+choosen_hour_fin+" "+choosen_minute_fin);
    }

    int getDuree()
    {
    	return Integer.decode(dureeEmission.getText().toString());
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
    
    private void swipeLeft() {
		positionEcran--;
		suivant.setVisibility(View.VISIBLE);
		if (positionEcran == -1) {
			positionEcran = 0;
		} else {
			if (positionEcran == 0) {
				precedent.setVisibility(View.INVISIBLE);
			}
			viewFlipper.setInAnimation(slideRightIn);
			viewFlipper.setOutAnimation(slideRightOut);
			viewFlipper.showPrevious();
		}
    }
    private void swipeRight() {
		positionEcran++;
		precedent.setVisibility(View.VISIBLE);
		if (positionEcran > nbEcrans) {
			positionEcran = nbEcrans;
		} else {
			if (positionEcran == nbEcrans) {
				suivant.setVisibility(View.INVISIBLE);
				boutonOK.setVisibility(View.VISIBLE);
			}
			viewFlipper.setInAnimation(slideLeftIn);
			viewFlipper.setOutAnimation(slideLeftOut);
			viewFlipper.showNext();
		}
    }
    
    class MyGestureDetector extends SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
    	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	swipeRight();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	swipeLeft();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
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
    	alertDialog.setTitle("La liste des chaînes est vide");
    	alertDialog.setIcon(R.drawable.icon_fbm_reverse);
    	alertDialog.setMessage(
			"La liste des chaînes est vide.\n"+
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
//		        	new ProgNetwork(true, true).execute((Void[])null);
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

    public static void showProgress(Activity a, int progress)
    {
    	if (progressDialog == null)
    	{
    		progressDialog = new ProgressDialog(a);
    		progressDialog.setIcon(R.drawable.fm_magnetoscope);
    		progressDialog.setTitle("Importation");
    		progressDialog.setMessage(progressText);
    		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    		progressDialog.show();
        }
        progressDialog.setProgress(progress);
    }

    public static void setPdMax(int max)
    {
    	FBMHttpConnection.FBMLog("setPdMax "+max);
    	progressDialog.setMax(max);
    }

    public static void showPatientezChaines(Activity a)
    {
		progressDialog = new ProgressDialog(a);
		progressDialog.setIcon(R.drawable.fm_magnetoscope);
		progressDialog.setTitle(a.getString(R.string.pvrPatientez));
		progressDialog.setMessage(a.getString(R.string.pvrTelechargementChaines));
		progressDialog.setCancelable(false);
		progressDialog.show();
    }

    public static void showPatientezDonnees(Activity a)
    {
		progressDialog = new ProgressDialog(a);
		progressDialog.setIcon(R.drawable.fm_magnetoscope);
		progressDialog.setTitle(a.getString(R.string.pvrPatientez));
		progressDialog.setMessage(a.getString(R.string.pvrTelechargementDonnees));
		progressDialog.show();
    }

    private static void dismissAd()
    {
    	if (alertDialog != null)
    	{
    		alertDialog.dismiss();
    		alertDialog = null;
    	}
    }

    public static void dismissPd()
    {
    	if (progressDialog != null)
    	{
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    }

    private void refreshDateTimeButtons()
    {
        ButtonDateDeb.setText(makeDate(choosen_year_deb, choosen_month_deb, choosen_day_deb));
        ButtonTimeDeb.setText(makeTime(choosen_hour_deb,choosen_minute_deb));
        ButtonDateFin.setText(makeDate(choosen_year_fin, choosen_month_fin, choosen_day_fin));
        ButtonTimeFin.setText(makeTime(choosen_hour_fin,choosen_minute_fin));    	
    }
    
    private void preparerActivite() {
    	refreshDateTimeButtons();
    	
        // Dans tous les cas on remplit le spinner du boitier (même si on l'affiche pas)
    	// car son init remplit certaines variables nécessaires aux disques (chaque disque dépend d'un boitier)
    	remplirSpinner(R.id.pvrPrgBoitier);
    	// Suppression du layout de sélection si on n'a qu'un boitier HD
		if (plusieursBoitiersHD) {
    		if ((orientationPortrait) && (selectedLayout == LAYOUT_BENOIT)) {
        		viewFlipper.showPrevious();
        	}
    	}
    	else if (orientationPortrait == false) {
    		findViewById(R.id.pvrPrgLayoutBoitier).setVisibility(View.GONE);
    	}

		// Remplissage des spinners
    	remplirSpinner(R.id.pvrPrgChaine);
    	remplirSpinner(R.id.pvrPrgDisque);
    	
    	disqueSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				afficherInfosDisque();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
    	});
		
    	// S'il s'agit d'une modification, remplir le formulaire
    	final Cursor enr = remplirFiche();
    	
    	String dureeInput = dureeEmission.getText().toString();
    	// Durées avec le spinner
    	dureeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				
				String duree = (String) getResources().getTextArray(R.array.pvrValeursDurees)[position];
				dureeEmission.setText(duree);
				setFin();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
    	});
    	int lastPos = dureeSpinner.getSelectedItemPosition();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.pvrListeDurees,
        		android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dureeSpinner.setAdapter(adapter);
        if (lastPos != -1)
        {
        	dureeSpinner.setSelection(lastPos, true);
        }
        else
        {
            dureeSpinner.setSelection(8, true);
        }
        dureeEmission.setText(dureeInput);
        setFin();
        // Activation d'un listener sur le bouton OK
        final Button button = (Button) findViewById(R.id.pvrPrgBtnOK);
        button.setOnClickListener(new View.OnClickListener() {
        	/**
        	 * traite le formulaire
        	 * @author bduffez
        	 *
        	 */
            class TraiterFormulaireTask extends AsyncTask<Void, Integer, String> {
            	ProgressDialog progressDialog = null;

                protected void onPreExecute() {
            		progressDialog = new ProgressDialog(progAct);
            		progressDialog.setIcon(R.drawable.fm_magnetoscope);
            		progressDialog.setTitle(getString(R.string.pvrPatientez));
            		progressDialog.setMessage(getString(enr == null ? R.string.pvrProgrammationEnCours : R.string.pvrModificationEnCours));
            		progressDialog.show();
                }
            	
                protected String doInBackground(Void... arg0) {
                	String res = doAction();
                	if (res == null)
                	{
                		EnregistrementsNetwork.updateEnregistrementsFromConsole(progAct);
                	}
    	        	return res;
                }
                
                protected void onPostExecute(String errMsg) {
                    progressDialog.dismiss();
                    progressDialog = null;

                    if (errMsg != null) {
						setResult(EnregistrementsActivity.RESULT_PROG_NOK);
                        afficherMsgErreur(errMsg, progAct);
	                }
                    else {
						setResult(EnregistrementsActivity.RESULT_PROG_OK);
                    	Toast.makeText(progAct, getString(R.string.pvrModificationsEnregistrees),
                    			Toast.LENGTH_SHORT).show();
                    	
                    	finish();
                    }
        			
        			EnregistrementsActivity.enrAct.updaterEnregistrements(false);
                }
            }
            
            public void onClick(View v) {
            	new TraiterFormulaireTask().execute((Void[])null);
            }
            
            /**
             * doAction: traite le formulaire, et envoie la requete à la console de free
             * @return : String le message d'erreur, le cas échéant, null sinon
             */
            private String doAction() {
        		List<NameValuePair> postVars = new ArrayList<NameValuePair>();
        		Integer service, duree, where_id, ide = 0;
        		int h, m;
        		String date, emission, heure, minutes;
        		
        		ChainesDbAdapter db = new ChainesDbAdapter(progAct);
        		db.open();

        		// Duree, emission, nom
        		duree = Integer.parseInt(dureeEmission.getText().toString());
        		emission = ((TextView) findViewById(R.id.pvrPrgNom)).getText().toString();
        		
        		if (emission.length() == 0) {
        			return getString(R.string.pvrErreurNomEmission);
        		}

        		if (enr != null) {
        			ide = enr.getInt(enr.getColumnIndex(EnregistrementsDbAdapter.KEY_IDE));
        		}
        		
        		// Service
        		Spinner spinnerQualite = (Spinner) findViewById(R.id.pvrPrgQualite);
        		servicesCursor.moveToPosition(spinnerQualite.getSelectedItemPosition());
        		FBMHttpConnection.FBMLog("SERVICE : "+servicesCursor.getString(servicesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_SERVICE_DESC)));
        		service = servicesCursor.getInt(servicesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_SERVICE_ID));
        		
        		// Date et heure
       	       	if (selectedLayout == LAYOUT_BENOIT) {
	        		DatePicker datePicker = (DatePicker) findViewById(R.id.pvrPrgDate);
	        		date = makeDate(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

	        		TimePicker timePicker = (TimePicker) findViewById(R.id.pvrPrgHeure);
	        		h = timePicker.getCurrentHour();
	        		m = timePicker.getCurrentMinute();
     	       	}
       	       	else {
       	       		date = makeDate(choosen_year_deb, choosen_month_deb, choosen_day_deb);
            		h = choosen_hour_deb;
            		m = choosen_minute_deb;
       	       	}
        		
        		if (h < 10) {	heure = "0" + h; }
        		else { 			heure = "" + h; }
        		if (m < 10) {	minutes = "0" + m; }
        		else { 			minutes = "" + m; }
        		
        		// Disque
        		int disqueId = disqueSpinner.getSelectedItemPosition();
        		disquesCursor.moveToPosition(disqueId);
        		where_id = disquesCursor.getInt(disquesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_DISQUE_ID));

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
        		
            	// Post vars pour modification:
            	//chaine=12&service=0&date=10%2F01%2F2010&heure=23&minutes=09
            	//&duree=185&emission=pouet&where_id=0&ide=12&submit=MODIFIER+L%27ENREGISTREMENT
            	if (enr != null) {
            		postVars.add(new BasicNameValuePair("submit", "MODIFIER+L%27ENREGISTREMENT"));
            		postVars.add(new BasicNameValuePair("ide", ide.toString()));
            	}
            	//pour un ajout:
            	// chaine=7&service=0&date=07%2F01%2F2010&heure=12&minutes=01
            	//&duree=134&emission=pouet&where_id=0&submit=PROGRAMMER+L%27ENREGISTREMENT

            	else {
            		postVars.add(new BasicNameValuePair("submit", "PROGRAMMER L'ENREGISTREMENT"));
            	}

        		// Requete HTTP
        		String url = "http://adsl.free.fr/admin/magneto.pl";
        		if (plusieursBoitiersHD)
        			postVars.add(new BasicNameValuePair("box", ""+mBoitierHD));
        		FBMHttpConnection.FBMLog("Programmation sur le serveur de Free");
        		String resultat = FBMHttpConnection.getPage(FBMHttpConnection.postAuthRequest(url, postVars, true, true));

        		int erreurPos = resultat.indexOf("erreurs");
        		if (erreurPos > 0) {
        			int debutErr, finErr;
        			String msgErreur;
        			
        			msgErreur = resultat.substring(erreurPos);            			
        			debutErr = msgErreur.indexOf("<span style=\"color: #cc0000\">") + 29;
        			finErr = msgErreur.substring(debutErr).indexOf("<");
        			msgErreur = msgErreur.substring(debutErr, debutErr+finErr);
        			
        			if (msgErreur.indexOf("Erreur interne 1") >= 0) {
        				msgErreur += "\n" + getString(R.string.pvrErreurInterne1);
        			}
        			db.close();
        			return getString(R.string.pvrErreurConsole) + "\n" + msgErreur;
        		}
        		db.close();
        		return null;
            }
        });
    }
	
	static void afficherMsgErreur(String msg, Activity a) {	
    	alertDialog = new AlertDialog.Builder(a).create();
    	alertDialog.setTitle("Erreur!");
    	alertDialog.setIcon(R.drawable.fm_magnetoscope);
    	alertDialog.setMessage(msg);
    	alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
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
    private Cursor remplirFiche() {
		// Est-ce un ajout ou une modification d'un enregistrement ?
		Bundle bundle = getIntent().getExtras();	        
        if (bundle == null) {
        	return null;
        }
        
        //TODO: faire le remplissage si on vient de l'onglet grille des programmes
        // pour l'instant ça ne remplit la fiche que d'un enregistrement venant
        // de sqlite
        
        if (mRowId >= 0) {
	        // Récupération des infos concernant cet enregistrement
	        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
	        db.open();
	         Cursor c = db.fetchEnregistrement(mRowId);
	        db.close();
	        
	        // Y'a qqn ?
	        if (c.getCount() <= 0 || c.moveToFirst() == false) {
	        	return null;
	        }
	        
	        // Views
	        // TODO : ces widgets sont déjà en variable de classe
	        Spinner chaines = (Spinner) findViewById(R.id.pvrPrgChaine);
	        DatePicker date = (DatePicker) findViewById(R.id.pvrPrgDate);
	        TimePicker heure = (TimePicker) findViewById(R.id.pvrPrgHeure);
	        EditText duree = (EditText) findViewById(R.id.pvrPrgDuree);
	        EditText nom = (EditText) findViewById(R.id.pvrPrgNom);
	        
	        // Remplissage
//	        chaines.setSelection(getChaineSpinnerId(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE))));
	        chaines.setSelection(c.getInt(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE_ID)));
	        disqueSpinner.setSelection(getDisqueSpinnerId(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_WHERE_ID))));
	        
	        String strDate = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE));
	        int day = Integer.parseInt(strDate.substring(0,2));
	        int month = Integer.parseInt(strDate.substring(3, 5));
	        int year = Integer.parseInt(strDate.substring(6));
	        date.updateDate(year, month-1, day);//month = 0..11
	        
	        String strHeure = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_HEURE));
	        heure.setCurrentHour(Integer.parseInt(strHeure.substring(0,2)));
	        heure.setCurrentMinute(Integer.parseInt(strHeure.substring(3)));
	        
	        duree.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DUREE)));
	        nom.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_NOM)));

	        int disqueId = Integer.parseInt(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_WHERE_ID)));
	        disqueSpinner.setSelection(disqueId);
	        afficherInfosDisque();
	        return c;
        }
        
        afficherInfosDisque();
        
        return null;
    }

    private int getGiga(int size)
    {
    	return size / 1048576;
    }
    
    private void afficherInfosDisque() {
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
	//    	FBMHttpConnection.FBMLog("afficherInfosDisque : "+disqueId + " "+mBoitierHDName);
	    	Cursor c = db.fetchDisque(disqueId, mBoitierHDName);
	    	startManagingCursor(c);
	        if (c.moveToFirst()) {
	        	int gigaFree = getGiga(c.getInt(c.getColumnIndex(ChainesDbAdapter.KEY_DISQUE_FREE_SIZE)));
	        	int gigaTotal = getGiga(c.getInt(c.getColumnIndex(ChainesDbAdapter.KEY_DISQUE_TOTAL_SIZE)));
	
	        	((TextView) findViewById(R.id.pvrPrgInfosDisque1))
	    			.setText(getString(R.string.pvrInfosDisque1).replace("#nom", c.getString(c.getColumnIndex(ChainesDbAdapter.KEY_DISQUE_LABEL))));
	        	((TextView) findViewById(R.id.pvrPrgInfosDisque2))
	    			.setText(getString(R.string.pvrInfosDisque2).replace("#libre", ""+gigaFree));
	        	((TextView) findViewById(R.id.pvrPrgInfosDisque3))
	    			.setText(getString(R.string.pvrInfosDisque3).replace("#total", ""+gigaTotal));
	        	switch (selectedLayout)	{
	        		case LAYOUT_BENOIT:
	                	((TextView) findViewById(R.id.pvrPrgInfosDisque4))
	        			.setText(getString(R.string.pvrInfosDisque4).replace("#mount", c.getString(c.getColumnIndex(ChainesDbAdapter.KEY_DISQUE_MOUNT))));
	        		break;
	        		case LAYOUT_OLIVIER:
	                	((TextView) findViewById(R.id.pvrPrgInfosDisque4))
	        			.setText(getString(R.string.pvrInfosDisque4_2).replace("#mount", c.getString(c.getColumnIndex(ChainesDbAdapter.KEY_DISQUE_MOUNT))));
	                break;
	        	}
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
					String [] chaines = new String [] {ChainesDbAdapter.KEY_NAME};
					int [] to = new int[] {android.R.id.text1};
					chainesSpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, chainesCursor, chaines, to);
					chainesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinner.setAdapter(chainesSpinnerAdapter);
					// TODO : Modifier pour ajouter le numéro de chaîne + le logo
				}
				break;
				
			case R.id.pvrPrgDisque:
				disquesCursor = db.getListeDisques(mBoitierHDName);
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
	
    // Fonctions pour récupérer la position d'une chaine/disque dans un spinner
/*	private int getChaineSpinnerId_old(String chaine) {
		int i = 0;

		if (chainesCursor.moveToFirst())
		{
            int chaineNameIndex = chainesCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_NAME);
        	do
        	{
        		if (chainesCursor.getString(chaineNameIndex).equals(chaine))
        			return i;
        		i++;
        	}
       		while (chainesCursor.moveToNext());
        }
		return -1;
	}
*/
    
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
//    		if (!getChaines)
        		setProgressBarIndeterminateVisibility(true);
//        		else
//        			showPatientezDonnees(progAct);
        }

        protected Boolean doInBackground(Void... arg0) {
        	return new PvrNetwork(progAct, getChaines, getDisques).getData();
        }

        protected void onProgressUpdate(Integer... progress)
        {
            showProgress(progAct, progress[0]);
        }	        

        protected void onPostExecute(Boolean telechargementOk) {
        	if (telechargementOk == Boolean.TRUE) {
        	}
        	else {
    			afficherMsgErreur(progAct.getString(R.string.pvrErreurTelechargementDonnees), progAct);
        	}
        	preparerActivite();
        	dismissPd();
    		setProgressBarIndeterminateVisibility(false);
        }
        
        public ProgNetwork(boolean getChaines, boolean getDisques)
        {
        	FBMHttpConnection.FBMLog("ProgNetwork START "+getChaines + " "+getDisques);
        	this.getChaines = getChaines;
        	this.getDisques = getDisques;
        }
	}
}