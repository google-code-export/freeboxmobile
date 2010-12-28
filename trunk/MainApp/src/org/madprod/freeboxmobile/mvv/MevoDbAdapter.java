package org.madprod.freeboxmobile.mvv;

import java.lang.String; 

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

public class MevoDbAdapter implements MevoConstants
{

	private final ContentResolver resolver; 
	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public MevoDbAdapter(Context ctx)
	{
		resolver = ctx.getContentResolver();

//		Log.d(TAG,"DATABASE PATH : "+ctx.getDatabasePath("tptp"));
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

	public boolean initTempValues()
	{
		ContentValues args = new ContentValues();
		args.put(KEY_LINK, "");
		args.put(KEY_DEL, "");
		return resolver.update(CONTENT_URI, args, KEY_DEL+"!=''", null)> 0;
//		return mDb.update(DATABASE_TABLE, args, KEY_DEL+"!=''", null) > 0;
	}

	public String convertDateTime(String org)
	{
//		String dest = null;
		
		String[] datetime = org.split(" ");
		String[] date = datetime[1].split("/");
		String[] time = datetime[0].split(":");
		return (date[2]+"-"+date[1]+"-"+date[0]+" "+time[0]+":"+time[1]+":"+time[2]);
//		return dest;
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
	public Uri createMessage(int status, int presence, String source, String quand, String link, String del, int length, String name)
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
		return resolver.insert(CONTENT_URI, initialValues);
//		return mDb.insert(DATABASE_TABLE, null, initialValues);
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
		return resolver.delete(CONTENT_URI, KEY_ROWID + "=" + rowId, null)>0;	
//		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public boolean setMessageRead(long rowId)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_STATUS, MSG_STATUS_LISTENED);
		return resolver.update(CONTENT_URI, args, KEY_ROWID+"='" + rowId+"'", null)> 0;
//		return mDb.update(DATABASE_TABLE, args, KEY_ROWID+"='" + rowId+"'", null) > 0;		
	}
	
	public boolean setMessageUnRead(long rowId)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_STATUS, MSG_STATUS_UNLISTENED);
		return resolver.update(CONTENT_URI, args, KEY_ROWID+"='" + rowId+"'", null)> 0;
	}

	public boolean deleteAllMessages()
	{
		return resolver.delete(CONTENT_URI, null, null)> 0;
//		return mDb.delete(DATABASE_TABLE, null, null) > 0;		
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllMessages()
	{
		return resolver.query(CONTENT_URI, null, null, null, KEY_QUAND+" DESC");
//		return mDb.query(DATABASE_TABLE, new String[]
//		                                            { KEY_ROWID, KEY_STATUS, KEY_PRESENCE, KEY_SOURCE,
//				 									  KEY_QUAND, KEY_LINK, KEY_DEL, KEY_LENGTH,
//													  KEY_NAME
//	                                            	}, null, null, null, null, KEY_QUAND+" DESC");
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
		return resolver.query(CONTENT_URI, null, KEY_NAME + "='" + name+"'", null, KEY_QUAND+" DESC");
// 		Cursor mCursor =
//		mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_STATUS, KEY_PRESENCE, KEY_SOURCE,
//				KEY_LINK, KEY_DEL, KEY_LENGTH, KEY_NAME }, KEY_NAME + "='" + name+"'", null, null, null, null,
//				null);
//		return mCursor;
	}

	public Cursor fetchMessage(long id)
	{
		return resolver.query(CONTENT_URI, null, KEY_ROWID + "=" + id, null, KEY_QUAND+" DESC");
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
		
		return resolver.update(CONTENT_URI, args, KEY_NAME+"='" + name+"'", null)> 0;
//		return mDb.update(DATABASE_TABLE, args, KEY_NAME+"='" + name+"'", null) > 0;	
	}

//	/**
//	 * Retourne le nombre de messages non lus
//	 * @return nb de messages non lus
//	 */
//	public long getNbUnreadMsg()
//	{
//		return resolver.update(CONTENT_URI, args, KEY_NAME+"='" + name+"'", null)> 0;
//		return mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE + " WHERE status = '"+MSG_STATUS_UNLISTENED+"' AND "+KEY_PRESENCE+" > '0'").simpleQueryForLong();
//	}
//
//	public long getNbMsg()
//	{
//		return mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE + " WHERE "+KEY_PRESENCE+" > '0'").simpleQueryForLong();
//	}

	
	/**
	 * Update the message using the details provided. The message to be updated is
	 * specified using its name
	 * 
	 * @param presence presence value
	 * @param link link of the message
	 * @param del link to delete the message on server
	 * @param name name of message to update
	 * @return true if the message was successfully updated, false otherwise
	 */
	public boolean updateMessage(int presence, String link, String del, String name)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_PRESENCE, presence);
		args.put(KEY_LINK, link);
		args.put(KEY_DEL, del);
		return resolver.update(CONTENT_URI, args, KEY_NAME+"='" + name+"'", null)> 0;
//        return mDb.update(DATABASE_TABLE, args, KEY_NAME+"='" + name+"'", null) > 0;
	}
}
