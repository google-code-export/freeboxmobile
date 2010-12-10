package org.madprod.freeboxmobile.tv;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * @author olivier rosello
 * *$Id$
 * 
 */

public class TvActivity extends ListActivity implements TvConstants
{
	private List< Map<String,Object> > streamsList;
	GoogleAnalyticsTracker tracker;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Tv/HomeTv");
		streamsList = new ArrayList< Map<String,Object> >();
        setChaines();
		setContentView(R.layout.tv_main_list);
		setTitle(getString(R.string.app_name)+" "+FBMHttpConnection.getTitle());
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		if (!mgr.getString(KEY_SPLASH_TV, "0").equals(Utils.getFBMVersion(this)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_TV, Utils.getFBMVersion(this));
			editor.commit();
			displayHelp();
		}
		/*
		else
		{
			displayHelp();
		}
		*/
    }

    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	tracker.stop();
    }
    
    @Override
    protected void onStart()
    {
    	Log.i(TAG,"TvActivity Start");
    	super.onStart();
    }

    private void displayHelp()
    {
    	AlertDialog d3 = new AlertDialog.Builder(TvActivity.this).create();
		d3.setIcon(R.drawable.icon_fbm_reverse);
		d3.setTitle("Aide");
		d3.setMessage("A tout moment, vous pouvez relire les informations qui vont suivre en utilisant la touche menu -> Aide.\n\n");
		d3.setButton(DialogInterface.BUTTON_POSITIVE, "Continuer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				checkOS();
			}
		});
		d3.show();    	    	
    }

    private void displayWrongOS()
    {
		tracker.trackPageView("TV/DisplayWrongOS");
    	AlertDialog d3 = new AlertDialog.Builder(TvActivity.this).create();
		d3.setIcon(R.drawable.icon_fbm_reverse);
		d3.setTitle("Problème de version d'Android");
		d3.setMessage("La version d'Android de votre téléphone ("+Build.VERSION.RELEASE+") ne devrait pas être compatible avec le player vidéo nécessaire à la visualisation des chaînes TV.\n\n"+
				"Si c'est le cas, Vplayer ne sera pas visible sur le market ou ne fonctionnera pas après installation.\n");
		d3.setButton(DialogInterface.BUTTON_POSITIVE, "J'ai compris", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				checkPhone();
			}
		});
		d3.show();    	
    }

    private void displayWrongPhone()
    {
		tracker.trackPageView("TV/DisplayWrongPhone");
    	AlertDialog d3 = new AlertDialog.Builder(TvActivity.this).create();
		d3.setIcon(R.drawable.icon_fbm_reverse);
		d3.setTitle("Problème de compatibilité");
		d3.setMessage("Il se peut que votre appareil ne soit pas compatible avec le player vidéo nécessaire à la visualisation de la télévision.\n\n"+
				"Si c'est le cas, Vplayer ne sera pas visible sur le market ou ne fonctionnera pas après installation.\n");
		d3.setButton(DialogInterface.BUTTON_POSITIVE, "J'ai compris", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				downloadPlayer();
			}
		});
		d3.show();
    }
    
	public static int getPlatformVersion()
	{
		try
		{
			Field verField = Class.forName("android.os.Build$VERSION").getField("SDK_INT");
			int ver = verField.getInt(verField);
			return ver;
		}
		catch (Exception e)
		{
			// android.os.Build$VERSION is not there on Cupcake
			return 3;
		}
	}

	// Code from enh project (enh.googlecode.com)
	private String getFieldReflectively(Build build, String fieldName)
	{
		try
		{
			final Field field = Build.class.getField(fieldName);
			return field.get(build).toString();
		}
		catch (Exception ex)
		{
			return "inconnu";
		}
	}

    private void checkOS()
    {
    	if (getPlatformVersion() < Build.VERSION_CODES.ECLAIR_MR1)
    	{
    		displayWrongOS();
    	}
    	else
    	{
    		checkPhone();
    	}
    }

    /*
     *  Fonctionne sur :
     *  - Google Nexus One (2.2)
     *  - Samsung Galaxy S (2.2)
     *  - HTC Desire HD (2.2)
     *  - HTC Desire
     *  - Acer Liquid (2.2)
     *  - Archos 70 (2.2)
     *  - Archos 10.1
     *  - Motorola Milestone (2.2)
     * 
     *  Ne Fonctionne pas sur :
     *  - Dell Streak (2.2)
     *  
     *  Inivisible sur :
     *  - HTC Hero (2.1) (2.2)
     *  - HTC Legend
     *  - HTC Dream
     *  - HTC Wildfire
     *  - HTC Magic (2.2)
     *  - HTC Tattoo ?
     *  - Motorola Dext
     *  - Samsung Galaxy (Original)
     *  - Samsung Galaxy Spica
     *  - Samsung Naos
     *  - Samsung Teos
     *  - LG Optimus GT540
     *  - Sony Ericsson x10 mini pro (U20i)
     *  
     */

    /*
     * A prendre en compte ci-dessous (chaîne MODEL inconnue) :
     *  - Sony XPeria x10mini
     */

    private void checkPhone()
    {
		final Build build = new Build();
    	String b = Build.MODEL.toLowerCase();
		final String cpuAbi = getFieldReflectively(build, "CPU_ABI");

		// Here we check if CPU is ok (available only for OS > 1.6) and if model is ok (for 1.5...)
    	if (
    			!cpuAbi.contains("armeabi-") ||
    			b.contains("u20i") ||
    			b.contains("legend") ||
    			b.contains("hero") ||
    			b.contains("dream") ||
    			b.contains("magic") ||
    			b.contains("tattoo") ||
    			b.contains("wildfire") ||
    			b.contains("steak") ||
    			b.contains("legend") ||
    			b.contains("mb200") ||		// Motorola Dext
    			b.contains("gt540") ||		// LG Optimus
    			b.contains("gt-i5800") ||	// Samsung Teos    			
    			b.contains("gt-i5801") ||	// Samsung Naos
    			b.contains("gt-i5700") ||	// Samsung Galaxy Spica
    			b.contains("gt-i7500")		// Samsung Galaxy (Original)
    		)
    	{
    		displayWrongPhone();
    	}
    	else
    	{
    		downloadPlayer();
    	}
    }
    
    private void downloadPlayer()
    {
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("Attention ! Merci de lire");
		d.setIcon(R.drawable.icon_fbm_reverse);
    	d.setMessage(
    		"Cette fonctionnalité ne fonctionnera QUE si vous êtes sur le réseau Free :\n- soit connecté à une Freebox,\n- soit connecté au réseau FreeWifi (lorsque vous êtes en déplacement).\n\n"+
			"Pour utiliser cette fonctionnalité, vous devez installer une application capable de lire les flux vidéos 'TS' comme 'VPlayer' (disponible sur Android 2.1 et +) :\n"+
			"- Cliquez sur 'Installer' pour installer VPlayer à partir du market.\n"+
			"- Cliquez sur 'Continuer' si vous avez déjà installé vplayer ou une autre application capable de lire de tels flux.\n"+
			"- Cliquez sur 'Annuler' pour ne rien faire et revenir à l'écran précédent.\n"
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Installer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
	    		tracker.trackPageView("TV/InstallPlayer");
	        	AlertDialog d2 = new AlertDialog.Builder(TvActivity.this).create();
	    		d2.setTitle("Installer VPlayer");
	    		d2.setIcon(R.drawable.icon_fbm_reverse);
	        	d2.setMessage(
	        		"Vous pouvez installer VPlayer soit à partir du market, soit en téléchargeant l'application en direct.\n"
	    		);
	    		d2.setButton(DialogInterface.BUTTON_NEGATIVE, "A partir du market", new DialogInterface.OnClickListener()
	    		{
	    			public void onClick(DialogInterface dialog, int which)
	    			{
	    				dialog.dismiss();
	    	    		tracker.trackPageView("TV/InstallPlayer");	    	    		
	    				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.abitno.vplayer")));
	    			}
	    		});
	    		d2.setButton(DialogInterface.BUTTON_POSITIVE, "En téléchargement direct", new DialogInterface.OnClickListener()
	    		{
	    			public void onClick(DialogInterface dialog, int which)
	    			{
	    				dialog.dismiss();
	    	    		tracker.trackPageView("TV/InstallPlayerDirect");
	    	        	AlertDialog d3 = new AlertDialog.Builder(TvActivity.this).create();
	    	    		d3.setIcon(R.drawable.icon_fbm_reverse);
	    	    		d3.setTitle("Téléchargement de VPlayer");
	    	    		d3.setMessage("Une fois le téléchargement terminé, cliquer sur sa notification 'VPLayer.apk' dans la barre de notification en haut de l'écran afin d'installer VPlayer.\n");
	    	    		d3.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener()
	    	    		{
	    	    			public void onClick(DialogInterface dialog, int which)
	    	    			{
	    	    				dialog.dismiss();
	    	    				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://files-for-abitno.googlecode.com/files/VPlayer.apk")));
	    	    			}
	    	    		});
	    	    		d3.show();
	    			}
	    		});
	    		d2.show();
			}
		});
		d.setButton(DialogInterface.BUTTON_NEUTRAL, "Continuer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		d.setButton(DialogInterface.BUTTON_NEGATIVE, "Annnuler", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				finish();
			}
		});
    	d.show();    	
    }
    
    @Override
    protected void onResume()
    {
		Log.i(TAG,"TvActivity Resume");
    	super.onResume();
        SimpleAdapter mList = new SimpleAdapter(this, streamsList, R.layout.tv_main_list_row, new String[] {M_TITRE, M_LOGO}, new int[] {R.id.tv_main_row_titre, R.id.tv_main_row_img});
        setListAdapter(mList);
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, 1, 0, "Aide").setIcon(android.R.drawable.ic_menu_help);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
	        case 1:
	        	checkOS();
	        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        String streamName = (String) streamsList.get((int) id).get(M_TITRE);
        String streamShort = (String) streamsList.get((int) id).get(M_SHORT);

    	if (false)//(moduleName.equals(getString(R.string.buttonTv)))
    	{
    		Intent i = new Intent("kr.mobilesoft.yxplayer.PLAYER");
//	    		Intent i = new Intent("kr.mobilesoft.yxplayer.StreamsView");
    		i.putExtra("url", "http://vipmms9.yacast.net/bfm_bfmtv");
    		i.putExtra("uri", "http://vipmms9.yacast.net/bfm_bfmtv");
//	    		i.putExtra("add", "http://vipmms9.yacast.net/bfm_bfmtv");
//	    		i.putExtra("stream", "http://vipmms9.yacast.net/bfm_bfmtv");
//	    		i.putExtra("streams", "http://vipmms9.yacast.net/bfm_bfmtv");
//	    		i.putExtra("URL", "http://vipmms9.yacast.net/bfm_bfmtv");
//	    		i.putExtra("URI", "http://vipmms9.yacast.net/bfm_bfmtv");
//	    		i.putExtra("open", "http://vipmms9.yacast.net/bfm_bfmtv"); // fait planter
//	    		i.putExtra("video", "http://vipmms9.yacast.net/bfm_bfmtv");
    		try
    		{
    			startActivity(i);
    		}
    		catch (Exception e)
    		{
    			Toast.makeText(this, "Pb ! "+e.getMessage(), 2000).show();
    			Log.d(TAG, "PB : "+e.getMessage());
    		}
    	}
    	if (streamShort != null)
    	{
    	    Intent intent = new Intent();
    	 	intent.setAction(android.content.Intent.ACTION_VIEW);
    	 	intent.setDataAndType(Uri.parse("http://tv.freebox.fr/stream_"+streamShort), "video/mp4");
    	 	try
    	 	{
    	 		startActivity(intent);
    	 	}
    	 	catch (Exception e)
    	 	{
    	 		Toast.makeText(this, "Problème : "+e.getMessage(), Toast.LENGTH_LONG).show();
    	 	}
    	}
    }

	private void addChaine(String titre, String shorter, int logo, String id)
	{
    	Map<String,Object> map = new HashMap<String,Object>();
		map.put(M_TITRE, titre);
		map.put(M_SHORT, shorter);
		map.put(M_LOGO, logo);
		map.put(M_ID, id);
		streamsList.add(map);		
	}

	private void setChaines()
    {
		addChaine("France 2",	"france2",	R.drawable.tv_fr2,		"201");
		addChaine("France 3",	"france3",	R.drawable.tv_fr3,		"202");
		addChaine("France 4",	"france4",	R.drawable.tv_fr4,		"376");
		addChaine("France 5",	"france5",	R.drawable.tv_fr5,		"203");
		addChaine("Direct 8",	"direct8",	R.drawable.tv_direct8,	"372");
		addChaine("NT1",		"nt1",		R.drawable.tv_nt1,		"374");
		addChaine("NRJ 12",		"nrj12",	R.drawable.tv_nrj12,	"375");
		addChaine("LCP",		"lcp",		R.drawable.tv_lcp,		"226");
		addChaine("BFM TV",		"bfmtv",	R.drawable.tv_bfm,		"400");
		addChaine("TV5",		"tv5",		R.drawable.tv_tv5,		"206");
		addChaine("France O",	"franceo",	R.drawable.tv_franceo,	"238");
		addChaine("AlJazeera",	"aljazeera",R.drawable.tv_aljazeerai,"494");
		addChaine("Demain",		"demain",	R.drawable.tv_demaintv,	"227");
		addChaine("Liberty Tv",	"libertytv",R.drawable.tv_libertytv,"215");
		addChaine("Fashion Tv",	"fashiontv",R.drawable.chaine_vide,	"0");
		addChaine("Guysen",		"guysen",	R.drawable.chaine_vide,	"0");
		addChaine("NRJ Hits",	"nrjhits",	R.drawable.tv_nrjhits,	"620");
		addChaine("NRJ Paris",	"nrjparis",	R.drawable.tv_nrjparis,	"0");
		addChaine("KTO",		"kto",		R.drawable.tv_kto,		"0");
		addChaine("Luxe TV",	"luxetv",	R.drawable.tv_luxetv,	"0");
    }
}
