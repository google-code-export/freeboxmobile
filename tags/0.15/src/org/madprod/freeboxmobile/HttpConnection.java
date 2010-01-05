package org.madprod.freeboxmobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List; 

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.message.BasicNameValuePair; 

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
 
/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class HttpConnection implements Constants
{
	private static String USER_AGENT = "FreeboxMobile (Linux; U; Android; fr-fr;)";

	private static String login = null;
	private static String password = null;
	// Variables id et idt d'accès à MonCompteFree
	private static String id = null;
	private static String idt = null;

	private static int connectionStatus = CONNECT_NOT_CONNECTED;
	
	private static final String serverUrl = "http://subscribe.free.fr/login/login.pl";
	
	public static ProgressDialog httpProgressDialog = null;
	public static AlertDialog errorAlert = null;
	private static Activity activity = null;
	
    /*
    private void _getPrefs()
    {
    	login = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null);
		password = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_PASSWORD, null);
		Log.d(DEBUGTAG,"hc identifiant:"+login);
    }
     */

	/**
	 * Init les variables statiques
	 * Ferme le progressdialog si il était ouvert (arrive dans le cas d'un screen rotation)
	 * Doit être appelé dans le onCreate de chaque Activity
	 * @param a activity
	 */
	public static void initVars(Activity a)
	{
		// On teste pour si on entre ici suite
        if (httpProgressDialog != null)
        {
           	httpProgressDialog.show();
        }
        if (errorAlert != null)
        {
           	errorAlert.show();
        }
		login = a.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_USER, null);
		password = a.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_PASSWORD, null);
		ConnectFree.setActivity(a);
	}

	public static void closeDisplay()
	{
		if (httpProgressDialog != null)
        {
           	httpProgressDialog.dismiss();
        }
       if (errorAlert != null)
        {
           	errorAlert.dismiss();
        }
//		ConnectFree.setActivity(null);
	}

	/**
	 * Init les variables statiques
	 * Cette fonction doit être utilisée par des classes n'ayant pas d'activity
	 * @param l
	 * @param p
	 */
	public static void setVars(String l, String p)
	{
		login = l;
		password = p;
	}
	
	public static String getId()
	{
		return (id);
	}

	public static String getIdt()
	{
		return (idt);
	}

	public static void showProgressDialog(Activity a)
	{
		httpProgressDialog = ProgressDialog.show(a, "Mon Compte Free", "Connexion en cours ...", true,false);
	}
	
	public static void dismissPd()
	{
		if (httpProgressDialog != null)
		{
			httpProgressDialog.dismiss();
			httpProgressDialog = null;
		}
	}

	public static void showError(Activity a)
	{
		errorAlert = new AlertDialog.Builder(a).create();
		errorAlert.setTitle("Connexion impossible");
		errorAlert.setMessage(
				"Impossible de se connecter au portail de Free.\n"+
				"Vérifiez votre identifiant, " +
				"votre mot de passe et votre "+	
				"connexion à Internet (Wifi, 3G...)."
		);
		errorAlert.setButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					errorAlert = null;
				}
			}
		);
		errorAlert.show();
	}

	/*
	 * Se connecte à Free en affichant une progress Dialog si nécessaire
	 */
	// TODO : Remove this function
	public static int connectFreeUI()
	{
		if ((activity != null) && (login != null) && (password != null) && (connectionStatus != CONNECT_LOGIN_FAILED))// && (id == null)
		{
			if (connectionStatus != CONNECT_CONNECTED)
			{
					activity.runOnUiThread(new Runnable()
					{
						public void run()
						{
							httpProgressDialog = ProgressDialog.show(activity, "Mon Compte Free", "Connexion en cours ...", true,false);
						}
					});
			}
			else
				httpProgressDialog = null;
		}
		// TODO : Mettre la connexion dans un thread
		connectionStatus = connectFree();
        if (httpProgressDialog != null)
        {
           	httpProgressDialog.dismiss();
           	httpProgressDialog = null;
        }
        return connectionStatus;
	}

    /**
     * Vérifie si le login et le pass ont bougé
     * @param l nouveau login
     * @param p nouveau mot de passe
     * @return 1 si ils ont bougé et qu'ils ne sont pas null, 0 sinon
     */
	public static boolean checkUpdated(String l, String p)
	{
		// Si les prefs ont bougé
		if (( l!=null && p!= null) && ((!l.equals(login)) || (!p.equals(password))))
		{
			Log.d(DEBUGTAG, "UPDATE: " + login + " / " + l);
			login = l;
			password = p;
			id = null;
			idt = null;
			connectionStatus = CONNECT_NOT_CONNECTED;
			return true;
		}
		else
			return false;
	}

	public static int connectFree()
	{
		Log.d(DEBUGTAG,"Connect Free start ");
        try
        {
    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    		nameValuePairs.add(new BasicNameValuePair("login", login));
    		nameValuePairs.add(new BasicNameValuePair("pass", password)); 
    		BufferedReader br = postRequest(serverUrl, nameValuePairs, true);
    		if (br != null)
    		{
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
        catch (Exception e)
        {
        	Log.e(DEBUGTAG, "connectFree : "+e);
        	e.printStackTrace();
        	connectionStatus = CONNECT_NOT_CONNECTED;
        	return connectionStatus;
        }
       	if (id != null && idt != null)
       	{
       		connectionStatus = CONNECT_CONNECTED;
       	}
       	else
       	{
       		Log.d(DEBUGTAG,"MonCompeFree : AUTHENTIFICATION FAILED !");
       		connectionStatus = CONNECT_LOGIN_FAILED;
       	}
       	return connectionStatus;
	}

	/**
	 * Donwload a file
	 * @param file destination file
	 * @param url source url
	 * @return 1 if success
	 */
	public static int getFile(File file, String url)
	{
		HttpURLConnection c;
		URL u;
		FileOutputStream f;
		int len;
        byte[] buffer = new byte[1024];

		Log.d(DEBUGTAG,"->DOWNLOADING FILE : "+url);
        try {
			u = new URL(url);
	        c = (HttpURLConnection) u.openConnection();
	        c.setRequestMethod("GET");
	        c.setDoOutput(true);
	    	c.connect();
	        f = new FileOutputStream(file);
	        InputStream in = c.getInputStream();
	
			while ( (len = in.read(buffer)) > 0 )
	        {
	            f.write(buffer, 0, len);
	        }
	    	f.close();
	    	in.close();
	    	Log.d(DEBUGTAG,"->FILE DOWNLOADED");
	    	return 1;
		}
        catch (Exception e)
        {
        	Log.e(DEBUGTAG, "getFile : "+e);
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Sends a GET request
	 * @param url : url to get
	 * @retour true pour avec une valeur non nulle en retour (si on veut le contenu de la page ou pas)
	 * @return HttpResponse response from the server
	 */
	public static BufferedReader getRequest(String url, boolean retour)
	{
		Log.d(DEBUGTAG, "GET: " + url);

		HttpClient client = new DefaultHttpClient(new BasicHttpParams());
		HttpGet getMethod = new HttpGet(url);
		getMethod.setHeader("User-Agent", USER_AGENT);
		try
		{
			HttpResponse httpResponse = client.execute(getMethod);
			if ((httpResponse != null) && (retour))
			{
				HttpEntity responseEntity = httpResponse.getEntity();
				return (new BufferedReader(new InputStreamReader(responseEntity.getContent())));
			}
			else
				return (null);
		}
       	catch (IOException e)
       	{
        	Log.e(DEBUGTAG,"getRequest : "+e);
        	return (null);
        }
	}

	/**
	* Sends a POST request
	* @param  url : url to post
	* @param  nameValuePairs : a list of NameValuePair with parameters to post
	* @retour true pour avec une valeur non nulle en retour (si on veut le contenu de la page ou pas)
	* @return HttpResponse response from the server or null
	* @throws SocketTimeoutException
	*             , SocketException
	*/
	public static BufferedReader postRequest(String url, List<NameValuePair> nameValuePairs, boolean retour)
	{
		Log.d(DEBUGTAG, "POST: " + url);

		HttpClient client = new DefaultHttpClient(new BasicHttpParams());
		HttpPost postMethod = new HttpPost(url);
		postMethod.setHeader("User-Agent", USER_AGENT);
   
		try
		{
			postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse httpResponse = client.execute(postMethod);
        	if ((httpResponse != null) && (retour))
        	{
	        	HttpEntity responseEntity = httpResponse.getEntity();
	        	return (new BufferedReader(new InputStreamReader(responseEntity.getContent())));
        	}
        	else
        		return (null);
		}
		catch (Exception e)
		{
			Log.e(DEBUGTAG, "postRequest : "+e);
			return (null);
		}
	}
	
	/**
	 * BufferedReader to String conversion
	 * @param	BufferedReader
	 * @return	String
	 * @throws	IOException
	 */
	public static String getPage(BufferedReader reader) {
		StringBuilder sb = new StringBuilder();
		
		if (reader == null) {
			return null;
		}
		
		String line = null;
		try {
		     while ((line = reader.readLine()) != null) {
		          sb.append(line + "\n");
		     }
		} catch (IOException e) {
			Log.e(DEBUGTAG, "getPage: "+e);
		     return null;
		}
		
		return sb.toString();
	}
}
