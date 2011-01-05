package org.madprod.mevo;


import java.io.IOException;    
import java.util.Timer;
import java.util.TimerTask;

import org.madprod.mevo.icons.IconView;
import org.madprod.mevo.tools.Constants;
import org.madprod.mevo.tools.Utils;
import org.madprod.mevo.tracker.TrackerConstants;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

@SuppressWarnings("deprecation")
public class PlayerActivity extends Activity implements Constants, SensorEventListener{

	private GoogleAnalyticsTracker tracker;
	private MevoMessage message;
	private final Contact contact = new Contact();
	private MediaPlayer mp ;
	private SeekBar messageSeekBar;
	private final Timer messageTimer = new Timer();
	private TimerTask messageUpdateTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_player);


		if (getIntent().getExtras() != null){
			Bundle b = getIntent().getExtras();
			message = (MevoMessage)b.get("message");
		}


		super.onCreate(savedInstanceState);
		if (message == null || TextUtils.isEmpty(message.getFileName())) finish();

		if (mp == null){

			getContact();
			Uri file = Uri.parse(message.getFileName());
			mp = MediaPlayer.create(this, file);
			if (mp != null) {
				try {
					mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
					mp.prepare();

				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				mp.setScreenOnWhilePlaying(true);
				mp.setVolume(1000,1000);
				mp.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						messageUpdateTask.cancel();
						messageTimer.purge();
						setMessageSeekBar(0, 0, mp.getDuration());
						setIconPlay(false);
					}
				});

				mp.setOnErrorListener(new OnErrorListener() {

					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						Log.i(TAG,"onERROR "+what+" "+extra);
						if (mp != null)
						{
							mp.stop();
						}	
						setMessageSeekBar(-1, 0, 0);
						return true;
					}
				});	
				messageSeekBar = (SeekBar)findViewById(R.id.seekbarCall);
				setSeekBarBehavior();

			}else{
				findViewById(R.id.seekbarCall).setEnabled(false);

			}
			tracker = GoogleAnalyticsTracker.getInstance();
			tracker.start(TrackerConstants.ANALYTICS_MAIN_TRACKER, 20, this);
			tracker.trackPageView(TrackerConstants.MESSAGE);		

			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.key_proximity), getResources().getBoolean(R.bool.default_proximity))){
				SensorManager sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
				boolean proximitySupported = sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_UI);

				if (!proximitySupported){
					sensorMgr.unregisterListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY));
				}				
			}

			setHpEnabled(false);

			findViewById(R.id.btn_title_home).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onHomeClick(v);}});
			findViewById(R.id.btn_title_play).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onPlay(v);}});
			findViewById(R.id.btn_title_pause).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onPause(v);}});
			findViewById(R.id.btn_title_hp_on).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onHpOn(v);}});
			findViewById(R.id.btn_title_hp_off).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onHpOff(v);}});
			findViewById(R.id.callback).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onCallback(v);}});
			findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onDelete(v);}});
			findViewById(R.id.sendSms).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onSendSms(v);}});			
			findViewById(R.id.findNumber).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onSearchNumber(v);}});
			findViewById(R.id.addContact).setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onAddContact(v);}});
		}
	}

	private void getContact(){


		ContentResolver resolver = getContentResolver();

		// define the columns I want the query to return
		String[] projection = new String[]{
				Contacts.Phones.DISPLAY_NAME,
				Contacts.Phones.TYPE,
				Contacts.Phones.NUMBER,
				Contacts.Phones.LABEL,
				Contacts.Phones.PERSON_ID

		};


		String numberUri = Uri.encode(message.getSource());

		if (numberUri != null && numberUri.length() > 0){

			Uri contactUri = Uri.withAppendedPath(Phones.CONTENT_FILTER_URL, numberUri);
			Cursor c = resolver.query(contactUri, projection, null, null, null);
			// if the query returns 1 or more results
			// return the first result
			try{
				if (c.moveToFirst()){
					contact.setId(c.getLong(c.getColumnIndex(Contacts.Phones.PERSON_ID)));
					contact.setName(c.getString(c.getColumnIndex(Contacts.Phones.DISPLAY_NAME)));
					contact.setLabel(c.getString(c.getColumnIndex(Contacts.Phones.LABEL)));
					contact.setPhone(message.getSource());
					contact.setPhoneType(c.getInt(c.getColumnIndex(Contacts.Phones.TYPE)));
				}

			}finally{
				c.close();				
			}

		}else{
			contact.setName("Inconnu");
			contact.setLabel("Inconnu");
			contact.setPhone("Inconnu");
			findViewById(R.id.callback).setVisibility(View.GONE);
			findViewById(R.id.sendSms).setVisibility(View.GONE);			
			findViewById(R.id.findNumber).setVisibility(View.GONE);
			findViewById(R.id.addContact).setVisibility(View.GONE);

		}


		if (contact.getId() != null){
			Uri uri = ContentUris.withAppendedId(People.CONTENT_URI, contact.getId());				
			((IconView)findViewById(R.id.imgPerson)).setImageBitmap(Contacts.People.loadContactPhoto(this, uri, R.drawable.fm_repondeur, null));				
			((TextView)findViewById(R.id.namePerson)).setText(contact.getName());
			((TextView)findViewById(R.id.phonePerson)).setText(contact.getPhone() + " ( "+Utils.getPhoneTypeString(this, contact.getPhoneType(), contact.getLabel())+" ) ");
			findViewById(R.id.findNumber).setVisibility(View.GONE);
			findViewById(R.id.addContact).setVisibility(View.GONE);
		}else{
			((TextView)findViewById(R.id.namePerson)).setText("Inconnu");
			((TextView)findViewById(R.id.phonePerson)).setText(message.getSource());
		}


		((TextView)findViewById(R.id.dateCall)).setText(Utils.convertDateTimeHR(message.getDate()));
		((TextView)findViewById(R.id.lengthCall)).setText(message.getLength());					




	}







	@Override
	protected void onDestroy() {
		//		tracker.stop();
		messageTimer.cancel();
		if (messageUpdateTask != null) {
			messageUpdateTask.cancel();	
		}
		if (mp != null) mp.release();

		super.onDestroy();
	}


	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onResume() {
		super.onResume();
		getContact();
	}


	private void setSeekBarBehavior(){

		messageSeekBar.setMax(mp.getDuration());
		messageSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			}

			public void onStartTrackingTouch(SeekBar seekBar)
			{
				if (messageUpdateTask != null) messageUpdateTask.cancel();
				if (messageTimer != null) messageTimer.purge();

			}

			public void onStopTrackingTouch(SeekBar seekBar)
			{

				if (mp != null)
				{
					mp.seekTo(seekBar.getProgress());
				}
				messageTimer.schedule(messageUpdateTask = new UpdateTimeTask(), 10, 250);
			}
		});
	}	

	class UpdateTimeTask extends TimerTask{

		public void run(){				
			if (mp != null)	
				setMessageSeekBar(0, mp.getCurrentPosition(), mp.getDuration());	

		}

	}	

	public void setMessageSeekBar(int visibility, int position, int maximum){

		this.messageSeekBar.setMax(maximum);	
		this.messageSeekBar.setProgress(position);
		this.messageSeekBar.setVisibility(visibility);
	}

	/** Handle "home" action. */
	public void onHomeClick(View v) {
		Utils.goHome(this);
	}

	/** Handle "home" action. */
	public void onPlay(View v) {
		if (mp != null){
			this.messageTimer.schedule(messageUpdateTask = new UpdateTimeTask(), 250, 250);
			setIconPlay(true);
			mp.start();
		}
	}

	/** Handle "home" action. */
	public void onPause(View v) {
		if (mp != null){
			setIconPlay(false);
			mp.pause();
		}
	}

	public void setIconPlay(boolean state){
		if (state){
			findViewById(R.id.btn_title_pause).setVisibility(View.VISIBLE);
			findViewById(R.id.btn_title_play).setVisibility(View.GONE);			
		}else{
			findViewById(R.id.btn_title_pause).setVisibility(View.GONE);
			findViewById(R.id.btn_title_play).setVisibility(View.VISIBLE);
		}
	}

	/** Handle "home" action. */
	public void onHpOn(View v) {
		if (mp != null){
			setHpEnabled(true);
		}

	}

	/** Handle "home" action. */
	public void onHpOff(View v) {
		if (mp != null){
			setHpEnabled(false);
		}
	}

	/** Handle "home" action. */
	public void onCallback(View v) {
		Utils.callback(this, message);
		tracker.trackPageView("Mevo/Callback");
	}

	/** Handle "home" action. */
	public void onSendSms(View v) {
		Utils.sendSms(this, message);
		tracker.trackPageView("Mevo/SendSms");
	}

	/** Handle "home" action. */
	public void onDelete(View v) {
		tracker.trackPageView("Mevo/RemoveMessage");
		finish();
		Utils.removeMessage(this, message);
	}


	/** Handle "home" action. */
	public void onSearchNumber(View v) {
		Utils.searchNumber(this, message);
		tracker.trackPageView("Mevo/AnnuaireInverse");
	}

	/** Handle "home" action. */
	public void onAddContact(View v) {
		Utils.addContact(this, message);
		tracker.trackPageView("Mevo/AddNumber");
	}	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];	
		if (x<1.0f){
			findViewById(R.id.screen).setVisibility(View.INVISIBLE);
		}else{
			findViewById(R.id.screen).setVisibility(View.VISIBLE);
		}

	}


	private void setHpEnabled(boolean state){
		AudioManager mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
		if (state){
			findViewById(R.id.btn_title_hp_off).setVisibility(View.VISIBLE);
			findViewById(R.id.btn_title_hp_on).setVisibility(View.GONE);
			mAudioManager.setMode(AudioManager.MODE_NORMAL);
			mAudioManager.setSpeakerphoneOn(true);			
		}else{
			findViewById(R.id.btn_title_hp_off).setVisibility(View.GONE);
			findViewById(R.id.btn_title_hp_on).setVisibility(View.VISIBLE);
			mAudioManager.setMode(AudioManager.MODE_IN_CALL);
			mAudioManager.setSpeakerphoneOn(false);

		}


	}

}
