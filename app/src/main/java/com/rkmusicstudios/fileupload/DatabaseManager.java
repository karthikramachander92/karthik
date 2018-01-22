package com.rkmusicstudios.fileupload;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by GEIDEA_A3 on 16-Nov-17.
 */

public class DatabaseManager {

    private static String TAG = "DatabaseHandler";
    private DatabaseHelper databaseHelper;
    public static String mclassName = "DatabaseAdapter";

    private String TABLE_BACKUP_IMAGE = "BackupImage";

    private String KEY_FILE = "file";
    private String KEY_LAST_MODIFIED = "last_modified";
    private String KEY_UPLOAD_STATUS = "upload_status";

    private String CREATE_TABLE_BACKUP_IMAGE = "CREATE TABLE IF NOT EXISTS " + TABLE_BACKUP_IMAGE + "("
            + KEY_FILE + " TEXT PRIMARY KEY, "
            + KEY_LAST_MODIFIED + " TEXT, "
            + KEY_UPLOAD_STATUS + " INTEGER DEFAULT 0);";


    public DatabaseManager(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public long addFiles(@NonNull List<FileData> dataObject) {

        long insertRecordResult = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        try {

            if (dataObject != null && dataObject.size() > 0) {
                for (int i = 0; i < dataObject.size(); i++) {

                    ContentValues recordValues = new ContentValues();

                    recordValues.put(KEY_FILE, dataObject.get(i).getFile());
                    recordValues.put(KEY_LAST_MODIFIED, dataObject.get(i).getLastModified());
                    recordValues.put(KEY_UPLOAD_STATUS, dataObject.get(i).getUploadStatus());

                    insertRecordResult = db.insertWithOnConflict(TABLE_BACKUP_IMAGE, null, recordValues, SQLiteDatabase.CONFLICT_IGNORE);

                }
                Log.e(TAG, "Files Inserted Successfully");
            } else {
                Log.e(TAG, "No Files to insert");
            }

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (db != null)
                    db.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }

        }
        return insertRecordResult;
    }


    public List<FileData> getAllFiles() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String queryString = "SELECT * FROM " + TABLE_BACKUP_IMAGE + ";";

        List<FileData> fileDataList = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(queryString, null);

            if (cursor.moveToFirst()) {
                do {

                    FileData fileData = new FileData();
                    fileData.setFile(cursor.getString(cursor.getColumnIndex(KEY_FILE)));
                    fileData.setUploadStatus(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD_STATUS)));
                    fileData.setLastModified(cursor.getString(cursor.getColumnIndex(KEY_LAST_MODIFIED)));

                    fileDataList.add(fileData);

                } while (cursor.moveToNext());

            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return fileDataList;
    }

    public List<FileData> getNotUploadedFiles() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String queryString = "SELECT * FROM " + TABLE_BACKUP_IMAGE + " WHERE  " + KEY_UPLOAD_STATUS + " <> 1;";

        List<FileData> fileDataList = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(queryString, null);

            if (cursor.moveToFirst()) {
                do {

                    FileData fileData = new FileData();
                    fileData.setFile(cursor.getString(cursor.getColumnIndex(KEY_FILE)));
                    fileData.setUploadStatus(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD_STATUS)));
                    fileData.setLastModified(cursor.getString(cursor.getColumnIndex(KEY_LAST_MODIFIED)));

                    fileDataList.add(fileData);

                } while (cursor.moveToNext());

            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return fileDataList;
    }

    public void updateFileUploadStatus(@NonNull String file, int uploadStatus) {

        Log.e(TAG, "Updating File upload status to " + uploadStatus);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(KEY_UPLOAD_STATUS, uploadStatus);
            db.update(TABLE_BACKUP_IMAGE, cv, KEY_FILE + " =? ", new String[]{file});
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (db != null)
                    db.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

    public void dropBackupTable() {
        Log.e(TAG, "All file uploaded. cleared table");
        String query = "DELETE FROM " + TABLE_BACKUP_IMAGE + ";";
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try {
            db.execSQL(query);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (db != null)
                    db.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public FileData getRecentFile() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_BACKUP_IMAGE + " WHERE " + KEY_LAST_MODIFIED + " = (SELECT MAX(" + KEY_LAST_MODIFIED + ")  FROM " + TABLE_BACKUP_IMAGE + ");";

        Cursor cursor = null;
        FileData fileData = new FileData();
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {

                fileData.setFile(cursor.getString(cursor.getColumnIndex(KEY_FILE)));
                fileData.setUploadStatus(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD_STATUS)));
                fileData.setLastModified(cursor.getString(cursor.getColumnIndex(KEY_LAST_MODIFIED)));

            }

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return fileData;
    }


    private class DatabaseHelper extends SQLiteOpenHelper {

        private final static String DATABASE_NAME = "BackupManager";
        private final static int DATABASE_VERSION = 1;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TABLE_BACKUP_IMAGE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }

    }
}
