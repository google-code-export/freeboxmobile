package org.madprod.freeboxmobile.guide;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;
import org.madprod.freeboxmobile.pvr.EnregistrementsDbAdapter;
import org.madprod.freeboxmobile.pvr.ProgrammationActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideDetailsActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FBMHttpConnection.initVars(this, null);
        FBMHttpConnection.FBMLog("GUIDEDETAILS CREATE");
        setContentView(R.layout.guide_details);
        final Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
	        TextView titreEmission = (TextView) findViewById(R.id.GuideDetailsTitre);
	        TextView dateHeureEmission = (TextView) findViewById(R.id.GuideDetailsDateHeure);
	        TextView dureeEmission = (TextView) findViewById(R.id.GuideDetailsDuree);
	        TextView descEmission = (TextView) findViewById(R.id.GuideDetailsDesc);
	        TextView nomChaine = (TextView) findViewById(R.id.GuideDetailsNomChaine);
	        Button enregistrer = (Button) findViewById(R.id.GuideDetailsButtonEnregistrer);
	        
	        titreEmission.setText(extras.getString(ChainesDbAdapter.KEY_PROG_TITLE));
	        descEmission.setText(extras.getString(ChainesDbAdapter.KEY_PROG_RESUM_L));
	        Integer duree = extras.getInt(ChainesDbAdapter.KEY_PROG_DUREE);
	        dureeEmission.setText("Durée : "+(duree.toString())+" minute"+(duree > 1 ? "s":""));
	
	        String dt = extras.getString(ChainesDbAdapter.KEY_PROG_DATETIME_DEB);
	        String date[] = dt.split(" ");
	        String amj[] = date[0].split("-");
	        String hm[] = date[1].split(":");
	        dateHeureEmission.setText("Diffusé le "+amj[2]+"/"+amj[1]+"/"+amj[0]+" à "+hm[0]+"h"+hm[1]);
	        
	        nomChaine.setText(extras.getString(ChainesDbAdapter.KEY_GUIDECHAINE_NAME)+" (canal "+
	        		((Integer)extras.getInt(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL)).toString()+")");
	        enregistrer.setOnClickListener(
					new View.OnClickListener()
					{
						@Override
						public void onClick(View arg0)
						{
							Intent i = new Intent(GuideDetailsActivity.this, ProgrammationActivity.class);
/*							i.putExtra(ChainesDbAdapter.KEY_PROG_TITLE, p.titre);
							i.putExtra(ChainesDbAdapter.KEY_PROG_CHANNEL_ID, p.channel_id);
							i.putExtra(ChainesDbAdapter.KEY_PROG_DUREE, p.duree);
							i.putExtra(ChainesDbAdapter.KEY_PROG_DATETIME_DEB, p.datetime_deb);
							i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL, p.canal);
							i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_NAME, p.chaine_name);
							i.putExtra(ChainesDbAdapter.KEY_PROG_RESUM_L, p.resum_l);
*/
							i.putExtras(extras);
					        startActivity(i);

						}
					}
				);
        }
        else
        {
        	FBMHttpConnection.FBMLog("GUIDEDETAILS ouvert sans données");
        }
/*		i.putExtra(ChainesDbAdapter.KEY_PROG_CHANNEL_ID, p.channel_id);
		i.putExtra(ChainesDbAdapter.KEY_PROG_DUREE, p.duree);
		i.putExtra(ChainesDbAdapter.KEY_PROG_DATETIME_DEB, p.datetime_deb);
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL, p.canal);
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_NAME, p.chaine_name);
*/
        setTitle(getString(R.string.app_name)+" - Détails du programme");
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    }
}
