package com.ym.traegergill.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.BaseActivity;
import com.ym.traegergill.activity.CreateAccountActivity;
import com.ym.traegergill.activity.SetUpActivity;
import com.ym.traegergill.activity.SignInActivity;
import com.ym.traegergill.adapter.MyFragmentRvAdapter;
import com.ym.traegergill.bean.MyFragmentBean;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.db.SQLiteDbUtil;
import com.ym.traegergill.db.bean.UserData;
import com.ym.traegergill.tools.CircularAnimUtil;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.SharedPreferencesUtils;
import com.ym.traegergill.tools.WifiUtil;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2017/9/27.
 */

public class MyFragment extends BaseFragment {
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.content)
    CoordinatorLayout content;
    private SharedPreferencesUtils spUtils;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.sign_in)
    TextView signIn;
    @BindView(R.id.create_account)
    TextView createAccount;
    Unbinder unbinder;
    @BindView(R.id.setting)
    ImageView setting;
    @BindView(R.id.hi_account)
    TextView hiAccount;
    @BindView(R.id.order_history)
    TextView orderHistory;
    @BindView(R.id.support)
    TextView support;
    @BindView(R.id.no_login_layout)
    LinearLayout noLoginLayout;
    @BindView(R.id.login_layout)
    LinearLayout loginLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private List<MyFragmentBean> infos;
    MyFragmentRvAdapter adapter;
    private UpdateUserStatusReceiver updateUserStatusReceiver;


    class UpdateUserStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS)) {
                updateUI();
            }else  if (action.equals(TraegerGillBroadcastHelper.ACTION_TEST_WIFI)) {
                //updateUI();
                String data = intent.getStringExtra("data");
                support.setText(data);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS);
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_TEST_WIFI);
        updateUserStatusReceiver = new UpdateUserStatusReceiver();
        getActivity().registerReceiver(updateUserStatusReceiver, intentFilter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销广播
        try {
            getActivity().unregisterReceiver(updateUserStatusReceiver);
        } catch (Exception e) {

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my, container, false);
        unbinder = ButterKnife.bind(this, view);
        init();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (spUtils.getBoolean(Constants.ISLOGIN, false)) {
            loginLayout.setVisibility(View.VISIBLE);
            noLoginLayout.setVisibility(View.GONE);
            setting.setVisibility(View.VISIBLE);
        } else {
            loginLayout.setVisibility(View.GONE);
            noLoginLayout.setVisibility(View.VISIBLE);
            setting.setVisibility(View.VISIBLE);
        }
    }
    SQLiteDbUtil dbUtil;
    private void init() {
        spUtils = SharedPreferencesUtils.getSharedPreferencesUtil(getActivity());
        dbUtil = SQLiteDbUtil.getSQLiteDbUtil();
        title.setText("MY");

        if (infos == null) {
            infos = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                infos.add(new MyFragmentBean("SETTING" + i));
            }
        }
        if (adapter == null) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            adapter = new MyFragmentRvAdapter(getActivity());
            adapter.setmList(infos);
            recyclerView.setAdapter(adapter);
            adapter.setItemClick(new MyFragmentRvAdapter.OnItemClick() {
                @Override
                public void OnClick(RecyclerView parent, View view, int position, MyFragmentBean Info) {
                    spUtils.setValue(Constants.TEST_NUM,position);
                    switch (position){
                        case 0:
                            break;
                        case 1:
                           OUtil.TLog( new Gson().toJson(dbUtil.query(UserData.class)));
                            break;
                        case 2:
                            dbUtil.drop(UserData.class);
                            OUtil.TLog("drop");
                            break;
                    }
                }
            });
        } else {
            adapter.setmList(infos);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @OnClick({R.id.order_history, R.id.support, R.id.sign_in, R.id.create_account, R.id.setting,R.id.login_layout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.order_history:
                DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.ez_notSupport_5G_tip)
                        , getActivity().getString(R.string.ez_notSupport_5G_change), getActivity().getString(R.string.ez_notSupport_5G_continue), null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        ToastUtil.shortToast(getActivity(),"BUTTON_POSITIVE");
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        ToastUtil.shortToast(getActivity(),"BUTTON_NEGATIVE");
                                        break;
                                }
                            }
                        }).show();
                break;
            case R.id.support:
                WifiInfoShow();
                // startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                break;
            case R.id.sign_in:
                // 先将图片展出铺满，然后启动新的Activity
                CircularAnimUtil.fullActivity(getActivity(), view)
                        .colorOrImageRes(R.mipmap.bg)
                        .go(new CircularAnimUtil.OnAnimationEndListener() {
                            @Override
                            public void onAnimationEnd() {
                                startActivity(new Intent(getActivity(), SignInActivity.class));
                            }
                        });
                break;
            case R.id.create_account:
                // 先将图片展出铺满，然后启动新的Activity
                CircularAnimUtil.fullActivity(getActivity(), view)
                        .colorOrImageRes(R.color.white)
                        .go(new CircularAnimUtil.OnAnimationEndListener() {
                            @Override
                            public void onAnimationEnd() {
                                startActivity(new Intent(getActivity(), CreateAccountActivity.class));
                            }
                        });
                break;
            case R.id.setting:
                startActivity(new Intent(getActivity(), SetUpActivity.class));
                ((BaseActivity)getActivity()).overridePendingTransition(getActivity(),BaseActivity.ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;
            case R.id.login_layout:
                startActivity(new Intent(getActivity(), SetUpActivity.class));
                ((BaseActivity)getActivity()).overridePendingTransition(getActivity(),BaseActivity.ANIMATE_SLIDE_TOP_FROM_BOTTOM);

                break;

        }
    }

    private void WifiInfoShow() {
/*

        info.getBSSID();// 获取BSSID地址。

        info.getSSID(); //获取SSID地址。  需要连接网络的ID

        info.getIpAddress();  //获取IP地址。4字节Int, XXX.XXX.XXX.XXX 每个XXX为一个字节

        info.getMacAddress() ; //获取MAC地址。

        info.getNetworkId() ;  //获取网络ID。

        info.getLinkSpeed() ;  //获取连接速度，可以让用户获知这一信息。

        info.getRssi() ;       //获取RSSI，RSSI就是接受信号强度指示

*/

        if(WifiUtil.isWifiConnected(getActivity())){
            WifiManager wifiManager= (WifiManager) getActivity().getSystemService(getActivity().WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String str = "已连接WIFI\n WIFI信息 ：  "+wifiInfo.toString() + "\n" +
                    " WIFI名称 ：  "+wifiInfo.getSSID();
            showToastSuccess(str);
            //若想兼容4.4就要这样：
         /*   String tempSsidString = wifiInfo.getSSID();
            if (tempSsidString != null && tempSsidString.length() > 2) {
                List<ScanResult> scanResults=wifiManager.getScanResults();
                for(ScanResult scanResult:scanResults){
                    int intfrequency = scanResult.frequency;
                }
            }*/


        }else{
            showToastError("未连接WIFI 跳转!");
            startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
        }
    }
}
