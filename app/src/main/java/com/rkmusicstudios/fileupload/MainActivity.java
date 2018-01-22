package com.rkmusicstudios.fileupload;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rkmusicstudios.fileupload.Gallery.GalleryActivity;

/**
 * Created by GEIDEA_A3 on 06-Nov-17.
 */

public class MainActivity extends AppCompatActivity{

    Activity context;
    TextView path;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    String UPLOAD_DIRECTORY = "";

    Intent mServiceIntent;
    private UploadService mUploadService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        requestAllPermissions();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener

    }


    @Override
    protected void onDestroy() {

        Log.e("MAINACT", "onDestroy!");
        super.onDestroy();

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.e ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.e ("isMyServiceRunning?", false+"");
        return false;
    }

    public void chooseFile(View view) {
    }


    public void uploadFile(View view) {

        startActivity(new Intent(MainActivity.this, GalleryActivity.class));
    }


    public void requestAllPermissions() {


        if (ActivityCompat.checkSelfPermission(context, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionsRequired[0])) {
                //Show Information about why you need the permission

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
                        builder.setTitle(R.string.need_permission_to_login);
                        builder.setMessage(R.string.phone_state_permission);
                        builder.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(context, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                            }
                        });
                        builder.setNegativeButton(R.string.cancel1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                });


            }
//            else if (permissionStatus.getPermissionStatus(permissionsRequired[0])) {
//                //Previously Permission Request was cancelled with 'Dont Ask Again',
//                // Redirect to Settings after showing Information about why you need the permission
//
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
//                        builder.setTitle(R.string.need_permission_to_login);
//                        builder.setMessage(R.string.phone_state_permission);
//                        builder.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                                sentToSettings = true;
//                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
//                                intent.setData(uri);
//                                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
//                                Toast.makeText(context, R.string.goto_get_camera_permission, Toast.LENGTH_LONG).show();
//                            }
//                        });
//                        builder.setNegativeButton(R.string.cancel1, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                            }
//                        });
//                        builder.show();
//                    }
//                });
//
//            }
            else {
                //just request the permission
                ActivityCompat.requestPermissions(context, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }


        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {
                proceedAfterPermission();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionsRequired[0])) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
                builder.setTitle(R.string.need_permission_to_login);
                builder.setMessage(R.string.phone_state_permission);
                builder.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(context, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(R.string.cancel1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(), R.string.unable_to_get_permission, Toast.LENGTH_LONG).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
                        builder.setTitle(R.string.need_permission_to_login);
                        builder.setMessage(R.string.phone_state_permission);
                        builder.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            }
                        });
                        builder.setNegativeButton(R.string.cancel1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                });
//
            }
        }
    }


    private void proceedAfterPermission() {

        ConnectivityManager ConnectionManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = ConnectionManager.getActiveNetworkInfo();
        //  if (networkInfo == null || !networkInfo.isConnected()) {
        try {

            path = (TextView) findViewById(R.id.path);
            mUploadService = new UploadService(this);
            mServiceIntent = new Intent(this, mUploadService.getClass());
            if (!isMyServiceRunning(mUploadService.getClass())) {
                startService(mServiceIntent);
            }


        } catch (ActivityNotFoundException e) {

            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  }else{

        // }
    }

}
