package org.madprod.freeboxmobile.pvr;

import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class EnregistrementActivity extends Activity {
	private long idEnregistrement;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enregistrement);

        Bundle bundle = getIntent().getExtras();
        
        if (bundle != null) {
            // Récupération des infos concernant cet enregistrement
            EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
            db.open();
            idEnregistrement = bundle.getLong(EnregistrementsDbAdapter.KEY_ROWID);
            Cursor enregistrement = db.fetchEnregistrement(idEnregistrement);
            db.close();
            
            if (enregistrement.getCount() > 0 && enregistrement.moveToFirst()) {
		        // Récupération des TextView
		        TextView chaine = (TextView) findViewById(R.id.pvrDispChaine);
		        TextView date = (TextView) findViewById(R.id.pvrDispDate);
		        TextView heure = (TextView) findViewById(R.id.pvrDispHeure);
		        TextView duree = (TextView) findViewById(R.id.pvrDispDuree);
		        TextView nom = (TextView) findViewById(R.id.pvrDispNom);
		        
		        // Affichage des données
		    	chaine.setText(enregistrement.getString(enregistrement.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE)));
		    	date.setText(enregistrement.getString(enregistrement.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE)));
		    	heure.setText(enregistrement.getString(enregistrement.getColumnIndex(EnregistrementsDbAdapter.KEY_HEURE)));
		    	duree.setText(enregistrement.getString(enregistrement.getColumnIndex(EnregistrementsDbAdapter.KEY_DUREE))
		    			+ " minutes");
		    	nom.setText(enregistrement.getString(enregistrement.getColumnIndex(EnregistrementsDbAdapter.KEY_NOM)));
		    	
		    	// Roles des boutons
		    	Button modif = (Button) findViewById(R.id.pvrBtnModif);
		    	Button suppr = (Button) findViewById(R.id.pvrBtnSuppr);
		    	
		    	// Modif: on lance l'activité ProgrammationActivity avec le paramètre ROWID
		    	modif.setOnClickListener(
		    			new View.OnClickListener() {
		    				public void onClick(View v) {
		    					Intent i = new Intent();
		    					i.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.pvr.ProgrammationActivity");
				    			i.putExtra(EnregistrementsDbAdapter.KEY_ROWID, idEnregistrement);
						    	startActivity(i);
				    		}
				    	});
		    	
		    	suppr.setOnClickListener(new OnClickListener() {
		    		public void onClick(View v) {
		    			// Demande confirmation
		    		}
		    	});
            }
        }
    }
}