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
    public static final String KEY_BOITIER = "boitier";
    public static final String KEY_DATE = "date";
    public static final String KEY_HEURE = "heure";
    public static final String KEY_DUREE = "duree";
    public static final String KEY_NOM = "nom";
    public static final String KEY_IDE = "ide";
    public static final String KEY_CHAINE_ID = "chaine_id";
    public static final String KEY_SERVICE_ID = "service_id";
    public static final String KEY_BOITIER_ID = "boitier_id";
    public static final String KEY_H = "h";
    public static final String KEY_MIN = "min";
    public static final String KEY_DUR = "dur";
    public static final String KEY_NAME = "name";
    public static final String KEY_WHERE_ID = "where_id";
    public static final String KEY_REPEAT_A = "repeat_a";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_STATUS = "status"; // 0:pas présent sur la console - 1:présent sur la console - 2:effacé par l'utilisateur

    private static final String TAG = "EnregistrementsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private static final String TABLE_ENREGISTREMENTS =
    	"(" + KEY_ROWID + " integer primary key autoincrement, "
		+ KEY_CHAINE + " text not null,"
		+ KEY_BOITIER + " text not null,"
		+ KEY_DATE + " text not null,"
		+ KEY_HEURE + " text not null,"
		+ KEY_DUREE + " integer not null,"
		+ KEY_NOM + " text not null,"
		+ KEY_IDE + " integer not null,"
		+ KEY_CHAINE_ID + " integer not null,"
		+ KEY_SERVICE_ID + " integer not null,"
		+ KEY_BOITIER_ID + " integer not null,"
		+ KEY_H + " integer not null,"
		+ KEY_MIN + " integer not null,"
		+ KEY_DUR + " integer not null,"
		+ KEY_NAME + " text not null,"
		+ KEY_WHERE_ID + " integer not null,"
		+ KEY_REPEAT_A + " text not null,"
		+ KEY_STATUS + " integer not null"
		+ ");";

    static final String DATABASE_NAME = "pvr";
    private static final String DATABASE_TABLE_ENR = "enregistrements";

    private static final int DATABASE_VERSION = 11;
    
    private static final String DATABASE_CREATE_ENR =
        "create table " + DATABASE_TABLE_ENR + TABLE_ENREGISTREMENTS;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
			super(context, DATABASE_NAME+"_"+FBMHttpConnection.getIdentifiant(), null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "DatabaseHelper onCreate called");
            db.execSQL(DATABASE_CREATE_ENR);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ENR);
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
    
    /**
     * Create a new enregistrement using the title and body provided. If the enregistrement is
     * successfully created return the new rowId for that enregistrement, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the enregistrement
     * @param body the body of the enregistrement
     * @return rowId or -1 if failed
     */
    public long createEnregistrement(String chaine, String boitier, String date, String heure, String duree,
    		String nom, String ide, String chaine_id, String service_id, int boitier_id, String h, String min,
    		String dur, String name, String where_id, String repeat_a)
    {
    	Log.i(TAG,"CREATE ENREGISTREMENT");
        ContentValues initialValues = new ContentValues();
        
        initialValues.put(KEY_CHAINE, chaine);
        initialValues.put(KEY_BOITIER, boitier);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_HEURE, heure);
        initialValues.put(KEY_DUREE, duree);
        initialValues.put(KEY_NOM, nom);
        initialValues.put(KEY_IDE, ide);
        initialValues.put(KEY_CHAINE_ID, chaine_id);
        initialValues.put(KEY_SERVICE_ID, service_id);
        initialValues.put(KEY_BOITIER_ID, boitier_id);
        initialValues.put(KEY_H, h);
        initialValues.put(KEY_MIN, min);
        initialValues.put(KEY_DUR, dur);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_WHERE_ID, where_id);
        initialValues.put(KEY_REPEAT_A, repeat_a);
        initialValues.put(KEY_STATUS, 1);

        return mDb.insert(DATABASE_TABLE_ENR, null, initialValues);
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
    public long modifyEnregistrement_unused(int rowId, String chaine, String boitier, String date, String heure, String duree,
    		String nom, String ide, String chaine_id, String service_id, int boitier_id, String h, String min,
    		String dur, String name, String where_id, String repeat_a) {
        ContentValues newValues = new ContentValues();
        
        newValues.put(KEY_CHAINE, chaine);
        newValues.put(KEY_BOITIER, boitier);
        newValues.put(KEY_DATE, date);
        newValues.put(KEY_HEURE, heure);
        newValues.put(KEY_DUREE, duree);
        newValues.put(KEY_NOM, nom);
        newValues.put(KEY_IDE, ide);
        newValues.put(KEY_CHAINE_ID, chaine_id);
        newValues.put(KEY_SERVICE_ID, service_id);
        newValues.put(KEY_BOITIER_ID, boitier_id);
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
     * En pratique, l'enregistrement voit juste son statut passer à 2
     * ca peut permettre un jour d'afficher l'historique
     * 
     * @param rowId id of enregistrement to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteEnregistrement(long rowId)
    {
    	ContentValues args = new ContentValues();
        args.put(KEY_STATUS, 2);
        return mDb.update(DATABASE_TABLE_ENR, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllEnregistrements(String[] colonnes, String sort)
    {
        return mDb.query(DATABASE_TABLE_ENR, colonnes, KEY_STATUS+" = 1", null, null, null, sort);
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
                		KEY_BOITIER,
                		KEY_DATE,
                		KEY_HEURE,
                		KEY_DUREE,
                		KEY_NOM,
                		KEY_IDE,
                		KEY_CHAINE_ID,
                		KEY_SERVICE_ID,
                		KEY_BOITIER_ID,
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

    /*
    public void swapEnr()
    {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_ENR);
    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_ENR_TEMP+" RENAME TO "+DATABASE_TABLE_ENR);
        mDb.execSQL(DATABASE_CREATE_ENR_TEMP);
    }
*/
    public boolean cleanEnregistrements(int bId)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_STATUS, 0);
    	return mDb.update(DATABASE_TABLE_ENR, args, KEY_BOITIER_ID + " = "+bId, null) > 0;
    }

	public long isEnregistrementPresent(int ide)
	{
		return mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE_ENR + " WHERE "+KEY_IDE+" = "+ide).simpleQueryForLong();
	}

    /**
     * Update the enregistrement using the details provided. The enregistrement to be updated is
     * specified using the ide, and it is altered to use the title and body
     * values passed in
     * @return true if the enregistrement was successfully updated, false otherwise
     */
    public boolean updateEnregistrement(String chaine, String boitier, String date, String heure,
    		String duree, String nom, String ide, String chaine_id, String service_id, String h,
    		String min, String dur, String name, String where_id, String repeat_a) {
        ContentValues args = new ContentValues();
        args.put(KEY_CHAINE, chaine);
        args.put(KEY_BOITIER, boitier);
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
        args.put(KEY_STATUS, 1);

        return mDb.update(DATABASE_TABLE_ENR, args, KEY_IDE + "=" + ide, null) > 0;
    }
}
