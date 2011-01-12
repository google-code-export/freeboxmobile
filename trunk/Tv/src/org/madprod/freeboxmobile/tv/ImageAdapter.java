package org.madprod.freeboxmobile.tv;

import java.util.ArrayList;
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
	private ArrayList<Chaine> listeChaines;
	String USER_AGENT = null;
	Context c;

	public ImageAdapter(Context c, ArrayList<Chaine> lc)
	{
		this.c = c;
		listeChaines = lc;
	}
    
    private final ImageDownloader imageDownloader = new ImageDownloader();
    
    public int getCount()
    {
        return listeChaines.size();
    }

    public Object getItem(int position)
    {
        return listeChaines.get(position);
    }

    public long getItemId(int position)
    {
   		return (long) listeChaines.get(position).getChannelId();
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
    	text.setText((String) listeChaines.get(position).getName());
    	logo = (ImageView)view.findViewById(R.id.tv_main_row_img);
    	logo.setImageResource(R.drawable.chaine_vide);
        imageDownloader.download((String) listeChaines.get(position).getLogoUrl(), (ImageView) logo);
        return view;
    }

    public ImageDownloader getImageDownloader()
    {
        return imageDownloader;
    }
}
