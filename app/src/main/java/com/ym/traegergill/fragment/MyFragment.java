package com.ym.traegergill.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.model.HttpParams;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.sdk.TuyaUser;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.BaseActivity;
import com.ym.traegergill.activity.BrowserActivity;
import com.ym.traegergill.activity.CreateAccountActivity;
import com.ym.traegergill.activity.DealerLocatorActivity;
import com.ym.traegergill.activity.FeedBackActivity;
import com.ym.traegergill.activity.SetUpActivity;
import com.ym.traegergill.activity.ShowVersionActivity;
import com.ym.traegergill.activity.SignInActivity;
import com.ym.traegergill.activity.TempChartActivity;
import com.ym.traegergill.adapter.MyFragmentRvAdapter;
import com.ym.traegergill.bean.MyFragmentBean;
import com.ym.traegergill.broadcast.TraegerGillBroadcastHelper;
import com.ym.traegergill.db.SQLiteDbUtil;
import com.ym.traegergill.net.URLs;
import com.ym.traegergill.tools.CircularAnimUtil;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.MyNetTool;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.SharedPreferencesUtils;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.tuya.utils.ProgressUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

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
    @BindView(R.id.my_email)
    TextView myEmail;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.sign_in)
    TextView signIn;
    @BindView(R.id.create_account)
    TextView createAccount;
    Unbinder unbinder;

    @BindView(R.id.hi_account)
    TextView hiAccount;
    @BindView(R.id.setting)
    TextView setting;
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
    private boolean needUpdate = false;
    private SharedPreferencesUtils spUtils;
    class UpdateUserStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS)) {
                if (spUtils.getBoolean(Constants.ISLOGIN, false)) {
                    netUserInfo(TuyaUser.getUserInstance().getUser().getUid());
                }else{
                    updateUI();
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TraegerGillBroadcastHelper.ACTION_UPDATE_USERSTATUS);
        updateUserStatusReceiver = new UpdateUserStatusReceiver();
        getActivity().registerReceiver(updateUserStatusReceiver, intentFilter);
    }
    private void netAppVersion() {
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
                        String content = obj.optString("content");
                        String nowVer = getVersion();
                        if(content.equals(nowVer)){
                            //=====test=====
                        /*    needUpdate = true;
                            setMessagePoint();*/
                            //=====test=====
                        }else{
                            needUpdate = true;
                            setMessagePoint();

                        }
                    }else{
                        showToastError(obj.optString("msg"));
                        TLog(obj.optString("msg"));
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
        HttpParams httpParams = new HttpParams();
        if(!MyNetTool.netHttpParams(getActivity(), URLs.getAppVersion,callback,httpParams)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netAppVersion();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });
        }

    }

    private void setMessagePoint(){
        if(needUpdate && infos!=null){
            infos.get(infos.size()-1).setFlag(true);
            adapter.notifyDataSetChanged();
        }


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
        if(OUtil.isNetworkConnected(getActivity())){
            netAppVersion();
        }
        init();
        return view;
    }




    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (spUtils.getBoolean(Constants.ISLOGIN, false) && TuyaUser.getUserInstance().isLogin()) {
            loginLayout.setVisibility(View.VISIBLE);
            noLoginLayout.setVisibility(View.GONE);
            setting.setVisibility(View.VISIBLE);
            user = TuyaUser.getUserInstance().getUser();
            myEmail.setText(user.getEmail());
            hiAccount.setText("Hi " + spUtils.getString(Constants.FIRST_NAME) + "!");
        } else {
            loginLayout.setVisibility(View.GONE);
            noLoginLayout.setVisibility(View.VISIBLE);
            setting.setVisibility(View.VISIBLE);
        }
    }



    SQLiteDbUtil dbUtil;
    User user;
    private void init() {
        spUtils = SharedPreferencesUtils.getSharedPreferencesUtil(getActivity());
        dbUtil = SQLiteDbUtil.getSQLiteDbUtil();
        title.setText("MORE");
        String[] resS = getResources().getStringArray(R.array.SettingList);
        if (infos == null) {
            infos = new ArrayList<>();
            for (int i = 0; i < resS.length; i++) {
                infos.add(new MyFragmentBean(resS[i],false));
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
                    Intent intent;
                    switch (position) {
                        case 0:
                            startActivity(new Intent(getActivity(), DealerLocatorActivity.class));
                            break;
                         case 1:
                             startActivity(new Intent(getActivity(), FeedBackActivity.class));
                            break;
                        case 2:
                            intent = new Intent(getActivity(), BrowserActivity.class);
                            intent.putExtra(BrowserActivity.EXTRA_LOGIN, false);
                            intent.putExtra(BrowserActivity.EXTRA_TITLE, infos.get(position).getName());
                            intent.putExtra(BrowserActivity.EXTRA_URI, Constants.URL1);
                            getActivity().startActivity(intent);
                            break;
                        case 3:
                            intent = new Intent(getActivity(), BrowserActivity.class);
                            intent.putExtra(BrowserActivity.EXTRA_LOGIN, false);
                            intent.putExtra(BrowserActivity.EXTRA_TITLE, infos.get(position).getName());
                            intent.putExtra(BrowserActivity.EXTRA_URI, Constants.URL2);
                            getActivity().startActivity(intent);
                            break;
                        case 4:
                            intent = new Intent(getActivity(), ShowVersionActivity.class);
                            getActivity().startActivity(intent);
                            break;
        /*                case 5:
                            intent = new Intent(getActivity(), TempChartActivity.class);
                            getActivity().startActivity(intent);
                            break;*/
                        default:
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


    @OnClick({R.id.support, R.id.sign_in, R.id.create_account, R.id.setting, R.id.login_layout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.support:
                Intent intent = new Intent(getActivity(), BrowserActivity.class);
                intent.putExtra(BrowserActivity.EXTRA_LOGIN, false);
                intent.putExtra(BrowserActivity.EXTRA_TITLE, "Support");
                intent.putExtra(BrowserActivity.EXTRA_URI, Constants.TUYA_VIEW_HELP);
                getActivity().startActivity(intent);
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
                ((BaseActivity) getActivity()).overridePendingTransition(getActivity(), BaseActivity.ANIMATE_SLIDE_TOP_FROM_BOTTOM);
                break;
            case R.id.login_layout:
               /* startActivity(new Intent(getActivity(), SetUpActivity.class));
                ((BaseActivity) getActivity()).overridePendingTransition(getActivity(), BaseActivity.ANIMATE_SLIDE_TOP_FROM_BOTTOM);
*/
                break;

        }
    }


    private void netUserInfo(final String uid) {
        StringCallback callback = new StringCallback() {
            @Override
            public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                TLog("json : " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (obj.optInt("code") == 200) {
                        JSONObject content = obj.optJSONObject("content");
                        spUtils.setValue(Constants.ISLOGIN, true);
                        spUtils.setValue(Constants.EMAIL,content.optString("account"));
                        spUtils.setValue(Constants.FIRST_NAME,content.optString("firstname"));
                        spUtils.setValue(Constants.LAST_NAME,content.optString("lastname"));
                        updateUI();
                    }else{
                        TLog(obj.optString("msg"));
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

        if(!MyNetTool.netCross(getActivity(),uid,URLs.getUserinfo,callback)){
            showRenetDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            netUserInfo(uid);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            });
        }
    }


    public String getVersion() {
        try {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }
}
