package org.madprod.freeboxmobile.tv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onResume()
    {
		Log.i(TAG,"TvActivity Resume");
    	super.onResume();
        SimpleAdapter mList = new SimpleAdapter(this, streamsList, R.layout.tv_main_list_row, new String[] {M_TITRE}, new int[] {R.id.tv_main_row_titre});
        setListAdapter(mList);
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
	private void addChaine(String titre, String shorter, String id)
	{
    	Map<String,Object> map = new HashMap<String,Object>();
		map.put(M_TITRE, titre);
		map.put(M_SHORT, shorter);
		map.put(M_ID, id);
		streamsList.add(map);		
	}

	private void setChaines()
    {
		addChaine("France 2",	"france2",	"201");
		addChaine("France 3",	"france3",	"202");
		addChaine("France 4",	"france4",	"376");
		addChaine("France 5",	"france5",	"203");
		addChaine("Direct 8",	"direct8",	"372");
		addChaine("NT1",		"nt1",		"374");
		addChaine("NRJ 12",		"nrj12",	"375");
		addChaine("LCP",		"lcp",		"226");
		addChaine("BFM TV",		"bfmtv",	"400");
		addChaine("TV5",		"tv5",		"206");
		addChaine("France O",	"franceo",	"238");
		addChaine("AlJazeera",	"aljazeera","494");
		addChaine("Demain",		"demain",	"227");
		addChaine("Liberty Tv",	"libertytv","215");
		addChaine("Fashion Tv",	"fashiontv","0");
		addChaine("Guysen",		"guysen",	"0");
		addChaine("NRJ Hits",	"nrjhits",	"620");
		addChaine("NRJ Paris",	"nrjparis",	"0");
		addChaine("KTO",		"kto",		"0");
		addChaine("Luxe TV",	"luxetv",	"0");
    }
}
