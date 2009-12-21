package org.madprod.freeboxmobile;

import java.lang.String;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FreeboxMobileDbAdapter implements Constants
{
	private static final String TAG = "FreeboxMobileDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "freeboxmobile";
	private static final String DATABASE_TABLE = "mevo";
	private static final int DATABASE_VERSION = 3;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE =
		"create table "+DATABASE_TABLE+" (_id integer primary key autoincrement, "
			+ "status integer not null, presence integer not null, source text not null, quand datetime not null, "
			+ "length integer not null, link text not null, del text not null, name text not null);";

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public FreeboxMobileDbAdapter(Context ctx)
	{
		this.mCtx = ctx;
	}

	/**
	 * Open the messages database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public FreeboxMobileDbAdapter open() throws SQLException
	{
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close()
	{
		mDbHelper.close();
	}

	public boolean initTempValues()
	{
		ContentValues args = new ContentValues();
		args.put(KEY_LINK, "");
		args.put(KEY_DEL, "");
		return mDb.update(DATABASE_TABLE, args, KEY_DEL+"!=''", null) > 0;
	}

	public String convertDateTime(String org)
	{
		String dest = null;
		
		Log.d(DEBUGTAG,"= PARSE DATE 1");
		String[] datetime = org.split(" ");
		String[] date = datetime[1].split("/");
		String[] time = datetime[0].split(":");
		
		Log.d(DEBUGTAG,"PARSE DATETIME : "+date[2]+"-"+date[1]+"-"+date[0]+" "+time[0]+":"+time[1]+":"+time[2]);
		dest = date[2]+"-"+date[1]+"-"+date[0]+" "+time[0]+":"+time[1]+":"+time[2];
		return dest;
	}

	/**
	 * Create a new message using parameters provided. If the message is
	 * successfully created return the new rowId for that message, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param status
	 * @param source
	 * @param quand
	 * @param link
	 * @param del
	 * @param length
	 * @param name
	 * @return rowId or -1 if failed
	 */
	public long createMessage(int status, int presence, String source, String quand, String link, String del, int length, String name)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_STATUS, status);
		initialValues.put(KEY_PRESENCE, presence);
		initialValues.put(KEY_SOURCE, source);
		initialValues.put(KEY_QUAND, convertDateTime(quand));
		initialValues.put(KEY_LINK, link);
		initialValues.put(KEY_DEL, del);
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_LENGTH, length);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the message with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteMessage(long rowId)
	{
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean deleteAllMessages()
	{
		return mDb.delete(DATABASE_TABLE, null, null) > 0;		
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllMessages()
	{
		return mDb.query(DATABASE_TABLE, new String[]
		                                            { KEY_ROWID, KEY_STATUS, KEY_PRESENCE, KEY_SOURCE,
				 									  KEY_QUAND, KEY_LINK, KEY_DEL, KEY_LENGTH,
													  KEY_NAME
	                                            	}, KEY_PRESENCE + "> '0'", null, null, null, KEY_QUAND+" DESC");
	}
	
	/**
	 * Return a Cursor positioned at the note that matches the given name (filename of the message)
	 * 
	 * @param name
	 *            filename of message to retrieve
	 * @return Cursor positioned to matching message, if found
	 */
	public Cursor fetchMessage(String name)
	{
 		Cursor mCursor =
		mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_STATUS, KEY_PRESENCE, KEY_SOURCE,
				KEY_LINK, KEY_DEL, KEY_LENGTH, KEY_NAME }, KEY_NAME + "='" + name+"'", null, null, null, null,
				null);
		return mCursor;
	}

	/**
	 * Met à jour une valeur int d'un message
	 * @param name nom du fichier du message
	 * @param k    colonne à mettre à jour
	 * @param val  valeur à mettre à jour
	 * @return valeur de mDb.update
	 */
	public boolean updateIntValue(String name, String k, int val)
	{
		ContentValues args = new ContentValues();
		args.put(k, val);
		return mDb.update(DATABASE_TABLE, args, KEY_NAME+"='" + name+"'", null) > 0;	
	}

	/**
	 * Retourne le nombre de messages non lus
	 * @return nb de messages non lus
	 */
	public long getNbUnreadMsg()
	{
		long id;
		id = this.mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE + " WHERE status = '"+MSG_STATUS_UNLISTENED+"' AND "+KEY_PRESENCE+" > '0'").simpleQueryForLong();
		return id;
	}
	
	/**
	 * Update the message using the details provided. The message to be updated is
	 * specified using its name
	 * 
	 * @param name
	 *            name of message to update
	 * @return true if the message was successfully updated, false otherwise
	 */
	public boolean updateMessage(int presence, String link, String del, String name)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_PRESENCE, presence);
		args.put(KEY_LINK, link);
		args.put(KEY_DEL, del);
        return mDb.update(DATABASE_TABLE, args, KEY_NAME+"='" + name+"'", null) > 0;
	}
}
