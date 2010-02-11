package org.madprod.freeboxmobile.pvr;

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
 * @author bduffez
 *
 */
public class ChainesDbAdapter {

    public static final String KEY_NAME = "name";
    public static final String KEY_CHAINE_ID = "chaine_id";
    public static final String KEY_SERVICE_DESC = "service_desc";
    public static final String KEY_SERVICE_ID = "service_id";
    public static final String KEY_PVR_MODE = "pvr_mode";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "ChainesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    
    private static final String TABLE_CHAINE = " (_id integer primary key autoincrement, "
	        + "name text not null,"
	        + "chaine_id integer not null,"
	        + "service_desc text not null,"
	        + "service_id integer not null,"
	        + "pvr_mode integer not null);";

    private static final String DATABASE_NAME = "pvrchaines_" + FBMHttpConnection.getIdentifiant();
    private static final String DATABASE_TABLE = "chaines";
    private static final String DATABASE_TABLE_TEMP = "chainestemp";
    private static final int DATABASE_VERSION = 6;

    private static final String DATABASE_CREATE =
        "create table "+DATABASE_TABLE+TABLE_CHAINE;

    private static final String DATABASE_CREATE_TEMP =
        "create table "+DATABASE_TABLE_TEMP+TABLE_CHAINE;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "DatabaseHelper onCreate called");

            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE_TEMP);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_TEMP);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ChainesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the Chaines database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ChainesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDb.close();
        mDbHelper.close();
    }
/*
    public void detruire() {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
        mDb.execSQL(DATABASE_CREATE);
    }
*/
    public void swapChaines() {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_TEMP+" RENAME TO "+DATABASE_TABLE);
        mDb.execSQL(DATABASE_CREATE_TEMP);
    }

    /**
     * Create a new Chaine using the title and body provided. If the Chaine is
     * successfully created return the new rowId for that Chaine, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the Chaine
     * @param body the body of the Chaine
     * @return rowId or -1 if failed
     */
    public long createChaine(String name, int chaine_id, String service_desc, int service_id,
    		int pvr_mode) {
        ContentValues initialValues = new ContentValues();
        
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_CHAINE_ID, chaine_id);
        initialValues.put(KEY_SERVICE_DESC, service_desc);
        initialValues.put(KEY_SERVICE_ID, service_id);
        initialValues.put(KEY_PVR_MODE, pvr_mode);
        
        return mDb.insert(DATABASE_TABLE_TEMP, null, initialValues);
    }
    
    /**
     * Modifies an existing Chaine using the title and body provided. If the Chaine is
     * successfully created return the new rowId for that Chaine, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the Chaine
     * @param body the body of the Chaine
     * @return rowId or -1 if failed
     */
    public long modifyChaine(int rowId, String name, int chaine_id, String service_desc,
    		int service_id, int pvr_mode) {
        ContentValues newValues = new ContentValues();

        newValues.put(KEY_NAME, name);
        newValues.put(KEY_CHAINE_ID, chaine_id);
        newValues.put(KEY_SERVICE_DESC, service_desc);
        newValues.put(KEY_SERVICE_ID, service_id);
        newValues.put(KEY_PVR_MODE, pvr_mode);
        
        String[] strRowId = new String[] { new Integer(rowId).toString() };
        
        Log.d(TAG, "MODIF = "+newValues.toString());

        return mDb.update(DATABASE_TABLE, newValues, KEY_ROWID+" = ?", strRowId);
    }

    /**
     * Delete the Chaine with the given rowId
     * 
     * @param rowId id of Chaine to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteChaine(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean deleteAllChaines() {
    	
    	return mDb.delete(DATABASE_TABLE, "1", null) > 0;
    }

    /**
     * Return a Cursor over the list of all Chaines in the database
     * 
     * @return Cursor over all Chaines
     */
    public Cursor fetchAllChaines(String[] colonnes) {

        return mDb.query(DATABASE_TABLE, colonnes, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the Chaine that matches the given rowId
     * 
     * @param rowId id of Chaine to retrieve
     * @return Cursor positioned to matching Chaine, if found
     * @throws SQLException if Chaine could not be found/retrieved
     */
    public Cursor fetchChaine(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE,
                		new String[] {
                		KEY_ROWID,
                		KEY_NAME,
                		KEY_CHAINE_ID,
                		KEY_SERVICE_DESC,
                		KEY_SERVICE_ID,
                		KEY_PVR_MODE},
                		KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the Chaine using the details provided. The Chaine to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of Chaine to update
     * @param title value to set Chaine title to
     * @param body value to set Chaine body to
     * @return true if the Chaine was successfully updated, false otherwise
     */
    public boolean updateChaine(long rowId, String name, String chaine_id, String service_desc,
    		String service_id, String pvr_mode) {
        ContentValues args = new ContentValues();

        args.put(KEY_NAME, name);
        args.put(KEY_CHAINE_ID, chaine_id);
        args.put(KEY_SERVICE_DESC, service_desc);
        args.put(KEY_SERVICE_ID, service_id);
        args.put(KEY_PVR_MODE, pvr_mode);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
