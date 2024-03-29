package org.madprod.freeboxmobile.guide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;
import org.madprod.freeboxmobile.pvr.ProgrammationActivity;

import static org.madprod.freeboxmobile.StaticConstants.TAG;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
	GoogleAnalyticsTracker tracker;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Guide/GuideDetails");
        FBMNetTask.register(this);
        Log.i(TAG,"GUIDEDETAILS CREATE");
        setContentView(R.layout.guide_details);
        final Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
	        final TextView titreEmission = (TextView) findViewById(R.id.GuideDetailsTitre);
	        final TextView dateHeureEmission = (TextView) findViewById(R.id.GuideDetailsDateHeure);
	        TextView dureeEmission = (TextView) findViewById(R.id.GuideDetailsDuree);
	        TextView descEmission = (TextView) findViewById(R.id.GuideDetailsDesc);
	        TextView nomChaine = (TextView) findViewById(R.id.GuideDetailsNomChaine);
	        final ImageButton partager = (ImageButton)  findViewById(R.id.GuideDetailsButtonPartager);
	        ImageButton enregistrer = (ImageButton) findViewById(R.id.GuideDetailsButtonEnregistrer);
	        ImageButton regarder = (ImageButton) findViewById(R.id.GuideDetailsButtonRegarder);
	        ImageView logoChaine = (ImageView) findViewById(R.id.GuideDetailsLogoChaine);
	        ImageButton youTube = (ImageButton) findViewById(R.id.GuideDetailsYouTube);
	        ImageButton alloCine = (ImageButton) findViewById(R.id.GuideDetailsAlloCine);
	        
	        titreEmission.setText(extras.getString(ChainesDbAdapter.KEY_PROG_TITLE));
	        descEmission.setText(extras.getString(ChainesDbAdapter.KEY_PROG_RESUM_L));
	        Integer duree = extras.getInt(ChainesDbAdapter.KEY_PROG_DUREE);
	        dureeEmission.setText("Durée : "+(duree.toString())+" minute"+(duree > 1 ? "s":""));
	
	        String dt = extras.getString(ChainesDbAdapter.KEY_PROG_DATETIME_DEB);
	        String date[] = dt.split(" ");
	        String amj[] = date[0].split("-");
	        String hm[] = date[1].split(":");
	        
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			try
			{
				c.setTime(sdf.parse(date[0]));
				c.setFirstDayOfWeek(Calendar.MONDAY);
				String cdate = jours[c.get(Calendar.DAY_OF_WEEK)].toLowerCase()+" ";
				cdate += amj[2].length() == 1 ? "0" : "";
				cdate += amj[2];
				cdate += " "+mois[c.get(Calendar.MONTH)];
				cdate += " "+amj[0];
				cdate += " à "+hm[0]+"h"+hm[1];
		        dateHeureEmission.setText("Diffusé le "+cdate);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
				dateHeureEmission.setText("Diffusé le "+amj[2]+"/"+amj[1]+"/"+amj[0]+" à "+hm[0]+"h"+hm[1]);
			}

			youTube.setOnClickListener(
					new View.OnClickListener()
					{
						@Override
						public void onClick(View arg0)
						{
							tracker.trackPageView("Guide/YouTube");
							searchFilm(
									"com.google.android.youtube",
									null,
									(String) titreEmission.getText(),
									"http://m.youtube.com/results?search_query="+titreEmission.getText()+"&aq=f");
						}
					}
				);

			alloCine.setOnClickListener(
					new View.OnClickListener()
					{
						@Override
						public void onClick(View arg0)
						{
							tracker.trackPageView("Guide/AlloCine");
							// Si le programme est un film on peut chercher via l'appli AlloCiné, sinon on cherche via le site
							if (extras.getInt(ChainesDbAdapter.KEY_PROG_GENRE_ID) == 1)
							{
								searchFilm(
										"com.allocine.androidapp",
										".activities.SearchActivity",
										(String) titreEmission.getText(),
										"http://mobile.allocine.fr/recherche/default.html?motcle="+titreEmission.getText());
							}
							else
							{
								searchFilm(
										null,
										null,
										(String) titreEmission.getText(),
										"http://mobile.allocine.fr/recherche/default.html?motcle="+titreEmission.getText());
							}	
						}
					}
				);

			partager.setOnClickListener(
					new View.OnClickListener()
					{
						@Override
						public void onClick(View arg0)
						{
							String text = titreEmission.getText()+"\n"+dateHeureEmission.getText()+
								" sur "+extras.getString(ChainesDbAdapter.KEY_GUIDECHAINE_NAME)+
								"\n\n"+"Partagé par FreeboxMobile pour Android.";
					        Intent i = new Intent(Intent.ACTION_SEND)
				    			.putExtra(Intent.EXTRA_TEXT, text).setType("text/plain")
				    			.putExtra(Intent.EXTRA_SUBJECT, "ProgrammeTV partagé par FreeboxMobile")
//				    			.addCategory(Intent.CATEGORY_DEFAULT)
				    			;
							tracker.trackPageView("Guide/Share");
					    	startActivityForResult(Intent.createChooser(i, "Partagez ce programme avec"),0);
						}
					}
				);


			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try
			{
				Date dfin = sdf.parse(extras.getString(ChainesDbAdapter.KEY_PROG_DATETIME_FIN));
		        if (dfin.before(new Date()))
		        {
		    		enregistrer.setFocusable(false);
		    		enregistrer.setClickable(false);
		        }
		        else
		        {
			        enregistrer.setOnClickListener(
							new View.OnClickListener()
							{
								@Override
								public void onClick(View arg0)
								{
									proposerFin(extras);
								}
							}
						);
		        }
			}
	        catch (ParseException e)
	        {
	        	Log.e(TAG,"GUIDEDETAILSACTIVITY : pb parse date "+e.getMessage());
				e.printStackTrace();
			}
	        nomChaine.setText(extras.getString(ChainesDbAdapter.KEY_GUIDECHAINE_NAME)+" (canal "+
	        		((Integer)extras.getInt(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL)).toString()+")");
//	        regarder.setText("Regarder "/*+extras.getString(ChainesDbAdapter.KEY_GUIDECHAINE_NAME)*/);
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
        	Log.d(TAG,"GUIDEDETAILS ouvert sans données");
        }
        setTitle(getString(R.string.app_name)+" - Détails du programme");
    }
    
    @Override
    protected void onDestroy()
    {
    	tracker.stop();
    	FBMNetTask.unregister(this);
    	super.onDestroy();
    }

    private void searchFilm(final String packageName, final String className, String query, String alternateURL)
    {
        
        if (packageName != null)
        {
            Intent i = new Intent("android.intent.action.SEARCH");
	        if (className == null)
	        {
	            i.setPackage(packageName);    
	        }
	        else
	        {
	            i.setClassName(packageName, packageName+className);
	        }
	        i.putExtra(SearchManager.QUERY, query);
	        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);            
	        try
	        {
	            startActivity(i);
	        }
	        catch (Exception e)
	        {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(alternateURL)));
				Log.e(TAG, e.getMessage());
	        }
        }
        else
        {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(alternateURL)));        	
        }
    }

    private void proposerFin(final Bundle extras)
    {
    	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Enregistrer");
    	alertDialog.setIcon(R.drawable.fm_magnetoscope);
		alertDialog.setMessage("Enregistrer plusieurs programmes à la suite ?");
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Non", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
    			Intent i = new Intent(GuideDetailsActivity.this, ProgrammationActivity.class);
    			i.putExtras(extras);
    	        startActivity(i);
    	        dialog.dismiss();
			}
		});
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"Oui", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
		    	CharSequence[] iTitle = new CharSequence[PVR_MAX_PROGS];
		    	final CharSequence[] iEnd = new CharSequence[PVR_MAX_PROGS];
		    	int i = 0;
		    	String workString;
		    	ChainesDbAdapter mDbHelper = new ChainesDbAdapter(GuideDetailsActivity.this);
				mDbHelper.openRead();
		    	Cursor c = mDbHelper.getNextProgs(extras.getInt(ChainesDbAdapter.KEY_PROG_CHANNEL_ID), extras.getString(ChainesDbAdapter.KEY_PROG_DATETIME_DEB), 5);
		    	if (c != null)
		    	{
		    		if (c.moveToFirst())
		    		{
		    			do
		    			{
		    				workString = c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DATETIME_DEB)).substring(11,16);
		    				
		    				iTitle[i] = workString+" "+c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_TITLE));
		    				iEnd[i++] = c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DATETIME_FIN));
		    			} while (c.moveToNext());
		    		}
			    	c.close();
		    	}
		    	mDbHelper.close();
		    	AlertDialog.Builder builder = new AlertDialog.Builder(GuideDetailsActivity.this);
		    	builder.setTitle("Sélectionnez le dernier programme à enregistrer :");
		    	builder.setIcon(R.drawable.fm_magnetoscope);
		    	builder.setItems(iTitle, new DialogInterface.OnClickListener()
		    	{
		    		public void onClick(DialogInterface dialog, int item)
		    		{
		    			Intent i = new Intent(GuideDetailsActivity.this, ProgrammationActivity.class);
		    			i.putExtras(extras);
		    			i.putExtra(ChainesDbAdapter.KEY_PROG_TITLE, i.getStringExtra(ChainesDbAdapter.KEY_PROG_TITLE)+" et +");
		    			i.putExtra(ChainesDbAdapter.KEY_PROG_DATETIME_FIN, iEnd[item]);
		    	        startActivity(i);
		    	    }
		    	});
		    	builder.create().show();
			}
		});
		alertDialog.show();
    }
}
