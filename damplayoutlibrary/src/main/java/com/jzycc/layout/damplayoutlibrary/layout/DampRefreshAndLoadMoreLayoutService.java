package com.jzycc.layout.damplayoutlibrary.layout;

import android.view.View;

import com.jzycc.layout.damplayoutlibrary.layout.decoration.GroupItemDecoration;

/**
 * @author : Jzy
 * date   : 18-11-7
 */
public interface DampRefreshAndLoadMoreLayoutService {

    /**
     * 设置默认topView
     */
    void openRefresh();

    /**
     * 添加自定义topView
     */
    void openRefresh(View view, int viewHeight);

    /**
     * 设置默认bottomView
     */
    void openLoadMore();

    /**
     * 设置自定义bottomView
     */
    void openLoadMore(View view, int viewHeight);

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

    /**
     * @param groupDecoration 需要传入{@link GroupItemDecoration}实例
     * 如果容器内的View是RecyclerView的话，调用此方法可以为recyclerView添加{@link GroupItemDecoration}
     */
    void setGroupDecoration(GroupItemDecoration groupDecoration);

}
