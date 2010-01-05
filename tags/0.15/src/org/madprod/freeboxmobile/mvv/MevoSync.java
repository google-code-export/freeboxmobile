package org.madprod.freeboxmobile.mvv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.madprod.freeboxmobile.home.HomeActivity;
import org.madprod.freeboxmobile.HttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.ServiceUpdateUIListener;
import org.madprod.freeboxmobile.WakefullIntentService;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class MevoSync extends WakefullIntentService implements MevoConstants
{
	private static final String mevoUrl = "http://adsl.free.fr/admin/tel/";
	private static final String mevoListPage = "notification_tel.pl";

	private static int connectionStatus = CONNECT_NOT_CONNECTED;

    private static MevoDbAdapter mDbHelper;

    // For Service
    public static ServiceUpdateUIListener UI_UPDATE_LISTENER;
    private static Activity CUR_ACTIVITY;

    static ProgressDialog myProgressDialog = null;
    static AlertDialog myAlertDialog = null;

	static NotificationManager mNotificationManager = null;

	/* ------------------------------------------------------------------------
	 * CREATION ET GESTION DU SERVICE
	 * ------------------------------------------------------------------------
	 */

	public MevoSync()
	{
		super("MevoSync");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		int newmsg = 0;

		Log.i(DEBUGTAG,"MevoSync onHandleIntent ");

		File log = new File(Environment.getExternalStorageDirectory()+DIR_FBM, "fbm.log");
		try
		{
			BufferedWriter out = new BufferedWriter (new FileWriter(log.getAbsolutePath(), true));
			out.write("start: ");
			out.write(new Date().toString());
			out.write("\n");
			out.close();
		}
		catch (IOException e)
		{
			Log.e(DEBUGTAG, "Exception appending to log file ",e);
		}

		mDbHelper = new MevoDbAdapter(this);

		HttpConnection.setVars(
				getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null),
				getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_PASSWORD, null)
				);
		newmsg = checkUpdate();
		if (newmsg > 0)
		{
			_initNotif(newmsg);
			if (UI_UPDATE_LISTENER != null)
			{
				UI_UPDATE_LISTENER.updateUI();
			}
		}

		try
		{
			BufferedWriter out = new BufferedWriter (new FileWriter(log.getAbsolutePath(), true));
			out.write("end: ");
			out.write(new Date().toString());
			out.write("\n\n");
			out.close();
		}
		catch (IOException e)
		{
			Log.e(DEBUGTAG, "Exception appending to log file ",e);
		}

		super.onHandleIntent(intent);
	}

	public static void setActivity(Activity activity)
	{
		CUR_ACTIVITY = activity;
		if (activity == null)
		{
			if (myProgressDialog != null)
			{
				myProgressDialog.dismiss();
			}
			if (myAlertDialog != null)
			{
				myAlertDialog.dismiss();
			}
		}
		else
		{
			if (myProgressDialog != null)
			{
				myProgressDialog.show();
			}
			if (myAlertDialog != null)
			{
				myAlertDialog.show();
			}
			mNotificationManager= (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);
		}
	}

	public static void setUpdateListener(ServiceUpdateUIListener l)
	{
		UI_UPDATE_LISTENER = l;
	}

	@Override
	public void onCreate()
	{
		Log.i(DEBUGTAG,"MevoSync onCreate");
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		Log.i(DEBUGTAG,"MevoSync onDestroy");
		super.onDestroy();
	}

	public static void cancelNotif(int id)
	{
		if (mNotificationManager != null)
			mNotificationManager.cancel(id);
	}

	private void _initNotif(int newmsg)
	{
		int icon = R.drawable.icon_fbm;
		CharSequence tickerText;
		CharSequence contentText;

		if (mNotificationManager == null)
		{
			mNotificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
		
		if (newmsg > 1)
		{
			tickerText = getString(R.string.app_name)+" : "+newmsg+" "+getString(R.string.http_new_msgs);
			contentText = newmsg+" "+getString(R.string.http_new_msgs);
		}
		else
		{
			tickerText = "FreeboxMobile : 1 "+getString(R.string.http_new_msg);
			contentText = newmsg+" "+getString(R.string.http_new_msg);
		}
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = getString(R.string.app_name);

		Intent notificationIntent = new Intent(this, MevoActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.defaults |= Notification.DEFAULT_SOUND;
		mNotificationManager.notify(NOTIF_MEVO, notification);
	}

	public int checkUpdate()
	{
		int newmsg = -1;

		String login = getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_USER, null);
		String password = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_PASSWORD, null);
		if ((login != null) && (password != null) && (connectionStatus != CONNECT_LOGIN_FAILED))// && (id == null)
		{
			if (connectionStatus != CONNECT_CONNECTED)
			{
				if (CUR_ACTIVITY != null)
				{
					CUR_ACTIVITY.runOnUiThread(new Runnable()
						{
							public void run()
							{
								myProgressDialog = ProgressDialog.show(CUR_ACTIVITY, "Mon Compte Free", "Connexion en cours ...", true,false);
							}
						});
				}
			}
			else
				myProgressDialog = null;
			connectionStatus = HttpConnection.connectFree();
            if (myProgressDialog != null)
            {
            	myProgressDialog.dismiss();
            	myProgressDialog = null;
            }
			if (connectionStatus == CONNECT_CONNECTED)
	        {
				if (CUR_ACTIVITY != null)
				{
					CUR_ACTIVITY.runOnUiThread(new Runnable()
						{
							public void run()
							{
								myProgressDialog = ProgressDialog.show(CUR_ACTIVITY, "Mon Compte Free", "Vérification des nouveaux messages ...", true,false);
							}
						});
				}
				else
					myProgressDialog = null;
	       		newmsg = getMessageList();
	            if (myProgressDialog != null)
	            {
	            	myProgressDialog.dismiss();
	            	myProgressDialog = null;
	            }
	        }
			else
			{
				if (CUR_ACTIVITY != null)
				{
					CUR_ACTIVITY.runOnUiThread(new Runnable()
					{
						public void run()
						{
			            	HttpConnection.showError(CUR_ACTIVITY);
						}
					});
				}
			}
		}
		return newmsg;
	}

	/**
	 * Change timer to ms value (or cancel if ms == 0)
	 * @param ms : timer value in ms
	 * @param a : activity
	 */
	public static void changeTimer(int ms, Activity a)
	{
		AlarmManager amgr = (AlarmManager) a.getSystemService(HomeActivity.ALARM_SERVICE);
		Intent i = new Intent(a, OnMevoAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(a, 0, i, 0);
		if (ms != 0)
		{
			amgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), ms, pi);
			Log.i(DEBUGTAG, "Timer  changed to "+ms);
		}
		else
		{
			amgr.cancel(pi);
			Log.i(DEBUGTAG, "Timer canceled");			
		}
	}

	// ------------------------------------------------------------------------
	// FIN GESTION DU SERVICE
	// ------------------------------------------------------------------------
	
	
	// ------------------------------------------------------------------------
	// GESTION DU RESEAU
	// ------------------------------------------------------------------------

    /**
     * Vérifie s'il n'y a pas de nouveau messages sur les serveurs de Free
     * (et les download s'il y en a)
     * Doit être appelé à partir d'une Activity (qui aura appelé setActivity dans son onCreate()
     * 
     */
    public static void refresh()
    {
		connectionStatus = CONNECT_NOT_CONNECTED;
		Intent i = new Intent(CUR_ACTIVITY, MevoSync.class);
		WakefullIntentService.acquireStaticLock(CUR_ACTIVITY);
		CUR_ACTIVITY.startService(i);
    }

	public static void deleteMsg(String name, Activity a)
	{
		File file;
		Cursor curs;
		
		Log.d(DEBUGTAG, "deleteMsg "+name);
		if (mDbHelper == null)
		{
			mDbHelper = new MevoDbAdapter(CUR_ACTIVITY);
		}

		// On efface le fichier du message
		file = new File(Environment.getExternalStorageDirectory().toString()+DIR_MEVO,name);
		if (file.delete())
		{
			Log.d(DEBUGTAG, "Delete file ok");
		}
		else
		{
			Log.d(DEBUGTAG, "Delete file not ok");
		}

		// On efface le message du serveur de Free
		mDbHelper.open();
		curs = mDbHelper.fetchMessage(name);
		if (curs.moveToFirst() == false)
		{
			Log.d(DEBUGTAG, "delete : message non trouvé !!!!");
		}
		else
		{
			// Si le message est présent sur le serveur de Free
			if (!(curs.getString(curs.getColumnIndex(KEY_DEL)).equals("")))
			{
	    		String tel = curs.getString(curs.getColumnIndex(KEY_DEL));
	   			if (tel.indexOf("tel=")>-1)
	   			{
	   				tel = tel.substring(tel.indexOf("tel=")+4);
	   				if (tel.indexOf("&")>-1)
	   				{
	   					tel = tel.substring(0, tel.indexOf('&'));
	   				}
	    		}
	   			final String partURL = "&tel="+tel+"&fichier="+curs.getString(curs.getColumnIndex(KEY_NAME));

	   			// On lance la suppression sur le serveur dans un thread séparé car ca peut être long
/*				Thread t = new Thread(new Runnable() {
					@Override
		            public void run()
		        	{
*/						// (re)connection afin d'avoir un id et idt frais :)
			   			String url = mevoUrl+"efface_message.pl?id="+HttpConnection.getId()+"&idt="+HttpConnection.getIdt()+partURL;
						Log.d(DEBUGTAG,"Deleting message"+url);

						// On reconstitue l'url parceque id & idt ont peut etre changé
						if (HttpConnection.connectFree() == CONNECT_CONNECTED)
						{
							Log.d(DEBUGTAG, "Deleting on server");
							HttpConnection.getRequest(url, false);
						}
						else
							Log.d(DEBUGTAG, "NOT Deleting on server");
//		        	}
//		        });
//		        t.setName("FBM Delete Message");
//		        t.start();
			}
		}
		// Puis on marque le message comme effacé dans la base
		// (on l'efface pas à proprement dit de la base, ca pourrait servir pour un historique)
		mDbHelper.updateMessage(0, "", "", name);

		if (UI_UPDATE_LISTENER != null)
			UI_UPDATE_LISTENER.updateUI();

		mDbHelper.close();
       	curs.close();
	}

	public static int getMessageList()
	{

		String fullurl = mevoUrl + mevoListPage + "?id=" + HttpConnection.getId() + "&idt=" + HttpConnection.getIdt();
		Log.d(DEBUGTAG, "GET: " + fullurl);

		int newmsg = -1;
		try
		{
	    	BufferedReader br = HttpConnection.getRequest(fullurl, true);
			String s = " ";
			String status = null;
			String from = null;
			String when = null;
			String length = null;
			String priv = null;
			String link = null;
			String del = null;
			String name = null;
			int intstatus = -1;
			int presence = 0;
			Cursor curs;
			File file;
			
	        newmsg = 0;
	        file = new File(Environment.getExternalStorageDirectory().toString()+DIR_MEVO);
	        file.mkdirs();
	        mDbHelper.open();
	        mDbHelper.initTempValues();
			while ( (s=br.readLine())!= null && s.indexOf("Provenance") == -1)
			{
			}
			if (s.indexOf("Provenance")>-1)
			{
				while ((s=br.readLine())!= null && s.indexOf("</tbody>") == -1)
				{
					if (s.indexOf("<td") != -1)
					{
						if (s.indexOf("Pas de nouveau message") != -1)
							Log.d(DEBUGTAG,"Pas de nouveau message !");
						else
						{
							Log.d(DEBUGTAG,"MESSAGE");
							priv = s.substring(s.indexOf("<td"));
							priv = priv.substring(priv.indexOf(">")+1);
							status = priv.substring(0,priv.indexOf("<"));
							Log.d(DEBUGTAG,"->STATUS:"+status);
							priv = priv.substring(priv.indexOf("<td"));
							priv = priv.substring(priv.indexOf(">")+1);
							from = priv.substring(0,priv.indexOf("<"));
							Log.d(DEBUGTAG,"->FROM:"+from);
							priv = priv.substring(priv.indexOf("<td"));
							priv = priv.substring(priv.indexOf(">")+1);
							when = priv.substring(0,priv.indexOf("<"));
							Log.d(DEBUGTAG,"->WHEN:"+when);
							priv = priv.substring(priv.indexOf("<td"));
							priv = priv.substring(priv.indexOf(">")+1);
							length = priv.substring(0,priv.indexOf(" "));
							Log.d(DEBUGTAG,"->LENGTH:"+length);
							s = br.readLine();
							priv = s.substring(s.indexOf("href=")+6);
							link = priv.substring(0,priv.indexOf("'"));
							Log.d(DEBUGTAG,"->LINK:"+link);
							priv = priv.substring(priv.indexOf("href=")+6);
							del = priv.substring(0,priv.indexOf("'"));
							Log.d(DEBUGTAG,"->DEL:"+del);
							name = link.substring(link.indexOf("fichier=")+8);
							Log.d(DEBUGTAG,"->NAME:"+name);
							if (status.compareTo(STR_NEWMESSAGE) == 0)
							{
								intstatus = 0;
							}
							else
							{
								intstatus = 1;
							}
	
				    	    // Get the mevo file and store it on sdcard
					        file = new File(Environment.getExternalStorageDirectory().toString()+DIR_MEVO,name);
					        if (file.exists() == false)
					        {
								HttpConnection.getFile(file, mevoUrl+link);
					        }
					        presence = 4;
					        curs = mDbHelper.fetchMessage(name);
							if (curs.moveToFirst() == false)
				        	{
								// Store data in db if the message is not present in the db
				        		Log.d(DEBUGTAG,"STORING IN DB");
					        	mDbHelper.createMessage(intstatus, presence, from, when, link, del, Integer.parseInt(length), name);
					        	newmsg++;
				        	}
				        	else
				        	{
								// Update data in db if the message is already present in the db
				        		Log.d(DEBUGTAG,"UPDATING DB");
				        		mDbHelper.updateMessage(presence, link, del, name);
				        	}
				        	curs.close();
						}
					}
				}
				Log.d(DEBUGTAG,"fin extract");
			}
			else
			{
				Log.d(DEBUGTAG,"pb extract");
			}
			mDbHelper.close();
		}

		catch (Exception e)
		{
			Log.e(DEBUGTAG, "getMessageList : " + e.getMessage());
			e.printStackTrace();
		}
		Log.d(DEBUGTAG,"getmessage end "+newmsg);
		return newmsg;
 	}

	public static void showPdDelete()
	{
		myProgressDialog = ProgressDialog.show(CUR_ACTIVITY, "Suppression", "Suppression du serveur en cours...", true,false);
	}

	public static void dismissPd()
	{
		if (myProgressDialog != null)
		{
			myProgressDialog.dismiss();
			myProgressDialog = null;
		}
	}
}
