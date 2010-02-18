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
 * $Id$
 */

public class ChainesDbAdapter {

    public static final String KEY_NAME = "name";
    public static final String KEY_CHAINE_ID = "chaine_id";
    public static final String KEY_CHAINE_BOITIER = "chaine_boitier";
    public static final String KEY_SERVICE_DESC = "service_desc";
    public static final String KEY_SERVICE_ID = "service_id";
    public static final String KEY_PVR_MODE = "pvr_mode";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_BOITIER_NAME = "b_name";
    public static final String KEY_BOITIER_ID = "b_id";
    public static final String KEY_DISQUE_FREE_SIZE = "d_free_size";
    public static final String KEY_DISQUE_TOTAL_SIZE = "d_total_size";
    public static final String KEY_DISQUE_ID = "d_id";
    public static final String KEY_DISQUE_NOMEDIA = "d_nomedia";
    public static final String KEY_DISQUE_DIRTY = "d_dirty";
    public static final String KEY_DISQUE_READONLY = "d_readonly";
    public static final String KEY_DISQUE_BUSY = "d_busy";
    public static final String KEY_DISQUE_MOUNT = "d_mount";
    public static final String KEY_DISQUE_LABEL = "d_label";

    private static final String TAG = "ChainesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    
    private static final String TABLE_CHAINES = " (_id integer primary key autoincrement, "
	        + KEY_NAME+" text not null,"
	        + KEY_CHAINE_ID+" integer not null,"
	        + KEY_CHAINE_BOITIER+" integer not null);";

    private static final String TABLE_SERVICES = " (_id integer primary key autoincrement, "
    	+ KEY_CHAINE_ID+" integer not null,"
    	+ KEY_CHAINE_BOITIER+" integer not null,"
        + KEY_SERVICE_DESC+" text not null,"
        + KEY_SERVICE_ID+" integer not null,"
        + KEY_PVR_MODE+" integer not null);";

    private static final String TABLE_BOITIERSDISQUES = " (_id integer primary key autoincrement, "
    	+ KEY_BOITIER_NAME+" text not null,"
    	+ KEY_BOITIER_ID+" integer not null,"
        + KEY_DISQUE_FREE_SIZE+" integer not null,"
        + KEY_DISQUE_TOTAL_SIZE+" integer not null,"
        + KEY_DISQUE_ID+" integer not null,"
        + KEY_DISQUE_NOMEDIA+" integer not null,"
        + KEY_DISQUE_DIRTY+" integer not null,"
        + KEY_DISQUE_READONLY+" integer not null,"
        + KEY_DISQUE_BUSY+" integer not null,"
        + KEY_DISQUE_MOUNT+" text not null,"
        + KEY_DISQUE_LABEL+" text not null);";

    private static final String DATABASE_NAME = "pvrchaines";
    private static final String DATABASE_TABLE_CHAINES = "chaines";
    private static final String DATABASE_TABLE_CHAINESTEMP = "chainestemp";
    private static final String DATABASE_TABLE_SERVICES = "services";
    private static final String DATABASE_TABLE_SERVICESTEMP = "servicestemp";
    private static final String DATABASE_TABLE_BOITIERSDISQUES = "boitiersdisques";
    private static final String DATABASE_TABLE_BOITIERSDISQUESTEMP = "boitiersdisquestemp";
    
    private static final int DATABASE_VERSION = 16;

    private static final String DATABASE_CREATE_CHAINES =
        "create table "+DATABASE_TABLE_CHAINES+TABLE_CHAINES;
    private static final String DATABASE_CREATE_CHAINESTEMP =
        "create table "+DATABASE_TABLE_CHAINESTEMP+TABLE_CHAINES;

    private static final String DATABASE_CREATE_SERVICES =
        "create table "+DATABASE_TABLE_SERVICES+TABLE_SERVICES;
    private static final String DATABASE_CREATE_SERVICESTEMP =
        "create table "+DATABASE_TABLE_SERVICESTEMP+TABLE_SERVICES;

    private static final String DATABASE_CREATE_BOITIERSDISQUES =
        "create table "+DATABASE_TABLE_BOITIERSDISQUES+TABLE_BOITIERSDISQUES;
    private static final String DATABASE_CREATE_BOITIERSDISQUESTEMP =
        "create table "+DATABASE_TABLE_BOITIERSDISQUESTEMP+TABLE_BOITIERSDISQUES;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
			super(context, DATABASE_NAME+"_"+FBMHttpConnection.getIdentifiant(), null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "DatabaseHelper onCreate called");

            db.execSQL(DATABASE_CREATE_CHAINES);
            db.execSQL(DATABASE_CREATE_CHAINESTEMP);
            db.execSQL(DATABASE_CREATE_SERVICES);
            db.execSQL(DATABASE_CREATE_SERVICESTEMP);
            db.execSQL(DATABASE_CREATE_BOITIERSDISQUES);
            db.execSQL(DATABASE_CREATE_BOITIERSDISQUESTEMP);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINESTEMP);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICESTEMP);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUESTEMP);
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

    public void swapChaines() {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICES);
    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_SERVICESTEMP+" RENAME TO "+DATABASE_TABLE_SERVICES);
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINES);
    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_CHAINESTEMP+" RENAME TO "+DATABASE_TABLE_CHAINES);
        mDb.execSQL(DATABASE_CREATE_CHAINESTEMP);
        mDb.execSQL(DATABASE_CREATE_SERVICESTEMP);
    }
    
    public void swapBoitiersDisques()
    {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUES);
    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_BOITIERSDISQUESTEMP+" RENAME TO "+DATABASE_TABLE_BOITIERSDISQUES);
        mDb.execSQL(DATABASE_CREATE_BOITIERSDISQUESTEMP);    	
    }

    public void cleanTempChaines()
    {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICESTEMP);
        mDb.execSQL(DATABASE_CREATE_SERVICESTEMP);
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINESTEMP);
        mDb.execSQL(DATABASE_CREATE_CHAINESTEMP);
    }

    public void cleanTempBoitiersDisques()
    {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUESTEMP);
        mDb.execSQL(DATABASE_CREATE_BOITIERSDISQUESTEMP);    	
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
    public long createChaine(String name, int chaine_id, int boitier_id) {
        ContentValues initialValues = new ContentValues(3);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_CHAINE_ID, chaine_id);
        initialValues.put(KEY_CHAINE_BOITIER, boitier_id);
        return mDb.insert(DATABASE_TABLE_CHAINESTEMP, null, initialValues);
    }
    
    public long createService(int chaine_id, int boitier_id, String service_desc, int service_id, int pvr_mode)
    {
    	ContentValues initialValues = new ContentValues(5);
        initialValues.put(KEY_CHAINE_ID, chaine_id);
        initialValues.put(KEY_CHAINE_BOITIER, boitier_id);
        initialValues.put(KEY_SERVICE_DESC, service_desc);
        initialValues.put(KEY_SERVICE_ID, service_id);
        initialValues.put(KEY_PVR_MODE, pvr_mode);        
        return mDb.insert(DATABASE_TABLE_SERVICESTEMP, null, initialValues);
    }

	public long createBoitierDisque(String b_name, int b_id, int d_free_size, int d_total_size, int d_id,
			int d_nomedia, int d_dirty, int d_readonly, int d_busy, String d_mount, String d_label)
	{
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_BOITIER_NAME, b_name);
        initialValues.put(KEY_BOITIER_ID, b_id);
        initialValues.put(KEY_DISQUE_FREE_SIZE,d_free_size);
        initialValues.put(KEY_DISQUE_TOTAL_SIZE,d_total_size);
        initialValues.put(KEY_DISQUE_ID,d_id);
        initialValues.put(KEY_DISQUE_NOMEDIA,d_nomedia);
        initialValues.put(KEY_DISQUE_DIRTY,d_dirty);
        initialValues.put(KEY_DISQUE_READONLY,d_readonly);
        initialValues.put(KEY_DISQUE_BUSY,d_busy);
        initialValues.put(KEY_DISQUE_MOUNT,d_mount);
        initialValues.put(KEY_DISQUE_LABEL,d_label);
        return mDb.insert(DATABASE_TABLE_BOITIERSDISQUESTEMP, null, initialValues);
	}

    public Cursor fetchDisque(int disqueId, String boitierName) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE_BOITIERSDISQUES,
                		new String[] {
                		KEY_ROWID,
                		KEY_BOITIER_NAME,
                		KEY_BOITIER_ID,
                		KEY_DISQUE_FREE_SIZE,
                		KEY_DISQUE_TOTAL_SIZE,
                		KEY_DISQUE_ID,
                		KEY_DISQUE_NOMEDIA,
                		KEY_DISQUE_DIRTY,
                		KEY_DISQUE_READONLY,
                		KEY_DISQUE_BUSY,
                		KEY_DISQUE_MOUNT,
                		KEY_DISQUE_LABEL,
                		},
                		KEY_BOITIER_NAME + "='" + boitierName + "' AND "+KEY_DISQUE_ID+"="+disqueId,
                		null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getListeDisques(String boitierName) throws SQLException {
        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE_BOITIERSDISQUES,
            		new String[] {
            		KEY_ROWID,
            		KEY_BOITIER_NAME,
            		KEY_BOITIER_ID,
            		KEY_DISQUE_FREE_SIZE,
            		KEY_DISQUE_TOTAL_SIZE,
            		KEY_DISQUE_ID,
            		KEY_DISQUE_NOMEDIA,
            		KEY_DISQUE_DIRTY,
            		KEY_DISQUE_READONLY,
            		KEY_DISQUE_BUSY,
            		KEY_DISQUE_MOUNT,
            		KEY_DISQUE_LABEL,
            		},
            		KEY_BOITIER_NAME + "='" + boitierName+"'",
            		null, null, null, null, null);
	    if (mCursor != null) {
	        mCursor.moveToFirst();
	    }
	    return mCursor;    	
    }

    public Cursor fetchBoitiers() throws SQLException
    {
        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE_BOITIERSDISQUES,
            		new String[] {
            		KEY_BOITIER_NAME,
            		KEY_BOITIER_ID,
            		}, "",
            		null, null, null, null, null);
	    if (mCursor != null) {
	        mCursor.moveToFirst();
	    }
	    return mCursor;    	
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
    public long modifyChaine_unused(int rowId, String name, int chaine_id, String service_desc,
    		int service_id, int pvr_mode) {
        ContentValues newValues = new ContentValues();

        newValues.put(KEY_NAME, name);
        newValues.put(KEY_CHAINE_ID, chaine_id);
        newValues.put(KEY_SERVICE_DESC, service_desc);
        newValues.put(KEY_SERVICE_ID, service_id);
        newValues.put(KEY_PVR_MODE, pvr_mode);
        
        String[] strRowId = new String[] { new Integer(rowId).toString() };
        
        Log.d(TAG, "MODIF = "+newValues.toString());

        return mDb.update(DATABASE_TABLE_CHAINES, newValues, KEY_ROWID+" = ?", strRowId);
    }

    /**
     * Delete the Chaine with the given rowId
     * 
     * @param rowId id of Chaine to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteChaine_unused(long rowId) {

        return mDb.delete(DATABASE_TABLE_CHAINES, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean deleteAllChaines_unused() {
    	
    	return mDb.delete(DATABASE_TABLE_CHAINES, "1", null) > 0;
    }

    /**
     * Return a Cursor over the list of all Chaines in the database
     * 
     * @return Cursor over all Chaines
     */
    public Cursor fetchAllChaines(int boitier_id) {
		return mDb.query(DATABASE_TABLE_CHAINES, new String[] {KEY_ROWID, KEY_NAME,
	        KEY_CHAINE_ID},
	        KEY_CHAINE_BOITIER + "=" + boitier_id, null, null, null, null);
    }

    public Cursor fetchServicesChaine(int chaineId, int boitier_id) {
		return mDb.query(DATABASE_TABLE_SERVICES, new String[] {KEY_ROWID, KEY_SERVICE_DESC,
	        KEY_SERVICE_ID, KEY_PVR_MODE},
	        KEY_CHAINE_ID + "=" + chaineId + " AND " + KEY_CHAINE_BOITIER + "=" + boitier_id,
	        null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the Chaine that matches the given rowId
     * 
     * @param rowId id of Chaine to retrieve
     * @return Cursor positioned to matching Chaine, if found
     * @throws SQLException if Chaine could not be found/retrieved
     */
    public Cursor fetchChaine_unused(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE_CHAINES,
                		new String[] {
                		KEY_ROWID,
                		KEY_NAME,
                		KEY_CHAINE_ID},
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
    public boolean updateChaine_unused(long rowId, String name, String chaine_id, String service_desc,
    		String service_id, String pvr_mode) {
        ContentValues args = new ContentValues();

        args.put(KEY_NAME, name);
        args.put(KEY_CHAINE_ID, chaine_id);
        args.put(KEY_SERVICE_DESC, service_desc);
        args.put(KEY_SERVICE_ID, service_id);
        args.put(KEY_PVR_MODE, pvr_mode);

        return mDb.update(DATABASE_TABLE_CHAINES, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
