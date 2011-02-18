package org.madprod.freeboxmobile.guide;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.ServiceUpdateUIListener;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.WakefullIntentService;
import org.madprod.freeboxmobile.home.HomeListActivity;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;
import org.madprod.freeboxmobile.pvr.PvrNetwork;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class GuideCheck extends WakefullIntentService implements GuideConstants
{
    public static ServiceUpdateUIListener UI_UPDATE_LISTENER;
    private static Activity CUR_ACTIVITY;

    private static ProgressDialog myProgressDialog = null;

	public GuideCheck()
	{
		super("GuideCheck");
		Log.i(TAG,"GuideCheck constructor");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.i(TAG,"GuideCheck onHandleIntent ");

		ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
	        
		if (cm == null || cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected())
		{
			Log.d(TAG, "airplane mode");
			super.onHandleIntent(intent);
			return;
		}

		String selectedDate = intent.getStringExtra("selectedDate");

		File log = new File(Environment.getExternalStorageDirectory()+DIR_FBM, file_log);
		try
		{
			BufferedWriter out = new BufferedWriter (new FileWriter(log.getAbsolutePath(), true));
			out.write("guide_start : ");
			out.write(new Date().toString());
			out.write("\n");
			out.close();
		}
		catch (IOException e)
		{
			Log.e(TAG, "Exception appending to log file ",e);
		}

		if (selectedDate == null)
		{
			GuideUtils.makeCalDates();
			
			FBMHttpConnection.initVars(null, getBaseContext());
	
			ChainesDbAdapter mDbHelper = new ChainesDbAdapter(this);
	        mDbHelper.open();
            Log.d(TAG,"Nettoyage des anciens programmes effacés : "+mDbHelper.deleteOldProgs());
            Log.d(TAG,"Nettoyage de l'ancienne historique : "+mDbHelper.deleteOldHisto());

	        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == true)
	        {
		    	if (mDbHelper.getNbFavoris() == 0)
		    	{
		    		getData(this, null, 4, true, false); // To get chaines logos
		    		showProgress(R.drawable.fm_guide_tv, "Guide TV", "Téléchargement du programme TV...");
		        	new PvrNetwork(false, false).getData(); // to get favoris list
		        	closeProgress();
		    	}
	        }
			String dateToGet = GuideUtils.calDates.get((GuideUtils.calDates.size() - 1));
			Log.d(TAG, "last date : "+dateToGet);
			getData(this, dateToGet, 24, false, true);
			dateToGet = GuideUtils.calDates.get((GuideUtils.calDates.size() - 2));
			getData(this, dateToGet, 24, false, true);
			int nbJours = GuideUtils.calDates.size() - 3;
			while (nbJours >= 0)
			{
				dateToGet = GuideUtils.calDates.get((nbJours--));
				getData(this, dateToGet, 24, false, false);			
			}
			mDbHelper.close();
		}
		else
		{
			getData(this, selectedDate, 24, false, true);
		}

		if (UI_UPDATE_LISTENER != null)
		{
			UI_UPDATE_LISTENER.updateUI();
		}
		
		try
		{
			BufferedWriter out = new BufferedWriter (new FileWriter(log.getAbsolutePath(), true));
			out.write("guide_end : ");
			out.write(new Date().toString());
			out.write("\n");
			out.close();
		}
		catch (IOException e)
		{
			Log.e(TAG, "Exception appending to log file ",e);
		}

		Log.d(TAG, "GuideCheck END");
		closeProgress();
		super.onHandleIntent(intent);
	}

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (myProgressDialog != null)
    		myProgressDialog.dismiss();
    	myProgressDialog = null;
    	Log.d(TAG, "GuideCheck on Destroy");
    }

	/**
	 * Défini la callback à appeler pour rafraichir l'activité une fois les données à jour
	 * Si la callback est définie (mais vide), les progress apparaissent en cas de mise à jour en background
	 * Sinon non (en gros, il vaut mieux la définir dans les activités du guide, et ne pas la définir en dehors)
	 * @param l
	 */
	public static void setUpdateListener(ServiceUpdateUIListener l)
	{
		UI_UPDATE_LISTENER = l;
	}

	/**
	 * Défini l'activité qui sera utilisée pour les boites de dialogue
	 * @param activity
	 */
	public static void setActivity(Activity activity)
	{
		Log.d(TAG, "setActivity : "+activity);
		if (activity == null)
		{
			UI_UPDATE_LISTENER = null;
			if (myProgressDialog != null)
			{
				myProgressDialog.dismiss();
			}
		}
		else
		{
			if (myProgressDialog != null)
			{
				myProgressDialog.show();
			}
		}
		CUR_ACTIVITY = activity;
	}

	/**
	 * Rafraichi tous les programmes (et les favoris + logos chaines si besoin)
	 * pour la date donnée. Si date == null, rafraichi toute la semaine
	 * @param selectedDate date à rafraichir, null => toute la semaine
	 */
    public static void refresh(String selectedDate)
    {
		Intent i = new Intent(CUR_ACTIVITY, GuideCheck.class);
		i.putExtra("selectedDate", selectedDate);
//		WakefullIntentService.acquireStaticLock(CUR_ACTIVITY);
		CUR_ACTIVITY.startService(i);
    }

	/**
	 * set periodical guide download timer
	 * @param c : context
	 */
	public static void setTimer(Context c)
	{
		AlarmManager amgr = (AlarmManager) c.getSystemService(HomeListActivity.ALARM_SERVICE);
		Intent i = new Intent(c, OnGuideAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(c, 0, i, 0);
		amgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, /*System.currentTimeMillis(),*/SystemClock.elapsedRealtime(),AlarmManager.INTERVAL_HALF_DAY/* AlarmManager.INTERVAL_DAY*/, pi);
		Log.i(TAG, "GuideTimer set");
	}

	private int getData(Context context, String datetime, Integer duree_h, boolean getChaines, boolean forceRefresh)
	{
    	Log.d(TAG,"GUIDESYNCDATA START "+datetime+" "+duree_h+" "+getChaines+" "+forceRefresh);
		JSONObject jObject;
		JSONObject jChannelsObject;
		JSONObject jChannelObject;
		JSONObject jHorairesObject;
		JSONObject jHoraireObject;
		int channel_id;
		ChainesDbAdapter db;
		int courant = 0;
		int max = 0;
		boolean ok = true;
	
		showProgress(R.drawable.fm_guide_tv, "Guide TV", "Téléchargement du programme TV...");
		
		db = new ChainesDbAdapter(context);
		db.open();
	
		if	(
				(
					(forceRefresh == false) &&
					(getChaines == false)
				) &&
				(
					(
						(duree_h == 4) &&
						(db.isHistoGuidePresent(datetime) > 0)
					) ||
					(
						(duree_h == 24) &&
						(db.isDayHistoGuidePresent(datetime) > 0)
					)
				)
			)
		{
			// On a déjà les données, on les charge donc pas
			Log.d(TAG,"ON A DEJA LES DONNEES");
			db.close();
			closeProgress();
			return DATA_FROM_CACHE;
		}
	    List<NameValuePair> param = new ArrayList<NameValuePair>();
	    param.add(new BasicNameValuePair("ajax","get_chaines"));
	    if (datetime != null)
	    {
	        if (duree_h == 24)
	        {
	            param.add(new BasicNameValuePair("date", datetime+" 00:00:00"));	
	        }
	        else
	        {
	    		param.add(new BasicNameValuePair("date", datetime));
	        }
	        param.add(new BasicNameValuePair("duree_h", duree_h.toString()));
	    }
	    else // Nécessaire pour récuperer certaines données lorsqu'on ne veut pas récuperer les programmes
	    {
	    	param.add(new BasicNameValuePair("date", "2010-01-01 00:00:00"));
	    }
	    Log.d(TAG, "ICI:"+param);
	    String resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthXmlRequest(MAGNETO_URL, param, true, true, "UTF8"));
	    if (resultat != null)
	    {
	    	Log.d(TAG,"Guide Network start "+new Date());
	    	try
	    	{
				jObject = new JSONObject(resultat);
				if (jObject.has("redirect"))
				{
					Log.d(TAG,"Authentification expirée");
					if (FBMHttpConnection.connect() == CONNECT_CONNECTED)
					{
						resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthXmlRequest(MAGNETO_URL, param, true, true, "UTF8"));
					}
					if ((FBMHttpConnection.connect() != CONNECT_CONNECTED) || (resultat == null))
					{
						db.close();
						closeProgress();
						Log.d(TAG,"DATA NOT DOWNLOADED");
						return (DATA_NOT_DOWNLOADED);
					}
					jObject = new JSONObject(resultat);
				}
				if (datetime != null)
				{
					Log.d(TAG, "Starting progs download...");
					jChannelsObject = jObject.getJSONObject("progs");
					max = jChannelsObject.length();
					if (myProgressDialog != null)
					{
						Log.d(TAG, "Setting MAX "+max);
						updateProgress("Actualisation des programmes TV pour "+max+" chaînes favorites...", max, 0);
					}
					String channelId;
					db.mDb.beginTransaction();
					try
					{
						for (Iterator <String> it = jChannelsObject.keys() ; it.hasNext() ;)
						{
							if (myProgressDialog != null)
							{
								updateProgress(datetime+"\n\nActualisation des programmes TV pour "+max+" chaînes favorites...", max, courant++);
							}
							channelId = it.next();
							jHorairesObject = jChannelsObject.getJSONObject(channelId);
							for (Iterator <String> jt = jHorairesObject.keys(); jt.hasNext() ;)
							{
								channel_id = Integer.parseInt(channelId);
								String datetime_deb = jt.next();
								// Je ne sais pas à quoi correspond le channel_id 173 mais les programmes sont vides
								// et il n'y a pas de chaine qui corresponde dans la liste des chaines
								// donc...
								jHoraireObject = jHorairesObject.getJSONObject(datetime_deb);
								if (!(db.isProgrammePresent(channel_id, datetime_deb)) && (channel_id != 173))
								{									
//									jHoraireObject = jHorairesObject.getJSONObject(datetime_deb);
									db.createProgramme(
										Integer.parseInt(getJSONString(jHoraireObject, "genre_id")),
										channel_id,
										getJSONString(jHoraireObject,"resum_s"),
										getJSONString(jHoraireObject,"resum_l"),
										getJSONString(jHoraireObject,"title").replaceAll("&amp;","&"),
										Integer.parseInt(getJSONString(jHoraireObject,"duree")),
										datetime_deb,
										getJSONString(jHoraireObject,"datetime_fin")
										);
								}
								/*
								else
								{
									if (channel_id != 173)
									{
										try
										{
											Log.d(TAG, "UPDATE : "+Integer.parseInt(getJSONString(jHoraireObject, "id")));
										}
										catch (Exception e)
										{
											Log.d(TAG, "UPDATE : no ID ! "+getJSONString(jHoraireObject, "id"));
										}
										db.updateProgramme(
												Integer.parseInt(getJSONString(jHoraireObject, "genre_id")),
												channel_id,
												getJSONString(jHoraireObject,"resum_s"),
												getJSONString(jHoraireObject,"resum_l"),
												getJSONString(jHoraireObject,"title").replaceAll("&amp;","&"),
												Integer.parseInt(getJSONString(jHoraireObject,"duree")),
												datetime_deb,
												getJSONString(jHoraireObject,"datetime_fin")
												);
									}
								}
*/
								// TODO : sinon rafraichir ? (ci-dessus)
								
								// On affiche les émissions qui auraient un genre non supporté (pour debug)
								
								Integer g;
								try
								{
									g = Integer.parseInt(getJSONString(jHoraireObject, "genre_id"));
									if (
											(g > 0) &&
											((g < genres.length) && (genres[g].length() == 0)) ||
											(g >= genres.length)
										)
									{
										Log.d(TAG, "Genre inconnu "+g+": "+getJSONString(jHoraireObject,"title").replaceAll("&amp;","&"));
									}
								}
								catch (Exception e)
								{
									
								}
								
							}
						}
						if (duree_h == 4)
						{
							db.createHistoGuide(datetime);
						}
						else
						{
							db.createDayHistoGuide(datetime);
							db.createHistoGuide(datetime+" 00:00:00");
							db.createHistoGuide(datetime+" 01:00:00");
							db.createHistoGuide(datetime+" 02:00:00");
							db.createHistoGuide(datetime+" 03:00:00");
							db.createHistoGuide(datetime+" 04:00:00");
							db.createHistoGuide(datetime+" 05:00:00");
							db.createHistoGuide(datetime+" 06:00:00");
							db.createHistoGuide(datetime+" 07:00:00");
							db.createHistoGuide(datetime+" 08:00:00");
							db.createHistoGuide(datetime+" 09:00:00");
							db.createHistoGuide(datetime+" 10:00:00");
							db.createHistoGuide(datetime+" 11:00:00");
							db.createHistoGuide(datetime+" 12:00:00");
							db.createHistoGuide(datetime+" 13:00:00");
							db.createHistoGuide(datetime+" 14:00:00");
							db.createHistoGuide(datetime+" 15:00:00");
							db.createHistoGuide(datetime+" 16:00:00");
							db.createHistoGuide(datetime+" 17:00:00");
							db.createHistoGuide(datetime+" 18:00:00");
							db.createHistoGuide(datetime+" 19:00:00");
							db.createHistoGuide(datetime+" 20:00:00");
							// Tester si isDayHistoGuidePresent(datetime jour précédent)
							// Si oui : db.createHistoGuide(datetimejourprécédent+ " 21-22-23"...);
							try
							{
								Date date = new SimpleDateFormat("yyy-MM-dd").parse(datetime);
								Calendar c = Calendar.getInstance();
								c.setFirstDayOfWeek(Calendar.MONDAY);
								c.setTime(date);
								c.add(Calendar.DAY_OF_MONTH, -1);
								String datetimehier = (c.get(Calendar.YEAR)+"-"+((c.get(Calendar.MONTH)+1)<10?"0":"")+(c.get(Calendar.MONTH)+1)+"-"+(c.get(Calendar.DAY_OF_MONTH)<10?"0":"")+c.get(Calendar.DAY_OF_MONTH));
								db.createHistoGuide(datetimehier+" 21:00:00");
								db.createHistoGuide(datetimehier+" 22:00:00");
								db.createHistoGuide(datetimehier+" 23:00:00");							
							}
							catch (ParseException e)
							{
								e.printStackTrace();
								Log.d(TAG, "Probleme parsing date");
							}
							
						}
						db.mDb.setTransactionSuccessful();
					}
					finally
					{
						db.mDb.endTransaction();
					}					
				}
				if (getChaines)
				{
					File file, filen;
					String image, canal;
					courant = 0;
					jChannelsObject = jObject.getJSONObject("chaines");
					max = jChannelsObject.length();
/*					dProgressMessage("Actualisation de la liste des "+max+" chaînes disponibles pour le Guide...",max);
					publishProgress(0);
*/
					db.mDb.beginTransaction();
					try
					{
						for (Iterator <String> it = jChannelsObject.keys() ; it.hasNext() ;)
						{
	//						publishProgress(courant++);
							String channelId = it.next();
							if (channelId.length() > 0)
							{
								channel_id = Integer.parseInt(channelId);
							}
							else
							{
								Log.d(TAG,"ChannelId == -1");
								channel_id = -1;
							}
							jChannelObject = jChannelsObject.getJSONObject(channelId);
							image = getJSONString(jChannelObject, "image");
							canal = getJSONString(jChannelObject, "canal");
							// TODO : si chaine déjà présente -> update
							
							// On teste si on a le fichier qui correspond à la chaine
					        file = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES, image);
					        if ((file.exists() == true) && (file.length() == 0))
					        {
					        	Log.d(TAG, "File size == 0, deleting... "+image);
					        	file.delete();
					        }
					        if (file.exists() == false)
					        {
								if (FBMHttpConnection.getFile(file, IMAGES_URL+image, null, false))
								{
									Log.d(TAG, "Downloading logo : "+image);
							        filen = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES, canal+".png");
									try
									{
										Log.d(TAG,"Copy file "+image);
										Utils.copyFile(file, filen);
									}
									catch (IOException e)
									{
										Log.d(TAG,"Impossible de copier "+image+" "+canal);
										e.printStackTrace();
									}
								}
					        }
					        
							if (db.isGuideChainePresent(channel_id) == 0)
							{
								db.createGuideChaine(
									Integer.parseInt(getJSONString(jChannelObject, "fbx_id")),
									channel_id,
									Integer.parseInt(canal),
									getJSONString(jChannelObject, "name"),
									image
									);
							}				        
						}
						db.mDb.setTransactionSuccessful();
					}
					finally
					{
						db.mDb.endTransaction();
					}
				}
			}
	    	catch (JSONException e)
	    	{
				Log.e(TAG,"JSONException ! "+e.getMessage());
				Log.d(TAG,resultat);
				e.printStackTrace();
				ok = false;
			}
	    }
	    else
	    {
	    	ok = false;
	    }
		Log.d(TAG,"GUIDESYNCDATA Network end "+new Date());
		db.close();
		closeProgress();
		if (ok)
		{
	    	return DATA_NEW_DATA;
		}
	    Log.d(TAG,"==> Impossible de télécharger les programmes");
	    return DATA_NOT_DOWNLOADED;
	}

	void closeProgress()
	{
    	if (myProgressDialog != null)
    	{
    		Log.d(TAG, "Progress Dialog close "+myProgressDialog);
			myProgressDialog.dismiss();
    		myProgressDialog = null;
    	}
    	else
    	{
    		Log.d(TAG, "Progress Dialog already NULL");
    	}
	}

	void updateProgress(final String message, final int max, final int pos)
	{
		if (CUR_ACTIVITY != null)
		{
			CUR_ACTIVITY.runOnUiThread(
				new Runnable()
				{
					public void run()
					{
						if (myProgressDialog != null)
						{
							if (pos != 0)
							{
								myProgressDialog.setProgress(pos);						
							}
							else
							{
								myProgressDialog.setMessage(message);
								myProgressDialog.setMax(max);
								myProgressDialog.setProgress(pos);
								if (CUR_ACTIVITY != null)
									myProgressDialog.show();
							}
						}
					}
				});
		}		
	}

	void showProgress(final int icon, final String title, final String message)
	{
		if (CUR_ACTIVITY != null)
		{
			CUR_ACTIVITY.runOnUiThread(
				new Runnable()
				{
					public void run()
					{
						closeProgress();
						myProgressDialog = new ProgressDialog(CUR_ACTIVITY);
			    		Log.d(TAG, "Progress Dialog open "+myProgressDialog);
						myProgressDialog.setIcon(icon);
						myProgressDialog.setTitle(title);
						myProgressDialog.setMessage(message);
						myProgressDialog.setCancelable(false);
						myProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						if (CUR_ACTIVITY != null) // CUR_ACTIVITY a pu passer à null entre temps
							myProgressDialog.show();
					}
				});
		}
	}

	protected int getJSONBoolean(JSONObject o, String key)
	{
		if (o.has(key))
		{
			try
			{
				return (o.getBoolean(key)?1:0);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	protected int getJSONInt(JSONObject o, String key)
	{
		if (o.has(key))
		{
			try
			{
				return o.getInt(key);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return 0;
	}

	protected String getJSONString(JSONObject o, String key)
	{
		if (o.has(key))
		{
			try
			{
				return o.getString(key);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return "";
	}
}
