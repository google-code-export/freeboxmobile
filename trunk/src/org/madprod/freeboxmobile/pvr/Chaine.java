package org.madprod.freeboxmobile.pvr;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.madprod.freeboxmobile.FBMHttpConnection;

import android.database.Cursor;

/**
 *  conteneur correspondant au JSON d'une chaine
 * @author bduffez
 * $Id$
 *
 * Exemple:
 * {
 * "name":"TF1",
 * "id":1,
 * "service":
 * 	[
 * 		{"pvr_mode":"private","desc":"","id":0},
 * 		{"pvr_mode":"private","desc":"HD","id":487},
 * 		{"pvr_mode":"private","desc":"standard","id":333},
 * 		{"pvr_mode":"private","desc":"bas débit","id":346}
 *  ]
 * }
 */

public class Chaine implements PvrConstants {
	static class Service {
		private int mPvrMode;
		private String mServiceDesc;
		private int mServiceId;
		
		public Service(Cursor c)
		{
    		this.mPvrMode = c.getInt(c.getColumnIndex(ChainesDbAdapter.KEY_PVR_MODE));
    		this.mServiceDesc = c.getString(c.getColumnIndex(ChainesDbAdapter.KEY_SERVICE_DESC));
    		this.mServiceId = c.getInt(c.getColumnIndex(ChainesDbAdapter.KEY_SERVICE_ID));
		}
		
		public Service(String json) {
			try {
				JSONObject o = new JSONObject(json);

				String mode = o.getString("pvr_mode");
				if (mode.equals("private")) {
					this.mPvrMode = PVR_MODE_PRIVATE;
				} else if (mode.equals("public")) {
					this.mPvrMode = PVR_MODE_PUBLIC;
				}
				else {
					this.mPvrMode = PVR_MODE_DISABLED;
				}

				this.mServiceDesc = o.getString("desc");
				this.mServiceId = o.getInt("id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public int getServiceId() {
			return this.mServiceId;
		}
		public int getPvrMode() {
			return this.mPvrMode;
		}
		public String getDesc() {
			return this.mServiceDesc;
		}
	}
	
	private String mName;
	private int mId;
	private int mBoitierId;
	private List<Service> mServices;
	
	public Chaine(String json, int boitier_id) {
		try {
			JSONObject o = new JSONObject(json);
			
			this.mName = o.getString("name");
			this.mId = o.getInt("id");
			this.mBoitierId = boitier_id;
			this.mServices = new ArrayList<Service>();
			
			String servicesJson = o.getString("service");
			String serviceJson;
			int debut, fin;
			do {
				debut = servicesJson.indexOf("{");
				fin = servicesJson.indexOf("}") + 1;
				
				if (debut < 0 || fin <= 0 || fin > servicesJson.length()) {
					break;
				}

				serviceJson = servicesJson.substring(debut, fin);
				this.mServices.add(new Service(serviceJson));
				
				servicesJson = servicesJson.substring(fin+1);
			} while (true);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void storeDb(ChainesDbAdapter db)
	{
		if (db.createChaine(this.mName, this.mId, this.mBoitierId) == -1)
			FBMHttpConnection.FBMLog("CHAINE STOREDB : Chaine non insérée "+this.mName);
		int size = mServices.size();
		
		for (int i = 0; i < size; i++) {
			if (db.createService(this.mId, this.mBoitierId, mServices.get(i).getDesc(), mServices.get(i).getServiceId(), mServices.get(i).getPvrMode()) == -1)
				FBMHttpConnection.FBMLog("CHAINE STOREDB :Service non inséré "+this.mName);
		}
	}

	public String getName() {
		return this.mName;
	}
	
	public int getChaineId() {
		return this.mId;
	}
	
	public List<Service> getServices() {
		return this.mServices;
	}
	
	public Service getService(int serviceId) {
		int size = mServices.size();
		
		for (int i = 0; i < size; i++) {
			if (mServices.get(i).getServiceId() == serviceId) {
				return mServices.get(i);
			}
		}
		
		return null;
	}
}