package org.madprod.freeboxmobile.pvr;

import org.madprod.freeboxmobile.FBMHttpConnection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EnregistrementsDbAdapter {

    public static final String KEY_CHAINE = "chaine";
    public static final String KEY_DATE = "date";
    public static final String KEY_HEURE = "heure";
    public static final String KEY_DUREE = "duree";
    public static final String KEY_NOM = "nom";
    public static final String KEY_IDE = "ide";
    public static final String KEY_CHAINE_ID = "chaine_id";
    public static final String KEY_SERVICE_ID = "service_id";
    public static final String KEY_H = "h";
    public static final String KEY_MIN = "min";
    public static final String KEY_DUR = "dur";
    public static final String KEY_NAME = "name";
    public static final String KEY_WHERE_ID = "where_id";
    public static final String KEY_REPEAT_A = "repeat_a";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "EnregistrementsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private static final String TABLE_ENREGISTREMENTS =
            "(" + KEY_ROWID + " integer primary key autoincrement, "
			        + KEY_CHAINE + " text not null,"
			        + KEY_DATE + " text not null,"
			        + KEY_HEURE + " text not null,"
			        + KEY_DUREE + " integer not null,"
			        + KEY_NOM + " text not null,"
			        + KEY_IDE + " integer not null,"
			        + KEY_CHAINE_ID + " integer not null,"
			        + KEY_SERVICE_ID + " integer not null,"
			        + KEY_H + " integer not null,"
			        + KEY_MIN + " integer not null,"
			        + KEY_DUR + " integer not null,"
			        + KEY_NAME + " text not null,"
			        + KEY_WHERE_ID + " integer not null,"
			        + KEY_REPEAT_A + " text);";

    static final String DATABASE_NAME = "pvr";
    private static final String DATABASE_TABLE_ENR = "enregistrements";
    private static final String DATABASE_TABLE_ENR_TEMP = "enregistrementstemp";
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_CREATE_ENR =
        "create table " + DATABASE_TABLE_ENR + TABLE_ENREGISTREMENTS;
    private static final String DATABASE_CREATE_ENR_TEMP =
        "create table " + DATABASE_TABLE_ENR_TEMP + TABLE_ENREGISTREMENTS;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
			super(context, DATABASE_NAME+"_"+FBMHttpConnection.getIdentifiant(), null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "DatabaseHelper onCreate called");
            db.execSQL(DATABASE_CREATE_ENR);
            db.execSQL(DATABASE_CREATE_ENR_TEMP);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ENR);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ENR_TEMP);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public EnregistrementsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the enregistrements database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public EnregistrementsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    public void detruire() {

    	mDb.rawQuery("drop table "+DATABASE_TABLE_ENR, null);
    	mDb.rawQuery("drop table "+DATABASE_TABLE_ENR_TEMP, null);
    	mDb.rawQuery(DATABASE_CREATE_ENR, null);
    	mDb.rawQuery(DATABASE_CREATE_ENR_TEMP, null);
    }


    /**
     * Create a new enregistrement using the title and body provided. If the enregistrement is
     * successfully created return the new rowId for that enregistrement, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the enregistrement
     * @param body the body of the enregistrement
     * @return rowId or -1 if failed
     */
    public long createEnregistrement(String chaine, String date, String heure, String duree,
    		String nom, String ide, String chaine_id, String service_id, String h, String min,
    		String dur, String name, String where_id, String repeat_a) {
        ContentValues initialValues = new ContentValues();
        
        initialValues.put(KEY_CHAINE, chaine);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_HEURE, heure);
        initialValues.put(KEY_DUREE, duree);
        initialValues.put(KEY_NOM, nom);
        initialValues.put(KEY_IDE, ide);
        initialValues.put(KEY_CHAINE_ID, chaine_id);
        initialValues.put(KEY_SERVICE_ID, service_id);
        initialValues.put(KEY_H, h);
        initialValues.put(KEY_MIN, min);
        initialValues.put(KEY_DUR, dur);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_WHERE_ID, where_id);
        initialValues.put(KEY_REPEAT_A, repeat_a);

        return mDb.insert(DATABASE_TABLE_ENR_TEMP, null, initialValues);
    }
    
    /**
     * Modifies an existing enregistrement using the title and body provided. If the enregistrement is
     * successfully created return the new rowId for that enregistrement, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the enregistrement
     * @param body the body of the enregistrement
     * @return rowId or -1 if failed
     */
    public long modifyEnregistrement(int rowId, String chaine, String date, String heure, String duree,
    		String nom, String ide, String chaine_id, String service_id, String h, String min,
    		String dur, String name, String where_id, String repeat_a) {
        ContentValues newValues = new ContentValues();
        
        newValues.put(KEY_CHAINE, chaine);
        newValues.put(KEY_DATE, date);
        newValues.put(KEY_HEURE, heure);
        newValues.put(KEY_DUREE, duree);
        newValues.put(KEY_NOM, nom);
        newValues.put(KEY_IDE, ide);
        newValues.put(KEY_CHAINE_ID, chaine_id);
        newValues.put(KEY_SERVICE_ID, service_id);
        newValues.put(KEY_H, h);
        newValues.put(KEY_MIN, min);
        newValues.put(KEY_DUR, dur);
        newValues.put(KEY_NAME, name);
        newValues.put(KEY_WHERE_ID, where_id);
        newValues.put(KEY_REPEAT_A, repeat_a);
        
        String[] strRowId = new String[] { new Integer(rowId).toString() };
        
        Log.d(TAG, "MODIF = "+newValues.toString());

        return mDb.update(DATABASE_TABLE_ENR, newValues, KEY_ROWID+" = ?", strRowId);
    }

    /**
     * Delete the enregistrement with the given rowId
     * 
     * @param rowId id of enregistrement to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteEnregistrement(long rowId) {

        return mDb.delete(DATABASE_TABLE_ENR, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean deleteAllEnregistrements() {
    	
    	return mDb.delete(DATABASE_TABLE_ENR, "1", null) > 0;
    }

    /**
     * Return a Cursor over the list of all enregistrements in the database
     * 
     * @return Cursor over all enregistrements
     */
    public Cursor fetchAllEnregistrements(String[] colonnes) {
    	return fetchAllEnregistrements(colonnes, null);
    }
    public Cursor fetchAllEnregistrements(String[] colonnes, String sort) {

        return mDb.query(DATABASE_TABLE_ENR, colonnes, null, null, null, null, sort);
    }

    /**
     * Return a Cursor positioned at the enregistrement that matches the given rowId
     * 
     * @param rowId id of enregistrement to retrieve
     * @return Cursor positioned to matching enregistrement, if found
     * @throws SQLException if enregistrement could not be found/retrieved
     */
    public Cursor fetchEnregistrement(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE_ENR,
                		new String[] {
                		KEY_ROWID,
                		KEY_CHAINE,
                		KEY_DATE,
                		KEY_HEURE,
                		KEY_DUREE,
                		KEY_NOM,
                		KEY_IDE,
                		KEY_CHAINE_ID,
                		KEY_SERVICE_ID,
                		KEY_H,
                		KEY_MIN,
                		KEY_DUR,
                		KEY_NAME,
                		KEY_WHERE_ID,
                		KEY_REPEAT_A},
                		KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public void swapEnr()
    {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_ENR);
    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_ENR_TEMP+" RENAME TO "+DATABASE_TABLE_ENR);
        mDb.execSQL(DATABASE_CREATE_ENR_TEMP);
    }

    /**
     * Update the enregistrement using the details provided. The enregistrement to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of enregistrement to update
     * @param title value to set enregistrement title to
     * @param body value to set enregistrement body to
     * @return true if the enregistrement was successfully updated, false otherwise
     */
    public boolean updateEnregistrement(long rowId, String chaine, String date, String heure,
    		String duree, String nom, String ide, String chaine_id, String service_id, String h,
    		String min, String dur, String name, String where_id, String repeat_a) {
        ContentValues args = new ContentValues();
        
        args.put(KEY_CHAINE, chaine);
        args.put(KEY_DATE, date);
        args.put(KEY_HEURE, heure);
        args.put(KEY_DUREE, duree);
        args.put(KEY_NOM, nom);
        args.put(KEY_IDE, ide);
        args.put(KEY_CHAINE_ID, chaine_id);
        args.put(KEY_SERVICE_ID, service_id);
        args.put(KEY_H, h);
        args.put(KEY_MIN, min);
        args.put(KEY_DUR, dur);
        args.put(KEY_NAME, name);
        args.put(KEY_WHERE_ID, where_id);
        args.put(KEY_REPEAT_A, repeat_a);

        return mDb.update(DATABASE_TABLE_ENR, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
