package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class WhatsNewActivity extends Activity
{
	WebView browser;
	
	@Override
	public void onCreate(Bundle icile)
	{
		super.onCreate(icile);
		setContentView(R.layout.home_whatsnew);
		setTitle(getString(R.string.app_name)+" "+getString(R.string.whatsnew_title));
		browser = (WebView)findViewById(R.id.webkit);
		browser.getSettings().setJavaScriptEnabled(true);
		browser.loadUrl("http://code.google.com/p/freeboxmobile/wiki/ChangeLog#"+Utils.getFBMVersion(this));
	}
}
