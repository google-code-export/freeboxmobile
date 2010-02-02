package org.madprod.freeboxmobile.home;

import org.madprod.freeboxmobile.Config;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class HomeActivity extends Activity implements HomeConstants
{
	private Activity homeActivity;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		Log.d(DEBUGTAG,"MainActivity Create "+getString(R.string.app_version));
        super.onCreate(savedInstanceState);

		// On teste si on est dans le cas d'un premier lancement pour cette version de l'appli
		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);

		// Simulate first launch (for test only, these lines have to be commented for release)
/*		Editor e = mgr.edit();
		e.remove(KEY_NRA);
		e.commit();
		e = mgr.edit();
*/
		if (!mgr.getString(KEY_SPLASH, "0").equals(getString(R.string.app_version)))
		{
	        Log.d(DEBUGTAG,Environment.getExternalStorageDirectory().toString()+"/freeboxmobile");
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH, getString(R.string.app_version));
			editor.commit();
			displayAbout();
		}

        homeActivity = this;
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == false)
        	showSdCardError();
		setContentView(R.layout.home_main);
		setTitle(getString(R.string.app_name)+" "+FBMHttpConnection.getTitle());
    }

    @Override
    protected void onStart()
    {
    	Log.d(DEBUGTAG,"MainActivity Start");
    	super.onStart();
    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        if (mgr.getString(KEY_TITLE, "").equals(""))
		{
			showNoCompte();
		}
        
		FBMHttpConnection.initVars(this, null);

        Button phoneButton = (Button) findViewById(R.id.phone);
        Button ligneButton = (Button) findViewById(R.id.ligne);
        Button pvrButton = (Button) findViewById(R.id.magneto);

        if (mgr.getString(KEY_TITLE, "").equals(""))
		{
		}
		else
		{
			phoneButton.setOnClickListener(
					new View.OnClickListener()
					{
						public void onClick(View view)
						{
					    	Intent i = new Intent(homeActivity, org.madprod.freeboxmobile.mvv.MevoActivity.class);
					    	startActivity(i);
						}
					}
				);
			Log.d(DEBUGTAG, "type:"+mgr.getString(KEY_LINETYPE, ""));
			if (mgr.getString(KEY_LINETYPE, "1").equals("1"))
			{
				ligneButton.setOnClickListener(
						new View.OnClickListener()
						{
								public void onClick(View view)
								{
									Intent i = new Intent(homeActivity, org.madprod.freeboxmobile.ligne.LigneInfoActivity.class);
							    	startActivity(i);
								}
						}
					);
			}
			else
			{
				ligneButton.setOnClickListener(
						new View.OnClickListener()
						{
								public void onClick(View view)
								{
									showNonDegroupe();
								}
						}
					);				
			}

			if (mgr.getString(KEY_LINETYPE, "1").equals("1"))
			{
				pvrButton.setOnClickListener(
						new View.OnClickListener()
						{
							public void onClick(View view)
							{
						    	Intent i = new Intent(homeActivity, org.madprod.freeboxmobile.pvr.EnregistrementsActivity.class);
						    	startActivity(i);
							}
						}
					);
			}
			else
			{
				pvrButton.setOnClickListener(
						new View.OnClickListener()
						{
								public void onClick(View view)
								{
									showNonDegroupe();
								}
						}
					);				
			}
		}
		setTitle(getString(R.string.app_name)+" "+FBMHttpConnection.getTitle());
    }
    
    @Override
    protected void onStop()
	{
    	Log.d(DEBUGTAG,"MainActivity Stop");
		super.onStop();
//		this.mDbHelper.close();
	}

    @Override
    protected void onDestroy()
    {
    	Log.d(DEBUGTAG,"MainActivity Destroy");
    	super.onDestroy();
    }

    @Override
    protected void onPause()
    {
		Log.d(DEBUGTAG,"MainActivity Pause");
    	super.onPause();
		FBMHttpConnection.closeDisplay();
    }
    
    @Override
    protected void onResume()
    {
		Log.d(DEBUGTAG,"MainActivity Resume");
    	super.onResume();
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, HOME_OPTION_COMPTES, 0, R.string.home_option_comptes).setIcon(android.R.drawable.ic_menu_myplaces);
        menu.add(0, HOME_OPTION_CONFIG, 1, R.string.home_option_config).setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, HOME_OPTION_SHARE, 2, R.string.home_option_share).setIcon(android.R.drawable.ic_menu_share);
        menu.add(0, HOME_OPTION_VOTE, 3, R.string.home_option_vote).setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, HOME_OPTION_ABOUT, 4, R.string.home_option_about).setIcon(android.R.drawable.ic_menu_info_details);
        menu.add(0, HOME_OPTION_LOG, 4, R.string.home_option_log).setIcon(android.R.drawable.ic_menu_send);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	Intent i;

    	switch (item.getItemId())
    	{
    		case HOME_OPTION_COMPTES:
    			i = new Intent(this, ComptesActivity.class);
		    	startActivityForResult(i, ACTIVITY_COMPTES);
    			return true;
    		case HOME_OPTION_CONFIG:
		    	i = new Intent(this, Config.class);
		    	startActivity(i);
		    	return true;
    		case HOME_OPTION_VOTE:
    			displayVote();
    			return true;
    		case HOME_OPTION_ABOUT:
    			displayAbout();
    			return true;
    		case HOME_OPTION_SHARE:
    			shareApp();
    			return true;
    		case HOME_OPTION_LOG:
    			displayLog();
    			return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void shareApp()
    {
    	SpannableString ss = new SpannableString(getResources().getString(R.string.mail_link));
    	ss.setSpan(new URLSpan(getResources().getString(R.string.app_url)), 0, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    	SpannableStringBuilder ssb = new SpannableStringBuilder(getResources().getString(R.string.mail_text1)).append(' ').append(ss);
    	Intent i = new Intent(Intent.ACTION_SEND)
    		.putExtra(Intent.EXTRA_TEXT, ssb)
    		.putExtra(Intent.EXTRA_SUBJECT, 
    				getString(R.string.mail_subject)) 
    				.setType("message/rfc822");
    	startActivity(Intent.createChooser(i,  "Choisissez votre logiciel de mail")); 
    }
    
    private void displayVote()
    {
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name));
    	d.setMessage(
			"Votez pour soutenir Freebox Mobile !\n\n"+
			"Afin d'améliorer la visibilité sur l'Android Market "+
			"il est important de noter (bien :) ) Freebox Mobile.\n\n"+
			"Après avoir cliqué sur Ok, vous pourrez voter pour Freebox Mobile (les étoiles !) "+
			"et éventuellement mettre un commentaire."
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=org.madprod.freeboxmobile"));
					startActivity(i);
				}
			});
		d.setButton(DialogInterface.BUTTON_NEGATIVE, "Plus tard", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		d.show();
    }

    private void displayLog()
    {
    	ScrollView s = new ScrollView(this);
    	s.setPadding(10,10,10,10);
    	LinearLayout l = new LinearLayout(this);
    	l.setOrientation(LinearLayout.VERTICAL);
    	s.addView(l);
    	TextView tlog = new TextView(this);
    	TextView t = new TextView(this);
    	t.setText("Envoi d'un rapport d'erreur : afin d'aider les développeurs, à leur demande "+
    			"vous pouvez leur envoyer le rapport d'erreur ci-dessous par email.\n"+
    			"Confidentialité : vos mots de passe ne sont pas transmis. "+
    			"Les données transmises sont affichées ci-dessous. Vous pourrez les modifier avant envoi.\n");
    	if (FBMHttpConnection.fbmlog.equals(""))
    	{
        	tlog.setText("Log vide !");    		
    	}
    	else
    	{
    		tlog.setText(FBMHttpConnection.fbmlog);
    	}
    	tlog.setTextColor(0xffffffff);
    	t.setTextColor(0xffffffff);
    	l.addView(t);
    	l.addView(tlog);
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name) + " : Rapport d'erreur");
    	d.setView(s);
    	d.setButton(DialogInterface.BUTTON_POSITIVE, "Envoyer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
		    	SpannableStringBuilder ssb = new SpannableStringBuilder(
		    			getString(R.string.app_name)+" "+
		    			getString(R.string.app_version)+"\n\n"+
		    			FBMHttpConnection.fbmlog);
		    	Intent i = new Intent(Intent.ACTION_SEND)
		    		.putExtra(Intent.EXTRA_EMAIL, new String[]{"freeboxmobile@free.fr"})
		    		.putExtra(Intent.EXTRA_TEXT, ssb)
		    		.putExtra(Intent.EXTRA_SUBJECT, 
		    				getString(R.string.mail_subject)) 
		    				.setType("message/rfc822");
		    	startActivity(Intent.createChooser(i,  "Choisissez votre logiciel de mail")); 
				dialog.dismiss();
			}
		});
    	d.setButton(DialogInterface.BUTTON_NEGATIVE, "Ne pas envoyer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
    	d.show();   	
    }

    private void displayAbout()
    {	
    	AlertDialog d = new AlertDialog.Builder(this).create();
    	ScrollView s = new ScrollView(this);
    	TextView t = new TextView(this);
    	t.setLinksClickable(true);
    	t.setAutoLinkMask(Linkify.ALL);
    	t.setText(			"Freebox Mobile est une application indépendante de Free.\n\n"+
			"Site web :\nhttp://freeboxmobile.googlecode.com\n\n"+
			"Contact :\nfreeboxmobile@free.fr\n\n"+
			"Version : "+getString(R.string.app_version)+"\n\n"+
			"Facebook :\nhttp://www.facebook.com/search/?q=freeboxmobile\n\n"+
			"Auteurs :\n"+
			"- Olivier Rosello : Architecture / Réseau / Home / Info ADSL / Téléphone\n"+
			"- Benoit Duffez : Magnétosocope\n\n"+
			"Cette application opensource utilise :\n"+
			"- Android-XMLRPC : http://code.google.com/p/android-xmlrpc/\n"+
			"- Icônes Tango : http://tango.freedesktop.org/\n" +
			"- Frimousse : http://www.frimousse.org\n"
			);
    	s.setPadding(10,10,10,10);
    	t.setMovementMethod(LinkMovementMethod.getInstance());
    	t.setTextColor(0xffffffff);
    	t.setLinkTextColor(0xffffffff);
    	t.setTextSize(16);
    	s.addView(t);
    	d.setView(s);
		d.setTitle(getString(R.string.app_name));
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			});
    	d.setButton(DialogInterface.BUTTON_NEUTRAL, "Nouveautés", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				Intent i = new Intent(homeActivity, org.madprod.freeboxmobile.home.WhatsNewActivity.class);
		    	startActivity(i);
			}
		});
		d.show();
    }

    private void showNonDegroupe()
    {
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name));
		d.setMessage(
			"Cette fonctionnalité n'est accessible qu'aux abonnés dégroupés."
		);

		d.setButton(DialogInterface.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		d.show();      
    }

    private void showNoCompte()
    {
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("Pas de compte configuré");
		d.setMessage(
			"Veuillez configurer au moins un compte pour pouvoir utiliser cette application.\n\n"+
			"Vous pouvez configurer des comptes en utilisant la touche MENU sur la page d'accueil "+
			"ou en utilisant le bouton ci-dessous."
		);

		d.setButton(DialogInterface.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
		    	startActivityForResult(new Intent(homeActivity, org.madprod.freeboxmobile.home.ComptesActivity.class), ACTIVITY_COMPTES);
			}
		});
		d.show();      
    }
    
    private void showSdCardError()
    {
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("SDCard Necessaire !");
		d.setMessage(
			"Une carte SD est nécessaire afin d'utiliser "+
			"cette application."
		);
		d.setButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
				}
			}
		);
		d.show();      
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode)
        {
        	case ACTIVITY_COMPTES:
            	if (FBMHttpConnection.checkUpdated(
            			getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null),
            			getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_PASSWORD, null)
            			))
            	{
            		FBMHttpConnection.initVars(homeActivity, null);
//            		new ConnectFree().execute();
            	}
        		break;
        }
    }
}