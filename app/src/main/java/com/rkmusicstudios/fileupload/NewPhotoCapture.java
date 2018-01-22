package com.rkmusicstudios.fileupload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import java.io.File;

/**
 * Created by GEIDEA_A3 on 15-Nov-17.
 */

public class NewPhotoCapture extends BroadcastReceiver {

    private static PhotoCaptureListener listener;

    public static void setPhotoCaptureListener(PhotoCaptureListener captureListener){
        listener = captureListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Uri imageUri = intent.getData();
        Log.e("captured image path",getRealPathFromURI(context,imageUri));
        listener.onNewPhotoCaptured(new File(getRealPathFromURI(context,imageUri)));
    }
    private String getRealPathFromURI(Context mContext,Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

}
