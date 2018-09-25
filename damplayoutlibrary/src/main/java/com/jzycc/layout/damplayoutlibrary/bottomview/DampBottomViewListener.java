package com.jzycc.layout.damplayoutlibrary.bottomview;

/**
 * author Jzy(Xiaohuntun)
 * date 18-9-11
 */
public interface DampBottomViewListener {
    void startLoadMore();

    void stopLoadMore();

    void loadOver();

    void onScrollChanged(int dy, int topViewPosition);
}