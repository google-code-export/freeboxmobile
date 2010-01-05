package org.madprod.freeboxmobile.pvr;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import org.madprod.freeboxmobile.HttpConnection;
import org.madprod.freeboxmobile.R;

public class PvrActivity extends TabActivity {
	TabHost mTabHost;
	public static int connectionStatus = HttpConnection.CONNECT_NOT_CONNECTED;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pvr);
        
        connectionStatus = HttpConnection.connectFreeUI();

        mTabHost = getTabHost();
        
        mTabHost.addTab(mTabHost.newTabSpec("tab_enregistrements")
				        		.setIndicator("Enregistrements")
				        		.setContent(new Intent(this, EnregistrementsActivity.class)));
        
        mTabHost.addTab(mTabHost.newTabSpec("tab_programmation")
        						.setIndicator("Programmer")
        						.setContent(new Intent(this, ProgrammationActivity.class)));

        mTabHost.addTab(mTabHost.newTabSpec("tab_grille")
        						.setIndicator("Grille des programmes")
        						.setContent(R.id.textview3));

        mTabHost.setCurrentTab(0);
    }
}