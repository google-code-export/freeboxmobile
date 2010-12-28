package org.madprod.freeboxmobile.services;

import java.io.BufferedReader; 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.guide.GuideChoixChainesActivity;
import org.madprod.freeboxmobile.home.HomeListActivity;
import org.madprod.freeboxmobile.mvv.AudioConverter;
import org.madprod.freeboxmobile.mvv.MevoConstants;
import org.madprod.freeboxmobile.mvv.MevoDbAdapter;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

public class MevoService extends Service implements MevoConstants{

	
	private static final String mevoUrl = "https://adsls.free.fr/admin/tel/";
	private static final String mevoDelPage = "efface_message.pl";
	private static final String mevoListPage = "notification_tel.pl";
    private static MevoDbAdapter mDbHelper;

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
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					new SearchMessages().execute();
				}
			});
			t.run();
		}

		
		@Override
		public void deleteMessage(long id) throws RemoteException{
			MevoDbAdapter mda = new MevoDbAdapter(getApplicationContext());
			Log.e(TAG, "message "+id);
			boolean deleted = false;
			
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
				String path = Environment.getExternalStorageDirectory().toString()+DIR_FBM+FBMHttpConnection.getIdentifiant()+DIR_MEVO+c.getString(c.getColumnIndex(KEY_NAME));
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
	
    private class SearchMessages extends AsyncTask<Void, Void, Integer>
    {

		@Override
		protected Integer doInBackground(Void... params) {
			int newmsg = 0;
			
						Log.i(TAG,"MevoSync onHandleIntent ");
			
						File log = new File(Environment.getExternalStorageDirectory()+DIR_FBM, file_log);
						Log.d(TAG,"File log created ");
						try
						{
							BufferedWriter out = new BufferedWriter (new FileWriter(log.getAbsolutePath(), true));
							out.write("mevosync_start: ");
							out.write(new Date().toString());
							out.write("\n");
							out.close();
						}
						catch (IOException e)
						{
							Log.e(TAG,"Exception appending to log file "+e.getMessage());
							e.printStackTrace();
						}
						Log.d(TAG,"Buffered ok ");
			
				        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == true)
				        {
							mDbHelper = new MevoDbAdapter(getApplicationContext());
							Log.d(TAG,"mDbHelper ok ");
					
							FBMHttpConnection.initVars(null, getBaseContext());
							Log.d(TAG,"start to get message list");
							newmsg = getMessageList();
							Log.d(TAG,"end to get message list");
				        }
				        else
				        {
							Log.e(TAG,"SD Card not mounted");        	
				        }
				        
						try
						{
							BufferedWriter out = new BufferedWriter (new FileWriter(log.getAbsolutePath(), true));
							out.write("mevosync_end: ");
							out.write(new Date().toString());
							out.write("\n");
							out.close();
						}
						catch (IOException e)
						{
							Log.e(TAG,"Exception appending to log file "+e.getMessage());
							e.printStackTrace();
						}
			
						Log.d(TAG,"newmsg = "+newmsg);
						
						return newmsg;
		}
    	
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			Intent i = new Intent();
			i.setAction("org.madprod.freeboxmobile.action.MEVO_SERVICE_COMPLETED");
			i.putExtra("NEW", result);
			sendBroadcast(i);
		}
		
		public int getMessageList()
		{
			int newmsg = -1;
			try
			{
		    	BufferedReader br = new BufferedReader(FBMHttpConnection.getAuthRequest(mevoUrl+mevoListPage, null, true, true, "ISO8859_1"));
				String s = " ";
				String status = null;
				String from = null;
				String when = null;
				String length = null;
				String priv = null;
				String link = null;
				String del = null;
				String name = null;
				int intstatus = -1;
				int presence = 0;
				Cursor curs;
				File file, filet;

		        newmsg = 0;
		        file = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+FBMHttpConnection.getIdentifiant()+DIR_MEVO);
		        file.mkdirs();

				mDbHelper.initTempValues();
				Log.d(TAG,"mDbHelper initTempValues OK");
		
				while ( (s=br.readLine())!= null && s.indexOf("Provenance") == -1)
				{
				}
				if ((s != null) && (s.indexOf("Provenance")>-1))
				{
					while ((s=br.readLine())!= null && s.indexOf("</tbody>") == -1)
					{
						if (s.indexOf("<td") != -1)
						{
							if (s.indexOf("Pas de nouveau message") != -1)
								Log.d(TAG,"Pas de nouveau message !");
							else
							{
								Log.d(TAG,"MESSAGE");
								priv = s.substring(s.indexOf("<td"));
								priv = priv.substring(priv.indexOf(">")+1);
								status = priv.substring(0,priv.indexOf("<"));
								Log.d(TAG,"->STATUS:"+status);
								priv = priv.substring(priv.indexOf("<td"));
								priv = priv.substring(priv.indexOf(">")+1);
								from = priv.substring(0,priv.indexOf("<"));
								Log.d(TAG,"->FROM:"+from);
								priv = priv.substring(priv.indexOf("<td"));
								priv = priv.substring(priv.indexOf(">")+1);
								when = priv.substring(0,priv.indexOf("<"));
								Log.d(TAG,"->WHEN:"+when);
								priv = priv.substring(priv.indexOf("<td"));
								priv = priv.substring(priv.indexOf(">")+1);
								length = priv.substring(0,priv.indexOf(" "));
								Log.d(TAG,"->LENGTH:"+length);
								s = br.readLine();
								priv = s.substring(s.indexOf("href=")+6);
								link = priv.substring(0,priv.indexOf("'"));
								Log.d(TAG,"->LINK:"+link);
								priv = priv.substring(priv.indexOf("href=")+6);
								del = priv.substring(0,priv.indexOf("'"));
								Log.d(TAG,"->DEL:"+del);
								name = link.substring(link.indexOf("fichier=")+8);
								Log.d(TAG,"->NAME:"+name);
								if (status.compareTo(STR_NEWMESSAGE) == 0)
								{
									intstatus = 0;
								}
								else
								{
									intstatus = 1;
								}
		
					    	    // Get the mevo file and store it on sdcard
						        file = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+FBMHttpConnection.getIdentifiant()+DIR_MEVO,name+".wav");
						        if (file.exists() == false)
						        {
							        filet = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+FBMHttpConnection.getIdentifiant()+DIR_MEVO,name+"_temp");
									FBMHttpConnection.getFile(filet, mevoUrl+link, null, false);
									FileInputStream is = new FileInputStream(filet);
									try
								    {
										byte[] byteData = null;
										byte[] byteDataPCM = null;
										int iSize = -1;
										long wavSize;
										byteData = new byte[is.available()];
										iSize = is.read(byteData);
										is.close();
										filet.delete();

										byteDataPCM = new byte[iSize * 2]; // Pour éviter de pourrir encore plus le son, je convertis du 8kHz en 16kHz... d'où le x2

										AudioConverter.free2pcm(byteData, 0, byteDataPCM, 0, iSize, false, 1);

										FileOutputStream fstream = new FileOutputStream(file);
										
										// WAV writer
										fstream.write("RIFF".getBytes());
										wavSize = iSize*2 + 36;
										fstream.write(AudioConverter.longToBytes32(wavSize));
										fstream.write("WAVE".getBytes()); //WAVE
										
										fstream.write("fmt ".getBytes()); // fmt
										
										fstream.write(0x10); // 16 for PCM
										fstream.write(0x00);
										fstream.write(0x00);
										fstream.write(0x00);
										
										fstream.write(0x01);		// PCM
										fstream.write(0x00);

										fstream.write(0x01);		// 1 channel (mono)
										fstream.write(0x00);

										fstream.write(0x40);	// sample rate : 8000 Hz
										fstream.write(0x1f);
										fstream.write(0x00);
										fstream.write(0x00);
										
										fstream.write(0x80);	// byte rate : 16000
										fstream.write(0x3E);
										fstream.write(0x00);
										fstream.write(0x00);
										
										fstream.write(0x02);		// Block align = numchannels * bits per sample / 8
										fstream.write(0x00);
										
										fstream.write(0x10);		// bits per sample (16)
										fstream.write(0x00);
										
										fstream.write("data".getBytes());	// data
										
										wavSize = iSize*2;
										fstream.write(AudioConverter.longToBytes32(wavSize));
										
										fstream.write(byteDataPCM);
										fstream.close();
										Log.i(TAG,"File converted ! "+name+".wav");
								    }
									catch (FileNotFoundException e)
									{
										Log.e(TAG,"Error while converting data "+e.getMessage());
								    }
						        }
						        presence = 4;
						        curs = mDbHelper.fetchMessage(name+".wav");
								if (curs.moveToFirst() == false)
					        	{
									// Store data in db if the message is not present in the db
									Log.i(TAG,"STORING IN DB");
						        	mDbHelper.createMessage(intstatus, presence, from, when, link, del, Integer.parseInt(length), name+".wav");
						        	newmsg++;
					        	}
					        	else
					        	{
									// Update data in db if the message is already present in the db
					        		Log.i(TAG,"UPDATING DB");
					        		mDbHelper.updateMessage(presence, link, del, name+".wav");
					        	}
					        	curs.close();
							}
						}
					}
				}
				else
				{
					Log.d(TAG,"pb extract");
				}
			}

			catch (Exception e)
			{
				Log.e(TAG,"getMessageList : " + e.getMessage());
				e.printStackTrace();
			}
			Log.i(TAG,"getmessage end "+newmsg);
			return newmsg;
	 	}
    }
}
