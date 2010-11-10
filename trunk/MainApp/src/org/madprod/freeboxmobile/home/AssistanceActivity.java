package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class AssistanceActivity extends Activity
{
	@Override
	public void onCreate(Bundle icile)
	{
		WebView viewAssistance;
		super.onCreate(icile);
		setContentView(R.layout.home_whatsnew);
		setTitle(getString(R.string.app_name)+" "+getString(R.string.whatsnew_title));
		viewAssistance = (WebView)findViewById(R.id.webkit);
		viewAssistance.getSettings().setJavaScriptEnabled(true);
		byte[] postData= null;
		viewAssistance.postUrl("https://assistance.free.fr/compte/auth_i.php", postData);
	}
}
