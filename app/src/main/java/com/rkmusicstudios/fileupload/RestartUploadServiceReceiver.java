package com.rkmusicstudios.fileupload;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by GEIDEA_A3 on 10-Nov-17.
 */

public class RestartUploadServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(RestartUploadServiceReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        UploadService mUploadService = new UploadService(context);
        Intent mServiceIntent = new Intent(context, mUploadService.getClass());
        context.startService(mServiceIntent);

    }


}
