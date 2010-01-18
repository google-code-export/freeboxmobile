package org.madprod.freeboxmobile.pvr;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  conteneur correspondant au JSON d'un disque
 * @author bduffez
 *
 * Exemple:
 * {
 * "free_size":3376040,
 * "total_size":41917188,
 * "label":"Disque dur",
 * "id":0,
 * "mount_point":"/Disque dur/Enregistrements"
 * }
 *
 */
public class Disque {
	private int mFreeSize;
	private int mTotalSize;
	private String mLabel;
	private int mId;
	private String mMountPt;
	
	public Disque(String json) {
		try {
			JSONObject o = new JSONObject(json);
			
			this.mFreeSize = o.getInt("free_size");
			this.mTotalSize = o.getInt("total_size");
			this.mLabel = o.getString("label");
			this.mId = o.getInt("id");
			this.mMountPt = o.getString("mount_point");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getMountPt() {
		return this.mMountPt;
	}
	
	public int getId() {
		return this.mId;
	}
}