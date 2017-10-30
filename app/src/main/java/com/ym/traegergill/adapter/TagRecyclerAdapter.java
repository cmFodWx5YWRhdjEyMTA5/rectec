package com.ym.traegergill.adapter;

/**
 * Created by Administrator on 2017/7/31.
 */

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;


import com.ym.traegergill.R;
import com.ym.traegergill.bean.TagBean;
import com.ym.traegergill.tools.OUtil;

import java.util.List;

/**
 * Created by chengxiakuan on 2016/10/14.
 */
public class TagRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private Context context;
    private List<TagBean> list;
    public boolean[] flags;
    private String mainTitle;
    private int layoutId;
    private boolean canEditBiao,checked = true;
    public TagRecyclerAdapter(Context context, List<TagBean> list,String mainTitle) {
        this.context = context;
        this.list = list;
        this.mainTitle = mainTitle;
        flags = new boolean[list.size()];
        layoutId = R.layout.item_tag;
        mLayoutInflater = LayoutInflater.from(context);
        canEditBiao = true;
        checked = false;
    }

    public String getMainTitle() {
        return mainTitle;
    }

    public TagRecyclerAdapter(Context context, List<TagBean> list, int layoutId) {
        this.context = context;
        this.list = list;
        this.layoutId = layoutId;
        flags = new boolean[list.size()];
        mLayoutInflater = LayoutInflater.from(context);
        canEditBiao = false;
    }
    public TagRecyclerAdapter(Context context, List<TagBean> list, int layoutId, boolean canEditBiao) {
        this.context = context;
        this.list = list;
        this.layoutId = layoutId;
        flags = new boolean[list.size()];
        mLayoutInflater = LayoutInflater.from(context);
        this.canEditBiao = canEditBiao;
    }

    public TagRecyclerAdapter(Context context, List<TagBean> list, int layoutId, boolean canEditBiao, boolean checked) {
        this.context = context;
        this.list = list;
        this.layoutId = layoutId;
        flags = new boolean[list.size()];
        mLayoutInflater = LayoutInflater.from(context);
        this.canEditBiao = canEditBiao;
        this.checked = checked;
    }

    public void setList(List<TagBean> list) {
        this.list = list;
        flags = new boolean[list.size()];
    }

    public List<TagBean> getList() {
        return list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = mLayoutInflater.inflate(layoutId,null);
        return new MyViewHolder(inflate);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            if(canEditBiao){
                myViewHolder.biaoqian_checkbox.setEnabled(true);
            }else{
                myViewHolder.biaoqian_checkbox.setEnabled(false);
            }
            myViewHolder.biaoqian_checkbox.setText(list.get(position).getName());
            myViewHolder.biaoqian_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    flags[position] = b;
                    if(b){
                        compoundButton.setTextColor(ContextCompat.getColor(context,R.color.orange));
                    }else{
                        compoundButton.setTextColor(ContextCompat.getColor(context,R.color.color444));
                    }

                }
            });
            myViewHolder.biaoqian_checkbox.setChecked(checked);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox biaoqian_checkbox;

        public MyViewHolder(View view) {
            super(view);
            biaoqian_checkbox = (CheckBox) view.findViewById(R.id.tag_checkbox);
           /* int width = (int)(OUtil.getScreenWidth(view.getContext())/3.3);
            biaoqian_checkbox.setMinWidth(width);*/

        }
    }

}