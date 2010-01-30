package org.madprod.freeboxmobile.pvr;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.pvr.Chaine.Service.PVR_MODE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
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
 *
 */
public class ProgrammationActivity extends Activity {
	private List<Chaine> mChaines = null;
	private List<Disque> mDisques = null;
	private long mRowId = -1;
	Activity progAct = null;
	final String TAG = "FreeboxMobileProg";
	private boolean nomEmissionSaisi = false;
	private boolean[] joursChoisis = { false, false, false, false, false, false, false };
	ProgressDialog progressDialog = null;
	TextView nomEmission = null;
	Spinner chainesSpinner = null;
	Spinner dureeSpinner = null;
	
	boolean orentationPortrait = false;
	int positionEcran = 0;
	int nbEcrans = 3;
    private Button suivant, precedent, boutonOK, buttonRecur;
	private GestureDetector gestureDetector;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
    private Animation slideRightOut;
    CheckBox lendi, mordi, credi, joudi, dredi, sadi, gromanche;
    ViewFlipper viewFlipper;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pvr_programmation);
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
        boutonOK = (Button) findViewById(R.id.pvrPrgBtnOK);
        suivant = (Button) findViewById(R.id.pvrPrgBtnSuivant);
        precedent = (Button) findViewById(R.id.pvrPrgBtnPrecedent);
        buttonRecur = (Button) findViewById(R.id.pvrPrgBtnRecur);
        nomEmission = (TextView) findViewById(R.id.pvrPrgNom);
        chainesSpinner = (Spinner) findViewById(R.id.pvrPrgChaine);
        dureeSpinner = (Spinner) findViewById(R.id.pvrPrgDurees);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        lendi = ((CheckBox) findViewById(R.id.pvrPrgJour0));
        mordi = ((CheckBox) findViewById(R.id.pvrPrgJour1));
        credi = ((CheckBox) findViewById(R.id.pvrPrgJour2));
        joudi = ((CheckBox) findViewById(R.id.pvrPrgJour3));
        dredi = ((CheckBox) findViewById(R.id.pvrPrgJour4));
        sadi = ((CheckBox) findViewById(R.id.pvrPrgJour5));
        gromanche = ((CheckBox) findViewById(R.id.pvrPrgJour6));
        viewFlipper = (ViewFlipper) findViewById(R.id.pvrFlipper);
        
        dureeSpinner.setSelection(8);// 8 == 2H
        
        orentationPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        
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
        
        // Qualité
        chainesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				remplirSpinner(R.id.pvrPrgQualite, (Spinner) arg0);
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
        if (orentationPortrait) {
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
        
        new TelechargerChainesDisquesTask().execute((Void[])null);
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
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (orentationPortrait) {
        	return gestureDetector.onTouchEvent(event);
        }
	    return false;
    }
	
	@Override
	protected void onPause() {
		super.onPause();
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		// TODO: enregistrer l'état du formulaire qqpart pour
		// le récupérer quand on revient sur l'activité
	}
    
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EnregistrementsDbAdapter.KEY_ROWID, mRowId);
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

		setTheme(android.R.style.Theme_Black);
		AlertDialog alert = new AlertDialog.Builder(this)
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
						}
					}) 
            .setNegativeButton(getString(R.string.Annuler),
            		new DialogInterface.OnClickListener() { 
            			public void onClick(DialogInterface dialog, int whichButton) {
							progAct.setTheme(android.R.style.Theme_Light);
            				resetJours();
            				dialog.cancel();
            			} 
            		})
        	.setTitle(getString(R.string.pvrChoixJours))
            .setIcon(R.drawable.pvr_date)
            .create();
		alert.show();
    }
    
	/**
	 * télécharge la liste des chaines et disques
	 * @author bduffez
	 *
	 */
    class TelechargerChainesDisquesTask extends AsyncTask<Void, Integer, Boolean> {
        protected void onPreExecute() {
        	progressDialog = ProgressDialog.show(progAct, getString(R.string.pvrPatientez),
        			getString(R.string.pvrTelechargementChaines), true, false);
        }
    	
        protected Boolean doInBackground(Void... arg0) {
        	return telechargerEtParser();
        }
        
        protected void onPostExecute(Boolean telechargementOk) {
        	if (telechargementOk == Boolean.TRUE) {
        		preparerActivite();
        	}
        	else {
        		afficherMsgErreur(getString(R.string.pvrErreurTelechargementChaines));
        		boutonOK.setEnabled(false);
        	}
            
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    
    /**
     * 
     * @return true en cas de succès, false sinon
     */
    private boolean telechargerEtParser() {        
        // Récupérer chaines et disques durs        
        String url = "http://adsl.free.fr/admin/magneto.pl";
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("detail","1"));
    	
        String resultat = PvrUtils.getPage(FBMHttpConnection.getAuthRequest(url, param, true, true));
//        FBMHttpConnection.FBMLog("telechargerEtParser : "+resultat);
        if (resultat != null) {
			FBMHttpConnection.FBMLog("telechargerEtParser not null");
	        int posChaines = resultat.indexOf("var serv_a = [{");
	        int posDisques = resultat.indexOf("var disk_a = [{");
	        
	        if (posChaines > 0 && posDisques > 0) {
	    		FBMHttpConnection.FBMLog("telechargerEtParser posChaines > 0 && posDisques > 0");
	        	// Récupération du javascript correspondant à la liste des chaines
	        	String strChaines = resultat.substring(posChaines+14, posDisques);
	        	int finChaines = strChaines.lastIndexOf("}");
	        	strChaines = strChaines.substring(0, finChaines+1);
	        	
	        	// Récupération du javascript correspondant à la liste des disques durs
	        	String strDisques = resultat.substring(posDisques+14);
	        	int fin = strDisques.lastIndexOf("}];")+1;
	        	strDisques = strDisques.substring(0, fin);
	        	
	        	// Conversion JSON -> objet dans la RAM
	        	getListeChaines(strChaines);
	        	getListeDisques(strDisques);
	        	
	        	return true;
	        }
        }
		FBMHttpConnection.FBMLog("telechargerEtParser null");
        FBMHttpConnection.FBMLog("Impossible de télécharger le json des chaines/disques");
    	Log.d(TAG, "Impossible de télécharger le json des chaines/disques");
    	return false;
    }
    
    private void preparerActivite() {
		// Remplissage des spinners
    	remplirSpinner(R.id.pvrPrgChaine);
    	remplirSpinner(R.id.pvrPrgDisque);
		
    	// S'il s'agit d'une modification, remplir le formulaire
    	final Cursor enr = remplirFiche();
    	
    	// Durées avec le spinner
    	final Spinner s = (Spinner) findViewById(R.id.pvrPrgDurees);
    	s.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String duree = (String) getResources().getTextArray(R.array.pvrValeursDurees)[position];
				((TextView) findViewById(R.id.pvrPrgDuree)).setText(duree);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
    	});
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.pvrListeDurees,
        		android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

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
                	progressDialog = ProgressDialog.show(progAct, getString(R.string.pvrPatientez),
                			getString(enr == null ? R.string.pvrProgrammationEnCours : R.string.pvrModificationEnCours),
                			true, false);
                }
            	
                protected String doInBackground(Void... arg0) {
    	        	return doAction();
                }
                
                protected void onPostExecute(String errMsg) {                    
                    progressDialog.dismiss();
                    progressDialog = null;

                    if (errMsg != null) {
						setResult(EnregistrementsActivity.RESULT_PROG_NOK);
                        afficherMsgErreur(errMsg);
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
        		Integer chaine, service, duree, where_id, ide = 0;
        		int h, m;
        		String date, emission, heure, minutes, nomChaine;
        		
        		// Duree, emission, nom
        		duree = Integer.parseInt((((TextView) findViewById(R.id.pvrPrgDuree)).getText().toString()));
        		emission = ((TextView) findViewById(R.id.pvrPrgNom)).getText().toString();
        		
        		if (emission.length() == 0) {
        			return getString(R.string.pvrErreurNomEmission);
        		}
        		
        		// Chaine
        		Spinner spinnerChaines = (Spinner) findViewById(R.id.pvrPrgChaine);
        		int chaineId = spinnerChaines.getSelectedItemPosition();
        		chaine = mChaines.get(chaineId).getChaineId();
        		nomChaine = mChaines.get(chaineId).getName();
        		
        		if (enr != null) {
        			ide = enr.getInt(enr.getColumnIndex(EnregistrementsDbAdapter.KEY_IDE));
        		}
        		
        		// Service
        		Spinner spinnerQualite = (Spinner) findViewById(R.id.pvrPrgQualite);
        		int serviceId = spinnerQualite.getSelectedItemPosition();
        		service = mChaines.get(chaineId).getServices().get(serviceId).getServiceId();
        		
        		// Date
        		DatePicker datePicker = (DatePicker) findViewById(R.id.pvrPrgDate);
        		date  = datePicker.getDayOfMonth() < 10 ? "0" : "";
        		date += datePicker.getDayOfMonth();
        		date += "/";
        		date += datePicker.getMonth()+1 < 10 ? "0" : "";
        		date += datePicker.getMonth()+1;
        		date += "/";
        		date += datePicker.getYear();
        		
        		// Heure minutes
        		TimePicker timePicker = (TimePicker) findViewById(R.id.pvrPrgHeure);
        		h = timePicker.getCurrentHour();
        		m = timePicker.getCurrentMinute();
        		if (h < 10) {	heure = "0" + h; }
        		else { 			heure = "" + h; }
        		if (m < 10) {	minutes = "0" + m; }
        		else { 			minutes = "" + m; }
    
        		// Disque
        		int disqueId = ((Spinner) findViewById(R.id.pvrPrgDisque)).getSelectedItemPosition();
        		where_id = mDisques.get(disqueId).getId();

        		// Creation des variables POST
        		postVars.add(new BasicNameValuePair("chaine", chaine.toString()));
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
            		postVars.add(new BasicNameValuePair("submit", "PROGRAMMER+L%27ENREGISTREMENT"));
            	}

        		// Requete HTTP
        		String url = "http://adsl.free.fr/admin/magneto.pl";
        		String resultat = PvrUtils.getPage(FBMHttpConnection.postAuthRequest(url, postVars, true, true));

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
        			
        			return getString(R.string.pvrErreurConsole) + "\n" + msgErreur;
        		}
        		// Pas d'erreur, on MAJ la db
        		else {
    				EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(progAct);
    				db.open();

    				// Modification
        			if (enr != null) {
        				int rowId = enr.getInt(enr.getColumnIndex(EnregistrementsDbAdapter.KEY_ROWID));
        				db.modifyEnregistrement(rowId, nomChaine, date, heure+"h"+minutes, duree.toString(),
        			    		emission, ide.toString(), chaine.toString(), service.toString(), heure, minutes,
        			    		duree.toString(), emission, where_id.toString(), repeat_a);
        			}
        			// Ajout
        			else {
        				db.createEnregistrement(nomChaine, date, heure+"h"+minutes, duree.toString(),
        			    		emission, ide.toString(), chaine.toString(), service.toString(), heure, minutes,
        			    		duree.toString(), emission, where_id.toString(), repeat_a);
        			}
        			
    				db.close();
        		}
        		
        		return null;
            }
        });
    }
	
	private void afficherMsgErreur(String msg) {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("Erreur!");
		d.setMessage(msg);
		d.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		d.show();
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
	        Spinner chaines = (Spinner) findViewById(R.id.pvrPrgChaine);
	        DatePicker date = (DatePicker) findViewById(R.id.pvrPrgDate);
	        TimePicker heure = (TimePicker) findViewById(R.id.pvrPrgHeure);
	        EditText duree = (EditText) findViewById(R.id.pvrPrgDuree);
	        EditText nom = (EditText) findViewById(R.id.pvrPrgNom);
	        Spinner disques = (Spinner) findViewById(R.id.pvrPrgDisque);
	        
	        // Remplissage
	        chaines.setSelection(getChaineSpinnerId(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE))));
	        
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
	        disques.setSelection(disqueId);
	        return c;
        }
        
        return null;
    }
    
    private void remplirSpinner(int id) {
    	remplirSpinner(id, null);
    }
    private void remplirSpinner(int id, Spinner chaineSpinner) {
		Spinner spinner = (Spinner) findViewById(id);
		List<String> liste = new ArrayList<String>();
		int i, size;
		
		// Construction de la liste de String à mettre dans le spinner
		if (id == R.id.pvrPrgChaine) {
			size = mChaines.size();
			for (i = 0; i < size; i++) {
				liste.add(mChaines.get(i).getName());
			}
		} else if (id == R.id.pvrPrgDisque) {
			size = mDisques.size();
			String disqueName;
			Disque disque;
			for (i = 0; i < size; i++) {
				disque = mDisques.get(i);
				disqueName = disque.getLabel();
				disqueName += " (" + disque.getGigaFree() + "/" + disque.getGigaTotal();
				disqueName += " " + getString(R.string.pvrGioLibres) + ")";
				liste.add(disqueName);
			}
		} else { // R.id.pvrPrgQualite
			int idChaine = chaineSpinner.getSelectedItemPosition();
			List<Chaine.Service> services = mChaines.get(idChaine).getServices();
			size = services.size();
			String serviceName;
			for (i = 0; i < size; i++) {
				if (i == 0 && idChaine == 0) {
					liste.add(getString(R.string.pvrNonEnregistrable));
				} else {
					serviceName = services.get(i).getDesc();
					if (serviceName.length() == 0) {
						serviceName = getString(R.string.pvrTxtQualiteParDefaut);
					}
					if (services.get(i).getPvrMode() != PVR_MODE.PUBLIC) {
						serviceName += " *";
					}
					liste.add(serviceName);
				}
			}
		}
		
		ArrayAdapter<String> adapter= new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, liste);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
    }
	
    // Fonctions pour récupérer la position d'une chaine/disque dans un spinner
	private int getChaineSpinnerId(String chaine) {
		int i, size = mChaines.size();

		for (i = 0; i < size; i++) {
			if (mChaines.get(i).getName().equals(chaine)) {
				return i;
			}
		}
		
		return -1;
	}
	private int getDisqueSpinnerId(String disque) {
		int i, size = mDisques.size();
		
		for (i = 0; i < size; i++) {
			if (mDisques.get(i).getLabel().equals(disque)) {
				return i;
			}
		}
		
		return -1;
	}
    
	// Fonctions pour parser le javascript listant les chaines et les disques durs
	private List<Chaine> getListeChaines(String strChaines) {
		getListe(strChaines, "}]},{\"name\"", 3, true);
		return mChaines;
	}
	private List<Disque> getListeDisques(String strDisques) {
		getListe(strDisques, "}", 1, false);
		return mDisques;
	}
	
	/**
	 * Crée la liste des chaines ou des disques (selon le boolean)
	 * @param strSource:	l'array de JSON (commence par { sinon crash)
	 * @param sep:			ce qui sépare deux objets JSON
	 * @param shift:		le nombre d'octets inutiles entre deux objets JSON
	 * @param isChaines:	true si c'est la liste des chaines, false si c'est celle des disques
	 */
	private void getListe(String strSource, String sep, int shift, boolean isChaines) {
		String str;
		int pos;
		
		// Init
		if (isChaines) {
			mChaines = new ArrayList<Chaine>();
		} else  {
			mDisques = new ArrayList<Disque>();
		}
		
		// Loop
		do {
			// pos contient la position de la fin du string JSON
			pos = strSource.indexOf(sep) + shift;
			if (pos <= 10) {
				pos = strSource.lastIndexOf("}") + 1;
				
				if (pos <= 10) {
					break;
				}
			}
			
			// Récupération du string JSON
			str = strSource.substring(0, pos);
			
			// Ajout à la liste
			if (isChaines) {
				mChaines.add(new Chaine(str));
			} else {
				mDisques.add(new Disque(str));
			}
			
			// Préparation du prochain item JSON
			if (strSource.length() > pos+1) {
				strSource = strSource.substring(pos+1);
			}
			else {
				break;
			}
		} while(true);
    }
}