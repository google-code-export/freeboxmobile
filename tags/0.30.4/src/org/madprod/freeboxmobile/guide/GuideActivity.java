package org.madprod.freeboxmobile.guide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;
import org.madprod.freeboxmobile.pvr.ProgrammationActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideActivity extends ListActivity implements GuideConstants
{
	private static ProgressDialog progressDialog = null;
	public static String progressText;
	private static ChainesDbAdapter mDbHelper;
	private static Activity guideAct;
	private SectionedAdapter adapter;
	private Spinner heuresSpinner;
	private Spinner datesSpinner;
	private Button buttonOk;
	
	private String selectedHeure;
	private String selectedDate;
	private String finDateHeure;
	
	private static boolean mode_reduit;
	
	// Cette liste sert à récupérer la date correspondant à l'indice du spinner
	private List<String> calDates = new ArrayList<String>();
	private ArrayList<GuideAdapter> ga = null;
	private ArrayList<ListeChaines> listesChaines;

	String jours[] = {"", "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		ArrayAdapter<String> spinnerAdapter;
        super.onCreate(savedInstanceState);

        guideAct = this;
        FBMHttpConnection.initVars(this, null);
        FBMHttpConnection.FBMLog("GUIDE CREATE");
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		mode_reduit = mgr.getBoolean(KEY_MODE, false);
        
        setContentView(R.layout.guide);
        registerForContextMenu(getListView());

        mDbHelper = new ChainesDbAdapter(this);
        mDbHelper.open();
        FBMHttpConnection.FBMLog("Nettoyage des anciens programmes effacés : "+mDbHelper.deleteOldProgs());
        FBMHttpConnection.FBMLog("Nettoyage de l'ancienne historique : "+mDbHelper.deleteOldHisto());
        Calendar c = Calendar.getInstance();

        int h = c.get(Calendar.HOUR_OF_DAY);
        selectedHeure = (h<10?"0"+h:h)+":00:00";
        
        c.setFirstDayOfWeek(Calendar.MONDAY);
        datesSpinner = (Spinner) findViewById(R.id.DatesSpinner);
        List<String> dates = new ArrayList<String>();
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
    	selectedDate = calDates.get(0);
    	setFinDateHeure();
    	
		spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dates);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		datesSpinner.setAdapter(spinnerAdapter);

        datesSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				Calendar cal = Calendar.getInstance();
				String sdate = calDates.get(arg2).split("-")[2];
				int jour = Integer.decode(sdate);
				if (cal.get(Calendar.DAY_OF_MONTH) == jour)
				{
					remplirHeuresSpinner(cal.get(Calendar.HOUR_OF_DAY));
				}
				else
				{
					remplirHeuresSpinner(0);	
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
        });

		buttonOk = (Button) findViewById(R.id.GuideButtonOk);
        buttonOk.setOnClickListener(
        	new View.OnClickListener()
        	{
        		public void onClick(View view)
        		{
        			selectedDate = calDates.get(datesSpinner.getSelectedItemPosition());
        			String s = (String) heuresSpinner.getSelectedItem();
        			selectedHeure = s.split("h")[0];
        			if (selectedHeure.length() == 1)
        			{
        				selectedHeure = "0"+selectedHeure;
        			}
        			selectedHeure += ":00:00";
        			FBMHttpConnection.FBMLog("Item at position : "+s+" "+selectedHeure);
        			setFinDateHeure();
        			new GuideActivityNetwork(selectedDate+" "+selectedHeure, false, true, false, false).execute((Void[])null);
        		}
        	}
        );
        boolean nochaine = getFromDb();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
    	if ((mDbHelper.getNbChaines() == 0) || (nochaine))
    	{
    		new GuideActivityNetwork(sdf.format(new Date()), true, true, false, false).execute((Void[])null);    		
    	}
    	else
    		new GuideActivityNetwork(sdf.format(new Date()), false, true, false, false).execute((Void[])null);
    	setTitle(getString(R.string.app_name)+" Guide TV - "+FBMHttpConnection.getTitle());
    	
		if (!mgr.getString(KEY_SPLASH_GUIDE, "0").equals(this.getString(R.string.app_version)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_GUIDE, this.getString(R.string.app_version));
			editor.commit();
			displayAboutGuide();
		}
    }
	
    @Override
    public void onStart()
    {
    	super.onStart();
    	FBMHttpConnection.FBMLog("GUIDE onStart");
    }

    @Override
    public void onDestroy()
    {
        mDbHelper.close();
    	super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	menu.removeItem(GUIDE_OPTION_MODE);
        if (mode_reduit)
        {
        	menu.add(0, GUIDE_OPTION_MODE, 2, R.string.guide_option_mode_etendu).setIcon(android.R.drawable.ic_menu_view);
        }
        else
        {
        	menu.add(0, GUIDE_OPTION_MODE, 2, R.string.guide_option_mode_reduit).setIcon(android.R.drawable.ic_menu_view);
        }
        return true;
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, GUIDE_OPTION_REFRESH, 0, R.string.guide_option_refresh).setIcon(android.R.drawable.ic_menu_rotate);
        menu.add(0, GUIDE_OPTION_SELECT, 1, R.string.guide_option_select).setIcon(android.R.drawable.ic_menu_add);
        if (mode_reduit)
        	menu.add(0, GUIDE_OPTION_MODE, 2, R.string.guide_option_mode_etendu).setIcon(android.R.drawable.ic_menu_view);
        else
        	menu.add(0, GUIDE_OPTION_MODE, 2, R.string.guide_option_mode_reduit).setIcon(android.R.drawable.ic_menu_view);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    		case GUIDE_OPTION_SELECT:
    			startActivityForResult(new Intent(this, GuideChoixChainesActivity.class),0);
    			return true;
    		case GUIDE_OPTION_REFRESH:
				String s = selectedHeure.split(":")[0];
				selectedHeure = s+":00:00";
				FBMHttpConnection.FBMLog("Refresh manuel : "+selectedHeure);
				setFinDateHeure();
    			new GuideActivityNetwork(selectedDate+" "+selectedHeure, false, true, true, true).execute((Void[])null);
    			return true;
    		case GUIDE_OPTION_MODE:
    			mode_reduit = (mode_reduit ? false : true);
    			Editor editor = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).edit();
    			editor.putBoolean(KEY_MODE, mode_reduit);
    			editor.commit();
    	    	adapter.notifyDataSetChanged();
    			return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id)
	{
		super.onListItemClick(l, v, pos, id);
		launchActivity(GuideDetailsActivity.class, pos);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{
		AdapterContextMenuInfo info;

		super.onCreateContextMenu(menu, view, menuInfo);
		info = (AdapterContextMenuInfo) menuInfo;
		Programme p = (Programme) adapter.getItem((int)info.id);
	    menu.setHeaderTitle(p.titre);
	    menu.add(0, GUIDE_CONTEXT_ENREGISTRER, 0, "Enregistrer");
	    menu.add(0, GUIDE_CONTEXT_DETAILS, 1, "Détails");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	switch (item.getItemId())
    	{
			case GUIDE_CONTEXT_ENREGISTRER:
				launchActivity(ProgrammationActivity.class, (int)info.id);
			break;
			case GUIDE_CONTEXT_DETAILS:
				launchActivity(GuideDetailsActivity.class, (int)info.id);
			break;
    	}
    	return super.onContextItemSelected(item);
	}

	@Override
    protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString("selectedDate", selectedDate);
		outState.putString("selectedHeure", selectedHeure);
        outState.putString("finDateHeure", finDateHeure);
        outState.putInt("spinnerDate", datesSpinner.getSelectedItemPosition());
        outState.putInt("spinnerHeure", heuresSpinner.getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
    	FBMHttpConnection.FBMLog("onRestore !");
/*
            dureeEmission.setText(savedInstanceState.getString("da"));
            nomEmission.setText(savedInstanceState.getString("nomEmission"));
            nomEmissionSaisi = savedInstanceState.getBoolean("nomEmissionSaisi");
            choosen_year_deb = savedInstanceState.getInt("choosen_year_deb");
            choosen_month_deb = savedInstanceState.getInt("choosen_month_deb");
            choosen_day_deb = savedInstanceState.getInt("choosen_day_deb");
            choosen_hour_deb = savedInstanceState.getInt("choosen_hour_deb");
            choosen_minute_deb = savedInstanceState.getInt("choosen_minute_deb");
*/    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	FBMHttpConnection.FBMLog("ON ACTIVITY RESULT : "+resultCode);
    	switch (resultCode)
    	{
    		// Si on a supprimé un favori sans en ajouter, pas besoin d'une mise à jour réseau
    		case -1 :
    	    	FBMHttpConnection.FBMLog("RESULT SUPPR");
    			getFromDb();
    			break;
    		// Si on a ajouté un favori -> mise à jour réseau
    		case 1:
    	    	FBMHttpConnection.FBMLog("RESULT ADD");
    	    	new GuideActivityNetwork(selectedDate+" "+selectedHeure, false, true, true, true).execute((Void[])null);
    	    break;
    	    default:
    	    break;
    	}
    }
    
    private void remplirHeuresSpinner(int h)
    {
    	Integer oldHeure = -1;
    	
    	if (heuresSpinner != null)
    	{
    		oldHeure = Integer.decode(((String) heuresSpinner.getSelectedItem()).split("h")[0]);
    	}

        List<String> heures = new ArrayList<String>();
        for (Integer i=h; i < 24; i++)
        {
        	heures.add(i.toString()+"h - "+(i+4>23?((Integer)(i+4-24)).toString():i+4)+"h");
        }
        heuresSpinner = (Spinner) findViewById(R.id.HeuresSpinner);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, heures);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		heuresSpinner.setAdapter(spinnerAdapter);
		if ((oldHeure != -1) && (h <= oldHeure))
		{
			heuresSpinner.setSelection(oldHeure - h, true);
		}
    }
    
    private void launchActivity(Class<?> cls, int pos)
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

	private void setFinDateHeure()
	{
		String sdatefin;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		Date dtdeb;
		try
		{
			dtdeb = sdf.parse(selectedHeure);
			long heurefin = dtdeb.getHours()+4;
			long datefin = datesSpinner.getSelectedItemId();
			if (heurefin > 23)
			{
				heurefin -= 24;
				sdatefin = calDates.get((int) (datefin+1));
			}
			else
			{
				sdatefin = calDates.get((int) (datefin));
			}
			finDateHeure = sdatefin+" "+(heurefin<10?"0"+heurefin:heurefin)+":00:00";
		}
		catch (ParseException e)
		{
			FBMHttpConnection.FBMLog("setfinDateHeure pb decode heure "+ e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
	/**
	 * getFromDb
	 * @return true if there is no chaine in db
	 */
	public boolean getFromDb()
	{
		boolean nochaine = false;

		FBMHttpConnection.FBMLog("getFromDb");
	    adapter = new SectionedAdapter()
	    {
	    	protected View getHeaderView(String caption, Bitmap bmp, int index, View convertView, ViewGroup parent)
	    	{
	    		LinearLayout header = (LinearLayout) convertView;
	    		if (convertView == null)
	    		{
					header = (LinearLayout)getLayoutInflater().inflate(R.layout.guide_list_header, null);
	    		}
	    		TextView result = (TextView)header.findViewById(R.id.TextViewHeader);
	    		ImageView logo = (ImageView)header.findViewById(R.id.ImageViewHeader);
				if (bmp != null)
				{
					logo.setImageBitmap(bmp);
				}
				else
				{
					logo.setVisibility(View.GONE);
				}
	    		result.setText(caption);
	    		return(header);
	    	}    	
	    };
	    listesChaines = new ArrayList<ListeChaines>();
    	Cursor chainesIds = mDbHelper.getChainesProg();
    	startManagingCursor (chainesIds);
        if (chainesIds != null)
		{
			if (chainesIds.moveToFirst())
			{
				Cursor chaineCursor;
				ListeChaines l;
				int columnIndex = chainesIds.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_CHANNEL_ID);
				do
				{
					l = new ListeChaines();
					l.chaine_id = chainesIds.getInt(columnIndex);
					chaineCursor = mDbHelper.getGuideChaine(l.chaine_id);
					if ((chaineCursor != null))
					{
						if (chaineCursor.moveToFirst())
						{
							l.canal = chaineCursor.getInt(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL));
							l.name = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_NAME));
							l.image = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE));
							l.guidechaine_id = chaineCursor.getInt(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_ID));
							chaineCursor.close();
						}
						else
						{
							nochaine = true;
						}
					}
					else
						nochaine = true;

			        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			        String datetoget = selectedDate+" "+selectedHeure;
			        try
			        {
						Date dselect = sdf.parse(datetoget);
				        if (dselect.before(new Date()))
				        {
				        	datetoget = sdf.format(new Date());
				        	selectedDate = datetoget.split(" ")[0];
				        	selectedHeure = datetoget.split(" ")[1];
				        }
					}
			        catch (ParseException e)
			        {
			        	FBMHttpConnection.FBMLog("GUIDEACTIVITY : pb parse date "+e.getMessage());
						e.printStackTrace();
					}
					l.programmes = mDbHelper.getProgrammes(l.chaine_id, datetoget, finDateHeure);
					startManagingCursor(l.programmes);
					listesChaines.add(l);
				} while (chainesIds.moveToNext());
				
				// TODO : si nochaine == true, il manque une chaine, lancer un téléchargement des chaines du guide
				if (nochaine == true)
				{
					FBMHttpConnection.FBMLog("IL MANQUE AU MOINS UNE CHAINE");
				}
				// Ici on trie pour avoir les listes de programmes dans l'ordre des numéros de chaine
				Collections.sort(listesChaines);
				
				// Puis on créé les différentes sous-listes (une par chaine)
				ga = new ArrayList<GuideAdapter>();
				Iterator<ListeChaines> it = listesChaines.iterator();
				String filepath;
				Bitmap bmp;
				while(it.hasNext())
				{
					l = it.next();
					GuideAdapter g = new GuideAdapter(this, l);
					ga.add(g);
			        filepath = Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+l.image;
					bmp = BitmapFactory.decodeFile(filepath);
					adapter.addSection(l.name+" ("+((Integer)l.canal).toString()+")",bmp,g);
				}
			}
		}
        setListAdapter(adapter);
        return nochaine;
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
				g.changeDateTime(selectedDate+" "+selectedHeure, finDateHeure);
			}
    	}
		adapter.notifyDataSetChanged();
    }
    
    public static class GuideAdapter extends BaseAdapter
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

		public void changeDateTime(String d, String f)
		{
			listeChaines.programmes = mDbHelper.getProgrammes(listeChaines.chaine_id, d, f);
			guideAct.startManagingCursor(listeChaines.programmes);
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
    
    private class ListeChaines implements Comparable<ListeChaines>
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
    
    public static void showProgress(Activity a, int progress)
    {
		if (progressDialog == null)
    	{
    		progressDialog = new ProgressDialog(a);
    		progressDialog.setIcon(R.drawable.fm_magnetoscope);
    		progressDialog.setTitle("Importation");
    		progressDialog.setMessage(progressText);
    		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    		progressDialog.setCancelable(false);
    		progressDialog.show();
        }
		progressDialog.setMessage(progressText);
        progressDialog.setProgress(progress);
    }

    public static void setPdMax(int max)
    {
    	progressDialog.setMax(max);
    }
    
    public static void dismissPd()
    {
    	FBMHttpConnection.FBMLog("GuideActivity Dismiss");
    	if (progressDialog != null)
    	{
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    }
    
	private void displayAboutGuide()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name)+" - GuideTV");
		d.setIcon(R.drawable.fm_guide_tv);
		d.setMessage(
			"Le guide est en version beta.\n\n"+
			"Ses fonctionnalités, son look et ses performances seront améliorés "+
			"dans les prochaines semaines.\n\n"+
			"Mais il est déjà pratique comme cela :)"
			);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					displayHelp();
				}
			}
			);
		d.show();
    }

	private void displayHelp()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name)+" - GuideTV");
		d.setIcon(R.drawable.fm_guide_tv);
		d.setMessage(
			"Pour ajouter ou supprimer des favoris au guide, utilisez l'option 'Gérez les favoris' disponible dans le menu.");
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

	private void displayError()
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

    private class GuideActivityNetwork extends AsyncTask<Void, Integer, Integer>
    {
    	private String debdatetime;
    	private boolean getChaines;
    	private boolean getProg;
    	private boolean forceRefresh;
    	private boolean refreshActivity;
    	
        protected void onPreExecute()
        {
        }

        protected Integer doInBackground(Void... arg0)
        {
        	return new GuideNetwork(GuideActivity.this, debdatetime, getChaines, getProg, 0, forceRefresh).getData();
        }
        
        protected void onProgressUpdate(Integer... progress)
        {
            showProgress(GuideActivity.this, progress[0]);
        }

        protected void onPostExecute(Integer result)
        {
        	if (((result != DATA_NOT_DOWNLOADED) && (getChaines)) || (refreshActivity))
        	{
        		getFromDb();
        	}
        	if (result == DATA_NOT_DOWNLOADED)
        	{
        		displayError();
        	}
       		dismissPd();
       		if ((!refreshActivity) && (!getChaines))
       		{
       			refresh();
       		}
        }
        
        /**
         * 
         * @param d
         * @param chaine
         * @param prog
         * @param force
         * @param refreshactivity : pour rafraichir toute l'activité après un onActivityResult du choix des favoris
         */
        public GuideActivityNetwork(String d, boolean chaine, boolean prog, boolean force, boolean refreshactivity)
        {
       		debdatetime = d;
        	getChaines = chaine;
        	getProg = prog;
        	forceRefresh = force;
        	refreshActivity = refreshactivity;
        	FBMHttpConnection.FBMLog("GUIDEACTIVITYNETWORK START "+d+" "+chaine+" "+prog);
        }
    }
}
