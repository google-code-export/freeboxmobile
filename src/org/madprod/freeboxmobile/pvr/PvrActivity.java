package org.madprod.freeboxmobile.pvr;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import org.madprod.freeboxmobile.HttpConnection;
import org.madprod.freeboxmobile.R;

public class PvrActivity extends TabActivity {
	TabHost mTabHost;
	public static int connectionStatus = HttpConnection.CONNECT_NOT_CONNECTED;
	public static PvrActivity activity = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pvr);
        
        activity = this;
        
        connectionStatus = HttpConnection.connectFreeUI();
        setTitle(getString(R.string.app_name) + " - Magnétoscope numérique");

        mTabHost = getTabHost();
        ImageView tab1, tab2, tab3;

        tab1 = new ImageView(this);
        tab1.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_view));
        tab2 = new ImageView(this);
        tab2.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_add));
        tab3 = new ImageView(this);
        tab3.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
        
        mTabHost.addTab(mTabHost.newTabSpec("tab_enregistrements")
				        		.setIndicator(tab1)
				        		.setContent(new Intent(this, EnregistrementsActivity.class)));
        
        mTabHost.addTab(mTabHost.newTabSpec("tab_programmation")
        						.setIndicator(tab2)
        						.setContent(new Intent(this, ProgrammationActivity.class)));

        mTabHost.addTab(mTabHost.newTabSpec("tab_grille")
        						.setIndicator(tab3)
        						.setContent(R.id.textview3));

        mTabHost.setCurrentTab(0);
    }
    
    public void goFirstTab() {
    	getTabHost().setCurrentTab(0);
    }
}