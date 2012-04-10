package org.madprod.freeboxmobile.fax;

import org.madprod.freeboxmobile.Constants;

public interface FaxConstants extends Constants {
	static final String KEY_SPLASH_FAX		= "splashscreen_fax";
	public static final String END 			= "\r\n";
	public static final String TWO_HYPHENS 	= "--";
	public static final String BOUNDARY 	= "*****++++++************++++++++++++";
	
	public static final String END_CONTENT_DISPOSITION = TWO_HYPHENS + BOUNDARY + END;
	
//	public static final String UPLOAD_FAX_URL = "https://adsls.free.fr/admin/tel/fax/tel_ulfax.pl";
	public static final String UPLOAD_FAX_URL = "https://adsls.free.fr/tel_ulfax.pl";
	
}
