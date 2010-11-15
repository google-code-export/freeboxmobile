package org.madprod.freeboxmobile.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.madprod.freeboxmobile.home.HomeListActivity;
import org.madprod.freeboxmobile.remotecontrol.CommandManager;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.widget.Toast;

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
		public void sendCommand(String cmd, boolean longClick, int repeat)
		throws RemoteException {


			cm.refreshCodes(getApplicationContext());
			try {
				if (cmd != null){
					if (repeat > 0){
						cm.sendCommand(cmd, longClick, repeat);
					}else{
						cm.sendCommand(cmd, longClick);			
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
