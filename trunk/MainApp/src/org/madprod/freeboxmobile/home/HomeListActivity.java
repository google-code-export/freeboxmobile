package org.madprod.freeboxmobile.home;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.madprod.freeboxmobile.Config;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;
import org.madprod.freeboxmobile.Utils;
import org.madprod.freeboxmobile.fax.FaxActivity;
import org.madprod.freeboxmobile.guide.GuideMenuActivity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class HomeListActivity extends ListActivity implements HomeConstants
{
	private List< Map<String,Object> > modulesList;
	private AsyncTask<Void, Integer, Integer> task = null;
	GoogleAnalyticsTracker tracker;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		FBMHttpConnection.initVars(this, null);
		Log.d(TAG,"MainActivity Create "+Utils.getFBMVersion(this)+"\n"+new Date().toString());

		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, this);
		tracker.trackPageView("Home/Home");
		
		// TESTS POUR TROUVER OU EST LE BUG HTTPS CHEZ FREE
		/*
		List<NameValuePair> postVars = new ArrayList<NameValuePair>();
		String mUrl;
		String resultat;
		postVars.add(new BasicNameValuePair("detail", "0"));
		postVars.add(new BasicNameValuePair("box", "0"));
        mUrl = "https://www.google.com/analytics/reporting/login?ctu=https://www.google.com/analytics/settings/%3Fet%3Dreset%26hl%3Dfr";
        resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(mUrl, postVars, true, true, "UTF8"));
        Log.d(TAG, "RESULTAT G 1:"+resultat.length());
        resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(mUrl, postVars, true, true, "UTF8"));
        Log.d(TAG, "RESULTAT G 2:"+resultat.length());
        resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(mUrl, postVars, true, true, "UTF8"));
        Log.d(TAG, "RESULTAT G 3:"+resultat.length());
        resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(mUrl, postVars, true, true, "UTF8"));
        Log.d(TAG, "RESULTAT G 4:"+resultat.length());
        mUrl = "https://adsls.free.fr/admin/magneto.pl";
        resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(mUrl, postVars, true, true, "UTF8"));
        Log.d(TAG, "RESULTAT F 1:"+resultat.length());
        resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(mUrl, postVars, true, true, "UTF8"));
        Log.d(TAG, "RESULTAT F 2:"+resultat.length());
        resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(mUrl, postVars, true, true, "UTF8"));
        Log.d(TAG, "RESULTAT F 3:"+resultat.length());
        resultat = FBMHttpConnection.getPage(FBMHttpConnection.getAuthRequest(mUrl, postVars, true, true, "UTF8"));
        Log.d(TAG, "RESULTAT F 4:"+resultat.length());
        */
		
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
		setContentView(R.layout.home_main_list);
		setTitle(getString(R.string.app_name)+" "+FBMHttpConnection.getTitle());
	}

    @Override
    protected void onStart()
    {
    	Log.i(TAG,"MainActivity Start");
    	super.onStart();
    	
    	FBMNetTask.register(this);
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == false)
        	showSdCardError();
    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);

    	// Si on a lancé la dernière fois il y a + de 24 heures (ou si c'est une nouvelle version)
    	DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    	String date = format.format(new Date());
        if ((!(mgr.getString(KEY_LAST_LAUNCH, "").equals(date))) || (!(mgr.getString(KEY_SPLASH, "0").equals(Utils.getFBMVersion(this)))))
		{
        	Log.d(TAG, "VERIF NEW VERSION");
    		Editor editor = mgr.edit();
			editor.putString(KEY_LAST_LAUNCH, date);
        	FBMHttpConnection.checkVersion();
        	editor.commit();
		}

    	// Si l'utilisateur n'a pas configuré de compte
        if (mgr.getString(KEY_TITLE, "").equals(""))
		{
			showNoCompte();
		}
        else
        {
    		// Si on est sur une version trop ancienne
            // on rafraichi pas mal d'infos
    		if (mgr.getInt(KEY_CODE, 0) < 41)
        	{
        		Log.d(TAG,"HOME : on rafraichi le compte "+mgr.getString(KEY_FBMVERSION, "0"));
        		refreshCompte();
        		Editor editor = mgr.edit();
				editor.putString(KEY_FBMVERSION, Utils.getFBMVersion(this));
				editor.putInt(KEY_CODE, Utils.getFBMCode(this));
				editor.commit();
        	}
        }
        // Si on est sur un premier lancement de la nouvelle version :
		if (!mgr.getString(KEY_SPLASH, "0").equals(Utils.getFBMVersion(this)))
		{
			// Si on avait l'ancienne structure pour stocker les logos des chaînes, on migre :
			File of = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+OLDDIR_CHAINES);
        	File file = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES);
	        if (of.exists())
	        {
	        	Log.i(TAG,"Ancien dossier des chaînes, on renomme "+OLDDIR_CHAINES+" en "+DIR_CHAINES);
	        	if (of.renameTo(file))
	        	{
	        		Log.i(TAG,"rename ok");
	        	}
	        	else
	        	{
	        		Log.i(TAG,"rename notok");
	        	}
	        }
	        // Si le dossier des chaînes n'existe pas, on le créé
        	if (!file.exists())
        	{
        		Log.i(TAG, "Création du dossier des chaînes : "+DIR_CHAINES);
        		file.mkdirs();
        	}
        	// Si on a pas de .nomedia dedans, on en met un (pour ne pas que les logos des chaînes apparaissent dans la gallerie photo)
        	file = new File(Environment.getExternalStorageDirectory().toString()+DIR_FBM+DIR_CHAINES+".nomedia");
        	if (!file.exists())
	        {
    			Log.i(TAG, "Création du .nomedia");
        		try
        		{
        			Log.i(TAG, "ok");
        			file.createNewFile();
        		}
        		catch(IOException e)
        		{
        			Log.e(TAG, "Echec de la creation du .nomedia "+e.getMessage());
        		}
	        }

	        Editor editor = mgr.edit();
			editor.putString(KEY_SPLASH, Utils.getFBMVersion(this));
			editor.putInt(KEY_CODE, Utils.getFBMCode(this));
			editor.commit();
			// On supprime le log que si on est pas sur une beta
			if (Utils.getFBMVersion(this).contains("rc") == false)
			{
				File log = new File(Environment.getExternalStorageDirectory()+DIR_FBM, file_log);
				log.delete();
			}
			displayAbout();
		}
        Log.d(TAG,"type:"+mgr.getString(KEY_LINETYPE, ""));
    }

    @Override
    protected void onStop()
	{
    	Log.i(TAG,"MainActivity Stop");
		super.onStop();
	}

    @Override
    protected void onDestroy()
    {
    	Log.i(TAG,"MainActivity Destroy");
		FBMNetTask.unregister(this);
    	super.onDestroy();
    	tracker.stop();
    }

    @Override
    protected void onResume()
    {
		Log.i(TAG,"MainActivity Resume");
    	super.onResume();
        SimpleAdapter mList = new SimpleAdapter(this, modulesList, R.layout.home_main_list_row, new String[] {M_ICON, M_TITRE, M_DESC}, new int[] {R.id.home_main_row_img, R.id.home_main_row_titre, R.id.home_main_row_desc});
        setListAdapter(mList);
    }
    
    @Override
    protected void onPause()
    {
		Log.i(TAG,"MainActivity Pause");
    	super.onPause();
    }

    private void refreshCompte()
    {
    	if ((task != null) && (task.getStatus() == AsyncTask.Status.FINISHED) || (task == null))
    	{
	    	SharedPreferences mgr = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
	        task = new ManageCompte(new ComptePayload(
	        		mgr.getString(KEY_TITLE, ""),
	        		mgr.getString(KEY_USER, ""),
	        		mgr.getString(KEY_PASSWORD, ""),
	        		(mgr.getString(KEY_LINETYPE, "0").equals(LINE_TYPE_FBXOPTIQUE) ? COMPTES_TYPE_FO : COMPTES_TYPE_ADSL),
	        		null, true)).execute();
    	}
    }

    private void setModules()
    {
    	Map<String,Object> map;

    	
    	map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_television);
		map.put(M_TITRE, getString(R.string.buttonTv));
		map.put(M_DESC, "NOUVEAU : Regardez les chaînes de TV Freebox ! (BETA)");
//		map.put(M_CLASS, TvActivity.class);
		map.put(M_CLASS, null);
		modulesList.add(map);
		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_guide_tv);
		map.put(M_TITRE, getString(R.string.buttonGuide));
		map.put(M_DESC, "Consultez le guide TV, programmez des enregistrements");
		map.put(M_CLASS, GuideMenuActivity.class);
		modulesList.add(map);
		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_actus_freenautes);
		map.put(M_TITRE, getString(R.string.buttonActu));
		map.put(M_DESC, "Consultez l'actualité de Free et de la Freebox");
		map.put(M_CLASS, null);
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
		map.put(M_ICON, R.drawable.fm_telecommande);
		map.put(M_TITRE, getString(R.string.buttonTelecommande));
		map.put(M_DESC, "Amusez vous avec nos télécommandes\n[BETA]");
		map.put(M_CLASS, null);
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
		map.put(M_DESC, "Utilisez votre compte Freebox pour envoyer des Fax à partir de votre mobile\n[BETA]");
		map.put(M_CLASS, FaxActivity.class);
		modulesList.add(map);
		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_assistance);
		map.put(M_TITRE, getString(R.string.buttonAssistance));
		map.put(M_DESC, "Accédez au site web de l'assistance Free");
//		map.put(M_CLASS, AssistanceActivity.class);
		map.put(M_CLASS, null);
		modulesList.add(map);
		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_freewifi);
		map.put(M_TITRE, getString(R.string.buttonFreeWifi));
		map.put(M_DESC, "Configurer votre accès FreeWifi afin de pouvoir bénéficier de millions de HotSpots Wifi en France");
		map.put(M_CLASS, null);
		modulesList.add(map);
		map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_webmail);
		map.put(M_TITRE, getString(R.string.buttonWebmail));
		map.put(M_DESC, "Si vous avez une adresse email en @free.fr, accèdez au webmail ici");
		map.put(M_CLASS, null);
		modulesList.add(map);
    	map = new HashMap<String,Object>();
		map.put(M_ICON, R.drawable.fm_radios);
		map.put(M_TITRE, getString(R.string.buttonMusique));
		map.put(M_DESC, "Ecoutez la musique présente chez vous\n\nCette fonctionnalité n'est pas encore disponible");
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
        	Log.d(TAG,"LINE TYPE:"+mgr.getString(KEY_LINETYPE, "-1"));
	    	if (moduleName.equals(getString(R.string.buttonLigne)) &&
    			!mgr.getString(KEY_LINETYPE, "1").equals("0") &&
    			!mgr.getString(KEY_LINETYPE, "1").equals("1"))
	        {
	        	showNonADSL();
	        	return;
	        }
//	    		checkExtApp("me.abitno.vplayer", "", "VPLayer");
	    	if (moduleName.equals(getString(R.string.buttonAssistance)))
	    	{
//	    		FBMHttpConnection.connectAssistance();
	            Intent assistanceIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://assistance.free.fr/i/#_home"));
	            assistanceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    		tracker.trackPageView("Assistance");
	            startActivity(assistanceIntent);
	    	}
	    	else if (moduleName.equals(getString(R.string.buttonWebmail)))
	    	{
	    		openExtApp("org.geeek.free", ".activity.SplashScreenActivity", "Webmail Free.fr", false);
	    	}else if (moduleName.equals(getString(R.string.buttonActu)))
		    {
		    	openExtApp("org.madprod.infofreenautes", ".splashscreen.SplashScreen", "Actu Freenautes", true);
		    }else if (moduleName.equals(getString(R.string.buttonTelecommande)))
		    {
			    	openExtApp("org.madprod.freeboxmobile.remotecontrol.earlypropale", ".Main", "Early Propale", true);		    		
		    }
	    	else if (moduleName.equals(getString(R.string.buttonFreeWifi)))
	    	{
	    		openExtApp("com.mba.freewifi", ".FreeWifiConnect", "FreeWifi Connect", false);
	    	}
	    	else if (moduleName.equals("Freebox v6") || moduleName.equals("Conférence de presse Free"))
	    	{
	    		displayV6();
	    	}
        }
    	if (moduleClass != null)
    	{
    		startActivity(new Intent(this, moduleClass));
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG,"onActivityResult");
		FBMNetTask.register(this);
        switch(requestCode)
        {
        	case ACTIVITY_COMPTES:
            	if (FBMHttpConnection.checkUpdated(
            			getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_USER, null),
            			getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getString(KEY_PASSWORD, null)
            			))
            	{
//            		FBMNetTask.register(this);
            	}
        		break;
        }
    }
    
    private void displayV6()
    {
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("Conférence de presse Freebox v6");
		d.setIcon(R.drawable.icon_fbm);
    	d.setMessage(
			"Afin de visionner la conférence de presse en direct, vous devez avoir installé Adobe Flash Player (Android 2.2 minimum requis).\n"
		);
		d.setButton(DialogInterface.BUTTON_POSITIVE, "Installer Flash Player", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
	    		tracker.trackPageView("Home/InstallFlash");
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.adobe.flashplayer")));
			}
		});
		d.setButton(DialogInterface.BUTTON_NEGATIVE, "Regarder la diffusion live", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
	    		tracker.trackPageView("Home/ShowV6");
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.universfreebox.com/live")));
			}
		});
		d.show();
    }

    private void openExtApp(final String packageName, final String className, final String appName, boolean plugin)
    {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setClassName(packageName, packageName+className);
		i.putExtra("nosplashscreen", true);
		try
		{
			if (!plugin)
			{
				tracker.trackPageView(appName);
			}
			startActivity(i);
		}
		catch (ActivityNotFoundException e)
		{
			String type;
			if (plugin)
				type = "le module";
			else
				type = "l'application";
	    	AlertDialog d = new AlertDialog.Builder(this).create();
			d.setTitle(getString(R.string.app_name));
			d.setIcon(R.drawable.icon_fbm);
	    	d.setMessage(
				"Pour utiliser cette fonctionnalité, vous devez installer "+type+" '"+appName+"'.\n\n"+
				"Cliquez sur 'Continuer' pour l'installer ou sur 'Plus tard' pour continuer à utiliser Freebox Mobile "+
				"sans cette fonctionnalité.\n"
			);
			d.setButton(DialogInterface.BUTTON_POSITIVE, "Continuer", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
			            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
			            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			            try
			            {
			            	startActivity(marketIntent);
			            }
			            catch (ActivityNotFoundException e)
			            {
			    	    	AlertDialog ad = new AlertDialog.Builder(HomeListActivity.this).create();
			    			ad.setTitle(getString(R.string.app_name));
			    			ad.setIcon(R.drawable.icon_fbm);
			    	    	ad.setMessage("Impossible d'ouvrir Android Market !");
			    			ad.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int which)
								{
									dialog.dismiss();
								}
							});
			    	    	ad.show();
			            }
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
		tracker.trackPageView("Home/ShareApp");
    	startActivity(Intent.createChooser(i,  "Choisissez votre logiciel de mail")); 
    }
    
    private void displayVote()
    {
    	AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name));
		d.setIcon(R.drawable.icon_fbm);
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
	    		tracker.trackPageView("Home/Vote");
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.madprod.freeboxmobile")));
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
		d.setIcon(R.drawable.icon_fbm);
    	d.setView(s);
    	d.setButton(DialogInterface.BUTTON_NEUTRAL, "Continuer", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
		    	startActivity(new Intent(HomeListActivity.this, SendlogActivity.class));
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
    		"Info : Installation sur carte SD : L'installation sur SD n'est possible sur Android que "+
    		"pour des applications simples, n'utilisant pas des fonctionnalités avancées. "+
    		"A cause de ces contraintes liées à Android, cette application n'est pas installable sur SD. "+
    		"Consultez la FAQ sur notre site web pour plus d'information.\n\n"+
			"Site web :\nhttp://www.freeboxmobile.org\n\n"+
			"Contact :\ncontact@freeboxmobile.org\n\n"+
			"Version : "+Utils.getFBMVersion(this)+"\n\n"+
			"Facebook (devenez fan !) :\nhttp://www.facebook.com/search/?q=freeboxmobile\n\n"+
			"Auteurs :\n"+
			"- Olivier Rosello : Architecture / Réseau / Home / Info ADSL / Téléphone / Guide des Programmes\n"+
			"- Clément Beslon : Télécommande Wifi / Actualité Freenautes\n"+
			"- Bruno Alacoque : Skins Télécommande\n"+
			"- Nacer Laradji : Hébergement, gestion des serveurs\n"+
			"- Benoit Duffez : Magnétosocope\n"+
			"- Ludovic Meurillon : Fax\n"+
			"- Alban Pelé : Icônes de la page d'accueil\n"+
			"\n"+
			"Cette application opensource utilise :\n"+
			"- Android-XMLRPC : http://code.google.com/p/android-xmlrpc/\n"+
			"- Icônes Tango : http://tango.freedesktop.org/\n" +
			"- Frimousse : http://www.frimousse.org\n"+
			"\n"+
			"Les serveurs de Freebox Mobile sont gracieusement fournis par Ovea:\nhttp://www.oveaconnect.me/\n"
			);
    	s.setPadding(10,10,10,10);
    	t.setMovementMethod(LinkMovementMethod.getInstance());
    	t.setTextColor(0xffffffff);
    	t.setLinkTextColor(0xffffffff);
    	t.setTextSize(16);
    	s.addView(t);
    	d.setView(s);
		d.setTitle(getString(R.string.app_name));
		d.setIcon(R.drawable.icon_fbm);
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
		    	startActivity(new Intent(HomeListActivity.this, org.madprod.freeboxmobile.home.WhatsNewActivity.class));
			}
		});
		d.show();
    }

    private void showNonADSL()
    {
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(getString(R.string.app_name));
		d.setIcon(R.drawable.icon_fbm);
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
		d.setIcon(R.drawable.icon_fbm);
		d.setTitle(getString(R.string.app_name));
		d.setMessage(
			"Cette fonctionnalité n'est accessible qu'aux abonnés dégroupés.\n"
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
		d.setIcon(R.drawable.icon_fbm);
		d.setMessage(
			"Veuillez configurer au moins un compte pour pouvoir utiliser cette application.\n\n"+
			"Vous pouvez configurer des comptes en utilisant la touche MENU sur la page d'accueil "+
			"ou en utilisant le bouton ci-dessous."
		);

		d.setButton(DialogInterface.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
		    	startActivityForResult(new Intent(HomeListActivity.this, org.madprod.freeboxmobile.home.ComptesActivity.class), ACTIVITY_COMPTES);
			}
		});
		d.show();      
    }
    
    private void showSdCardError()
    {
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("SDCard Necessaire !");
		d.setIcon(R.drawable.icon_fbm);
		d.setMessage(
			"Une carte SD est nécessaire afin d'utiliser "+
			"cette application."
		);
		d.setButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					//TODO : désactiver les modules qui dépendent de la SD ?
					finish();
				}
			}
		);
		d.show();      
    }
}

/*

Reflexion sur le chargement dynamique de la home.

Icône ?

Lancement d'un module externe :

      PackageManager packageManager = context.getPackageManager();
      String packageName = "org.madprod.freeboxmobile";
      Intent intent = packageManager.getLaunchIntentForPackage(packageName);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      context.startActivity(intent);
      
      
Format du json :
{
	"modules":
	[
		{
			"name" : "Messagerie Vocale Visuelle",
			"desc" : "Accédez à la messagerie vocale de votre Freebox",
			"cat"  : "phone",
			"type" : "internal",
			"launch_activity" : "org.madprod.freebmobile.mvv",
			"package_name" : "",
			"ipadsl" : true,
			"adsl" : true,
			"ftth" : true,
			"mobile" : false,
			"vmin_fbx" : 1,
			"vmax_fbx" : 9999,
			"vmin_android" : 3,
			"vmax_android" : 9999,
		},
		{
			"name" : "Télévision sur Mobile",
			"desc" : "Accédez lors de vos déplacements à une vingtaines des chaînes",
			"cat"  : "tv",
			"type" : "plugin",
			"package_name" : "org.madprod.freebmobile.tvmobile",
			"ipadsl" : true,
			"adsl" : true,
			"ftth" : true,
			"mobile" : true,
			"vmin_fbx" : 1,
			"vmax_fbx" : 9999,
			"vmin_android" : 7,
			"vmax_android" : 9999,
		},
		{
			"name" : "Télévision Multipostes",
			"desc" : "Accédez depuis chez vous aux chaînes de votre Freebox",
			"cat"  : "tv",
			"type" : "plugin",
			"package_name" : "org.madprod.freebmobile.tvmultiposte",
			"ipadsl" : false,
			"adsl" : true,
			"ftth" : true,
			"mobile" : false,
			"vmin_fbx" : 3,
			"vmax_fbx" : 9999,
			"vmin_android" : 7,
			"vmax_android" : 9999,
		}
		{
			"name" : "Webmail Free.fr",
			"desc" : "Si vous avez une adresse en @free.fr, accédez au webmail ici",
			"cat"  : "external",
			"type" : "external",
			"package_name" : "org.geeek.free",
			"ipadsl" : true,
			"adsl" : true,
			"ftth" : true,
			"mobile" : true,
			"vmin_fbx" : 1,
			"vmax_fbx" : 9999,
			"vmin_android" : 3,
			"vmax_android" : 9999,
		}
	]
}
*/