package com.jzycc.layout.damplayoutlibrary.bottomview;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jzycc.layout.damplayoutlibrary.R;


/**
 * author Jzy(Xiaohuntun)
 * date 18-9-6
 */
public class DampBottomViewChild extends FrameLayout implements DampBottomViewListener {
    private Activity mContext;
    private ImageView ivLoad;
    private ImageView ivCenter;
    private ObjectAnimator animator;
    private TextView tvLoadOver;
    public final static int DAMPBOTTOMVIEW_HEIGHT = 60;
    private int isLoadState = 0;

    /**
     * 加载相关操作前的状态
     */
    private static final int LOAD_MORE_PRE = 0;

    /**
     * 加载中
     */
    private static final int LOAD_MORE_ING = 1;

    /**
     * 所有数据加载完成
     */
    private static final int LOAD_MORE_OVER = 2;

    /**
     * 加载完成
     */
    private static final int LOAD_MORE_ING_II = 3;


    public DampBottomViewChild(@NonNull Context context) {
        super(context);
        mContext = (Activity) context;
        initThis();
    }

    public DampBottomViewChild(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = (Activity) context;
    }

    public DampBottomViewChild(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = (Activity) context;
    }


    private void initThis() {
        View inflate = inflate(getContext(), R.layout.damp_bottom_view, this);
        ivLoad = (ImageView) inflate.findViewById(R.id.iv_load);
        ivCenter = (ImageView) inflate.findViewById(R.id.iv_center);
        tvLoadOver = (TextView) inflate.findViewById(R.id.tv_loadOver);
        setVisibility(View.GONE);
    }

    @Override
    public void onLoading() {
        setVisibility(View.VISIBLE);
        isLoadState = LOAD_MORE_ING;
        ivLoad.setVisibility(View.VISIBLE);
        ivCenter.setVisibility(View.VISIBLE);
        tvLoadOver.setVisibility(View.GONE);
        animator = ObjectAnimator.ofFloat(ivLoad, "rotation", 0f, -360f);
        animator.setDuration(1500);
        animator.setRepeatCount(-1);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    @Override
    public void onComplete() {
        isLoadState = LOAD_MORE_PRE;
        if (animator != null) {
            animator.cancel();
        }
        setVisibility(View.GONE);
    }

    @Override
    public void onLoaded() {
        setVisibility(View.VISIBLE);
        isLoadState = LOAD_MORE_OVER;
        if (animator != null) {
            animator.cancel();
        }
        ivLoad.setVisibility(View.GONE);
        ivCenter.setVisibility(View.GONE);
        tvLoadOver.setVisibility(View.VISIBLE);
    }

    @Override
    public void onScrollChanged(int dy, int changedBottomViewPosition) {

    }

    public void setImageColorResource(int color) {
        ivLoad.setColorFilter(mContext.getResources().getColor(color));
        ivCenter.setColorFilter(mContext.getResources().getColor(color));
    }

    public void setTextColorResource(int color) {
        tvLoadOver.setTextColor(mContext.getResources().getColor(color));
    }

    public void setLoadOverText(String s) {
        tvLoadOver.setText(s);
    }

    public static class Builder {

        private Context context;

        private DampBottomViewChild viewChild;

        public Builder(Context context) {
            this.context = context;
            viewChild = new DampBottomViewChild(context);
        }

        public Builder setImageColorResource(int colorResource) {
            viewChild.setImageColorResource(colorResource);
            return this;
        }
        public Builder setTextColorResource(int color) {
            viewChild.setTextColorResource(color);
            return this;
        }

        public Builder setLoadOverText(String s) {
            viewChild.setLoadOverText(s);
            return this;
        }

        public DampBottomViewChild build() {
            if(viewChild == null){
                viewChild = new DampBottomViewChild(context);
            }
            return viewChild;
        }
    }
}