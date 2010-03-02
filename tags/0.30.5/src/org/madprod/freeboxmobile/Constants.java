package org.madprod.freeboxmobile;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public interface Constants
{
    static final int ACTIVITY_COMPTES = 0;

    // For database & prefs
    static final String KEY_ROWID = "_id";
    static final String KEY_USER		= "user";
    static final String KEY_PASSWORD	= "password";
    static final String KEY_TITLE		= "title";
    static final String KEY_NRA			= "nra";
    static final String KEY_DSLAM		= "dslam";
    static final String KEY_IP			= "ip";
    static final String KEY_TEL			= "tel";
    static final String KEY_LINELENGTH	= "length";
    static final String KEY_ATTN		= "attn";
    static final String KEY_LINETYPE	= "linetype"; // 0 = non dégroupé / 1 = dégroupé
    static final String KEY_FBMVERSION	= "fbmversion"; // version de FBM qui a généré les infos du compte

    static final String KEY_LAST_REFRESH = "lastrefresh_";

    static final String KEY_PREFS		= "freeboxmobile";
    static final String KEY_MEVO_PREFS_FREQ	= "mevo_freq";
    static final String KEY_INFOADSL_PREFS_FREQ	= "infoadsl_freq";
    static final String KEY_LAST_DSLAM_CHECK = "last_dslam_check";

	static final String TAG		= "FBM";
	static final String DIR_FBM			= "/freeboxmobile/";

    public static final int CONNECT_LOGIN_FAILED = -1;
    public static final int CONNECT_NOT_CONNECTED = 0;
    public static final int CONNECT_CONNECTED = 1;
    
    static final String file_log = "fbm.log";

    // Pour Notification Manager
	public final int NOTIF_MEVO = 1;
	public final int NOTIF_INFOADSL = 2;
}
