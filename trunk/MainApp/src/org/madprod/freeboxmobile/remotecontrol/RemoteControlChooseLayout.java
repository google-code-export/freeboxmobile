package org.madprod.freeboxmobile.remotecontrol;

import java.io.File;  

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 *
 * @author Clement Beslon
 * $Id: RemoteControlActivity.java 2010-05-04 $
 * 
 */

public class RemoteControlChooseLayout extends Activity implements Constants, RemoteControlActivityConstants
{


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		FBMNetTask.register(this);
		setContentView(R.layout.remotecontrol_preferencelayout);
		setTitle(getString(R.string.app_name)+" - Gestion des skins");
		
		createView(this);
		
	}

	
	

	private void createView(final Context context) {
		ImageButton ib = (ImageButton)findViewById(R.id.mosaicHorizontalIB);
		
		File f = new File(getFilesDir()+PATHMOSAICHORIZONTAL+"/skin.png");
		if (f.exists()){
			ib.setImageDrawable(Drawable.createFromPath(f.getAbsolutePath()));
		}
		ib.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(context, RemoteControlListLayout.class);
				i.putExtra("type", "mosaic");
				i.putExtra("sens", "horizontal");
				startActivityForResult(i, 0);
			}
		});
		
		ib = (ImageButton)findViewById(R.id.mosaicVerticalIB);
		f = new File(getFilesDir()+PATHMOSAICVERTICAL+"/skin.png");
		if (f.exists()){
			ib.setImageDrawable(Drawable.createFromPath(f.getAbsolutePath()));
		}
		ib.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(context, RemoteControlListLayout.class);
				i.putExtra("type", "mosaic");
				i.putExtra("sens", "vertical");
				startActivityForResult(i, 0);
			}
		});
		
		ib = (ImageButton)findViewById(R.id.remoteHorizontalIB);
		f = new File(getFilesDir()+PATHREMOTEHORIZONTAL+"/skin.png");
		if (f.exists()){
			ib.setImageDrawable(Drawable.createFromPath(f.getAbsolutePath()));
		}
		ib.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(context, RemoteControlListLayout.class);
				i.putExtra("type", "remote");
				i.putExtra("sens", "horizontal");
				startActivityForResult(i, 0);
			}
		});
		
		ib = (ImageButton)findViewById(R.id.remoteVerticalIB);
		f = new File(getFilesDir()+PATHREMOTEVERTICAL+"/skin.png");
		if (f.exists()){
			ib.setImageDrawable(Drawable.createFromPath(f.getAbsolutePath()));
		}
		ib.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(context, RemoteControlListLayout.class);
				i.putExtra("type", "remote");
				i.putExtra("sens", "vertical");
				startActivityForResult(i, 0);
			}
		});
		
	}




	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (resultCode) {
		case SAVE:
			break;

		}
		
		
		super.onActivityResult(requestCode, resultCode, data);
		

	}
	
	@Override
	public void onDestroy()
	{
		FBMNetTask.unregister(this);
		super.onDestroy();
	}

}