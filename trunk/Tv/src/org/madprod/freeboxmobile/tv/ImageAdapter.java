package org.madprod.freeboxmobile.tv;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter implements TvConstants
{
	private List< Map<String,Object> > streamsList;
	String USER_AGENT = null;
	Context c;

	public ImageAdapter(Context c, List< Map<String,Object> > sl)
	{
		this.c = c;
		streamsList = sl;
	}
    
    private final ImageDownloader imageDownloader = new ImageDownloader();
    
    public int getCount()
    {
        return streamsList.size();
    }

    public Object getItem(int position)
    {
        return streamsList.get(position);
    }

    public long getItemId(int position)
    {
        return (long) Integer.decode(streamsList.get(position).get(M_ID).toString());
    }

    public View getView(int position, View view, ViewGroup parent)
    {
    	Log.i(TAG, "ici : "+position);
    	ImageView logo;
        if (view == null)
        {
        	LayoutInflater inflater = LayoutInflater.from(c);
        	view = inflater.inflate(R.layout.tv_main_list_row, null);
        }
    	TextView text = (TextView)view.findViewById(R.id.tv_main_row_titre);
    	text.setText((String) streamsList.get(position).get(M_TITRE));
    	logo = (ImageView)view.findViewById(R.id.tv_main_row_img);
    	logo.setImageResource(R.drawable.chaine_vide);
        imageDownloader.download((String) streamsList.get(position).get(M_LOGO), (ImageView) logo);
        return view;
    }

    public ImageDownloader getImageDownloader()
    {
        return imageDownloader;
    }
}
