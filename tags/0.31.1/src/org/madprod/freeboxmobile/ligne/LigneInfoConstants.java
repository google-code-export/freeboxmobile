package org.madprod.freeboxmobile.ligne;

import org.madprod.freeboxmobile.Constants;

import android.view.Menu;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public interface LigneInfoConstants extends Constants
{
	static final String KEY_TICKETID	= "ticketid";
	static final String KEY_TITLE		= "title";
    static final String KEY_DESC		= "desc";
    static final String KEY_START		= "start";
    static final String KEY_END			= "end";
    
    static final int LIGNEINFO_OPTION_REFRESH = Menu.FIRST + 1;
}
