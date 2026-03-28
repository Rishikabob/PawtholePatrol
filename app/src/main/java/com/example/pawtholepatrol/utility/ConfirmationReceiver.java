package com.example.pawtholepatrol.utility;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConfirmationReceiver extends BroadcastReceiver {

    public static final String PRESS_YES = "";
    public static final String PRESS_NO = "";
    private static final String LOG_TAG = "ConfirmationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // On button press, dismiss the inquiry notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(EventConfirmationHelper.INQUIRY_ID);

        // Return the result of the button press
        if (PRESS_YES.equals(intent.getAction())) {
            EventConfirmationHelper.deliverResult(true);
        } else if (PRESS_NO.equals(intent.getAction())) {
            EventConfirmationHelper.deliverResult(false);
        } else {
            Log.w(LOG_TAG, "Did not register any action");
        }
    }
}
