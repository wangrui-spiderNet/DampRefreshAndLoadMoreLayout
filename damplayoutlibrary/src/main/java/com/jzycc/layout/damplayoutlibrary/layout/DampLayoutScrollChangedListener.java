package com.jzycc.layout.damplayoutlibrary.layout;

/**
 * @author : Jzy
 * date   : 18-11-15
 */
public interface DampLayoutScrollChangedListener {
    void onPullDownScrollChanged(int dy, int topViewPosition);

    void onUpGlideScrollChanged(int dy, int bottomViewPosition);
}
