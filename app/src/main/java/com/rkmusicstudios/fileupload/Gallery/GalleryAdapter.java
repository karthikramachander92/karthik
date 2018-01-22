package com.rkmusicstudios.fileupload.Gallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rkmusicstudios.fileupload.R;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by GEIDEA_A3 on 29-Nov-17.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryViewHolder>{

    LayoutInflater mInflater;
    Context context;
    ArrayList<Uri> uris;

    public GalleryAdapter(Context context,ArrayList<Uri> uris) {
        this.context = context;
        this.uris = uris;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.gallery_thumbnail,parent,false);
        return new GalleryViewHolder(context,view);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, final int position) {
        holder.setThumbnailUri(uris.get(position));

        holder.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Image Position",position+"");

                Intent intent = new Intent(context, FullScreenSwipeGallery.class);
                intent.putParcelableArrayListExtra("images", uris);
                intent.putExtra("pos",position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uris.size();
    }

}
