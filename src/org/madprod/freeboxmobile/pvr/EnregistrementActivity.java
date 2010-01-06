package org.madprod.freeboxmobile.pvr;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.madprod.freeboxmobile.HttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class EnregistrementActivity extends Activity {
	private long idEnregistrement;
	ProgressDialog prog = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pvr_enregistrement);
        
        final Activity enrAct = this;

        Bundle bundle = getIntent().getExtras();
        
        if (bundle != null) {
            // Récupération des infos concernant cet enregistrement
            EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(this);
            db.open();
            idEnregistrement = bundle.getLong(EnregistrementsDbAdapter.KEY_ROWID);
            Cursor c = db.fetchEnregistrement(idEnregistrement);
            db.close();
            
            if (c.getCount() > 0 && c.moveToFirst()) {
		        // Récupération des TextView
		        TextView chaine = (TextView) findViewById(R.id.pvrDispChaine);
		        TextView date = (TextView) findViewById(R.id.pvrDispDate);
		        TextView heure = (TextView) findViewById(R.id.pvrDispHeure);
		        TextView duree = (TextView) findViewById(R.id.pvrDispDuree);
		        TextView nom = (TextView) findViewById(R.id.pvrDispNom);
		        
		        // Affichage des données
		    	chaine.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE)));
		    	date.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE)));
		    	heure.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_HEURE)));
		    	duree.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DUREE))
		    			+ " minutes");
		    	nom.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_NOM)));
		    	
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
		    	// Suppr: on supprime & ferme l'activité
		    	suppr.setOnClickListener(new OnClickListener() {
		    		public void onClick(View v) {
		    	    	prog = ProgressDialog.show(enrAct, "Veuillez patienter", "Suppression en cours...", true,false);
		    			Thread deleteThread = new Thread(new Runnable() {
		    				public void run() {
		    					doSuppression();

		    					if (prog != null) {
		    						prog.dismiss();
		    						prog = null;
		    					}
		    					
		    					enrAct.finish();
		    				}
		    			});
		    			deleteThread.start();
		    		}
		    		
		    		private void doSuppression() {
		    			// TODO: Demande confirmation
		     		    			
		    			// Post vars pour suppression
	                	// ide=11&chaine_id=6&service_id=0&date=31%2F12%2F2009&h=23
	                	// &min=09&dur=15&name=titre&where_id=0&repeat_a=&supp=Supprimer
		    			
		    			// DB
		                EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(enrAct);
		                db.open();
		                Cursor c = db.fetchEnregistrement(idEnregistrement);
		                db.close();
		                
		                if (c == null || c.moveToFirst() == false) {
		                	return;
		                }
		    			
		                // Vars
	            		List<NameValuePair> postVars = new ArrayList<NameValuePair>();
	            		String ide, chaine_id, service_id, date, h, min, dur, name, where_id, repeat_a;

	            		ide = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_IDE));
	            		chaine_id = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE_ID));
	            		service_id = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_SERVICE_ID));
	            		date = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE));
	            		h = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_H));
	            		min = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_MIN));
	            		dur = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DUR));
	            		name = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_NAME));
	            		where_id = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_WHERE_ID));
	            		repeat_a = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_REPEAT_A));
	            		
	            		c.close();

	            		// Creation des variables POST
	            		postVars.add(new BasicNameValuePair("ide", ide));
	            		postVars.add(new BasicNameValuePair("chaine_id", chaine_id));
	            		postVars.add(new BasicNameValuePair("service_id", service_id));
	            		postVars.add(new BasicNameValuePair("date", date));
	            		postVars.add(new BasicNameValuePair("h", h));
	            		postVars.add(new BasicNameValuePair("min", min));
	            		postVars.add(new BasicNameValuePair("dur", dur));
	            		postVars.add(new BasicNameValuePair("name", name));
	            		postVars.add(new BasicNameValuePair("where_id", where_id));
	            		postVars.add(new BasicNameValuePair("repeat_a", repeat_a));

	            		postVars.add(new BasicNameValuePair("supp", "Supprimer"));
	            		
	            		// Requete HTTP
	                	int a = HttpConnection.connectFree();
	            		String url = "http://adsl.free.fr/admin/magneto.pl?id=";
	            		url += HttpConnection.getId()+"&idt="+HttpConnection.getIdt();
	            		HttpConnection.postRequest(url, postVars, true);
		    		}
		    	});
            }
            
            c.close();
        }
    }
}