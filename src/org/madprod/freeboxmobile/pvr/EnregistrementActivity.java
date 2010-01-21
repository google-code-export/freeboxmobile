package org.madprod.freeboxmobile.pvr;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.guide.Guide;
import org.madprod.freeboxmobile.guide.Guide.Chaines.Chaines_Chaine;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
		        TextView recur = (TextView) findViewById(R.id.pvrDispRecursivite);
		        LinearLayout recurLayout = (LinearLayout) findViewById(R.id.pvrDispRecursiviteLayout);
		        String strRecur, strRecurDb;
		        
		        // Logo
		        int canalChaine = c.getInt(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE_ID));
		        new TelechargerLogoChaineTask().execute(canalChaine);
		        
		        // Affichage des données
		    	chaine.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_CHAINE)));
		    	date.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DATE)));
		    	heure.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_HEURE)));
		    	duree.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_DUREE))
		    			+ " minutes");
		    	nom.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_NOM)));
		    	
		    	// Disque
		    	//TODO: stocker qqpart des infos sur le disque (espace libre, etc) et l'afficher ici
		    	//disque.setText(c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_WHERE_ID)));
		    	
		    	// Récurrence
		    	strRecurDb = c.getString(c.getColumnIndex(EnregistrementsDbAdapter.KEY_REPEAT_A));
		    	if (strRecurDb.length() == 8) {
		    		strRecur = "";
		    		if (strRecurDb.charAt(0) ==  '1')	strRecur += getString(R.string.pvrLundi)+"\n";
		    		if (strRecurDb.charAt(1) ==  '1')	strRecur += getString(R.string.pvrMardi)+"\n";
		    		if (strRecurDb.charAt(2) ==  '1')	strRecur += getString(R.string.pvrMercredi)+"\n";
		    		if (strRecurDb.charAt(3) ==  '1')	strRecur += getString(R.string.pvrJeudi)+"\n";
		    		if (strRecurDb.charAt(4) ==  '1')	strRecur += getString(R.string.pvrVendredi)+"\n";
		    		if (strRecurDb.charAt(5) ==  '1')	strRecur += getString(R.string.pvrSamedi)+"\n";
		    		if (strRecurDb.charAt(6) ==  '1')	strRecur += getString(R.string.pvrDimanche)+"\n";
		    		recur.setText(strRecur);
		    		recurLayout.setVisibility(View.VISIBLE);
		    	}
		    	
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
	            		url += FBMHttpConnection.getId()+"&idt="+FBMHttpConnection.getIdt();
	            		FBMHttpConnection.postRequest(url, postVars, true);
		    		}
		    	});
            }
            
            c.close();
        }
    }
	

	/**
	 * télécharge le logo d'une chaine
	 * @author bduffez
	 *
	 */
    class TelechargerLogoChaineTask extends AsyncTask<Integer, Integer, Bitmap> {
        protected void onPreExecute() {
        }
    	
        protected Bitmap doInBackground(Integer... arg0) {
    		String url = "http://adsl.free.fr/admin/magneto.pl?id=";
    		url += "id="+FBMHttpConnection.getId();
    		url += "&idt="+FBMHttpConnection.getIdt();
    		url += "&ajax=get_chaines";
    		url += "&date=2010-01-14+19%3A00%3A00";//3A = :
    		String json = FBMHttpConnection.getPage(FBMHttpConnection.getRequest(url, true));
    		Guide guideTv = new Guide(json, false, true, false); 
    		url = "http://adsl.free.fr/im/chaines/";
    		Chaines_Chaine chaine = guideTv.getChaine(arg0[0]);
    		url += chaine.getImage();
    		InputStream is = FBMHttpConnection.getRequestIS(url);
    		return BitmapFactory.decodeStream(is);
        }
        
        protected void onPostExecute(Bitmap bmp) {
    		ImageView im = (ImageView) findViewById(R.id.pvrLogoChaine);
    		if (bmp != null) {
    			im.setImageBitmap(bmp);
    		}
        }
    }
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	finish();
    }
}