package org.madprod.freeboxmobile.pvr;

import org.json.JSONException;
import org.json.JSONObject;

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
	boolean mNoMedia;
	boolean mDirty;
	boolean mReadOnly;
	boolean mBusy;
	
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
	
	private boolean getBoolean(JSONObject o, String key) {
		if (o.has(key)) {
			try {
				return o.getBoolean(key);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public String getMountPt() {
		return this.mMountPt;
	}
	
	public String getLabel() {
		return this.mLabel;
	}
	
	public boolean isOk() {
		return mDirty == false && mBusy == false && mReadOnly == false && mNoMedia == false;
	}
	
	public int getId() {
		return this.mId;
	}
}