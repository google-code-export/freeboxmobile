package org.madprod.freeboxmobile.tv;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public interface TvConstants
{
    static final String KEY_SPLASH_TV = "splashscreen_tv";
    static final String KEY_PREFS_VERSION = "prefs_tv";
    
    static final String ANALYTICS_MAIN_TRACKER = "UA-9016955-4";
    static final String KEY_PREFS		= "freeboxmobiletv";
    static final String TAG				= "FBMTV";
    static final String[] listStreamsKeys={"fav1", "fav2", "fav3"};
    static final int[] defaultValues = {Chaine.STREAM_TYPE_MULTIPOSTE_TNTSD, Chaine.STREAM_TYPE_MULTIPOSTE_SD, Chaine.STREAM_TYPE_INTERNET};
}
