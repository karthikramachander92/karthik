package com.rkmusicstudios.fileupload;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;

/**
 * Created by GEIDEA_A3 on 06-Nov-17.
 */

public interface RetrofitInterface {

    @Multipart
    @POST("/upload.php")
    Call<Response> uploadImage(@Part MultipartBody.Part image);

    @Multipart
    @POST("/new/upload.php")
    Call<Response> uploadImages(@Part List<MultipartBody.Part> image);

    @GET("files/Node-Android-Chat.zip")
    @Streaming
    Call<ResponseBody> downloadFile();

}
