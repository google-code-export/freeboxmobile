package org.madprod.mevo;


import java.util.List;  

import org.madprod.freeboxmobile.services.IMevo;
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
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
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
	private boolean binded = false;




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
				return;
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
					int time = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_refresh), getString(R.integer.default_refresh)));
					Log.d(TAG, "On First Exec (Mevo) : time set to "+time+" minutes");
					MevoSync.changeTimer(time, this);
				}
				
				
			}
		}


		try{
			if (!(binded = bindService(new Intent("org.madprod.freeboxmobile.services.MevoService"), mMevoConnection, Context.BIND_AUTO_CREATE))){
				showPopupFbm();		
				return;
			}
		}catch(SecurityException e){
			e.printStackTrace();
		}

		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(TrackerConstants.ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView(TrackerConstants.HOME);




		final ListView lv = getListView();
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v, final int
					pos, long id) {
				Cursor cMessages = (Cursor)lv.getAdapter().getItem(pos);
				Long idMessage = cMessages.getLong(cMessages.getColumnIndex(KEY_ROWID));
				int status = cMessages.getInt(cMessages.getColumnIndex(KEY_STATUS));
				String presence = cMessages.getString(cMessages.getColumnIndex(KEY_PRESENCE));
				String source = cMessages.getString(cMessages.getColumnIndex(KEY_SOURCE));
				String date = cMessages.getString(cMessages.getColumnIndex(KEY_QUAND));
				String link = cMessages.getString(cMessages.getColumnIndex(KEY_LINK));
				String delete = cMessages.getString(cMessages.getColumnIndex(KEY_DEL));
				String name = cMessages.getString(cMessages.getColumnIndex(KEY_NAME));				
				String length = cMessages.getString(cMessages.getColumnIndex(KEY_LENGTH));
				String filename = "";
				try {
					filename = mMevo.getMessageFile(idMessage);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				if (filename == null) filename = "";
				Log.e(TAG, "file = "+filename);
				final MevoMessage message = new MevoMessage(idMessage, status, presence, source, date, link, delete, name, length, filename);



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

				final ActionItem deleteAction = new ActionItem();
				deleteAction.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));				
				deleteAction.setTitle("Supprimer");
				deleteAction.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Utils.removeMessage(HomeActivity.this, message);
						tracker.trackPageView("Mevo/RemoveMessage");
						qa.dismiss();
					}
				});					
				qa.addActionItem(deleteAction);

				final ActionItem unreadAction = new ActionItem();
				unreadAction.setIcon(getResources().getDrawable(R.drawable.bouton_vide_small));				
				unreadAction.setTitle("Marquer non lu");
				unreadAction.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Utils.setUnreadMessage(HomeActivity.this, message);
						tracker.trackPageView("Mevo/SetUnread");
						qa.dismiss();
					}
				});					
				qa.addActionItem(unreadAction);

				qa.setAnimStyle(QuickAction.ANIM_AUTO);

				qa.show();
				return true;
			}


		});   




		findViewById(R.id.btn_fbm).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onFBMClick(v);}});
		findViewById(R.id.btn_title_refresh).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onRefreshClick(v);}});
		findViewById(R.id.btn_title_settings).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onSettings(v);}});
		
		

		dataBind();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}




	private void dataBind(){
		Cursor c = managedQuery(Uri.parse("content://org.madprod.freeboxmobile.Provider/messages"), null, null, null, null);

		if (c.getCount() == 0){
			findViewById(R.id.noMessage).setVisibility(View.VISIBLE);
			getListView().setVisibility(View.GONE);
		}else{
			findViewById(R.id.noMessage).setVisibility(View.GONE);
			getListView().setVisibility(View.VISIBLE);			
		}

		
		startManagingCursor(c);
		
		setListAdapter(new MevoMessageAdapter(HomeActivity.this, R.layout.mevo_messages_row, c));
		getListAdapter().registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				dataBind();
				super.onChanged();
			}
		});
		
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
		}

	};


	@Override
	protected void onDestroy() {
		if (mMevoConnection != null && binded){
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
//		dataBind();
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
		Cursor cMessages = (Cursor)l.getAdapter().getItem(pos);
		Long idMessage = cMessages.getLong(cMessages.getColumnIndex(KEY_ROWID));
		int status = cMessages.getInt(cMessages.getColumnIndex(KEY_STATUS));
		String presence = cMessages.getString(cMessages.getColumnIndex(KEY_PRESENCE));
		String source = cMessages.getString(cMessages.getColumnIndex(KEY_SOURCE));
		String date = cMessages.getString(cMessages.getColumnIndex(KEY_QUAND));
		String link = cMessages.getString(cMessages.getColumnIndex(KEY_LINK));
		String delete = cMessages.getString(cMessages.getColumnIndex(KEY_DEL));
		String name = cMessages.getString(cMessages.getColumnIndex(KEY_NAME));				
		String length = cMessages.getString(cMessages.getColumnIndex(KEY_LENGTH));
		String filename = "";
		try {
			filename = mMevo.getMessageFile(idMessage);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (filename == null) filename = "";
		Log.e(TAG, "file = "+filename);
		MevoMessage message = new MevoMessage(idMessage, status, presence, source, date, link, delete, name, length, filename);

		Intent i = new Intent(this, PlayerActivity.class);
		i.putExtra("message", message);
		startActivity(i);

		try{
			mMevo.setMessageRead(idMessage);
		}catch (RemoteException e) {
			e.printStackTrace();
		} 

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
			StateMevoRefresh.mSyncing = false;
			updateRefreshStatus();
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
					AlertDialog ad = new AlertDialog.Builder(HomeActivity.this).create();
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




