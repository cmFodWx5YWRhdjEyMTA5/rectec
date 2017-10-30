package com.ym.traegergill.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.adapter.TagRecyclerAdapter;
import com.ym.traegergill.bean.TagBean;
import com.ym.traegergill.other.MyLayoutManager;
import com.ym.traegergill.tools.OUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/9/25.
 */

public class FilterActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.filter_box)
    LinearLayout filterBox;
    List<TagRecyclerAdapter> adapters;
    @BindView(R.id.clear)
    TextView clear;
    @BindView(R.id.apply)
    TextView apply;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText("FILTER");
        adapters = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            initFilter(i);
        }
        ImageView offset = new ImageView(this);
        offset.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, OUtil.dip2px(getApplicationContext(), 50)));
        filterBox.addView(offset);
    }

    private void initFilter(int index) {
        View temp = LayoutInflater.from(getActivity()).inflate(R.layout.view_filter_item, null);
        TextView header = (TextView) temp.findViewById(R.id.rv_header);
        RecyclerView rv_filter = (RecyclerView) temp.findViewById(R.id.rv_filter);
        header.setText("Main Ingredient " + index);
        MyLayoutManager layout = new MyLayoutManager();
        rv_filter.setLayoutManager(layout);
        layout.setAutoMeasureEnabled(true);
        List<TagBean> datas = new ArrayList<>();
        datas.add(new TagBean("Beef" + index));
        datas.add(new TagBean("Pork"+ index));
        datas.add(new TagBean("Poultry"+ index));
        datas.add(new TagBean("Lamb Lamb Lamb"+ index));
        datas.add(new TagBean("Seafood"+ index));
        datas.add(new TagBean("Nuts"+ index));
        datas.add(new TagBean("Drink"+ index));
        TagRecyclerAdapter adapter = new TagRecyclerAdapter(getActivity(), datas,header.getText().toString());
        rv_filter.setAdapter(adapter);
        filterBox.addView(temp);
        adapters.add(adapter);
    }

    @OnClick({R.id.clear, R.id.apply})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.clear:
                for (TagRecyclerAdapter adapter : adapters) {
                    adapter.flags = new boolean[adapter.getItemCount()];
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.apply:
                StringBuilder strBuilder = new StringBuilder();
                for (TagRecyclerAdapter adapter : adapters) {
                    int count = 1;
                    strBuilder.append("header : " + adapter.getMainTitle() + "\n");
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        if(adapter.flags[i]){
                            TagBean bean = adapter.getList().get(i);
                            strBuilder.append("  " + (count++)+" : "+bean.getName());
                        }
                    }
                    strBuilder.append("\n\n");
                }
                OUtil.TLog(strBuilder.toString());
                showToastSuccess("Filter 内容 :\n" + strBuilder.toString());
                break;
        }
    }
}
