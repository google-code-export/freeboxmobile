package org.madprod.freeboxmobile;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
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

public class FreeboxMobileMevo extends ListActivity implements Constants
{
    MessagesAdapter mAdapter;
    static Activity mevoActivity;
    AudioManager mAudioManager;
    Cursor mCursor;
    
    Button speakerButton;
    Button callbackButton;
    Button deleteButton;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		Log.d(DEBUGTAG,"MevoActivity create");
        super.onCreate(savedInstanceState);
    	HttpConnection.setActivity(this);
        setContentView(R.layout.mevo);
        registerForContextMenu(getListView());
        mAdapter = new MessagesAdapter(this, getContentResolver()); 
        setListAdapter(mAdapter);
        mevoActivity = this;
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        speakerButton = (Button) findViewById(R.id.MevoButtonSpeaker);
        callbackButton = (Button) findViewById(R.id.MevoButtonCallback);
        deleteButton = (Button) findViewById(R.id.MevoButtonDelete);
		if (mAudioManager.getMode() != AudioManager.MODE_IN_CALL)
			speakerButton.setTextColor(Color.BLACK);
		else
			speakerButton.setTextColor(Color.WHITE);
        speakerButton.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
						if (mAudioManager.getMode() != AudioManager.MODE_IN_CALL)
						{
							mAudioManager.setMode(AudioManager.MODE_IN_CALL);
							mAudioManager.setSpeakerphoneOn(false);
							speakerButton.setTextColor(Color.WHITE);
						}
						else
						{
							mAudioManager.setMode(AudioManager.MODE_NORMAL);
							mAudioManager.setSpeakerphoneOn(true);
							speakerButton.setTextColor(Color.BLACK);
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
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	HttpConnection.setActivity(this);
       	HttpConnection.setUpdateListener(new ServiceUpdateUIListener()
    	{
			@Override
			public void updateUI()
			{
				Log.d(DEBUGTAG,"updateUI");
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						mAdapter.refreshUI();
					}
				});
			}
    	}
    	);
    	Log.d(DEBUGTAG,"MevoActivity Start");
    }
    
    private void logMsgDetails(long id)
    {
    	this.mAdapter.logMessageInfos((int) id);
    }

    @Override
	public void onStop()
	{
		super.onStop();
		this.mAdapter.stop();
	}

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{
		AdapterContextMenuInfo info;

		super.onCreateContextMenu(menu, view, menuInfo);
		try
		{
			info = (AdapterContextMenuInfo) menuInfo;
		    menu.setHeaderTitle(this.mAdapter.getMessageString((int) info.id,KEY_CALLER));
		    menu.add(0, MEVO_CONTEXT_CALLBACK, 0, R.string.mevo_context_callback);
		    menu.add(0, MEVO_CONTEXT_VIEWDETAILS, 0, R.string.mevo_context_msgdetails);
		    if (this.mAdapter.getMessageInt((int)info.id, KEY_STATUS) == MSG_STATUS_LISTENED)
		    {
			    menu.add(0, MEVO_CONTEXT_MARKUNREAD, 0, R.string.mevo_context_markunread);
		    }
		    else
		    {
			    menu.add(0, MEVO_CONTEXT_MARKREAD, 0, R.string.mevo_context_markread);		    	
		    }
		    menu.add(0, MEVO_CONTEXT_DELETE, 0, R.string.mevo_context_delete);
//		    menu.add(0, MEVO_CONTEXT_DELETEFRMSRV, 0, R.string.mevo_context_deletefrmsrv);
		    menu.add(0, MEVO_CONTEXT_SEND, 0, R.string.mevo_context_send);
		}
		catch (ClassCastException e)
		{
			Log.d(DEBUGTAG,"Bad Context Menu Info"+e);
			return;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id)
	{
		super.onListItemClick(l, v, pos, id);
		mAdapter.onItemClick(pos, v, id);
		if (callbackButton.isFocusable() == false)
		{
			TextView t = ((TextView) findViewById(R.id.caller_name));
			t.setText(mAdapter.getMessageString(pos, KEY_CALLER));
			t.setVisibility(View.VISIBLE);
			t = ((TextView) findViewById(R.id.caller_number));
			t.setText(mAdapter.getMessageString(pos, KEY_NB_TYPE)+" : "+mAdapter.getMessageString(pos, KEY_SOURCE));
			t.setVisibility(View.VISIBLE);
			callbackButton.setFocusable(true);
			callbackButton.setClickable(true);
			callbackButton.setTextColor(Color.BLACK);
			deleteButton.setFocusable(true);
			deleteButton.setClickable(true);
			deleteButton.setTextColor(Color.BLACK);
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, MEVO_OPTION_REFRESH, 0, R.string.mevo_option_update).setIcon(android.R.drawable.ic_menu_rotate);
        menu.add(0, MEVO_OPTION_CONFIG, 1, R.string.mevo_option_config).setIcon(android.R.drawable.ic_menu_preferences);
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
	        case MEVO_OPTION_CONFIG:
	        	Intent i = new Intent();
		    	i.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.Config");
		    	startActivity(i);
	            return true;
	        case MEVO_OPTION_REFRESH:
	        	HttpConnection.refresh();
	        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onResume()
	{
		Log.d(DEBUGTAG,"onResume() start");
		super.onResume();
		mAdapter.refreshUI();
		Log.d(DEBUGTAG,"onResume() end");
	}

	@Override
	public void onPause()
	{
		Log.d(DEBUGTAG,"onPause() start");
		super.onPause();
		mAdapter.stopPlay();
		mAdapter.releaseMP();
		Log.d(DEBUGTAG,"onPause() end");
	}
	
    public static class MessagesAdapter extends BaseAdapter implements OnCompletionListener, OnErrorListener
    {
    	private Context mContext;
    	private ArrayList<Message> messages = new ArrayList<Message>();
        protected FreeboxMobileDbAdapter mDbHelper = null;
        
        private String msg_unit;
        
        private int			play_current_pos = -1;
        private MediaPlayer play_current_mp = null;

    	public MessagesAdapter(Context context, ContentResolver cr)
    	{
    		mContext = context;
    		mDbHelper = new FreeboxMobileDbAdapter(mContext);
			msg_unit = mContext.getString(R.string.mevo_text_seconds);

//			getMessages();
    	}

    	private void getMessages()
    	{
    		Cursor messagesCursor;

            Log.d(DEBUGTAG,"getMessages() called");

            mDbHelper.open();

    		play_current_mp = null;
//    		play_current_pos = -1;

			messages.clear();
            this.updateMessageCount();
        	// Get all of the rows from the database and create the item list
            messagesCursor = mDbHelper.fetchAllMessages();

            ((Activity) mContext).startManagingCursor(messagesCursor);
            
            if (messagesCursor.moveToFirst())
            {
            	do
            	{
            		Message m = new Message(mContext, mDbHelper);
            		m.setMsgFromCursor(messagesCursor);
            		messages.add(m);
            	} while (messagesCursor.moveToNext());
            }
            messagesCursor.close();
    	}

    	public void releaseMP()
    	{
    		Iterator<Message> i = messages.iterator();
    		while (i.hasNext())
    		{
    			i.next().releaseMP();
    		}
    	}
    	public String getMessageString(int id, String key)
    	{
    		return ((Message) this.getItem(id)).getStringValue(key);
    	}

    	public int getMessageInt(int id, String key)
    	{
    		return ((Message) this.getItem(id)).getIntValue(key);
    	}

    	public void updateMessageCount()
    	{
    		int nb;
    		
    		nb = (int) this.mDbHelper.getNbUnreadMsg();
    		switch (nb)
    		{
    			case 0:
        			((Activity) this.mContext).setTitle(mContext.getString(R.string.app_name)+this.mContext.getString(R.string.mevo_title_nomsg) );    				
    			break;
    			case 1:
        			((Activity) this.mContext).setTitle(mContext.getString(R.string.app_name)+this.mContext.getString(R.string.mevo_title_message) );    				
        		break;
        		default:
        			((Activity) this.mContext).setTitle(mContext.getString(R.string.app_name)+" - "+nb+" "+this.mContext.getString(R.string.mevo_title_messages) );
        		break;
    		}    		
    	}

    	public void setMessageInt(int id, String k, int val)
    	{
    		((Message)this.getItem(id)).setIntValue(k, val, true);
    		notifyDataSetInvalidated();
    	}

    	public void refreshUI()
    	{
    		getMessages();
    		notifyDataSetChanged();
    		notifyDataSetInvalidated();
    	}

    	public void supprMsg(int id)
    	{
    		HttpConnection.deleteMsg(((Message)this.getItem(id)).getStringValue(KEY_NAME));
    	}

    	public void callback(int id)
    	{
    		try
    		{
    			Intent intent = new Intent(Intent.ACTION_CALL);
    			intent.setData(Uri.parse("tel:"+((Message)this.getItem(id)).getStringValue(KEY_SOURCE)));
    			mevoActivity.startActivity(intent);
    		}
    		catch (Exception e)
    		{
    			Log.e(DEBUGTAG,"Impossible de passer l'appel "+((Message)this.getItem(id)).getStringValue(KEY_SOURCE),e);
    		}
    	}

    	public void logMessageInfos(int id)
    	{
			((Message) this.getItem((int) id)).log();
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

			Message curMsg = messages.get(position);
			if (convertView == null)
			{
				holder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater.from(this.mContext);
				convertView = inflater.inflate(R.layout.messages_row, null);
				
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
			
			holder.quand.setText(curMsg.getStringValue(KEY_QUAND));
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
//				convertView.setBackgroundColor(Color.WHITE);
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
					curMsg.setMsgSource("/sdcard/freeboxmobile/mevo/"+curMsg.getStringValue(KEY_NAME));
			}
			return convertView;
		}

		public void stop()
		{
			if (play_current_mp != null)
				play_current_mp.release();
			if (mDbHelper != null)
				mDbHelper.close();
		}

		@Override
    	public void onCompletion(MediaPlayer mp)
		{
    		Log.d(DEBUGTAG,"OnCompletion ! "+this.play_current_pos);
			messages.get(this.play_current_pos).setIntValue(KEY_PLAY_STATUS, PLAY_STATUS_STOP, false);
    		notifyDataSetInvalidated();
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra)
		{
			Log.d(DEBUGTAG,"onERROR "+what+" "+extra);
			play_current_mp.stop();
    		notifyDataSetChanged();
			return true;
		}
		
		public void stopPlay()
		{
			// Si on était pas dans le cas d'un premier play
			if (play_current_mp != null)
			{
				if (play_current_mp.isPlaying())
				{
					Log.d(DEBUGTAG,"Stop current play");
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
			Message m = messages.get(pos);
			Log.d(DEBUGTAG,"onITEMClick v:"+view+" pos:"+pos+" id:"+id+" cp:"+play_current_pos);
			// Si on jouait un message d'une autre ligne,
			// on lance le message de cette ligne à la place
			if ((play_current_pos != pos) || (play_current_mp == null))
			{
				stopPlay();
				try
				{
    				play_current_pos = pos;
					Log.d(DEBUGTAG,"Prepare play "+pos);
					play_current_mp = m.getMP();
					m.setIntValue(KEY_STATUS, MSG_STATUS_LISTENED, true);
					m.setIntValue(KEY_PLAY_STATUS, PLAY_STATUS_PLAY, false);
					play_current_mp.setOnCompletionListener(this);
					play_current_mp.start();
				}
				catch (IllegalStateException e)
				{
					m.setIntValue(KEY_PLAY_STATUS, PLAY_STATUS_STOP, false);
					play_current_mp.stop();
					Log.d(DEBUGTAG,"MEDIAPLAYER : Illegal State Exception "+e);
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
				}
				// Sinon on le reprend
				else
				{
					this.play_current_mp.start();
	        		m.setIntValue(KEY_PLAY_STATUS, PLAY_STATUS_PLAY, false);
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
    }
}
