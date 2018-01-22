package com.rkmusicstudios.fileupload;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by GEIDEA_A3 on 10-Nov-17.
 */

public class UploadService extends Service implements PhotoCaptureListener, ConnectivityReceiverListener {

    public int counter = 0;

    public static final String URL = "http://192.168.1.238/";

    List<FileData> files;

    int indexToUpload = 0;

    int total_files_to_upload = 0;
    int total_uploaded_response = 0;

    String UPLOAD_DIRECTORY = "";
    SharedPreferences sharedPreferences;

    boolean isUploadHappening = false;

    DateFormat dateFormat;


    DatabaseManager databaseManager;
    ConnectivityReceiver connectivityReceiver;


    public UploadService(Context applicationContext) {
        super();
        Log.e("HERE", "here I am!");
    }

    public UploadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("HERE", "here I am at OnCreate");
        sharedPreferences = getSharedPreferences("backup", MODE_PRIVATE);

        UPLOAD_DIRECTORY = Environment.getExternalStorageDirectory().toString().trim() + "/DCIM/Camera";

        databaseManager = new DatabaseManager(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        //StrictMode.enableDefaults();
//        getFilesAndStartUpload();

        //new CheckConnectivityAndStart().execute();

        // startTimer();
        ConnectivityReceiver.connectivityReceiverListener = this;

        connectivityReceiver = new ConnectivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, filter);

        scheduleUploadAlarm(this, 15, 15);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            if (!isUploadHappening)
                getFilesAndStartUploadFromDatabase();
            else
                Log.e("Upload Status", "Upload is happening");
        } else {
            Log.e("Internet Connection", "No Internet available");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e("HERE", "here I am at OnStart!");
        NewPhotoCapture.setPhotoCaptureListener(this);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectivityReceiver);
        Log.e("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("com.rkmusicstudios.RestartUploadService");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }


    public void scheduleUploadAlarm(Context mContext, int hour, int min) {

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(mContext, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_UPLOAD_ALARM);
        // PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, 0);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

        // reset previous pending intent
        alarmManager.cancel(pendingIntent);

        // Set the alarm to start at approximately 08:00 morning.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, hour);
//        calendar.set(Calendar.MINUTE, min);
//        calendar.set(Calendar.SECOND, 0);

        // if the scheduler date is passed, move scheduler time to tomorrow
//        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
//            calendar.add(Calendar.DAY_OF_YEAR, 1);
//        }

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }


    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.e("in timer", "in timer ++++  " + (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void updateLastFileDateAndTime(long date) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("last_update", date);
        editor.commit();
    }

    private long getlastUpdateDataAndTime() {
        return sharedPreferences.getLong("last_update", -1);
    }


    private void getFilesAndStartUploadFromDatabase() {

        List<FileData> fileDataList = databaseManager.getAllFiles();
        ArrayList<File> file = getAllFiles(UPLOAD_DIRECTORY);

        if (fileDataList.size() > 0) {
            Log.e("File in DB", "Size: " + fileDataList.size());
        } else {
            Log.e("File in DB", "No file to upload");
        }

        for (int i = 0; i < file.size(); i++) {
            Log.e("Files", file.get(i).getName() + " ====== " + new Date(file.get(i).lastModified()));
        }


        if (file != null) {

//            for (int i = 0; i < file.size(); i++) {
//                Log.e("Files",file.get(i).getName()+" ====== "+new Date(file.get(i).lastModified()));
//            }

            List<FileData> fileDataListToDb = new ArrayList<>();
            for (int i = 0; i < file.size(); i++) {

                String[] filename = file.get(i).getName().split("\\.", -1);

                if (filename[1].equals("jpg") || filename[1].equals("png") || filename[1].equals("jpeg")) {

                    Log.e("File in DB", filename[0]);
                    Date lastmodified = new Date(file.get(i).lastModified());


                    FileData fileData = new FileData();
                    fileData.setFile(file.get(i).getName());
                    fileData.setLastModified(dateFormat.format(lastmodified));
                    fileData.setUploadStatus(0);

                    if (fileDataList.size() <= 0) {

                        long lastUpdate = getlastUpdateDataAndTime();

                        if (lastUpdate != -1) {

                            Date lastUpdateTime = new Date(lastUpdate);

                            Log.e("file time/Last update", lastmodified.toString() + " ****** " + lastUpdateTime.toString());

                            if (lastmodified.after(lastUpdateTime)) {
                                Log.e("File UPload", file.get(i).getName() + " is new file");
                                fileDataListToDb.add(fileData);

                            } else {
                                Log.e("File UPload", file.get(i).getName() + " is already uploaded");
                            }

                        } else {
                            Log.e("File UPloadss", file.get(i).getName() + " is new file. no last update");
                            fileDataListToDb.add(fileData);
                        }
                    } else {
                        Log.e("File UPloadss", file.get(i).getName() + " is new file. files present in db.");
                        fileDataListToDb.add(fileData);
                    }


                }
            }

            if (fileDataListToDb != null && fileDataListToDb.size() > 0) {
                databaseManager.addFiles(fileDataListToDb);

                Log.e("Recent File", databaseManager.getRecentFile().getFile());
            } else {
                Log.e("Recent File", "No new files to upload");
            }

        }


        fileDataList = databaseManager.getNotUploadedFiles();

        files = fileDataList;

        Collections.sort(files, (new Comparator<FileData>() {

            @Override
            public int compare(FileData t1, FileData t2) {
                try {

                    Date t1_date = dateFormat.parse(t1.getLastModified());
                    Date t2_date = dateFormat.parse(t2.getLastModified());

                    if (t1_date.before(t2_date)) {
                        return 1;
                    } else if (t1_date.after(t2_date)) {
                        return -1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    Log.e("parse error", e.toString());
                    throw new IllegalArgumentException(e);
                }
            }
        }));


        if (files.size() > 0) {
            Log.e("File in DB", "Size: " + fileDataList.size());

            for (int i = 0; i < files.size(); i++) {
                Log.e("Files", files.get(i).getLastModified());
            }

        } else {
            Log.e("File in DB", "No file to upload");
        }


        //checkAndUploadOnSuccess();
        checkAndUpload();

    }


    private void UploadRetrofit(final int i) {

        Log.e("File " + i, files.get(i).getFile());

        try {

            File file = new File(UPLOAD_DIRECTORY + "/" + files.get(i).getFile());
            Uri uploaduri = Uri.fromFile(file);

            InputStream is = getContentResolver().openInputStream(uploaduri);

           /* Thread th = new Thread(){
                @Override
                public void run() {
                    super.run();
                    MultipartRequest multipartRequest;

                    multipartRequest = new MultipartRequest(UploadService.this);
                    multipartRequest.addFile("fileToUpload",UPLOAD_DIRECTORY+"/"+ files.get(i),files.get(i));
                    multipartRequest.execute(URL+"new/uploaded.php");

                }
            };

            th.start();*/

            //UpdateBillToServer(getBytes(is));

            uploadMultiPartImage(files.get(i).getFile(), getBytes(is));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void uploadMultiPartImage(final String filename, byte[] imageBytes) {

        Gson gsons = new GsonBuilder()
                .setLenient()
                .create();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gsons))
                .client(okHttpClient)
                .build();

        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);


        List<MultipartBody.Part> parts = new ArrayList<>();
        parts.clear();

        parts.add(prepareFilePart("fileToUpload[]", new File(files.get(indexToUpload).getFile())));


//        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
//
//        MultipartBody.Part body = MultipartBody.Part.createFormData("fileToUpload", filename.trim(), requestFile);


        Call<Response> call = retrofitInterface.uploadImages(parts);

        isUploadHappening = true;

        call.enqueue(new Callback<Response>() {

            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {


                if (response.isSuccessful()) {

                    Response responseBody = response.body();
                    //mImageUrl = URL + responseBody.getPath();

                    Log.e("Retrofit", "onSuccess: " + response.body().getMessage() + ": " + response.toString());
                    total_uploaded_response += 1;

//                    if((indexToUpload-1) == 0) {
//                        updateLastFileDateAndTime(new File(UPLOAD_DIRECTORY + "/" + files.get(indexToUpload - 1)).lastModified());
//                    }

                    databaseManager.updateFileUploadStatus(files.get(indexToUpload - 1).getFile(), 1);
                    checkAndUploadOnSuccess();

                } else {

                    ResponseBody errorBody = response.errorBody();

                    Gson gson = new Gson();

                    databaseManager.updateFileUploadStatus(files.get(indexToUpload - 1).getFile(), 2);

                    try {

                        //Response errorResponse = gson.fromJson(errorBody.string(), Response.class);
                        Log.e("Retrofit", "onError: " + errorBody.string() + ": " + response.toString());
                        checkAndUploadOnError();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                Log.e("Total files to Upload", total_uploaded_response + "/" + total_files_to_upload);


            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {


                Log.e("Retrofit", "onFailure: " + t.getMessage());

                total_uploaded_response += 1;

                Log.e("Total files to Upload", total_uploaded_response + "/" + total_files_to_upload);

                databaseManager.updateFileUploadStatus(files.get(indexToUpload - 1).getFile(), 3);

                checkAndUploadOnSuccess();
            }
        });
    }


    private void uploadImage(String filename, byte[] imageBytes) {

        Gson gsons = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gsons))
                .build();

        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);


        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);

        MultipartBody.Part body = MultipartBody.Part.createFormData("fileToUpload", filename.trim(), requestFile);


        Call<Response> call = retrofitInterface.uploadImage(body);

        call.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {


                if (response.isSuccessful()) {

                    Response responseBody = response.body();
                    // mImageUrl = URL + responseBody.getPath();

                    Log.e("Retrofit", "onSuccess: " + response.body().getMessage());
                    checkAndUploadOnSuccess();

                } else {

                    ResponseBody errorBody = response.errorBody();

                    Gson gson = new Gson();

                    try {

                        //Response errorResponse = gson.fromJson(errorBody.string(), Response.class);
                        Log.e("Retrofit", "onError: " + errorBody.string());

                        checkAndUploadOnError();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {


                Log.e("Retrofit", "onFailure: " + t.getMessage());
                checkAndUploadOnSuccess();
            }
        });
    }


    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();

        int buffSize = 2048;
        byte[] buff = new byte[buffSize];

        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }

        byteBuff.flush();

        return byteBuff.toByteArray();
    }


    public ArrayList<File> getAllFiles(String path) {
        Log.e("Path", path);
        ArrayList<File> result = new ArrayList<File>(); //ArrayList cause you don't know how many files there is
        File folder = new File(path); //This is just to cast to a File type since you pass it as a String
        File[] filesInFolder = folder.listFiles(); // This returns all the folders and files in your path
        for (File file : filesInFolder) { //For each of the entries do:
            if (!file.isDirectory()) { //check that it's not a dir
                result.add(file); //push the filename as a string
            }
        }

        return result;
    }


    private void checkAndUploadOnSuccess() {
        if (indexToUpload < files.size()) {
            UploadRetrofit(indexToUpload);
            indexToUpload += 1;
        } else {
            isUploadHappening = false;
            indexToUpload = 0;

            total_uploaded_response = 0;

            if (databaseManager.getNotUploadedFiles().size() <= 0) {

                String recent_file = databaseManager.getRecentFile().getFile();

                if (recent_file != null) {
                    Log.e("Update last file", String.valueOf(UPLOAD_DIRECTORY + "/" + recent_file));
                    updateLastFileDateAndTime(new File(UPLOAD_DIRECTORY + "/" + recent_file).lastModified());
                }
                databaseManager.dropBackupTable();
            }
        }
    }

    private void checkAndUpload() {

        if (indexToUpload < files.size()) {
            UploadRetrofit(indexToUpload);
            indexToUpload += 1;
        } else {
            isUploadHappening = false;
            indexToUpload = 0;

            total_uploaded_response = 0;

            if (databaseManager.getNotUploadedFiles().size() <= 0) {

                String recent_file = databaseManager.getRecentFile().getFile();

                if (recent_file != null) {
                    Log.e("Update last file", String.valueOf(UPLOAD_DIRECTORY + "/" + recent_file));
                    updateLastFileDateAndTime(new File(UPLOAD_DIRECTORY + "/" + recent_file).lastModified());
                }
                databaseManager.dropBackupTable();
            }
        }

    }

    private void checkAndUploadOnError() {
        if (indexToUpload < files.size()) {
            UploadRetrofit(indexToUpload);
        } else {
            isUploadHappening = false;
            indexToUpload = 0;

            total_uploaded_response = 0;

            if (databaseManager.getNotUploadedFiles().size() <= 0) {

                String recent_file = databaseManager.getRecentFile().getFile();

                if (recent_file != null) {
                    Log.e("Update last file", String.valueOf(UPLOAD_DIRECTORY + "/" + recent_file));
                    updateLastFileDateAndTime(new File(UPLOAD_DIRECTORY + "/" + recent_file).lastModified());
                }

                databaseManager.dropBackupTable();
            }
        }
    }


    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, File imagefile) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri

        File file = new File(UPLOAD_DIRECTORY + "/" + imagefile);
        Uri uploaduri = Uri.fromFile(file);


        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("image/jpeg"),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    @Override
    public void onNewPhotoCaptured(File file) {

        Log.e("New Image Broadcast", "new image takem");

        String path = file.getName();

        if (path != null) {
            Log.e("FileObserver: ", "File Created: " + path);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(UploadService.this, "photo taken", Toast.LENGTH_LONG).show();
                }
            });


            if (isUploadHappening) {
                Log.e("FileObserver: ", "Upload Happening");

                List<FileData> cap_data = new ArrayList<>();
                FileData data = new FileData();
                data.setFile(path);
                data.setUploadStatus(0);
                data.setLastModified(dateFormat.format(new Date(new File(UPLOAD_DIRECTORY + "/" + path).lastModified())));
                cap_data.add(data);

                files.add(indexToUpload, data);

                databaseManager.addFiles(cap_data);

                total_files_to_upload = files.size();
            } else {
                Log.e("FileObserver: ", "Upload not Happening");
                getFilesAndStartUploadFromDatabase();
            }

        }

    }


}
