package org.madprod.freeboxmobile.mvv;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
			Log.e(DEBUGTAG,"PARSE DATETIME HR "+e);
		}
		return ret;
	}

	public void log()
	{
		Log.d(DEBUGTAG,"--- MSG LOG ---");
		Log.d(DEBUGTAG," STATUS   :" + msgValues.getAsInteger(KEY_STATUS));
		Log.d(DEBUGTAG," PRESENCE :" + msgValues.getAsInteger(KEY_PRESENCE));
		Log.d(DEBUGTAG," SOURCE   :" + msgValues.getAsString(KEY_SOURCE));
		Log.d(DEBUGTAG," QUAND    :" + msgValues.getAsString(KEY_QUAND));
		Log.d(DEBUGTAG," LINK     :" + msgValues.getAsString(KEY_LINK));
		Log.d(DEBUGTAG," DEL      :" + msgValues.getAsString(KEY_DEL));
		Log.d(DEBUGTAG," NAME     :" + msgValues.getAsString(KEY_NAME));
		Log.d(DEBUGTAG," LENGTH   :" + msgValues.getAsInteger(KEY_LENGTH));
		Log.d(DEBUGTAG," CALLER   :" + msgValues.getAsString(KEY_CALLER));
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

	public void setMsgSource(String src)
	{
		if (mp != null)
			mp.release();

		this.mp = new MediaPlayer();
		try
		{
			mp.setDataSource(src);
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mp.prepare();
			mp.setScreenOnWhilePlaying(true);
			mp.setVolume(1000,1000);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
