package org.madprod.freeboxmobile.guide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.WrapBitmap;
import org.madprod.freeboxmobile.guide.GuideConstants.Categorie;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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

public abstract class GuideUtils extends ListActivity implements Constants
{
	// Cette liste sert à récupérer la date correspondant à l'indice du spinner
	protected static List<String> calDates = new ArrayList<String>();
	protected static List<String> dates;
	private static boolean mNewBitmapAvailable;
	protected static boolean mode_reduit;
	protected static ChainesDbAdapter mDbHelper;
	protected SectionedAdapter adapter;
	protected ArrayList<GuideAdapter> ga = null;
	protected static String progressText;

	protected ArrayList<ListeChaines> listesChaines;
	protected static ArrayList<Categorie> categories;
	protected static String[] categoriesDialog;

	protected String selectedDate;
	protected String selectedHeure;
	protected String finDateHeure;
	protected int selectedChaine;

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
        if (calDates != null)
        {
        	calDates.clear();
        }
        if (dates != null)
        {
        	dates.clear();
        }
        for (i=0; i < 6; i++)
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
	
	public LinearLayout addVisuelChaine(String image, String name, Integer tag, View.OnClickListener o, Activity a)
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
			if (bmp != null)
			{
				bmp.setDensity(128);
				i.setImageBitmap(bmp.getBitmap());
			}
			else
			{
				Log.e(TAG, "Bitmap not found : "+filepath);
			}
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
	
	/**
	 * Affiche la scrollbar horizontale "barre de favoris"
	 * @param a activity
	 * @param o
	 * @param id
	 * @param itemSelected
	 */
	public void displayFavoris(final Activity a, OnClickListener o, int id, int itemSelected)
    {
		ArrayList<Favoris> listeFavoris = new ArrayList<Favoris>();
		List< Map<String,Object> > chainesToSelect;
		// -1 si une chaine a été enlevée / 1 si une chaine a été ajoutée / 0 si pas bougé
		// si suppression et ajout : 1
		ChainesDbAdapter mDbHelper = new ChainesDbAdapter(a);
		try
		{
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
	        chainesIds.close();
	        mDbHelper.close();
		}
		catch (Exception e)
		{
			Log.e(TAG, "GuideUtils : displayFavoris : "+e.getMessage());
		}
	}

	abstract protected boolean getFromDb();
	
    protected void chooseCategories()
    {
		setTheme(android.R.style.Theme_Black);
    	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    	boolean[] checked = new boolean[categories.size()];
    	for (int i = 0; i < categories.size(); i++)
    	{
    		checked[i] = categories.get(i).checked;
    	}
		alertDialog = new AlertDialog.Builder(this)
			.setMultiChoiceItems(categoriesDialog,
					checked,
					new DialogInterface.OnMultiChoiceClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which, boolean what)
						{
							Categorie c = categories.get(which);
							c.checked = what;
							categories.set(which,categories.get(which));						
						}
					})
			.setPositiveButton(getString(R.string.OK),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							setTheme(android.R.style.Theme_Light);
							getFromDb();
							refresh();
							dialog.dismiss();
						}
					}) 
        	.setTitle("Catégories à afficher dans le guide :")
            .setIcon(R.drawable.fm_guide_tv)
            .create();
		alertDialog.show();
    }

    protected void launchActivity(Class<?> cls, int pos)
    {
		Programme p = (Programme) adapter.getItem(pos);
		Intent i = new Intent(this, cls);
		i.putExtra(ChainesDbAdapter.KEY_PROG_TITLE, p.titre);
		i.putExtra(ChainesDbAdapter.KEY_PROG_CHANNEL_ID, p.channel_id);
		i.putExtra(ChainesDbAdapter.KEY_PROG_DUREE, p.duree);
		i.putExtra(ChainesDbAdapter.KEY_PROG_DATETIME_DEB, p.datetime_deb);
		i.putExtra(ChainesDbAdapter.KEY_PROG_DATETIME_FIN, p.datetime_fin);
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL, p.canal);
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, p.image);
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_NAME, p.chaine_name);
		i.putExtra(ChainesDbAdapter.KEY_PROG_RESUM_L, p.resum_l);
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_ID, p.guidechaine_id);
        startActivity(i);
    }

    public static class Programme
    {
    	public String image;
		public int channel_id;
		public int guidechaine_id;
    	public int duree;
    	public String datetime_deb;
    	public String datetime_fin;
    	public String titre;
    	public int canal;
    	public String chaine_name;
    	public String resum_l;
    }
    
    protected class ListeChaines implements Comparable<ListeChaines>
    {
    	public int chaine_id;
    	public int guidechaine_id;
    	public int canal;
    	public String name;
    	public String image;
    	public Cursor programmes;
    	
		@Override
		public int compareTo(ListeChaines another)
		{
			return (canal - another.canal);
		}
    }

	protected void displayHelp()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name)+" - GuideTV");
		d.setIcon(R.drawable.fm_guide_tv);
		d.setMessage(
			"Pour filtrer les programmes par catégorie, utilisez l'option 'Choisir les catégories' disponible dans le menu.");
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			}
			);
		d.show();
    }

	protected void displayError()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name)+" - Guide TV");
		d.setMessage("Problème réseau, veuillez réessayer.");
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			}
			);
		d.show();
    }
	
	protected static class GuideAdapter extends BaseAdapter
    {
    	private Context mContext;
    	private ListeChaines listeChaines;
    	private int titreCI;
    	private int dureeCI;
    	private int descCI;
    	private int heuredebCI;
    	private int heurefinCI;
    	
    	public GuideAdapter(Context c, ListeChaines l)
    	{
    		this.mContext = c;
    		this.listeChaines = l;
    		if (listeChaines.programmes != null)
    		{
	    		this.titreCI = listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_TITLE);
	    		this.dureeCI = listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DUREE);
	    		this.descCI = listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_RESUM_S);
	    		this.heuredebCI = listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DATETIME_DEB);
	    		this.heurefinCI = listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DATETIME_FIN);
    		}
    	}
    	
		@Override
		public int getCount()
		{
			if (listeChaines.programmes != null)
				return listeChaines.programmes.getCount();
			else
				return 0;
		}

		@Override
		public Object getItem(int position)
		{
			Programme p = new Programme();
			listeChaines.programmes.moveToPosition(position);
			p.channel_id = listeChaines.programmes.getInt(listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_CHANNEL_ID));
			p.datetime_deb = listeChaines.programmes.getString(heuredebCI);
			p.datetime_fin = listeChaines.programmes.getString(heurefinCI);
			p.duree = listeChaines.programmes.getInt(dureeCI);
			p.titre = listeChaines.programmes.getString(titreCI);
			p.canal = listeChaines.canal;
			p.chaine_name = listeChaines.name;
			p.resum_l = listeChaines.programmes.getString(listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_RESUM_L));
			p.image = listeChaines.image;
			p.guidechaine_id = listeChaines.guidechaine_id;
			return p;
		}

		@Override
		public long getItemId(int position)
		{
			return listeChaines.programmes.getInt(listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_CHANNEL_ID));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder	holder;
			
			if (listeChaines.programmes != null)
			{
				listeChaines.programmes.moveToPosition(position);
				if (convertView == null)
				{
					holder = new ViewHolder();
					LayoutInflater inflater = LayoutInflater.from(mContext);
					convertView = inflater.inflate(R.layout.guide_row, null);
					
					holder.titre = (TextView)convertView.findViewById(R.id.guideRowTitre);
					holder.heure = (TextView)convertView.findViewById(R.id.guideRowHeure);
					holder.desc = (TextView)convertView.findViewById(R.id.guideRowDesc);
			        holder.duree = (TextView)convertView.findViewById(R.id.guideRowDuree);
			        
			        convertView.setTag(holder);
				}
				else
				{
					holder = (ViewHolder)convertView.getTag();
				}
				holder.titre.setText(listeChaines.programmes.getString(titreCI));
				holder.heure.setText(convertDateTimeHoraire(listeChaines.programmes.getString(heuredebCI)));
				if (mode_reduit)
				{
					holder.duree.setVisibility(View.GONE);
					holder.desc.setVisibility(View.GONE);
				}
				else
				{
					holder.duree.setVisibility(View.VISIBLE);
					holder.desc.setVisibility(View.VISIBLE);
					holder.duree.setText(convertDuree(listeChaines.programmes.getInt(dureeCI)));
					holder.desc.setText(listeChaines.programmes.getString(descCI));
				}
			}
			return convertView;
		}

		public void changeDateTime(String d, String f, Activity a)
		{
			listeChaines.programmes = mDbHelper.getProgrammes(listeChaines.chaine_id, d, f, categories);
			a.startManagingCursor(listeChaines.programmes);
		}
		
		public String convertDuree(int duree)
		{
			String ret = "";
			if (duree > 59)
			{
				Integer hour = duree / 60;
				ret += hour.toString();
				ret += "h";
				int m = duree - hour * 60;
				ret += (m < 10 ? "0"+m : ""+m);
			}
			else
			{
				ret = duree + "min"+(duree>1?"s":"");
			}
			return ret;
		}
		
		public String convertDateTimeHoraire(String org)
		{
			String[] datetime = org.split(" ");
			String[] temps = datetime[1].split(":");
			return temps[0]+":"+temps[1];
		}

		private class ViewHolder
		{
			TextView 	titre;
			TextView	desc;
			TextView	heure;
			TextView	duree;
		}
    }
    
    public void refresh()
    {
    	GuideAdapter g;
    	if (ga != null)
    	{
			Iterator<GuideAdapter> it = ga.iterator();
			while(it.hasNext())
			{
				g = it.next();
				g.changeDateTime(selectedDate+" 00:00:00", finDateHeure, this);
			}
    	}
		adapter.notifyDataSetChanged();
    }

}
