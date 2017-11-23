package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.ym.traegergill.R;
import com.ym.traegergill.adapter.TagRecyclerAdapter;
import com.ym.traegergill.bean.TagBean;
import com.ym.traegergill.modelBean.Filter;
import com.ym.traegergill.modelBean.FilterGroupByFilterTypeModel;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.other.MyLayoutManager;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

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
        unbinder = ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText("FILTER");
        gson = new Gson();
        adapters = new ArrayList<>();
        netFilterData();
    }
    Gson gson ;
    private void netFilterData() {
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    TLog(obj.optString("msg"));
                    if (obj.optInt("code") == 200) {
                        //showToastSuccess(obj.optString("msg"));
                        JSONArray data = obj.optJSONArray("content");
                        for(int i = 0 ;i<data.length();i++){
                            FilterGroupByFilterTypeModel model = gson.fromJson(data.optJSONObject(i).toString(),FilterGroupByFilterTypeModel.class);
                            initFilter(model);
                        }
                        ImageView offset = new ImageView(getActivity());
                        offset.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, OUtil.dip2px(getApplicationContext(), 50)));
                        filterBox.addView(offset);
                    }else{
                        showToastError(obj.optString("msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onAfter(boolean isFromCache, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                super.onAfter(isFromCache, s, call, response, e);
                ProgressUtil.hideLoading();
            }
        };
        HttpParams params = new HttpParams();
        ProgressUtil.showLoading(this,"loading..");
        if(!MyNetTool.netHttpParams(getActivity(),URLs.findFilterListGroupByFilterType,callback,params)){
            ProgressUtil.hideLoading();
            DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.network_error)
                    , getActivity().getString(R.string.action_close), getActivity().getString(R.string.retry), null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    System.exit(0);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    netFilterData();
                                    break;
                            }
                        }
                    }).show();
        }
    }

    private void initFilter(FilterGroupByFilterTypeModel model) {
        View temp = LayoutInflater.from(getActivity()).inflate(R.layout.view_filter_item, null);
        TextView header = (TextView) temp.findViewById(R.id.rv_header);
        RecyclerView rv_filter = (RecyclerView) temp.findViewById(R.id.rv_filter);
        header.setText(model.getFilterTypeName());
        MyLayoutManager layout = new MyLayoutManager();
        rv_filter.setLayoutManager(layout);
        layout.setAutoMeasureEnabled(true);
        List<TagBean> datas = new ArrayList<>();
        for(Filter filter : model.getFilterList()){
            datas.add(new TagBean(filter.getFilterid(),filter.getFilterName()));
        }
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
                List<HashMap<String, Integer>> filteridList = new ArrayList<>();
                for (TagRecyclerAdapter adapter : adapters) {
                    int count = 1;
                    strBuilder.append("header : " + adapter.getMainTitle() + "\n");
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        if(adapter.flags[i]){
                            TagBean bean = adapter.getList().get(i);
                            strBuilder.append("  " + (count++)+" : "+bean.getName());
                            HashMap map = new HashMap<String, Integer>();
                            map.put("filterid",bean.getId());
                            filteridList.add(map);
                        }
                    }
                    strBuilder.append("\n\n");
                }
                OUtil.TLog(strBuilder.toString());
                //showToastSuccess("Filter 内容 :\n" + strBuilder.toString());
                Intent intent = new Intent(getActivity(),FilterRecipesActivity.class);
                intent.putExtra("recipeFilterListStr",gson.toJson(filteridList));
                startActivity(intent);
                overridePendingTransition(getActivity(),ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;
        }
    }
}
