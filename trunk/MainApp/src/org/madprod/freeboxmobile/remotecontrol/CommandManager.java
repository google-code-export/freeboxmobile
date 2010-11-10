package org.madprod.freeboxmobile.remotecontrol;

import java.io.IOException;   
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.madprod.freeboxmobile.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

@SuppressWarnings("serial")
public class CommandManager extends Activity implements Constants{
	private static CommandManager cm = new CommandManager();
	static final String TAG	= "FBM";
	private static HashMap<String, Boolean> adresses = new HashMap<String, Boolean>(){
		{
			put("hd1.freebox.fr", true);
			put("hd2.freebox.fr", true);			
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

//	public int refreshAdresses(){
//		int count = 0;
//		Log.d(TAG, "Rafraichissement des adresses");
//		for (String adresse : adresses.keySet()){
//			Log.d(TAG, "Adresse "+adresse);
//			Socket s = new Socket();
//			try {
//				s.setSoTimeout(2000);
//				s.connect(new InetSocketAddress(adresse, 80), 2000);
//				adresses.put(adresse, true);
//				count++;
//				s.close();
//			} catch (IOException e) {
//				adresses.put(adresse, false);					
//			}
//			Log.d(TAG, "Result : "+adresses.get(adresse));
//
//		}
//		Log.d(TAG, "Nb Boitiers OK : "+count);
//		return count;
//	}

	public void sendCommand(String cmd, boolean longClick, int repeat){
		sendCommand(cmd.trim()+"&long="+longClick+"&repeat="+repeat);
	}

	public void sendCommand(String cmd, boolean longClick){
		sendCommand(cmd.trim()+"&long="+longClick);		
	}

	public void sendCommand(String cmd, int repeat){
		sendCommand(cmd.trim()+"&repeat="+repeat);		
	}

	public void sendCommand(String cmd){
		Log.d(TAG, "Envoi de la commande : "+cmd);
//		refreshAdresses();
		for (String adresse : adresses.keySet()){
			Log.d(TAG, "adresse : "+adresse);
			Log.d(TAG, "etat : "+adresses.get(adresse));
			Log.d(TAG, "code : "+codes.get(adresse));
			if (adresses.get(adresse) && codes.get(adresse) != null){
				Log.d(TAG, "Envoi a l adresse : "+adresse +" la commande : http://"+adresse+"/pub/remote_control?code="+codes.get(adresse)+"&key="+cmd);
				try {
//					URI uri = new URI("http://"+adresse+"/pub/remote_control?code="+codes.get(adresse)+"&key="+cmd);
//					HttpGet methodGet = new HttpGet(uri);
					URL url = new URL("http://"+adresse+"/pub/remote_control?code="+codes.get(adresse)+"&key="+cmd);
					URLConnection urlConn = url.openConnection(); 
					urlConn.setDoInput(true); 
					urlConn.setUseCaches(false);
					urlConn.getContentLength();

					
//					httpClient.execute(methodGet);																											
//				} catch (URISyntaxException e) {
//					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		}
	}

	public void refreshCodes(Context c){
		Log.d(TAG, "Rafraichissement des codes");
		
		preferences = c.getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
//		.getDefaultSharedPreferences(c);

		
		
		if (preferences.getBoolean("boitier1_state", false)){
			codes.put("hd1.freebox.fr", preferences.getString("boitier1_code", null));
		}else{
			codes.put("hd1.freebox.fr", null);				
		}
		Log.d(TAG, "Result : "+codes.get("hd1.freebox.fr"));

		if (preferences.getBoolean("boitier2_state", false)){
			codes.put("hd2.freebox.fr", preferences.getString("boitier2_code", null));
		}else{
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
