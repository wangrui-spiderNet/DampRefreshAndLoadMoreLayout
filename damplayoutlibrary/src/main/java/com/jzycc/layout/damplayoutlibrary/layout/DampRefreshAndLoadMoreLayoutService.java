package com.jzycc.layout.damplayoutlibrary.layout;

import android.view.View;

/**
 * @author : Jzy
 * date   : 18-11-7
 */
public interface DampRefreshAndLoadMoreLayoutService {

    /**
     * 设置默认topView
     */
    void setTopView();

    /**
     * 添加自定义topView
     */
    void setTopView(View view, int topViewHeight);

    /**
     * 设置默认bottomView
     */
    void setBottomView();

    /**
     * 设置自定义bottomView
     */
    void setBottomView(View view, int bottomViewHeight);

    /**
     * @param dampRefreshListener 添加refresh相关监听
     */
    void addOnDampRefreshListener(DampRefreshAndLoadMoreLayout.DampRefreshListener dampRefreshListener);

    /**
     * @param dampLoadMoreListener 添加loadmore相关监听
     */
    void addOnDampLoadMoreListener(DampRefreshAndLoadMoreLayout.DampLoadMoreListener dampLoadMoreListener);

    /**
     * @param duration 动画播放时长
     */
    void setAnimationDuration(int duration);

    /**
     * @param value 阻尼最大时middleView顶部到容器顶部的距离
     */
    void setPullDownDampDistance(int value);

    /**
     * @param value 阻尼最大时middleView底部到容器底部的距离
     */
    void setUpGlideDampDistance(int value);

    /**
     * @param pullDownDampValue 下拉时最大阻尼系数
     *                          可选范围在0f~100f之间
     */
    void setPullDownDampValue(float pullDownDampValue);

    /**
     * @param upGlideDampValue bottomView最大阻尼系数
     *                         可选范围在0f~100f之间
     */
    void setUpGlideDampValue(float upGlideDampValue);
}
