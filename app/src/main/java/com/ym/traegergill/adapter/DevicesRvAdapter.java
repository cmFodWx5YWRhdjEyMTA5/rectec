package com.ym.traegergill.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuya.smart.sdk.bean.DeviceBean;
import com.ym.traegergill.R;
import com.ym.traegergill.tools.OUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/10/10.
 */

public class DevicesRvAdapter extends RecyclerView.Adapter<DevicesRvAdapter.MyViewHolder> {


    private Context mContext;
    private List<DeviceBean> mDataList;
    private OnMyItemClickListener onMyItemClickListener;
    private OnMyItemLongClickListener onMyItemLongClickListener;
    public void setOnMyItemClickListener(OnMyItemClickListener onMyItemClickListener) {
        this.onMyItemClickListener = onMyItemClickListener;
    }
    public void setOnMyItemLongClickListener(OnMyItemLongClickListener onMyItemLongClickListener) {
        this.onMyItemLongClickListener = onMyItemLongClickListener;
    }
    public DevicesRvAdapter(Context context, List<DeviceBean> mDatas) {
        mContext = context;
        mDataList = mDatas;
    }

    public DeviceBean getItem(int position) {
        return mDataList.get(position);
    }

    public void setData(List<DeviceBean> myDevices) {
        mDataList.clear();
        if (myDevices != null) {
            mDataList.addAll(myDevices);
        }
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        DeviceBean data = mDataList.get(position);
        if(data.getIsOnline()){
            holder.deviceStatus.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onMyItemClickListener != null) {
                        onMyItemClickListener.onNormalClick(view, position);
                    }
                }
            });
        }else{
            holder.deviceStatus.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OUtil.toastError(mContext,mContext.getString(R.string.ty_offline_title));
                    OUtil.TLog("设备是离线状态");
                }
            });
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (onMyItemLongClickListener != null) {
                    onMyItemLongClickListener.onNormalLongClick(view, position);
                }
                return true;
            }
        });
        holder.deviceName.setText(data.getName());
         /*
        holder.isOpen.setEnabled(false);
        holder.isOpen.setChecked(data.dps.get("1").equals(true));*/

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.device_icon)
        ImageView deviceIcon;
        @BindView(R.id.device_name)
        TextView deviceName;
        @BindView(R.id.device_status)
        TextView deviceStatus;
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