package org.madprod.freeboxmobile.guide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.app.Activity;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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
	
	public static void makeCalDates()
	{
		Calendar c = Calendar.getInstance();
		c.setFirstDayOfWeek(Calendar.MONDAY);
        dates = new ArrayList<String>();

        Integer i, mois, jour;
        for (i=0; i < 7; i++)
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

    /**
     * Calcule la fin de la plage (de 4h) à récupérer sur le serveur
     * @param selectedHeure : heure de début
     * @param datefin : datesSpinner.getSelectedItemId();
     */
	public static String setFinDateHeure_unused(String selectedHeure, long datefin)
	{
		String sdatefin;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		Date dtdeb;
		try
		{
			dtdeb = sdf.parse(selectedHeure);
			long heurefin = dtdeb.getHours()+4;
//			long datefin = datesSpinner.getSelectedItemId();
			if (heurefin > 23)
			{
				heurefin -= 24;
				sdatefin = GuideUtils.calDates.get((int) (datefin+1));
			}
			else
			{
				sdatefin = GuideUtils.calDates.get((int) (datefin));
			}
			return (sdatefin+" "+(heurefin<10?"0"+heurefin:heurefin)+":00:00");
		}
		catch (ParseException e)
		{
			Log.e(TAG,"setfinDateHeure pb decode heure "+ e.getMessage());
			e.printStackTrace();
			return "";
		}
	}
	
	public static LinearLayout addVisuelChaine(String image, String name, Integer tag, View.OnClickListener o, Activity a)
	{
		LinearLayout il = new LinearLayout(a);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	params.gravity = (Gravity.CENTER);
    	params.setMargins(5,5,5,5);
        String filepath = Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+image;
		Bitmap bmp = BitmapFactory.decodeFile(filepath);
		ImageView i = new ImageView(a);
		i.setImageBitmap(bmp);
		i.setLayoutParams(params);
		i.setTag(tag);
        i.setOnClickListener(o);
        il.addView(i);
		TextView t = new TextView(a);
		t.setText(name);
		t.setTextSize(8);
		t.setGravity(Gravity.CENTER);
		il.addView(t);
		il.setOrientation(LinearLayout.VERTICAL);
		il.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		il.setGravity(Gravity.CENTER_HORIZONTAL);
		return il;
	}
}
