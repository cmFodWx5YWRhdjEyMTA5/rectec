package com.ym.traegergill.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.tools.BlurBitmapUtil;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/10/9.
 */

public class ConnectWifiGuidActivity extends BaseActivity {
    @BindView(R.id.backg)
    ImageView backg;
    @BindView(R.id.ll_top_text)
    LinearLayout llTopText;
    @BindView(R.id.sign_in)
    TextView signIn;
    @BindView(R.id.create_account)
    TextView createAccount;
    @BindView(R.id.connect)
    TextView connect;
    @BindView(R.id.purchase)
    TextView purchase;
    @BindView(R.id.back)
    ImageView back;
    private SharedPreferencesUtils spUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_wifi_guid);
        unbinder = ButterKnife.bind(this);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (spUtils.getBoolean(Constants.ISLOGIN, false)
                && spUtils.getBoolean(Constants.ISFIRSTTIME, true)) {
            //登录状态 并且是第一次
            connect.setVisibility(View.VISIBLE);
            createAccount.setVisibility(View.GONE);
            signIn.setVisibility(View.GONE);
        } else if(!spUtils.getBoolean(Constants.ISLOGIN, false)){
            //非登录状态
            connect.setVisibility(View.GONE);
            createAccount.setVisibility(View.VISIBLE);
            signIn.setVisibility(View.VISIBLE);
        }else if(spUtils.getBoolean(Constants.ISLOGIN, false)
                && !spUtils.getBoolean(Constants.ISFIRSTTIME, true)){
            //登录状态 && 不是第一次
            startActivity(new Intent(getActivity(),DevicesActivity.class));
            overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
            this.finish();
        }
    }

    private void init() {
        spUtils = SharedPreferencesUtils.getSharedPreferencesUtil(getActivity());
        Bitmap initBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg);
        Bitmap blurBitmap = BlurBitmapUtil.blurBitmap(this, initBitmap, 15f);
        backg.setImageBitmap(blurBitmap);
        startIntroAnimation(back, 1, -1);
        startIntroAnimation(llTopText, 2, -1);
        updateUI();
    }


    private void startIntroAnimation(View temp, int index, int up) {
        int dis = OUtil.getSceenHeight(getActivity());
        temp.setTranslationY(up * dis);
        temp.animate()
                .translationY(0)
                .setDuration(500)
                .setStartDelay(100 * index);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this,ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }

    @OnClick({R.id.sign_in, R.id.create_account, R.id.connect, R.id.purchase})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sign_in:
                startActivity(new Intent(getActivity(), SignInActivity.class));
                overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;
            case R.id.create_account:
                startActivity(new Intent(getActivity(), CreateAccountActivity.class));
                overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;
            case R.id.connect:
                startActivity(new Intent(getActivity(), AddDevicesGuidActivity.class));
                overridePendingTransition(this,ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;
            case R.id.purchase:
                break;
        }
    }
}
