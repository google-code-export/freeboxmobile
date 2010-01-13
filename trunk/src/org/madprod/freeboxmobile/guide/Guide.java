package org.madprod.freeboxmobile.guide;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Conteneur correspondant au JSON du guide TV
 * @author bduffez
 *
{
	"progs":
	{
		"321":
		{
			"2010-01-13 18:40:00":
			{
				"genre_id":"19",
				"channel_id":"321",
				"resum_s":"Sport (Football)\n8mes de finale de la Coupe de la Ligue",
				"datetime_fin":"2010-01-13 20:35:00",
				"resum_l":" - Tous publics\nSport (Football)",
				"title":"Football",
				"duree":"115",
				"datetime":"2010-01-13 18:40:00"
			},
			...
		},
		...
	},
	"chaines":
	{
		"127":
		{
			"fbx_id":"249",
			"name":"CHASSE & PECHE",
			"id":"127",
			"canal":"161",
			"image":"chasseetpeche.png"
		},
		...
	},
	"date":"2010-01-13 19:00:00"
}	
 *
 */
public class Guide {
	Progs mProgs = null;
	Chaines mChaines = null;
	String mDate = null;
	
	/**
	 * Constructeur complet
	 * @param json		le JSON
	 * @param progs		doit on récupérer la liste des programmes?
	 * @param chaines		"		"		"		   chaines?
	 * @param date			"		"		 date?
	 */
	public Guide(String json, boolean progs, boolean chaines, boolean date) {
		try {
			JSONObject o = new JSONObject(json);
			
			if (progs) {
				mProgs = new Progs(o.getString("progs"));
			}
			
			if (chaines) {
				mChaines = new Chaines(o.getString("chaines"));
			}
			
			if (date) {
				mDate = o.getString("date");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Chaines.Chaines_Chaine getChaine(int id) {
		return mChaines.getChaine(id);
	}
	
	/**
	 * Conteneur pour la liste des programmes disponible à telle heure
	 * @author bduffez
	 *
	 */
	class Progs {
		List<Progs_Chaine> chaines;
		
		Progs(String json) {
			//TODO
		}
		
		/**
		 * Conteneur pour la liste des émissions d'une chaine
		 * @author bduffez
		 *
		 */
		class Progs_Chaine {
			List<Progs_Chaine_Emission> emissions;
			
			/**
			 * Conteneur pour une émission donnée
			 * @author bduffez
			 *
			 */
			class Progs_Chaine_Emission {
				int genreId;
				int channelId;
				String resum_s;
				String resum_l;
				String title;
				int duree;
				String dateTime;
				String dateTimeFin;
			}
		}
	}
	
	/**
	 * Conteneur pour la liste des chaines
	 * @author bduffez
	 *
	 */
	public class Chaines {
		List<Chaines_Chaine> mChaines;
		
		public Chaines(String json) {
			JSONObject o;
			try {
				o = new JSONObject(json);
				int numChaines = o.length();
				JSONArray chaines = o.names();
				mChaines = new ArrayList<Chaines_Chaine>();
				
				for (int i = 0; i < numChaines; i++) {
					mChaines.add(new Chaines_Chaine(o.getString(chaines.getString(i))));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public Chaines_Chaine getChaine(int canal) {
			int nbChaines = mChaines.size();
			
			for (int i = 0; i < nbChaines; i++) {
				Log.d("prout", "la chaine "+i+" ou "+mChaines.get(i).getImage()+" ou "+mChaines.get(i).getCanal()+" n'est pas "+canal);
				if (mChaines.get(i).getCanal() == canal) {
					return mChaines.get(i);
				}
			}
			
			return null;
		}
		
		public class Chaines_Chaine {
			int fbxId;
			int id;
			int canal;
			String name;
			String image;
			
			Chaines_Chaine(String json) {
				try {
					JSONObject o = new JSONObject(json);
					fbxId = o.getInt("fbx_id");
					id = o.getInt("id");
					canal = o.getInt("canal");
					name = o.getString("name");
					image = o.getString("image");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			public int getCanal() {		return canal; }
			public String getImage() {	return image; }
		}
	}
}
