package org.madprod.freeboxmobile;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List; 

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException; 
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.message.BasicNameValuePair; 

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
 
/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class HttpConnection extends WakefullIntentService implements Constants
{
	private static String USER_AGENT = "FreeboxMobile (Linux; U; Android; fr-fr;)";
	 
	private static String password = null;
	private static String login = null;
	// Variables id et idt d'accès à MonCompteFree
	private static String id = null;
	private static String idt = null;

	private static final String serverUrl = "http://subscribe.free.fr/login/login.pl";
	private static final String mevoUrl = "http://adsl.free.fr/admin/tel/";
	private static final String mevoListPage = "notification_tel.pl";

    private static FreeboxMobileDbAdapter mDbHelper;

    // For Service
    public static ServiceUpdateUIListener UI_UPDATE_LISTENER;
    private static Activity MAIN_ACTIVITY;

    static ProgressDialog myProgressDialog = null;

    private static final int CONNECT_CONNECTED = 1;
    private static final int CONNECT_NOT_CONNECTED = 0;
    private static final int CONNECT_LOGIN_FAILED = -2;
    private static int connectionStatus = CONNECT_NOT_CONNECTED;

	/* ------------------------------------------------------------------------
	 * CREATION ET GESTION DU SERVICE
	 * ------------------------------------------------------------------------
	 */
	
	public HttpConnection()
	{
		super("HttpConnection");
	}

	public static void setActivity(Activity activity)
	{
		MAIN_ACTIVITY = activity;
	}

	public static void setUpdateListener(ServiceUpdateUIListener l)
	{
		UI_UPDATE_LISTENER = l;
	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.i(DEBUGTAG,"HttpConnection onHandleIntent ");

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

		mDbHelper = new FreeboxMobileDbAdapter(this);
		_getPrefs();

		if ((login != null) && (password != null))
		{
			_getUpdate();
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
	
	@Override
	public void onCreate()
	{
		Log.i(DEBUGTAG,"HttpConnection onCreate");
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		Log.i(DEBUGTAG,"HttpConnection onDestroy");
		super.onDestroy();
	}

	private static void _getUpdate()
	{
		if ((login != null) && (password != null) && (connectionStatus != CONNECT_LOGIN_FAILED))// && (id == null)
		{
			if (connectionStatus != CONNECT_CONNECTED)
			{
				if (MAIN_ACTIVITY != null)
				{
					MAIN_ACTIVITY.runOnUiThread(new Runnable()
						{
							public void run()
							{
								myProgressDialog = ProgressDialog.show(MAIN_ACTIVITY, "Mon Compte Free", "Connexion en cours ...", true,false);
							}
						});
				}
			}
			else
				myProgressDialog = null;
			connectionStatus = connectFree();
            if (myProgressDialog != null)
            	myProgressDialog.dismiss(); 
			if (connectionStatus == CONNECT_CONNECTED)
	        {
				if (MAIN_ACTIVITY != null)
				{
					MAIN_ACTIVITY.runOnUiThread(new Runnable()
						{
							public void run()
							{
								myProgressDialog = ProgressDialog.show(MAIN_ACTIVITY, "Mon Compte Free", "Vérification des nouveaux messages ...", true,false);
							}
						});
				}
				else
					myProgressDialog = null;
	       		getMessageList();
	            if (myProgressDialog != null)
	            	myProgressDialog.dismiss();
	        }
			else
			{
				if (MAIN_ACTIVITY != null)
				{
					MAIN_ACTIVITY.runOnUiThread(new Runnable()
					{
						public void run()
						{
							_showConnectionError();
						}
					});
				}
			}
		}
	}
	
	private static void _showConnectionError()
	{
		AlertDialog d = new AlertDialog.Builder(MAIN_ACTIVITY).create();
		d.setTitle("Connexion impossible !");
		d.setMessage(
			"Impossible de se connecter au portail de Free.\n"+
			"Vérifiez votre identifiant, " +
			"votre mot de passe et votre "+
			"connexion à Internet (Wifi, 3G...)."
		);
		d.setButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			}
		);
		d.show(); 
	}

	private static void _changeTimer(int ms)
	{
		AlarmManager amgr = (AlarmManager)MAIN_ACTIVITY.getSystemService(freeboxmobile.ALARM_SERVICE);
		Intent i = new Intent(MAIN_ACTIVITY, OnAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(MAIN_ACTIVITY, 0, i, 0);
		//TODO : Use setInexactRepeating to save power...
		//TODO : Take into account ms
		amgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), PERIOD, pi);

		Log.i(DEBUGTAG, "Timer  changed to "+ms);
	}

	// ------------------------------------------------------------------------
	// FIN GESTION DU SERVICE
	// ------------------------------------------------------------------------
	
	
	// ------------------------------------------------------------------------
	// GESTION DU RESEAU
	// ------------------------------------------------------------------------

    private void _getPrefs()
    {
		login = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null);
		password = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_PASSWORD, null);
		Log.d(DEBUGTAG,"hc identifiant:"+login);
		Log.d(DEBUGTAG,"hc password:"+password);
    }

	/*
	 * Déclenche une nouvelle connexion afin de rafraichir messages, etc...
	 * Pour cela, réinstalle le timer
	 */
	public static void refresh()
	{
		connectionStatus = CONNECT_NOT_CONNECTED;
		_changeTimer(PERIOD);
	}

	public static void refreshPrefs()
	{
		if (MAIN_ACTIVITY != null)
		{
			login = MAIN_ACTIVITY.getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null);
			password = MAIN_ACTIVITY.getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_PASSWORD, null);		
		}
	}

	public static int checkUpdated()
	{
		if (MAIN_ACTIVITY != null)
		{
			String l = MAIN_ACTIVITY.getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null);
			String p = MAIN_ACTIVITY.getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_PASSWORD, null);
	
			// Si les prefs ont bougé
			if (( l!=null && p!= null) && ((!l.equals(login)) || (!p.equals(password))))
			{
				Log.d(DEBUGTAG, "UPDATE: " + login+"/"+password+" - "+l+"/"+p);
				login = l;
				password = p;
				id = null;
				idt = null;
				connectionStatus = CONNECT_NOT_CONNECTED;
				_changeTimer(PERIOD);
				return 0;
			}
			else
				return -1;
		}
		else
		{
			return -1;
		}
	}

	public static void deleteMsg(String name)
	{
		File file;
		Cursor curs;
		
		Log.d(DEBUGTAG, "deleteMsg "+name);
		if (mDbHelper == null)
		{
			mDbHelper = new FreeboxMobileDbAdapter(MAIN_ACTIVITY);
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
				if ((login == null) || (password == null))
				{
					refreshPrefs();
				}
	    		String tel = curs.getString(curs.getColumnIndex(KEY_DEL));
	   			if (tel.indexOf("tel=")>-1)
	   			{
	   				tel = tel.substring(tel.indexOf("tel=")+4);
	   				if (tel.indexOf("&")>-1)
	   				{
	   					tel = tel.substring(0, tel.indexOf('&'));
	   				}
	   				Log.d(DEBUGTAG,"tel="+tel);
	    		}
	   			final String partURL = "&tel="+tel+"&fichier="+curs.getString(curs.getColumnIndex(KEY_NAME));

	   			// On lance la suppression sur le serveur dans un thread séparé car ca peut être long
				Thread t = new Thread(new Runnable() {
					@Override
		            public void run()
		        	{
						// (re)connection afin d'avoir un id et idt frais :)
						connectFree();
			   			String url = mevoUrl+"efface_message.pl?id="+id+"&idt="+idt+partURL;
						Log.d(DEBUGTAG,"Deleting message"+url);

						// On reconstitue l'url parceque id & idt ont peut etre changé
						if ((id != null) && (idt != null))
						{
							Log.d(DEBUGTAG, "Deleting on server");
							HttpClient client = new DefaultHttpClient(new BasicHttpParams());
							HttpGet getMethod = new HttpGet(url);
							getMethod.setHeader("User-Agent", USER_AGENT);
							try
							{
								client.execute(getMethod);
							}
					       	catch (IOException e)
					       	{
					        	Log.e(DEBUGTAG,"deleteMsg: "+e);
					        }
						}
						else
							Log.d(DEBUGTAG, "NOT Deleting on server");
//						Log.d(DEBUGTAG, "End thread");
		        	}
		        });
		//		t.setDaemon(true);
		        t.setName("FBM Delete Message");
		        t.start();
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

	public static void getMessageList()
	{
		String fullurl = mevoUrl + mevoListPage + "?id=" + id + "&idt=" + idt;
		Log.d(DEBUGTAG, "GET: " + fullurl);

		HttpClient client = new DefaultHttpClient(new BasicHttpParams());
		HttpGet getMethod = new HttpGet(fullurl);
		getMethod.setHeader("User-Agent", USER_AGENT);
		HttpResponse httpResponse = null;
		try
		{
			httpResponse = client.execute(getMethod);

			HttpEntity responseEntity = httpResponse.getEntity();
	    	BufferedReader br = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
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
			int newmsg = 0;
			Cursor curs;
			File file;
			URL u;
			FileOutputStream f;
			InputStream in;
			HttpURLConnection c;
			int len1;
	        byte[] buffer = new byte[1024];
	        
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
							Log.d(DEBUGTAG,"NEW MESSAGE !");
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
					        	Log.d(DEBUGTAG,"->DOWNLOADING MESSAGE");
						        u = new URL(mevoUrl+link);
						        c = (HttpURLConnection) u.openConnection();
						        c.setRequestMethod("GET");
						        c.setDoOutput(true);
					        	c.connect();
						        f = new FileOutputStream(file);
						        in = c.getInputStream();
						        len1 = 0;
						        while ( (len1 = in.read(buffer)) > 0 )
						        {
						            f.write(buffer, 0, len1);
						        }
					        	f.close();
					        	in.close();
					        	Log.d(DEBUGTAG,"->MESSAGE DOWNLOADED");
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
				if ((newmsg == 1) && (MAIN_ACTIVITY != null))
				{
					MAIN_ACTIVITY.runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(MAIN_ACTIVITY, R.string.http_new_msg , Toast.LENGTH_SHORT).show();
				    		if (UI_UPDATE_LISTENER != null)
				    			UI_UPDATE_LISTENER.updateUI();
						}
					});
				}
				else if ((newmsg > 1) && (MAIN_ACTIVITY != null))
				{
					final int n = newmsg;
					MAIN_ACTIVITY.runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(MAIN_ACTIVITY, n+" "+R.string.http_new_msgs , Toast.LENGTH_SHORT).show();
				    		if (UI_UPDATE_LISTENER != null)
				    			UI_UPDATE_LISTENER.updateUI();
						}
					});
				}
			}
			else
			{
				Log.d(DEBUGTAG,"pb extract");
			}
			mDbHelper.close();
		}
			
		catch (Exception e)
		{
			Log.e(DEBUGTAG, "getMessageList" + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			getMethod.abort();
		}
 	}

	public static int connectFree()
	{
		Log.d(DEBUGTAG,"Connect Free start ");
        try
        {
        	HttpResponse httpResponse = null;
        	httpResponse = postRequest();
        	if (httpResponse != null)
        	{
	        	HttpEntity responseEntity = httpResponse.getEntity();
	        	BufferedReader br = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
	    		String s = null;
	    		String priv = null;
	    		while ( (s=br.readLine())!=null )
	    		{
	    			if (s.indexOf("idt=")>-1)
	    			{
	    				priv = s.substring(s.indexOf("idt=")+4);
	    				if (priv.indexOf("&")>-1)
	    				{
	    					priv = priv.substring(0, priv.indexOf('&'));
	    				}
	    				else
	    				{
	    					priv = priv.substring(0, priv.indexOf('"'));
	    				}
	    				idt = priv;
	    				Log.d(DEBUGTAG,"idt :"+priv);
	    				priv = s.substring(s.indexOf("?id=")+4);
	    				priv = priv.substring(0, priv.indexOf('&'));
	    				id = priv;
	    				Log.d(DEBUGTAG,"id :"+priv);
	    				break;
	    			}
	    		}
        	}
        }
        catch (ClientProtocolException e)
        {
        	e.printStackTrace();
        }
       	catch (IOException e)
       	{
       		e.printStackTrace();
        }
       	if (id != null && idt != null)
       	{
       		return CONNECT_CONNECTED;
       	}
       	else
       	{
       		Log.d(DEBUGTAG,"MonCompeFree : AUTHENTIFICATION FAILED !");
       		return CONNECT_LOGIN_FAILED;
       	}
	}

	/**
	* Sends a POST request
	*
	* @return HttpResponse response from the server
	* @throws SocketTimeoutException
	*             , SocketException
	*/
	private static HttpResponse postRequest()
	throws ClientProtocolException, IOException
	{
		String url = serverUrl;
		Log.d(DEBUGTAG, "POST: " + url + " login: "+login);

		HttpClient client = new DefaultHttpClient(new BasicHttpParams());
		HttpPost postMethod = new HttpPost((url));
		postMethod.setHeader("User-Agent", USER_AGENT);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
   
		nameValuePairs.add(new BasicNameValuePair("login", login));
		nameValuePairs.add(new BasicNameValuePair("pass", password)); 
   
		postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	
		try
		{
			HttpResponse httpResponse = client.execute(postMethod);
			return httpResponse;
		}
		catch (IOException e)
		{
			Log.d(DEBUGTAG, "Connexion impossible "+e);
			return null;
		}
	}
}