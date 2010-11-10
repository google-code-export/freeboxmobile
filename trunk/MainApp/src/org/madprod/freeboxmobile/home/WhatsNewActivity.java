package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.Constants;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class WhatsNewActivity extends Activity implements Constants
{
	GoogleAnalyticsTracker tracker;
	
	@Override
	public void onCreate(Bundle icile)
	{
		super.onCreate(icile);
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Home/WhatsNew");

		setContentView(R.layout.home_whatsnew);
		setTitle(getString(R.string.app_name)+" "+getString(R.string.whatsnew_title));
		WebView browser = (WebView)findViewById(R.id.webkit);
		browser.getSettings().setJavaScriptEnabled(true);
		browser.loadUrl("http://code.google.com/p/freeboxmobile/wiki/ChangeLog#"+Utils.getFBMVersion(this));
	}

    @Override
    protected void onDestroy()
    {
    	tracker.stop();
    	super.onDestroy();
    }
}
