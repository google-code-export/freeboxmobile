package org.madprod.mevo.tools;


import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
public class DetachableResultReceiver extends ResultReceiver implements Constants{

    private ArrayList<Receiver> mReceivers = new ArrayList<DetachableResultReceiver.Receiver>();

    public DetachableResultReceiver(Handler handler) {
        super(handler);
    }


    public void setReceiver(Receiver receiver) {
    	if (!mReceivers.contains(receiver))
        mReceivers.add(receiver);
    }

    public void unsetReceiver(Receiver receiver) {
        mReceivers.remove(receiver);
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
    	for (Receiver r : mReceivers){
            if (r != null) {
                r.onReceiveResult(resultCode, resultData);
            }
    	}
        Log.w(TAG, "Dropping result on floor for code " + resultCode + ": "
               + resultData.toString());
    }
}
