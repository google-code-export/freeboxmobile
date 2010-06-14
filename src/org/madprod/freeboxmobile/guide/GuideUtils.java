package org.madprod.freeboxmobile.guide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.WrapBitmap;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.app.Activity;
import android.app.ListActivity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideUtils implements Constants
{
	// Cette liste sert à récupérer la date correspondant à l'indice du spinner
	public static List<String> calDates = new ArrayList<String>();
	public static List<String> dates;
	private static boolean mNewBitmapAvailable;
	
	static
	{
		try
		{
			WrapBitmap.checkAvailable();
			mNewBitmapAvailable = true;
        	Log.d(TAG, "NEW VERSION OK");
		}
		catch (Throwable t)
		{
			mNewBitmapAvailable = false;
        	Log.d(TAG, "NEW VERSION NOT OK");
		}
	}
	
	public static void makeCalDates()
	{
		Calendar c = Calendar.getInstance();
		c.setFirstDayOfWeek(Calendar.MONDAY);
        dates = new ArrayList<String>();

        Integer i, mois, jour;
        for (i=0; i < 5; i++)
        {
        	mois = c.get(Calendar.MONTH)+1;
        	jour = c.get(Calendar.DAY_OF_MONTH);
        	dates.add(jours[c.get(Calendar.DAY_OF_WEEK)] +" "+ ((Integer)c.get(Calendar.DAY_OF_MONTH)).toString());
        	calDates.add(c.get(Calendar.YEAR)+"-"+(mois<10?"0":"")+mois.toString()+"-"+(jour<10?"0":"")+jour.toString());
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        // Un de plus pour la date/heure de fin
    	mois = c.get(Calendar.MONTH)+1;
    	jour = c.get(Calendar.DAY_OF_MONTH);
    	calDates.add(c.get(Calendar.YEAR)+"-"+(mois<10?"0":"")+mois.toString()+"-"+(jour<10?"0":"")+jour.toString());
	}
	
    public static ArrayAdapter<String> remplirHeuresSpinner(Activity a, int h, int hsid)
    {
    	Integer oldHeure = -1;
    	ArrayAdapter<String> heuresAdapter;
    	
        Spinner hs = (Spinner) a.findViewById(hsid);
    	if (hs.getSelectedItem() != null)
    	{
    		oldHeure = Integer.parseInt(((String) hs.getSelectedItem()).split("h")[0]);
    	}

        List<String> heures = new ArrayList<String>();
        for (Integer i=h; i < 24; i++)
        {
        	heures.add(i.toString()+"h - "+(i+4>23?((Integer)(i+4-24)).toString():i+4)+"h");
        }
		heuresAdapter = new ArrayAdapter<String>(
				a, android.R.layout.simple_spinner_item, heures);
		heuresAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		hs.setAdapter(heuresAdapter);
		if ((oldHeure != -1) && (h <= oldHeure))
		{
			hs.setSelection(oldHeure - h, true);
		}
		return heuresAdapter;
    }
	
	public static LinearLayout addVisuelChaine(String image, String name, Integer tag, View.OnClickListener o, Activity a)
	{
		LinearLayout il = new LinearLayout(a);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	params.gravity = (Gravity.CENTER);
    	params.setMargins(5,5,5,5);
		ImageView i = new ImageView(a);
        String filepath = Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+image;
        if (mNewBitmapAvailable)
        {
			WrapBitmap bmp = new WrapBitmap(filepath);
			bmp.setDensity(128);
			i.setImageBitmap(bmp.getBitmap());        	
        }
        else
        {
			Bitmap bmp = BitmapFactory.decodeFile(filepath);
			i.setImageBitmap(bmp);
        }
		i.setLayoutParams(params);
//		i.setTag(tag);
//        i.setOnClickListener(o);
        il.addView(i);
		TextView t = new TextView(a);
		t.setText(name);
		t.setTextSize(10);
		t.setGravity(Gravity.CENTER);
		il.addView(t);
		il.setTag(tag);
		il.setOnClickListener(o);
		il.setOrientation(LinearLayout.VERTICAL);
		il.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		il.setGravity(Gravity.CENTER_HORIZONTAL);
		return il;
	}
	
	public static void displayFavoris(final Activity a, OnClickListener o, int id, int itemSelected)
    {
		ArrayList<Favoris> listeFavoris = new ArrayList<Favoris>();
		List< Map<String,Object> > chainesToSelect;
		// -1 si une chaine a été enlevée / 1 si une chaine a été ajoutée / 0 si pas bougé
		// si suppression et ajout : 1
		ChainesDbAdapter mDbHelper = new ChainesDbAdapter(a);
		mDbHelper.open();
		Log.d(TAG,"getFavoris");

		listeFavoris.clear();

    	Cursor chainesIds = mDbHelper.getFavoris();
        if (chainesIds != null)
		{
			Favoris f;
			if (chainesIds.moveToFirst())
			{
				Cursor chaineCursor;
				int CI_progchannel_id = chainesIds.getColumnIndexOrThrow(ChainesDbAdapter.KEY_FAVORIS_ID);
				do
				{
					f = new Favoris();
					f.guidechaine_id = chainesIds.getInt(CI_progchannel_id);
					chaineCursor = mDbHelper.getGuideChaine(f.guidechaine_id);
					if ((chaineCursor != null) && (chaineCursor.moveToFirst()))
					{
						f.canal = chaineCursor.getInt(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL));
						f.name = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_NAME));
						f.image = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE));
						chaineCursor.close();
					}
					else
					{
						if (chaineCursor != null)
						{
							chaineCursor.close();							
						}
					}
					listeFavoris.add(f);
				} while (chainesIds.moveToNext());
			}
			// Ici on trie pour avoir les listes de programmes dans l'ordre des numéros de chaine
			Collections.sort(listeFavoris);

			// On créé l'horizontal scrollview en haut avec la liste des chaines favorites
			Iterator<Favoris> it = listeFavoris.iterator();
	    	LinearLayout csly = (LinearLayout) a.findViewById(id);
	    	csly.removeAllViews();
	    	csly.setGravity(Gravity.CENTER);
			while(it.hasNext())
			{
				final Favoris ff = it.next();
				if (ff.name != null)
				{
					csly.addView(addVisuelChaine(ff.image, ff.name, (Integer)ff.guidechaine_id, o, a));
				}
			}
	        if (itemSelected == -1)
	        {
		        chainesIds.close();
		        mDbHelper.close();
		        return;
	        }
	
			chainesToSelect = new ArrayList< Map<String,Object> >();
			Cursor allChaines = mDbHelper.getListChaines();
			if (allChaines != null)
			{
				if (allChaines.moveToFirst())
				{
					final int CI_image = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE);
					final int CI_canal = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL);
					final int CI_name = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_NAME);
					final int CI_id = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_ID);
					String image;
					Map<String,Object> map;
					for (it = listeFavoris.iterator(); it.hasNext();)
					{
						f = it.next();
						while ((!allChaines.isAfterLast()) &&
								(allChaines.getInt(CI_canal) != f.canal))
						{
							image = allChaines.getString(CI_image);
							map = new HashMap<String,Object>();
							if (image.length() > 0)
							{
								map.put(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+image);
							}
							else
							{
								map.put(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, R.drawable.chaine_vide);
							}
							map.put(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL, allChaines.getInt(CI_canal));
							map.put(ChainesDbAdapter.KEY_GUIDECHAINE_NAME, allChaines.getString(CI_name));
							map.put(ChainesDbAdapter.KEY_GUIDECHAINE_ID, allChaines.getInt(CI_id));
							chainesToSelect.add(map);
							allChaines.moveToNext();
						}
						allChaines.moveToNext();
					}
					// On doit refaire un tour pour les chaines non sélectionnées
					// dont le numéro est > au numéro de la dernière chaine des favoris
					while (!allChaines.isAfterLast())
					{
						image = allChaines.getString(CI_image);
						map = new HashMap<String,Object>();
						if (image.length() > 0)
						{
							map.put(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+image);
						}
						else
						{
							map.put(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, R.drawable.chaine_vide);
						}
						map.put(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL, allChaines.getInt(CI_canal));
						map.put(ChainesDbAdapter.KEY_GUIDECHAINE_NAME, allChaines.getString(CI_name));
						map.put(ChainesDbAdapter.KEY_GUIDECHAINE_ID, allChaines.getInt(CI_id));
						chainesToSelect.add(map);
						allChaines.moveToNext();							
					}
			        SimpleAdapter mList = new SimpleAdapter(
			        		a, chainesToSelect, R.layout.guide_favoris_row, 
			        		new String[] {ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, ChainesDbAdapter.KEY_GUIDECHAINE_NAME, ChainesDbAdapter.KEY_GUIDECHAINE_ID},
			        		new int[] {R.id.ImageViewFavoris, R.id.TextViewFavoris, R.id.HiddenTextView});
			        ((ListActivity) a).setListAdapter(mList);
			        if (itemSelected > 0)
			        {
			        	((ListActivity) a).setSelection(itemSelected);
			        }
				}
				allChaines.close();
			}
	    }
        mDbHelper.close();
	}
}
