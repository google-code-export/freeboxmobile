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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlrpc.android.XMLRPCClient;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.MimeTypeMap;
 
/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class FBMHttpConnection implements Constants
{
	public static String USER_AGENT = "FreeboxMobile (Linux; U; Android; fr-fr;)";

	private static String title = null;
	private static String login = null;
	private static String password = null;
	private static String fbmversion = "";
	// Variables id et idt d'accès à MonCompteFree
	private static String id = null;
	private static String idt = null;

	private static int connectionStatus = CONNECT_NOT_CONNECTED;
	
	private static final String serverUrl = "https://subscribes.free.fr/login/login.pl";
	private static final String suiviTechUrl = "https://adsls.free.fr/suivi/suivi_techgrrr.pl";
	public static final String frimousseUrl = "http://www.frimousse.org/outils/xmlrpc";

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
		USER_AGENT = c.getString(R.string.app_name)+"/"+c.getString(R.string.app_version)+" (Linux; U; Android "+Build.VERSION.RELEASE+"; fr-fr;)";
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
	
	public static String getTitle()
	{
		return (title);
	}

	public static String getIdentifiant()
	{
		return (login);
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
			Log.d(TAG, "UPDATE: " + login + " / " + l);
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
	
	public static int connect()
	{
		return (connectionFree(login, password));
	}

	public static int checkConnected(int defValue)
	{
		if ((id == null) || (idt == null))
		{
			Log.d(TAG,"checkConnected : ON A JAMAIS ETE AUTHENTIFIE - ON S'AUTHENTIFIE");
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
			Log.d(TAG,"connectFreeCheck : "+v);

			// restauration des données présentes avant
			login = mLogin;
			password = mPassword;
			id = mId;
			idt = mIdt;
		}
		else
		{
			Log.d(TAG, "connectFreeCheck failed");
		}
		return (v);
	}

	private static String parsePage(String s, String tag, String first, String last)
	{
		String r = "";
		int start;
		int end;
		int itag;

		if (s != null)
		{
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
						Log.d(TAG,"parsePage end pb : "+tag);
				}
				else
					Log.d(TAG,"parsePage start pb : "+tag);
			}
			else
				Log.d(TAG,"parsePage itag pb : "+tag);
		}
		else
		{
			Log.d(TAG,"parsePage : page null !");
		}
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
		Log.d(TAG,"["+tag+"] "+dest);
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
			String br = getPage(getAuthRequest(suiviTechUrl, null, true, true, "ISO8859_1"));
			String offre = parsePage(br, "Raccordée actuellement en offre", "<font", "</font>");
			if (offre.contains("Freebox dégroupé"))
		    	consoleValues.put(KEY_LINETYPE, "1");
			else if (offre.contains("Fibre Optique"))
				consoleValues.put(KEY_LINETYPE, "2");
			else
				consoleValues.put(KEY_LINETYPE, "0");
	    	// TODO : enlever la ligne suivante après debug
	    	if (consoleValues.get(KEY_LINETYPE).equals("0"))
				Log.d(TAG,"DEBUG INFO TECHNIQUES : "+br);		    		
	    	Log.d(TAG,"type:"+consoleValues.get(KEY_LINETYPE));
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
					Log.d(TAG,"XMLRPC : "+response[0]);
					consoleValues.put(KEY_DSLAM,(String) response[0]);
				}
				else
				{
					consoleValues.put(KEY_DSLAM,"");
					Log.d(TAG,"DSLAM pas trouvé");
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
			Log.e(TAG, "parseConsole : " + e.getMessage());
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
		
		Log.d(TAG,"Connect Free start ");
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
				Log.d(TAG,"connectFree : "+s);
	    		String priv;
				if (s.indexOf("idt=")>-1)
				{
					priv = s.substring(s.indexOf("idt=")+4);
					if (priv.indexOf("]")>-1)
					{
						priv = priv.substring(0, priv.indexOf(']'));
					}
					m_idt = priv;
					Log.d(TAG,"idt :"+priv);
					priv = s.substring(s.indexOf("?id=")+4);
					priv = priv.substring(0, priv.indexOf('&'));
					m_id = priv;
					Log.d(TAG,"id :"+priv);
				}
			}
        }
		catch (Exception e)
		{
			Log.e(TAG,"connectFree : "+e.getMessage());
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
       		Log.d(TAG,"MonCompeFree : AUTHENTIFICATION FAILED !");
       		connectionStatus = CONNECT_LOGIN_FAILED;
       		id = null;
       		idt = null;
       	}
       	return connectionStatus;
    }

	private static HttpURLConnection prepareConnection(String url, String method) throws IOException
	{
		URL u = new URL(url);
		Log.d(TAG,"PREPARECONNECTION : URL["+ u +"]METHOD ["+method+"]");
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.setRequestMethod(method);
		c.setAllowUserInteraction(false);
		c.setUseCaches(false);
		c.setInstanceFollowRedirects(false);
		c.setRequestProperty("User-Agent", USER_AGENT);
		// TODO : Check this !
		c.setConnectTimeout(30000);
		c.setReadTimeout(30000);
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
//		Log.d(TAG,"->DOWNLOADING FILE : "+url);
        try
        {
			if (connected == CONNECT_CONNECTED)
			{
//				Log.d(TAG,"GETFILE : VERIF SI ON EST AUTHENTIFIE");
				c = prepareConnection(url+"?"+makeStringForPost(p, auth, null), "GET");
				c.setDoInput(true);
				Log.d(TAG,"HEADERS : "+c.getHeaderFields());
				Log.d(TAG,"RESPONSE : "+c.getResponseCode()+" "+c.getResponseMessage());
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
				Log.d(TAG,"GETFILE : PAS AUTHENTIFIE SUR LA CONSOLE - SESSION EXPIREE");
				connected = connectionFree(login, password);
				if (connected == CONNECT_CONNECTED)
				{
					Log.d(TAG,"GETFILE : REAUTHENTIFICATION OK");
					c = prepareConnection(url+"?"+makeStringForPost(p, auth, null), "GET");
					c.setDoInput(true);
					Log.d(TAG,"HEADERS : "+c.getHeaderFields());
					Log.d(TAG,"RESPONSE : "+c.getResponseCode()+" "+c.getResponseMessage());
				}
			}
			else
			{
//				Log.d(TAG,"GETFILE : AUTHENTIFICATION OK");
				connected = CONNECT_CONNECTED;
			}
			if (connected == CONNECT_CONNECTED)
			{
//				Log.d(TAG,"GETFILE : LECTURE FICHIER");
		    	c.connect();
		        f = new FileOutputStream(file);
		        InputStream in = c.getInputStream();

				while ( (len = in.read(buffer)) > 0 )
		        {
		            f.write(buffer, 0, len);
		        }
		    	f.close();
		    	in.close();
//		    	Log.d(TAG,"->FILE DOWNLOADED");
		    	return true;
			}
        }
        catch (Exception e)
        {
        	Log.e(TAG,"getFile : "+e.getMessage());
        	e.printStackTrace();
		}
        return false;
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
				Log.d(TAG,"CHARSET : "+charset);
			}
			else
				Log.d(TAG,"CHARSET : not found - keep default "+charset);
		}
		else
			Log.d(TAG,"CHARSET default : "+charset);
		return charset;
	}

	/**
	 * getAuthRequest : perform a GET on an URL with p parameters
	 * do not provide id or idt in URL
	 * @param url : url to get
	 * @param p : parameters for the GET request (null if none) (url must not have parameters in not null)
	 * @param auth : true if you need id & idt added automatically for authentification on Free console (url must not have parameters in this case)
	 * @param retour : set it to true of you want an InputStream with the page in return
	 * @param charset : default charset
	 * @return InputStreamReader HTML Page or null
	 */
	public static InputStreamReader getAuthRequest(String url, List<NameValuePair> p, boolean auth, boolean retour, String charset)
	{
		int c;

		HttpURLConnection h = null;

		c = checkConnected(CONNECT_CONNECTED);
		try
		{
			Log.d(TAG,"-- GET ISR : "+url);
			if (c == CONNECT_CONNECTED)
			{
				Log.d(TAG,"GETISR : VERIF SI ON EST AUTHENTIFIE");
				if (retour)
					h = prepareConnection(url+"?"+makeStringForPost(p, auth, charset), "GET");
				else
					h = prepareConnection(url+"?"+makeStringForPost(p, auth, charset), "HEAD");
				h.setDoInput(true);
				charset = getCharset(h.getContentType(), charset);

				Log.d(TAG,"HEADERS : "+h.getHeaderFields());
				// TODO : Tenir compte du getResponseCode()
				Log.d(TAG,"RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
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
				Log.d(TAG,"GETISR : PAS AUTHENTIFIE SUR LA CONSOLE - SESSION EXPIREE");
				c = connectionFree(login, password);
				if (c == CONNECT_CONNECTED)
				{
					Log.d(TAG,"GETISR :  REAUTHENTIFICATION OK");
					h = prepareConnection(url+"?"+makeStringForPost(p, auth, charset), retour ? "GET": "HEAD");
					h.setDoInput(true);
					Log.d(TAG,"HEADERS : "+h.getHeaderFields());
					// TODO : Tenir compte du getResponseCode()
					Log.d(TAG,"RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
				}
			}
			else
			{
				Log.d(TAG,"GETISR : AUTHENTIFICATION OK");
				c = CONNECT_CONNECTED;
			}
			if ((c == CONNECT_CONNECTED) && (retour == true))
			{
				Log.d(TAG,"GETISR : LECTURE DONNEES - TYPE : "+h.getContentType());
				charset = getCharset(h.getContentType(), charset);

				return (new InputStreamReader(h.getInputStream(), charset));
			}
		}
		catch (Exception e)
		{
			Log.e(TAG,"getAuthRequestISR "+e.getMessage());
			e.printStackTrace();
		}
		return (null);
	}

	public static void makePost(HttpURLConnection h, boolean retour, String params) throws IOException
	{
		h.setDoOutput(true);
		if (retour)
			h.setDoInput(true);
		OutputStreamWriter o = new OutputStreamWriter(h.getOutputStream());
		o.write(params);
		o.flush();
		o.close();		
	}
	
	public static InputStreamReader postAuthRequest(String url, List<NameValuePair> p, boolean auth, boolean retour)
	{
		HttpURLConnection h = null;
		int c;
		String pagesCharset = "ISO8859_1";

		Log.d(TAG,"POST: " + url);
		try
		{
			c = checkConnected(CONNECT_CONNECTED);
			if (c == CONNECT_CONNECTED)
			{
				Log.d(TAG,"POST : VERIFICATION DE SESSION");
				h = prepareConnection(url+(auth ? "?"+makeStringForPost(null, auth, null) : ""), "POST");
//				makePost(h, retour, makeStringForPost(p, false, null));
				h.setDoOutput(true);
				if (retour)
					h.setDoInput(true);
				OutputStreamWriter o = new OutputStreamWriter(h.getOutputStream());
				o.write(makeStringForPost(p, false, null));
				o.flush();
				o.close();
				Log.d(TAG,"RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
/*				if (h.getResponseCode() == -1)
				{
					h = prepareConnection(url+(auth ? "?"+makeStringForPost(null, auth, null) : ""), "POST");
					makePost(h, retour, makeStringForPost(p, false, null));
					Log.d(TAG,"RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
				}
*/
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
				Log.d(TAG,"POST : PAS AUTHENTIFIE SUR LA CONSOLE - SESSION EXPIREE");
				c = connectionFree(login, password);
				if (c == CONNECT_CONNECTED)
				{
					Log.d(TAG,"POST :  REAUTHENTIFICATION OK");
					h = prepareConnection(url+(auth ? "?"+makeStringForPost(null, auth, null) : ""), "POST");
					h.setDoOutput(true);
					if (retour)
						h.setDoInput(true);
					OutputStreamWriter o = new OutputStreamWriter(h.getOutputStream());
					o.write(makeStringForPost(p, false, null));
					o.flush();
					o.close();
					Log.d(TAG,"RESPONSE : "+h.getResponseCode()+" "+h.getResponseMessage());
				}
			}
			else
			{
				Log.d(TAG,"POST : AUTHENTIFICATION OK");
				c = CONNECT_CONNECTED;
			}
			if ((c == CONNECT_CONNECTED) && (retour))
			{
				Log.d(TAG,"POST : LECTURE DONNEES");
				pagesCharset = getCharset(h.getContentType(), pagesCharset);
				return (new InputStreamReader(h.getInputStream(), pagesCharset));
			}
		}
		catch (Exception e)
		{
			Log.e(TAG,"EXCEPTION PostAuthRequest : "+e.getMessage());
			e.printStackTrace();
		}
		return (null);
	}

	private static String makeStringForPost(List<NameValuePair> p, boolean auth, String charset)
    {
        String listConcat = "";
        boolean log = true;
  
        if (charset == null)
        {
        	charset = "ISO8859_1";
        }
        if ((p != null) && (p.size() > 0))
        {
            for(int i = 0 ; i < p.size() ; i++)
            {
            	if (i != 0)
            		listConcat += "&";
                try
                {
            		if (p.get(i).getName().equals("pass"))
            			log = false;
	                listConcat += URLEncoder.encode(p.get(i).getName(), charset);
	                listConcat += '=';
	                listConcat += URLEncoder.encode(p.get(i).getValue(), charset);
                }
                catch (Exception e)
                {
                	Log.e(TAG, "makeStringForPost PB ENCODE : "+e.getMessage());
                	e.printStackTrace();
	                listConcat += URLEncoder.encode(p.get(i).getName());
	                listConcat += '=';
	                listConcat += URLEncoder.encode(p.get(i).getValue());
                }
            }
        }
		if (auth)
		{
			if ((p != null) && (p.size() > 0))
			{
				listConcat += "&";
			}
			try
			{
				listConcat += URLEncoder.encode("id", charset);
				listConcat += '=';
				listConcat += URLEncoder.encode(id, charset);
				listConcat += '&';
				listConcat += URLEncoder.encode("idt", charset);
				listConcat += '=';
				listConcat += URLEncoder.encode(idt, charset);
			} catch (UnsupportedEncodingException e)
			{
            	Log.e(TAG, "makeStringForPost PB ENCODE ID IDT : "+e.getMessage());
            	e.printStackTrace();
			}
		}
        if (log)
        	Log.d(TAG,"makeStringForPost : "+listConcat);
        return (listConcat);
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
		Log.d(TAG,"->POSTING FILE TO "+uploadFaxUrl);
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
			final long fileLength = fileToPost.length();
			final int bufferSize = 1024;
			final byte[] buffer = new byte[bufferSize];
			
			ds.writeBytes(END_CONTENT_DISPOSITION);
			
			for (NameValuePair pair : params) {
	           	ds.writeBytes("Content-Disposition: form-data; name=\""+pair.getName()+"\""+END+END+pair.getValue()+END);
	   			ds.writeBytes(END_CONTENT_DISPOSITION);
			}
	
			ds.writeBytes("Content-Disposition: form-data; name=\"document\"; filename=\""+ fileToPost.getAbsolutePath() + "\"" + END);
			final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileToPost.getName());
			ds.writeBytes("Content-Type : "+mimeType+END+END);
			
			int length = -1;
			float writtenBytes = 0;
			
			//Calcul de la progression
			//int lastSendedValue = 0;
			//int minStep = 5;
			
			while ((length = fStream.read(buffer)) != -1) {
				ds.write(buffer, 0, length);
				writtenBytes +=length;
				
				/* Calcul de la progression
				int progress = (int)(((float)writtenBytes/(float)fileLength)*100);
				if(progress >= lastSendedValue+minStep || progress==100){
					lastSendedValue = progress;
				}*/
			}
			
			ds.writeBytes(END);
			ds.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + END);
			
			/* close streams */
			fStream.close();
			ds.flush();
			ds.close();
	
			//Test du code de retour
			if (conn.getResponseCode() != expectedHttpStatus){
				Log.d(TAG,"Mauvais code Http retourné lors du post multipart : "+conn.getResponseCode()+" au lieu de "+expectedHttpStatus);
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
			final String result = b.toString();
			Log.d(TAG,"Reponse FAX lue : "+result);
			return result;
		}
		Log.d(TAG,"Connexion impossible pour faxer le fichier "+fileToPost.getName());
		return null;
	}
}
