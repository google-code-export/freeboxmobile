package org.madprod.freeboxmobile.pvr;

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
    private static final String DATABASE_CREATE =
            "create table enregistrements (_id integer primary key autoincrement, "
			        + "chaine text not null,"
			        + "date text not null,"
			        + "heure text not null,"
			        + "duree integer not null,"
			        + "nom text not null,"
			        + "ide integer not null,"
			        + "chaine_id integer not null,"
			        + "service_id integer not null,"
			        + "h integer not null,"
			        + "min integer not null,"
			        + "dur integer not null,"
			        + "name text not null,"
			        + "where_id integer not null,"
			        + "repeat_a text);";

    private static final String DATABASE_NAME = "fbxvcr";
    private static final String DATABASE_TABLE = "enregistrements";
    private static final int DATABASE_VERSION = 3;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS enregistrements");
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

    	mDb.rawQuery("drop table enregistrements", null);
    	mDb.rawQuery("create table enregistrements (_id integer primary key autoincrement, "
	        + "date text not null,"
	        + "heure text not null,"
	        + "duree integer not null,"
	        + "nom text not null,"
	        + "ide integer not null,"
	        + "chaine_id integer not null,"
	        + "service_id integer not null,"
	        + "h integer not null,"
	        + "min integer not null,"
	        + "dur integer not null,"
	        + "name text not null,"
	        + "where_id integer not null,"
	        + "repeat_a text not null)", null);
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

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the enregistrement with the given rowId
     * 
     * @param rowId id of enregistrement to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteEnregistrement(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean deleteAllEnregistrements() {
    	
    	return mDb.delete(DATABASE_TABLE, "1", null) > 0;
    }

    /**
     * Return a Cursor over the list of all enregistrements in the database
     * 
     * @return Cursor over all enregistrements
     */
    public Cursor fetchAllEnregistrements(String[] colonnes) {

        return mDb.query(DATABASE_TABLE, colonnes, null, null, null, null, null);
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

                mDb.query(true, DATABASE_TABLE,
                		new String[] {
                		KEY_ROWID,
                		KEY_CHAINE,
                		KEY_DATE,
                		KEY_HEURE,
                		KEY_DUREE,
                		KEY_NOM,
                		KEY_IDE,
                		KEY_CHAINE_ID,
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
    		String duree, String nom, String ide, String chaine_id, String h,
    		String min, String dur, String name, String where_id, String repeat_a) {
        ContentValues args = new ContentValues();
        
        args.put(KEY_CHAINE, chaine);
        args.put(KEY_DATE, date);
        args.put(KEY_HEURE, heure);
        args.put(KEY_DUREE, duree);
        args.put(KEY_NOM, nom);
        args.put(KEY_IDE, ide);
        args.put(KEY_CHAINE_ID, chaine_id);
        args.put(KEY_H, h);
        args.put(KEY_MIN, min);
        args.put(KEY_DUR, dur);
        args.put(KEY_NAME, name);
        args.put(KEY_WHERE_ID, where_id);
        args.put(KEY_REPEAT_A, repeat_a);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
