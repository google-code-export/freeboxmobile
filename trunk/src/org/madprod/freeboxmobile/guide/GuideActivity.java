package org.madprod.freeboxmobile.guide;

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

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideActivity extends ListActivity implements GuideConstants
{
	private static ProgressDialog progressDialog;
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
	
	// Cette liste sert à récupérer la date correspondant à l'indice du spinner
	private List<String> calDates = new ArrayList<String>();
	private ArrayList<GuideAdapter> ga = null;
	private ArrayList<ListeChaines> listesChaines = new ArrayList<ListeChaines>();

	String jours[] = {"", "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		ArrayAdapter<String> spinnerAdapter;
        super.onCreate(savedInstanceState);

        guideAct = this;
        FBMHttpConnection.initVars(this, null);
        FBMHttpConnection.FBMLog("GUIDE CREATE");
        
        setContentView(R.layout.guide);
        registerForContextMenu(getListView());

        mDbHelper = new ChainesDbAdapter(this);
        mDbHelper.open();

        // On rempli les spinners
        List<String> heures = new ArrayList<String>();
        for (Integer i=0; i < 24; i++)
        {
        	heures.add(i.toString()+"h - "+(i+4>23?((Integer)(i+4-24)).toString():i+4)+"h");
        }
        heuresSpinner = (Spinner) findViewById(R.id.HeuresSpinner);
		spinnerAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, heures);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		heuresSpinner.setAdapter(spinnerAdapter);

        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        heuresSpinner.setSelection(h);
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
		
		datesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int i, long l)
			{
//				selectedDate = calDates.get(i);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
        });
		heuresSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int i, long l)
			{
//				selectedHeure = (i<10?"0"+i:i)+":00:00";
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
        			int i = heuresSpinner.getSelectedItemPosition();
        			selectedHeure = (i<10?"0"+i:i)+":00:00";
        			setFinDateHeure();
        			new GuideActivityNetwork(selectedDate+" "+selectedHeure, false, true, false).execute((Void[])null);
        		}
        	}
        );
        getFromDb();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
    	if ((ga == null) || (ga.size() == 0))
    	{
    		new GuideActivityNetwork(sdf.format(new Date()), true, true, false).execute((Void[])null);    		
    	}
    	else
    		new GuideActivityNetwork(sdf.format(new Date()), false, true, false).execute((Void[])null);
    	setTitle(getString(R.string.app_name)+" Guide TV - "+FBMHttpConnection.getTitle());
    	setListAdapter(adapter);
    	
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
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
    }

    @Override
    public void onDestroy()
    {
        mDbHelper.close();
    	super.onDestroy();
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, GUIDE_OPTION_REFRESH, 0, R.string.guide_option_refresh).setIcon(android.R.drawable.ic_menu_rotate);
        menu.add(0, GUIDE_OPTION_SELECT, 1, R.string.guide_option_select).setIcon(android.R.drawable.ic_menu_add);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    		case GUIDE_OPTION_SELECT:
    			return true;
    		case GUIDE_OPTION_REFRESH:
    			new GuideActivityNetwork(selectedDate+" "+selectedHeure, false, true, true).execute((Void[])null);
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

    private void launchActivity(Class<?> cls, int pos)
    {
		Programme p = (Programme) adapter.getItem(pos);
		Intent i = new Intent(this, cls);
		i.putExtra(ChainesDbAdapter.KEY_PROG_TITLE, p.titre);
		i.putExtra(ChainesDbAdapter.KEY_PROG_CHANNEL_ID, p.channel_id);
		i.putExtra(ChainesDbAdapter.KEY_PROG_DUREE, p.duree);
		i.putExtra(ChainesDbAdapter.KEY_PROG_DATETIME_DEB, p.datetime_deb);
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL, p.canal);
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE, p.image);
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_NAME, p.chaine_name);
		i.putExtra(ChainesDbAdapter.KEY_PROG_RESUM_L, p.resum_l);
		i.putExtra(ChainesDbAdapter.KEY_GUIDECHAINE_ID, p.guidechaine_id);
        startActivity(i);
    }
    
	private void setFinDateHeure()
	{
		// TODO : code cleaning ici
		String sdatefin;
		long heurefin = heuresSpinner.getSelectedItemId() + 4;
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
    
	public void getFromDb()
	{
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

    	Cursor chainesIds = mDbHelper.getChainesProg();
    	startManagingCursor (chainesIds);
        if (chainesIds != null)
		{
			if (chainesIds.moveToFirst())
			{
				Cursor chaineCursor;
				ListeChaines l;
				boolean nochaine = false;
				int columnIndex = chainesIds.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_CHANNEL_ID);
				do
				{
					l = new ListeChaines();
					l.chaine_id = chainesIds.getInt(columnIndex);
					chaineCursor = mDbHelper.getGuideChaine(l.chaine_id);
					if ((chaineCursor != null) && (chaineCursor.moveToFirst()))
					{
						l.canal = chaineCursor.getInt(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL));
						l.name = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_NAME));
						l.image = chaineCursor.getString(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE));
						l.guidechaine_id = chaineCursor.getInt(chaineCursor.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_ID));
						chaineCursor.close();
					}
					else
						nochaine = true;

					l.programmes = mDbHelper.getProgrammes(l.chaine_id, selectedDate+" "+selectedHeure, finDateHeure);
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
	}
	
    public void refresh()
    {
    	FBMHttpConnection.FBMLog("Refreshing...");
    	GuideAdapter g;
		Iterator<GuideAdapter> it = ga.iterator();
		while(it.hasNext())
		{
			g = it.next();
			g.changeDateTime(selectedDate+" "+selectedHeure, finDateHeure);
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
    	private int heureCI;
    	
    	public GuideAdapter(Context c, ListeChaines l)
    	{
    		this.mContext = c;
    		this.listeChaines = l;
    		if (listeChaines.programmes != null)
    		{
	    		this.titreCI = listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_TITLE);
	    		this.dureeCI = listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DUREE);
	    		this.descCI = listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_RESUM_S);
	    		this.heureCI = listeChaines.programmes.getColumnIndexOrThrow(ChainesDbAdapter.KEY_PROG_DATETIME_DEB);
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
			p.datetime_deb = listeChaines.programmes.getString(heureCI);
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
				holder.heure.setText(convertDateTimeHoraire(listeChaines.programmes.getString(heureCI)));
				holder.duree.setText(convertDuree(listeChaines.programmes.getInt(dureeCI)));
				holder.desc.setText(listeChaines.programmes.getString(descCI));
			}
			return convertView;
		}

		public void changeDateTime(String d, String f)
		{
			listeChaines.programmes = mDbHelper.getProgrammes(listeChaines.chaine_id, d, f);
			guideAct.startManagingCursor(listeChaines.programmes);
    		notifyDataSetChanged();
    		notifyDataSetInvalidated();
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
    	if (progressDialog != null)
    	{
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    }
    
	private void displayAboutGuide()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name)+" - Guide TV");
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
				}
			}
			);
		d.show();
    }

    private class GuideActivityNetwork extends AsyncTask<Void, Integer, Boolean>
    {
    	private String debdatetime;
    	private boolean getChaines;
    	private boolean getProg;
    	private boolean forceRefresh;
    	
        protected void onPreExecute()
        {
        }

        protected Boolean doInBackground(Void... arg0)
        {
        	boolean res = new GuideNetwork(GuideActivity.this, debdatetime, getChaines, getProg, forceRefresh).getData();
        	if ((res) && (getChaines))
        	{
        		getFromDb();
        	}
        	return res;
        }
        
        protected void onProgressUpdate(Integer... progress)
        {
            showProgress(GuideActivity.this, progress[0]);
        }

        protected void onPostExecute(Boolean telechargementOk) {
        	if (telechargementOk == Boolean.TRUE) {
        	}
        	else {
        		// TODO : afficher erreur si il y a erreur
//        		ProgrammationActivity.afficherMsgErreur(activity.getString(R.string.pvrErreurTelechargementChaines), activity);
        	}
       		dismissPd();
        	refresh();
        }
        
        public GuideActivityNetwork(String d, boolean chaine, boolean prog, boolean force)
        {
       		debdatetime = d;
        	getChaines = chaine;
        	getProg = prog;
        	forceRefresh = force;
        	FBMHttpConnection.FBMLog("GUIDEACTIVITYNETWORK START "+d+" "+chaine+" "+prog);
        }
    }
}
