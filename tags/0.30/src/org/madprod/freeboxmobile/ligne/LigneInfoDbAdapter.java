package org.madprod.freeboxmobile.ligne;

import org.madprod.freeboxmobile.FBMHttpConnection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class LigneInfoDbAdapter implements LigneInfoConstants
{
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "ligneinfo";
    private static final String DATABASE_TABLE = "tickets";
    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, "
                + KEY_TICKETID +" integer, "+ KEY_TITLE +" text not null, " + KEY_DESC + " text not null, "
                + KEY_START + " datetime not null, " + KEY_END + " datetime not null );";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
    	DatabaseHelper(Context context)
    	{
			super(context, DATABASE_NAME+"_"+FBMHttpConnection.getIdentifiant(), null, DATABASE_VERSION);
    	}

    	@Override
    	public void onCreate(SQLiteDatabase db)
    	{
    		db.execSQL(DATABASE_CREATE);
    	}

    	@Override
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    	{
    		Log.d(DEBUGTAG, "ComptesDbAdapter : Upgrading database from version " + oldVersion + " to "
                + newVersion);
    		db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
    		onCreate(db);
    	}
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public LigneInfoDbAdapter(Context ctx)
    {
        this.mCtx = ctx;
    }

    /**
     * Open the comptes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public LigneInfoDbAdapter open() throws SQLException
    {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
    	mDb.close();
//        mDbHelper.close();
    }

    /**
     * Create a new ticket
     * 
     * @param ticketId the id of the ticket
     * @param title the title of the ticket
     * @param desc the description of the ticket
     * @param start the start date of the ticket
     * @param end the end date of the ticket
     * @return rowId or -1 if failed
     */
    public long createTicket(Integer ticketId, String title, String desc, String start, String end)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TICKETID, ticketId);
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_DESC, desc);
        initialValues.put(KEY_START, start);
        initialValues.put(KEY_END, end);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Return a Cursor over the list of all comptes in the database
     * 
     * @return Cursor over all comptes
     */
    public Cursor fetchAllTickets()
    {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TICKETID,
                KEY_TITLE, KEY_DESC, KEY_START, KEY_END},
                null, null, null, null,  KEY_END+" DESC");
    }

    /**
     * Return a Cursor positioned at the ticket that matches the given rowId
     * 
     * @param rowId id of ticket to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchTicket(long rowId) throws SQLException
    {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_TICKETID, KEY_TITLE, KEY_DESC, KEY_START, KEY_END},
                        KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null)
        {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * isTicketPresent returns true if ticket is present in db 
     * @param ticketId : the ticketid
     * @return
     */
    public boolean isTicketPresent(Integer ticketId)
    {
    	Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
        		}, KEY_TICKETID + "=" + ticketId, null,
                null, null, null, null);
	    if ((mCursor != null) && (mCursor.moveToFirst()))
	    {
	    	return true;
	    }
	    else
	    {
	    	return false;
	    }    	
    }
}
