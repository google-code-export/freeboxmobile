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
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Main extends Activity implements Constants{

	public static IRemoteControl mRemoteControl = null;
	public static int box = -1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		verifyInstallFbm();
		
	}
	
	@Override
	protected void onResume() {
		if (mRemoteControl != null){
			isParametersCorrect();	
		}
		super.onResume();
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

			if(!isParametersCorrect()){
				Log.d(LOGNAME, "parameters are not correct");


			}else{
				if (box == -1){
					choiceBox();
				}
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.add("Choisir le boitier");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		choiceBox();
		return super.onOptionsItemSelected(item);
	}

	private void choiceBox(){

		final CharSequence[] items = {"Boitier 1", "Boitier 2"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Indiquer le boitier");
		builder.setSingleChoiceItems(items, box, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int item) {    	    	
				try {
					if (mRemoteControl.isBoxActivated(item+1)){
						box = item;
						dialog.cancel();
					}else{
						final AlertDialog.Builder builderProblem = new AlertDialog.Builder(Main.this);
						builderProblem.setTitle("Boitier non configuré");
						builderProblem.setMessage("Allez dans la configuration de Freebox Mobile pour activer le code et activer le boitier");
						builderProblem.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
						AlertDialog alert = builderProblem.create();
						alert.show();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}
		});
		final AlertDialog alert = builder.create();
		alert.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				if (box != -1){
					dialog.cancel();
				}else{
					final AlertDialog.Builder builderProblem = new AlertDialog.Builder(Main.this);
					builderProblem.setTitle("Aucun boitier choisi");
					builderProblem.setMessage("Aucun boitier choisi. La touche OK fermera l'application");
					builderProblem.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							finish();
						}
					});
					builderProblem.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							alert.show();
						}
					});
					AlertDialog alert = builderProblem.create();
					alert.setCancelable(false);
					alert.show();

				}
			}
		});
		alert.show();
		//		try {
		//			if (mRemoteControl != null)
		//			Toast.makeText(Main.this, "box 0 validated = "+mRemoteControl.isBoxActivated(0), Toast.LENGTH_SHORT).show();
		//		} catch (RemoteException e) {
		//			e.printStackTrace();
		//		}
	}
	
	
    private void openConfig()
    {
		Intent i = new Intent();
		i.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.Config");
		try
		{
			startActivity(i);
		}
		catch (ActivityNotFoundException e)
		{
	    	AlertDialog d = new AlertDialog.Builder(this).create();
			d.setTitle(getString(R.string.app_name));
	    	d.setMessage(
				"Probleme pour accèder à la configuration de Freebox Mobile.\n\n"+
				"Veuillez mettre à jour votre version de Freebox Mobile.\n"
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
    }
    private boolean isParametersCorrect(){
    	
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		boolean wifiEnabled = false;
		int nbCodes = 0;

		if (wifi.isWifiEnabled()){
			WifiInfo infos = wifi.getConnectionInfo();
			if(infos.getSSID()!=null){
				wifiEnabled = true;
				Log.d(LOGNAME, "wifi enabled");
			}else{
				Log.d(LOGNAME, "wifi not enabled");					
			}
		}

		Boolean boitier1_state = false;
		Boolean boitier2_state = false;
		try{
			boitier1_state = mRemoteControl.isBoxActivated(1);
			boitier2_state = mRemoteControl.isBoxActivated(2);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (boitier1_state){
			nbCodes ++;
			Log.d(LOGNAME, "boitier 1 enabled");					
		}else{
			Log.d(LOGNAME, "boitier 1 not enabled");									
		}
		if (boitier2_state){
			Log.d(LOGNAME, "boitier 2 enabled");					
			nbCodes ++;
		}else{
			Log.d(LOGNAME, "boitier 2 not enabled");													
		}
		if (!wifiEnabled || nbCodes==0){

			final AlertDialog d = new AlertDialog.Builder(Main.this).create();
			d.setTitle(getString(R.string.app_name)+" - Télécommande");
			d.setIcon(R.drawable.fm_telecommande);
			d.setMessage(
					"Avant d'utiliser la télécommande, vous devez paramètrer les éléments suivants :\n\n"+
					((!wifiEnabled)?"- Connectez vous en wifi a votre Freebox\n":"")+
					((nbCodes == 0)?"- Paramètrer le(s) code(s) du(des) boitier(s)\n":"")+
					"\nQue souhaitez vous faire ?"+
					"\n\n"
			);


			if (!wifiEnabled){
				d.setButton(DialogInterface.BUTTON_POSITIVE, "Configurer Wifi", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
						dialog.dismiss();
					}
				});
			}
			if (nbCodes == 0){
				d.setButton(DialogInterface.BUTTON_NEUTRAL, "Configurer Télécommande", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						openConfig();
					}
				}
				);
			}
			
			d.setButton(DialogInterface.BUTTON_NEGATIVE, "Retour à l'accueil", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					finish();
					dialog.dismiss();
				}
			}
			);
			d.setCancelable(false);
			d.show();
			return false;
		}else{
			return true;
		}
    }
	
}