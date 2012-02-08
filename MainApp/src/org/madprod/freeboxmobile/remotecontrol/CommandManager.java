package org.madprod.freeboxmobile.remotecontrol;

import java.io.IOException;   
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.http.client.ClientProtocolException;
import org.madprod.freeboxmobile.Constants;

import static org.madprod.freeboxmobile.StaticConstants.TAG;
import static org.madprod.freeboxmobile.StaticConstants.KEY_PREFS;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

@SuppressWarnings("serial")
public class CommandManager extends Activity {
	private static CommandManager cm = new CommandManager();
	static final String TAG	= "FBM";
	private static String[] adresses = new String[]{"hd1.freebox.fr", "hd2.freebox.fr"};
	private static boolean[] states = new boolean[]{false, false};
	private static String[] codes = new String[2];


	private SharedPreferences preferences;


	public static CommandManager getCommandManager() {

		return cm;
	}

	public void sendCommand(String cmd, boolean longClick, int repeat, int box) throws IOException{
		sendCommand(cmd.trim()+"&long="+longClick+"&repeat="+repeat, box);
	}

	public void sendCommand(String cmd, boolean longClick, int box) throws IOException{
		sendCommand(cmd.trim()+"&long="+longClick, box);		
	}

	public void sendCommand(String cmd, int repeat, int box) throws IOException{
		sendCommand(cmd.trim()+"&repeat="+repeat, box);		
	}

	public void sendCommand(final String cmd, int box) throws IOException{
		Log.d(TAG, "Envoi de la commande : "+cmd);

		for (int i = 0; i<adresses.length; i++){
			if (box > 0 && box<adresses.length){
				if (box-1 == i){
					String tmp = adresses[i];
					Log.d(TAG, "adresse : "+adresses[i]);
					Log.d(TAG, "etat : "+states[i]);
					Log.d(TAG, "code : "+codes[i]);
					if (states[i] && codes[i] != null){
						Log.d(TAG, "Envoi a l adresse : "+tmp +" la commande : http://"+tmp+"/pub/remote_control?code="+codes[i]+"&key="+cmd);
						URL url = new URL("http://"+tmp+"/pub/remote_control?code="+codes[i]+"&key="+cmd);
						URLConnection urlConn = url.openConnection(); 
						urlConn.setReadTimeout(500);
						urlConn.setConnectTimeout(500);
						urlConn.getContentLength();				
					}					
				}
			}else{
				String tmp = adresses[i];
				Log.d(TAG, "adresse : "+adresses[i]);
				Log.d(TAG, "etat : "+states[i]);
				Log.d(TAG, "code : "+codes[i]);
				if (states[i] && codes[i] != null){
					Log.d(TAG, "Envoi a l adresse : "+tmp +" la commande : http://"+tmp+"/pub/remote_control?code="+codes[i]+"&key="+cmd);
					URL url = new URL("http://"+tmp+"/pub/remote_control?code="+codes[i]+"&key="+cmd);
					URLConnection urlConn = url.openConnection(); 
					urlConn.setReadTimeout(500);
					urlConn.setConnectTimeout(500);
					urlConn.getContentLength();				
				}					

			}
		}
	}

	public void refreshCodes(Context c){
		Log.d(TAG, "Rafraichissement des codes");

		preferences = c.getSharedPreferences(KEY_PREFS, MODE_PRIVATE);

		Log.d(TAG, "Preference : "+preferences);


		if (preferences.getBoolean("boitier1_state", false)){
			codes[0] = preferences.getString("boitier1_code", null);
			states[0] = true;
		}else{
			codes[0] = null;
			states[0] = false;
		}
		Log.d(TAG, "Result : "+codes[0]);

		if (preferences.getBoolean("boitier2_state", false)){
			codes[1] = preferences.getString("boitier2_code", null);
			states[1] = true;
		}else{
			codes[1] = null;
			states[1] = false;
		}
		Log.d(TAG, "Result : "+codes[1]);



	}


	public boolean isAtLeastOneAdresseActive() {
		for (int i = 0; i<states.length; i++){
			if (states[i] == true) return true;
		}
		return false;
	}	

	public boolean isBoxActive(int box) {
		if (box > 0 && box <= adresses.length){
			if (states[box-1] && codes[box-1] != null) return true;
		}
		return false;
	}	

}
