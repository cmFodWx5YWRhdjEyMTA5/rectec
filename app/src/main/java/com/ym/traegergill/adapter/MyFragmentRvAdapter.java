package com.ym.traegergill.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.bean.MyFragmentBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Administrator on 2017/8/2.
 */

public class MyFragmentRvAdapter extends RecyclerView.Adapter<MyFragmentRvAdapter.ViewHolder> implements View.OnClickListener {

    private Context mContext;
    private List<MyFragmentBean> mList;
    private OnItemClick mItemClick;
    private RecyclerView mRecyclerView;

    public MyFragmentRvAdapter(Context context) {
        mContext = context;
    }

    public void setmList(List<MyFragmentBean> mList) {
        this.mList = mList;
    }

    public void setItemClick(OnItemClick ItemClick) {
        mItemClick = ItemClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_function_myfragment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;

    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textFunction.setText(mList.get(position).getName());
        holder.itemView.setOnClickListener(this);
        if(mList.get(position).isFlag()){
            holder.messageIcon.setVisibility(View.VISIBLE);
        }else{
            holder.messageIcon.setVisibility(View.GONE);
        }


    }
    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onClick(View view) {
        int position = mRecyclerView.getChildAdapterPosition(view);
        mItemClick.OnClick(mRecyclerView, view, position, mList.get(position));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_function)
        TextView textFunction;
        @BindView(R.id.message_icon)
        ImageView messageIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public interface OnItemClick {
        void OnClick(RecyclerView parent, View view, int position, MyFragmentBean Info);
    }
}
