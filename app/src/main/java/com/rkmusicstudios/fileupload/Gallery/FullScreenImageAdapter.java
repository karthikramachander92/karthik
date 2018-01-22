package com.rkmusicstudios.fileupload.Gallery;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rkmusicstudios.fileupload.R;

import java.util.ArrayList;

/**
 * Created by GEIDEA_A3 on 30-Nov-17.
 */

public class FullScreenImageAdapter extends PagerAdapter {

    ArrayList<Uri> uris;
    Activity activity;

    public FullScreenImageAdapter(Activity activity, ArrayList<Uri> uris) {
        this.activity = activity;
        this.uris = uris;
    }

    @Override
    public int getCount() {
        return uris.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        ZoomImageView imgDisplay;

        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.full_screen_layout, container,
                false);

        imgDisplay = (ZoomImageView) viewLayout.findViewById(R.id.imgDisplay);

        Glide.with(activity).load(uris.get(position))
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgDisplay);


//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        Bitmap bitmap = BitmapFactory.decodeFile(.get(position), options);
//        imgDisplay.setImageBitmap(bitmap);

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }
}
