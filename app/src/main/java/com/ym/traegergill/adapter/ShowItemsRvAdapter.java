package com.ym.traegergill.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.ym.traegergill.R;
import com.ym.traegergill.bean.PhotoBean;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.OUtil;

import java.util.List;

public class ShowItemsRvAdapter extends RecyclerView.Adapter<ShowItemsRvAdapter.MyViewHolder> {

    private Context mContext;
    private List<PhotoBean> mDataList;
    private OnMyItemClickListener onMyItemClickListener;
    private  RequestOptions options;
    public void setOnMyItemClickListener(OnMyItemClickListener onMyItemClickListener) {
        this.onMyItemClickListener = onMyItemClickListener;
    }

    public ShowItemsRvAdapter(Context context, List<PhotoBean> mDatas) {
        mContext = context;
        mDataList = mDatas;
        int width = OUtil.getScreenWidth(mContext)/2;
        options = new RequestOptions()
                .placeholder(Constants.PLACEHOLDER)
                .error(Constants.ERROR)
                .error(R.color.orange)
                .override(width,width)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_show_items, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
       /*
        .centerCrop(); // 长的一边撑满
        .fitCenter(); // 短的一边撑满
        */
        int width = OUtil.getScreenWidth(mContext)/2;
        ViewGroup.LayoutParams LayoutParams = holder.itemView.getLayoutParams();
        LayoutParams.height = LayoutParams.width = width;
        holder.itemView.setLayoutParams(LayoutParams);

        Glide.with(mContext)
                .load(mDataList.get(position).getImageUrl())
                .transition(DrawableTransitionOptions.withCrossFade(R.anim.shake_error,1000))
                .apply(options)
                .thumbnail(0.1f)
                .into(holder.mImageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onMyItemClickListener!=null){
                    onMyItemClickListener.onNormalClick(view,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageView;

        public MyViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.iv_image);
        }
    }
    public interface OnMyItemClickListener {
        void onNormalClick(View v, int position);
    }
}