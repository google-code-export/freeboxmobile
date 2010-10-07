package org.madprod.freeboxmobile.mvv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.mvv.MevoMessage;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.ServiceUpdateUIListener;
import org.madprod.freeboxmobile.Utils;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Contacts.Intents.Insert;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class MevoActivity extends ListActivity implements MevoConstants
{
    MessagesAdapter mAdapter;
    static Activity mevoActivity;
    AudioManager mAudioManager;
    Cursor mCursor;
    int audioManagerModeAtStart;

    ImageView speakerButton;
    static Button callbackButton;
    static Button deleteButton;

	static GoogleAnalyticsTracker tracker;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		Log.i(TAG,"MevoActivity create");
        super.onCreate(savedInstanceState);
        FBMNetTask.register(this);
    	MevoSync.setActivity(this);
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Mevo/ListeAppels");
        setContentView(R.layout.mevo);
        registerForContextMenu(getListView());
        mAdapter = new MessagesAdapter(this, getContentResolver()); 
        setListAdapter(mAdapter);
        mevoActivity = this;        
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        speakerButton = (ImageView) findViewById(R.id.MevoButtonHP);
        callbackButton = (Button) findViewById(R.id.MevoButtonCallback);
        deleteButton = (Button) findViewById(R.id.MevoButtonDelete);
        audioManagerModeAtStart = mAudioManager.getMode(); 
		if (mAudioManager.getMode() != AudioManager.MODE_IN_CALL)
			speakerButton.setImageResource(R.drawable.mevo_hp_vert);
		else
			speakerButton.setImageResource(R.drawable.mevo_hp_rouge);
        speakerButton.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
						if (mAudioManager.getMode() != AudioManager.MODE_IN_CALL)
						{
							setHpOff();
						}
						else
						{
							setHpOn();
						}
				    }
				}
			);
        callbackButton.setOnClickListener(
        		new View.OnClickListener()
        		{
        			public void onClick(View view)
        			{
        				mAdapter.callback(mAdapter.getCurrentPos());
        			}
        		}
        );
		callbackButton.setFocusable(false);
		callbackButton.setClickable(false);
        deleteButton.setOnClickListener(
        		new View.OnClickListener()
        		{
        			public void onClick(View view)
        			{
        				mAdapter.supprMsg(mAdapter.getCurrentPos());
        			}
        		}
        );
		deleteButton.setFocusable(false);
		deleteButton.setClickable(false);
		
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		if (!mgr.getString(KEY_SPLASH_MEVO, "0").equals(Utils.getFBMVersion(this)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH_MEVO, Utils.getFBMVersion(this));
			editor.commit();
			displayAboutMevo();
		}
    }

    @Override
    public void onStart()
    {
    	super.onStart();
    	MevoSync.setActivity(this);
       	MevoSync.setUpdateListener(
   			new ServiceUpdateUIListener()
	    	{
				@Override
				public void updateUI()
				{
					Log.i(TAG,"updateUI");
					runOnUiThread(
						new Runnable()
						{
							public void run()
							{
								mAdapter.refreshUI();
							}
						});
				}
	    	});
    	Log.i(TAG,"MevoActivity Start");
    }
    
    private void logMsgDetails(long id)
    {
    	this.mAdapter.logMessageInfos((int) id);
    }

    @Override
	public void onStop()
	{
		this.mAdapter.stop();
		MevoSync.setUpdateListener(null);
		mAudioManager.setMode(audioManagerModeAtStart);
		super.onStop();
	}

    @Override
    public void onDestroy()
    {
    	FBMNetTask.unregister(this);
    	super.onDestroy();
    	tracker.stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
    	outState.putString("hpBoutton", (mAudioManager.getMode()!=AudioManager.MODE_IN_CALL)?"1":"0");
    	Log.i(TAG,"onSaveInstanceState called");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
            super.onRestoreInstanceState(savedInstanceState);
            Log.i(TAG,"onRestoreInstanceState called");
            if (savedInstanceState.getString("hpBoutton").equals("1"))
            {
            	setHpOn();
            }
            else
            {
            	setHpOff();
            }
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{
		AdapterContextMenuInfo info;

		super.onCreateContextMenu(menu, view, menuInfo);
		try
		{
			info = (AdapterContextMenuInfo) menuInfo;
			int id = (int)info.id;

		    menu.setHeaderTitle(this.mAdapter.getMessageString(id,KEY_CALLER));
		    if (!((MevoMessage)this.mAdapter.getItem(id)).getStringValue(KEY_SOURCE).equals(""))
		    {
			    menu.add(0, MEVO_CONTEXT_CALLBACK, 0, R.string.mevo_context_callback);
		    	menu.add(0, MEVO_CONTEXT_SENDSMS, 0, R.string.mevo_context_sendsms);
		    }
		    if (((MevoMessage) this.mAdapter.getItem(id)).getStringValue(KEY_SOURCE).equals(((MevoMessage) this.mAdapter.getItem(id)).getStringValue(KEY_CALLER)))
		    {
		    	menu.add(0, MEVO_CONTEXT_SEARCHNUMBER, 0, R.string.mevo_context_searchnumber);
		    	menu.add(0, MEVO_CONTEXT_ADDNUMBER, 0, R.string.mevo_context_addnumber);
		    }
//		    menu.add(0, MEVO_CONTEXT_VIEWDETAILS, 0, R.string.mevo_context_msgdetails);
		    if (this.mAdapter.getMessageInt(id, KEY_STATUS) == MSG_STATUS_LISTENED)
		    {
			    menu.add(0, MEVO_CONTEXT_MARKUNREAD, 0, R.string.mevo_context_markunread);
		    }
		    else
		    {
			    menu.add(0, MEVO_CONTEXT_MARKREAD, 0, R.string.mevo_context_markread);		    	
		    }
		    menu.add(0, MEVO_CONTEXT_DELETE, 0, R.string.mevo_context_delete);
//		    menu.add(0, MEVO_CONTEXT_DELETEFRMSRV, 0, R.string.mevo_context_deletefrmsrv);
//		    menu.add(0, MEVO_CONTEXT_SEND, 0, R.string.mevo_context_send);
		}
		catch (ClassCastException e)
		{
			Log.e(TAG,"Bad Context Menu Info"+e);
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id)
	{
		super.onListItemClick(l, v, pos, id);
		mAdapter.onItemClick(pos, v, id);
		TextView t = ((TextView) findViewById(R.id.caller_name));
		t.setText(mAdapter.getMessageString(pos, KEY_CALLER));
		t.setVisibility(View.VISIBLE);
		t = ((TextView) findViewById(R.id.caller_number));
		if ((mAdapter.getMessageString(pos, KEY_SOURCE)).equals(""))
		{
			t.setText(mAdapter.getMessageString(pos, KEY_NB_TYPE));			
		}
		else
		{
			t.setText(mAdapter.getMessageString(pos, KEY_NB_TYPE)+" : "+mAdapter.getMessageString(pos, KEY_SOURCE));
		}
		t.setVisibility(View.VISIBLE);
		if (callbackButton.isFocusable() == false)
		{
			callbackButton.setFocusable(true);
			callbackButton.setClickable(true);
			callbackButton.setTextColor(Color.BLACK);
    		callbackButton.setVisibility(View.VISIBLE);
			deleteButton.setFocusable(true);
			deleteButton.setClickable(true);
			deleteButton.setTextColor(Color.BLACK);
    		deleteButton.setVisibility(View.VISIBLE);
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, MEVO_OPTION_REFRESH, 0, R.string.mevo_option_update).setIcon(android.R.drawable.ic_menu_rotate);
        return true;
    }

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	switch (item.getItemId())
    	{
			case MEVO_CONTEXT_CALLBACK:
				mAdapter.callback((int)info.id);
			break;
    		case MEVO_CONTEXT_SENDSMS:
    			mAdapter.sendSMS((int)info.id);
    		break;
    		case MEVO_CONTEXT_ADDNUMBER:
    			mAdapter.addNumber((int)info.id);
    		break;
    		case MEVO_CONTEXT_SEARCHNUMBER:
    			mAdapter.searchNumber((int)info.id);
        	break;
    		case MEVO_CONTEXT_MARKREAD:
    			mAdapter.setMessageInt((int)info.id, KEY_STATUS,MSG_STATUS_LISTENED);
    			mAdapter.updateMessageCount();
    		break;
    		case MEVO_CONTEXT_MARKUNREAD:
    			mAdapter.setMessageInt((int)info.id, KEY_STATUS,MSG_STATUS_UNLISTENED);
    			mAdapter.updateMessageCount();
    		break;
    		case MEVO_CONTEXT_VIEWDETAILS:
    			Toast.makeText(this, "Fonctionnalité non encore disponible", Toast.LENGTH_LONG).show();
    			logMsgDetails(info.id);
    		break;
    		case MEVO_CONTEXT_DELETE:
    			mAdapter.supprMsg((int)info.id);
    		break;
    		case MEVO_CONTEXT_DELETEFRMSRV:
    			Toast.makeText(this, "Fonctionnalité non encore disponible", Toast.LENGTH_LONG).show();
    		break;
    		case MEVO_CONTEXT_SEND:
    			Toast.makeText(this, "Fonctionnalité non encore disponible", Toast.LENGTH_LONG).show();
    		break;
    	}
    	return super.onContextItemSelected(item);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
	        case MEVO_OPTION_REFRESH:
	        	MevoSync.refresh();
	        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onResume()
	{
		Log.i(TAG,"onResume() start");
		super.onResume();
		MevoSync.cancelNotif(NOTIF_MEVO);
		mAdapter.refreshUI();
	}

	@Override
	public void onPause()
	{
		Log.i(TAG,"MevoActivity pause");
		super.onPause();
		mAdapter.stopPlay();
		mAdapter.releaseMP();
		MevoSync.setActivity(null);
	}

	private void setHpOff()
	{
		mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		mAudioManager.setSpeakerphoneOn(false);
		speakerButton.setImageResource(R.drawable.mevo_hp_rouge);
	}
	
	private void setHpOn()
	{
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		mAudioManager.setSpeakerphoneOn(true);
		speakerButton.setImageResource(R.drawable.mevo_hp_vert);
	}

	private void displayAboutMevo()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name)+" - Messagerie Vocale");
		d.setMessage(
			"Incitez vos correspondants à vous laisser des messages !\n\n"+
			"Créez une annonce d'accueil en composant **1 sur "+
			"le téléphone de votre Freebox."
			);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			}
			);
		d.show();
    }

    public static class MessagesAdapter extends BaseAdapter implements OnCompletionListener, OnErrorListener
    {
    	private Context mContext;
    	private ArrayList<MevoMessage> messages = new ArrayList<MevoMessage>();
        protected MevoDbAdapter mDbHelper = null;
        
        SeekBar messageSeekBar;
        private Timer messageTimer = new Timer();
        private TimerTask messageUpdateTask = null;

        private String msg_unit;
        
        private int			play_current_pos = -1;
        private MediaPlayer play_current_mp = null;

    	public MessagesAdapter(Context context, ContentResolver cr)
    	{
    		mContext = context;
    		mDbHelper = new MevoDbAdapter(mContext);
			msg_unit = mContext.getString(R.string.mevo_text_seconds);
			this.messageSeekBar = ((SeekBar) ((Activity) mContext).findViewById(R.id.message_seekbar));
			this.messageSeekBar.setVisibility(View.GONE);
			this.messageSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
			{
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
				{
						// Si on modifie la position du player sur cet event, on le fait à chaque timer (250ms)
                }
				
				public void onStartTrackingTouch(SeekBar seekBar)
				{
					messageUpdateTask.cancel();
					messageTimer.purge();
				}
				
				public void onStopTrackingTouch(SeekBar seekBar)
				{
					messageTimer.schedule(messageUpdateTask = new UpdateTimeTask(), 250, 250);
					if (play_current_mp != null)
					{
						play_current_mp.seekTo(seekBar.getProgress());
					}
				}
			});
    	}

        class UpdateTimeTask extends TimerTask
        {
        	public void run()
        	{
        		if ((play_current_mp != null) && (play_current_mp.isPlaying() == true))
        		{
        			setMessageSeekBar(0, play_current_mp.getCurrentPosition(), play_current_mp.getDuration());
        		}
        	}
        }

    	public void setMessageSeekBar(int visibility, int position, int maximum)
    	{
    		this.messageSeekBar.setMax(maximum);
    		this.messageSeekBar.setProgress(position);
    		this.messageSeekBar.setVisibility(visibility);
    	}
    	
    	private void hideBouttons()
    	{
    		callbackButton.setFocusable(false);
    		callbackButton.setClickable(false);
    		callbackButton.setTextColor(0xFFFF);
    		callbackButton.setVisibility(View.INVISIBLE);
    		deleteButton.setFocusable(false);
    		deleteButton.setClickable(false);
    		deleteButton.setTextColor(0x00888888);
    		deleteButton.setVisibility(View.INVISIBLE);
    		TextView t = ((TextView) ((Activity) mContext).findViewById(R.id.caller_name));
    		t.setText("");
    		t.setVisibility(View.GONE);
    		t = ((TextView) ((Activity) mContext).findViewById(R.id.caller_number));
    		t.setText("");
    		t.setVisibility(View.GONE);
			messageSeekBar.setVisibility(View.GONE);
    	}

    	private void getMessages()
    	{
    		Cursor messagesCursor;

            Log.i(TAG,"getMessages() called");

    		play_current_mp = null;

        	releaseMP();
            mDbHelper.open();
			messages.clear();
            if (updateMessageCount() == 0)
            {
            	hideBouttons();
            }

        	// Get all of the rows from the database and create the item list
            messagesCursor = mDbHelper.fetchAllMessages();

            ((Activity) mContext).startManagingCursor(messagesCursor);

            if (messagesCursor.moveToFirst())
            {
            	do
            	{
            		MevoMessage m = new MevoMessage(mContext, mDbHelper);
            		m.setMsgFromCursor(messagesCursor);
            		messages.add(m);
            	} while (messagesCursor.moveToNext());
            }
            messagesCursor.close();
    	}

    	public void releaseMP()
    	{
    		Iterator<MevoMessage> i = messages.iterator();
    		while (i.hasNext())
    		{
    			i.next().releaseMP();
    		}
    		if (mDbHelper != null)
     			mDbHelper.close();
    	}

    	public String getMessageString(int id, String key)
    	{
    		return ((MevoMessage) this.getItem(id)).getStringValue(key);
    	}

    	public int getMessageInt(int id, String key)
    	{
    		return ((MevoMessage) this.getItem(id)).getIntValue(key);
    	}

    	public int updateMessageCount()
    	{
    		int nb = (int) mDbHelper.getNbUnreadMsg();
    		switch (nb)
    		{
/*    			case 0:
        			((Activity) mContext).setTitle(mContext.getString(R.string.app_name)+" "+mContext.getString(R.string.mevo_title_nomsg) );    				
    			break;
    			case 1:
        			((Activity) mContext).setTitle(mContext.getString(R.string.app_name)+" "+mContext.getString(R.string.mevo_title_message) );    				
        		break;
*/        		default:
//        			((Activity) mContext).setTitle(mContext.getString(R.string.app_name)+" ("+nb+") "+mContext.getString(R.string.mevo_title_messages) );
        			((Activity) mContext).setTitle(mContext.getString(R.string.app_name)+" "+mContext.getString(R.string.mevo_title_mevo)+" ("+nb+") - "+FBMHttpConnection.getTitle());
        		break;
    		}
    		Log.d(TAG,"Mevo nombre msgs : "+mDbHelper.getNbMsg());
    		return nb;
    	}

    	public void setMessageInt(int id, String k, int val)
    	{
    		((MevoMessage)this.getItem(id)).setIntValue(k, val, true);
    		notifyDataSetInvalidated();
    	}

    	public void refreshUI()
    	{
    		getMessages();
//    		updateMessageCount();
    		notifyDataSetChanged();
    		notifyDataSetInvalidated();
    	}

    	public void supprMsg(int id)
    	{
			if (play_current_pos == id)
			{
				if ((play_current_mp != null) && (play_current_mp.isPlaying()))
				{
					play_current_mp.release();
					play_current_mp = null;
					play_current_pos = -1;
				}
			}
			hideBouttons();
			((MevoMessage) this.getItem(id)).releaseMP();
			new DeleteMessage().execute(((MevoMessage)this.getItem(id)).getStringValue(KEY_NAME));
    	}

    	public void callback(int id)
    	{
    		try
    		{
    			Intent intent = new Intent(Intent.ACTION_CALL);
    			intent.setData(Uri.parse("tel:"+((MevoMessage)this.getItem(id)).getStringValue(KEY_SOURCE)));
    			tracker.trackPageView("Mevo/Callback");
    			mevoActivity.startActivity(intent);
    		}
    		catch (Exception e)
    		{
    			Log.e(TAG,"Impossible de passer l'appel "+((MevoMessage)this.getItem(id)).getStringValue(KEY_SOURCE)+" "+e.getMessage());
    			e.printStackTrace();
    		}
    	}

    	public void addNumber(int id)
    	{
			Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);

			try
			{
				// For Android >= 2.0
				intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
				intent.putExtra(ContactsContract.Intents.Insert.PHONE, ((MevoMessage)this.getItem(id)).getStringValue(KEY_SOURCE));
    			tracker.trackPageView("Mevo/AddNumber");
				mevoActivity.startActivity(intent);
			}
			catch (Throwable t)
			{
				// For Android 1.5 - 1.6
				intent.setType(Contacts.People.CONTENT_ITEM_TYPE);
				intent.putExtra(Insert.PHONE, ((MevoMessage)this.getItem(id)).getStringValue(KEY_SOURCE));
    			tracker.trackPageView("Mevo/AddNumber");
				mevoActivity.startActivity(intent);
			}
    	}

    	public void searchNumber(int id)
    	{
//            Intent searchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.118218.fr/recherche/?q="+((MevoMessage)this.getItem(id)).getStringValue(KEY_SOURCE)+"&rewrited=&rewritedLocality=&address=&b=0&typ=r&st=I"));
            Intent searchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.118218.fr/wap/resultats.php?requete="+((MevoMessage)this.getItem(id)).getStringValue(KEY_SOURCE)+"&particulier=on&activite=&page=1"));
            searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			tracker.trackPageView("Mevo/AnnuaireInverse");
            mevoActivity.startActivity(searchIntent);
    	}

    	public void sendSMS(int id)
    	{
    		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:"+((MevoMessage)this.getItem(id)).getStringValue(KEY_SOURCE)));
			intent.putExtra("sms_body", "Suite à ton message "); 
			intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
			tracker.trackPageView("Mevo/SendSMS");
			mevoActivity.startActivity(intent);		

//			Ci dessous : ca serait sympa si ca fonctionnait, ca permettrait de choisir avec quelle appli envoyer le SMS
// 			Mais ca ne foncionne pas :-(
/*
    		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+((MevoMessage)this.getItem(id)).getStringValue(KEY_SOURCE)));
			intent.putExtra("sms_body", "Suite à ton message "); 
			intent.setType("vnd.android-dir/mms-sms");
			mevoActivity.startActivity(intent);
*/		
    	}

    	public void logMessageInfos(int id)
    	{
			((MevoMessage) this.getItem((int) id)).log();
    	}

    	public int getCurrentPos()
    	{
    		return play_current_pos;
    	}

		@Override
		public int getCount()
		{
			return (this.messages.size());
		}

		@Override
		public Object getItem(int position)
		{
			return (this.messages.get(position));
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder	holder;

			MevoMessage curMsg = messages.get(position);
			if (convertView == null)
			{
				holder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater.from(this.mContext);
				convertView = inflater.inflate(R.layout.mevo_messages_row, null);
				
				holder.quand = (TextView)convertView.findViewById(R.id.quand);
				holder.length = (TextView)convertView.findViewById(R.id.length);
				holder.qui = (TextView)convertView.findViewById(R.id.source);
		        holder.bouton = (ImageView)convertView.findViewById(R.id.boutonLecture);
		        convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder)convertView.getTag();
			}

			holder.quand.setText(curMsg.getStringValue(KEY_QUAND_HR));
			holder.length.setText(curMsg.getStringValue(KEY_LENGTH)+" "+this.msg_unit);

			// Ici je mettais en gras les messages présents sur le serveur. Mais inutile, pas joli.
/* 			if (curMsg.getStringValue(KEY_DEL).compareTo("") != 0)
			{
				holder.qui.setTypeface(Typeface.DEFAULT_BOLD);
			}
*/
			if (position == play_current_pos)
			{
				convertView.setBackgroundColor(Color.LTGRAY);
			}
			else
			{
				convertView.setBackgroundDrawable(null);
			}
			holder.qui.setText(curMsg.getStringValue(KEY_CALLER));
			switch (curMsg.getIntValue(KEY_PLAY_STATUS))
			{
				case PLAY_STATUS_STOP :
					if (curMsg.getIntValue(KEY_STATUS) == MSG_STATUS_LISTENED)
					{
						holder.bouton.setImageResource(R.drawable.bouton_clear);
					}
					else
					{
						holder.bouton.setImageResource(R.drawable.bouton_vide_small);
					}
					break;
				case PLAY_STATUS_PLAY:
					holder.bouton.setImageResource(R.drawable.bouton_pause);
					break;
				case PLAY_STATUS_PAUSE:
					holder.bouton.setImageResource(R.drawable.bouton_lecture);
					break;
			}
			holder.qui.setKeyListener(null);
			if (!curMsg.isMPInit())
			{
					if (!curMsg.setMsgSource(Environment.getExternalStorageDirectory().toString()+DIR_FBM+FBMHttpConnection.getIdentifiant()+DIR_MEVO+curMsg.getStringValue(KEY_NAME)))
					{
//						Toast t = Toast.makeText(MevoActivity.mevoActivity, "Problème avec le fichier du message !", Toast.LENGTH_SHORT);
//						t.show();
					}
			}
			return convertView;
		}

		public void stop()
		{
			if (play_current_mp != null)
				play_current_mp.release();
			if (mDbHelper != null)
			{
				mDbHelper.close();
			}
		}

		@Override
    	public void onCompletion(MediaPlayer mp)
		{
    		Log.i(TAG,"OnCompletion ! "+this.play_current_pos);
			messages.get(this.play_current_pos).setIntValue(KEY_PLAY_STATUS, PLAY_STATUS_STOP, false);
    		notifyDataSetInvalidated();
    		this.messageUpdateTask.cancel();
    		this.messageTimer.purge();
			this.setMessageSeekBar(0, play_current_mp.getDuration(), play_current_mp.getDuration());
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra)
		{
			Log.i(TAG,"onERROR "+what+" "+extra);
			if ((play_current_mp != null) && (play_current_mp.isPlaying()))
			{
				play_current_mp.stop();
			}
			this.setMessageSeekBar(-1, 0, 0);
    		notifyDataSetChanged();
			return true;
		}

		public void stopPlay()
		{
			if (messageUpdateTask != null)
			{
				messageUpdateTask.cancel();
			}
			if (messageTimer != null)
			{
				messageTimer.purge();
			}
			// Si on était pas dans le cas d'un premier play
			if (play_current_mp != null)
			{
				if (play_current_mp.isPlaying())
				{
					Log.i(TAG,"Stop current play");
					play_current_mp.pause();
					play_current_mp.seekTo(0);
				}
				if (play_current_pos != -1)
				{
					messages.get(play_current_pos).setIntValue(KEY_PLAY_STATUS, PLAY_STATUS_STOP, false);
				}
			}
		}

		public void onItemClick(int pos, View view, long id)
		{
//			ViewHolder holder = (ViewHolder) view.findViewById(R.id.ligneMevo).getTag();
			MevoMessage m = messages.get(pos);
			Log.d(TAG,"onITEMClick v:"+view+" pos:"+pos+" id:"+id+" cp:"+play_current_pos);
			// Si on jouait un message d'une autre ligne,
			// on lance le message de cette ligne à la place
			if ((play_current_pos != pos) || (play_current_mp == null))
			{
				stopPlay();
				try
				{
    				play_current_pos = pos;
					Log.d(TAG,"Prepare play "+pos);
					play_current_mp = m.getMP();
					if (play_current_mp != null) // Dans le cas d'un fichier de message pas valide,play_current_mp == null
					{
						m.setIntValue(KEY_STATUS, MSG_STATUS_LISTENED, true);
						m.setIntValue(KEY_PLAY_STATUS, PLAY_STATUS_PLAY, false);
						play_current_mp.setOnCompletionListener(this);
						play_current_mp.start();
						this.setMessageSeekBar(0, play_current_mp.getCurrentPosition(), play_current_mp.getDuration());
						this.messageTimer.schedule(messageUpdateTask = new UpdateTimeTask(), 250, 250);
					}
					else
					{
						Toast t = Toast.makeText(MevoActivity.mevoActivity, "Problème avec le fichier du message !", Toast.LENGTH_SHORT);
						t.show();
					}
					updateMessageCount();
					MevoSync.cancelNotif(NOTIF_MEVO);
				}
				catch (IllegalStateException e)
				{
					m.setIntValue(KEY_PLAY_STATUS, PLAY_STATUS_STOP, false);
					play_current_mp.stop();
					this.messageUpdateTask.cancel();
					this.messageTimer.purge();
					this.setMessageSeekBar(-1, 0, 0);
					Log.d(TAG,"MEDIAPLAYER : Illegal State Exception "+e);
					e.printStackTrace();
				}
			}
			// sinon, l'utilisateur voulait juste arreter ou reprendre le message courant
			else
			{
				// Si on était déjà en train de jouer un message, on le pause
				if (play_current_mp.isPlaying())
				{
					play_current_mp.pause();
	        		m.setIntValue(KEY_PLAY_STATUS, PLAY_STATUS_PAUSE, false);
	        		this.setMessageSeekBar(0, play_current_mp.getCurrentPosition(), play_current_mp.getDuration());
	        		this.messageUpdateTask.cancel();
					this.messageTimer.purge();
				}
				// Sinon on le reprend
				else
				{
					this.play_current_mp.start();
	        		m.setIntValue(KEY_PLAY_STATUS, PLAY_STATUS_PLAY, false);
	        		this.setMessageSeekBar(0, play_current_mp.getCurrentPosition(), play_current_mp.getDuration());
					this.messageTimer.schedule(messageUpdateTask = new UpdateTimeTask(), 250, 250);
				}
			}
    		notifyDataSetInvalidated();
		}

		private class ViewHolder
		{
			TextView 	quand;
			TextView	qui;
			TextView	length;
			ImageView	bouton;
		}

	    private class DeleteMessage extends AsyncTask<String, Void, Void>
	    {
			@Override
			protected Void doInBackground(String... name)
			{
				MevoSync.deleteMsg(name[0]);
				return null;
			}

			@Override
			protected void onPreExecute()
			{
				MevoSync.showPdDelete();
			}

			@Override
			protected void onPostExecute(Void r)
			{
				MevoSync.dismissPd();
			}
		}
    }
}
