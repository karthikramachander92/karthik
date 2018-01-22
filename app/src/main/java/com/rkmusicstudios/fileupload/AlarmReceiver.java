package com.rkmusicstudios.fileupload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by GEIDEA_A3 on 17-Nov-17.
 */

public class AlarmReceiver extends BroadcastReceiver {

    public static String ACTION_UPLOAD_ALARM = "com.rkmusicstudios.fileupload.upload_alarm";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(ACTION_UPLOAD_ALARM)){
            Log.e("Upload alarm","Alarm Triggered");
            ConnectivityReceiver.triggerConnectionCheck(context);
        }
    }


}
