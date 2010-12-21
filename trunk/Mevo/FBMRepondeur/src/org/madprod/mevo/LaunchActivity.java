package org.madprod.mevo;





import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LaunchActivity extends Activity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Intent i = new Intent();
		i.setClass(this, HomeActivity.class);
//			new Intent("android.intent.action.MAIN");
//		i.addCategory("android.intent.category.INFO");
//		i.setPackage("org.madprod.infofreenautes");
		startActivity(i);
		finish();

	}


}




