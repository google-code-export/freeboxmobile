package org.madprod.freeboxmobile.mvv;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.madprod.freeboxmobile.FBMHttpConnection;

import static org.madprod.freeboxmobile.StaticConstants.TAG;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

/**
 *
 * @author Clément Beslon
 * $Id: NewsSync.java 71 2010-12-07 08:41:46Z clement $
 * 
 */

public class MevoSync extends IntentService implements MevoConstants
{


	private static MevoDbAdapter mDbHelper;
	private static final String mevoUrl = "https://adsls.free.fr/admin/tel/";
	private static final String mevoListPage = "notification_tel.pl";

	public MevoSync() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}


	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");
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

		Intent i = new Intent();
		i.setAction("org.madprod.freeboxmobile.action.MEVO_SERVICE_COMPLETED");
		i.putExtra("NEW", newmsg);
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

	public static void resetActivity(){
		
	}
	public static void setActivity(Activity a){
		
	}
	public static void setUpdateListener(Object o){
		
	}
}
