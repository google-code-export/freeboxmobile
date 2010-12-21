package org.madprod.mevo;

import org.madprod.mevo.tools.DetachableResultReceiver;

import android.os.Handler;

public class StateMevoRefresh {
	public static boolean mSyncing = false;
	public static DetachableResultReceiver mReceiver = new DetachableResultReceiver(new Handler());

}
