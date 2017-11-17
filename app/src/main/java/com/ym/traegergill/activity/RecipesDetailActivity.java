package com.ym.traegergill.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.modelBean.Recipe;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.GlideLoadUtil;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.SharedPreferencesUtils;
import com.ym.traegergill.tuya.utils.DialogUtil;

import org.json.JSONArray;
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

public class RecipesDetailActivity extends BaseActivity {
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    @BindView(R.id.iv_img)
    ImageView ivImg;
    @BindView(R.id.share)
    ImageView share;
    @BindView(R.id.collect)
    ImageView collect;
    @BindView(R.id.tv_difficulty)
    TextView tvDifficulty;
    @BindView(R.id.tv_serves)
    TextView tvServes;
    @BindView(R.id.tv_cook_time)
    TextView tvCookTime;
    @BindView(R.id.tv_prep_time)
    TextView tvPrepTime;
    @BindView(R.id.tv_hard_wood)
    TextView tvHardWood;
    @BindView(R.id.tv_describe)
    TextView tvDescribe;
    @BindView(R.id.tv_preparation)
    TextView tvPreparation;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.ll_ingedients)
    LinearLayout llIngedients;
    private Gson gson;
    int recipeid;
    boolean isLogin;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes_detail);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        gson = new Gson();
        isLogin = SharedPreferencesUtils.getSharedPreferencesUtil(getActivity()).getBoolean(Constants.ISLOGIN,false) && TuyaUser.getUserInstance().isLogin();
        netRecipesData();
    }

    private void netRecipesData() {
        StringCallback callback =  new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("isFromCache : " + isFromCache + " json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        JSONObject content = obj.optJSONObject("content");
                        JSONArray array = content.optJSONArray("resultIngredientList");
                        Recipe recipe = gson.fromJson(content.optJSONObject("recipe").toString(), Recipe.class);
                        boolean isCollected = content.optBoolean("isCollected");
                        setValue(array, recipe,isCollected);
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
        recipeid = getIntent().getIntExtra("recipeid", 0);
        if(isLogin){
            //登录状态
            String paramsCross = "recipeid="+recipeid;
            if(!MyNetTool.netCrossWithParams(getActivity(),TuyaUser.getUserInstance().getUser().getUid(),URLs.findRecipeById,paramsCross,callback)){
                DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.network_error)
                        , getActivity().getString(R.string.action_close), getActivity().getString(R.string.retry), null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        System.exit(0);
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        netRecipesData();
                                        break;
                                }
                            }
                        }).show();
            }
        }else{
            //非登录状态
            HttpParams params = new HttpParams();
            params.put("recipeid", recipeid + "");
            TLog("params : " + new Gson().toJson(params));
            if(!MyNetTool.netHttpParams(getActivity(),URLs.BASE + URLs.findRecipeById,callback,params)){
                DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.network_error)
                        , getActivity().getString(R.string.action_close), getActivity().getString(R.string.retry), null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        System.exit(0);
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        netRecipesData();
                                        break;
                                }
                            }
                        }).show();
            }
        }

    }

    private void setValue(JSONArray beans, Recipe recipe, boolean isCollected) {
        collect.setSelected(isCollected);
        int width = OUtil.getScreenWidth(getActivity());
        ivImg.setMinimumHeight(OUtil.dip2px(getActivity(), 200));
        RequestOptions options = new RequestOptions()
                .override(width, OUtil.dip2px(getActivity(), 200))
                .placeholder(Constants.PLACEHOLDER)
                .error(Constants.ERROR)
                .priority(Priority.HIGH)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        GlideLoadUtil.load(getActivity(), recipe.getMainPic(), options, ivImg);
        tvDifficulty.setText(recipe.getDifficulty());
        tvServes.setText(recipe.getServes());
        String timeiInner = recipe.getCookTime();
        timeiInner = timeiInner.substring(timeiInner.indexOf("than") + 4, timeiInner.length());
        tvCookTime.setText(timeiInner);
        String ptimeiInner = recipe.getPrepareTime();
        ptimeiInner = ptimeiInner.substring(ptimeiInner.indexOf("than") + 4, ptimeiInner.length());
        tvPrepTime.setText(ptimeiInner);
        tvHardWood.setText(recipe.getHardwood());
        tvDescribe.setText(recipe.getDescribe());
        tvPreparation.setText(recipe.getCookDurationDesc());
        tvTitle.setText(recipe.getTitle());
        llIngedients.removeAllViews();
        for (int i = 0; i < beans.length(); i++) {
            JSONObject bean = beans.optJSONObject(i);
            View temp = LayoutInflater.from(getActivity()).inflate(R.layout.view_ingredient, null);
            TextView tvUint = (TextView) temp.findViewById(R.id.tv_uint);
            TextView tvIngredientName = (TextView) temp.findViewById(R.id.tv_ingredientName);
            tvIngredientName.setText(bean.optString("ingredientName"));
            tvUint.setText(bean.optInt("quantity") +" " + bean.optString("unit"));
            llIngedients.addView(temp);
        }
    }


    @OnClick({R.id.share, R.id.collect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.share:
                break;
            case R.id.collect:
                animateHeartButton(view);
                netCollect();
                break;
        }
    }

    private void netCollect() {
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("isFromCache : " + isFromCache+"  json : " + s);
                if(isFromCache){
                    showToastError(getString(R.string.network_error));
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(s);
                    TLog(obj.optString("msg"));
                    if (obj.optInt("code") == 200) {
                        collect.setSelected(!collect.isSelected());
                        showToastSuccess(obj.optString("msg"));
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
            }
        };
        String params = "recipeid="+recipeid+"&isCollected="+!collect.isSelected();
        if(isLogin){
            if(!MyNetTool.netCrossWithParams(getActivity(), TuyaUser.getUserInstance().getUser().getUid(),URLs.collectRecipe,params,callback)){
                DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.network_error)
                        , getActivity().getString(R.string.action_close), getActivity().getString(R.string.retry), null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        System.exit(0);
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        netCollect();
                                        break;
                                }
                            }
                        }).show();
            }
        }else{
            showToastError("please login first");
        }

        /*        HttpParams params = new HttpParams();
        params.put("recipeid", recipeid + "");
        params.put("isCollected", !collect.isSelected() + "");
        MyNetTool.netHttpParams(getActivity(),URLs.BASE+URLs.collectRecipe,callback,params);*/
    }

    private void animateHeartButton(View imHeart) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(imHeart, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(imHeart, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(300);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(imHeart, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(300);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
 /*       bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                imHeart.setImageResource(R.mipmap.ic_heart_red);
            }
        });*/
        animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);
        animatorSet.start();
    }

}
