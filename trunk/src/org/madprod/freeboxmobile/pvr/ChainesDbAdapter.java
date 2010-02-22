package org.madprod.freeboxmobile.pvr;

import org.madprod.freeboxmobile.FBMHttpConnection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author bduffez
 * $Id$
 */

public class ChainesDbAdapter {

    public static final String KEY_CHAINE_NAME = "name";
    public static final String KEY_CHAINE_ID = "chaine_id"; // = gc_canal (KEY_GUIDECHAINE_CANAL)
    public static final String KEY_CHAINE_BOITIER = "chaine_boitier";
    
    public static final String KEY_GUIDECHAINE_FBXID = "gc_fbxid";
    public static final String KEY_GUIDECHAINE_IMAGE = "gc_image";
    public static final String KEY_GUIDECHAINE_ID = "gc_id"; // = channel_id (KEY_PROG_CHANNEL_ID)
    public static final String KEY_GUIDECHAINE_NAME = "gc_name";
    public static final String KEY_GUIDECHAINE_CANAL = "gc_canal"; // = chaine_id (KEY_CHAINE_ID)
    
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

    public static final String KEY_PROG_NAME = "name";
    public static final String KEY_PROG_GENRE_ID = "genre_id";
    public static final String KEY_PROG_CHANNEL_ID = "channel_id"; // = gc_id (KEY_GUIDECHAINE_ID)
    public static final String KEY_PROG_RESUM_S = "resum_s";
    public static final String KEY_PROG_RESUM_L = "resum_l";
    public static final String KEY_PROG_DATETIME_DEB = "datetime_deb";
    public static final String KEY_PROG_DATETIME_FIN = "datetime_fin";
    public static final String KEY_PROG_DUREE = "duree";
    public static final String KEY_PROG_TITLE = "title";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    
    private static final String TABLE_PROGRAMMES = " ("
    	+ KEY_ROWID+" integer primary key autoincrement, "
    	+ KEY_PROG_GENRE_ID+" integer not null,"
    	+ KEY_PROG_CHANNEL_ID+" integer not null,"
        + KEY_PROG_RESUM_S+" text not null,"
        + KEY_PROG_RESUM_L+" text not null,"
        + KEY_PROG_TITLE+" text not null,"
        + KEY_PROG_DUREE+" int not null,"
        + KEY_PROG_DATETIME_DEB+" datetime not null,"
        + KEY_PROG_DATETIME_FIN+" datetime not null);";
    
    private static final String TABLE_GUIDECHAINES = " ("
    	+ KEY_ROWID+" integer primary key autoincrement, "
        + KEY_GUIDECHAINE_NAME+" text not null,"
        + KEY_GUIDECHAINE_IMAGE+" text not null,"
        + KEY_GUIDECHAINE_CANAL+" integer not null,"
        + KEY_GUIDECHAINE_ID+" integer not null,"
        + KEY_GUIDECHAINE_FBXID+" integer not null);";

    private static final String TABLE_CHAINES = " ("
    	+ KEY_ROWID+" integer primary key autoincrement, "
        + KEY_CHAINE_NAME+" text not null,"
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
    public static final String DATABASE_TABLE_CHAINESTEMP = "chainestemp";
    private static final String DATABASE_TABLE_SERVICES = "services";
    public static final String DATABASE_TABLE_SERVICESTEMP = "servicestemp";
    private static final String DATABASE_TABLE_BOITIERSDISQUES = "boitiersdisques";
    private static final String DATABASE_TABLE_BOITIERSDISQUESTEMP = "boitiersdisquestemp";
    private static final String DATABASE_TABLE_PROGRAMMES = "programmes";
    private static final String DATABASE_TABLE_GUIDECHAINES = "guidechaines";

    private static final int DATABASE_VERSION = 24;

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

    private static final String DATABASE_CREATE_PROGRAMMES =
        "create table "+DATABASE_TABLE_PROGRAMMES+TABLE_PROGRAMMES;

    private static final String DATABASE_CREATE_GUIDECHAINES =
        "create table "+DATABASE_TABLE_GUIDECHAINES+TABLE_GUIDECHAINES;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
			super(context, DATABASE_NAME+"_"+FBMHttpConnection.getIdentifiant(), null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            FBMHttpConnection.FBMLog("DatabaseHelper onCreate called");

            db.execSQL(DATABASE_CREATE_CHAINES);
            db.execSQL(DATABASE_CREATE_CHAINESTEMP);
            db.execSQL(DATABASE_CREATE_SERVICES);
            db.execSQL(DATABASE_CREATE_SERVICESTEMP);
            db.execSQL(DATABASE_CREATE_BOITIERSDISQUES);
            db.execSQL(DATABASE_CREATE_BOITIERSDISQUESTEMP);
            db.execSQL(DATABASE_CREATE_PROGRAMMES);
            db.execSQL(DATABASE_CREATE_GUIDECHAINES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            FBMHttpConnection.FBMLog("Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINESTEMP);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICESTEMP);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUESTEMP);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_PROGRAMMES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_GUIDECHAINES);
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
     * METHODES POUR LES CHAINES DU GUIDE
     */
    public long createGuideChaine(int fbxid, int id, int canal, String name, String image)
    {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_GUIDECHAINE_FBXID, fbxid);
        initialValues.put(KEY_GUIDECHAINE_ID, id);
        initialValues.put(KEY_GUIDECHAINE_CANAL, canal);
        initialValues.put(KEY_GUIDECHAINE_NAME, name);
        initialValues.put(KEY_GUIDECHAINE_IMAGE, image);
        return mDb.insert(DATABASE_TABLE_GUIDECHAINES, null, initialValues);
    }

	public long isGuideChainePresent(int id)
	{
		return mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE_GUIDECHAINES + " WHERE "+KEY_GUIDECHAINE_ID+" = "+id).simpleQueryForLong();
	}

    public Cursor getGuideChaine(int id)
    {
		return mDb.query(DATABASE_TABLE_GUIDECHAINES, new String[] {KEY_ROWID, KEY_GUIDECHAINE_FBXID,
				KEY_GUIDECHAINE_ID, KEY_GUIDECHAINE_CANAL, KEY_GUIDECHAINE_NAME, KEY_GUIDECHAINE_IMAGE},
		        KEY_GUIDECHAINE_ID+" = "+id, null, null, null, null);    	
    }

    public Cursor getAllGuideChaines2_unused()
    {
		return mDb.rawQuery("SELECT "+
					KEY_GUIDECHAINE_ID+","+
					KEY_GUIDECHAINE_CANAL+","+
					KEY_GUIDECHAINE_NAME+","+
					KEY_GUIDECHAINE_IMAGE+","+
					KEY_PROG_CHANNEL_ID+
				" FROM "+DATABASE_TABLE_GUIDECHAINES+" INNER JOIN "+ DATABASE_TABLE_PROGRAMMES+
				" ON "+DATABASE_TABLE_GUIDECHAINES+"."+KEY_GUIDECHAINE_ID+" = "+
					DATABASE_TABLE_PROGRAMMES+"."+KEY_PROG_CHANNEL_ID,null);
/*		return mDb.query(DATABASE_TABLE_GUIDECHAINES, new String[] {KEY_ROWID, KEY_GUIDECHAINE_FBXID,
				KEY_GUIDECHAINE_ID, KEY_GUIDECHAINE_CANAL, KEY_GUIDECHAINE_NAME, KEY_GUIDECHAINE_IMAGE},
		        null, null, null, null, KEY_GUIDECHAINE_CANAL);
		        */
    }

	/*
     * METHODES POUR LES PROGRAMMES
     */
    
    public long createProgramme(int genre_id, int channel_id, String resum_s, String resum_l, String title, int duree, String datetime_deb, String datetime_fin)
    {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PROG_GENRE_ID, genre_id);
        initialValues.put(KEY_PROG_CHANNEL_ID, channel_id);
        initialValues.put(KEY_PROG_RESUM_S, resum_s);
        initialValues.put(KEY_PROG_RESUM_L, resum_l);
        initialValues.put(KEY_PROG_TITLE, title);
        initialValues.put(KEY_PROG_DUREE, duree);
        initialValues.put(KEY_PROG_DATETIME_DEB, datetime_deb);
        initialValues.put(KEY_PROG_DATETIME_FIN, datetime_fin);
        return mDb.insert(DATABASE_TABLE_PROGRAMMES, null, initialValues);
    }
    
    public Cursor getProgrammes(int chaineId, String deb, String fin)
    {
		return mDb.query(DATABASE_TABLE_PROGRAMMES, new String[] {KEY_ROWID, KEY_PROG_GENRE_ID,
		        KEY_PROG_CHANNEL_ID, KEY_PROG_RESUM_S, KEY_PROG_TITLE, KEY_PROG_DUREE, KEY_PROG_DATETIME_DEB},
		        KEY_PROG_CHANNEL_ID+" = "+chaineId+" AND "+
		        KEY_PROG_DATETIME_FIN+" > '"+deb+"' AND "+
		        KEY_PROG_DATETIME_DEB+" < '"+fin+"'"
		        , null, null, null, KEY_PROG_DATETIME_DEB);
    }

	public long isProgrammePresent(int channelId, String horaire_deb)
	{
		return mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE_PROGRAMMES + " WHERE "+KEY_PROG_CHANNEL_ID+" = "+channelId+" AND "+KEY_PROG_DATETIME_DEB+" = '"+horaire_deb+"'").simpleQueryForLong();
	}
	
	public Cursor getChainesProg()
	{
        return mDb.query(true, DATABASE_TABLE_PROGRAMMES,
        		new String[] {
        		KEY_PROG_CHANNEL_ID,
        		},
        		null,
        		null, null, null, KEY_PROG_CHANNEL_ID, null);
	}

    /*
     * METHODES POUR LES CHAINES
     */

    public void swapChaines() {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICES);
    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_SERVICESTEMP+" RENAME TO "+DATABASE_TABLE_SERVICES);
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINES);
    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_CHAINESTEMP+" RENAME TO "+DATABASE_TABLE_CHAINES);
        mDb.execSQL(DATABASE_CREATE_CHAINESTEMP);
        mDb.execSQL(DATABASE_CREATE_SERVICESTEMP);
    }

    public void cleanTempChaines()
    {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICESTEMP);
        mDb.execSQL(DATABASE_CREATE_SERVICESTEMP);
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINESTEMP);
        mDb.execSQL(DATABASE_CREATE_CHAINESTEMP);
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
        initialValues.put(KEY_CHAINE_NAME, name);
        initialValues.put(KEY_CHAINE_ID, chaine_id);
        initialValues.put(KEY_CHAINE_BOITIER, boitier_id);
        return mDb.insert(DATABASE_TABLE_CHAINESTEMP, null, initialValues);
    }

    public SQLiteDatabase getDb()
    {
    	return mDb;
    }

    /**
     * Return a Cursor over the list of all Chaines in the database
     * 
     * @return Cursor over all Chaines
     */
    public Cursor fetchAllChaines(int boitier_id) 
    {
		return mDb.query(DATABASE_TABLE_CHAINES, new String[] {KEY_ROWID, KEY_CHAINE_NAME,
	        KEY_CHAINE_ID},
	        KEY_CHAINE_BOITIER + "=" + boitier_id, null, null, null, KEY_CHAINE_ID);
    }    
    
    /*
     * METHODES POUR LES SERVICES DE CHAINES (QUALITE) 
     */

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

    public Cursor fetchServicesChaine(int chaineId, int boitier_id)
    {
		return mDb.query(DATABASE_TABLE_SERVICES, new String[] {KEY_ROWID, KEY_SERVICE_DESC,
	        KEY_SERVICE_ID, KEY_PVR_MODE},
	        KEY_CHAINE_ID + "=" + chaineId + " AND " + KEY_CHAINE_BOITIER + "=" + boitier_id,
	        null, null, null, null, null);
    }

    
    /*
     * METHODES POUR LES BOITIERS/DISQUES
     */

    public void swapBoitiersDisques()
    {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUES);
    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_BOITIERSDISQUESTEMP+" RENAME TO "+DATABASE_TABLE_BOITIERSDISQUES);
        mDb.execSQL(DATABASE_CREATE_BOITIERSDISQUESTEMP);    	
    }

    public void cleanTempBoitiersDisques()
    {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUESTEMP);
        mDb.execSQL(DATABASE_CREATE_BOITIERSDISQUESTEMP);    	
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
}
