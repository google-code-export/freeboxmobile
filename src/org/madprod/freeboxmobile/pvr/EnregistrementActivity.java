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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class EnregistrementActivity extends Activity {
	private long idEnregistrement;
	Activity enregistrementActivity = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pvr_enregistrement);
        
        enregistrementActivity = this;

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
						    	startActivityForResult(i, EnregistrementsActivity.ACTIVITY_PROGRAMMATION);
				    		}
				    	});
		    	// Suppr: on supprime & ferme l'activité
		    	suppr.setOnClickListener(new OnClickListener() {
		    		class DeleteEnregistrementTask extends AsyncTask<Void, Integer, Void> {
		    			ProgressDialog progressDialog = null;
						
						protected void onPreExecute() {
			    	    	progressDialog = ProgressDialog.show(enregistrementActivity, "Veuillez patienter", "Suppression en cours...", true,false);
						}

						@Override
						protected Void doInBackground(Void... params) {
	    					doSuppression();
	    					
	    					// TODO: vérifier la réponse de free, si la suppression a bien
	    					// été faite!
	    					
	    					EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(enregistrementActivity);
	    					db.open();
			                db.deleteEnregistrement(idEnregistrement);
			                db.close();

							return null;
						}
						
						protected void onPostExecute(Void v) {
							progressDialog.dismiss();
							progressDialog = null;
							setResult(EnregistrementsActivity.RESULT_SUPPRESSION_OK);
							enregistrementActivity.finish();
						}
		    		}
		    		
		    		public void onClick(View v) {
		    			new DeleteEnregistrementTask().execute((Void[])null);
		    		}
		    		
		    		private void doSuppression() {
		    			// TODO: Demande confirmation
		    			
		                // Vars
	            		List<NameValuePair> postVars = new ArrayList<NameValuePair>();
	            		String ide, chaine_id, service_id, date, h, min, dur, name, where_id, repeat_a;
		     		    			
		    			// Post vars pour suppression
	                	// ide=11&chaine_id=6&service_id=0&date=31%2F12%2F2009&h=23
	                	// &min=09&dur=15&name=titre&where_id=0&repeat_a=&supp=Supprimer
		    			
		    			// DB
		                EnregistrementsDbAdapter db = new EnregistrementsDbAdapter(enregistrementActivity);
		                db.open();
		                Cursor c = db.fetchEnregistrement(idEnregistrement);
		                db.close();
		                
		                if (c == null || c.moveToFirst() == false) {
		                	return;
		                }

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
	            		String url = "http://adsl.free.fr/admin/magneto.pl?id=";
	            		url += HttpConnection.getId()+"&idt="+HttpConnection.getIdt();
	            		HttpConnection.postRequest(url, postVars, true);
		    		}
		    	});
            }
            
            c.close();
        }
    }
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	finish();
    }
}