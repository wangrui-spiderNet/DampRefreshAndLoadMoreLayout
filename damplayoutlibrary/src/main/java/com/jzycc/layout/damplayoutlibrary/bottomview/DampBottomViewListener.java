package com.jzycc.layout.damplayoutlibrary.bottomview;

/**
 * author Jzy(Xiaohuntun)
 * date 18-9-11
 */
public interface DampBottomViewListener {

    /**
     * @param dy bottomView的偏移量
     * @param bottomViewPosition bottomView顶部到容器顶部的距离(初始位置为0)
     * 此方法可以监听bottomView的位置变化和具体数值
     */
    void onScrollChanged(int dy, int bottomViewPosition);

    /**
     * 此时加载被触发，初始化工作也可以在此处执行
     */
    void onLoadMore();

    /**
     * 此时加载结束
     */
    void onComplete();

    /**
     * 此时所有数据已经加载完毕
     */
    void onLoaded();
}