package com.ym.traegergill.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ym.traegergill.R;
import com.ym.traegergill.tools.OUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/11/17.
 */

public class RecipesSearchActivity extends BaseActivity {

    @BindView(R.id.searchFor)
    ImageView searchFor;
    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.cancel)
    TextView cancel;
    @BindView(R.id.cover)
    RelativeLayout cover;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes_search);
        unbinder = ButterKnife.bind(this);
        initListener();
    }
    private void initListener() {
        etSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER  && event.getAction() == KeyEvent.ACTION_UP ) {
                    search();
                }
                return false;
            }
        });
    }

    private void search() {
        String searchContent = etSearch.getText().toString().trim();
        OUtil.TLog("searchContent : " + searchContent);
        if (TextUtils.isEmpty(searchContent)) {
            showToastError(getString(R.string.cannot_input_empty_string));
            etSearch.setFocusable(true);
            etSearch.setFocusableInTouchMode(true);
            searchFor.requestFocus();

        } else {
            //showToastSuccess("搜索内容 ： "+searchContext);
            // 调用搜索的API方法
            searchContent(searchContent);
        }
    }

    private void searchContent(final String searchContent) {
        Intent intent = new Intent(getActivity(),FilterRecipesActivity.class);
        intent.putExtra("isBySearchContent",true);
        intent.putExtra("searchContent",searchContent);
        startActivity(intent);
        overridePendingTransition(getActivity(),ANIMATE_SLIDE_TOP_FROM_BOTTOM);
    }

    @OnClick({R.id.searchFor, R.id.cancel,R.id.cover})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.searchFor:
            case R.id.cancel:
            case R.id.cover:
                exit(view);
                break;
        }
    }


    public void exit(View view){
        //super.finish();
        this.finishAfterTransition();

    }

}
