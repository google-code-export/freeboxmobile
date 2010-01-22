package org.madprod.freeboxmobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List; 

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

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

public class FBMHttpConnection implements Constants
{
	private static String USER_AGENT = "FreeboxMobile (Linux; U; Android; fr-fr;)";

	private static String title = null;
	private static String login = null;
	private static String password = null;
	// Variables id et idt d'accès à MonCompteFree
	private static String id = null;
	private static String idt = null;

	private static int connectionStatus = CONNECT_NOT_CONNECTED;
	
	private static final String serverUrl = "http://subscribe.free.fr/login/login.pl";
	
	public static ProgressDialog httpProgressDialog = null;
	public static AlertDialog errorAlert = null;
	//private static Activity activity = null;
		
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
		USER_AGENT = a.getString(R.string.app_name)+"/"+a.getString(R.string.app_version)+" (Linux; U; Android; fr-fr;)";
		// On teste pour si on entre ici suite
        if (httpProgressDialog != null)
        {
           	httpProgressDialog.show();
        }
        if (errorAlert != null)
        {
           	errorAlert.show();
        }
        title = a.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_TITLE, null);
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
	}

	/**
	 * Init les variables statiques
	 * Cette fonction doit être utilisée par des classes n'ayant pas d'activity (à la place de initVars)
	 * @param l
	 * @param p
	 */
	public static void setVars(String l, String p)
	{
		login = l;
		password = p;
	}
	
	public static String getTitle()
	{
		return (title);
	}

	public static String getIdentifiant()
	{
		return (login);
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
		return FBMHttpConnection.connectionFree(login, password, false);
	}

	// En cas de résussite : http://adsl.free.fr/compte/console.pl?id=467389&idt=10eb38933107f10c
	// En cas d'erreur de login/pass : /login/login.pl?login=0909&error=1
	/**
	 * connectionFree : identifie sur le portail de Free avec le login/pass demandé
	 * @param l : login (identifiant = numéro de téléphone Freebox)
	 * @param p : password (mot de passe Freebox)
	 * @param check : true = juste vérifier les identifiants / false : se connecter (ie stocker id & idt)
	 * @return CONNECT_CONNECTED || CONNECT_NOT_CONNECTED || CONNECT_LOGIN_FAILED
	 */
	public static int connectionFree(String l, String p, boolean check)
	{
		String m_id = null;
		String m_idt = null;
		HttpURLConnection h = null;
		
		Log.d(DEBUGTAG,"Connect Free start ");
        try
        {
    		List<String> listParameter = new ArrayList<String>();
    		listParameter.add("login="+l);   		
    		listParameter.add("pass="+p);

    		URL myURL = new URL(serverUrl);
			URLConnection ucon = myURL.openConnection();

			if (!(ucon instanceof HttpURLConnection)) throw new IOException("Not an HTTPconnection.");
    		h = (HttpURLConnection) ucon;
    		h.setRequestMethod("POST");
    		h.setDoOutput(true);
    		OutputStreamWriter o = new OutputStreamWriter(h.getOutputStream());
			o.write(makeStringForPost(listParameter));
			o.flush();
			o.close();
			String s = h.getHeaderFields().get("location").toString();
    		String priv;
			if (s.indexOf("idt=")>-1)
			{
				priv = s.substring(s.indexOf("idt=")+4);
				if (priv.indexOf("]")>-1)
				{
					priv = priv.substring(0, priv.indexOf(']'));
				}
				m_idt = priv;
				Log.d(DEBUGTAG,"idt :"+priv);
				priv = s.substring(s.indexOf("?id=")+4);
				priv = priv.substring(0, priv.indexOf('&'));
				m_id = priv;
				Log.d(DEBUGTAG,"id :"+priv);
			}
        }
		catch (Exception e)
		{
        	Log.e(DEBUGTAG, "connectFree : "+e);
        	e.printStackTrace();
        	connectionStatus = CONNECT_NOT_CONNECTED;
        	if (check == false)
        	{
	        	id = null;
	        	idt = null;
        	}
        	return connectionStatus;
		}
		finally
		{
			if (h != null)
			{
				h.disconnect();
			}
		}
       	if (m_id != null && m_idt != null)
       	{
       		connectionStatus = CONNECT_CONNECTED;
       		if (check == false)
       		{
       			id = m_id;
       			idt = m_idt;
       		}
       	}
       	else
       	{
       		Log.d(DEBUGTAG,"MonCompeFree : AUTHENTIFICATION FAILED !");
       		connectionStatus = CONNECT_LOGIN_FAILED;
       		if (check == false)
       		{
	       		id = null;
	       		idt = null;
       		}
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
				return (new BufferedReader(new InputStreamReader(responseEntity.getContent(), "ISO8859_1")));
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
	 * getAuthRequest : perform a GET on an URL with p parameters
	 * do not provide id or idt in URL
	 * @param url : url to get
	 * @param p : parameters for the GET request (null if none)
	 * @param retour : set it to true of you want an InputStream with the page in return
	 * @return InputStream HTML Page or null
	 */
	public static InputStreamReader getAuthRequest(String url, List<String> p, boolean retour)
	{
		int c = CONNECT_CONNECTED;
		List<String> params;
		URL myURL;
		URLConnection ucon;
		HttpURLConnection h = null;

		Log.d(DEBUGTAG, "GET: " + url);
		
		if ((id == null) || (idt == null))
		{
			Log.d(DEBUGTAG, "GET : ON A JAMAIS ETE AUTHENTIFIE - ON S'AUTHENTIFIE");
			c = connectionFree(login, password, false);
		}
		params = new ArrayList<String>();
		params.add("id="+id);
		params.add("idt="+idt);		
		if (p != null)
		{
			params.addAll(p);
		}
		try
		{
			if (c == CONNECT_CONNECTED)
			{
				Log.d(DEBUGTAG, "GET : VERIF SI ON EST AUTHENTIFIE");
				myURL = new URL(url+"?"+makeStringForPost(params));

				Log.d(DEBUGTAG, "GET : URL "+myURL);
				ucon = myURL.openConnection();
				
				if (!(ucon instanceof HttpURLConnection)) throw new IOException("Not an HTTPconnection.");
				h = (HttpURLConnection) ucon;
				if (retour)
					h.setRequestMethod("GET");
				else
					h.setRequestMethod("HEAD");
				h.setInstanceFollowRedirects(false);
				h.setAllowUserInteraction(false);
				h.setRequestProperty("User-Agent", USER_AGENT);
				h.setDoInput(true);
				Log.d(DEBUGTAG, "HEADERS : "+h.getHeaderFields());
				Log.d(DEBUGTAG, "RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
				if (h.getHeaderFields().get("location") != null)
				{
					c = CONNECT_NOT_CONNECTED;
				}
			}
			if (c != CONNECT_CONNECTED)
			{
				if (h != null)
				{
					h.disconnect();
					h = null;
				}
				Log.d(DEBUGTAG, "GET : PAS AUTHENTIFIE SUR LA CONSOLE - SESSION EXPIREE");
				c = connectionFree(login, password, false);
				if (c == CONNECT_CONNECTED)
				{
					Log.d(DEBUGTAG, "GET :  REAUTHENTIFICATION OK");
					params.clear();
					params.add("id="+id);
					params.add("idt="+idt);		
					if (p != null)
					{
						params.addAll(p);
					}
					myURL = new URL(url+"?"+makeStringForPost(params));
					Log.d(DEBUGTAG, "GET : URL "+ myURL);
					ucon = myURL.openConnection();
					if (!(ucon instanceof HttpURLConnection)) throw new IOException("Not an HTTPconnection.");
					h = (HttpURLConnection) ucon;
					if (retour)
						h.setRequestMethod("GET");
					else
						h.setRequestMethod("HEAD");
					h.setAllowUserInteraction(false);
					h.setUseCaches(false);
					h.setInstanceFollowRedirects(false);
					h.setRequestProperty("User-Agent", USER_AGENT);
					h.setDoInput(true);
					Log.d(DEBUGTAG, "HEADERS : "+h.getHeaderFields());
					Log.d(DEBUGTAG, "RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
				}
			}
			else
			{
				Log.d(DEBUGTAG, "GET : AUTHENTIFICATION OK");
				c = CONNECT_CONNECTED;
			}
			if ((c == CONNECT_CONNECTED) && (retour == true))
			{
				Log.d(DEBUGTAG, "GET : LECTURE DONNEES");
				return (new InputStreamReader(h.getInputStream(), "ISO8859_1"));
			}
		}
		catch (Exception e)
		{
			Log.e(DEBUGTAG, "getAuthRequest "+e);
		}
		return (null);
	}

	public static InputStream getRequestIS(String url) {
		Log.d(DEBUGTAG, "GET: " + url);

		HttpClient client = new DefaultHttpClient(new BasicHttpParams());
		HttpGet getMethod = new HttpGet(url);
		getMethod.setHeader("User-Agent", USER_AGENT);
		try
		{
			HttpResponse httpResponse = client.execute(getMethod);
			if ((httpResponse != null))
			{
				HttpEntity responseEntity = httpResponse.getEntity();
				return responseEntity.getContent();
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

		Header locationHeader = postMethod.getFirstHeader("location");
		if (locationHeader != null)
		{
			String redirectLocation = locationHeader.getValue();
			Log.d(DEBUGTAG, "RESPONSE : "+redirectLocation);
		}
		try
		{
			postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse httpResponse = client.execute(postMethod);
        	if ((httpResponse != null) && (retour))
        	{
	        	HttpEntity responseEntity = httpResponse.getEntity();
	        	return (new BufferedReader(new InputStreamReader(responseEntity.getContent(), "ISO8859_1")));
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

	private static String makeStringForPost(List<String> listParameter)
    {
        String listConcat = "";
        if(listParameter.size() > 0)
        {
            listConcat += listParameter.get(0);
            for(int i = 1 ; i < listParameter.size() ; i++)
            {
                listConcat += "&";
                listConcat += listParameter.get(i);
            }
        }
        return listConcat;
    }
	
	/**
	 * BufferedReader to String conversion
	 * @param	BufferedReader
	 * @return	String
	 * @throws	IOException
	 */
	public static String getPage(BufferedReader reader)
	{
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
			Log.e(DEBUGTAG, "getPage: "+e);
		     return null;
		}		
		return sb.toString();
	}
}
