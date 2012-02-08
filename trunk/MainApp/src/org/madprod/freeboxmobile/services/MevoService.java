package org.madprod.freeboxmobile.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.mvv.MevoConstants;
import org.madprod.freeboxmobile.mvv.MevoDbAdapter;
import org.madprod.freeboxmobile.mvv.MevoSync;

import static org.madprod.freeboxmobile.StaticConstants.TAG;
import static org.madprod.freeboxmobile.StaticConstants.KEY_PREFS;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class MevoService extends Service implements MevoConstants{


	private static final String mevoUrl = "https://adsls.free.fr/admin/tel/";
	private static final String mevoDelPage = "efface_message.pl";

	final RemoteCallbackList<IRemoteControlServiceCallback> callbacks = new RemoteCallbackList<IRemoteControlServiceCallback>(); 
	final static Object lock = new Object();
	public RemoteCallbackList<IRemoteControlServiceCallback> getCallbacks() { 
		return callbacks; 
	}


	@Override
	public IBinder onBind(Intent intent) {
		return mMevoBinder;
	}

	private final IMevo.Stub mMevoBinder = new IMevo.Stub(){



		@Override
		public void checkMessages() throws RemoteException {	
			final Intent intent = new Intent(Intent.ACTION_SYNC, null, getApplicationContext(), MevoSync.class);
			startService(intent);
		}


		@Override
		public void deleteMessage(long id) throws RemoteException{
			MevoDbAdapter mda = new MevoDbAdapter(getApplicationContext());
			Log.e(TAG, "message "+id);

			Cursor c = mda.fetchMessage(id);
			if (c== null || !c.moveToFirst()){
				return;
			}


			File file = new File(getMessageFile(id));
			if (file.delete())
			{
				Log.d(TAG, "Delete file ok");
			}
			else
			{
				Log.d(TAG, "Delete file not ok");
			}

			String tel = c.getString(c.getColumnIndex(KEY_DEL));
			if (!(tel.equals("")))
			{
				if (tel.indexOf("tel=")>-1)
				{
					tel = tel.substring(tel.indexOf("tel=")+4);
					if (tel.indexOf("&")>-1)
					{
						tel = tel.substring(0, tel.indexOf('&'));
					}
				}

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("tel",tel));
				String msgFile = c.getString(c.getColumnIndex(KEY_NAME));
				if (msgFile.endsWith(".wav"))
				{
					msgFile = msgFile.substring(0, msgFile.length() - 4);
				}
				params.add(new BasicNameValuePair("fichier",msgFile));

				Log.d(TAG, "Deleting on server "+params);
				FBMHttpConnection.getAuthRequest(mevoUrl+mevoDelPage, params, true, false, "ISO8859_1");
			}


			mda.deleteMessage(id);
		}

		@Override
		public void setMessageRead(long id){
			MevoDbAdapter mda = new MevoDbAdapter(getApplicationContext());
			Log.e(TAG, "message "+id);

			mda.setMessageRead(id);

		}

		@Override
		public void setMessageUnRead(long id){
			MevoDbAdapter mda = new MevoDbAdapter(getApplicationContext());
			Log.e(TAG, "message "+id);

			mda.setMessageUnRead(id);
		}


		@Override
		public String getMessageFile(long messageId) throws RemoteException {
			MevoDbAdapter mda = new MevoDbAdapter(getApplicationContext());
			Cursor c = mda.fetchMessage(messageId);
			if (c != null && c.moveToFirst()){
				
				String login = FBMHttpConnection.getIdentifiant();
				if (login == null){
					login = getApplicationContext().getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE).getString(KEY_USER, null);

				}
				
				String path = Environment.getExternalStorageDirectory().toString()+DIR_FBM+login+DIR_MEVO+c.getString(c.getColumnIndex(KEY_NAME));
				Log.e(TAG, "path = "+path);
				return path;
			}
			return "";
		}

	};


	@Override
	public void onDestroy() {
		this.callbacks.kill();
		super.onDestroy();
	}

}
