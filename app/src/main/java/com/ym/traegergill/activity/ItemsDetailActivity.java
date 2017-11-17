package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.cache.CacheMode;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.GlideLoadUtil;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/9/22.
 */

public class ItemsDetailActivity extends BaseActivity {
    @BindView(R.id.iv_img)
    ImageView ivImg;
    @BindView(R.id.userImg)
    ImageView userImg;
    @BindView(R.id.recipes_img)
    ImageView recipesImg;
    @BindView(R.id.des_in_img)
    TextView desInImg;
    @BindView(R.id.tv_shareContent)
    TextView tvShareContent;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_des)
    TextView tvDes;
    @BindView(R.id.ll_recipes)
    LinearLayout llRecipes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_detail);
        ButterKnife.bind(this);
        init();
    }
    private void init() {
        netRecipeShareData();
    }
    int recipeid, recipeShareid;
    private void netRecipeShareData() {
        recipeShareid = getIntent().getIntExtra("recipeShareid", 0);
        HttpParams params = new HttpParams();
        params.put("recipeShareid", recipeShareid + "");
        TLog("params : " + new Gson().toJson(params));

        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {

                TLog("isFromCache : "+isFromCache+" json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        JSONObject content = obj.optJSONObject("content");
                        setValue(content);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAfter(boolean isFromCache, @Nullable String s, Call call, @Nullable Response response, @Nullable Exception e) {
                super.onAfter(isFromCache, s, call, response, e);
            }
        };
        if(!MyNetTool.netHttpParams(getActivity(),URLs.findRecipeShareById,callback,params)){
            DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.network_error)
                    , getActivity().getString(R.string.action_close), getActivity().getString(R.string.retry), null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    System.exit(0);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    netRecipeShareData();
                                    break;
                            }
                        }
                    }).show();
        }
    }

    private void setValue(JSONObject content) {
        String url = content.optString("shareMainPic");
        String shareContent = content.optString("shareContent");
        String title = content.optString("title");
        String sharePlatform = content.optString("sharePlatform");
        String shareUsername = content.optString("shareUsername");
        if(TextUtils.isEmpty(shareUsername)) {
            tvDes.setText(sharePlatform);
        }
        else{
            tvDes.setText(shareUsername);
        }
        tvTitle.setText(title);
        tvShareContent.setText(shareContent);
        int width = OUtil.getScreenWidth(getActivity());
        ivImg.setMinimumHeight(width);
        RequestOptions requestOptions = new RequestOptions()
                .override(width, width)
                .placeholder(Constants.PLACEHOLDER)
                .error(Constants.ERROR)
                .priority(Priority.HIGH)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        GlideLoadUtil.load(getActivity(), url, requestOptions, ivImg);
        recipeid = content.optInt("recipeid", -1);
        if (recipeid != -1) {
            RequestOptions options = new RequestOptions()
                    .override(width, OUtil.dip2px(getActivity(), 200))
                    .placeholder(Constants.PLACEHOLDER)
                    .error(Constants.ERROR)
                    .priority(Priority.LOW)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL);


            llRecipes.setVisibility(View.VISIBLE);
            GlideLoadUtil.load(getActivity(), content.optString("recipePic"), options, recipesImg);
            desInImg.setText(content.optString("recipeName"));
        } else {
            llRecipes.setVisibility(View.GONE);
        }

    }


    @OnClick({R.id.iv_img, R.id.userImg, R.id.recipes_img})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_img:
                break;
            case R.id.userImg:
                break;
            case R.id.recipes_img:
                getActivity().startActivity(
                        new Intent(getActivity(), RecipesDetailActivity.class).putExtra("recipeid", recipeid),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                getActivity(),
                                Pair.create(view, getActivity().getString(R.string.iv_img_transitionName))
                        ).toBundle());
                break;
        }
    }
}
