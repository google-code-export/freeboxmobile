package org.madprod.mevo.tools;

import java.text.ParseException; 
import java.text.SimpleDateFormat;
import java.util.Date;

import org.madprod.freeboxmobile.services.MevoMessage;
import org.madprod.mevo.HomeActivity;
import org.madprod.mevo.R;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Contacts.Phones;
import android.provider.Contacts.Intents.Insert;
import android.util.Log;

@SuppressWarnings("deprecation")
public class Utils implements Constants{
	

	
	
    public static void goHome(Context context) {
        final Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    
    public static void goFBM(Context context) {
    	PackageManager packageManager = context.getPackageManager();
    	String packageName = "org.madprod.freeboxmobile";

    	Intent intent = packageManager.getLaunchIntentForPackage(packageName);
//        intent.setClassName("org.madprod.freeboxmobile", "org.madprod.freeboxmobile.home.HomeListActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    
	public static String getVersionName(Context context) 
	{
	  try {
	    PackageInfo pinfo = context.getPackageManager().getPackageInfo("org.madprod.mevo", 0);
	    return pinfo.versionName;
	  } catch (android.content.pm.PackageManager.NameNotFoundException e) {
	    return null;
	  }
	}

	
	public static String getContactFromNumber(Context context, String number)
	{
		ContentResolver resolver = context.getContentResolver();

		// define the columns I want the query to return
		String[] projection = new String[]{
				Contacts.Phones.DISPLAY_NAME,
				Contacts.Phones.TYPE,
				Contacts.Phones.NUMBER,
				Contacts.Phones.LABEL };

		if ((number != null) && (number.length() > 0))
		{
			String n = Uri.encode(number);
			Uri contactUri = Uri.withAppendedPath(Phones.CONTENT_FILTER_URL, n);
//	For 2.0+ :
//			Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_URI, Uri.encode(phoneNumber));
			Cursor c = resolver.query(contactUri, projection, null, null, null);
			// if the query returns 1 or more results
			// return the first result
			try{
				if (c.moveToFirst())
				return c.getString(c.getColumnIndex(Contacts.Phones.DISPLAY_NAME));

			}finally{
				c.close();				
			}
		}
		return number;
	}

	public static String convertDateTimeHR(String org)
	{
		String ret = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			Date deb = sdf.parse(org);
			Date fin = new Date();//sdf.parse(new Date());
			long diff = fin.getTime() - deb.getTime();
			int nbJours = (int) (diff / 86400000);
			String[] datetime = org.split(" ");
//			if (nbJours == 0)
//				ret = datetime[1];
//			else if (nbJours == 1)
//				ret = "Hier "+datetime[1];
//			else
			String[] date = datetime[0].split("-");
			if (nbJours <366)
			{
				ret = date[2]+"/"+date[1]+" "+datetime[1];
			}
			else
			{
				ret = date[2]+"/"+date[1]+"/"+date[0]+" "+datetime[1];				
			}
		}
		catch (ParseException e)
		{
			Log.e(TAG,"PARSE DATETIME HR "+e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}
	
	
	public static String getPhoneTypeString(Context _context, int type, String label){
		switch (type)
		{
			case Contacts.Phones.TYPE_CUSTOM:
				return label;
			case Contacts.Phones.TYPE_FAX_HOME:
				return _context.getString(R.string.mevo_msgtype_fax_home);
			case Contacts.Phones.TYPE_FAX_WORK:
				return _context.getString(R.string.mevo_msgtype_fax_work);
			case Contacts.Phones.TYPE_HOME:
				return _context.getString(R.string.mevo_msgtype_home);
			case Contacts.Phones.TYPE_MOBILE:
				return _context.getString(R.string.mevo_msgtype_mobile);
			case Contacts.Phones.TYPE_OTHER:
				return _context.getString(R.string.mevo_msgtype_other);
			case Contacts.Phones.TYPE_PAGER:
				return _context.getString(R.string.mevo_msgtype_pager);
			case Contacts.Phones.TYPE_WORK:
				return _context.getString(R.string.mevo_msgtype_work);
			default:
				return "???";
		}
	}
	
	public static void sendSms(Context context, MevoMessage message){
		Uri uri = Uri.parse("smsto:"+message.getSource());   
		Intent it = new Intent(Intent.ACTION_SENDTO, uri);   
		it.putExtra("sms_body", "Suite à ton message ");   
		context.startActivity(Intent.createChooser(it, "Envoyer par :"));		
	}	
	
	public static void removeMessage(Context context, MevoMessage message){
		try {
			HomeActivity.mMevo.deleteMessage(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}	
	
	public static void searchNumber(Context context, MevoMessage message){
		Intent searchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.118218.fr/wap/resultats.php?requete="+message.getSource()+"&particulier=on&activite=&page=1"));
		searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(searchIntent);	
	}
	
	public static void addContact(Context context, MevoMessage message){
		Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);

		try
		{
			// For Android >= 2.0
			intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
			intent.putExtra(ContactsContract.Intents.Insert.PHONE, message.getSource());
			context.startActivity(intent);
		}
		catch (Throwable t)
		{
			// For Android 1.5 - 1.6
			intent.setType(Contacts.People.CONTENT_ITEM_TYPE);
			intent.putExtra(Insert.PHONE, message.getSource());
			context.startActivity(intent);
		}

	}
	
	
	public static void callback(Context context, MevoMessage message){
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:"+message.getSource()));
		context.startActivity(Intent.createChooser(intent, "Appeler avec :"));
	}
}
