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
public class DampBottomViewChild extends FrameLayout implements DampBottomViewListener{
    private Activity mContext;
    private ImageView ivLoad;
    private ImageView ivCenter;
    private ObjectAnimator animator;
    private TextView tvLoadOver;
    public final static int DAMPBOTTOMVIEW_HEIGHT = 60;
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


    private void initThis(){
        View inflate = inflate(getContext(), R.layout.damp_bottom_view, this);
        ivLoad = (ImageView)inflate.findViewById(R.id.iv_load);
        ivCenter = (ImageView)inflate.findViewById(R.id.iv_center);
        tvLoadOver = (TextView)inflate.findViewById(R.id.tv_loadOver);

    }

    @Override
    public void startLoadMore() {
        ivLoad.setVisibility(View.VISIBLE);
        ivCenter.setVisibility(View.VISIBLE);
        tvLoadOver.setVisibility(View.GONE);
        animator = ObjectAnimator.ofFloat(ivLoad,"rotation",0f,-360f);
        animator.setDuration(1500);
        animator.setRepeatCount(-1);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    @Override
    public void stopLoadMore() {
        //结束动画
        if(animator!=null){
            animator.cancel();
        }
    }

    @Override
    public void loadOver() {
        if(animator!=null){
            animator.cancel();
        }
        ivLoad.setVisibility(View.GONE);
        ivCenter.setVisibility(View.GONE);
        tvLoadOver.setVisibility(View.VISIBLE);
    }

    @Override
    public void getScrollChanged(int dy, int changedBottomViewPosition) {

    }

    public void setImageColorResource(int color){
        ivLoad.setColorFilter(color);
        ivCenter.setColorFilter(color);
    }

    public void setTextColorResource(int color){
        tvLoadOver.setTextColor(color);
    }

    public void setLoadOverText(String s){
        tvLoadOver.setText(s);
    }

}