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
    public ContentValues result;
    public Long rowid;
    public int exit = Activity.RESULT_CANCELED;
    // refresh est utilisé pour savoir s'il s'agit d'une édition provenant de CompteEditActivity
    // ou d'un refresh provenant de HomeActivity
    public boolean refresh;
    
    public ComptePayload(String title, String login, String password, Long rowid, boolean refresh)
    {
    	this.title = title;
    	this.login = login;
    	this.password = password;
    	this.rowid = rowid;
    	this.refresh = refresh;
    }
}