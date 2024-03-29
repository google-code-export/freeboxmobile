package org.madprod.freeboxmobile.tv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * @author olivier rosello
 * *$Id$
 * 
 */

public class MainActivity extends /*List*/Activity implements TvConstants
{
	GoogleAnalyticsTracker tracker;
	public static Map<Integer, Chaine> mapChaines = new HashMap<Integer, Chaine>();
	private static ArrayList<Chaine> listChaines = new ArrayList<Chaine>();
	static String USER_AGENT = null;
    static boolean modeFull = false;
	private static ImageAdapter listAdapter = null; 
	private static ProgressDialog pd = null;
	private static long startPlay = 0;
	private int networkState = -1;
	private Map<Integer, Integer> streamsPrefs = new HashMap<Integer, Integer>();
	GridView gridview;
	
	final static int NETWORK_TYPE_UNKNOWN = -1;
	final static int NETWORK_TYPE_PROBLEM = 0;
	final static int NETWORK_TYPE_INTERNET = 1;
	final static int NETWORK_TYPE_FREE = 2;
	final static int NETWORK_TYPE_MULTIPOSTE = 3;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//		setContentView(R.layout.tv_main_list);
		setContentView(R.layout.tv_main_grid);
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Tv/HomeTv");
		TextView t = (TextView) findViewById(R.id.module_version);
		t.setText(getString(R.string.module_name)+" "+Utils.getMyVersion(this));
		gridview = (GridView) findViewById(R.id.gridview);
		
		gridview.setOnItemClickListener(
			new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View v, int position, long id)
				{
			        Log.d(TAG, "LOG : "+listChaines);
			        Log.d(TAG, " POS : "+listChaines.get(position));
			        Log.d(TAG, " STREAM : "+listChaines.get(position).getStream(Chaine.STREAM_TYPE_INTERNET));
			        if ((listChaines != null) &&
			        	(listChaines.get(position) != null) &&
			        	(listChaines.get(position).getFavoriteStream(streamsPrefs) != null) &&
			        	(listChaines.get(position).getFavoriteStream(streamsPrefs).get(Chaine.M_URL) != null) &&
			        	(listChaines.get(position).getFavoriteStream(streamsPrefs).get(Chaine.M_MIME) != null)
			        	)
				        callStream(
				        		listChaines.get(position).getFavoriteStream(streamsPrefs).get(Chaine.M_URL),
				        		listChaines.get(position).getFavoriteStream(streamsPrefs).get(Chaine.M_MIME)
			        );
//					Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
				}
			}
			);

		gridview.setOnItemLongClickListener(
			new OnItemLongClickListener()
			{
				public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
				{
			        Log.d(TAG, "LOOOONNNNNNNNNNNNNNNNNGGGGGGGGGGGGGGGGG :!!!!!!!!!!!!!!!");
			        Log.d(TAG, "LOG : "+listChaines);
			        Log.d(TAG, " POS : "+listChaines.get(position));
				    int max = Chaine.STREAM_NAME.length;
			        Log.d(TAG, " MAX : "+max);
			        //CharSequence[] items;// = {"Red", "Green", "Blue"};
			        //items = new CharSequence[max];
			        ArrayList<CharSequence> items = new ArrayList<CharSequence>(); 
			        Integer i = 0;
				    while (i<max)
				    {
				    	Log.d(TAG, "i = "+i);
//				    	Log.d(TAG, "i:"+i+" NAME:"+Chaine.STREAM_NAME[i]+" TYPE:"+Chaine.STREAM_TYPE[i]);
					    if (listChaines.get(position).getStream(Chaine.STREAM_TYPE[i]) != null)
					    {
					    	Log.d(TAG, "Name = "+Chaine.STREAM_NAME[i]);
					    	items.add(Chaine.STREAM_NAME[i]);// = Chaine.STREAM_NAME[i];
//					    	Log.d(TAG, "Item i = "+items[i]);
//						    menu.add(0, Chaine.STREAM_TYPE[i], i, Chaine.STREAM_NAME[i]);
					    }
				    	i++;
				    }

				    Log.d(TAG, "OUTSIDE loop !");
			        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				    Log.d(TAG, "OUTSIDE loop 2 !");
			        builder.setTitle("Pick a color");
				    Log.d(TAG, "OUTSIDE loop 3 ! ");

				    final CharSequence[] it = new CharSequence[items.size()];//(CharSequence[]) items.toArray();
				    for (i = 0; i < items.size(); i++)
				    {
				    	Log.d(TAG, "COPY "+i);
				    	it[i] = items.get(i);
				    }
			        builder.setItems(it, new DialogInterface.OnClickListener()
			        {
			        	public void onClick(DialogInterface dialog, int item)
			            {
			                Toast.makeText(getApplicationContext(), "Res : "+item+" - "+it[item], Toast.LENGTH_SHORT).show();
			            }
			        });
				    Log.d(TAG, "OUTSIDE loop  4 !");

			        AlertDialog alert = builder.create();
				    Log.d(TAG, "OUTSIDE loop 5 !");

//			        alert.setTitle("Sélectionnez le flux pour "+listChaines.get(position).getName());
			        alert.setTitle("Sélectionnez le flux pour ");
				    Log.d(TAG, "OUTSIDE loop 6 !");

			        alert.show();
				    Log.d(TAG, "OUTSIDE loop 7 !");

					return true;
				}
			}
			);

		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		if (!mgr.getString(KEY_SPLASH_TV, "0").equals(Utils.getMyVersion(this)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_TV, Utils.getMyVersion(this));
			editor.commit();
			displayHelp();
		}
		// A partir de quelle version il faut reseter les prefs par défaut
		if (mgr.getInt(KEY_PREFS_VERSION, 0) < 5)
		{
			Log.d(TAG, "Too old prefs version : "+mgr.getInt(KEY_PREFS_VERSION, 0));
			initDefaultPrefs();

			Editor editor = mgr.edit();
			editor.putInt(KEY_PREFS_VERSION, Utils.getMyCode(this));
			editor.commit();
		}
    }

    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	tracker.stop();
    }

    @Override
    protected void onStart()
    {
    	super.onStart();
    	Log.i(TAG,"TvActivity Start");
    	verifyInstallFbm();
    	setupStreamsPrefs();
    	if (modeFull)
    	{
			Toast.makeText(MainActivity.this, "ModeFull activé (pour tests seulement) !", Toast.LENGTH_LONG).show();
    	}
    	if (!isConnectionOk())
    	{
    		showPopupNetwork();
    	}
    	else
    	{
        	if (pd != null)
        	{
        		pd.show();
        	}
	    	if (listAdapter == null)
	    	{
	    		new AsyncGetStreams().execute();
	    	}
	    	else
	    	{
	    		gridview.setAdapter(MainActivity.listAdapter);	    		
//	    		setListAdapter(MainActivity.listAdapter);
//	            registerForContextMenu(getListView());
	    	}
	    	if ((Calendar.getInstance().getTimeInMillis() - startPlay) < (10 * 1000))
	    	{
		    	Log.d(TAG, "DELAIS : "+startPlay+" - "+(Calendar.getInstance().getTimeInMillis() - startPlay));
				Toast.makeText(MainActivity.this, "La lecture n'a pas été longue... Vous avez peut être un problème de débit sur votre réseau.", Toast.LENGTH_LONG).show();
	    	}
    	}
    }

    @Override
    protected void onResume()
    {
		Log.i(TAG,"TvActivity Resume");
    	super.onResume();
    }

    @Override
    protected void onPause()
    {
		Log.i(TAG,"TvActivity Pause");
    	super.onResume();
    	if (pd != null)
    	{
    		pd.dismiss();
    	}
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, 1, 0, "Aide").setIcon(android.R.drawable.ic_menu_help);
        menu.add(0, 2, 0, "Rafraichir").setIcon(android.R.drawable.ic_menu_rotate);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
	        case 1:
	        	checkOS();
        	return true;
	        case 2:
	        	new AsyncGetStreams().execute();
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
/*
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);

        Log.d(TAG, "LOG : "+listChaines);
        Log.d(TAG, " POS : "+listChaines.get(position));
        Log.d(TAG, " STREAM : "+listChaines.get(position).getStream(Chaine.STREAM_TYPE_INTERNET));
        if ((listChaines != null) &&
        	(listChaines.get(position) != null) &&
        	(listChaines.get(position).getFavoriteStream(streamsPrefs) != null) &&
        	(listChaines.get(position).getFavoriteStream(streamsPrefs).get(Chaine.M_URL) != null) &&
        	(listChaines.get(position).getFavoriteStream(streamsPrefs).get(Chaine.M_MIME) != null)
        	)
	        callStream(
	        		listChaines.get(position).getFavoriteStream(streamsPrefs).get(Chaine.M_URL),
	        		listChaines.get(position).getFavoriteStream(streamsPrefs).get(Chaine.M_MIME)
        );
    }
*/
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{
		AdapterContextMenuInfo info;

		super.onCreateContextMenu(menu, view, menuInfo);
		info = (AdapterContextMenuInfo) menuInfo;
	    menu.setHeaderTitle("Sélectionnez le flux pour "+listChaines.get((int)info.position).getName());
	    Integer i = 0;
	    int max = Chaine.STREAM_NAME.length;
	    while (i<max)
	    {
//	    	Log.d(TAG, "i:"+i+" NAME:"+Chaine.STREAM_NAME[i]+" TYPE:"+Chaine.STREAM_TYPE[i]);
		    if (listChaines.get((int)info.position).getStream(Chaine.STREAM_TYPE[i]) != null)
		    {
			    menu.add(0, Chaine.STREAM_TYPE[i], i, Chaine.STREAM_NAME[i]);
		    }
	    	i++;
	    }
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        callStream(
        		listChaines.get(info.position).getStream(item.getItemId()).get(Chaine.M_URL),
        		listChaines.get(info.position).getStream(item.getItemId()).get(Chaine.M_MIME)
        );

    	return super.onContextItemSelected(item);
	}

	private void initDefaultPrefs()
	{
		int i = 0;

		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = mgr.edit();
		while (i < listStreamsKeys.length)
		{
			editor.putString(listStreamsKeys[i], Integer.toString(defaultValues[i]));
			i++;
		}
		editor.commit();
	}

	private void setupStreamsPrefs()
	{
		boolean cont = true;
		int i = 0;
		String result = null;

		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		while ((cont) && (i < listStreamsKeys.length))
		{
			result = mgr.getString(listStreamsKeys[i], null);
			if (result != null)
			{
				Log.d(TAG, "add : "+i+" - "+result);
				streamsPrefs.put(i, Integer.parseInt(result));
			}
			else
			{
				cont = false;
			}
			i++;
		}
		modeFull = mgr.getBoolean("modeFull", false);
	}

	public void onFBMClick(View v)
	{
		Utils.goFBM(this);
	}

	public void onRefreshClick(View v)
	{
    	new AsyncGetStreams().execute();
	}

	public void onSettings(View v)
	{
		startActivity(new Intent(this, SettingsActivity.class));
//    	checkOS();
	}

	private void callStream(String streamUrl, String mimeType)
	{
    	if (streamUrl != null)
    	{
    	    Intent intent = new Intent();
    	 	intent.setAction(android.content.Intent.ACTION_VIEW);
    	 	intent.setDataAndType(Uri.parse(streamUrl), mimeType);
    	 	try
    	 	{
				startPlay = Calendar.getInstance().getTimeInMillis();
    	 		startActivity(intent);
    	 	}
    	 	catch (Exception e)
    	 	{
    	 		Toast.makeText(this, "Problème : "+e.getMessage(), Toast.LENGTH_LONG).show();
    	 	}
    	}
    	else
    	{
	 		Toast.makeText(this, "Problème avec le flux (null)", Toast.LENGTH_LONG).show();    		
    	}
	}

    private boolean isConnectionOk()
    {
    	ConnectivityManager mConnectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	// Skip if no connection, or background data disabled
    	NetworkInfo info = mConnectivity.getActiveNetworkInfo();
    	if (info == null || !mConnectivity.getBackgroundDataSetting())
    	{
    	    return false;
    	}
    	int netType = info.getType();
    	int netSubtype = info.getSubtype();
    	if (netType == ConnectivityManager.TYPE_WIFI)
    	{
    	    return info.isConnected();
    	}
    	else if (netType == ConnectivityManager.TYPE_MOBILE
    	    && netSubtype >= TelephonyManager.NETWORK_TYPE_UMTS
    	    && !mTelephony.isNetworkRoaming())
    	{
    	        return info.isConnected();
    	}
    	else
    	{
    		Log.d(TAG, "CONNECTION : "+netSubtype);
    	    return false;
    	}
    }

    private void getStreamsFBM(int networkType)
    {
    	Log.d(TAG, "getStreamsFBM started");
    	if (networkType > 0)
    	{
			Chaine c;
	    	mapChaines.clear();
	    	String json = getPage(getUrl("http://tv.freeboxmobile.net/json/streams_fbm.json"));
	    	if (json != null)
	    	{
				try
				{
					JSONArray jStreams;
					int nbStreams;
					int nbChannels;
					int type;
	
					JSONObject jChannel, jStream;
					JSONArray jChannels = new JSONObject(json).getJSONArray("channels");
					nbChannels = jChannels.length();
					Log.i(TAG, "number chaines : "+ nbChannels);
					for (int i = 0; i < nbChannels; i++)
					{
						jChannel = jChannels.getJSONObject(i);
						Log.i(TAG, "Name : "+jChannel.getString("name"));
						if ((jChannel.getBoolean("active") != false) || (modeFull))
						{
							try
							{
								c = new Chaine(jChannel.getInt("num"), jChannel.getString("icon"), jChannel.getString("name"));
								jStreams = jChannel.getJSONArray("streams");
								nbStreams = jStreams.length();
								for (int j = 0; j < nbStreams; j++)
								{
									jStream = jStreams.getJSONObject(j);
									if ((jStream.getBoolean("active") != false) || (modeFull))
									{
										switch (jStream.getInt("type"))
										{
											case 2:
												type = Chaine.STREAM_TYPE_INTERNET;
												break;
											case 1:
												type = Chaine.STREAM_TYPE_TVFREEBOX;
												break;
											default:
												type = -1;
												break;
										}
										try
										{
											c.addStream(type, jStream.getString("url"), jStream.getString("mime"));
										}
										catch (JSONException e)
										{
											e.printStackTrace();
										}
									}
								}
								mapChaines.put(jChannel.getInt("num"), c);
							}
							catch (JSONException e)
							{
								e.printStackTrace();
							}
						}
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
	    	}
			else
			{
				Log.e(TAG, "Impossible de charger le json !");
			}
//	    	if (networkType > 0)
			if ((modeFull) || (networkType == NETWORK_TYPE_MULTIPOSTE))
	    	{
	    		InputStreamReader m3u;
		    	m3u = getUrl("http://mafreebox.freebox.fr/freeboxtv/playlist.m3u");
/*		    	File f=new File("/sdcard/freeboxmobile/playlist.m3u");
		    	FileInputStream m3ufis = null;
				try
				{
					m3ufis = new FileInputStream(f);
			    }
				catch (FileNotFoundException e1)
				{
					e1.printStackTrace();
				}
		    	m3u = new InputStreamReader(m3ufis);
*/
				if (m3u != null)
				{
					BufferedReader reader = new BufferedReader(m3u); 
					StringBuilder sb = new StringBuilder();

					if (reader != null)
					{
						String line = null;
						String channelTitle = null;
						String num = null;
						Integer cnum = null;
						try 
						{
							line = reader.readLine();
							do
							{
								if (line.contains("EXTINF"))
								{
									num = line.substring(line.indexOf(",") + 1, line.indexOf(" "));
									try
									{
										cnum = new Integer(num);
										c = mapChaines.get(cnum);
										if (c  == null) // If we already have the channel into the map (due to another existing stream) 
										{
											if (line.indexOf("(") != -1)
												c = new Chaine(cnum, "http://tv.freeboxmobile.net/"+cnum+".png", line.substring(line.indexOf("-") + 2, line.indexOf("(")));
											else
												c = new Chaine(cnum, "http://tv.freeboxmobile.net/"+cnum+".png", line.substring(line.indexOf("-") + 2));
										}
										channelTitle = line;
										while ((line != null) && line.contains("rtsp") == false)
										{
											line = reader.readLine();
										}
										if (line != null)
										{
											if (line.contains("flavour=sd"))
											{
												c.addStream(Chaine.STREAM_TYPE_MULTIPOSTE_SD, line, "video/mp2");
												mapChaines.put(cnum, c);
											}
											// modeFull here because currently player doesn't play mp4 streams
											if ((modeFull) && (line.contains("flavour=ld")))
											{
												c.addStream(Chaine.STREAM_TYPE_MULTIPOSTE_LD, line, "video/mp4");
												mapChaines.put(cnum, c);
											}
											// modeFull here because currently player doesn't play mp4 streams
											if ((modeFull) && (line.contains("flavour=hd")))
											{
												c.addStream(Chaine.STREAM_TYPE_MULTIPOSTE_HD, line, "video/mp4");
												mapChaines.put(cnum, c);
											}
											if (line.contains("tsid"))
											{
												if (!channelTitle.contains("HD"))
												{
//													Log.d(TAG, "adding TNTSD : "+channelTitle);
													c.addStream(Chaine.STREAM_TYPE_MULTIPOSTE_TNTSD, line, "video/mp2");
													mapChaines.put(cnum, c);
												}
												else
												{
													// modeFull here because currently player doesn't play mp4 streams
													if (modeFull)
													{
	//													Log.d(TAG, "adding TNTHD : "+channelTitle);
														c.addStream(Chaine.STREAM_TYPE_MULTIPOSTE_TNTHD, line, "video/mp4");
														mapChaines.put(cnum, c);
													}
												}
											}
										}
									}
									catch (Exception e)
									{
										Log.d(TAG, "pb conversion num chaine : "+num);
									}
								}
								sb.append(line+"\n");
						    }
							while ((line = reader.readLine()) != null);
						}
						catch (IOException e)
						{
							Log.e(TAG, "parse m3u : "+e);
							e.printStackTrace();
						}		
					}
				}
	    	}
	    	listChaines.clear();
	    	listChaines.addAll(mapChaines.values());
	    	Collections.sort(listChaines);
    	}
    }

    private void displayHelp()
    {
    	AlertDialog d3 = new AlertDialog.Builder(MainActivity.this).create();
		d3.setIcon(R.drawable.icon_fbm);
		d3.setTitle("Aide");
		d3.setMessage("A tout moment, vous pouvez relire les informations qui vont suivre en utilisant la touche menu -> Aide.\n\n");
		d3.setButton(DialogInterface.BUTTON_POSITIVE, "Continuer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				checkOS();
			}
		});
		d3.show();    	    	
    }

    private void displayWrongOS()
    {
		tracker.trackPageView("TV/DisplayWrongOS");
    	AlertDialog d3 = new AlertDialog.Builder(MainActivity.this).create();
		d3.setIcon(R.drawable.icon_fbm);
		d3.setTitle("Problème de version d'Android");
		d3.setMessage("La version d'Android de votre téléphone ("+Build.VERSION.RELEASE+") ne devrait pas être compatible avec le player vidéo nécessaire à la visualisation des chaînes TV.\n\n"+
				"Si c'est le cas, VPlayer ne sera pas visible sur le market ou ne fonctionnera pas après installation.\n");
		d3.setButton(DialogInterface.BUTTON_POSITIVE, "J'ai compris", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				//checkPhone();
				downloadPlayer();
			}
		});
		d3.show();    	
    }

    /*
    private void displayWrongPhone()
    {
		tracker.trackPageView("TV/DisplayWrongPhone");
    	AlertDialog d3 = new AlertDialog.Builder(MainActivity.this).create();
		d3.setIcon(R.drawable.icon_fbm);
		d3.setTitle("Problème de compatibilité");
		d3.setMessage("Il se peut que votre appareil ne soit pas compatible avec le player vidéo nécessaire à la visualisation de la télévision.\n\n"+
				"Si c'est le cas, Vplayer ne sera pas visible sur le market ou ne fonctionnera pas après installation.\n");
		d3.setButton(DialogInterface.BUTTON_POSITIVE, "J'ai compris", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				downloadPlayer();
			}
		});
		d3.show();
    }
    */

	private void checkOS()
	{
		if (Utils.getPlatformVersion() < Build.VERSION_CODES.ECLAIR_MR1)
		{
			displayWrongOS();
		}
		else
		{
			//checkPhone();
			downloadPlayer();
		}
	}

    /*
     *  Fonctionne sur :
     *  - Google Nexus One (2.2)
     *  - Samsung Galaxy S (2.2)
     *  - HTC Desire HD (2.2)
     *  - HTC Desire
     *  - Acer Liquid (2.2)
     *  - Archos 70 (2.2)
     *  - Archos 10.1
     *  - Motorola Milestone (2.2)
     *  - Motorola Milestone 2
     *  - Google Nexus S
     * 
     *  Ne Fonctionne pas sur :
     *  - Dell Streak (2.2)
     *  
     *  Inivisible sur :
     *  - HTC Hero (2.1) (2.2)
     *  - HTC Legend
     *  - HTC Dream
     *  - HTC Wildfire
     *  - HTC Magic (2.2)
     *  - HTC Tattoo ?
     *  - Motorola Dext
     *  - Samsung Galaxy (Original)
     *  - Samsung Galaxy Spica
     *  - Samsung Naos
     *  - Samsung Teos
     *  - LG Optimus GT540
     *  - Sony Ericsson x10 mini pro (U20i)
     *  
     */

    /*
     * A prendre en compte ci-dessous (chaîne MODEL inconnue) :
     *  - Sony XPeria x10mini
     */

    /*
    private void checkPhone()
    {
		final Build build = new Build();
    	String b = Build.MODEL.toLowerCase();
		final String cpuAbi = Utils.getFieldReflectively(build, "CPU_ABI");

		// Here we check if CPU is ok (available only for OS > 1.6) (bad cpu string is "armeabi", good ones is "armeabi-*"
		// and if model is ok (for 1.5...)
    	if (
    			!cpuAbi.contains("armeabi-") ||
    			b.contains("u20i") ||		// Sony Ericsson x10 mini pro
    			b.contains("legend") ||
    			b.contains("hero") ||
    			b.contains("dream") ||
    			b.contains("magic") ||
    			b.contains("tattoo") ||
    			b.contains("wildfire") ||
    			b.contains("steak") ||
    			b.contains("legend") ||
    			b.contains("mb200") ||		// Motorola Dext
    			b.contains("gt540") ||		// LG Optimus
    			b.contains("gt-i5800") ||	// Samsung Teos    			
    			b.contains("gt-i5801") ||	// Samsung Naos
    			b.contains("gt-i5700") ||	// Samsung Galaxy Spica
    			b.contains("gt-i7500")		// Samsung Galaxy (Original)
    		)
    	{
    		displayWrongPhone();
    	}
    	else
    	{
    		downloadPlayer();
    	}
    }
*/
    
    private void showNetworkRestrictions()
    {
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("Attention ! Merci de lire");
		d.setIcon(R.drawable.icon_fbm);
    	d.setMessage(
    		"En étant connecté à une Freebox, vous aurez plus de chaînes à votre disposition."
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
    	d.show();
    }

    private void showGlobalRestrictions()
    {
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("Important ! Merci de lire");
		d.setIcon(R.drawable.icon_fbm);
    	d.setMessage(
       		"En vous connectant à une Freebox v6 avec la TNT (le scan TNT doit être effectué sur la v6), vous obtiendrez toutes les chaînes de la TNT sur Freebox Mobile !"
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
    	d.show();
    }

    private void downloadPlayer()
    {
    	if (!modeFull)
    		showNetworkRestrictions();
    	showGlobalRestrictions();
		if (!isModuleInstalled("com.daroonsoft.player", "com.daroonsoft.player.HomeActivity"))
		{
	    	AlertDialog d = new AlertDialog.Builder(this).create();
			d.setTitle("Attention ! Merci de lire");
			d.setIcon(R.drawable.icon_fbm);
	    	d.setMessage(
				"Pour utiliser cette fonctionnalité, vous devez installer une application capable de lire les flux vidéos 'TS' comme 'Daroon Player' :\n"+
				"- Cliquez sur 'Installer' pour installer Daroon Player.\n"+
				"- Cliquez sur 'Continuer' si vous avez déjà installé vplayer ou une autre application capable de lire de tels flux.\n"+
				"- Cliquez sur 'Annuler' pour ne rien faire et revenir à l'écran précédent.\n"
			);
			d.setButton(DialogInterface.BUTTON_POSITIVE, "Installer", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
		    		tracker.trackPageView("TV/InstallPlayer");
//    				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Daroonsoft")));
    				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://market.android.com/details?id=com.daroonsoft.player")));
				}
			});
			d.setButton(DialogInterface.BUTTON_NEUTRAL, "Continuer", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
			d.setButton(DialogInterface.BUTTON_NEGATIVE, "Annnuler", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					finish();
				}
			});
	    	d.show();
		}
    }

	private boolean isModuleInstalled(String module, String activity)
	{
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setClassName(module, activity);
		List<ResolveInfo> activitiesList = getPackageManager().queryIntentActivities(i, 0);
		if (activitiesList.isEmpty())
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	private void verifyInstallFbm()
	{
		if (!isModuleInstalled("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.home.HomeListActivity"))
		{
			showPopupFbm();
			SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_TV, "0");
			editor.commit();
		}
	}

	public static String getPage(InputStreamReader isr)
	{
		if (isr == null)
		{
			return null;
		}

		BufferedReader reader = new BufferedReader(isr); 
		StringBuilder sb = new StringBuilder();
		
		if (reader == null)
		{
			return null;
		}

		String line = null;
		try 
		{
			while ((line = reader.readLine()) != null)
			{
		          sb.append(line+"\n");
		    }
		}
		catch (IOException e)
		{
			Log.e(TAG, "getPage: "+e);
			e.printStackTrace();
			return null;
		}		
		return sb.toString();
	}

	private InputStreamReader getUrl(String url)
	{
		if (USER_AGENT == null)
		{
			Build build = new Build();
			USER_AGENT = getString(R.string.app_name)+"/"+Utils.getMyVersion(this)+" (Linux; U; Android "+Build.VERSION.RELEASE+"; "+ Utils.getFieldReflectively(build,"MANUFACTURER")+";"+Utils.getFieldReflectively(build,"MODEL")+";fr-fr;)";
		}
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;
        try
        {
        	httpget.setHeader("User-Agent", USER_AGENT);
        	response = httpclient.execute(httpget);
        	HttpEntity entity = response.getEntity();
        	return (new InputStreamReader(entity.getContent(), "UTF8"));
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
	}

	private void showPopupNetwork()
	{
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setCancelable(false);
		d.setTitle(getString(R.string.app_name)+"\nDésolé !");
		d.setIcon(R.drawable.icon_fbm);
		d.setMessage(
				"Pour utiliser ce module, vous devez être connecté en Wifi ou en 3G afin d'avoir suffisament de débit.\n"
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				finish();
			}
		});
		d.show();
	}

	private void showPopupFbm()
	{
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setCancelable(false);
		d.setTitle(getString(R.string.app_name));
		d.setIcon(R.drawable.icon_fbm);
		d.setMessage(
				"Pour utiliser cette fonctionnalité, vous devez installer la derniere version de Freebox Mobile.\n\n"+
				"Cliquez sur 'Continuer' pour l'installer ou sur 'Annuler' pour quitter "+getString(R.string.app_name)
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Continuer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.madprod.freeboxmobile" ));
				marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try
				{
					startActivity(marketIntent);
					finish();
				}
				catch (ActivityNotFoundException e)
				{
					AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
					ad.setTitle(getString(R.string.app_name));
					ad.setIcon(R.drawable.icon_fbm);
					ad.setMessage("Impossible d'ouvrir Android Market !");
					ad.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							finish();
						}
					});
					ad.show();
				}
			}
		});
		d.setButton(DialogInterface.BUTTON_NEGATIVE, "Quitter", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				finish();
			}
		});
		d.show();
	}

    private class AsyncGetStreams extends AsyncTask<Void, Void, Void>
    {
    	int networkType = -1;
    	
		@Override
		protected Void doInBackground(Void... v)
		{
			Log.d(TAG, "doInBackground started");
			networkType = testNet();
			getStreamsFBM(networkType);
			return null;
		}

		@Override
		protected void onPreExecute()
		{
	        Log.d(TAG, "onPreExecute started");
	        if (pd == null)
	        {
				pd = new ProgressDialog(MainActivity.this);
				pd.setIcon(R.drawable.icon_fbm);
				pd.setTitle("Freebox Tv Mobile");
				pd.setMessage("Téléchargement de la liste des chaînes...");
				pd.show();
	        }
		}

		@Override
		protected void onPostExecute(Void r)
		{
			if (pd != null)
			{
				pd.dismiss();
				pd = null;
			}
			displayNet(networkType);
			if (listAdapter == null)
			{
				MainActivity.listAdapter = new ImageAdapter(MainActivity.this, listChaines);

				Log.d(TAG, "gv : "+gridview);
				Log.d(TAG, "la : "+MainActivity.listAdapter);
	    		gridview.setAdapter(MainActivity.listAdapter);	    		
//		        setListAdapter(MainActivity.listAdapter);
//		        registerForContextMenu(getListView());
			}
			else
			{
				listAdapter.notifyDataSetChanged();
			}
	        Log.d(TAG, "onPostExecute finished");
		}

		private void displayNet(int n)
		{
			final String pasFreebox = "En vous connectant à une Freebox Révolution avec TNT, plus de chaînes seront disponibles";
			switch (n)
			{
				case -1:
				case 0:
					Toast.makeText(MainActivity.this, "Problème réseau...\n\nEssayez de rafraichir via la touche menu.", Toast.LENGTH_LONG).show();
				break;
				case 1:
//					Toast.makeText(MainActivity.this, "En vous connectant au réseau Free, plus de chaînes seront disponibles", Toast.LENGTH_LONG).show();
					Toast.makeText(MainActivity.this, "Connecté à Internet.\n\n"+pasFreebox, Toast.LENGTH_LONG).show();
				break;
				case 2:
					Toast.makeText(MainActivity.this, "Connecté au réseau Free.\n\n"+pasFreebox, Toast.LENGTH_SHORT).show();
				break;
				case 3:
					Toast.makeText(MainActivity.this, "Connecté à une Freebox\nUn clic long sur une chaîne permet de choisir le type de flux.", Toast.LENGTH_SHORT).show();
				break;
			}
		}

		/*
		 *  Returns :
		 *  -1 : unknown state (default)
		 *  0 : network problem
		 *  1 : normal network
		 *  2 : Free network
		 *  3 : multiposte
		 */

		private int testNet()
		{
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, 1000);
			HttpConnectionParams.setSoTimeout(params, 1000);
			HttpConnectionParams.setStaleCheckingEnabled(params, true);
			HttpClient client = new DefaultHttpClient(params);
	        HttpGet request = new HttpGet("http://mafreebox.freebox.fr/freeboxtv/playlist.m3u");
	        try
	        {
	            HttpResponse response = client.execute(request);
	            if (response.getStatusLine().getStatusCode() == 200)
	            {
	    			Log.d(TAG, "Multiposte network");
	    			networkState = NETWORK_TYPE_MULTIPOSTE;
	    			return networkState;
	            }
	        }
	        catch (Exception e)
	        {
				Log.d(TAG, "network problem while checking multiposte");
				e.printStackTrace();
		        networkState = NETWORK_TYPE_PROBLEM;
	        }
	        networkState = NETWORK_TYPE_INTERNET;
/*
 * Commented because tv.freebox.fr is now closed :-(
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 5000);
			client = new DefaultHttpClient(params);
	        request = new HttpGet("http://tv.freebox.fr");
	        try
	        {
	            HttpResponse response = client.execute(request);
	            if (response.getStatusLine().getStatusCode() != 200)
	            {
	    			Log.d(TAG, "normal network");
	    			networkState = 1;
	            }
	            else
	            {
	    			Log.d(TAG, "Free network");
	    			networkState = 2;
	            }
	        }
	        catch (Exception e)
	        {
				Log.d(TAG, "network problem");
				e.printStackTrace();
				networkState = 0;
	        }
*/
	        return networkState;
		}
    }
}
