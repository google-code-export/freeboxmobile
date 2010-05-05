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
    static final String OLDDIR_CHAINES = "chaines/";
    static final String DIR_CHAINES = ".chaines/";

    static final String MAGNETO_URL = "https://adsls.free.fr/admin/magneto.pl";

    public static final int CONNECT_LOGIN_FAILED = -1;
    public static final int CONNECT_NOT_CONNECTED = 0;
    public static final int CONNECT_CONNECTED = 1;
    
    static final String file_log = "fbm.log";

    // Pour Notification Manager
	public final int NOTIF_MEVO = 1;
	public final int NOTIF_INFOADSL = 2;
	
	public final String jours[] = {"", "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
	public final String mois[] = {"janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre"};
	
    // Types de comptes - doit respecter l'ordre du nom des comptes définis dans array.xml
	// Utile uniquement pour le spinner de la creation d'un nouveau compte
    static final int COMPTES_TYPE_ADSL = 0;
    static final int COMPTES_TYPE_FO = 1;
    
    // Types de lignes,+ précis que le type de compte. C'est ce qui est stocké et utilisé
    static final String LINE_TYPE_FBXIPADSL = "0";
    static final String LINE_TYPE_FBXDEGROUPE = "1";
    static final String LINE_TYPE_FBXOPTIQUE = "2";
}
