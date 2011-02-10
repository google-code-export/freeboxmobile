package org.madprod.freeboxmobile.pvr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.guide.GuideConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * 
 * $Id$
 */

public class ChainesDbAdapter implements GuideConstants
{
	public static final String KEY_FAVORIS_ID = "fav_id";
	public static final String KEY_FAVORIS_TS = "fav_ts"; // timestamp of the last check
	
	public static final String KEY_HISTO_DATE = "date";

    public static final String KEY_CHAINE_NAME = "name";
    public static final String KEY_CHAINE_ID = "chaine_id"; // = gc_canal (KEY_GUIDECHAINE_CANAL)
    public static final String KEY_CHAINE_BOITIER = "chaine_boitier";
    
    public static final String KEY_GUIDECHAINE_FBXID = "gc_fbxid";
    public static final String KEY_GUIDECHAINE_IMAGE = "gc_image";
    public static final String KEY_GUIDECHAINE_ID = "gc_id"; // = channel_id (KEY_PROG_CHANNEL_ID)
    public static final String KEY_GUIDECHAINE_NAME = "gc_name";
    public static final String KEY_GUIDECHAINE_CANAL = "gc_canal"; // = chaine_id (KEY_CHAINE_ID)
//    public static final String KEY_GUIDECHAINE_FAVORIS = "gc_favoris"; // TODO : remove, unused
    
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

    public static final String KEY_PROG_GENRE_ID = "genre_id";
    public static final String KEY_PROG_CHANNEL_ID = "channel_id"; // = gc_id (KEY_GUIDECHAINE_ID)
    public static final String KEY_PROG_RESUM_S = "resum_s";
    public static final String KEY_PROG_RESUM_L = "resum_l";
    public static final String KEY_PROG_DATETIME_DEB = "datetime_deb";
    public static final String KEY_PROG_DATETIME_FIN = "datetime_fin";
    public static final String KEY_PROG_DUREE = "duree";
    public static final String KEY_PROG_TITLE = "title";

    private DatabaseHelper mDbHelper;
    public SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    
    private static final String TABLE_FAVORIS = " ("
    	+ KEY_FAVORIS_ID +" integer primary key, "
    	+ KEY_FAVORIS_TS +" datetime not null);";

	private static final String TABLE_HISTOGUIDE = " ("
    	+ KEY_ROWID+" integer primary key autoincrement, "
    	+ KEY_PROG_DATETIME_DEB+" datetime not null);";

	private static final String TABLE_DAYHISTOGUIDE = " ("
    	+ KEY_ROWID+" integer primary key autoincrement, "
    	+ KEY_HISTO_DATE +" date not null);";

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
//        + KEY_GUIDECHAINE_FAVORIS+" integer not null,"
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
    private static final String DATABASE_TABLE_CHAINESTEMP = "chainestemp";
    private static final String DATABASE_TABLE_SERVICES = "services";
    private static final String DATABASE_TABLE_SERVICESTEMP = "servicestemp";
    private static final String DATABASE_TABLE_BOITIERSDISQUES = "boitiersdisques";
    private static final String DATABASE_TABLE_BOITIERSDISQUESTEMP = "boitiersdisquestemp";
    private static final String DATABASE_TABLE_PROGRAMMES = "programmes";
    private static final String DATABASE_TABLE_GUIDECHAINES = "guidechaines";
    private static final String DATABASE_TABLE_HISTOGUIDE = "histoguide";
    private static final String DATABASE_TABLE_DAYHISTOGUIDE = "dayhistoguide";
    private static final String DATABASE_TABLE_FAVORIS = "favoris";

    private static final int DATABASE_VERSION = 30;

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

    private static final String DATABASE_CREATE_HISTOGUIDE =
        "create table "+DATABASE_TABLE_HISTOGUIDE+TABLE_HISTOGUIDE;

    private static final String DATABASE_CREATE_DAYHISTOGUIDE =
        "create table "+DATABASE_TABLE_DAYHISTOGUIDE+TABLE_DAYHISTOGUIDE;

    private static final String DATABASE_CREATE_FAVORIS =
        "create table "+DATABASE_TABLE_FAVORIS+TABLE_FAVORIS;

    private final Context mCtx;

    private SQLiteStatement sql_createRawChaine;
    private SQLiteStatement sql_createRawService;

    private static class DatabaseHelper extends SQLiteOpenHelper implements Constants
    {
        DatabaseHelper(Context context)
        {
			super(context, DATABASE_NAME+"_"+FBMHttpConnection.getIdentifiant(), null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            Log.i(TAG,"ChainesDbAdapter DatabaseHelper onCreate called");

            db.execSQL(DATABASE_CREATE_CHAINES);
            db.execSQL(DATABASE_CREATE_CHAINESTEMP);
            db.execSQL(DATABASE_CREATE_SERVICES);
            db.execSQL(DATABASE_CREATE_SERVICESTEMP);
            db.execSQL(DATABASE_CREATE_BOITIERSDISQUES);
            db.execSQL(DATABASE_CREATE_BOITIERSDISQUESTEMP);
            db.execSQL(DATABASE_CREATE_PROGRAMMES);
            db.execSQL(DATABASE_CREATE_GUIDECHAINES);
            db.execSQL(DATABASE_CREATE_HISTOGUIDE);
            db.execSQL(DATABASE_CREATE_DAYHISTOGUIDE);
            db.execSQL(DATABASE_CREATE_FAVORIS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG,"Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINESTEMP);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICESTEMP);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUESTEMP);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_PROGRAMMES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_GUIDECHAINES);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_HISTOGUIDE);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_DAYHISTOGUIDE);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_FAVORIS);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ChainesDbAdapter(Context ctx)
    {
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
    public ChainesDbAdapter open() throws SQLException
    {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
		this.sql_createRawChaine = null;
		this.sql_createRawService = null;
        return this;
    }

    public ChainesDbAdapter openRead() throws SQLException
    {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getReadableDatabase();
		this.sql_createRawChaine = null;
		this.sql_createRawService = null;
        return this;
    }

    public void close()
    {
    	close_sqls();
   		mDb.close();
    }

    /*
     * METHODES POUR LES FAVORIS
     * PRINCIPE : lors du mise à jour :
     * - on met à jour les timestamp des favoris qui existent déjà
     * - on ajoute les nouveaux favoris
     * - puis on supprime les vieux timestamp qui restent (qui correspondent aux chaînes qui ne sont plus en favoris)
     */
    
	public long updateFavoris(int fav_id, String datetime)
	{
		ContentValues favValues = new ContentValues();
		favValues.put(KEY_FAVORIS_TS, datetime);
		if (mDb.update(DATABASE_TABLE_FAVORIS, favValues, KEY_FAVORIS_ID + " = "+fav_id, null) == 0)
		{
			favValues.put(KEY_FAVORIS_ID, fav_id);
			mDb.insert(DATABASE_TABLE_FAVORIS, null, favValues);			
		}
		return 0;
	}
    
	public long flushFavoris(String datetime)
	{
		return mDb.delete(DATABASE_TABLE_FAVORIS, KEY_FAVORIS_TS +" != '"+datetime+"'", null);
	}
    
	public Cursor getFavoris()
	{
		return mDb.query(DATABASE_TABLE_FAVORIS, new String[] {KEY_FAVORIS_ID},
		       null, null, null, null, KEY_FAVORIS_ID);    	
	}
    
	public long getNbFavoris()
	{
		long ret;
		SQLiteStatement sqls;

		sqls = mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE_FAVORIS);
		ret = sqls.simpleQueryForLong();
		sqls.close();
		return ret;
	}

    /*
     * METHODES POUR L'HISTORIQUE DU GUIDE
     * Histoguide permet de savoir si on a déjà chargé les programmes pour 
     * un timestamp donné
     */
    
	/**
	 * DayHistoGuide : savoir si on a en cache (db) tous les programmes d'une journée
	 */
    public long createDayHistoGuide(String date)
    {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_HISTO_DATE, date);
        return mDb.insert(DATABASE_TABLE_DAYHISTOGUIDE, null, initialValues);
    }

    /**
     * HistoGuide : savoir si on a en cache (db) les programmes des 4 heures qui suivent un timestamp 
     * @param datetime
     * @return
     */
    public long createHistoGuide(String datetime)
    {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_PROG_DATETIME_DEB, datetime);
        return mDb.insert(DATABASE_TABLE_HISTOGUIDE, null, initialValues);
    }
    
	public long isDayHistoGuidePresent(String date)
	{
		long ret;
		SQLiteStatement sqls;

		sqls = mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE_DAYHISTOGUIDE + " WHERE "+KEY_HISTO_DATE+" = ?");
		sqls.bindString(1, date);
		ret = sqls.simpleQueryForLong();
		sqls.close();
		return ret;
	}

	public long isHistoGuidePresent(String datetime)
	{
		long ret;
		
		SQLiteStatement sqls = mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE_HISTOGUIDE + " WHERE "+KEY_PROG_DATETIME_DEB+" = ?");
		sqls.bindString(1, datetime);
		ret = sqls.simpleQueryForLong();
		sqls.close();
		return ret;
	}
	
	public long deleteOldHisto()
	{
		if (!mDb.inTransaction())
		{
			long res = 0;
			
			Calendar c = Calendar.getInstance();
	    	Integer mois = c.get(Calendar.MONTH)+1;
	    	Integer jour = c.get(Calendar.DAY_OF_MONTH);
			String date = c.get(Calendar.YEAR)+"-"+(mois<10?"0":"")+mois.toString()+"-"+(jour<10?"0":"")+jour.toString();
			try
			{
				res = mDb.delete(DATABASE_TABLE_HISTOGUIDE, KEY_PROG_DATETIME_DEB+" < '"+date+" 00:00:00'", null);
				res += mDb.delete(DATABASE_TABLE_DAYHISTOGUIDE, KEY_HISTO_DATE+" < '"+date+"'", null);
			}
			catch (SQLiteException e)
			{
				Log.e(TAG, "deleteOldHisto Exception : "+e.getMessage());
			}
			return res;
		}
		else
		{
			return -1;
		}
	}
	
	// Utilisé en cas d'ajout de nouvelles chaines aux favoris, 
	// comme la nouvelle chaine ne sera pas dans l'historique...
	public long clearHistorique()
	{
		long res = 0;
		
		try
		{
			res = mDb.delete(DATABASE_TABLE_DAYHISTOGUIDE, null, null);
			res += mDb.delete(DATABASE_TABLE_HISTOGUIDE, null, null);
		}
		catch (SQLiteException e)
		{
			Log.e(TAG, "clearHistorique Exception : "+e.getMessage());
		}
		return res;
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
//        initialValues.put(KEY_GUIDECHAINE_FAVORIS, 0);
        return mDb.insert(DATABASE_TABLE_GUIDECHAINES, null, initialValues);
    }

	public long isGuideChainePresent(int id)
	{
		long ret;
		SQLiteStatement sqls = mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE_GUIDECHAINES + " WHERE "+KEY_GUIDECHAINE_ID+" = ?");
		sqls.bindLong(1, id);
		ret = sqls.simpleQueryForLong();
		sqls.close();
		return ret;
	}

	/**
	 * get chaine details for selected guidechaine_id
	 * @param id
	 * @return
	 */
    public Cursor getGuideChaine(int id)
    {
		return mDb.query(DATABASE_TABLE_GUIDECHAINES, new String[] {KEY_ROWID, KEY_GUIDECHAINE_FBXID,
				KEY_GUIDECHAINE_ID, KEY_GUIDECHAINE_CANAL, KEY_GUIDECHAINE_NAME, KEY_GUIDECHAINE_IMAGE},
		        KEY_GUIDECHAINE_ID+" = "+id, null, null, null, KEY_GUIDECHAINE_ID);    	
    }
    
    public Cursor getListChaines()
    {
    	return mDb.query(DATABASE_TABLE_GUIDECHAINES, new String[] {
    			KEY_GUIDECHAINE_ID,
    			KEY_GUIDECHAINE_CANAL,
    			KEY_GUIDECHAINE_NAME,
    			KEY_GUIDECHAINE_IMAGE
    			},
		        null, null, null, null, KEY_GUIDECHAINE_CANAL);
    }

	public long getNbChaines()
	{
		long ret;
		SQLiteStatement sqls = mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE_GUIDECHAINES);
		ret = sqls.simpleQueryForLong();
		sqls.close();
		return ret;		
	}

	/*
     * METHODES POUR LES PROGRAMMES
     */

	public Cursor getProgsNow()
	{
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String now = sdf.format(new Date());
    	String sql =
    		"SELECT " + KEY_GUIDECHAINE_NAME+","+KEY_GUIDECHAINE_IMAGE+","+KEY_GUIDECHAINE_ID+","+KEY_GUIDECHAINE_CANAL+","+KEY_PROG_CHANNEL_ID+","+KEY_PROG_TITLE+","+KEY_PROG_DUREE+","+KEY_PROG_DATETIME_DEB+","+KEY_PROG_DATETIME_FIN+","+KEY_PROG_RESUM_L+
    		" FROM "+DATABASE_TABLE_PROGRAMMES+","+DATABASE_TABLE_GUIDECHAINES+
    		" WHERE "+KEY_PROG_DATETIME_FIN+" > '"+now+"' AND "+KEY_PROG_DATETIME_DEB+" <= '"+now+"' AND "+KEY_GUIDECHAINE_ID+" = "+KEY_PROG_CHANNEL_ID+
    		" ORDER BY "+KEY_GUIDECHAINE_CANAL;
//    	Log.i(TAG, "SQL : "+sql);
    	return mDb.rawQuery(sql, null);
	}

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
    
    public Cursor getNextProgs(int chaineId, String datetime, Integer nb)
    {
		return mDb.query(
			DATABASE_TABLE_PROGRAMMES,
			new String[]
	        {
				KEY_ROWID, KEY_PROG_CHANNEL_ID, KEY_PROG_TITLE, KEY_PROG_DUREE, KEY_PROG_DATETIME_DEB, KEY_PROG_DATETIME_FIN, KEY_PROG_GENRE_ID
			},
		    KEY_PROG_CHANNEL_ID+" = "+chaineId+" AND "+KEY_PROG_DATETIME_DEB+" >= '"+datetime+"'"
		    , null, null, null,
		    KEY_PROG_DATETIME_DEB,
		    PVR_MAX_PROGS.toString());    	
    }

    /**
     * get Programs for chaine <chaineId> where datefin > deb and datedeb < fin and correspond to categories
     * @param chaineId
     * @param deb
     * @param fin
     * @return
     */
    public Cursor getProgrammes(int chaineId, String deb, String fin, ArrayList<Categorie> categories)
    {
    	String where =
    		KEY_PROG_CHANNEL_ID+" = "+chaineId+" AND "+
        	KEY_PROG_DATETIME_FIN+" > '"+deb+"' AND "+
        	KEY_PROG_DATETIME_DEB+" < '"+fin+"'";
    	
    	String c = "";
    	for (int i = 0; i < categories.size(); i++)
    	{
    		if (categories.get(i).checked == true)
    		{
    			if (c.length() > 0)
    			{
    				c += " OR ";
    			}
   				c += KEY_PROG_GENRE_ID+" = "+categories.get(i).id;
    		}
    	}
    	
    	if (c.length() > 0)
    	{
    		where += " AND ("+c+")";
    	}
		return mDb.query(DATABASE_TABLE_PROGRAMMES, new String[]
                   {KEY_ROWID, KEY_PROG_GENRE_ID,
		        	KEY_PROG_CHANNEL_ID, KEY_PROG_RESUM_S, KEY_PROG_RESUM_L, KEY_PROG_TITLE,
		        	KEY_PROG_DUREE, KEY_PROG_DATETIME_DEB, KEY_PROG_DATETIME_FIN},
		        where
		        , null, null, null, KEY_PROG_DATETIME_DEB);
    }

	public boolean isProgrammePresent(int channelId, String horaire_deb)
	{
		long ret;
		SQLiteStatement sqls = mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE_PROGRAMMES + " WHERE "+KEY_PROG_CHANNEL_ID+" = ? AND "+KEY_PROG_DATETIME_DEB+" = ?");
		sqls.bindLong(1, channelId);
		sqls.bindString(2, horaire_deb);
		ret = sqls.simpleQueryForLong();
		sqls.close();
		return (ret > 0);
	}

	public int deleteOldProgs()
	{
		if (!mDb.inTransaction())
		{
			Calendar c = Calendar.getInstance();
	    	Integer mois = c.get(Calendar.MONTH)+1;
	    	Integer jour = c.get(Calendar.DAY_OF_MONTH);
			String date = c.get(Calendar.YEAR)+"-"+(mois<10?"0":"")+mois.toString()+"-"+(jour<10?"0":"")+jour.toString()+" 00:00:00";
			return mDb.delete(DATABASE_TABLE_PROGRAMMES, KEY_PROG_DATETIME_FIN+" < '"+date+"'", null);
		}
		else
		{
			return -1;
		}
	}

    /*
     * METHODES POUR LES CHAINES
     */

	private void close_sqls()
	{
		if (this.sql_createRawChaine != null)
		{
			this.sql_createRawChaine.close();
			this.sql_createRawService.close();
			this.sql_createRawChaine = null;
			this.sql_createRawService = null;
		}
	}
	
	public void create_sqls()
	{
		this.sql_createRawChaine = mDb.compileStatement("INSERT INTO "+DATABASE_TABLE_CHAINESTEMP+" ("+KEY_CHAINE_NAME+" , "+KEY_CHAINE_ID+" , "+KEY_CHAINE_BOITIER+") VALUES (?,?,?)");
		this.sql_createRawService = mDb.compileStatement("INSERT INTO "+DATABASE_TABLE_SERVICESTEMP+" ("+KEY_CHAINE_ID+" , "+KEY_CHAINE_BOITIER+" , "+KEY_SERVICE_DESC+" , "+KEY_SERVICE_ID+" , "+KEY_PVR_MODE+") VALUES (?,?,?,?,?)");		
	}
	
	public void makeChaines()
	{
		close_sqls();
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICESTEMP);
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINESTEMP);
        mDb.execSQL(DATABASE_CREATE_CHAINESTEMP);
        mDb.execSQL(DATABASE_CREATE_SERVICESTEMP);
        create_sqls();
	}
	
    public void swapChaines()
    {
    	if (checkTable(DATABASE_TABLE_SERVICESTEMP) && checkTable(DATABASE_TABLE_CHAINESTEMP))
    	{
    		close_sqls();
    		mDb.beginTransaction();
	        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICES);
	    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_SERVICESTEMP+" RENAME TO "+DATABASE_TABLE_SERVICES);
	        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINES);
	    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_CHAINESTEMP+" RENAME TO "+DATABASE_TABLE_CHAINES);
	    	mDb.setTransactionSuccessful();
	    	mDb.endTransaction();
    	}
    }

    public void cleanTempChaines()
    {
		close_sqls();
		mDb.beginTransaction();
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SERVICESTEMP);
        mDb.execSQL(DATABASE_CREATE_SERVICESTEMP);
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_CHAINESTEMP);
        mDb.execSQL(DATABASE_CREATE_CHAINESTEMP);
    	mDb.setTransactionSuccessful();
    	mDb.endTransaction();
        create_sqls();
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
    public long createChaine(String name, int chaine_id, int boitier_id)
    {
        ContentValues initialValues = new ContentValues(3);
        initialValues.put(KEY_CHAINE_NAME, name);
        initialValues.put(KEY_CHAINE_ID, chaine_id);
        initialValues.put(KEY_CHAINE_BOITIER, boitier_id);
        return mDb.insert(DATABASE_TABLE_CHAINESTEMP, null, initialValues);
    }
    
	public void createRawChaine(String name, Long chaine_id, Long boitier_id)
	{
//		Log.d(TAG, "Try to insert : "+name);
//		String sql = "INSERT INTO "+DATABASE_TABLE_CHAINESTEMP+" ("+KEY_CHAINE_NAME+" , "+KEY_CHAINE_ID+" , "+KEY_CHAINE_BOITIER+") VALUES (?,?,?)";
//		String sqls = "INSERT INTO "+DATABASE_TABLE_CHAINESTEMP+" ("+KEY_CHAINE_NAME+" , "+KEY_CHAINE_ID+" , "+KEY_CHAINE_BOITIER+") VALUES ('"+name+"',"+chaine_id.toString()+","+boitier_id.toString()+")";
//		SQLiteStatement sqls = mDb.compileStatement("INSERT INTO "+DATABASE_TABLE_CHAINESTEMP+" ("+KEY_CHAINE_NAME+" , "+KEY_CHAINE_ID+" , "+KEY_CHAINE_BOITIER+") VALUES (?,?,?)");
//		Object[] ba = new Object[]{name, chaine_id, boitier_id};
		sql_createRawChaine.bindString(1, name);
		sql_createRawChaine.bindLong(2,chaine_id);
		sql_createRawChaine.bindLong(3, boitier_id);
		Long r = sql_createRawChaine.executeInsert();
//		Log.d(TAG, "Try to insert chaine : "+r+" ");
//		mDb.execSQL(sql);
		return;
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
    
	public long getChainesNb()
	{
		long ret;
		SQLiteStatement sqls = mDb.compileStatement("SELECT COUNT(*) FROM "+DATABASE_TABLE_CHAINES);
		ret = sqls.simpleQueryForLong();
		sqls.close();
		return ret;
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

    public void createRawService(Long chaine_id, Long boitier_id, String service_desc, Integer service_id, Integer pvr_mode)
    {
    	sql_createRawService.bindLong(1, chaine_id);
    	sql_createRawService.bindLong(2, boitier_id);
    	sql_createRawService.bindString(3, service_desc);
    	sql_createRawService.bindLong(4, service_id);
    	sql_createRawService.bindLong(5, pvr_mode);
		Long r = sql_createRawService.executeInsert();
//		Log.d(TAG, "Try to insert serv : "+r+" ");
		return;
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

    public void makeBoitiersDisques()
    {
        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUESTEMP);
        mDb.execSQL(DATABASE_CREATE_BOITIERSDISQUESTEMP);    	    	
    }

    public void swapBoitiersDisques()
    {
    	if (checkTable(DATABASE_TABLE_BOITIERSDISQUESTEMP))
    	{
    		mDb.beginTransaction();
	        mDb.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_BOITIERSDISQUES);
	    	mDb.execSQL("ALTER TABLE "+DATABASE_TABLE_BOITIERSDISQUESTEMP+" RENAME TO "+DATABASE_TABLE_BOITIERSDISQUES);
	    	mDb.setTransactionSuccessful();
	    	mDb.endTransaction();
    	}
    }

    public boolean checkTable(String table)
    {
		boolean ret;
		SQLiteStatement sqls = mDb.compileStatement("SELECT COUNT("+KEY_ROWID+") FROM "+table);
		ret = sqls.simpleQueryForLong() > 0;
		sqls.close();
		return ret;
    }
    
	public long createBoitierDisque(String b_name, Long b_id, int d_free_size, int d_total_size, int d_id,
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
    
    public Cursor getListeDisques(int boitierId) throws SQLException
    {
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
            		KEY_BOITIER_ID + "='" + boitierId+"'",
            		null, null, null, null, null);
	    return mCursor;    	
    }

    public Cursor fetchBoitiers() throws SQLException
    {
        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE_BOITIERSDISQUES,
            		new String[] {
            		KEY_ROWID,
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
