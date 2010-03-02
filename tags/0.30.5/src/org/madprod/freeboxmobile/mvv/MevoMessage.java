package org.madprod.freeboxmobile.mvv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.Phones;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class MevoMessage implements MevoConstants
{
	private ContentValues msgValues;
	private Context _context;
    protected MevoDbAdapter mDbHelper;
	private MediaPlayer mp;

	public MevoMessage(Context c, MevoDbAdapter db)
	{
		_context = c;
		mDbHelper = db;
		mp = null;
		ContentValues msgValues;

		msgValues = new ContentValues();
		msgValues.put(KEY_STATUS, 0);
		msgValues.put(KEY_PRESENCE, 0);
		msgValues.put(KEY_SOURCE, "");
		msgValues.put(KEY_QUAND, 0);
		msgValues.put(KEY_QUAND_HR, 0);
		msgValues.put(KEY_LINK, "");
		msgValues.put(KEY_DEL, "");
		msgValues.put(KEY_NAME, "");
		msgValues.put(KEY_LENGTH, 0);
		msgValues.put(KEY_CALLER, "");
		msgValues.put(KEY_PLAY_STATUS, PLAY_STATUS_STOP);
		this.msgValues = msgValues;
	}

	private void getContactFromNumber(String number)
	{
		ContentResolver resolver = _context.getContentResolver();

		// define the columns I want the query to return
		String[] projection = new String[]{
				Contacts.Phones.DISPLAY_NAME,
				Contacts.Phones.TYPE,
				Contacts.Phones.NUMBER,
				Contacts.Phones.LABEL };

		if ((number != null) && (number.length() > 0))
		{
			String n = Uri.encode(number);
			Uri contactUri = Uri.withAppendedPath(Phones.CONTENT_FILTER_URL, n);
//	For 2.0+ :
//			Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_URI, Uri.encode(phoneNumber));
			Cursor c = resolver.query(contactUri, projection, null, null, null);
			// if the query returns 1 or more results
			// return the first result
			if (c.moveToFirst())
			{
				msgValues.put(KEY_CALLER, c.getString(c.getColumnIndex(Contacts.Phones.DISPLAY_NAME)));
				switch (c.getInt(c.getColumnIndex(Contacts.Phones.TYPE)))
				{
					case Contacts.Phones.TYPE_CUSTOM:
						msgValues.put(KEY_NB_TYPE, c.getString(c.getColumnIndex(Contacts.Phones.LABEL)));
					break;
					case Contacts.Phones.TYPE_FAX_HOME:
						msgValues.put(KEY_NB_TYPE, _context.getString(R.string.mevo_msgtype_fax_home));
					break;
					case Contacts.Phones.TYPE_FAX_WORK:
						msgValues.put(KEY_NB_TYPE, _context.getString(R.string.mevo_msgtype_fax_work));
					break;
					case Contacts.Phones.TYPE_HOME:
						msgValues.put(KEY_NB_TYPE, _context.getString(R.string.mevo_msgtype_home));
					break;
					case Contacts.Phones.TYPE_MOBILE:
						msgValues.put(KEY_NB_TYPE, _context.getString(R.string.mevo_msgtype_mobile));
					break;
					case Contacts.Phones.TYPE_OTHER:
						msgValues.put(KEY_NB_TYPE, _context.getString(R.string.mevo_msgtype_other));
					break;
					case Contacts.Phones.TYPE_PAGER:
						msgValues.put(KEY_NB_TYPE, _context.getString(R.string.mevo_msgtype_pager));
					break;
					case Contacts.Phones.TYPE_WORK:
						msgValues.put(KEY_NB_TYPE, _context.getString(R.string.mevo_msgtype_work));
					break;
					default:
						msgValues.put(KEY_NB_TYPE, "???");
					break;
				}
				c.close();
			}
			else // Si pas de contact avec ce numéro
			{
				c.close();
				msgValues.put(KEY_CALLER, number);
				msgValues.put(KEY_NB_TYPE, _context.getString(R.string.unknown));
			}
		}
		else // Si numéro inconnu
		{
			msgValues.put(KEY_CALLER, _context.getString(R.string.unknown));
			msgValues.put(KEY_NB_TYPE, _context.getString(R.string.mevo_hidden_number));
		}
	}

	public static String convertDateTimeHR(String org)
	{
		String ret = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			Date deb = sdf.parse(org);
			Date fin = new Date();//sdf.parse(new Date());
			long diff = fin.getTime() - deb.getTime();
			int nbJours = (int) (diff / 86400000);
			String[] datetime = org.split(" ");
//			if (nbJours == 0)
//				ret = datetime[1];
//			else if (nbJours == 1)
//				ret = "Hier "+datetime[1];
//			else
			String[] date = datetime[0].split("-");
			if (nbJours <366)
			{
				ret = date[2]+"/"+date[1]+" "+datetime[1];
			}
			else
			{
				ret = date[2]+"/"+date[1]+"/"+date[0]+" "+datetime[1];				
			}
		}
		catch (ParseException e)
		{
			Log.e(TAG,"PARSE DATETIME HR "+e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}

	public void log()
	{
		Log.d(TAG,"--- MSG LOG ---");
		Log.d(TAG," STATUS   :" + msgValues.getAsInteger(KEY_STATUS));
		Log.d(TAG," PRESENCE :" + msgValues.getAsInteger(KEY_PRESENCE));
		Log.d(TAG," SOURCE   :" + msgValues.getAsString(KEY_SOURCE));
		Log.d(TAG," QUAND    :" + msgValues.getAsString(KEY_QUAND));
		Log.d(TAG," LINK     :" + msgValues.getAsString(KEY_LINK));
		Log.d(TAG," DEL      :" + msgValues.getAsString(KEY_DEL));
		Log.d(TAG," NAME     :" + msgValues.getAsString(KEY_NAME));
		Log.d(TAG," LENGTH   :" + msgValues.getAsInteger(KEY_LENGTH));
		Log.d(TAG," CALLER   :" + msgValues.getAsString(KEY_CALLER));
	}

	public void setMsgFromCursor(Cursor c)
	{
		ContentValues msgValues = this.msgValues;
		msgValues.put(KEY_STATUS, c.getInt(c.getColumnIndex(MevoDbAdapter.KEY_STATUS)));
		msgValues.put(KEY_PRESENCE, c.getInt(c.getColumnIndex(MevoDbAdapter.KEY_PRESENCE)));
		msgValues.put(KEY_SOURCE, c.getString(c.getColumnIndex(MevoDbAdapter.KEY_SOURCE)));
		msgValues.put(KEY_QUAND, c.getString(c.getColumnIndex(MevoDbAdapter.KEY_QUAND)));
		msgValues.put(KEY_LINK, c.getString(c.getColumnIndex(MevoDbAdapter.KEY_LINK)));
		msgValues.put(KEY_DEL, c.getString(c.getColumnIndex(MevoDbAdapter.KEY_DEL)));
		msgValues.put(KEY_NAME, c.getString(c.getColumnIndex(MevoDbAdapter.KEY_NAME)));
		msgValues.put(KEY_LENGTH, c.getInt(c.getColumnIndex(MevoDbAdapter.KEY_LENGTH)));
		getContactFromNumber(msgValues.getAsString(KEY_SOURCE));
		this.msgValues = msgValues;
		msgValues.put(KEY_QUAND_HR, convertDateTimeHR(c.getString(c.getColumnIndex(MevoDbAdapter.KEY_QUAND))));
	}

	public ContentValues getMessage()
	{
		return msgValues;
	}

	public void setIntValue(String k, int val, boolean updateDb)
	{
		this.msgValues.put(k, val);
		if (updateDb)
		{
			mDbHelper.updateIntValue(this.msgValues.getAsString(KEY_NAME), k, val);
		}
	}


	public boolean isMPInit()
	{
		return (this.mp != null);
	}

	public MediaPlayer getMP()
	{
		return this.mp;
	}

	public boolean setMsgSource(String src)
	{
		if (mp != null)
		{
			mp.release();
			mp = null;
		}

		this.mp = new MediaPlayer();
		try
		{
			// According to http://www.remwebdevelopment.com/dev/a63/Playing-Audio-error-PVMFErrNotSupported-Prepare-failed-status0x1-.html
			// Try workaround for Issue 91
			File file = new File(src);
		    FileInputStream fis = new FileInputStream(file);
		    mp.setDataSource(fis.getFD());
//			mp.setDataSource(src);
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mp.prepare();
			mp.setScreenOnWhilePlaying(true);
			mp.setVolume(1000,1000);
			return true;
		}
		catch (IllegalArgumentException e)
		{
			Log.e(TAG,"setMsgSource IllegalArgumentException : "+e.getMessage()+" ");
			e.printStackTrace();
		}
		catch (IllegalStateException e)
		{
			Log.e(TAG,"setMsgSource IllegalStateException : "+e.getMessage());
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			Log.e(TAG,"setMsgSource FileNotFoundException : "+e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e)
		{
			Log.e(TAG,"setMsgSource IOException : "+e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public String getStringValue(String k)
	{
		return msgValues.getAsString(k);
	}

	public int getIntValue(String k)
	{
		return msgValues.getAsInteger(k);
	}

	public void releaseMP()
	{
		if (mp != null)
		{
			mp.release();
			mp = null;
		}
	}
}
