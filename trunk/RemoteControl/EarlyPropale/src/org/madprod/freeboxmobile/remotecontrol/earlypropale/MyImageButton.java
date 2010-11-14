package org.madprod.freeboxmobile.remotecontrol.earlypropale;


import android.content.Context;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MyImageButton extends ImageButton implements Constants{


	OnClickListener onclick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (Main.mRemoteControl != null){
				Log.d(LOGNAME, "appui court sur : "+getTag());
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Main.mRemoteControl.sendCommand(getTag().toString(), false, 0);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				});
				t.start();
			}
		}
	};

	OnLongClickListener onLongClick = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			if (Main.mRemoteControl != null){
				try {
					Log.d(LOGNAME, "appui long sur : "+getTag());
					Main.mRemoteControl.sendCommand(getTag().toString(), true, 0);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			return true;
		}
	};

	public MyImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setOnClickListener(onclick);
		setOnLongClickListener(onLongClick);
	}

	public MyImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnClickListener(onclick);
		setOnLongClickListener(onLongClick);
	}

	public MyImageButton(Context context) {
		super(context);
		setOnClickListener(onclick);
		setOnLongClickListener(onLongClick);	
	}



}
