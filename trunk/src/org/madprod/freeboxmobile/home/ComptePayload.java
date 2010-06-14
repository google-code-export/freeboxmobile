package org.madprod.freeboxmobile.home;

import android.app.Activity;
import android.content.ContentValues;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class ComptePayload
{
	public String title;
    public String login;
    public String password;
    public int type;
    public ContentValues result;
    public Long rowid;
    public int exit = Activity.RESULT_CANCELED;
    // refresh est utilisÈ pour savoir s'il s'agit d'une Èdition provenant de CompteEditActivity
    // ou d'un refresh provenant de HomeActivity
    // refresh == true s'il s'agit d'un refresh des donn√©es
    // refresh == false s'il s'agit de la cr√©ation d'un compte
    public boolean refresh;
    
    public ComptePayload(String title, String login, String password, int type, Long rowid, boolean refresh)
    {
    	this.title = title;
    	this.login = login;
    	this.password = password;
    	this.rowid = rowid;
    	this.refresh = refresh;
    	this.type = type;
    }
}