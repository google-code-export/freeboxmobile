package org.madprod.freeboxmobile.pvr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import org.madprod.freeboxmobile.HttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;


public class ProgrammationActivity extends Activity {
	private List<Chaine> mChaines = null;
	private List<Disque> mDisques = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.programmation);
        
        // R�cup�rer chaines et disques durs        
        String url = "http://adsl.free.fr/admin/magneto.pl?id=";
    	url += HttpConnection.getId()+"&idt="+HttpConnection.getIdt();
    	url += "&detail=1";
    	
        String resultat = HttpConnection.getPage(HttpConnection.getRequest(url, true));
        int posChaines = resultat.indexOf("var serv_a = [{");
        int posDisques = resultat.indexOf("var disk_a = [{");
        
        if (posChaines > 0 && posDisques > 0) {
        	// R�cup�ration du javascript correspondant � la liste des chaines
        	String strChaines = resultat.substring(posChaines+14, posDisques);
        	int finChaines = strChaines.lastIndexOf("}");
        	strChaines = strChaines.substring(0, finChaines+1);
        	
        	// R�cup�ration du javascript correspondant � la liste des disques durs
        	String strDisques = resultat.substring(posDisques+14);
        	int fin = strDisques.indexOf("}")+1;
        	strDisques = strDisques.substring(0, fin);
        	
        	// Conversion JSON -> objet dans la RAM
        	getListeChaines(strChaines);
        	getListeDisques(strDisques);

			// Remplissage des spinners
        	remplirSpinner(R.id.pvrPrgChaine);
        	remplirSpinner(R.id.pvrPrgDisque);
			
        	// S'il s'agit d'une modification, remplir le formulaire
        	final Cursor enr = remplirFiche();

            // Activation d'un listener sur le bouton OK
            final Button button = (Button) findViewById(R.id.PrgBtnOK);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
            		List<NameValuePair> postVars = new ArrayList<NameValuePair>();
            		Integer chaine, service, duree, where_id, ide = 0;
            		int h, m;
            		String date, emission, heure, minutes;
            		
            		// Chaine
            		Spinner spinnerChaines = (Spinner) findViewById(R.id.pvrPrgChaine);
            		int chaineId = spinnerChaines.getSelectedItemPosition();
            		chaine = mChaines.get(chaineId).getChaineId();
            		if (enr != null) {
            			ide = enr.getInt(enr.getColumnIndex(EnregistrementsDbAdapter.KEY_IDE));
            		}
            		
            		// Service
            		//TODO!
            		service = 0;
            		
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
            		
            		// Duree, emission, nom
            		duree = Integer.parseInt((((TextView) findViewById(R.id.pvrPrgDuree)).getText().toString()));
            		emission = ((TextView) findViewById(R.id.pvrPrgNom)).getText().toString();
        
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
            		
            		//TODO: urlencode(emission)
            		
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
                	HttpConnection.connectFree();
            		String url = "http://adsl.free.fr/admin/magneto.pl?id=";
            		url += HttpConnection.getId()+"&idt="+HttpConnection.getIdt();
            		HttpConnection.postRequest(url, postVars, true);
                }
            });
        }
        else {
        	Log.d("FreeboxVCR", "Impossible de t�l�charger le json");
        }
    }
    
	// S'il s'agit d'une modification, remplir le formulaire
    //@return Cursor l'enregistrement si c'est une modification d'un enregistrement existant
    // 				 null sinon
    private Cursor remplirFiche() {
		// Est-ce un ajout ou une modification d'un enregistrement ?
		Bundle bundle = getIntent().getExtras();	        
        if (bundle == null) {
        	return null;
        }
        
        //TODO: faire le remplissage si on vient de l'onglet grille des programmes
        // pour l'instant �a ne remplit la fiche que d'un enregistrement venant
        // de sqlite
        
        long idEnregistrement = bundle.getLong(EnregistrementsDbAdapter.KEY_ROWID);
        if (idEnregistrement > 0) {
	        // R�cup�ration des infos concernant cet enregistrement
	        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
	        db.open();
	         Cursor c = db.fetchEnregistrement(idEnregistrement);
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
		Spinner spinner = (Spinner) findViewById(id);
		List<String> liste = new ArrayList<String>();
		int i, size;
		
		// Construction de la liste de String � mettre dans le spinner
		if (id == R.id.pvrPrgChaine) {
			size = mChaines.size();
			for (i = 0; i < size; i++) {
				liste.add(mChaines.get(i).getName());
			}
		} else {
			size = mDisques.size();
			for (i = 0; i < size; i++) {
				liste.add(mDisques.get(i).getMountPt());
			}
		}
		ArrayAdapter<String> adapter= new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, liste);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
    }
	
    // Fonctions pour r�cup�rer la position d'une chaine/disque dans un spinner
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
			if (mDisques.get(i).getMountPt().equals(disque)) {
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
	//@param String: l'array de JSON (commence par { sinon crash)
	//@param String: ce qui s�pare deux objets JSON
	//@param int: le nombre d'octets inutiles entre deux objets JSON
	//@param boolean: true si c'est la liste des chaines, false si c'est celle des disques
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
			
			// R�cup�ration du string JSON
			str = strSource.substring(0, pos);
			
			// Ajout � la liste
			if (isChaines) {
				mChaines.add(new Chaine(str));
			} else {
				mDisques.add(new Disque(str));
			}
			
			// Pr�paration du prochain item JSON
			if (strSource.length() > pos+1) {
				strSource = strSource.substring(pos+1);
			}
			else {
				break;
			}
		} while(true);
    }
}