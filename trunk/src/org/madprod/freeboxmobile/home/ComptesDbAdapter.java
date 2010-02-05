package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.Constants;

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

public class ComptesDbAdapter implements Constants
{
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "comptes";
    private static final String DATABASE_TABLE = "comptes";
    private static final int DATABASE_VERSION = 6;

    private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, "
                + KEY_TITLE +" text not null, " + KEY_USER + " text not null, "
                + KEY_PASSWORD + " text not null, " + KEY_NRA + " text not null, "
                + KEY_DSLAM + " text not null, " + KEY_IP + " text not null, "
                + KEY_TEL + " text not null, "+ KEY_LINELENGTH + " text not null, "
                + KEY_ATTN + " text not null, "+ KEY_LINETYPE + " text not null, "
                + KEY_FBMVERSION + " text not null );";

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
    		Log.d(DEBUGTAG, "ComptesDbAdapter : Upgrading database from version " + oldVersion + " to "
                + newVersion);
    		if (newVersion > oldVersion)
    		{
	    		db.beginTransaction();
	    		boolean success = true;
	    		int nextVersion;
	    		for (int i = oldVersion; i < newVersion ; i++)
	    		{
	    			nextVersion = i + 1;
	    			switch (nextVersion)
	    			{
	    				case 3:
	    					success = upgradeToVersion3(db);
	    				break;
	    				case 4:
	    					success = upgradeToVersion4(db);
	    				break;
	    				case 5:
	    					success = upgradeToVersion5(db);
	    				break;
	    				case 6:
	    					success = upgradeToVersion6(db);
	    				break;
	    			}
	    			if (!success)
	    				break;
	    		}
	    		if (success)
	    		{
	    			db.setTransactionSuccessful();
	    		}
	    		db.endTransaction();
    		}
    		else
    		{
//	    		db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
	    		onCreate(db);
    		}
    	}
    	
    	boolean upgradeToVersion3(SQLiteDatabase db)
    	{
    		db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD "+KEY_NRA + " text DEFAULT '' not null");
    		db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD "+KEY_DSLAM + " text DEFAULT '' not null");
    		db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD "+KEY_IP + " text DEFAULT '' not null");
    		db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD "+KEY_TEL + " text DEFAULT '' not null");
    		return true;
    	}

    	boolean upgradeToVersion4(SQLiteDatabase db)
    	{
    		db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD "+KEY_LINELENGTH + " text DEFAULT '' not null");
    		db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD "+KEY_ATTN + " text DEFAULT '' not null");
    		return true;
    	}

    	boolean upgradeToVersion5(SQLiteDatabase db)
    	{
    		db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD "+KEY_LINETYPE + " text DEFAULT '' not null");
    		return true;
    	}
    	
    	boolean upgradeToVersion6(SQLiteDatabase db)
    	{
    		db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD "+KEY_FBMVERSION + " text DEFAULT '' not null");
    		return true;
    	}
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ComptesDbAdapter(Context ctx)
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
    public ComptesDbAdapter open() throws SQLException
    {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }

    /**
     * Create a new compte using the title and body provided. If the compte is
     * successfully created return the new rowId for that compte, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the compte
     * @param login the login of the compte
     * @param password the password of the compte
     * @return rowId or -1 if failed
     */
    public long createCompte(String title, String user, String password, String nra, String dslam, String ip, String length, String attn, String tel, String linetype, String fbmversion)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_USER, user);
        initialValues.put(KEY_PASSWORD, password);
        initialValues.put(KEY_NRA, nra);
        initialValues.put(KEY_DSLAM, dslam);
        initialValues.put(KEY_IP, ip);
        initialValues.put(KEY_LINELENGTH, length);
        initialValues.put(KEY_ATTN, attn);
        initialValues.put(KEY_TEL, tel);
        initialValues.put(KEY_LINETYPE, linetype);
        initialValues.put(KEY_FBMVERSION, fbmversion);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the compte with the given rowId
     * 
     * @param rowId id of compte to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteCompte(long rowId)
    {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all comptes in the database
     * 
     * @return Cursor over all comptes
     */
    public Cursor fetchAllComptes()
    {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_USER, KEY_PASSWORD, KEY_NRA, KEY_DSLAM, KEY_IP, KEY_TEL,
                KEY_LINELENGTH, KEY_ATTN, KEY_LINETYPE, KEY_FBMVERSION}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the compte that matches the given rowId
     * 
     * @param rowId id of compte to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchCompte(long rowId) throws SQLException
    {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_USER, KEY_PASSWORD, KEY_NRA, KEY_DSLAM,
                        KEY_IP, KEY_TEL, KEY_LINELENGTH, KEY_ATTN, KEY_LINETYPE, KEY_FBMVERSION}, KEY_ROWID
                        + "=" + rowId, null, null, null, null, null);
        if (mCursor != null)
        {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Update the compte using the details provided. The compte to be updated is
     * specified using the rowId, and it is altered to use the title, login and password
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set compte title to
     * @param login value to set compte login to
     * @param password value to set compte password to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateCompte(long rowId, String title, String user, String password, String nra, String dslam, String ip, String length, String attn, String tel, String linetype, String fbmversion)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_USER, user);
        args.put(KEY_PASSWORD, password);
        args.put(KEY_NRA, nra);
        args.put(KEY_DSLAM, dslam);
        args.put(KEY_IP, ip);
        args.put(KEY_LINELENGTH, length);
        args.put(KEY_ATTN, attn);
        args.put(KEY_TEL, tel);
        args.put(KEY_LINETYPE, linetype);
        args.put(KEY_FBMVERSION, fbmversion);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * isValuePresent returns true if value is present in column key 
     * @param key
     * @param value
     * @return
     */
    public boolean isValuePresent(String key, String value)
    {
    	Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
        		}, key + "='" + value + "'", null,
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

    public Cursor fetchFromTitle(String title)
    {
    	Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
    			KEY_ROWID,
                KEY_TITLE, KEY_USER, KEY_PASSWORD, KEY_NRA, KEY_DSLAM,
                KEY_IP, KEY_TEL, KEY_LINELENGTH, KEY_ATTN, KEY_LINETYPE,
                KEY_FBMVERSION
    			}, KEY_TITLE + "='" + title + "'", null,
                null, null, null, null);
        if (mCursor != null)
        {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * getLoginFromId returns login associated with given rowid
     * @param id
     * @param key key to retreive
     * @return login
     */
    public String getValueFromId(Integer id, String key)
    {
    	Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
            		key}, KEY_ROWID + "='" + id + "'", null,
                    null, null, null, null);
	    if ((mCursor != null) && (mCursor.moveToFirst()))
	    {
	        return mCursor.getString(mCursor.getColumnIndexOrThrow(key));
	    }
	    else
	    {
	    	return null;
	    }
    }

    /**
     * getIdFromLogin returns rowid associated with given login
     * @param login
     * @return rowid or null (if not found)
     */
    public Long getIdFromLogin(String login)
    {
    	Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
            		KEY_ROWID}, KEY_USER + "='" + login + "'", null,
                    null, null, null, null);
	    if ((mCursor != null) && (mCursor.moveToFirst()))
	    {
	    	Long ret = mCursor.getLong(mCursor.getColumnIndexOrThrow(KEY_ROWID));
	    	mCursor.close();
	        return ret;
	    }
	    else
	    {
	    	return null;
	    }
    }   

    /**
     * Return true if key == value and rowid == id
     * @param mRowId
     * @param key
     * @param value
     * @return
     */
    public boolean isMatch(Long mRowId, String key, String value)
    {
    	Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
        		}, KEY_ROWID + "='" + mRowId + "' AND "+key+"='"+value+"'", null,
                null, null, null, null);
    	if ((mCursor != null) && (mCursor.getCount() > 0))
    		return true;
    	else
    		return false;
    }

    /**
     * getCompteNumber : retourne le nombre de comptes présents dans la bdd
     * @return nombre de comptes présents ds la bdd
     */
    public int getComptesNumber()
    {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
        		}, null, null, null, null, null, null);
	    if (mCursor != null)
	    {
	    	return mCursor.getCount();
	    }
	    return 0;
    }
}
