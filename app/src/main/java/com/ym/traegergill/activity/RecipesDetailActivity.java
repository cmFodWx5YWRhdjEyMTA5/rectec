package com.ym.traegergill.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

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

public class RecipesDetailActivity extends BaseActivity {
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    @BindView(R.id.iv_img)
    ImageView ivImg;
    @BindView(R.id.share)
    ImageView share;
    @BindView(R.id.collect)
    ImageView collect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes_detail);
        ButterKnife.bind(this);

        init();

    }
    private void init() {
        int width = OUtil.getScreenWidth(getActivity());
        ivImg.setMinimumHeight(OUtil.dip2px(getActivity(), 200));
        String url = getIntent().getStringExtra("data");
        RequestOptions options = new RequestOptions()
                .override(width, OUtil.dip2px(getActivity(), 200))
                .placeholder(Constants.PLACEHOLDER)
                .error(Constants.ERROR)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(getActivity())
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade(R.anim.shake_error, 1000))
                .apply(options)
                .thumbnail(0.1f)
                .into(ivImg);

    }


    @OnClick({R.id.share, R.id.collect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.share:
                break;
            case R.id.collect:
                animateHeartButton(view);
                showToastSuccess("HELLO!");
                break;
        }
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
