package org.madprod.freeboxmobile;

import java.io.IOException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.Contacts;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class Message implements Constants
{
	private ContentValues msgValues;
	private Context _context;
    protected FreeboxMobileDbAdapter mDbHelper;
	private MediaPlayer mp;
    
	// TODO : convertir le datetime en "hier, ..."

	public Message(Context c, FreeboxMobileDbAdapter db)
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
 
		Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(number));
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
		else
		{
			c.close();
			msgValues.put(KEY_CALLER, number);
			msgValues.put(KEY_NB_TYPE, "Inconnu");
		}
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
		msgValues.put(KEY_STATUS, c.getInt(c.getColumnIndex(FreeboxMobileDbAdapter.KEY_STATUS)));
		msgValues.put(KEY_PRESENCE, c.getInt(c.getColumnIndex(FreeboxMobileDbAdapter.KEY_PRESENCE)));
		msgValues.put(KEY_SOURCE, c.getString(c.getColumnIndex(FreeboxMobileDbAdapter.KEY_SOURCE)));
		msgValues.put(KEY_QUAND, c.getString(c.getColumnIndex(FreeboxMobileDbAdapter.KEY_QUAND)));
		msgValues.put(KEY_LINK, c.getString(c.getColumnIndex(FreeboxMobileDbAdapter.KEY_LINK)));
		msgValues.put(KEY_DEL, c.getString(c.getColumnIndex(FreeboxMobileDbAdapter.KEY_DEL)));
		msgValues.put(KEY_NAME, c.getString(c.getColumnIndex(FreeboxMobileDbAdapter.KEY_NAME)));
		msgValues.put(KEY_LENGTH, c.getInt(c.getColumnIndex(FreeboxMobileDbAdapter.KEY_LENGTH)));
		getContactFromNumber(msgValues.getAsString(KEY_SOURCE));
		this.msgValues = msgValues;
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
			mp.release();
	}
}
