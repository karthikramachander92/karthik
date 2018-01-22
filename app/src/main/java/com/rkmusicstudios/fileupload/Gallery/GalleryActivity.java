package com.rkmusicstudios.fileupload.Gallery;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ImageView;

import com.rkmusicstudios.fileupload.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by GEIDEA_A3 on 29-Nov-17.
 */

public class GalleryActivity extends AppCompatActivity {

    RecyclerView thumbnailView;
    String path = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        thumbnailView = (RecyclerView) findViewById(R.id.recycler_view);

        path = Environment.getExternalStorageDirectory().toString().trim() + "/DCIM/.karthik";

        int numberOfColumns = 3;
        thumbnailView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        GalleryAdapter adapter = new GalleryAdapter(this, getAllFiles(path));
        thumbnailView.setAdapter(adapter);

    }

    public ArrayList<Uri> getAllFiles(String path) {
        Log.e("Path", path);
        ArrayList<Uri> result = new ArrayList<Uri>(); //ArrayList cause you don't know how many files there is
        File folder = new File(path); //This is just to cast to a File type since you pass it as a String
        File[] filesInFolder = folder.listFiles(); // This returns all the folders and files in your path
        for (File file : filesInFolder) { //For each of the entries do:
            if (!file.isDirectory()) { //check that it's not a dir
                result.add(Uri.fromFile(file)); //push the filename as a string
            }
        }

        return result;
    }

}
