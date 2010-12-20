package org.madprod.freeboxmobile.tv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author olivier rosello
 * *$Id$
 * 
 */

public class MainActivity extends ListActivity implements TvConstants
{
	GoogleAnalyticsTracker tracker;
	private List< Map<String,Object> > streamsList;
	String USER_AGENT = null;
	private static ImageAdapter listAdapter = null; 
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Tv/HomeTv");
		streamsList = new ArrayList< Map<String,Object> >();
		setContentView(R.layout.tv_main_list);
		setTitle(getString(R.string.app_name));
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		if (!mgr.getString(KEY_SPLASH_TV, "0").equals(Utils.getMyVersion(this)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_TV, Utils.getMyVersion(this));
			editor.commit();
			displayHelp();
		}
		/*
		else
		{
			displayHelp();
		}
		*/
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
		new AsyncGetStreams().execute();
    }

    @Override
    protected void onResume()
    {
		Log.i(TAG,"TvActivity Resume");
    	super.onResume();
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, 1, 0, "Aide").setIcon(android.R.drawable.ic_menu_help);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        String streamUrl = (String) streamsList.get((int) position).get(M_URL);
        String mimeType = (String) streamsList.get((int) position).get(M_MIME);

    	if (streamUrl != null)
    	{
    	    Intent intent = new Intent();
    	 	intent.setAction(android.content.Intent.ACTION_VIEW);
    	 	intent.setDataAndType(Uri.parse(streamUrl), mimeType);
    	 	try
    	 	{
    	 		startActivity(intent);
    	 	}
    	 	catch (Exception e)
    	 	{
    	 		Toast.makeText(this, "Problème : "+e.getMessage(), Toast.LENGTH_LONG).show();
    	 	}
    	}
    }

    private void getStreams(boolean isFree)
    {
    	streamsList.clear();
    	String json = getPage(getJson("http://tv.freeboxmobile.net/json/streams_fbm.json"));
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
					jStreams = jChannel.getJSONArray("streams");
					nbStreams = jStreams.length();
					for (int j = 0; j < nbStreams; j++)
					{
						jStream = jStreams.getJSONObject(j);
						type = jStream.getInt("type");
						if (
								(isFree && type == 1) ||
								(type == 2)
							)
						{
							try
							{
								addChannel(jChannel.getString("name"), jChannel.getInt("num"), jChannel.getString("icon"), jStream.getString("url"), jStream.getString("mime"));
								break;
							}
							catch (JSONException e)
							{
								e.printStackTrace();
							}
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
			Log.e(TAG, "Pas de réseau !");
			// TODO : Popup à ouvrir ici !
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
				checkPhone();
			}
		});
		d3.show();    	
    }

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
    
	public static int getPlatformVersion()
	{
		try
		{
			Field verField = Class.forName("android.os.Build$VERSION").getField("SDK_INT");
			int ver = verField.getInt(verField);
			return ver;
		}
		catch (Exception e)
		{
			// android.os.Build$VERSION is not there on Cupcake
			return 3;
		}
	}

    private void checkOS()
    {
    	if (getPlatformVersion() < Build.VERSION_CODES.ECLAIR_MR1)
    	{
    		displayWrongOS();
    	}
    	else
    	{
    		checkPhone();
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
    
    private void downloadPlayer()
    {
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("Attention ! Merci de lire");
		d.setIcon(R.drawable.icon_fbm);
    	d.setMessage(
    		"Cette fonctionnalité ne fonctionnera QUE si vous êtes sur le réseau Free :\n- soit connecté à une Freebox,\n- soit connecté au réseau FreeWifi (lorsque vous êtes en déplacement).\n\n"+
			"Pour utiliser cette fonctionnalité, vous devez installer une application capable de lire les flux vidéos 'TS' comme 'VPlayer' (disponible sur Android 2.1 et +) :\n"+
			"- Cliquez sur 'Installer' pour installer VPlayer à partir du market.\n"+
			"- Cliquez sur 'Continuer' si vous avez déjà installé vplayer ou une autre application capable de lire de tels flux.\n"+
			"- Cliquez sur 'Annuler' pour ne rien faire et revenir à l'écran précédent.\n"
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Installer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
	    		tracker.trackPageView("TV/InstallPlayer");
	        	AlertDialog d2 = new AlertDialog.Builder(MainActivity.this).create();
	    		d2.setTitle("Installer VPlayer");
	    		d2.setIcon(R.drawable.icon_fbm);
	        	d2.setMessage(
	        		"Vous pouvez installer VPlayer soit à partir du market, soit en téléchargeant l'application en direct.\n"
	    		);
	    		d2.setButton(DialogInterface.BUTTON_NEGATIVE, "A partir du market", new DialogInterface.OnClickListener()
	    		{
	    			public void onClick(DialogInterface dialog, int which)
	    			{
	    				dialog.dismiss();
	    	    		tracker.trackPageView("TV/InstallPlayer");	    	    		
//	    				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.abitno.vplayer")));
	    				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:ABitNo")));
	    			}
	    		});
	    		d2.setButton(DialogInterface.BUTTON_POSITIVE, "En téléchargement direct", new DialogInterface.OnClickListener()
	    		{
	    			public void onClick(DialogInterface dialog, int which)
	    			{
	    				dialog.dismiss();
	    	    		tracker.trackPageView("TV/InstallPlayerDirect");
	    	        	AlertDialog d3 = new AlertDialog.Builder(MainActivity.this).create();
	    	    		d3.setIcon(R.drawable.icon_fbm);
	    	    		d3.setTitle("Téléchargement de VPlayer");
	    	    		d3.setMessage("Une fois le téléchargement terminé, cliquer sur sa notification 'VPLayer.apk' dans la barre de notification en haut de l'écran afin d'installer VPlayer.\n");
	    	    		d3.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener()
	    	    		{
	    	    			public void onClick(DialogInterface dialog, int which)
	    	    			{
	    	    				dialog.dismiss();
	    	    				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://files-for-abitno.googlecode.com/files/VPlayer.apk")));
	    	    			}
	    	    		});
	    	    		d3.show();
	    			}
	    		});
	    		d2.show();
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
    
    private void addChannel(String name, int nb, String logoUrl, String streamUrl, String mimeType)
    {
    	Map<String,Object> map = new HashMap<String,Object>();
		map.put(M_TITRE, ""+nb+" - "+name);
		map.put(M_URL, streamUrl);
		map.put(M_LOGO, logoUrl);
		map.put(M_MIME, mimeType);
		map.put(M_ID, nb);
		streamsList.add(map);		    	
    }
	
	private void verifyInstallFbm()
	{
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.home.HomeListActivity");
		List<ResolveInfo> activitiesList = getPackageManager().queryIntentActivities(i, 0);
		if (activitiesList.isEmpty())
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

	private InputStreamReader getJson(String url)
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

	private void showPopupFbm()
	{
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setCancelable(false);
		d.setTitle(getString(R.string.app_name));
		d.setIcon(R.drawable.icon_fbm);
		d.setMessage(
				"Pour utiliser cette fonctionnalité, vous devez installer la derniere version de Freebox Mobile'.\n\n"+
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
    	ProgressDialog myProgressDialog = null;
    	int result = -1;
    	
		@Override
		protected Void doInBackground(Void... v)
		{
			result = testNet();
			getStreams(result == 1);
			return null;
		}

		@Override
		protected void onPreExecute()
		{
	        Log.d(TAG, "onPreExecute started");
			myProgressDialog = new ProgressDialog(MainActivity.this);
			myProgressDialog.setIcon(R.drawable.icon_fbm);
			myProgressDialog.setTitle("Freebox Tv Mobile");
			myProgressDialog.setMessage("Téléchargement de la liste des chaînes...");
			myProgressDialog.show();
		}

		@Override
		protected void onPostExecute(Void r)
		{
			if ((myProgressDialog != null) && (myProgressDialog.isShowing()))
			{
				myProgressDialog.dismiss();
			}
			displayNet(result);
			MainActivity.listAdapter = new ImageAdapter(MainActivity.this, streamsList);
	        setListAdapter(MainActivity.listAdapter);
	        Log.d(TAG, "onPostExecute finished");
		}
		
		private void displayNet(int n)
		{
			switch (n)
			{
				case -1:
					Toast.makeText(MainActivity.this, "Problème réseau...", Toast.LENGTH_LONG).show();
				break;
				case 0:
					Toast.makeText(MainActivity.this, "En vous connectant au réseau Free, plus de chaînes seront disponibles", Toast.LENGTH_LONG).show();
				break;
				case 1:
					Toast.makeText(MainActivity.this, "Connecté au réseau Free", Toast.LENGTH_SHORT).show();
				break;
			}
		}
		
		/*
		 *  Returns :
		 *  -1 : network problem
		 *  0 : normal network
		 *  1 : Free network
		 */
		
		private int testNet()
		{
			try
			{
				URL u = new URL("http://tv.freebox.fr");
				HttpURLConnection c = (HttpURLConnection) u.openConnection();
				c.setRequestMethod("GET");
				c.setAllowUserInteraction(false);
				c.setUseCaches(false);
				c.setInstanceFollowRedirects(false);
				c.connect();
				Log.d(TAG, "TEST FREE : "+c.getResponseMessage());
				if (c.getResponseCode() != 200)
				{
					c.disconnect();
					return 0;
				}
				else
				{
					c.disconnect();
					return 1;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return -1;
			}
		}
	}
}
