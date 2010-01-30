package org.madprod.freeboxmobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List; 

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlrpc.android.XMLRPCClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
	private static final String suiviTechUrl = "http://adsl.free.fr/suivi/suivi_techgrrr.pl";
	public static final String frimousseUrl = "http://www.frimousse.org/outils/xmlrpc";
	
	public static ProgressDialog httpProgressDialog = null;
	public static AlertDialog errorAlert = null;

	/**
	 * Init les variables statiques
	 * Ferme le progressdialog si il était ouvert (arrive dans le cas d'un screen rotation)
	 * Doit être appelé dans le onCreate de chaque Activity
	 * @param a activity
	 */
	public static void initVars(Activity a, Context c)
	{
		if (c == null)
		{
			c = a.getBaseContext();
		}
		USER_AGENT = c.getString(R.string.app_name)+"/"+c.getString(R.string.app_version)+" (Linux; U; Android; fr-fr;)";
		// On teste pour si on entre ici suite
        if (httpProgressDialog != null)
        {
           	httpProgressDialog.show();
        }
        if (errorAlert != null)
        {
           	errorAlert.show();
        }
        title = c.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_TITLE, null);
		login = c.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_USER, null);
		password = c.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_PASSWORD, null);
	}

	/**
	 * A utiliser lors d'un changement de compte actif
	 * @param a
	 */
	public static void initCompte(Activity a)
	{
		id = null;
		idt = null;
		if (a != null)
			initVars(a, null);
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
//	public static void setVars(String l, String p, Context c)
//	{
//		login = l;
//		password = p;
//	}
	
	public static String getTitle()
	{
		return (title);
	}

	public static String getIdentifiant()
	{
		return (login);
	}

	public static void showProgressDialog(Activity a)
	{
		httpProgressDialog = ProgressDialog.show(a, "Mon Compte Free", "Connexion en cours ...", true,false);
	}

	public static void showProgressDialog2(Activity a)
	{
		httpProgressDialog = ProgressDialog.show(a, "Mise à jour des données", "Connexion en cours ...", true,false);
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
			return (connectionFree(login, password));
		}
		else
			return (defValue);
	}

	public static ContentValues connectFreeCheck(String l, String p)
	{
		String mLogin;
		String mPassword;
		String mId;
		String mIdt;
		ContentValues v = null;

		if (FBMHttpConnection.connectionFree(l, p) == CONNECT_CONNECTED)
		{
			// backup des données du compte loggué pour les restaurer après
			mLogin = login;
			mPassword = password;
			mId = id;
			mIdt = idt;
			// Reset de id & idt afin que ce compte (qui n'est peut etre pas celui sélectionné comme actif)
			// ne reste pas connecté
			initCompte(null);

			login = l;
			password = p;
			v = parseConsole(l, p);
			Log.d(DEBUGTAG, "connectFreeCheck : "+v);

			// restauration des données présentes avant
			login = mLogin;
			password = mPassword;
			id = mId;
			idt = mIdt;
		}
		else
		{
			Log.d(DEBUGTAG, "connectFreeCheck failed");
		}
		return (v);
	}

	private static String parsePage(BufferedReader br, String tag, String first, String last)
	{
		String s;
		String r = "";
		int start;
		int end;

		try
		{
			while ( (s=br.readLine())!= null && s.indexOf(tag) == -1)
			{
			}
			if ((s != null) && (s.indexOf(tag) != -1))
			{
				if (s.indexOf(first) == -1)
				{
					while ( (s=br.readLine())!= null && s.indexOf(first) == -1)
					{
					}
				}
				if (s != null && s.indexOf(first) != -1)
				{
					start = s.indexOf(first) + first.length();
					end = s.indexOf(last);
					if ((start != -1) && (end != -1))
						r = s.substring(start,end);
					else
						br.reset();
				}
			}
			else
				br.reset();
		}
		catch (Exception e)
		{
			Log.e(DEBUGTAG, "parsePage - "+tag+" : " + e.getMessage());
			e.printStackTrace();
		}
		Log.d(DEBUGTAG, "["+tag+"] "+r);
		return r;
	}
	
	/**
	 * Parse suiviTechUrl
	 * @param l : login du compte à parser
	 * @param p : mot de passe du compte à parser
	 */
	private static ContentValues parseConsole(String l, String p)
	{
		// TODO : Checker et stocker si la ligne est dégroupée ou pas
		InputStream is = getAuthRequest(suiviTechUrl, null, true, true);
		try
		{
			if (is != null)
			{
				ContentValues consoleValues = new ContentValues();
		    	BufferedReader br = new BufferedReader(new InputStreamReader(is, "ISO8859_1"));
		    	br.mark(20000);
		    	consoleValues.put(KEY_LINETYPE,
		    			parsePage(br, "Raccordée actuellement en offre", "0000\">", "</font>")
		    			.contains("Freebox dégroupé")?"1":"0");
		    	Log.d(DEBUGTAG,"type:"+consoleValues.get(KEY_LINETYPE));
		    	consoleValues.put(KEY_NRA, parsePage(br, "NRA :", "red\">", "</"));
		    	consoleValues.put(KEY_LINELENGTH, parsePage(br, "Longueur :", "red\">", " mètres"));
		    	consoleValues.put(KEY_ATTN, parsePage(br, "Affaiblissement :", "red\">", " dB"));
		    	consoleValues.put(KEY_IP, parsePage(br, "Votre adresse IP", "<b>", " / "));
		    	consoleValues.put(KEY_TEL, parsePage(br, "téléphone Freebox", "<b>", "</b>"));
				br.close();
				if (consoleValues.get(KEY_IP)!= "")
				{
					URI uri = URI.create(frimousseUrl);
					XMLRPCClient client = new XMLRPCClient(uri);
					Object[] response = (Object[]) client.call("getDSLAMListForPool", consoleValues.get(KEY_IP));
					if (response.length > 0)
					{
						Log.d(DEBUGTAG, "XMLRPC : "+response[0]);
						consoleValues.put(KEY_DSLAM,(String) response[0]);
					}
					else
					{
						consoleValues.put(KEY_DSLAM,"");
						Log.d(DEBUGTAG, "DSLAM pas trouvé");
					}
				}
				else
				{
					consoleValues.put(KEY_DSLAM,"");
				}
				return (consoleValues);
			}
			return (null);
		}
		catch (Exception e)
		{
			Log.e(DEBUGTAG, "parseConsole : " + e.getMessage());
			e.printStackTrace();
		}
		return (null);
	}

	// En cas de résussite : http://adsl.free.fr/compte/console.pl?id=417389&idt=10eb38933107f10c
	// En cas d'erreur de login/pass : /login/login.pl?login=0909090909&error=1
	/**
	 * connectionFree : identifie sur le portail de Free avec le login/pass demandé
	 * @param l : login (identifiant = numéro de téléphone Freebox)
	 * @param p : password (mot de passe Freebox)
	 * @return CONNECT_CONNECTED || CONNECT_NOT_CONNECTED || CONNECT_LOGIN_FAILED
	 */
	private static int connectionFree(String l, String p)
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
				Log.d(DEBUGTAG, "connectFree : "+s);
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
        	id = null;
        	idt = null;
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
   			id = m_id;
   			idt = m_idt;
       	}
       	else
       	{
       		Log.d(DEBUGTAG,"MonCompeFree : AUTHENTIFICATION FAILED !");
       		connectionStatus = CONNECT_LOGIN_FAILED;
       		id = null;
       		idt = null;
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
				connected = connectionFree(login, password);
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
				c = connectionFree(login, password);
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
				h = prepareConnection(url+(auth ? "?"+makeStringForPost(null, auth) : ""), "POST");
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
				c = connectionFree(login, password);
				if (c == CONNECT_CONNECTED)
				{
					Log.d(DEBUGTAG, "POST :  REAUTHENTIFICATION OK");
					h = prepareConnection(url+(auth ? "?"+makeStringForPost(null, auth) : ""), "POST");
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
        	try
        	{
                listConcat += URLEncoder.encode(p.get(0).getName(), "iso-8859-1");
                listConcat += '=';
                listConcat += URLEncoder.encode(p.get(0).getValue(), "iso-8859-1");
        	}
        	catch (Exception e)
        	{
            	Log.d(DEBUGTAG, "makeStringForPost PB ENCODE : "+e);
                listConcat += URLEncoder.encode(p.get(0).getName());
                listConcat += '=';
                listConcat += URLEncoder.encode(p.get(0).getValue());        		
        	}
            for(int i = 1 ; i < p.size() ; i++)
            {
                listConcat += "&";
                try
                {
	                listConcat += URLEncoder.encode(p.get(i).getName(), "iso-8859-1");
	                listConcat += '=';
	                listConcat += URLEncoder.encode(p.get(i).getValue(), "iso-8859-1");
                }
                catch (Exception e)
                {
                	Log.d(DEBUGTAG, "makeStringForPost PB ENCODE : "+e);
	                listConcat += URLEncoder.encode(p.get(i).getName());
	                listConcat += '=';
	                listConcat += URLEncoder.encode(p.get(i).getValue());
                }
            }
        }
        Log.d(DEBUGTAG, "makeStringForPost : "+listConcat);
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
