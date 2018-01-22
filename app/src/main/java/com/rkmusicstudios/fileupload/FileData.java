package com.rkmusicstudios.fileupload;

/**
 * Created by GEIDEA_A3 on 16-Nov-17.
 */

class FileData {

    private String file;
    private String lastModified;
    private int uploadStatus;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public int getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(int uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
}
