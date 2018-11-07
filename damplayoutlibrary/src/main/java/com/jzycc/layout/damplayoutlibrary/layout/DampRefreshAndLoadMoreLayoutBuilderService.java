package com.jzycc.layout.damplayoutlibrary.layout;

import android.view.View;

import com.jzycc.layout.damplayoutlibrary.layout.DampRefreshAndLoadMoreLayout.Builder;

/**
 * @author : Jzy
 * date   : 18-11-7
 */
public interface DampRefreshAndLoadMoreLayoutBuilderService {

    /**
     * @param dampRefreshAndLoadMoreLayout 需要传入实例
     * @return Builder
     */
    Builder attachLayout(DampRefreshAndLoadMoreLayout dampRefreshAndLoadMoreLayout);

    /**
     * 设置默认topView
     */
    Builder setTopView();

    /**
     * 添加自定义topView
     */
    Builder setTopView(View view, int topViewHeight);

    /**
     * 设置默认bottomView
     */
    Builder setBottomView();

    /**
     * 设置自定义bottomView
     */
    Builder setBottomView(View view, int bottomViewHeight);

    /**
     * @param dampRefreshListener 添加refresh相关监听
     */
    Builder addOnDampRefreshListener(DampRefreshAndLoadMoreLayout.DampRefreshListener dampRefreshListener);

    /**
     * @param dampLoadMoreListener 添加loadmore相关监听
     */
    Builder addOnDampLoadMoreListener(DampRefreshAndLoadMoreLayout.DampLoadMoreListener dampLoadMoreListener);

    /**
     * @param duration 动画播放时长
     */
    Builder setAnimationDuration(int duration);

    /**
     * @param value 阻尼最大时middleView顶部到容器顶部的距离
     */
    Builder setPullDownDampDistance(int value);

    /**
     * @param value 阻尼最大时middleView底部到容器底部的距离
     */
    Builder setUpGlideDampDistance(int value);

    /**
     * @param pullDownDampValue 下拉时最大阻尼系数
     *                          可选范围在0f~100f之间
     */
    Builder setPullDownDampValue(float pullDownDampValue);

    /**
     * @param upGlideDampValue bottomView最大阻尼系数
     *                         可选范围在0f~100f之间
     */
    Builder setUpGlideDampValue(float upGlideDampValue);

    DampRefreshAndLoadMoreLayout build();
}
