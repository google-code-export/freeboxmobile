package org.madprod.freeboxmobile.guide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author olivier rosello
 * *$Id$
 * 
 */

public class GuideNowActivity extends ListActivity implements GuideConstants
{
	GoogleAnalyticsTracker tracker;
	private NowAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Guide/GuideNow");
		setContentView(R.layout.guide_now_list);
		setTitle(getString(R.string.app_name)+" - Programmes en cours");
        adapter = new NowAdapter(this); 
        setListAdapter(adapter);
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
    	super.onStart();
    }

    @Override
    protected void onResume()
    {
    	super.onResume();
    	adapter.notifyDataSetChanged();
    	adapter.refresh();
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, 0, 0, "Mettre à jour l'affichage").setIcon(android.R.drawable.ic_menu_rotate);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    		case 0:
    			adapter.refresh();
    			adapter.notifyDataSetChanged();
    		return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id)
	{
		super.onListItemClick(l, v, pos, id);
		//Map<String,Object> map = new HashMap<String,Object>();
		Intent i = new Intent(this, GuideDetailsActivity.class);
		Map<String,Object> map = (Map<String, Object>) adapter.getItem(pos);
		i.putExtra(ChainesDbAdapter.KEY_PROG_TITLE, (String)map.get(ChainesDbAdapter.KEY_PROG_TITLE));
		i.putExtra(ChainesDbAdapter.KEY_PROG_CHANNEL_ID, (Integer)map.get(ChainesDbAdapter.KEY_PROG_CHANNEL_ID));
		i.putExtra(ChainesDbAdapter.KEY_PROG_DUREE, (Integer)map.get(ChainesDbAdapter.KEY_PROG_DUREE));
		i.putExtra(ChainesDbAdapter.KEY_PROG_DATETIME_DEB, (String)map.get(ChainesDbAdapter.KEY_PROG_DATETIME_DEB));
		i.putExtra(ChainesDbAdapter.KEY_PROG_DATETIME_FIN, (String)map.get(ChainesDbAdapter.KEY_PROG_DATETIME_FIN));
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL, (Integer)map.get(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL));
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, (String)map.get(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE));
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_NAME, (String)map.get(ChainesDbAdapter.KEY_GUIDECHAINE_NAME));
		i.putExtra(ChainesDbAdapter.KEY_PROG_RESUM_L, (String)map.get(ChainesDbAdapter.KEY_PROG_RESUM_L));
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_ID, (Integer)map.get(ChainesDbAdapter.KEY_GUIDECHAINE_ID));
        startActivity(i);
	}
	
    public static class NowAdapter extends BaseAdapter
    {
    	private Context mContext;
    	private List< Map<String,Object> > modulesList;

    	NowAdapter(Context cont)
    	{
        	mContext = cont;
    	}
    	
    	public void refresh()
    	{
        	Map<String,Object> map;
        	String image;

        	modulesList = new ArrayList< Map<String,Object> >();
        	ChainesDbAdapter mDbHelper = new ChainesDbAdapter(mContext);
    		mDbHelper.open();
        	Cursor c = mDbHelper.getProgsNow();
        	if (c != null)
        	{    		
        		if (c.moveToFirst())
        		{
        			do
        			{
        				if (c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_TITLE)).length() > 0)
        				{
    		    			map = new HashMap<String,Object>();
    		    			map.put(ChainesDbAdapter.KEY_PROG_TITLE, c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_TITLE)));
    		    			map.put(ChainesDbAdapter.KEY_PROG_DATETIME_DEB, c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DATETIME_DEB)));
    		    			map.put(ChainesDbAdapter.KEY_PROG_DATETIME_FIN, c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DATETIME_FIN)));
    		    			map.put(ChainesDbAdapter.KEY_PROG_RESUM_L, c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_RESUM_L)));
    		    			map.put(ChainesDbAdapter.KEY_PROG_DUREE, c.getInt(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DUREE)));
    		    			map.put(ChainesDbAdapter.KEY_PROG_CHANNEL_ID, c.getInt(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_CHANNEL_ID)));
    		    			map.put(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL, c.getInt(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL)));
    		    			map.put(ChainesDbAdapter.KEY_GUIDECHAINE_NAME, c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_NAME)));
    		    			map.put(ChainesDbAdapter.KEY_GUIDECHAINE_ID, c.getInt(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_ID)));

    		    			image = c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE)); 
    		    			if (image.length() > 0)
    		    			{
    		    			    String filepath = Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+image;
    		    	    		map.put(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, filepath);
    		    			}
    		    			modulesList.add(map);
        				}
/*    	    			Log.i(TAG, "ChannelID : "+c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_CHANNEL_ID))+
    	    			" DEB : "+c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DATETIME_DEB))+
    	    			" FIN : "+c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DATETIME_FIN))+
    	    			" TITRE : "+c.getString(c.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_TITLE))
    	    			);
*/
        			} while (c.moveToNext());
        		}
        		else
        		{
            		Log.d(TAG, "Guide Now : No data !");
        		}
        		c.close();
        	}
        	else
        	{
        		Log.d(TAG, "Guide Now : Cursor null !");
        	}
        	mDbHelper.close();
    	}

		@Override
		public int getCount()
		{
			if (modulesList != null)
			{
				return modulesList.size();
			}
			else
			{
				return 0;
			}
		}

		@Override
		public Object getItem(int position)
		{
			return modulesList.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;
			
			if (convertView == null)
			{
				holder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater.from(this.mContext);
				convertView = inflater.inflate(R.layout.guide_now_list_row, null);
				
				holder.title = (TextView)convertView.findViewById(R.id.guide_now_title);
				holder.deb = (TextView)convertView.findViewById(R.id.guide_now_start);
				holder.fin = (TextView)convertView.findViewById(R.id.guide_now_end);
		        holder.logo = (ImageView)convertView.findViewById(R.id.guide_now_logo_chaine);
		        holder.pb = (ProgressBar)convertView.findViewById(R.id.guide_now_ProgressBar);
		        convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder)convertView.getTag();
			}
	        
	        holder.title.setText((CharSequence)modulesList.get(position).get(ChainesDbAdapter.KEY_PROG_TITLE));
			Bitmap bmp = BitmapFactory.decodeFile((String)modulesList.get(position).get(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE));
	        holder.logo.setImageBitmap(bmp);
	        
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	        Date deb;
	        Date fin;
	        Date now = new Date();

			try
			{
				deb = sdf.parse((String)modulesList.get(position).get(ChainesDbAdapter.KEY_PROG_DATETIME_DEB));
				fin = sdf.parse((String)modulesList.get(position).get(ChainesDbAdapter.KEY_PROG_DATETIME_FIN));
		        holder.pb.setMax((int)((fin.getTime() - deb.getTime())/60000));
		        holder.pb.setProgress((int)((now.getTime() - deb.getTime())/60000));
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
	        holder.deb.setText((CharSequence)modulesList.get(position).get(ChainesDbAdapter.KEY_PROG_DATETIME_DEB).toString().substring(11, 16));
	        holder.fin.setText((CharSequence)modulesList.get(position).get(ChainesDbAdapter.KEY_PROG_DATETIME_FIN).toString().substring(11, 16));
			return convertView;
		}
		
		private class ViewHolder
		{
			TextView 	title;
			TextView	deb;
			TextView	fin;
			ImageView	logo;
			ProgressBar	pb;
		}
    }
}
