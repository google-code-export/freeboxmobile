package org.madprod.freeboxmobile.tv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.mvv.MevoSync;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
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
			displayWarningTv();
		}
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

    private void displayWarningTv()
    {
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("Attention ! Merci de lire");
		d.setIcon(R.drawable.icon_fbm_reverse);
    	d.setMessage(
    		"Cette fonctionnalité ne fonctionnera QUE si vous êtes sur le réseau Free : soit connecté à une Freebox, soit connecté au réseau FreeWifi (lorsque vous êtes en déplacement).\n\n"+
			"Pour utiliser cette fonctionnalité, vous devez installer une application capable de lire les flux vidéos 'TS' comme 'VPlayer'.\n\n"+
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
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.abitno.vplayer")));
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
	        	displayWarningTv();
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
    	 	//intent.setDataAndType(Uri.parse("/sdcard/test.mp4"), "video/mp4");
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

    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=cxLG2wtE7TM")));
    
    /*
    Intent i = new Intent(Intent.ACTION_VIEW);
    Uri u = Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, "1");
    i.setData(u);
    startActivity(i);
    */
    
    /*
     Intent intent = new Intent();
	intent.setAction(android.content.Intent.ACTION_VIEW);
	File file = new File(<my file>);
	intent.setDataAndType(Uri.fromFile(file), <mimetype>);	
	startActivity(intent); 

	Using mp3 and "audio/*" mimetype it works OK
     */
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
