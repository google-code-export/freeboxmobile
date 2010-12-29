package org.madprod.freeboxmobile.mvv;

import android.database.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.madprod.freeboxmobile.FBMHttpConnection;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MevoProvider extends ContentProvider implements MevoConstants{
	
	
	public DatabaseHelper mDbHelper;
	public SQLiteDatabase mDb;

	public static final String DATABASE_NAME = "freeboxmobile";
	private static final String DATABASE_TABLE = "mevo";
	private static final int DATABASE_VERSION = 4;


	
	
	/**
	 * Database creation sql statement
	 */
	// TODO : Utiliser les constantes de MevoConstantes là dedans...
	private static final String DATABASE_CREATE =
		"create table "+DATABASE_TABLE+" (_id integer primary key autoincrement, "
			+ "status integer not null, presence integer not null, source text not null, quand datetime not null, "
			+ "length integer not null, link text not null, del text not null, name text not null);";


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
			Log.w(TAG, "MevoDbAdapter : Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			if (oldVersion == 3 && newVersion == 4){
			}else{
				db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
				onCreate(db);
			}
		}
	}

	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.e(TAG, "delete uri = "+uri);
		int count = 0;
		
		if (isCollectionUri(uri)){
			count = mDb.delete(DATABASE_TABLE, selection, selectionArgs);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		Log.e(TAG, "getType uri = "+uri);
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		Log.e(TAG, "insert uri = "+uri);
		long rowId;
		ContentValues values;
		
		if (initialValues != null){
			values = new ContentValues(initialValues);
		}else{
			values = new ContentValues();
		}

		if (!isCollectionUri(uri)){
			throw new IllegalArgumentException("Uri inconnue : "+uri);
		}
		
		rowId = mDb.insert(DATABASE_TABLE, KEY_NAME,values);

		if (rowId > 0){
			Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(newUri, null);
			return uri;
		}
		throw new SQLException("Uri inconnue : "+uri);
//		throw new SQLException("Echec de l'insertion dans "+uri);
	}

	@Override
	public boolean onCreate() {
		mDbHelper = new DatabaseHelper(getContext());
		mDb = mDbHelper.getWritableDatabase();
		return (mDb == null)?false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(DATABASE_TABLE);
		
		if (isCollectionUri(uri)){
			qb.setProjectionMap(getDefaultProjection());
		}else{
			qb.appendWhere(getIdColumnName() + "=" + uri.getPathSegments().get(1));
		}
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)){
			orderBy = getDefaultSortOrder();
		}else{
			orderBy = sortOrder;
		}
		
		Cursor c = qb.query(mDb, projection, selection, null, null, null, orderBy);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		Log.e(TAG, "query uri = "+uri+" "+c.getCount());		
		return c;
		}



	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.e(TAG, "update uri = "+uri);
		int count = 0;

		if (isCollectionUri(uri)){
			count = mDb.update(DATABASE_TABLE, values, selection, selectionArgs);
		}
		
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private String getIdColumnName() {
		return KEY_ROWID;
	}

	private boolean isCollectionUri(Uri uri){
		return (uri.compareTo(CONTENT_URI) == 0)?true:false;
	}

	private Map<String, String> getDefaultProjection() {
		HashMap<String, String> projection = new HashMap<String, String>();
        projection.put(KEY_ROWID, KEY_ROWID);
        projection.put(KEY_STATUS, KEY_STATUS);
        projection.put(KEY_PRESENCE, KEY_PRESENCE);
        projection.put(KEY_SOURCE, KEY_SOURCE);
        projection.put(KEY_QUAND, KEY_QUAND);
        projection.put(KEY_LINK, KEY_LINK);
        projection.put(KEY_DEL, KEY_DEL);
        projection.put(KEY_LENGTH, KEY_LENGTH);
        projection.put(KEY_NAME, KEY_NAME);
        return projection;
	}
	
	private String getDefaultSortOrder() {
		return KEY_QUAND+" DESC";
	}
	
}
