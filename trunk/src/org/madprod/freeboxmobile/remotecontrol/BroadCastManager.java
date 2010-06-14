package org.madprod.freeboxmobile.remotecontrol;

import org.madprod.freeboxmobile.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BroadCastManager implements Constants{
	static final CommandManager cm = CommandManager.getCommandManager();
	private final Context context;
	private BroadcastReceiver remoteCommandRec ;
	private BroadcastReceiver remoteChannelRec ;
	private BroadcastReceiver remoteCodeRec ;

	
	public BroadCastManager(Context c) {
		context = c;
		initReceivers();
	}
	
	private void initReceivers(){

		
		remoteCommandRec = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				cm.refreshCodes(context);
				Log.d(TAG, "Receiver REMOTECOMMAND");
				if (intent.getExtras() != null){
					int repeat = intent.getIntExtra("repeat", -1);
					boolean longClick = intent.getBooleanExtra("long", false);
					String cmd = intent.getStringExtra("cmd");
					Log.d(TAG, "Cmd = "+cmd);
					if (cmd != null){
						if (repeat != -1 && longClick){
							cm.sendCommand(cmd, longClick, repeat);
						}else if (!longClick&&repeat !=-1) cm.sendCommand(cmd, repeat);
						else if (repeat<0&&longClick) cm.sendCommand(cmd, longClick);
						else cm.sendCommand(cmd);
					}
				
				
				}else{
					Log.d(TAG, "Pas de parametre passes a l intent");
				}
			}
		};

		context.registerReceiver(remoteCommandRec, new IntentFilter("REMOTECOMMAND"));		
		

		remoteChannelRec = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				cm.refreshCodes(context);
				Log.d(TAG, "Receiver REMOTECHANNEL");
				if (intent.getExtras() != null){
					int channel = intent.getIntExtra("channel", -1);
					Log.d(TAG, "Receiver - channel ="+channel);
					if (channel>0){
						String channelS = ""+channel;
						for (int i=0; i< channelS.length(); i++){
							if (i == channelS.length()-1)
								cm.sendCommand(""+channelS.charAt(i));
							else cm.sendCommand(""+channelS.charAt(i), true);
						}
					}
				}else{
					Log.d(TAG, "Pas de parametre passes a l intent");
				}
			}
		};
		
		
		context.registerReceiver(remoteChannelRec, new IntentFilter("REMOTECHANNEL"));		

		remoteCodeRec = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				cm.refreshCodes(context);
				Log.d(TAG, "Receiver REMOTECODE");
				if (intent.getExtras() != null){
					int code = intent.getIntExtra("code", -1);
					Log.d(TAG, "Receiver - code ="+code);
					if (code>0){
						String codeS = ""+code;
						for (int i=0; i< codeS.length(); i++){
							cm.sendCommand(""+codeS.charAt(i));
						}
					}
				}else{
					Log.d(TAG, "Pas de parametre passes a l intent");
				}
			}
		};
		context.registerReceiver(remoteCodeRec, new IntentFilter("REMOTECODE"));		

	
	
	
	}
	
	
	public void destroy(){
		context.unregisterReceiver(remoteChannelRec);
		context.unregisterReceiver(remoteCodeRec);
		context.unregisterReceiver(remoteCommandRec);
	}
}
