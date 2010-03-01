package org.madprod.freeboxmobile.guide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;
import org.madprod.freeboxmobile.pvr.ProgrammationActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideDetailsActivity extends Activity implements GuideConstants
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
	        final TextView titreEmission = (TextView) findViewById(R.id.GuideDetailsTitre);
	        final TextView dateHeureEmission = (TextView) findViewById(R.id.GuideDetailsDateHeure);
	        TextView dureeEmission = (TextView) findViewById(R.id.GuideDetailsDuree);
	        TextView descEmission = (TextView) findViewById(R.id.GuideDetailsDesc);
	        TextView nomChaine = (TextView) findViewById(R.id.GuideDetailsNomChaine);
	        Button partager = (Button)  findViewById(R.id.GuideDetailsButtonPartager);
	        Button enregistrer = (Button) findViewById(R.id.GuideDetailsButtonEnregistrer);
	        Button regarder = (Button) findViewById(R.id.GuideDetailsButtonRegarder);
	        ImageView logoChaine = (ImageView) findViewById(R.id.GuideDetailsLogoChaine);
	        
	        titreEmission.setText(extras.getString(ChainesDbAdapter.KEY_PROG_TITLE));
	        descEmission.setText(extras.getString(ChainesDbAdapter.KEY_PROG_RESUM_L));
	        Integer duree = extras.getInt(ChainesDbAdapter.KEY_PROG_DUREE);
	        dureeEmission.setText("Durée : "+(duree.toString())+" minute"+(duree > 1 ? "s":""));
	
	        String dt = extras.getString(ChainesDbAdapter.KEY_PROG_DATETIME_DEB);
	        String date[] = dt.split(" ");
	        String amj[] = date[0].split("-");
	        String hm[] = date[1].split(":");
	        dateHeureEmission.setText("Diffusé le "+amj[2]+"/"+amj[1]+"/"+amj[0]+" à "+hm[0]+"h"+hm[1]);

	        partager.setOnClickListener(
					new View.OnClickListener()
					{
						@Override
						public void onClick(View arg0)
						{
							String text = titreEmission.getText()+"\n"+dateHeureEmission.getText()+"\n\n"+"Partagé par FreeboxMobile pour Android.";
					        Intent i = new Intent(Intent.ACTION_SEND)
				    		.putExtra(Intent.EXTRA_TEXT, text).setType("text/plain")
				    		.putExtra(Intent.EXTRA_SUBJECT, "ProgrammeTV partagé par FreeboxMobile")
//				    		.addCategory(Intent.CATEGORY_DEFAULT)
				    		;
					    	startActivityForResult(Intent.createChooser(i, "Partagez ce programme avec"),0);
						}
					}
				);


	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        try
	        {
				Date dfin = sdf.parse(extras.getString(ChainesDbAdapter.KEY_PROG_DATETIME_FIN));
		        if (dfin.before(new Date()))
		        {
		    		enregistrer.setFocusable(false);
		    		enregistrer.setClickable(false);
		    		enregistrer.setTextColor(0xFF888888);
		        }
		        else
		        {
			        enregistrer.setOnClickListener(
							new View.OnClickListener()
							{
								@Override
								public void onClick(View arg0)
								{
									Intent i = new Intent(GuideDetailsActivity.this, ProgrammationActivity.class);
									i.putExtras(extras);
							        startActivity(i);
								}
							}
						);
		        }
			}
	        catch (ParseException e)
	        {
	        	FBMHttpConnection.FBMLog("GUIDEDETAILSACTIVITY : pb parse date "+e.getMessage());
				e.printStackTrace();
			}
	        nomChaine.setText(extras.getString(ChainesDbAdapter.KEY_GUIDECHAINE_NAME)+" (canal "+
	        		((Integer)extras.getInt(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL)).toString()+")");
	        regarder.setText("Regarder "/*+extras.getString(ChainesDbAdapter.KEY_GUIDECHAINE_NAME)*/);
	        String filepath = Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+extras.getString(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE);
			Bitmap bmp = BitmapFactory.decodeFile(filepath);
			if (bmp != null)
			{
				logoChaine.setImageBitmap(bmp);
			}
			else
			{
				logoChaine.setVisibility(View.GONE);
			}
        }
        else
        {
        	FBMHttpConnection.FBMLog("GUIDEDETAILS ouvert sans données");
        }
        setTitle(getString(R.string.app_name)+" - Détails du programme");
    }
}
