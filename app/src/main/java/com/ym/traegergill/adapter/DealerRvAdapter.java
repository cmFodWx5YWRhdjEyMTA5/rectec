package com.ym.traegergill.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.bean.DealerBean;
import com.ym.traegergill.tools.OUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/10/10.
 */

public class DealerRvAdapter extends RecyclerView.Adapter<DealerRvAdapter.MyViewHolder> {

    private Context mContext;
    private List<DealerBean> mDataList;
    private OnMyItemClickListener onMyItemClickListener;
    private OnMyItemLongClickListener onMyItemLongClickListener;

    public void setOnMyItemClickListener(OnMyItemClickListener onMyItemClickListener) {
        this.onMyItemClickListener = onMyItemClickListener;
    }

    public void setOnMyItemLongClickListener(OnMyItemLongClickListener onMyItemLongClickListener) {
        this.onMyItemLongClickListener = onMyItemLongClickListener;
    }

    public DealerRvAdapter(Context context, List<DealerBean> mDatas) {
        mContext = context;
        mDataList = mDatas;
    }

    public DealerBean getItem(int position) {
        return mDataList.get(position);
    }

    public void setData(List<DealerBean> myDevices) {
        mDataList.clear();
        if (myDevices != null) {
            mDataList.addAll(myDevices);
        }
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.items_dealer_rv, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        DealerBean data =  mDataList.get(position);
        holder.address.setText(data.getAddress());
        holder.name.setText(data.getName());
        holder.tel.setText("TEL:" + data.getTel());
        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onMyItemClickListener!=null)
                    onMyItemClickListener.onNormalClick(view,position);
            }
        });
        holder.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onMyItemClickListener!=null)
                    onMyItemClickListener.onNormalClick(view,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.tel)
        TextView tel;
        @BindView(R.id.call)
        ImageView call;
        @BindView(R.id.location)
        ImageView location;
        @BindView(R.id.address)
        TextView address;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnMyItemClickListener {
        void onNormalClick(View v, int position);
    }

    public interface OnMyItemLongClickListener {
        void onNormalLongClick(View v, int position);
    }


}