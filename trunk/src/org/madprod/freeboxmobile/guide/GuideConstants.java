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
    static final int GUIDE_OPTION_REFRESH = Menu.FIRST;
    static final int GUIDE_OPTION_SELECT = Menu.FIRST + 1;
    
    static final int GUIDE_CONTEXT_ENREGISTRER = Menu.FIRST;
    static final int GUIDE_CONTEXT_DETAILS = Menu.FIRST + 1;
    
    static final String DIR_CHAINES = "chaines/";
    static final String IMAGES_URL = "http://adsl.free.fr/im/chaines/";
}
