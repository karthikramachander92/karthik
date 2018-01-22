package com.rkmusicstudios.fileupload.Gallery;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rkmusicstudios.fileupload.R;

/**
 * Created by GEIDEA_A3 on 29-Nov-17.
 */

public class GalleryViewHolder extends RecyclerView.ViewHolder{

    private ImageView thumbnail;
    private Context context;


    public GalleryViewHolder(Context context,View itemView) {
        super(itemView);

        this.context = context;
        this.thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail_image);

    }

    public void setThumbnailUri(Uri thumbnailuri) {
        Glide.with(context).load(thumbnailuri)
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(this.thumbnail);
    }

    public void setClickListener(View.OnClickListener listener){
        this.thumbnail.setOnClickListener(listener);
    }

}
