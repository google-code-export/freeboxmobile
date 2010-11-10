package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.Constants;

import android.view.Menu;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public interface HomeConstants extends Constants
{
    static final String KEY_SPLASH		= "splashscreen";
    static final String KEY_CODE		= "fbm_code"; // Version code de l'application (cf manifest)
    static final String KEY_LAST_LAUNCH = "last_launch";
    
    static final int HOME_OPTION_COMPTES = Menu.FIRST;
    static final int HOME_OPTION_CONFIG = Menu.FIRST + 1;
    static final int HOME_OPTION_SHARE = Menu.FIRST + 2;
    static final int HOME_OPTION_VOTE  = Menu.FIRST + 3;
    static final int HOME_OPTION_ABOUT = Menu.FIRST + 4;
    static final int HOME_OPTION_LOG = Menu.FIRST + 5;
    static final int HOME_OPTION_REFRESH = Menu.FIRST + 6;

    // MENUS
    static final int COMPTES_OPTION_NEW = Menu.FIRST;
    static final int COMPTES_OPTION_DELETE = Menu.FIRST + 1;
    static final int COMPTES_OPTION_MODIFY = Menu.FIRST + 2;
    
    static final String M_TITRE = "titre";
    static final String M_DESC = "desc";
    static final String M_ICON = "icon";
    static final String M_CLASS = "class";
}
