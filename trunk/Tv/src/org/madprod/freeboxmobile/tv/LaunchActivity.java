package org.madprod.freeboxmobile.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LaunchActivity extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent i = new Intent();
		i.setClass(this, MainActivity.class);
		startActivity(i);
		finish();
	}
}
