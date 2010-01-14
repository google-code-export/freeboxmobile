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
    
    static final int HOME_OPTION_COMPTES = Menu.FIRST;
    static final int HOME_OPTION_CONFIG = Menu.FIRST + 1;
    static final int HOME_OPTION_SHARE = Menu.FIRST + 2;

    // Pour la gestion des comptes
    // DB
    public static final String KEY_ROWID = "_id";
    
    // MENUS
    static final int COMPTES_OPTION_NEW = Menu.FIRST;
    static final int COMPTES_OPTION_DELETE = Menu.FIRST + 1;
}
