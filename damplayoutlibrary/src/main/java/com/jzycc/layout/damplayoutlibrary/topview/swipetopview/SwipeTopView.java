package com.jzycc.layout.damplayoutlibrary.topview.swipetopview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.jzycc.layout.damplayoutlibrary.topview.DampTopViewListener;
import com.jzycc.layout.damplayoutlibrary.utils.PixelUtils;


/**
 * author Jzy(Xiaohuntun)
 * date 18-9-18
 */
public class SwipeTopView extends RelativeLayout implements DampTopViewListener{

    /**
     * 刷新相关操作前的状态
     */
    private static final int REFRESH_PRE = 0;

    /**
     * 下拉到了足够高度，松手即可刷新
     */
    private static final int REFRESH_READY = 1;

    /**
     * 下拉长度不足，松手回弹到原位
     */
    private static final int REFRESH_CANNOT = 2;

    /**
     * 刷新中
     */
    private static final int REFRESH_ING = 3;

    private int isRefreshState = 0;

    private Context mContext;

    private int[] colors = {
            0xFFFF0000,0xFFFF7F00,0xFF00FF00
            ,0xFF00FFFF,0xFF0000FF,0xFF8B00FF};

    private int color;

    public static final int SWIPETOPVIEW_HEIGHT = 60;

    private int topViewHeight;

    private MaterialProgressDrawable mProgress;

    private final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;

    private CircleImageView mCircleView;

    private float swipeAlpha = 0f;

    private float endTrim = 0f;
    private float startTrim = 0f;

    public SwipeTopView(Context context) {
        super(context);
        mContext = context;
        initThis();
    }

    public SwipeTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initThis();
    }

    public SwipeTopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initThis();
    }

    private void initThis(){
        mCircleView = new CircleImageView(mContext,CIRCLE_BG_LIGHT);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(mCircleView,layoutParams);

        topViewHeight = PixelUtils.dp2px(mContext,SWIPETOPVIEW_HEIGHT);

        mProgress = new MaterialProgressDrawable(mContext,mCircleView);
        mCircleView.setImageDrawable(mProgress);
        mProgress.updateSizes(MaterialProgressDrawable.DEFAULT);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mProgress.setColorSchemeColors(colors);
        mCircleView.setAlpha(0);
        mProgress.showArrow(true);
        mProgress.setStartEndTrim(0f,0f);
        mCircleView.setVisibility(View.GONE);

    }

    @Override
    public void getScrollChanged(int dy, int topViewPosition) {
        if(topViewPosition>=0&&(isRefreshState == REFRESH_PRE||isRefreshState == REFRESH_CANNOT)){
            float nowTopPosition = (float)topViewPosition;
            swipeAlpha = nowTopPosition*255/(float) topViewHeight;
            endTrim = nowTopPosition/(float)topViewHeight;
            mCircleView.setAlpha((int)swipeAlpha);
            mProgress.setAlpha((int)swipeAlpha);

            mProgress.setArrowScale(endTrim);
            endTrim = 0.8f*endTrim;
            mProgress.setStartEndTrim(0f,endTrim);
            startTrim = endTrim;
        }
        if(isRefreshState==REFRESH_READY){
            float nowTopPosition = (float)topViewPosition;
            endTrim = nowTopPosition/(float)topViewHeight;
            endTrim = 0.8f*endTrim;
            mProgress.setStartEndTrim(endTrim-startTrim,endTrim);
        }
    }

    @Override
    public void refreshComplete() {
        mProgress.stop();
    }

    @Override
    public void refreshing() {
        isRefreshState = REFRESH_ING;
        mProgress.start();
    }

    @Override
    public void refreshReady() {
        isRefreshState = REFRESH_READY;
    }

    @Override
    public void shouldInitialize() {
        isRefreshState = REFRESH_PRE;
        mCircleView.setAlpha(0);
        mProgress.setStartEndTrim(0f,0f);
        mProgress.showArrow(true);
        mCircleView.setVisibility(View.VISIBLE);
    }

    @Override
    public void refreshCannot() {
        isRefreshState = REFRESH_CANNOT;
    }

    public void setColorSchemeColors(int[] colors){
        mProgress.setColorSchemeColors(colors);
    }
}