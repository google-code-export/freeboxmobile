package org.madprod.freeboxmobile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
	private static String fbmversion = "";
	// Variables id et idt d'accès à MonCompteFree
	private static String id = null;
	private static String idt = null;

	public static String fbmlog = "";
//	private static String pagesCharset = "ISO8859_1";

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
		fbmversion = c.getString(R.string.app_version);
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

	public static void FBMLog(String s)
	{
		Log.d(DEBUGTAG, s);
		fbmlog += s+"\n";
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
			FBMLog("checkConnected : ON A JAMAIS ETE AUTHENTIFIE - ON S'AUTHENTIFIE");
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
			v.put(KEY_FBMVERSION, fbmversion);
			FBMLog("connectFreeCheck : "+v);

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

	private static String parsePage(String s, String tag, String first, String last)
	{
		String r = "";
		int start;
		int end;
		int itag;

		itag = s.indexOf(tag);
		if (itag != -1)
		{
			start = s.indexOf(first, itag);
			if (start != -1)
			{
				end = s.indexOf(last, start);
				if (end != -1)
				{
					start += first.length();
					r = s.substring(start,end);
				}
				else
					FBMLog("parsePage end pb : "+tag);
			}
			else
				FBMLog("parsePage start pb : "+tag);
		}
		else
			FBMLog("parsePage itag pb : "+tag);
		// On supprimer les éventuels tags HTML qui seraient dans la chaine découpée
		String dest="";
		int l = r.length();
		int i = 0;
		while (i<l)
		{
			if (r.charAt(i) == '<')
			{
				while ((i < l) && (r.charAt(i) != '>'))
				{
					i++;
				}
				if (i<l)
					i++;
			}
			if ((i<l) && (r.charAt(i) != '<'))
			{
				dest += r.charAt(i);
				i++;
			}
		}
		FBMLog("["+tag+"] "+dest);
		return dest;
	}
	
	/**
	 * Parse suiviTechUrl
	 * @param l : login du compte à parser
	 * @param p : mot de passe du compte à parser
	 */
	private static ContentValues parseConsole(String l, String p)
	{
		try
		{
			ContentValues consoleValues = new ContentValues();
			String br = getPage(getAuthRequestISR(suiviTechUrl, null, true, true));
			String offre = parsePage(br, "Raccordée actuellement en offre", "<font", "</font>");
			if (offre.contains("Freebox dégroupé"))
		    	consoleValues.put(KEY_LINETYPE, "1");
			else if (offre.contains("Fibre Optique"))
				consoleValues.put(KEY_LINETYPE, "2");
			else
				consoleValues.put(KEY_LINETYPE, "0");
	    	// TODO : enlever la ligne suivante après debug
	    	if (consoleValues.get(KEY_LINETYPE).equals("0"))
				FBMLog("DEBUG INFO TECHNIQUES : "+br);		    		
	    	FBMLog("type:"+consoleValues.get(KEY_LINETYPE));
	    	consoleValues.put(KEY_NRA, parsePage(br, "NRA :", "\">", "</"));
	    	consoleValues.put(KEY_LINELENGTH, parsePage(br, "Longueur :", "red\">", " mètres"));
	    	consoleValues.put(KEY_ATTN, parsePage(br, "Affaiblissement :", "red\">", " dB"));
	    	consoleValues.put(KEY_IP, parsePage(br, "Votre adresse IP", "<b>", " / "));
	    	consoleValues.put(KEY_TEL, parsePage(br, "téléphone Freebox", "<b>", "</b>"));
			if (consoleValues.get(KEY_IP) != "")
			{
				URI uri = URI.create(frimousseUrl);
				XMLRPCClient client = new XMLRPCClient(uri);
				Object[] response = (Object[]) client.call("getDSLAMListForPool", consoleValues.get(KEY_IP));
				if (response.length > 0)
				{
					FBMLog("XMLRPC : "+response[0]);
					consoleValues.put(KEY_DSLAM,(String) response[0]);
				}
				else
				{
					consoleValues.put(KEY_DSLAM,"");
					FBMLog("DSLAM pas trouvé");
				}
			}
			else
			{
				consoleValues.put(KEY_DSLAM,"");
			}
			return (consoleValues);
		}
		catch (Exception e)
		{
			FBMLog("parseConsole : " + e.getMessage());
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
		
		FBMLog("Connect Free start ");
        try
        {
    		List<NameValuePair> listParameter = new ArrayList<NameValuePair>();
    		listParameter.add(new BasicNameValuePair("login",l));   		
    		listParameter.add(new BasicNameValuePair("pass",p));

    		h = prepareConnection(serverUrl, "POST");
    		h.setDoOutput(true);
    		OutputStreamWriter o = new OutputStreamWriter(h.getOutputStream());
			o.write(makeStringForPost(listParameter, false, null));
			o.flush();
			o.close();
			if (h.getHeaderFields().get("location") != null)
			{
				String s = h.getHeaderFields().get("location").toString();
				FBMLog("connectFree : "+s);
	    		String priv;
				if (s.indexOf("idt=")>-1)
				{
					priv = s.substring(s.indexOf("idt=")+4);
					if (priv.indexOf("]")>-1)
					{
						priv = priv.substring(0, priv.indexOf(']'));
					}
					m_idt = priv;
					FBMLog("idt :"+priv);
					priv = s.substring(s.indexOf("?id=")+4);
					priv = priv.substring(0, priv.indexOf('&'));
					m_id = priv;
					FBMLog("id :"+priv);
				}
			}
        }
		catch (Exception e)
		{
			FBMLog("connectFree : "+e);
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
       		FBMLog("MonCompeFree : AUTHENTIFICATION FAILED !");
       		connectionStatus = CONNECT_LOGIN_FAILED;
       		id = null;
       		idt = null;
       	}
       	return connectionStatus;
    }

	private static HttpURLConnection prepareConnection(String url, String method) throws IOException
	{
		URL u = new URL(url);
		FBMLog("PREPARECONNECTION : URL["+ u +"]METHOD ["+method+"]");
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
				c = prepareConnection(url+"?"+makeStringForPost(p, auth, null), "GET");
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
					c = prepareConnection(url+"?"+makeStringForPost(p, auth, null), "GET");
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
	public static InputStream getAuthRequestIS(String url, List<NameValuePair> p, boolean auth, boolean retour)
	{
		// TODO : Renvoyer un InputStreamReader et non un InputStream
		int c;
		HttpURLConnection h = null;

		c = checkConnected(CONNECT_CONNECTED);
		try
		{
			FBMLog("-- GET IS : "+url);
			if (c == CONNECT_CONNECTED)
			{
				FBMLog("GETIS : VERIF SI ON EST AUTHENTIFIE");
				if (retour)
					h = prepareConnection(url+"?"+makeStringForPost(p, auth, null), "GET");
				else
					h = prepareConnection(url+"?"+makeStringForPost(p, auth, null), "HEAD");

				h.setDoInput(true);
				FBMLog("HEADERS : "+h.getHeaderFields());
				FBMLog("RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
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
				FBMLog("GETIS : PAS AUTHENTIFIE SUR LA CONSOLE - SESSION EXPIREE");
				c = connectionFree(login, password);
				if (c == CONNECT_CONNECTED)
				{
					FBMLog("GETIS :  REAUTHENTIFICATION OK");
					if (retour)
						h = prepareConnection(url+"?"+makeStringForPost(p, auth, null), "GET");
					else
						h = prepareConnection(url+"?"+makeStringForPost(p, auth, null), "HEAD");
					h.setDoInput(true);
					FBMLog("HEADERS : "+h.getHeaderFields());
					FBMLog("RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
				}
			}
			else
			{
				FBMLog("GETIS : AUTHENTIFICATION OK");
				c = CONNECT_CONNECTED;
			}
			if ((c == CONNECT_CONNECTED) && (retour == true))
			{
				FBMLog("GETIS : LECTURE DONNEES");
/*				FBMLog("GETIS : TYPE : "+h.getContentType());
				if (h.getContentType() != null)
				{
					String temp = h.getContentType();
					int pos = temp.indexOf("charset=");
					pagesCharset = temp.substring(pos+8);
					FBMLog("GET : CHARSET : "+pagesCharset);
				}
				*/
				return (h.getInputStream());
			}
		}
		catch (Exception e)
		{
			FBMLog("getAuthRequestIS "+e);
			e.printStackTrace();
		}
		return (null);
	}

	static String getCharset(String temp, String defaultCharset)
	{
		String charset = defaultCharset;
		if (temp != null)
		{
			int pos = temp.indexOf("charset=");
			if (pos != -1)
			{
				charset = temp.substring(pos+8);
				FBMLog("CHARSET : "+charset);
			}
			else
				FBMLog("CHARSET : not found - keep default "+charset);
		}
		else
			FBMLog("CHARSET : "+charset);
		return charset;
	}

	/**
	 * getAuthRequest : perform a GET on an URL with p parameters
	 * do not provide id or idt in URL
	 * @param url : url to get
	 * @param p : parameters for the GET request (null if none) (url must not have parameters in not null)
	 * @param auth : true if you need id & idt added automatically for authentification on Free console (url must not have parameters in this case)
	 * @param retour : set it to true of you want an InputStream with the page in return
	 * @return InputStreamReader HTML Page or null
	 */
	public static InputStreamReader getAuthRequestISR(String url, List<NameValuePair> p, boolean auth, boolean retour)
	{
		int c;
		HttpURLConnection h = null;
		String charset = "ISO8859_1";

		c = checkConnected(CONNECT_CONNECTED);
		try
		{
			FBMLog("-- GET ISR : "+url);
			if (c == CONNECT_CONNECTED)
			{
				FBMLog("GETISR : VERIF SI ON EST AUTHENTIFIE");
				if (retour)
					h = prepareConnection(url+"?"+makeStringForPost(p, auth, charset), "GET");
				else
					h = prepareConnection(url+"?"+makeStringForPost(p, auth, charset), "HEAD");
				h.setDoInput(true);
				charset = getCharset(h.getContentType(), charset);
/*				if (h.getContentType() != null)
				{
					String temp = h.getContentType();
					int pos = temp.indexOf("charset=");
					if (pos != -1)
					{
						charset = temp.substring(pos+8);
					}
					FBMLog("GETISR : CHARSET : "+charset);
				}
*/
				FBMLog("HEADERS : "+h.getHeaderFields());
				FBMLog("RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
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
				FBMLog("GETISR : PAS AUTHENTIFIE SUR LA CONSOLE - SESSION EXPIREE");
				c = connectionFree(login, password);
				if (c == CONNECT_CONNECTED)
				{
					FBMLog("GETISR :  REAUTHENTIFICATION OK");
					if (retour)
						h = prepareConnection(url+"?"+makeStringForPost(p, auth, charset), "GET");
					else
						h = prepareConnection(url+"?"+makeStringForPost(p, auth, charset), "HEAD");
					h.setDoInput(true);
					FBMLog("HEADERS : "+h.getHeaderFields());
					FBMLog("RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
				}
			}
			else
			{
				FBMLog("GETISR : AUTHENTIFICATION OK");
				c = CONNECT_CONNECTED;
			}
			if ((c == CONNECT_CONNECTED) && (retour == true))
			{
				FBMLog("GETISR : LECTURE DONNEES - TYPE : "+h.getContentType());
				charset = getCharset(h.getContentType(), charset);
/*
				if (h.getContentType() != null)
				{
					String temp = h.getContentType();
					charset = temp.substring(temp.indexOf("charset=")+8);
					FBMLog("GETISR : CHARSET : "+charset);
				}
*/
				return (new InputStreamReader(h.getInputStream(), charset));
			}
		}
		catch (Exception e)
		{
			FBMLog("getAuthRequestISR "+e);
			e.printStackTrace();
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
		String pagesCharset = "ISO8859_1";
		
		FBMLog("-- POST: " + url);
		Log.d(DEBUGTAG, "POST: " + url);
		try
		{
			c = checkConnected(CONNECT_CONNECTED);
			if (c == CONNECT_CONNECTED)
			{
				h = prepareConnection(url+(auth ? "?"+makeStringForPost(null, auth, null) : ""), "POST");
				h.setDoOutput(true);
				if (retour)
					h.setDoInput(true);
				OutputStreamWriter o = new OutputStreamWriter(h.getOutputStream());
				pagesCharset = getCharset(h.getContentType(), pagesCharset);
/*				if (h.getContentType() != null)
				{
					String temp = h.getContentType();
					pagesCharset = temp.substring(temp.indexOf("charset=")+8);
					FBMLog("POST : CHARSET : "+pagesCharset);
				}
*/
				o.write(makeStringForPost(p, false, pagesCharset));
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
				FBMLog("POST : PAS AUTHENTIFIE SUR LA CONSOLE - SESSION EXPIREE");
				Log.d(DEBUGTAG, "POST : PAS AUTHENTIFIE SUR LA CONSOLE - SESSION EXPIREE");
				c = connectionFree(login, password);
				if (c == CONNECT_CONNECTED)
				{
					FBMLog("POST :  REAUTHENTIFICATION OK");
					Log.d(DEBUGTAG, "POST :  REAUTHENTIFICATION OK");
					h = prepareConnection(url+(auth ? "?"+makeStringForPost(null, auth, pagesCharset) : ""), "POST");
					h.setDoOutput(true);
					if (retour)
						h.setDoInput(true);
					OutputStreamWriter o = new OutputStreamWriter(h.getOutputStream());
					o.write(makeStringForPost(p, false, pagesCharset));
					o.flush();
					o.close();
				}
			}
			else
			{
				FBMLog("POST : AUTHENTIFICATION OK");
				Log.d(DEBUGTAG, "POST : AUTHENTIFICATION OK");
				c = CONNECT_CONNECTED;
			}
			if ((c == CONNECT_CONNECTED) && (retour))
			{
				FBMLog("POST : LECTURE DONNEES");
				Log.d(DEBUGTAG, "POST : LECTURE DONNEES");
				pagesCharset = getCharset(h.getContentType(), pagesCharset);
/*				if (h.getContentType() != null)
				{
					String temp = h.getContentType();
					int pos = temp.indexOf("charset=");
					pagesCharset = temp.substring(pos+8);
					FBMLog("POST : CHARSET : "+pagesCharset);
				}
*/
				return (new InputStreamReader(h.getInputStream(), pagesCharset));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return (null);
	}

	private static String makeStringForPost(List<NameValuePair> p, boolean auth, String charset)
    {
        String listConcat = "";
        
        if (charset == null)
        {
        	charset = "iso-8859-1";
        }
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
                listConcat += URLEncoder.encode(p.get(0).getName(), charset);
                listConcat += '=';
                listConcat += URLEncoder.encode(p.get(0).getValue(), charset);
        	}
        	catch (Exception e)
        	{
        		FBMLog("makeStringForPost PB ENCODE : "+e);
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
	                listConcat += URLEncoder.encode(p.get(i).getName(), charset);
	                listConcat += '=';
	                listConcat += URLEncoder.encode(p.get(i).getValue(), charset);
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
//        Log.d(DEBUGTAG, "makeStringForPost : "+listConcat);
        return (listConcat);
    }
	
	/**
	 * BufferedReader to String conversion
	 * @param	BufferedReader
	 * @return	String
	 * @throws	IOException
	 */
/*  NOT USED ANYMORE
  	public static String getPage(InputStream is)
 
	{
		FBMHttpConnection.FBMLog("getPage start");
		if (is == null)
		{
			FBMHttpConnection.FBMLog("getPage is null");
			return null;
		}
		try
		{
			FBMHttpConnection.FBMLog("getPage try");
			InputStreamReader isr = new InputStreamReader(is, pagesCharset);
			if (isr == null) {
				FBMHttpConnection.FBMLog("isr == null!");
				return null;
			}
			return getPage(isr);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		FBMHttpConnection.FBMLog("getPage is end null");
		return null;
	}
*/
	
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
			Log.e(DEBUGTAG, "getPage: "+e);
			return null;
		}		
		return sb.toString();
	}
	

	
	
	/**
	 * Post d'un fichier en multipart
	 * 
	 * @throws IOException
	 * @return Le contenu de la réponse si le post a fonctionné, null sinon
	 */
	public static String postFileAuthRequest(String uploadFaxUrl, List<NameValuePair> params, File fileToPost, int expectedHttpStatus, boolean auth) throws IOException{
		final String END 		= "\r\n";
		final String TWO_HYPHENS = "--";
		final String BOUNDARY 	= "*****++++++************++++++++++++";
		final String END_CONTENT_DISPOSITION = TWO_HYPHENS + BOUNDARY + END;
		
		int connected = checkConnected(CONNECT_CONNECTED);
		Log.d(DEBUGTAG,"->POSTING FILE TO "+uploadFaxUrl);
		if (connected == CONNECT_CONNECTED){
		
			if(params == null){
				params = new ArrayList<NameValuePair>();
			}
			
			if (auth)
			{
				params.add(new BasicNameValuePair("id",id));
				params.add(new BasicNameValuePair("idt",idt));
			}
			
			final URL url = new URL(uploadFaxUrl);
			final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			//Connexion avec le serveur
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
	
			/* Properties spécifique au multipart */
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+ BOUNDARY);
	
			final DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
			final FileInputStream fStream = new FileInputStream(fileToPost);
			final int bufferSize = 1024;
			final byte[] buffer = new byte[bufferSize];
			
			ds.writeBytes(END_CONTENT_DISPOSITION);
			
			for (NameValuePair pair : params) {
	           	ds.writeBytes("Content-Disposition: form-data; name=\""+pair.getName()+"\""+END+END+pair.getValue()+END);
	   			ds.writeBytes(END_CONTENT_DISPOSITION);
			}
	
			ds.writeBytes("Content-Disposition: form-data; name=\"document\"; filename=\""+ fileToPost.getAbsolutePath() + "\"" + END);
			ds.writeBytes("Content-Type : application/pdf"+END+END);
			
			int length = -1;
			while ((length = fStream.read(buffer)) != -1) {
				ds.write(buffer, 0, length);
			}
			
			ds.writeBytes(END);
			ds.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + END);
			
			/* close streams */
			fStream.close();
			ds.flush();
			ds.close();
	
			//Test du code de retour
			if (conn.getResponseCode() != expectedHttpStatus){
				Log.d(DEBUGTAG, "Mauvais code Http retourné lors du post multipart : "+conn.getResponseCode()+" au lieu de "+expectedHttpStatus);
				return null;
			}
			
			//Le code de retour est correct on retourne le contenu de la réponse Http
			final StringBuffer b = new StringBuffer();
			final InputStream is = conn.getInputStream();
			final byte[] data = new byte[bufferSize];
			int leng = -1;
			while ((leng = is.read(data)) != -1) {
				b.append(new String(data, 0, leng));
			}
			return b.toString();
		}
		return null;
	}
}
