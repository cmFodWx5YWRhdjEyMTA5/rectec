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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.tools.OUtil;
import com.ym.traegergill.tools.RegularUtils;

import java.util.Objects;


/**
 * Created by Administrator on 2017/7/17.
 */

public class EditPickerDialog extends Dialog implements View.OnClickListener{
    private TextView sure,dialog_title;
    private ImageView back;
    private String title;
    private String strDefault;
    private EditText et_price;
    private Context context;
    private boolean onlyCanNum = false;
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sure:
                if(onlyCanNum && !RegularUtils.isNumeric(et_price.getText().toString())){
                    OUtil.toastError(context,context.getString(R.string.Input_number_only));
                    return;
                }
                if(listen!=null && !TextUtils.isEmpty(et_price.getText().toString())){
                    listen.callback(et_price.getText().toString());
                    EditPickerDialog.this.dismiss();
                }else{
                    OUtil.toastError(context,context.getString(R.string.cannot_input_empty_string));
                }
                break;
            case R.id.back:
                EditPickerDialog.this.dismiss();
                break;
        }
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public interface callBackListen{
        abstract void callback(String text);
    }
    private callBackListen listen;

    public void setCallBackListen(callBackListen listen){
        this.listen = listen;
    }

    public EditPickerDialog(Context context, @Nullable String strDefault,@Nullable boolean onlyCanNum) {
        super(context);
        this.context = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.strDefault = strDefault;
        this.onlyCanNum = onlyCanNum;
    }
    public EditPickerDialog(Context context, @Nullable String strDefault) {
        super(context);
        this.context = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.strDefault = strDefault;
        this.onlyCanNum = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit);
        back = (ImageView) findViewById(R.id.back);
        sure = (TextView) findViewById(R.id.sure);
        et_price = (EditText) findViewById(R.id.et_price);
        dialog_title = (TextView) findViewById(R.id.dialog_title);
        dialog_title.setText(title);
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = AbsListView.LayoutParams.WRAP_CONTENT;
        lp.height = AbsListView.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
        if(!TextUtils.isEmpty(strDefault)){
            et_price.setText(strDefault);
            et_price.setSelection(strDefault.length());
        }
        sure.setOnClickListener(this);
        back.setOnClickListener(this);
    }
    public void setText(String text){
        if(et_price == null){
            et_price = (EditText) findViewById(R.id.et_price);
        }
        if(et_price!=null){
            et_price.setText(text);
            et_price.setSelection(text.length());
        }
    }
    View tag;
    public View getTag() {
        return tag;
    }

    public void setTag(View tag) {
        this.tag = tag;
    }
}
