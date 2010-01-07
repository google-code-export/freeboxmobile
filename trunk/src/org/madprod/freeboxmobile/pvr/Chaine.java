package org.madprod.freeboxmobile.pvr;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  conteneur correspondant au JSON d'une chaine
 * @author bduffez
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
 * 		{"pvr_mode":"private","desc":"bas d�bit","id":346}
 *  ]
 * }
 */

public class Chaine {
	private static class Service {
		enum PVR_MODE { DISABLED, PUBLIC, PRIVATE };
		private PVR_MODE mPvrMode;
		private String mServiceDesc;
		private int mServiceId;
		
		public Service(String json) {
			try {
				JSONObject o = new JSONObject(json);

				String mode = o.getString("pvr_mode");
				if (mode.equals("private")) {
					this.mPvrMode = Service.PVR_MODE.PRIVATE;
				} else if (mode.equals("public")) {

					this.mPvrMode = Service.PVR_MODE.PUBLIC;
				}
				else {
					this.mPvrMode = Service.PVR_MODE.DISABLED;
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
	}
	
	private String mName;
	private int mId;
	private List<Service> mServices;
	
	public Chaine(String json) {
		try {
			JSONObject o = new JSONObject(json);
			
			this.mName = o.getString("name");
			this.mId = o.getInt("id");
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