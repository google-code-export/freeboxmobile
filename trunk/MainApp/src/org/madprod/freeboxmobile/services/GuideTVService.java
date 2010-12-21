package org.madprod.freeboxmobile.services;

import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class GuideTVService extends Service{

	final RemoteCallbackList<IRemoteControlServiceCallback> callbacks = new RemoteCallbackList<IRemoteControlServiceCallback>(); 
	final static Object lock = new Object();
	public RemoteCallbackList<IRemoteControlServiceCallback> getCallbacks() { 
		return callbacks; 
	}


	@Override
	public IBinder onBind(Intent intent) {
		return mGuideTvBinder;
	}

	private final IGuideTV.Stub mGuideTvBinder = new IGuideTV.Stub(){


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
		public Channel[] getListOfChannels(int numberBox) throws RemoteException {
			ChainesDbAdapter cda = new ChainesDbAdapter(getApplicationContext());
			cda.getListeDisques(numberBox);
			return null;
		}
	};


	@Override
	public void onDestroy() {
		this.callbacks.kill();
		super.onDestroy();
	}

}
