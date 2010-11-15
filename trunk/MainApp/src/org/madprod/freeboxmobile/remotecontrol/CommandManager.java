package org.madprod.freeboxmobile.remotecontrol;

import java.io.IOException;   
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.madprod.freeboxmobile.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

@SuppressWarnings("serial")
public class CommandManager extends Activity implements Constants{
	private static CommandManager cm = new CommandManager();
	static final String TAG	= "FBM";
	private static HashMap<String, Boolean> adresses = new HashMap<String, Boolean>(){
		{
			put("hd1.freebox.fr", false);
			put("hd2.freebox.fr", false);			
		}
	};

	private SharedPreferences preferences;



	private static HashMap<String, String> codes = new HashMap<String, String>(){
		{
			put("hd1.freebox.fr", null);
			put("hd2.freebox.fr", null);			
		}
	};


	public static CommandManager getCommandManager() {

		return cm;
	}

	public void sendCommand(String cmd, boolean longClick, int repeat) throws IOException{
		sendCommand(cmd.trim()+"&long="+longClick+"&repeat="+repeat);
	}

	public void sendCommand(String cmd, boolean longClick) throws IOException{
		sendCommand(cmd.trim()+"&long="+longClick);		
	}

	public void sendCommand(String cmd, int repeat) throws IOException{
		sendCommand(cmd.trim()+"&repeat="+repeat);		
	}

	public void sendCommand(final String cmd) throws IOException{
		Log.d(TAG, "Envoi de la commande : "+cmd);
		for (String adresse : adresses.keySet()){
			final String tmp = adresse;
			Log.d(TAG, "adresse : "+tmp);
			Log.d(TAG, "etat : "+adresses.get(tmp));
			Log.d(TAG, "code : "+codes.get(tmp));
			if (adresses.get(tmp) && codes.get(tmp) != null){
				Log.d(TAG, "Envoi a l adresse : "+tmp +" la commande : http://"+tmp+"/pub/remote_control?code="+codes.get(tmp)+"&key="+cmd);
					URL url = new URL("http://"+tmp+"/pub/remote_control?code="+codes.get(tmp)+"&key="+cmd);
					URLConnection urlConn = url.openConnection(); 
					urlConn.setReadTimeout(500);
					urlConn.setConnectTimeout(500);
					urlConn.getContentLength();

							
						
						
			}
		}
	}

	public void refreshCodes(Context c){
		Log.d(TAG, "Rafraichissement des codes");
		
		preferences = c.getSharedPreferences(KEY_PREFS, MODE_PRIVATE);

		Log.d(TAG, "Preference : "+preferences);
		
		
		if (preferences.getBoolean("boitier1_state", false)){
			codes.put("hd1.freebox.fr", preferences.getString("boitier1_code", null));
			adresses.put("hd1.freebox.fr", true);
		}else{
			codes.put("hd1.freebox.fr", null);				
			adresses.put("hd1.freebox.fr", false);
		}
		Log.d(TAG, "Result : "+codes.get("hd1.freebox.fr"));

		if (preferences.getBoolean("boitier2_state", false)){
			adresses.put("hd2.freebox.fr", true);
			codes.put("hd2.freebox.fr", preferences.getString("boitier2_code", null));
		}else{
			adresses.put("hd2.freebox.fr", false);
			codes.put("hd2.freebox.fr", null);				
		}
		Log.d(TAG, "Result : "+codes.get("hd2.freebox.fr"));
		
		
		
	}

	public static HashMap<String, Boolean> getAdresses(){
		return new HashMap<String, Boolean>(adresses);
	}

	public boolean isAdresseActive() {
		return adresses.values().contains(true);
	}	
	
	
}
