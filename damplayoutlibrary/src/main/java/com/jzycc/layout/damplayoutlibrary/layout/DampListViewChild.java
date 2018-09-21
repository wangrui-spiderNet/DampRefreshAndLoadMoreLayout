package com.jzycc.layout.damplayoutlibrary.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * author Jzy(Xiaohuntun)
 * date 18-9-20
 */
public class DampListViewChild extends ListView {
    private int mInitialDownY;

    public DampListViewChild(Context context) {
        super(context);
    }

    public DampListViewChild(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DampListViewChild(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mInitialDownY = (int)ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int nowY = (int)ev.getY();
                int offsetY = mInitialDownY-nowY;
                mInitialDownY = nowY;
                if((!canScrollVertically(-1)&&offsetY<0)||(!canScrollVertically(1)&&offsetY>0)){
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
