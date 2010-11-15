package org.madprod.freeboxmobile.remotecontrol.earlypropale;



import java.util.List; 

import org.madprod.freeboxmobile.services.IRemoteControl;
import org.madprod.freeboxmobile.services.IRemoteControlServiceCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Main extends Activity implements Constants{
	
	public static IRemoteControl mRemoteControl = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        verifyInstallFbm();
    }
    
    @Override
    protected void onStart() {
    	
    	final ViewFlipper line1 = (ViewFlipper)findViewById(R.id.ViewFlipperLine1);
    	if (line1 != null){
        	line1.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        	line1.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
    	}
    	
    	final ViewFlipper line2 = (ViewFlipper)findViewById(R.id.ViewFlipperLine2);
    	if (line2 != null){
    		line2.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    		line2.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
    	}
    	
    	final ViewFlipper line3 = (ViewFlipper)findViewById(R.id.ViewFlipperLine3);
    	if (line3 != null){    	
    		line3.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    		line3.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
    	}

    	final ViewFlipper line4 = (ViewFlipper)findViewById(R.id.ViewFlipperLine4);
    	if (line4 != null){    	
    		line4.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    		line4.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
    	}
    	
    	final ViewFlipper line5 = (ViewFlipper)findViewById(R.id.ViewFlipperLine5);
    	if (line5 != null){    	
        	line5.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        	line5.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
    	}
    	
    	OnClickListener screenRightListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (line1 != null) line1.showNext();
				if (line2 != null) line2.showNext();
				if (line3 != null) line3.showNext();
				if (line4 != null) line4.showNext();
				if (line5 != null) line5.showNext();
			}
		};
		
		final ImageButton screenRight = (ImageButton)findViewById(R.id.screen_right);
		if (screenRight != null) screenRight.setOnClickListener(screenRightListener);
		final ImageButton screenRight2 = (ImageButton)findViewById(R.id.screen_right2);
		if (screenRight2 != null) screenRight2.setOnClickListener(screenRightListener);
    	

    	
    	OnClickListener screenLeftListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (line1 != null) line1.showPrevious();
				if (line2 != null) line2.showPrevious();
				if (line3 != null) line3.showPrevious();
				if (line4 != null) line4.showPrevious();
				if (line5 != null) line5.showPrevious();
			}
		};
    	
    	final ImageButton screenLeft = (ImageButton)findViewById(R.id.screen_left);
    	if (screenLeft != null) screenLeft.setOnClickListener(screenLeftListener);
    	
    	final ImageButton screenLeft2 = (ImageButton)findViewById(R.id.screen_left2);
    	if (screenLeft2 != null) screenLeft2.setOnClickListener(screenLeftListener);
    	try{
        if (!bindService(new Intent("org.madprod.freeboxmobile.services.RemoteControlService"), mRemoteControlConnection, Context.BIND_AUTO_CREATE)){
        		showPopupFbm();
        }
    	}catch(SecurityException e){
    		showPopupSecurity();
    		e.printStackTrace();
    	}
        super.onStart();
    }
    
    
    private ServiceConnection mRemoteControlConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e("REMOTE", "Deconnecte");
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.e("REMOTE", "Connecte");
			mRemoteControl = IRemoteControl.Stub.asInterface(service);
			try { 
				 mRemoteControl.registerCallback(callback); 
			}catch (RemoteException e) {
				e.printStackTrace();
			} 
		}
		
	};
    
    
	final IRemoteControlServiceCallback callback = new IRemoteControlServiceCallback.Stub() {

		@Override
		public void dataChanged(int status, String message)
				throws RemoteException {
			Log.d(LOGNAME, message);
			if (status == 1){
				  mHandler.sendMessage(mHandler.obtainMessage(0,"Erreur "+message));
			}
			
		} 
		
	}; 
	
	

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
            	case 0:
                	Toast.makeText(getApplicationContext(), "message = "+msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };
    
    
    protected void onDestroy() {
    	
    	   if (mRemoteControlConnection != null) {
    	        unbindService(mRemoteControlConnection);
    	        mRemoteControlConnection = null;
    	   }

    	super.onDestroy();
    };
    
    private void verifyInstallFbm(){
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.home.HomeListActivity");
		List<ResolveInfo> activitiesList = getPackageManager().queryIntentActivities(i, 0);
		if (activitiesList.isEmpty()){
			showPopupFbm();
		}

    }
    private void showPopupFbm(){
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setCancelable(false);
		d.setTitle(getString(R.string.app_name));
//		d.setIcon(R.drawable.fm_actus_freenautes);
		d.setMessage(
				"Pour utiliser cette fonctionnalité, vous devez installer la derniere version de Freebox Mobile'.\n\n"+
				"Cliquez sur 'Continuer' pour l'installer ou sur 'Annuler' pour quitter Early Propale"
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Continuer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
	            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.madprod.freeboxmobile" ));
	            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            try
	            {
	            	startActivity(marketIntent);
					finish();
	            }
	            catch (ActivityNotFoundException e)
	            {
	    	    	AlertDialog ad = new AlertDialog.Builder(Main.this).create();
	    			ad.setTitle(getString(R.string.app_name));
//	    			ad.setIcon(R.drawable.fm_actus_freenautes);
	    	    	ad.setMessage("Impossible d'ouvrir Android Market !");
	    			ad.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
	    	    	ad.show();
	            }
			}
		});
	d.setButton(DialogInterface.BUTTON_NEGATIVE, "Quitter", new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			dialog.dismiss();
			finish();
		}
	});
	d.show();
    	
    }    
    
    
    private void showPopupSecurity(){
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setCancelable(false);
		d.setTitle(getString(R.string.app_name));
//		d.setIcon(R.drawable.fm_actus_freenautes);
		d.setMessage(
				"Probleme de permission.\n\n"+
				"Veuillez réinstaller le module\n"+
				"Cliquez sur 'Continuer' pour l'installer ou sur 'Annuler' pour quitter Early Propale"
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Continuer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
	            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.madprod.freeboxmobile.remotecontrol.earlypropale" ));
	            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            try
	            {
	            	startActivity(marketIntent);
					finish();
	            }
	            catch (ActivityNotFoundException e)
	            {
	    	    	AlertDialog ad = new AlertDialog.Builder(Main.this).create();
	    			ad.setTitle(getString(R.string.app_name));
//	    			ad.setIcon(R.drawable.fm_actus_freenautes);
	    	    	ad.setMessage("Impossible d'ouvrir Android Market !");
	    			ad.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
	    	    	ad.show();
	            }
			}
		});
	d.setButton(DialogInterface.BUTTON_NEGATIVE, "Quitter", new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			dialog.dismiss();
			finish();
		}
	});
	d.show();
    	
    }    
}