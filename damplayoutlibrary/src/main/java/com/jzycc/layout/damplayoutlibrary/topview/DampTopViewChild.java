package com.jzycc.layout.damplayoutlibrary.topview;

import android.animation.ObjectAnimator;
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
public class DampTopViewChild extends FrameLayout implements DampTopViewListener {

    private Context mContext;

    private TextView tvRefreshState;

    private ImageView ivRefreshState;

    private float mTopViewHeight;

    private float mMeasureHeight;

    private int isRefreshState;

    public final static int DAMPTOPVIEW_HEIGHT = 40;


    /**
     * 刷新相关操作前的状态
     */
    private static final int REFRESH_PRE = 0;

    /**
     * 下拉到了足够高度，松手即可刷新
     */
    private static final int REFRESH_READY = 1;

    /**
     * 刷新中
     */
    private static final int REFRESH_ING = 3;

    private ObjectAnimator animator;


    public DampTopViewChild(@NonNull Context context) {
        super(context);
        mContext = context;
        initThis();
    }

    public DampTopViewChild(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public DampTopViewChild(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    private void initThis() {
        View inflate = inflate(getContext(), R.layout.damp_top_view, this);

        tvRefreshState = inflate.findViewById(R.id.tv_refresh_state);

        ivRefreshState = inflate.findViewById(R.id.iv_refresh_state);

        mTopViewHeight = (float) dp2px(mContext, DAMPTOPVIEW_HEIGHT);

        mMeasureHeight = (float) dp2px(mContext, 26);
    }

    @Override
    public void onScrollChanged(int dy, int topViewPosition) {
        if (topViewPosition < 0 && isRefreshState != REFRESH_ING) {
            ivRefreshState.setRotation(0);
        }
        if (topViewPosition >= 0 && dy < 0 && isRefreshState != REFRESH_ING) {
            ivRefreshState.setRotation(measureImageRotation((float) topViewPosition));
        }
        if (topViewPosition >= mTopViewHeight) {
            ivRefreshState.setRotation(180);
        }
        if (dy > 0 && topViewPosition >= 0 && topViewPosition <= mTopViewHeight) {
            ivRefreshState.setRotation(measureImageRotation((float) topViewPosition));
            if ((mTopViewHeight - mMeasureHeight) <= 0) {
                ivRefreshState.setRotation(0);
            }
        }
        if (isRefreshState == REFRESH_READY && topViewPosition <= mTopViewHeight) {
            tvRefreshState.setText(R.string.damplayout_pull_down_refresh);
        }
    }

    @Override
    public void onComplete() {
        tvRefreshState.setText(R.string.damplayout_complete_refresh);
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    public void onRefresh() {
        isRefreshState = REFRESH_ING;
        tvRefreshState.setText(R.string.damplayout_refreshing);
        ivRefreshState.setImageResource(R.drawable.refresh_ing);
        startImageRotation();
    }

    @Override
    public void onReady() {
        isRefreshState = REFRESH_READY;
        tvRefreshState.setText(R.string.damplayout_loosen_refresh);
    }


    @Override
    public void onStart() {
        setVisibility(View.VISIBLE);
        tvRefreshState.setText(R.string.damplayout_pull_down_refresh);
        ivRefreshState.setRotation(0);
        ivRefreshState.setImageResource(R.drawable.pull_down);
        isRefreshState = REFRESH_PRE;

    }

    @Override
    public void onCancel() {
    }

    private float measureImageRotation(float topViewPosition) {
        return -(topViewPosition) / (mTopViewHeight) * 180;
    }

    /**
     * 将dp转化为px
     */
    private int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void startImageRotation() {
        animator = ObjectAnimator.ofFloat(ivRefreshState, "rotation", 0f, 360f);
        animator.setDuration(1000);
        animator.setRepeatCount(-1);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    public void setImageColorResource(int color) {
        ivRefreshState.setColorFilter(mContext.getResources().getColor(color));
    }

    public void setTextColorResource(int color) {
        tvRefreshState.setTextColor(mContext.getResources().getColor(color));
    }

    public static class Builder{
        private DampTopViewChild viewChild;

        public Builder(Context mContext) {
            viewChild = new DampTopViewChild(mContext);
        }

        public Builder setImageColorResource(int color) {
            viewChild.setImageColorResource(color);
            return this;
        }

        public Builder setTextColorResource(int color) {
            viewChild.setTextColorResource(color);
            return this;
        }

        public DampTopViewChild build() {
            return viewChild;
        }
    }
}