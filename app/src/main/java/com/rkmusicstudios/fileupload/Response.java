package com.rkmusicstudios.fileupload;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by GEIDEA_A3 on 06-Nov-17.
 */

public class Response implements Serializable {

    @SerializedName("message")
    private String message;

    @SerializedName("path")
    private String path;

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

}
