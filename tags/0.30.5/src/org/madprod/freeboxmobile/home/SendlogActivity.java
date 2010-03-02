package org.madprod.freeboxmobile.home;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;
import android.os.Build;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class SendlogActivity extends Activity implements HomeConstants
{
	private EditText desc;
	private String Wifi3G="";
	private String fbmlog = "";

	@Override
	public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setTitle(getString(R.string.app_name)+" "+FBMHttpConnection.getTitle()+" - Sendlog");
		setContentView(R.layout.home_sendlog);
		
		desc = (EditText) findViewById(R.id.EditTextInfolog);
		desc.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean b)
            {
				if (b == true)
				{
					if (desc.getText().toString().equals(getString(R.string.sendlog_pbdesc)))
					{
						desc.setText("");
					}
				}
            }
		});
        Button newButton = (Button) findViewById(R.id.ButtonSendlogValider);
        newButton.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View view)
        	{
        		String description = desc.getText().toString(); 
        		if ((!description.equals(getString(R.string.sendlog_pbdesc))) && (!description.equals("")))
        		{
	        		TelephonyManager telephonyManager =(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	        		String myLog = "Network Operator : " + telephonyManager.getNetworkOperatorName() + "\n";
	        		myLog += "SIM Operator : " + telephonyManager.getSimOperatorName() + "\n";
	        		myLog += "Le problème arrive : " + Wifi3G + "\n";
	        		myLog += getBuildDetailsAsString();
			    	SpannableStringBuilder ssb = new SpannableStringBuilder(
			    			getString(R.string.app_name)+" : "+
			    			getString(R.string.app_version)+"\n\n"+
			    			myLog+"\n"+
			    			"Description :\n----------\n" + description + "\n----------\n"+
			    			fbmlog);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
/*			    	Intent i = new Intent(Intent.ACTION_SEND)
			    		.putExtra(Intent.EXTRA_EMAIL, new String[]{"bugs@freeboxmobile.org"})
			    		.putExtra(Intent.EXTRA_TEXT, ssb)
			    		.putExtra(Intent.EXTRA_SUBJECT, 
			    				getString(R.string.mail_subject)+" "+sdf.format(new Date())) 
			    				.setType("message/rfc822");
			    				*/
			    	fbmlog = ssb.toString();
/*					Intent i = new Intent(Intent.ACTION_SEND)
		    		.putExtra(Intent.EXTRA_TEXT, fbmlog).setType("text/plain")
		    		.addCategory(Intent.CATEGORY_DEFAULT)
		    		;
			    	startActivityForResult(Intent.createChooser(i,  "Partagez ce programme avec"),0);
*/
			    	collectAndSendLog();
        		}
        		else
        		{
        			AlertDialog d = new AlertDialog.Builder(SendlogActivity.this).create();
        			d.setTitle("Description obligatoire");
        			d.setMessage(
        				"Veuillez rentrer une description du problème avant de valider.\n\n"+
        				"C'est important pour nous aider à résoudre le problème :)"
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
   			}
        });
        Spinner s = (Spinner) findViewById(R.id.Spinner3GWifi);
        s.setOnItemSelectedListener(new OnItemSelectedListener()
        {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int i, long l)
			{
				Wifi3G = parent.getItemAtPosition(i).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
        });

    }
	

    void collectAndSendLog()
    {
    	final String LOG_COLLECTOR_PACKAGE_NAME = "com.xtralogic.android.logcollector";//$NON-NLS-1$
   	    final String ACTION_SEND_LOG = "com.xtralogic.logcollector.intent.action.SEND_LOG";//$NON-NLS-1$
   	    final String EXTRA_SEND_INTENT_ACTION = "com.xtralogic.logcollector.intent.extra.SEND_INTENT_ACTION";//$NON-NLS-1$
   	    final String EXTRA_DATA = "com.xtralogic.logcollector.intent.extra.DATA";//$NON-NLS-1$
   	    final String EXTRA_ADDITIONAL_INFO = "com.xtralogic.logcollector.intent.extra.ADDITIONAL_INFO";//$NON-NLS-1$
   	    final String EXTRA_SHOW_UI = "com.xtralogic.logcollector.intent.extra.SHOW_UI";//$NON-NLS-1$
   	    final String EXTRA_FILTER_SPECS = "com.xtralogic.logcollector.intent.extra.FILTER_SPECS";//$NON-NLS-1$
   	    final String EXTRA_FORMAT = "com.xtralogic.logcollector.intent.extra.FORMAT";//$NON-NLS-1$
  	    final String EXTRA_BUFFER = "com.xtralogic.logcollector.intent.extra.BUFFER";//$NON-NLS-1$

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        final PackageManager packageManager = getPackageManager();
        final Intent intent = new Intent(ACTION_SEND_LOG);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        final boolean isInstalled = list.size() > 0;
        
        if (!isInstalled)
        {
            new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage("Pour pouvoir nous envoyer le log d'erreur, vous devez installer l'application Log Collector (gratuite et open source).")
            .setPositiveButton("Installer", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:" + LOG_COLLECTOR_PACKAGE_NAME));
                    marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(marketIntent); 
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        }
        else
        {
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.putExtra(EXTRA_SEND_INTENT_ACTION, Intent.ACTION_SENDTO);
	        final String email = "bugs@freeboxmobile.org";
	        intent.putExtra(EXTRA_DATA, Uri.parse("mailto:" + email));
	        intent.putExtra(EXTRA_ADDITIONAL_INFO, fbmlog/*"Additonal info: <additional info from the device (firmware revision, etc.)>\n"*/);
	        intent.putExtra(Intent.EXTRA_SUBJECT, "Freebox Mobile bug report "+sdf.format(new Date()));
	
	        intent.putExtra(EXTRA_FORMAT, "time");
	        
	        //The log can be filtered to contain data relevant only to your app
	        String[] filterSpecs = new String[3];
	        filterSpecs[0] = "AndroidRuntime:E";
	//                    filterSpecs[1] = TAG + ":V";
	        filterSpecs[1] = TAG;
	        filterSpecs[2] = "*:S";
	        intent.putExtra(EXTRA_FILTER_SPECS, filterSpecs);
	        
	        startActivityForResult(intent, 0);
        }
    }

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
    	Toast.makeText(this, "Merci de nous aider à améliorer Freebox Mobile !",
    			Toast.LENGTH_LONG).show();
    	finish();
	}

	// Code from android log collector (android-log-collector.googlecode.com)
	private String getFormattedKernelVersion() 
    {
        String procVersionStr;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/version"), 256);
            try {
                procVersionStr = reader.readLine();
            } finally {
                reader.close();
            }

            final String PROC_VERSION_REGEX =
                "\\w+\\s+" + /* ignore: Linux */
                "\\w+\\s+" + /* ignore: version */
                "([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
                "\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /* group 2: (xxxxxx@xxxxx.constant) */
                "\\([^)]+\\)\\s+" + /* ignore: (gcc ..) */
                "([^\\s]+)\\s+" + /* group 3: #26 */
                "(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
                "(.+)"; /* group 4: date */

            Pattern p = Pattern.compile(PROC_VERSION_REGEX);
            Matcher m = p.matcher(procVersionStr);

            if (!m.matches()) {
                Log.e(TAG, "Regex did not match on /proc/version: " + procVersionStr);
                return "Unavailable";
            } else if (m.groupCount() < 4) {
                Log.e(TAG, "Regex match on /proc/version only returned " + m.groupCount()
                        + " groups");
                return "Unavailable";
            } else {
                return (new StringBuilder(m.group(1)).append("\n").append(
                        m.group(2)).append(" ").append(m.group(3)).append("\n")
                        .append(m.group(4))).toString();
            }
        } catch (IOException e) {  
            Log.e(TAG,
                "IO Exception when getting kernel version for Device Info screen",
                e);

            return "Unavailable";
        }
    }
	
	// Code from enh project (enh.googlecode.com)
	String getBuildDetailsAsString()
	{
		final Build build = new Build();
		
		// These two fields were added to Build at API level 4 (Android 1.6).
		// Since currently (2010-01-03) about 25% of devices are running 1.5, we use reflection.
		// http://developer.android.com/resources/dashboard/platform-versions.html
		final String cpuAbi = getFieldReflectively(build, "CPU_ABI");
		final String manufacturer = getFieldReflectively(build, "MANUFACTURER");
		
		final StringBuilder result = new StringBuilder();
		result.append("Manufacturer : " + manufacturer + "\n");
		result.append("Model : " + Build.MODEL + "\n");
		result.append("Display : " + Build.DISPLAY + "\n");
		result.append("Product : " + Build.PRODUCT + "\n");
		result.append("CPU ABI : " + cpuAbi + "\n");
		result.append("Brand : " + Build.BRAND + "\n");
		result.append("Board : " + Build.BOARD + "\n");
		result.append("Device : " + Build.DEVICE + "\n");
		result.append("Release : " + Build.VERSION.RELEASE + "\n");
		result.append("Build Fingerprint : " + Build.FINGERPRINT + "\n");
		result.append("Kernel : "+getFormattedKernelVersion()+"\n");
		return result.toString();
	}

   // Code from enh project (enh.googlecode.com)
   private String getFieldReflectively(Build build, String fieldName)
   {
	   try
	   {
		   final Field field = Build.class.getField(fieldName);
		   return field.get(build).toString();
	   }
	   catch (Exception ex)
	   {
		   return "inconnu";
	   }
   }
}
