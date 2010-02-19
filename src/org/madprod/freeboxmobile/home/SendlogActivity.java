package org.madprod.freeboxmobile.home;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Build;
import org.madprod.freeboxmobile.FBMHttpConnection;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
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
	EditText desc;
	String Wifi3G=""; 

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
			    			FBMHttpConnection.fbmlog);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			    	Intent i = new Intent(Intent.ACTION_SEND)
			    		.putExtra(Intent.EXTRA_EMAIL, new String[]{"bugs@freeboxmobile.org"})
			    		.putExtra(Intent.EXTRA_TEXT, ssb)
			    		.putExtra(Intent.EXTRA_SUBJECT, 
			    				getString(R.string.mail_subject)+" "+sdf.format(new Date())) 
			    				.setType("message/rfc822");
			    	startActivityForResult(Intent.createChooser(i,  "Choisissez votre logiciel de mail"),0); 
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
	   
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
    	Toast.makeText(this, "Merci de nous aider à améliorer Freebox Mobile !",
    			Toast.LENGTH_LONG).show();
    	finish();
	}

   // Code from enh project (enh.googlecode.com)
   static String getBuildDetailsAsString()
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
	   result.append("Build Fingerprint : " + Build.FINGERPRINT + "\n");
	   return result.toString();
   }

   // Code from enh project (enh.googlecode.com)
   private static String getFieldReflectively(Build build, String fieldName)
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
