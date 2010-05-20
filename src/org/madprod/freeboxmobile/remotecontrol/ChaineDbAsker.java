package org.madprod.freeboxmobile.remotecontrol;

import java.util.HashMap;
import java.util.LinkedList;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.pvr.ChainesDbAdapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

public class ChaineDbAsker implements Constants{

	private final Context context;
	private final ChainesDbAdapter mDbHelper;

	public ChaineDbAsker(Context c) {
		context = c;
		mDbHelper = new ChainesDbAdapter(context);
	}
	
	
	@SuppressWarnings("serial")
	public HashMap<String, LinkedList<String>> getAllChainesInfo(){
		mDbHelper.open();

		
		
		Cursor allChaines = mDbHelper.getListChaines();
		final LinkedList<String> chainesImgs = new LinkedList<String>();
		final LinkedList<String> chainesIds = new LinkedList<String>();
		final LinkedList<String> chainesCanal = new LinkedList<String>();
		final LinkedList<String> chainesName = new LinkedList<String>();
		final int CI_image = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_IMAGE);
		final int CI_canal = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_CANAL);
		final int CI_id = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_ID);
		final int CI_name = allChaines.getColumnIndexOrThrow(ChainesDbAdapter.KEY_GUIDECHAINE_NAME);

		
		while (allChaines.moveToNext()){    	
			String image = allChaines.getString(CI_image);
			String canal = allChaines.getString(CI_canal);
			String id = allChaines.getString(CI_id);
			String name = allChaines.getString(CI_name);

			if (image.length() > 0){
				String filepath = Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+image;
				chainesImgs.addLast(filepath);
				chainesIds.addLast(id);
				chainesCanal.addLast(canal);
				chainesName.addLast(name);
			}
		}

		mDbHelper.close();
		return new HashMap<String, LinkedList<String>>(){
			{
				put("imgs", chainesImgs);
				put("ids", chainesIds);
				put("names", chainesName);
				put("channels", chainesCanal);
			}
		};
	}
}
