package org.madprod.freeboxmobile.mvv;

import org.madprod.freeboxmobile.Constants;

import android.view.Menu;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public interface MevoConstants extends Constants
{
    static final String KEY_SPLASH_MEVO	= "splashscreen_mevo";
	static final String DIR_MEVO		= DIR_FBM+"/mevo";

	static final int MSG_STATUS_UNLISTENED = 0;
	static final int MSG_STATUS_LISTENED = 1;

	// Chaîne affichée par Free sur la page mevo en cas de nouveau message
	static final String STR_NEWMESSAGE  = "Nouveau message";

	// Menu contextuel
	static final int MEVO_CONTEXT_CALLBACK    	= 1;
	static final int MEVO_CONTEXT_VIEWDETAILS	= 2;
	static final int MEVO_CONTEXT_DELETE		= 3;
	static final int MEVO_CONTEXT_DELETEFRMSRV	= 4;
	static final int MEVO_CONTEXT_SEND			= 5;
	static final int MEVO_CONTEXT_MARKUNREAD    = 6;
	static final int MEVO_CONTEXT_MARKREAD      = 7;

    static final int MEVO_OPTION_CONFIG = Menu.FIRST;
    static final int MEVO_OPTION_REFRESH = Menu.FIRST + 1;

    // db
	// Status of the message : unread = 0 / read = 1
	public static final String KEY_STATUS = "status";
	// Existance of the message : none = 0 / serveur = 1 / local = 2 / both = 4
	public static final String KEY_PRESENCE = "presence";
	// Caller phone number
	public static final String KEY_SOURCE = "source";
	// Date time of the message
	public static final String KEY_QUAND = "quand";
	// Length of the message (in seconds)
	public static final String KEY_LENGTH = "length";
	// Link to the file of the message on adsl.free.fr
	public static final String KEY_LINK = "link";
	// Link to delete the message on the server
	public static final String KEY_DEL = "del";
	// name of the file (on the server and in local)
	public static final String KEY_NAME = "name";
	public static final String KEY_ROWID = "_id";

	// utilisé dans MevoMessage.java
	public static final String KEY_CALLER = "caller";
	public static final String KEY_PLAY_STATUS = "img_status";
	public static final String KEY_NB_TYPE = "nb_type";
	public static final String KEY_QUAND_HR = "quand_hr";
	public static final int PLAY_STATUS_STOP = 0;
	public static final int PLAY_STATUS_PLAY = 1;
	public static final int PLAY_STATUS_PAUSE = 2;
	
	public final int NOTIF_MEVO = 1;
	
}
