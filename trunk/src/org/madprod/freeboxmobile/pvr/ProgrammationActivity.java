package org.madprod.freeboxmobile.pvr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import android.widget.TimePicker;


public class ProgrammationActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.programmation);
        
        // Récupérer chaines et disques durs
        /*HTTP_Request req = new HTTP_Request(EnregistrementsActivity.getUrlConsole()+"&detail=1");
        req.go();
        
        String resultat = req.getContents();*/
        
        String url = "http://adsl.free.fr/admin/magneto.pl?id=";
    	url += HttpConnection.getId()+"&idt="+HttpConnection.getIdt();
    	url += "&detail=1";
    	
        String resultat = recupererPage(url);
        int posChaines = resultat.indexOf("var serv_a = [{");
        int posDisques = resultat.indexOf("var disk_a = [{");
        
        if (posChaines > 0 && posDisques > 0) {
        	// Récupération du javascript correspondant à la liste des chaines
        	String strChaines = resultat.substring(posChaines+14, posDisques);
        	int finChaines = strChaines.lastIndexOf("}");
        	strChaines = strChaines.substring(0, finChaines+1);
        	
        	// Récupération du javascript correspondant à la liste des disques durs
        	String strDisques = resultat.substring(posDisques+14, resultat.lastIndexOf("}")+1);

			// Remplissage des spinners
        	remplirSpinner(R.id.pvrPrgChaine, strChaines);
        	remplirSpinner(R.id.pvrPrgDisque, strDisques);
			
        	// S'il s'agit d'une modification, remplir le formulaire
        	remplirFiche();

            // Activation d'un listener sur le bouton OK
            final Button button = (Button) findViewById(R.id.PrgBtnOK);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                	DatePicker date = (DatePicker) findViewById(R.id.pvrPrgDate);
                	Spinner chaine = (Spinner) findViewById(R.id.pvrPrgChaine);
                	Log.d("FreeboxVCR",">>>>>>>>>>>>>>> "+date.getDayOfMonth()+"/"+date.getMonth()+"/"+date.getYear());
                	Log.d("Bicou", "Chaine = "+chaine.getSelectedItem().toString());
                }
            });
        }
        else {
        	Log.d("FreeboxVCR", "Impossible de télécharger le json");
        }
    }
    
	// S'il s'agit d'une modification, remplir le formulaire
    private void remplirFiche() {
		// Est-ce un ajout ou une modification d'un enregistrement ?
		Bundle bundle = getIntent().getExtras();	        
        if (bundle == null) {
        	return;
        }
        
        // Récupération des infos concernant cet enregistrement
        EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
        db.open();
        long idEnregistrement = bundle.getLong(EnregistrementsDbAdapter.KEY_ROWID);
        Cursor enregistrement = db.fetchEnregistrement(idEnregistrement);
        db.close();
        
        // Y'a qqn ?
        if (enregistrement.getCount() <= 0 || enregistrement.moveToFirst() == false) {
        	return;
        }
        
        // Views
        Spinner chaines = (Spinner) findViewById(R.id.pvrPrgChaine);
        DatePicker date = (DatePicker) findViewById(R.id.pvrPrgDate);
        TimePicker heure = (TimePicker) findViewById(R.id.pvrPrgHeure);
        EditText duree = (EditText) findViewById(R.id.pvrPrgDuree);
        EditText nom = (EditText) findViewById(R.id.pvrPrgNom);
        
        // Remplissage
        //TODO chaines.setSelection(chaines.findViewById(id))
    }
    
    private void remplirSpinner(int id, String str) {
		Spinner spinner = (Spinner) findViewById(id);
		List<String> liste = id == R.id.pvrPrgChaine ? getListeChaines(str) : getListeDisques(str);
		ArrayAdapter<String> adapter= new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, liste);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
    }
    
	// Fonctions pour parser le javascript listant les chaines et les disques durs
	private List<String> getListeChaines(String strChaines) {
		return getListe(strChaines, "}]},{\"name\"", 3, "name");
	}
	private List<String> getListeDisques(String strDisques) {
		return getListe(strDisques, "}", 1, "mount_point");
	}
	private List<String> getListe(String strSource, String sep, int shift, String index) {
		final List<String> liste = new ArrayList<String>();
		String str;
		int pos;
		JSONObject json;
		
		do {
			pos = strSource.indexOf(sep) + shift;
			if (pos <= 10) {
				pos = strSource.lastIndexOf("}") + 1;
				
				if (pos <= 10) {
					break;
				}
			}
			
			str = strSource.substring(0, pos);
			try {
				json = new JSONObject(str);
				liste.add(json.getString(index));
			} catch (JSONException e) {
				Log.d("Bicou", "Erreur de récupération de liste JSON avec "+index);
				e.printStackTrace();
				break;
			}
			
			if (strSource.length() > pos+1) {
				strSource = strSource.substring(pos+1);
			}
			else {
				break;
			}
		} while(true);
		
		return liste;
	}

    private String recupererPage(String url) {
        StringBuilder sb = new StringBuilder();
    	BufferedReader reader = HttpConnection.getRequest(url, true);
    	
    	if (reader == null) {
    		return null;
    	}

        String line = null;
        try {
             while ((line = reader.readLine()) != null) {
                  sb.append(line + "\n");
             }
        } catch (IOException e) {
             e.printStackTrace();
             return null;
        }

        return sb.toString();
    }
}