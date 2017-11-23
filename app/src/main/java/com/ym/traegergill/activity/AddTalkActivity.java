package com.ym.traegergill.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;


import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.ym.traegergill.R;
import com.ym.traegergill.adapter.GridImageAdapter;
import com.ym.traegergill.iview.IAddTalkView;
import com.ym.traegergill.other.FullyGridLayoutManager;
import com.ym.traegergill.presenter.AddTalkPresenter;
import com.ym.traegergill.view.SelectDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2017/11/20.
 */

public class AddTalkActivity extends BaseActivity implements IAddTalkView {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.photo_recycler)
    RecyclerView photoRecycler;
    GridImageAdapter adapter;
    private AddTalkPresenter addTalkPresenter;
    private int maxSelectNum = 9;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_talk);
        unbinder = ButterKnife.bind(this);
        title.setText("ADD TALK");
        initPhotoRecy();
        initPresenter();
    }

    private void initPresenter() {
        addTalkPresenter = new AddTalkPresenter(this,this);
    }

    private void initPhotoRecy() {
        FullyGridLayoutManager manager = new FullyGridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
        photoRecycler.setLayoutManager(manager);
        adapter = new GridImageAdapter(this, onAddPicClickListener);
        adapter.setSelectMax(maxSelectNum);
        photoRecycler.setAdapter(adapter);
        adapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                addTalkPresenter.onItemClick(position);
            }
        });
        requestPermissions();

    }

    public void requestPermissions() {
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    PictureFileUtils.deleteCacheDirFile(AddTalkActivity.this);
                } else {
                    Toast.makeText(AddTalkActivity.this,
                            getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {

            List<String> names = new ArrayList<>();
            names.add("拍照");
            names.add("相册");
            showDialog(new SelectDialog.SelectDialogListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    addTalkPresenter.selectDialogOnItemClick(position);
                }
            }, names);

        }

    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        addTalkPresenter.onActivityResult(requestCode, resultCode, data);
    }
    private SelectDialog showDialog(SelectDialog.SelectDialogListener listener, List<String> names) {
        SelectDialog dialog = new SelectDialog(getActivity(), R.style
                .transparentFrameWindowStyle,
                listener, names);
        if (!getActivity().isFinishing()) {
            dialog.show();
        }
        return dialog;
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(this, ANIMATE_SLIDE_BOTTOM_FROM_TOP);
    }

    @Override
    public void updateDeviceData(List<LocalMedia> datas) {
        if (adapter != null) {
            adapter.setList(datas);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
