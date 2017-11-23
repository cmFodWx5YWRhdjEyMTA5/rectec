package com.ym.traegergill.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.ym.traegergill.R;
import com.ym.traegergill.adapter.GridImageAdapter;
import com.ym.traegergill.other.FullyGridLayoutManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/11/20.
 */

public class TalkActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.add)
    ImageView add;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        unbinder = ButterKnife.bind(this);
        init();
    }

    private void init() {
        title.setText("TALK");

    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this, ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }

    @OnClick(R.id.add)
    public void onViewClicked() {
        startActivity(new Intent(getActivity(), AddTalkActivity.class));
        ((BaseActivity) getActivity()).overridePendingTransition(getActivity(), BaseActivity.ANIMATE_SLIDE_TOP_FROM_BOTTOM);
    }
}
