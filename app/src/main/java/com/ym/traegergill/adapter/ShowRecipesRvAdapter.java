package com.ym.traegergill.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.ym.traegergill.R;
import com.ym.traegergill.modelBean.Recipe;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.GlideLoadUtil;
import com.ym.traegergill.tools.OUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowRecipesRvAdapter extends RecyclerView.Adapter<ShowRecipesRvAdapter.MyViewHolder> {


    private Context mContext;
    private List<Recipe> mDataList;
    private OnMyItemClickListener onMyItemClickListener;
    private RequestOptions options;
    public void setOnMyItemClickListener(OnMyItemClickListener onMyItemClickListener) {
        this.onMyItemClickListener = onMyItemClickListener;
    }

    public ShowRecipesRvAdapter(Context context, List<Recipe> mDatas) {
        mContext = context;
        mDataList = mDatas;
        int width = OUtil.getScreenWidth(mContext);
        options = new RequestOptions()
                .placeholder(Constants.PLACEHOLDER)
                .error(Constants.ERROR)
                .priority(Priority.NORMAL)
                .override(width,OUtil.dip2px(mContext,200))
                .diskCacheStrategy(DiskCacheStrategy.NONE);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_show_recipes, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
       /*
        .centerCrop(); // 长的一边撑满
        .fitCenter(); // 短的一边撑满
        */
        Recipe data = mDataList.get(position);

        GlideLoadUtil.load(mContext,data.getMainPic(),options,holder.img);


        holder.desc.setText(data.getDescribe());
        holder.desInImg.setText(data.getTitle());
        String timeiInner = data.getCookTime();
        timeiInner = timeiInner.substring(timeiInner.indexOf("than")+4,timeiInner.length());
        holder.time.setText(timeiInner);
       /* if(data.getTime() % 1 == 0){// 是这个整数，小数点后面是0
            int i = (new Double(data.getTime())).intValue();
            holder.time.setText(i+"hrs");
        }else{//不是整数，小数点后面不是0
            holder.time.setText(data.getTime()+"hrs");
        }*/

        holder.ingredients.setText(data.getIngredientSize()+" ingredients");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onMyItemClickListener != null) {
                    onMyItemClickListener.onNormalClick(view, position);
                }
            }
        });



    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.des_in_img)
        TextView desInImg;
        @BindView(R.id.desc)
        TextView desc;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.ingredients)
        TextView ingredients;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

    public interface OnMyItemClickListener {
        void onNormalClick(View v, int position);
    }
}