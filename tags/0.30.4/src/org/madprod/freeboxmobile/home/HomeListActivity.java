package org.madprod.freeboxmobile.home;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.madprod.freeboxmobile.Config;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.fax.FaxActivity;
import org.madprod.freeboxmobile.ligne.InfoAdslCheck;
import org.madprod.freeboxmobile.mvv.MevoSync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class HomeListActivity extends ListActivity implements HomeConstants
{
	private Activity homeActivity;
	private List< Map<String,Object> > modulesList;
	private AsyncTask<ComptePayload, Void, ComptePayload> task = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		FBMHttpConnection.FBMLog("MainActivity Create "+getString(R.string.app_version)+"\n"+new Date().toString());
        super.onCreate(savedInstanceState);

		// On teste si on est dans le cas d'un premier lancement pour cette version de l'appli
       	modulesList = new ArrayList< Map<String,Object> >();
       	setModules();

		// Simulate first launch (for test only, these lines have to be commented for release)
/*		SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		Editor e = mgr.edit();
		e.remove(KEY_NRA);
		e.commit();
		e = mgr.edit();
*/
        homeActivity = this;
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == false)
        	showSdCardError();
		setContentView(R.layout.home_main_list);
		setTitle(getString(R.string.app_name)+" "+FBMHttpConnection.getTitle());
    }

    @Override
    protected void onStart()
    {
    	FBMHttpConnection.FBMLog("MainActivity Start");
    	super.onStart();
    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		FBMHttpConnection.initVars(this, null);
		// Si l'utilisateur n'a pas configuré de compte
        if (mgr.getString(KEY_TITLE, "").equals(""))
		{
			showNoCompte();
		}
        else
        {
        	// S'il s'agit du premier lancement de cette version, on rafraichi pas mal d'infos
        	if (!mgr.getString(KEY_FBMVERSION, "0").equals(getString(R.string.app_version)))
        	{
        		FBMHttpConnection.FBMLog("HOME : on rafraichi le compte "+mgr.getString(KEY_FBMVERSION, "0"));
        		refreshCompte();
        		Editor editor = mgr.edit();
				editor.putString(KEY_FBMVERSION, getString(R.string.app_version));
				editor.commit();
        	}
        }
		// Si on est sur un premier lancement de la nouvelle version :
		if (!mgr.getString(KEY_SPLASH, "0").equals(getString(R.string.app_version)))
		{
			Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH, getString(R.string.app_version));
			editor.commit();
			File log = new File(Environment.getExternalStorageDirectory()+DIR_FBM, file_log);
			log.delete();

			// On reinstalle les timers de notif si nécessaire
			String ms = mgr.getString(KEY_MEVO_PREFS_FREQ, "0");
			if (!ms.equals("0"))
			{
				MevoSync.changeTimer(Integer.decode(ms), this);
			}
			ms = mgr.getString(KEY_INFOADSL_PREFS_FREQ, "0");
			if (!ms.equals("0"))
			{
				InfoAdslCheck.changeTimer(Integer.decode(ms), this);
			}
			displayAbout();
		}

        FBMHttpConnection.FBMLog("type:"+mgr.getString(KEY_LINETYPE, ""));
    }

    @Override
    protected void onStop()
	{
    	FBMHttpConnection.FBMLog("MainActivity Stop");
		super.onStop();
	}

    @Override
    protected void onDestroy()
    {
    	FBMHttpConnection.FBMLog("MainActivity Destroy");
    	super.onDestroy();
    }

    @Override
    protected void onPause()
    {
		FBMHttpConnection.FBMLog("MainActivity Pause");
    	super.onPause();
		FBMHttpConnection.closeDisplay();
    }

    private void refreshCompte()
    {
    	if ((task != null) && (task.getStatus() == AsyncTask.Status.FINISHED) || (task == null))
    	{
	    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
	        task = new ManageCompte(this).execute(new ComptePayload(
	        		mgr.getString(KEY_TITLE, ""),
	        		mgr.getString(KEY_USER, ""),
	        		mgr.getString(KEY_PASSWORD, ""),
	        		null, true));
    	}
    }

    private void setModules()
    {
    	Map<String,Object> map;

		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_guide_tv);
		map.put(M_TITRE, getString(R.string.buttonGuide));
		map.put(M_DESC, "Consultez le guide TV, programmez des enregistrements");
		map.put(M_CLASS, org.madprod.freeboxmobile.guide.GuideActivity.class);
		modulesList.add(map);    	
    	map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_magnetoscope);
		map.put(M_TITRE, getString(R.string.buttonPvr));
		map.put(M_DESC, "Programmez votre Freebox à distance");
		map.put(M_CLASS, org.madprod.freeboxmobile.pvr.EnregistrementsActivity.class);
		modulesList.add(map);
		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_repondeur);
		map.put(M_TITRE, getString(R.string.buttonMevo));
		map.put(M_DESC, "Accédez à la messagerie vocale de votre Freebox");
		map.put(M_CLASS, org.madprod.freeboxmobile.mvv.MevoActivity.class);
		modulesList.add(map);
		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_infos_adsl);
		map.put(M_TITRE, getString(R.string.buttonLigne));
		map.put(M_DESC, "Consultez l'état de votre ligne ADSL");
		map.put(M_CLASS, org.madprod.freeboxmobile.ligne.LigneInfoActivity.class);
		modulesList.add(map);
		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_telecopie);
		map.put(M_TITRE, getString(R.string.buttonFax));
		map.put(M_DESC, "Utilisez votre compte Freebox pour envoyer des Fax à partir de votre mobile\n\nCette fonctionnalité sera bientot disponible");
		map.put(M_CLASS, null);
//		map.put(M_CLASS, FaxActivity.class);
		modulesList.add(map);
		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_actus_freenautes);
		map.put(M_TITRE, getString(R.string.buttonActu));
		map.put(M_DESC, "Consultez l'actualité de Free et de la Freebox\n\nCette fonctionnalité n'est pas encore disponible");
		map.put(M_CLASS, null);
		modulesList.add(map);    	
    	map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_television);
		map.put(M_TITRE, getString(R.string.buttonTv));
		map.put(M_DESC, "Regardez vos chaînes de TV Freebox\n\nCette fonctionnalité n'est pas encore disponible");
		map.put(M_CLASS, null);
		modulesList.add(map);
    	map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_radios);
		map.put(M_TITRE, getString(R.string.buttonRadios));
		map.put(M_DESC, "Ecoutez les radios Freebox\n\nCette fonctionnalité n'est pas encore disponible");
		map.put(M_CLASS, null);
		modulesList.add(map);
		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.icon_fbm);
		map.put(M_TITRE, "Medias");
		map.put(M_DESC, "Accédez aux vidéos, enregistrements et musiques qui sont chez vous\n\nCette fonctionnalité n'est pas encore disponible");
		map.put(M_CLASS, null);
		modulesList.add(map);
    }

    @Override
    protected void onResume()
    {
		FBMHttpConnection.FBMLog("MainActivity Resume");
    	super.onResume();
    	
        SimpleAdapter mList = new SimpleAdapter(this, modulesList, R.layout.home_main_list_row, new String[] {M_ICON, M_TITRE, M_DESC}, new int[] {R.id.home_main_row_img, R.id.home_main_row_titre, R.id.home_main_row_desc});
        setListAdapter(mList);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        String moduleName = (String) modulesList.get((int) id).get(M_TITRE);
        Class<?> moduleClass = (Class<?>) modulesList.get((int) id).get(M_CLASS);
    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);

        if (mgr.getString(KEY_TITLE, "").equals(""))
		{
        	showNoCompte();
		}
        else
        {
        	FBMHttpConnection.FBMLog("LINE TYPE:"+mgr.getString(KEY_LINETYPE, "-1"));
	    	if (moduleName.equals(getString(R.string.buttonLigne)) &&
    			!mgr.getString(KEY_LINETYPE, "1").equals("0") &&
    			!mgr.getString(KEY_LINETYPE, "1").equals("1"))
	        {
	        	showNonADSL();
	        	return;
	        }
        }
    	if (moduleClass != null)
    	{
    		startActivity(new Intent(homeActivity, moduleClass));
    	}
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        super.onCreateOptionsMenu(menu);

        menu.add(0, HOME_OPTION_COMPTES, 0, R.string.home_option_comptes).setIcon(android.R.drawable.ic_menu_myplaces);
        menu.add(0, HOME_OPTION_CONFIG, 1, R.string.home_option_config).setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, HOME_OPTION_SHARE, 4, R.string.home_option_share).setIcon(android.R.drawable.ic_menu_share);
        menu.add(0, HOME_OPTION_VOTE, 3, R.string.home_option_vote).setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, HOME_OPTION_ABOUT, 9, R.string.home_option_about).setIcon(android.R.drawable.ic_menu_info_details);
        menu.add(0, HOME_OPTION_LOG, 8, R.string.home_option_log).setIcon(android.R.drawable.ic_menu_send);
        menu.add(0, HOME_OPTION_REFRESH, 2, R.string.home_option_refresh).setIcon(android.R.drawable.ic_menu_rotate);
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
    		case HOME_OPTION_REFRESH:
    			refreshCompte();
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
		d.setIcon(R.drawable.icon_fbm_reverse);
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
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=org.madprod.freeboxmobile")));
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
    			"vous pouvez leur envoyer le rapport d'erreur ci-dessous par email.\n\n"+
    			"Confidentialité : vos mots de passe ne sont pas transmis.\n"+
    			"Vous pourrez voir et modifier les données transmises avant envoi.\n");
    	tlog.setTextColor(0xffffffff);
    	t.setTextColor(0xffffffff);
    	l.addView(t);
    	l.addView(tlog);
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name) + " : Rapport d'erreur");
		d.setIcon(R.drawable.icon_fbm_reverse);
    	d.setView(s);
    	d.setButton(DialogInterface.BUTTON_NEUTRAL, "Continuer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
		    	startActivity(new Intent(homeActivity, SendlogActivity.class));
			}
		});
    	d.setButton(DialogInterface.BUTTON_POSITIVE, "Annuler", new DialogInterface.OnClickListener()
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
    	t.setText("Freebox Mobile est une application indépendante de Free.\n\n"+
			"Site web :\nhttp://www.freeboxmobile.org\n\n"+
			"Contact :\ncontact@freeboxmobile.org\n\n"+
			"Version : "+getString(R.string.app_version)+"\n\n"+
			"Facebook (devenez fan !) :\nhttp://www.facebook.com/search/?q=freeboxmobile\n\n"+
			"Auteurs :\n"+
			"- Olivier Rosello : Architecture / Réseau / Home / Info ADSL / Téléphone / Guide des Programmes\n"+
			"- Benoit Duffez : Magnétosocope\n"+
			"- Ludovic Meurillon : Fax\n"+
			"- Alban Pelé : Icônes de la page d'accueil\n"+
			"\n"+
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
		d.setIcon(R.drawable.icon_fbm_reverse);
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
		    	startActivity(new Intent(homeActivity, org.madprod.freeboxmobile.home.WhatsNewActivity.class));
			}
		});
		d.show();
    }

    private void showNonADSL()
    {
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name));
		d.setIcon(R.drawable.icon_fbm_reverse);
		d.setMessage(
			"Cette fonctionnalité n'est accessible qu'aux abonnés ADSL.\n\n"+
			"Etes-vous un chanceux en Fibre Optique ? :)\n"
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

    private void showNonDegroupe()
    {
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setIcon(R.drawable.icon_fbm_reverse);
		d.setTitle(getString(R.string.app_name));
		d.setMessage(
			"Cette fonctionnalité n'est accessible qu'aux abonnés dégroupé.\n"
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
		d.setIcon(R.drawable.icon_fbm_reverse);
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
		d.setIcon(R.drawable.icon_fbm_reverse);
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
            	}
        		break;
        }
    }
}
