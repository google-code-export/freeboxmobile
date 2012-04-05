package org.madprod.freeboxmobile.utils;

import static org.madprod.freeboxmobile.StaticConstants.KEY_PREFS;
import static org.madprod.freeboxmobile.StaticConstants.TAG;
import static org.madprod.freeboxmobile.StaticConstants.PREFS_APN_VERSION;

import java.lang.reflect.Field;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class ApnCheck
{
	Context mContext = null;

	public static final short APN_TYPE_INTERNET = 0;
	public static final short APN_TYPE_MMS = 1;
	public static final short APN_TYPE_ALL = 2;

	public static final short STATUS_APN_ADD_ERROR = -1;
	public static final short STATUS_APN_STATUS_UNKNOWN= 0;
	public static final short STATUS_APN_WORKING = 1;
	public static final short STATUS_APN_PRESENT = 2;
	public static final short STATUS_APN_ADDED = 3;
	
	public boolean needManualConfig = false;

	static final String[] apnNames = {"free", "mmsfree"};
	
	static final short BUILD_ICS = 14;
	
	public ApnCheck(Context c)
	{
		mContext = c;
	}

	public boolean deleteApn(short type)
	{
		final String COL_ID = "_id";
		final String COL_NAME = "name";
		final String COL_MCC = "mcc";
		final String COL_MNC = "mnc";
		final String COL_NUMERIC = "numeric";
		final String COL_APN = "apn";
		final String COL_TYPE = "type";
		final String COL_MMSC = "mmsc";
		final String COL_AUTHTYPE = "authtype";
		final String COL_CURRENT = "current";

		boolean result = false;
		Cursor cursor = null;
		Boolean freeApnFound = false;
		String APNToSearch = "non configuré";

		Uri contentUri = Uri.parse("content://telephony/carriers/");
		ContentResolver resolver = mContext.getContentResolver();

		switch (type)
		{
			case APN_TYPE_INTERNET :
			case APN_TYPE_ALL :
				APNToSearch = "free";
				break;
			case APN_TYPE_MMS :
				APNToSearch = "mmsfree";
				break;
			default :
				return result;
		}

		try
		{
			cursor = resolver.query(contentUri, new String[]{COL_ID, COL_NAME, COL_APN}, null, null, null);
			if (cursor != null)
			{
				try
				{
					if (cursor.moveToFirst())
					{
						while ((!cursor.isAfterLast()) && (freeApnFound == false))
						{
							String apn = cursor.getString(cursor.getColumnIndex(COL_APN));
							if ((apn != null) && (apn.compareToIgnoreCase(APNToSearch) == 0))
							{
								freeApnFound = true;
								int id, index;
								index = cursor.getColumnIndex(COL_ID);
						        id = cursor.getShort(index); // id of the row to update
								Log.d(TAG, "loop "+id);
								try
								{
									resolver.delete(contentUri, COL_ID+" = "+id, null);
								}
								catch (Exception e)
								{
									Log.e(TAG, "Delete APN : "+e.getMessage());
								}
								result = true;
							}
							cursor.moveToNext();
						}
					}
				}
				finally
				{
					if (cursor != null)
						cursor.close();
				}
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage());
		}
		return result;
	}

	/*
	 * From http://android.riteshsahu.com/tips/programmatically-accessing-apns-stored-on-the-phone
	 */
	public int checkApn(short type)
	{
		final String COL_ID = "_id";
		final String COL_NAME = "name";
		final String COL_MCC = "mcc";
		final String COL_MNC = "mnc";
		final String COL_NUMERIC = "numeric";
		final String COL_APN = "apn";
		final String COL_TYPE = "type";
		final String COL_MMSC = "mmsc";
		final String COL_AUTHTYPE = "authtype";
		final String COL_CURRENT = "current";
		boolean freeApnFound = false;
		boolean currentApnIsFree = false;
		int status = STATUS_APN_STATUS_UNKNOWN;

		Uri contentUri = Uri.parse("content://telephony/carriers/");

		SharedPreferences mgr = mContext.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE);
		int apnVersion = mgr.getInt(PREFS_APN_VERSION, -1);

		PackageInfo pinfo;
		try
		{
			pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			if (apnVersion < 82)
			{
				Log.d(TAG, "OS "+Build.VERSION.RELEASE+" Nouvelle version : ancienne = "+apnVersion+" - nouvelle = "+pinfo.versionCode);

				if (Build.VERSION.RELEASE.equals("1.5"))
				{
					deleteApn(APN_TYPE_INTERNET);
					deleteApn(APN_TYPE_MMS);
				}
				else
				{
					// Crash on 1.5
					if (Build.VERSION.SDK_INT < BUILD_ICS)
					{
						deleteApn(APN_TYPE_INTERNET);
						deleteApn(APN_TYPE_MMS);						
					}
				}
		    	Editor editor = mgr.edit();
				editor.putInt(PREFS_APN_VERSION, pinfo.versionCode);
		       	editor.commit();
			}
		}
		catch (NameNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ContentResolver resolver = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		Cursor cursor = null;

		if ((type == APN_TYPE_INTERNET) || (type == APN_TYPE_ALL))
		{
			// First we check if the selected APN is "free". If it is, we don't need to add it.
			cursor = mContext.getContentResolver().query(contentUri, new String[] {COL_APN}, "current=1", null, null); 
			if (cursor != null)
			{
				try
				{
					if (cursor.moveToFirst())
					{ 
						if (cursor.getString(0).equals("free"))
						{
							currentApnIsFree = true;
							status = STATUS_APN_WORKING;
	//						Toast.makeText(mContext, statusMsg, Toast.LENGTH_SHORT).show();
						}
					}
				}
				finally
				{
					if (cursor != null)
						cursor.close();
				}
			}
		}

		// If current apn is not "free", we need to check if it exists
		if (currentApnIsFree == false)
		{
			cursor = resolver.query(contentUri, new String[]{COL_NAME, COL_APN}, COL_APN+"='"+apnNames[type]+"'", null, null);
			if (cursor != null)
			{
				try
				{
					if (cursor.moveToFirst())
					{
						freeApnFound = true;
						status = STATUS_APN_PRESENT;
					}
				}
				finally
				{
					cursor.close();
				}
			}

			if (freeApnFound == false)
			{
				switch (type)
				{
					case APN_TYPE_INTERNET:
						values.put(COL_NAME, "Free");
						values.put(COL_MCC, 208);
						values.put(COL_MNC, 15);
						values.put(COL_APN, "free");
						values.put(COL_NUMERIC, 20815);
						values.put(COL_AUTHTYPE, -1);
						values.put(COL_CURRENT, 1);
						values.put(COL_TYPE, "default, supl");
						break;
					case APN_TYPE_MMS:
						values.put(COL_NAME, "Free MMS");
						values.put(COL_MCC, 208);
						values.put(COL_MNC, 15);
						values.put(COL_APN, "mmsfree");
						values.put(COL_NUMERIC, 20815);
						values.put(COL_AUTHTYPE, -1);
						values.put(COL_TYPE, "mms");
						values.put(COL_MMSC, "http://212.27.40.225");
//						values.put(COL_MMSC, "http://mms.free.fr");
						break;
					case APN_TYPE_ALL:
						values.put(COL_NAME, "Free");
						values.put(COL_MCC, 208);
						values.put(COL_MNC, 15);
						values.put(COL_APN, "free");
						values.put(COL_NUMERIC, 20815);
						values.put(COL_AUTHTYPE, -1);
						values.put(COL_CURRENT, 1);
						values.put(COL_MMSC, "http://mms.free.fr");
						values.put(COL_TYPE, "default, supl, mms");
						break;
				}
				try
				{
					resolver.insert(contentUri, values);
					status = STATUS_APN_ADDED;
				}
				catch (Exception e)
				{
					Log.e(TAG, e.getMessage());
					status = STATUS_APN_ADD_ERROR;
					if (! Build.VERSION.RELEASE.equals("1.5"))
						if (Build.VERSION.SDK_INT >= BUILD_ICS)
						{
							needManualConfig = true;
						}
				}
			}
		}
		return status;
	}
}
