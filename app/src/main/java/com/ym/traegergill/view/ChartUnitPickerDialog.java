package com.ym.traegergill.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ym.traegergill.R;

import java.util.List;

/**
 * Created by Administrator on 2017/7/17.
 */

public class ChartUnitPickerDialog extends Dialog implements View.OnClickListener{
    private SmallStringPickerView pickerview;
    private TextView sure,dialog_title;
    private List<String> mDatas;
    private ImageView back;
    private String title;
    private int btnRes = 0;
    private String returnText = "";
    private String strDefault;

    public String getmDatasByIndex(int index) {
        if(index<mDatas.size() && index>=0){
            return mDatas.get(index);
        }
        return null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sure:
                pickerview.performSelect();
                if(listen!=null){
                    listen.callback(returnText);
                    ChartUnitPickerDialog.this.dismiss();
                }
                break;
            case R.id.back:
                ChartUnitPickerDialog.this.dismiss();
                break;
        }
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setBtnColor(int btnRes) {
        this.btnRes = btnRes;
    }
    public interface callBackListen{
        abstract void callback(String text);
    }
    private callBackListen listen;

    public void setCallBackListen(callBackListen listen){
        this.listen = listen;
    }

    public ChartUnitPickerDialog(Context context, List<String> mDatas, @Nullable String strDefault) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mDatas = mDatas;
        this.strDefault = strDefault;
    }

    public ChartUnitPickerDialog(Context context, List<String> mDatas) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mDatas = mDatas;
        this.strDefault = strDefault;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_chartunit_piker);
        //rechargeNum = (TextView) findViewById(R.id.recharge_num);
        pickerview = (SmallStringPickerView) findViewById(R.id.pickerview);
        back = (ImageView) findViewById(R.id.back);
        sure = (TextView) findViewById(R.id.sure);
        dialog_title = (TextView) findViewById(R.id.dialog_title);
        dialog_title.setText(title);
        if(btnRes!=0){
            sure.setBackgroundResource(btnRes);
        }

        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = AbsListView.LayoutParams.WRAP_CONTENT;
        lp.height = AbsListView.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
        pickerview.setData(mDatas);
        if(!TextUtils.isEmpty(strDefault))
            pickerview.setSelected(strDefault);
        pickerview.setOnSelectListener(new SmallStringPickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                returnText = text;
            }
        });
        sure.setOnClickListener(this);
        back.setOnClickListener(this);
    }
    public void setSelect(String text){
        if(pickerview!=null && text!=null)
            pickerview.setSelected(text);
    }

}
