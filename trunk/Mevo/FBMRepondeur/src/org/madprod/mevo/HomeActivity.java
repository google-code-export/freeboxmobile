package org.madprod.mevo;


import java.util.ArrayList; 
import java.util.Arrays;
import java.util.List;

import org.madprod.freeboxmobile.services.IMevo;
import org.madprod.freeboxmobile.services.IRemoteControlServiceCallback;
import org.madprod.freeboxmobile.services.MevoMessage;
import org.madprod.mevo.quickactions.ActionItem;
import org.madprod.mevo.quickactions.QuickAction;
import org.madprod.mevo.services.MevoSync;
import org.madprod.mevo.tools.DetachableResultReceiver;
import org.madprod.mevo.tools.Utils;
import org.madprod.mevo.tracker.TrackerConstants;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class HomeActivity extends ListActivity implements TrackerConstants , DetachableResultReceiver.Receiver{
	private GoogleAnalyticsTracker tracker;
	private StateMevoRefresh mState;
	private List<MevoMessage> listMessages = new ArrayList<MevoMessage>();




	public static IMevo mMevo = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);



		mState = (StateMevoRefresh) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;

		if (previousState) {
			// Start listening for SyncService updates again
			StateMevoRefresh.mReceiver.setReceiver(this);
			updateRefreshStatus();

		} else {



			Intent i = new Intent(Intent.ACTION_MAIN);
			i.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.home.HomeListActivity");
			List<ResolveInfo> activitiesList = getPackageManager().queryIntentActivities(i, 0);
			if (activitiesList.isEmpty()){
				AlertDialog d = new AlertDialog.Builder(this).create();
				d.setCancelable(false);
				d.setTitle(getString(R.string.app_name));
				d.setIcon(R.drawable.fm_repondeur);
				d.setMessage(
						"Pour utiliser cette fonctionnalitée, vous devez installer Freebox Mobile'.\n\n"+
						"Cliquez sur 'Continuer' pour l'installer ou sur 'Annuler' pour quitter Actu Freenautes"

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
							AlertDialog ad = new AlertDialog.Builder(HomeActivity.this).create();
							ad.setTitle(getString(R.string.app_name));
							ad.setIcon(R.drawable.fm_repondeur);
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
			}else{






				mState = new StateMevoRefresh();
				StateMevoRefresh.mReceiver.setReceiver(this);
				updateRefreshStatus();


				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				if (prefs.getBoolean("FIRSTEXEC", true)){
					if (!StateMevoRefresh.mSyncing)
						onRefreshClick(null);
					Editor editor = prefs.edit();
					editor.putBoolean("FIRSTEXEC", false);
					editor.commit();
				}
			}
		}



		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(TrackerConstants.ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView(TrackerConstants.HOME);




		final ListView lv = getListView();
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v, final int
					pos, long id) {
				final MevoMessage message = (MevoMessage)((myAdapter)lv.getAdapter()).getItem(pos);



				final QuickAction qa = new QuickAction(v);

				final ActionItem callback= new ActionItem();
				callback.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_call));				
				callback.setTitle("Rappeler");
				callback.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Utils.callback(HomeActivity.this, message);
						tracker.trackPageView("Mevo/Callback");
						qa.dismiss();
					}
				});					
				qa.addActionItem(callback);


				final ActionItem sendSMS= new ActionItem();
				sendSMS.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_send));				
				sendSMS.setTitle("SMS");
				sendSMS.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Utils.sendSms(HomeActivity.this, message);
						tracker.trackPageView("Mevo/SendSms");
						qa.dismiss();
					}
				});					
				qa.addActionItem(sendSMS);

				final ActionItem delete= new ActionItem();
				delete.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));				
				delete.setTitle("Supprimer");
				delete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Utils.removeMessage(HomeActivity.this, message);
						tracker.trackPageView("Mevo/RemoveMessage");
						qa.dismiss();
					}
				});					
				qa.addActionItem(delete);


				qa.setAnimStyle(QuickAction.ANIM_AUTO);

				qa.show();
				return true;
			}


		});   




		findViewById(R.id.btn_title_refresh).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onRefreshClick(v);}});
		findViewById(R.id.btn_title_settings).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onSettings(v);}});
		dataBind();
	}

	@Override
	protected void onStart() {
		super.onStart();

		try{
			if (!bindService(new Intent("org.madprod.freeboxmobile.services.MevoService"), mMevoConnection, Context.BIND_AUTO_CREATE)){
			}
		}catch(SecurityException e){
			e.printStackTrace();
		}

	}




	private void dataBind(){
		if (mMevo != null){
			try{
				listMessages = Arrays.asList((MevoMessage[])mMevo.getListOfMessages());
			}catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		setListAdapter(new myAdapter(HomeActivity.this, listMessages));
	}

	private ServiceConnection mMevoConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e("REMOTE", "Deconnecte");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.e("REMOTE", "Connecte");
			mMevo = IMevo.Stub.asInterface(service);
			try { 
				mMevo.registerCallback(callback); 
				dataBind();
			}catch (RemoteException e) {
				e.printStackTrace();
			} 
		}

	};


	final IRemoteControlServiceCallback callback = new IRemoteControlServiceCallback.Stub() {

		@Override
		public void dataChanged(int status, String message)
		throws RemoteException {
			if (status == 1){
				mHandler.sendMessage(mHandler.obtainMessage(0,"Erreur "+message));
			}

		} 

	}; 



	private Handler mHandler = new Handler() {
		@Override public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				break;
			default:
				super.handleMessage(msg);
			}
		}

	};


	@Override
	protected void onDestroy() {
		if (mMevoConnection != null) {
			unbindService(mMevoConnection);
			mMevoConnection = null;
		}
		super.onDestroy();

	};


	@Override
	protected void onPause() {
		StateMevoRefresh.mReceiver.unsetReceiver(this);
		super.onPause();
	}


	@Override
	protected void onResume() {
		StateMevoRefresh.mReceiver.setReceiver(this);
		updateRefreshStatus();
		dataBind();
		super.onResume();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		StateMevoRefresh.mReceiver.unsetReceiver(this);
		return mState;
	}



	/** Handle "refresh" title-bar action. */
	public void onRefreshClick(View v) {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, MevoSync.class);
		intent.putExtra(MevoSync.EXTRA_STATUS_RECEIVER, StateMevoRefresh.mReceiver);
		startService(intent);
	}



	@Override
	public void onListItemClick(ListView l, View v, int pos, long id)
	{
		super.onListItemClick(l, v, pos, id);
		MevoMessage message = (MevoMessage)((myAdapter)l.getAdapter()).getItem(pos);

		Intent i = new Intent(this, PlayerActivity.class);
		i.putExtra("message", message);
		startActivity(i);

		try{
			mMevo.setMessageRead(message);
		}catch (RemoteException e) {
			e.printStackTrace();
		} 
		//		MediaPlayer mp = new MediaPlayer();
		//		try {
		//			mp.setDataSource(message.getFileName());
		//			mp.prepare();
		//		} catch (IllegalArgumentException e) {
		//			e.printStackTrace();
		//		} catch (IllegalStateException e) {
		//			e.printStackTrace();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		//		mp.start();


	}



	private void updateRefreshStatus() {
		View refresh = findViewById(R.id.btn_title_refresh);
		if (refresh != null){
			refresh.setVisibility(
					StateMevoRefresh.mSyncing ? View.GONE : View.VISIBLE);
		}
		View title_refresh_progress = findViewById(R.id.title_refresh_progress);
		if (title_refresh_progress != null){
			title_refresh_progress.setVisibility(StateMevoRefresh.mSyncing ? View.VISIBLE : View.GONE);
		}
	}


	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case MevoSync.STATUS_RUNNING: {
			StateMevoRefresh.mSyncing = true;
			updateRefreshStatus();
			break;
		}
		case MevoSync.STATUS_FINISHED: {
			StateMevoRefresh.mSyncing = false;
			updateRefreshStatus();
			break;
		}
		case MevoSync.STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			StateMevoRefresh.mSyncing = false;
			updateRefreshStatus();
			//			final String errorText = resultData.getString(Intent.EXTRA_TEXT);
			Toast.makeText(HomeActivity.this, "Probleme de réception des messages", Toast.LENGTH_LONG).show();
			break;
		}
		}
	}



	/** Handle "search" title-bar action. */
	public void onSettings(View v) {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	/** Handle "FBM" title-bar action. */
	public void onFBMClick(View v) {
		Utils.goFBM(this);
	}

}



