package com.ym.traegergill.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.ym.traegergill.R;
import com.ym.traegergill.tools.Constants;
import com.ym.traegergill.tools.OUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_detail);
        ButterKnife.bind(this);
        init();

    }
    String url;
    private void init() {
        int width = OUtil.getScreenWidth(getActivity());
        ivImg.setMinimumHeight(width);
        url = getIntent().getStringExtra("data");

        RequestOptions requestOptions = new RequestOptions()
                .override(width, width)
                .placeholder(Constants.PLACEHOLDER)
                .error(Constants.ERROR)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(getActivity())
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade(R.anim.shake_error, 1000))
                .apply(requestOptions)
                .thumbnail(0.1f)
                .into(ivImg);
        Glide.with(getActivity())
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade(R.anim.shake_error, 1000))
                .apply(requestOptions)
                .into(recipesImg);


/*
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_detail);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add("This is " + i);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerAdapter adapter = new RecyclerAdapter(list, this);
        recyclerView.setAdapter(adapter);*/
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
                        new Intent(getActivity(), RecipesDetailActivity.class).putExtra("data",url),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                getActivity(),
                                Pair.create(view, getActivity().getString(R.string.iv_img_transitionName))
                        ).toBundle());
                break;
        }
    }
}
