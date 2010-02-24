package org.madprod.freeboxmobile.guide;

import org.madprod.freeboxmobile.Constants;

import android.view.Menu;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public interface GuideConstants extends Constants
{
	static final String KEY_SPLASH_GUIDE = "splashscreen_guide";
	
    static final int GUIDE_OPTION_REFRESH = Menu.FIRST;
    static final int GUIDE_OPTION_SELECT = Menu.FIRST + 1;
    
    static final int GUIDE_CONTEXT_ENREGISTRER = Menu.FIRST;
    static final int GUIDE_CONTEXT_DETAILS = Menu.FIRST + 1;
    
    static final String DIR_CHAINES = "chaines/";
    static final String IMAGES_URL = "http://adsl.free.fr/im/chaines/";
    static final String MAGNETO_URL = "http://adsl.free.fr/admin/magneto.pl";
    
    static final int FAVORIS_COMMAND_NONE = 0;
    static final int FAVORIS_COMMAND_RESET = 1;
    static final int FAVORIS_COMMAND_ADD = 2;
    static final int FAVORIS_COMMAND_SUPPR = 3;
}
