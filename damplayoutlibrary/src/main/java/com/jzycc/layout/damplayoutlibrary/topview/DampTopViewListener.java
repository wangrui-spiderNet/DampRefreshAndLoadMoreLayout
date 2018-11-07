package com.jzycc.layout.damplayoutlibrary.topview;

/**
 * author Jzy(Xiaohuntun)
 * date 18-9-11
 */
public interface DampTopViewListener {

    /**
     * @param dy topView的偏移量
     * @param topViewPosition topView顶部到容器顶部的距离
     * 此方法可以监听topView的位置变化和具体数值
     */
    void onScrollChanged(int dy, int topViewPosition);

    /**
     * 需要初始化的步骤，此处在按下屏幕并下拉时触发
     */
    void onStart();

    /**
     * 此时松手不可以触发刷新
     */
    void onCancel();

    /**
     * 此时松手可以触发刷新
     */
    void onReady();

    /**
     * 此时正在刷新
     */
    void onRefreshing();

    /**
     * 此时刷新已经完成
     */
    void onComplete();
}
