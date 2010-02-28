package org.madprod.freeboxmobile.pvr;

class PvrUtils {
	public static String make02d(int a) {
		if (a < 10) {
			return "0"+a;
		}
		else {
			return ""+a;
		}
	}
}