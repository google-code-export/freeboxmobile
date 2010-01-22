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
import java.net.URLEncoder;
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
import org.apache.http.message.BasicNameValuePair;
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

	// TODO : Supprimer
	public static String getId()
	{
		return (id);
	}

	// TODO : Supprimer
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

	public static AlertDialog showError(Activity a)
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
		return errorAlert;
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
	
	private static int checkConnected(int defValue)
	{
		if ((id == null) || (idt == null))
		{
			Log.d(DEBUGTAG, "checkConnected : ON A JAMAIS ETE AUTHENTIFIE - ON S'AUTHENTIFIE");
			return (connectionFree(login, password, false));
		}
		else
			return (defValue);
	}

	// TODO : Passera en private ou supprimer ?
	public static int connectFree()
	{
		return FBMHttpConnection.connectionFree(login, password, false);
	}

	public static int connectFreeCheck(String l, String p)
	{
		return FBMHttpConnection.connectionFree(l, p, true);		
	}

	// En cas de résussite : http://adsl.free.fr/compte/console.pl?id=417389&idt=10eb38933107f10c
	// En cas d'erreur de login/pass : /login/login.pl?login=0909090909&error=1
	/**
	 * connectionFree : identifie sur le portail de Free avec le login/pass demandé
	 * @param l : login (identifiant = numéro de téléphone Freebox)
	 * @param p : password (mot de passe Freebox)
	 * @param check : true = juste vérifier les identifiants / false : se connecter (ie stocker id & idt)
	 * @return CONNECT_CONNECTED || CONNECT_NOT_CONNECTED || CONNECT_LOGIN_FAILED
	 */
	private static int connectionFree(String l, String p, boolean check)
	{
		String m_id = null;
		String m_idt = null;
		HttpURLConnection h = null;
		
		Log.d(DEBUGTAG,"Connect Free start ");
        try
        {
    		List<NameValuePair> listParameter = new ArrayList<NameValuePair>();
    		listParameter.add(new BasicNameValuePair("login",l));   		
    		listParameter.add(new BasicNameValuePair("pass",p));

    		h = prepareConnection(serverUrl, "POST");
    		h.setDoOutput(true);
    		OutputStreamWriter o = new OutputStreamWriter(h.getOutputStream());
			o.write(makeStringForPost(listParameter, false));
			o.flush();
			o.close();
			if (h.getHeaderFields().get("location") != null)
			{
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

	private static HttpURLConnection prepareConnection(String url, String method) throws IOException
	{
		URL u = new URL(url);
		Log.d(DEBUGTAG, "PREPARECONNECTION : URL "+ u);
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.setRequestMethod(method);
		c.setAllowUserInteraction(false);
		c.setUseCaches(false);
		c.setInstanceFollowRedirects(false);
		c.setRequestProperty("User-Agent", USER_AGENT);
		return (c);
	}
	
	/**
	 * Donwload a file
	 * @param file destination file
	 * @param url source url
	 * @param p parameters for GET (in p not null, url must not have parameters)
	 * @param auth true if we must add id & idt paramters for authentification (if true, url must not have parameters)
	 * @return 1 if success
	 */
	public static boolean getFile(File file, String url, List<NameValuePair> p, boolean auth)
	{
		HttpURLConnection c = null;
		FileOutputStream f;
		int len;
        byte[] buffer = new byte[1024];
        int connected;

        connected = checkConnected(CONNECT_CONNECTED);
		Log.d(DEBUGTAG,"->DOWNLOADING FILE : "+url);
        try
        {
			if (connected == CONNECT_CONNECTED)
			{
				Log.d(DEBUGTAG, "GETFILE : VERIF SI ON EST AUTHENTIFIE");
				c = prepareConnection(url+"?"+makeStringForPost(p, auth), "GET");
				c.setDoInput(true);
				Log.d(DEBUGTAG, "HEADERS : "+c.getHeaderFields());
				Log.d(DEBUGTAG, "RESPONSE : "+c.getResponseCode()+" "+c.getResponseMessage());
				if (c.getHeaderFields().get("location") != null)
				{
					connected = CONNECT_NOT_CONNECTED;
				}
			}
			if (connected != CONNECT_CONNECTED)
			{
				if (c != null)
				{
					c.disconnect();
					c = null;
				}
				Log.d(DEBUGTAG, "GETFILE : PAS AUTHENTIFIE SUR LA CONSOLE - SESSION EXPIREE");
				connected = connectionFree(login, password, false);
				if (connected == CONNECT_CONNECTED)
				{
					Log.d(DEBUGTAG, "GETFILE : REAUTHENTIFICATION OK");
					c = prepareConnection(url+"?"+makeStringForPost(p, auth), "GET");
					c.setDoInput(true);
					Log.d(DEBUGTAG, "HEADERS : "+c.getHeaderFields());
					Log.d(DEBUGTAG, "RESPONSE : "+c.getResponseCode()+" "+c.getResponseMessage());
				}
			}
			else
			{
				Log.d(DEBUGTAG, "GETFILE : AUTHENTIFICATION OK");
				connected = CONNECT_CONNECTED;
			}
			if (connected == CONNECT_CONNECTED)
			{
				Log.d(DEBUGTAG, "GETFILE : LECTURE FICHIER");
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
		    	return true;
			}
        }
        catch (Exception e)
        {
        	Log.e(DEBUGTAG, "getFile : "+e);
			e.printStackTrace();
		}
        return false;
	}

	/**
	 * Sends a GET request
	 * @param url : url to get
	 * @retour true pour avec une valeur non nulle en retour (si on veut le contenu de la page ou pas)
	 * @return HttpResponse response from the server
	 */
	// TODO : OBSOLETE, to remove
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
	 * @param p : parameters for the GET request (null if none) (url must not have parameters in not null)
	 * @param auth : true if you need id & idt added automatically for authentification on Free console (url must not have parameters in this case)
	 * @param retour : set it to true of you want an InputStream with the page in return
	 * @return InputStream HTML Page or null
	 */
	public static InputStream getAuthRequest(String url, List<NameValuePair> p, boolean auth, boolean retour)
	{
		int c;
		HttpURLConnection h = null;

		c = checkConnected(CONNECT_CONNECTED);
		try
		{
			if (c == CONNECT_CONNECTED)
			{
				Log.d(DEBUGTAG, "GET : VERIF SI ON EST AUTHENTIFIE");
				if (retour)
					h = prepareConnection(url+"?"+makeStringForPost(p, auth), "GET");
				else
					h = prepareConnection(url+"?"+makeStringForPost(p, auth), "HEAD");

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
					if (retour)
						h = prepareConnection(url+"?"+makeStringForPost(p, auth), "GET");
					else
						h = prepareConnection(url+"?"+makeStringForPost(p, auth), "HEAD");
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
				return (h.getInputStream());
			}
		}
		catch (Exception e)
		{
			Log.e(DEBUGTAG, "getAuthRequest "+e);
		}
		return (null);
	}

	// TODO : Obsolete, to remove
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
	* @param auth : true pour ajouter automatiquement id & idt
	* @retour true pour avec une valeur non nulle en retour (si on veut le contenu de la page ou pas)
	*/
	public static InputStreamReader postAuthRequest(String url, List<NameValuePair> p, boolean auth, boolean retour)
	{
		HttpURLConnection h = null;
		int c;

		Log.d(DEBUGTAG, "POST: " + url);
		try
		{
			c = checkConnected(CONNECT_CONNECTED);
			if (c == CONNECT_CONNECTED)
			{
//				if (auth)
					h = prepareConnection(serverUrl+(auth ? "?"+makeStringForPost(null, auth) : ""), "POST");
//				else
//					h = prepareConnection(serverUrl, "POST");
				h.setDoOutput(true);
				if (retour)
					h.setDoInput(true);
				OutputStreamWriter o = new OutputStreamWriter(h.getOutputStream());
				o.write(makeStringForPost(p, false));
				o.flush();
				o.close();
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
				Log.d(DEBUGTAG, "POST : PAS AUTHENTIFIE SUR LA CONSOLE - SESSION EXPIREE");
				c = connectionFree(login, password, false);
				if (c == CONNECT_CONNECTED)
				{
					Log.d(DEBUGTAG, "POST :  REAUTHENTIFICATION OK");
					h = prepareConnection(serverUrl+(auth ? "?"+makeStringForPost(null, auth) : ""), "POST");
//					h = prepareConnection(serverUrl, "POST");
					h.setDoOutput(true);
					if (retour)
						h.setDoInput(true);
					OutputStreamWriter o = new OutputStreamWriter(h.getOutputStream());
					o.write(makeStringForPost(p, false));
					o.flush();
					o.close();
				}
			}
			else
			{
				Log.d(DEBUGTAG, "POST : AUTHENTIFICATION OK");
				c = CONNECT_CONNECTED;
			}
			if ((c == CONNECT_CONNECTED) && (retour))
			{
				Log.d(DEBUGTAG, "POST : LECTURE DONNEES");
				return (new InputStreamReader(h.getInputStream(), "ISO8859_1"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return (null);
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
	// TODO : Obsolete, to remove
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

	private static String makeStringForPost(List<NameValuePair> p, boolean auth)
    {
        String listConcat = "";
		if ((p == null) && (auth))
		{
			p = new ArrayList<NameValuePair>();
		}
		if (auth)
		{
			p.add(new BasicNameValuePair("id",id));
			p.add(new BasicNameValuePair("idt",idt));
		}
        if ((p != null) && (p.size() > 0))
        {
            listConcat += URLEncoder.encode(p.get(0).getName());
            listConcat += '=';
            listConcat += URLEncoder.encode(p.get(0).getValue());
            for(int i = 1 ; i < p.size() ; i++)
            {
                listConcat += "&";
                listConcat += URLEncoder.encode(p.get(i).getName());
                listConcat += '=';
                listConcat += URLEncoder.encode(p.get(i).getValue());
            }
        }
//        Log.d(DEBUGTAG, "makeStringForPost : "+listConcat);
        return (listConcat);
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
