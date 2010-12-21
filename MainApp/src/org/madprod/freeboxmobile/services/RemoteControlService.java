package org.madprod.freeboxmobile.services;

import java.io.IOException; 

import org.madprod.freeboxmobile.remotecontrol.CommandManager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class RemoteControlService extends Service{

	final RemoteCallbackList<IRemoteControlServiceCallback> callbacks = new RemoteCallbackList<IRemoteControlServiceCallback>(); 
	final static Object lock = new Object();
	public RemoteCallbackList<IRemoteControlServiceCallback> getCallbacks() { 
		return callbacks; 
	}

	static final CommandManager cm = CommandManager.getCommandManager();

	@Override
	public IBinder onBind(Intent intent) {
		return mRemoteControlBinder;
	}

	private final IRemoteControl.Stub mRemoteControlBinder = new IRemoteControl.Stub(){

		@Override
		public void sendCommand(String cmd, boolean longClick, int repeat, int box)
		throws RemoteException {


			cm.refreshCodes(getApplicationContext());
			try {
				if (cmd != null){
					if (repeat > 0){
						cm.sendCommand(cmd, longClick, repeat, box);
					}else{
						cm.sendCommand(cmd, longClick, box);			
					}
				}
				sendAnswer(0, "command "+cmd+" OK");
			} catch (IOException e) {
				e.printStackTrace();
				sendAnswer(1, "command "+cmd+" PAS OK");
			}

		}

		@Override
		public void registerCallback(IRemoteControlServiceCallback cb)
		throws RemoteException {
			if(cb != null){ 
				getCallbacks().register(cb); 
			} 
		}

		@Override
		public void unregisterCallback(IRemoteControlServiceCallback cb)
		throws RemoteException {
			if(cb != null){ 
				getCallbacks().unregister(cb); 
			} 
		}

		@Override
		public void sendChannel(int channel, int box) throws RemoteException {
			cm.refreshCodes(getApplicationContext());
			if (channel>0){
				String channelS = ""+channel;
				try{
					for (int i=0; i< channelS.length(); i++){
						if (i == channelS.length()-1)
							cm.sendCommand(""+channelS.charAt(i), box);
						else cm.sendCommand(""+channelS.charAt(i), true,box);
					}

					sendAnswer(0, "channel "+channel+" OK");
				} catch (IOException e) {
					e.printStackTrace();
					sendAnswer(1, "channel "+channel+" PAS OK");
				}

			}
		}

		@Override
		public void sendParentalCode(int code, int box) throws RemoteException {
			if (code>0){
				try{
					String codeS = ""+code;
					for (int i=0; i< codeS.length(); i++){
						cm.sendCommand(""+codeS.charAt(i), box);
					}
					sendAnswer(1, "parental code "+code+" OK");
				} catch (IOException e) {
					e.printStackTrace();
					sendAnswer(1, "parental code "+code+" PAS OK");
				}

			}

		}

		@Override
		public boolean isBoxActivated(int box) throws RemoteException {
			cm.refreshCodes(getApplicationContext());
			return cm.isBoxActive(box);
		}
	};

	private void sendAnswer(int status, String message){
		synchronized(lock){
			final int N = callbacks.beginBroadcast(); 
			for (int i = 0; i < N; i++) { 
				try { 
					callbacks.getBroadcastItem(i).dataChanged(status, message); 
				}  
				catch (RemoteException e) {} 
			} 
			callbacks.finishBroadcast();
		}
	}




	@Override
	public void onDestroy() {
		this.callbacks.kill();
		super.onDestroy();
	}

}
