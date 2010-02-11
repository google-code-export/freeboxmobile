package org.madprod.freeboxmobile.pvr;

import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.FBMHttpConnection;

/**
 *  conteneur correspondant au JSON d'un disque
 * @author bduffez
 *
 * Exemple:
 * [
 * {
 * "free_size":38375124,
 * "total_size":38392640,
 * "label":"Disque dur",
 * "id":0,
 * "mount_point":"/Disque dur/Enregistrements"
 * },
 * {
 * "nomedia":false,
 * "dirty":false,
 * "total_size":3656712,
 * "readonly":false,
 * "free_size":3656088,
 * "label":"KEVIN",
 * "id":2,
 * "busy":false,
 * "mount_point":"/KEVIN"
 * }
 * ];
 *
 */
public class Disque {
	private int mFreeSize;
	private int mTotalSize;
	private String mLabel;
	private int mId;
	private String mMountPt;
	int mNoMedia;
	int mDirty;
	int mReadOnly;
	int mBusy;
	
	public Disque(String json) {
		try {
			JSONObject o = new JSONObject(json);
			
			this.mFreeSize = o.getInt("free_size");
			this.mTotalSize = o.getInt("total_size");
			this.mLabel = o.getString("label");
			this.mId = o.getInt("id");
			this.mMountPt = o.getString("mount_point");
			
			this.mNoMedia = getBoolean(o, "nomedia");
			this.mDirty = getBoolean(o, "dirty");
			this.mBusy = getBoolean(o, "busy");
			this.mReadOnly = getBoolean(o, "readonly");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void storeDb(ChainesDbAdapter db, String bName, int bNumber)
	{
		if (db.createBoitierDisque(bName, bNumber, mFreeSize, mTotalSize,
				mId, mNoMedia, mDirty, mReadOnly, mBusy, mMountPt, mLabel) == -1)
			FBMHttpConnection.FBMLog("DISQUE STOREDB : Boitier disque non inséré "+this.mLabel);
	}
	
	private int getBoolean(JSONObject o, String key) {
		if (o.has(key)) {
			try {
				return (o.getBoolean(key)?1:0);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public String getMountPt() {
		return this.mMountPt;
	}
	
	public String getLabel() {
		return this.mLabel;
	}
	
	public boolean isOk() {
		return mDirty == 0 && mBusy == 0 && mReadOnly == 0 && mNoMedia == 0;
	}

	public int getGigaFree() {
		Float f = new Float(mFreeSize);
		f /= 1024;
		f /= 1024;
		return f.intValue();
	}
	public int getGigaTotal() {
		Float f = new Float(mTotalSize);
		f /= 1024;
		f /= 1024;
		return f.intValue();
	}
	
	public int getId() {
		return this.mId;
	}
}
