package com.ym.traegergill.tools;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.ym.traegergill.R;
import com.ym.traegergill.net.URLs;

/**
 * Created by Administrator on 2017/11/1.
 */

public class GlideLoadUtil {
    public static void load(Context mContext, String url, RequestOptions options, ImageView mImageView){
        url = getMyServerUrl(url);
        Glide.with(mContext)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade(R.anim.shake_error,1000))
                .apply(options)
                .thumbnail(0.1f)
                .into(mImageView);
    }
    public static String getMyServerUrl(String url){
        if(url.startsWith("/")){
            url = URLs.domain + url;
        }
        return url;
    }
}
