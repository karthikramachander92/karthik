package com.rkmusicstudios.fileupload;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

/**
 * Created by GEIDEA_A3 on 13-Nov-17.
 */
public class MultipartRequest
{
    public Context context;
    public MultipartBody.Builder multipartBody;
    public OkHttpClient okHttpClient;

    public MultipartRequest(Context context)
    {
        this.context = context;
        this.multipartBody = new MultipartBody.Builder();
        this.multipartBody.setType(MultipartBody.FORM);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        this.okHttpClient = builder.build();

    }

    // Add String
    public void addString(String name, String value)
    {
        this.multipartBody.addFormDataPart(name, value);
    }

    // Add Image File
    public void addFile(String name, String filePath, String fileName)
    {
        this.multipartBody.addFormDataPart(name, fileName, RequestBody.create(MediaType.parse("image/jpeg"), new File(filePath)));
    }

    // Add Zip File
    public void addZipFile(String name, String filePath, String fileName)
    {
        this.multipartBody.addFormDataPart(name, fileName, RequestBody.create(MediaType.parse("application/zip"), new File(filePath)));
    }

    // Execute Url
    public String execute(String url)
    {
        RequestBody requestBody = null;
        Request request = null;
        okhttp3.Response response = null;
        int code = 200;
        String strResponse = null;

        try
        {
            requestBody = this.multipartBody.build();
            // Set Your Authentication key here.
            request = new Request.Builder().header("Key", "Value").url(url).post(requestBody).build();

            Log.e("====== REQUEST ======",""+request);
            response = okHttpClient.newCall(request).execute();
            Log.e("====== RESPONSE ======",""+response);

            if (!response.isSuccessful())
                throw new IOException();

            code = response.networkResponse().code();

            /*
             * "Successful response from server"
             */
            if (response.isSuccessful())
            {
                strResponse =response.body().string();
            }
            /*
             * "Invalid URL or Server not available, please try again."
             */
            else if (code == HttpStatus.SC_NOT_FOUND)
            {
                strResponse = "Invalid URL or Server not available, please try again";
            }
            /*
             * "Connection timeout, please try again."
             */
            else if (code == HttpStatus.SC_REQUEST_TIMEOUT)
            {
                strResponse = "Connection timeout, please try again";
            }
            /*
             * "Invalid URL or Server is not responding, please try again."
             */
            else if (code == HttpStatus.SC_SERVICE_UNAVAILABLE)
            {
                strResponse = "Invalid URL or Server is not responding, please try again";
            }
        }
        catch (Exception e)
        {
            Log.e("Exception", ""+e.toString());
        }
        finally
        {
            requestBody = null;
            request = null;
            response = null;
            multipartBody = null;
            if (okHttpClient != null)
                okHttpClient = null;

            System.gc();
        }
        return strResponse;
    }
}