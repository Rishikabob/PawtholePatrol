package com.example.pawtholepatrol.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConfirmationReceiver extends BroadcastReceiver {

    public static final String PRESS_YES = "com.example.pawtholepatrol.PRESS_YES";
    public static final String PRESS_NO  = "com.example.pawtholepatrol.PRESS_NO";
    private static final String LOG_TAG = "ConfirmationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PRESS_YES.equals(intent.getAction())) {
            EventConfirmationHelper.deliverResult(context, true);
        } else if (PRESS_NO.equals(intent.getAction())) {
            EventConfirmationHelper.deliverResult(context, false);
        } else {
            Log.w(LOG_TAG, "Did not register any action");
        }
    }
}
