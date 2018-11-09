package com.jzycc.layout.damplayoutlibrary.layout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jzycc.layout.damplayoutlibrary.bottomview.DampBottomViewChild;
import com.jzycc.layout.damplayoutlibrary.bottomview.DampBottomViewListener;
import com.jzycc.layout.damplayoutlibrary.topview.DampTopViewChild;
import com.jzycc.layout.damplayoutlibrary.topview.DampTopViewListener;

import java.util.ArrayList;
import java.util.List;

/**
 * author Jzy(Xiaohuntun)
 * date 18-9-4
 */
public class DampRefreshAndLoadMoreLayout extends LinearLayout implements DampRefreshAndLoadMoreLayoutService{
    private static final String TAG = "DampLayout";


    private Context mContext;

    /**
     * 刷新相关操作前的状态
     */
    public static final int REFRESH_PRE = 0;

    /**
     * 下拉到了足够高度，松手即可刷新
     */
    public static final int REFRESH_READY = 1;

    /**
     * 下拉长度不足，松手回弹到原位
     */
    public static final int REFRESH_CANNOT = 2;

    /**
     * 刷新中
     */
    public static final int REFRESH_ING = 3;

    /**
     * 刷新完成
     */
    public static final int REFRESH_COMPLETE = 4;

    /**
     * 记录当前刷新状态
     */
    private int isRefreshState = 0;

    /**
     * 加载相关操作前的状态
     */
    private static final int LOAD_MORE_PRE = 0;

    /**
     * 加载中 用于判断是否转交事件
     */
    private static final int LOAD_MORE_ING = 1;

    /**
     * 所有数据加载完成
     */
    private static final int LOAD_MORE_OVER = 2;

    /**
     * 加载中
     */
    private static final int LOAD_MORE_ING_II = 3;

    /**
     * 加载完成
     */
    private static final int LOAD_MORE_COMPLETE = 4;

    /**
     * 记录当前加载状态
     */
    private int isLoadMoreState = 0;

    /**
     * 手指仍在滑动时判断是否应该将事件交给middleView;
     */
    private boolean isShouldScrollMiddleView = false;

    /**
     * 此时middleView处理事件
     */
    private static final int DAMP_NONE = 0;

    /**
     * 顶部可以下拉
     */
    private static final int DAMP_TOP = 1;

    /**
     * 底部可以上拉
     */
    private static final int DAMP_BOTTOM = -1;

    /**
     * 当前拖动状态
     */
    private int isDampTopOrBottom = DAMP_NONE;

    /**
     * 下拉前
     */
    private static final int PULL_DOWN_PRE = 0;

    /**
     * 下拉中
     */
    private static final int PULL_DOWN_ING = 1;

    /**
     * 下拉完成
     */
    private static final int PULL_DOWN_COMPLETE = 2;

    /**
     * 当前下拉状态
     */
    private int isPullDownState = 0;

    /**
     * 上滑前
     */
    private static final int UPGLIDE_PRE = 0;

    /**
     * 上滑中
     */
    private static final int UPGLIDE_ING = 1;

    /**
     * 上滑完成
     */
    private static final int UPGLIDE_COMPLETE = 2;

    /**
     * 当前上滑状态
     */
    private int isUpglide = 0;

    /**
     * 顶层View
     */
    private View topView;

    /**
     * 中间层View
     */
    private View middleView;

    /**
     * 底层View
     */
    private View bottomView;

    /**
     * 顶部下拉时阻尼值最大时的距离
     */
    private float maxTopDampValue = 200f;

    /**
     * 是否是外部修改maxTopValue
     */
    private boolean isModidyMaxTopValue = false;

    /**
     * 底部上滑时阻尼值最大时的距离
     */
    private float maxBottomDampValue = 200f;

    /**
     * 是否外部修改MaxBottomValue
     */
    private boolean isModidyMaxBottomValue = false;

    /**
     * 保存上一次move时手指在Y轴的位置
     */
    private int mInitialDownY;

    /**
     * 保存topView的原始marginTop值
     */
    private int mInitialTopViewMarginTop;

    /**
     * 实时改变的topView的marginTop值
     */
    private int mChangedTopViewMarginTop = 0;

    /**
     * topView的MarginLayoutParams
     */
    private MarginLayoutParams topViewMarginParams;

    /**
     * 保存最后一次MotionEvent
     */
    private MotionEvent mLastMoveMotionEvent;

    /**
     * 单独保存dispatchTouchEvent中上一次MOVE的位置
     */
    private int mDispatchDownY;

    /**
     * 记录MiddleView移动的总值
     */
    private int mChangedMiddleHeight = 0;

    /**
     * BottomView的高度
     * 单位：dp
     */
    private int mBottomViewHeight = DampBottomViewChild.DAMPBOTTOMVIEW_HEIGHT;

    /**
     * TopView的高度
     * 单位：dp
     */
    private int mTopViewHeight = DampTopViewChild.DAMPTOPVIEW_HEIGHT;

    /**
     * 动画时长
     */
    private int animationDuration = 200;

    /**
     * 当前是否有动画在播放
     */
    private boolean isAnimationPlay = false;

    /**
     * 用于保存动画上一次数值，计算滑动距离。
     */
    private int preAnimationValue;


    /**
     * BottomView的高度
     * 单位：px
     */
    private int mInitialBottomViewHeight;

    /**
     * topView必须要实现的监听，用于监听容器状态来做刷新相关的操作
     */
    private DampTopViewListener mDampRefreshListenerInChild;

    /**
     * 这个接口用于提供给外部监听当前容器是否触发刷新
     */
    public interface DampRefreshListener {
        void onScrollChanged(int dy, int topViewPosition);

        void onRefreshing();
    }

    /**
     * 用于存放监听容器刷新状态的监听器的list
     */
    private List<DampRefreshListener> mDampRefreshListeners = new ArrayList<>();

    /**
     * topView必须要实现的监听，用于监听容器状态来做加载相关的操作
     */
    private DampBottomViewListener mDampLoadMoreListenerInChild;

    /**
     * 这个接口用于提供给外部监听当前容器是否触发加载
     */
    public interface DampLoadMoreListener {
        void onScrollChanged(int dy, int bottomViewPosition);

        void onLoading();
    }

    /**
     * 用于存放监听容器加载状态的监听器的list
     */
    private List<DampLoadMoreListener> mDampLoadMoreListeners = new ArrayList<>();

    /**
     * 加载到bottomView完全显示的动画
     */
    private ValueAnimator loadAnimator;

    private ValueAnimator defaultTopSpringbackAnimator = new ValueAnimator();

    private ValueAnimator defaultBottomSpringbackAnimator = new ValueAnimator();

    private boolean isShouldStopLoadAnimation = false;

    /**
     * 当前追踪的手指Id
     */
    private int mScrollPointerId = 0;

    private float mPullDownDampValue = 20f;

    private float mUpGlideDampValue = 20f;

    public static class Builder implements DampRefreshAndLoadMoreLayoutBuilderService{

        private DampRefreshAndLoadMoreLayout dampRefreshAndLoadMoreLayout;

        public Builder(Context context) {
            dampRefreshAndLoadMoreLayout = new DampRefreshAndLoadMoreLayout(context);
        }

        public Builder(){

        }

        @Override
        public Builder attachLayout(DampRefreshAndLoadMoreLayout dampRefreshAndLoadMoreLayout){
            this.dampRefreshAndLoadMoreLayout = dampRefreshAndLoadMoreLayout;
            return this;
        }

        @Override
        public Builder setTopView() {
            dampRefreshAndLoadMoreLayout.setTopView();
            return this;
        }

        @Override
        public Builder setTopView(View view, int topViewHeight) {
            dampRefreshAndLoadMoreLayout.setTopView(view,topViewHeight);
            return this;
        }

        @Override
        public Builder setBottomView() {
            dampRefreshAndLoadMoreLayout.setBottomView();
            return this;
        }

        @Override
        public Builder setBottomView(View view, int bottomViewHeight) {
            dampRefreshAndLoadMoreLayout.setBottomView(view,bottomViewHeight);
            return this;
        }

        @Override
        public Builder addOnDampRefreshListener(DampRefreshListener dampRefreshListener) {
            dampRefreshAndLoadMoreLayout.addOnDampRefreshListener(dampRefreshListener);
            return this;
        }

        @Override
        public Builder addOnDampLoadMoreListener(DampLoadMoreListener dampLoadMoreListener) {
            dampRefreshAndLoadMoreLayout.addOnDampLoadMoreListener(dampLoadMoreListener);
            return this;
        }

        @Override
        public Builder setAnimationDuration(int duration) {
            dampRefreshAndLoadMoreLayout.setAnimationDuration(duration);
            return this;
        }

        @Override
        public Builder setPullDownDampDistance(int value) {
            dampRefreshAndLoadMoreLayout.setPullDownDampDistance(value);
            return this;
        }

        @Override
        public Builder setUpGlideDampDistance(int value) {
            dampRefreshAndLoadMoreLayout.setUpGlideDampDistance(value);
            return this;
        }

        @Override
        public Builder setPullDownDampValue(float pullDownDampValue) {
            dampRefreshAndLoadMoreLayout.setPullDownDampValue(pullDownDampValue);
            return this;
        }

        @Override
        public Builder setUpGlideDampValue(float upGlideDampValue) {
            dampRefreshAndLoadMoreLayout.setUpGlideDampValue(upGlideDampValue);
            return this;
        }

        @Override
        public DampRefreshAndLoadMoreLayout build() {
            return dampRefreshAndLoadMoreLayout;
        }
    }

    public DampRefreshAndLoadMoreLayout(Context context) {
        super(context);
        mContext = context;
        initThis();
    }

    public DampRefreshAndLoadMoreLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initThis();

    }

    public DampRefreshAndLoadMoreLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initThis();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            middleView = getChildAt(0);
        }
    }

    private void initThis() {
        this.setOrientation(LinearLayout.VERTICAL);
        maxTopDampValue = dp2px(mContext, maxTopDampValue);
        maxBottomDampValue = dp2px(mContext, maxBottomDampValue);

    }

    /**
     * 初始化方法，初始化一些必须重置的状态
     */
    private void resetState() {
        isDampTopOrBottom = DAMP_NONE;
        isPullDownState = PULL_DOWN_PRE;
        isUpglide = UPGLIDE_PRE;
    }

    private void resetTopViewState() {
        mChangedTopViewMarginTop = mInitialTopViewMarginTop;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int actionIndex = ev.getActionIndex();

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mScrollPointerId = ev.getPointerId(0);
                mInitialDownY = (int) (ev.getY() + 0.5f);
                resetState();
                if (isLoadMoreState == LOAD_MORE_ING) {
                    isDampTopOrBottom = DAMP_BOTTOM;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mScrollPointerId = ev.getPointerId(actionIndex);
                mInitialDownY = (int) (ev.getY(actionIndex) + 0.5f);
                break;
            case MotionEvent.ACTION_MOVE:
                final int index = ev.findPointerIndex(mScrollPointerId);

                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id "
                            + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                int nowY = (int) (ev.getY(index) + 0.5f);

                int offsetY = mInitialDownY - nowY;
                mInitialDownY = nowY;
                if (topView != null) {
                    if (!middleView.canScrollVertically(-1)) {
                        if (offsetY < 0) {//判断子view是否滑动到顶部并且当前是下滑
                            isDampTopOrBottom = DAMP_TOP;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            return true;
                        }
                        if (offsetY >= 0) {
                            if (isRefreshState == REFRESH_ING && mChangedTopViewMarginTop > mInitialTopViewMarginTop) {
                                //刷新时若topview在初始位置下面，则拦截事件
                                isDampTopOrBottom = DAMP_TOP;
                                getParent().requestDisallowInterceptTouchEvent(true);
                                return true;
                            }
                        }
                    }
                } else {
                    if (!middleView.canScrollVertically(-1)) {
                        if (offsetY < 0) {//判断子view是否滑动到顶部并且当前是下滑
                            isDampTopOrBottom = DAMP_TOP;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            return true;
                        }
                    }
                }
                if (bottomView != null) {
                    if (!middleView.canScrollVertically(1)) {
                        if (offsetY > 0) {
                            //判断子view是否滑动到底部并且当前是上滑
                            isDampTopOrBottom = DAMP_BOTTOM;
                            if (isLoadMoreState == LOAD_MORE_PRE && isLoadMoreState != LOAD_MORE_OVER) {
                                if (mDampLoadMoreListeners != null) {
                                    for (DampLoadMoreListener dampLoadMoreListener : mDampLoadMoreListeners) {
                                        dampLoadMoreListener.onLoading();
                                    }
                                }
                                if (mDampLoadMoreListenerInChild != null) {
                                    mDampLoadMoreListenerInChild.onLoading();
                                }
                            }
                            getParent().requestDisallowInterceptTouchEvent(true);
                            return true;
                        }
                    }
                } else {
                    if (!middleView.canScrollVertically(1)) {
                        if (offsetY > 0) {
                            //判断子view是否滑动到顶部并且当前是上滑
                            isDampTopOrBottom = DAMP_BOTTOM;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            return true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (ev.getPointerId(actionIndex) == mScrollPointerId) {
                    final int newIndex = actionIndex == 0 ? 1 : 0;
                    mScrollPointerId = ev.getPointerId(newIndex);
                    mInitialDownY = (int) (ev.getY(newIndex) + 0.5f);
                }
                break;
            case MotionEvent.ACTION_UP:
                //重置必须要重置的状态
                resetState();
                mScrollPointerId = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                //重置必须要重置的状态
                resetState();
                mScrollPointerId = 0;
                break;
        }
        isDampTopOrBottom = DAMP_NONE;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int actionIndex = event.getActionIndex();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mScrollPointerId = event.getPointerId(0);
                mInitialDownY = (int) (event.getY() + 0.5f);
                if (isLoadMoreState == LOAD_MORE_ING) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mScrollPointerId = event.getPointerId(actionIndex);
                mInitialDownY = (int) (event.getY(actionIndex) + 0.5f);
                break;
            case MotionEvent.ACTION_MOVE:
                final int index = event.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id "
                            + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                int nowY = (int) (event.getY(index) + 0.5f);

                int offsetY = mInitialDownY - nowY;

                mInitialDownY = nowY;
                if (!isAnimationPlay) {
                    if (isDampTopOrBottom == DAMP_TOP && !middleView.canScrollVertically(-1)) {
                        if (topView != null) {
                            if (offsetY < 0 || isRefreshState == REFRESH_ING) {//判断当前是否是顶部可拉动状态
                                isPullDownState = PULL_DOWN_ING;//复原下拉状态
                            }
                            if (isPullDownState == PULL_DOWN_ING) {
                                float nowMarginTop;
                                if (offsetY > 0 || mChangedTopViewMarginTop <= 0) {
                                    nowMarginTop = mChangedTopViewMarginTop - offsetY;
                                    nowMarginTop += 0.5f;
                                } else if (isRefreshState == REFRESH_ING && topView.getTop() >= mTopViewHeight) {
                                    nowMarginTop = (mChangedTopViewMarginTop - offsetY * measureDampTopValue(mChangedTopViewMarginTop + mTopViewHeight));
                                    nowMarginTop += 0.5f;
                                } else {
                                    nowMarginTop = (mChangedTopViewMarginTop - offsetY * measureDampTopValue(mChangedTopViewMarginTop + mTopViewHeight));
                                    nowMarginTop += 0.5f;
                                }

                                setTopMarigin(topView, topViewMarginParams, (int) nowMarginTop, mInitialTopViewMarginTop);

                                mChangedTopViewMarginTop = (int) nowMarginTop;
                                if (mChangedTopViewMarginTop > -mInitialTopViewMarginTop) {
                                    //当前下拉距离足够刷新
                                    if (isRefreshState != REFRESH_ING) {
                                        isRefreshState = REFRESH_READY;
                                        if (mDampRefreshListenerInChild != null) {
                                            mDampRefreshListenerInChild.onReady();
                                        }
                                    }
                                } else {
                                    //当前下拉距离不够刷新
                                    if (isRefreshState != REFRESH_ING) {
                                        isRefreshState = REFRESH_CANNOT;
                                        if (mDampRefreshListenerInChild != null) {
                                            mDampRefreshListenerInChild.onCancel();
                                        }
                                    }
                                }
                                if (nowMarginTop < mInitialTopViewMarginTop) {
                                    //如果顶部view回到原位但是仍然在上滑时添加此标记
                                    isPullDownState = PULL_DOWN_COMPLETE;
                                    mChangedTopViewMarginTop = mInitialTopViewMarginTop;
                                }
                                if (mDampRefreshListenerInChild != null) {
                                    mDampRefreshListenerInChild.onScrollChanged((int) (offsetY * measureDampTopValue(mChangedTopViewMarginTop) + 0.5f), mChangedTopViewMarginTop);
                                }
                                if (mDampRefreshListeners != null) {
                                    for (DampRefreshListener dampRefreshListener : mDampRefreshListeners) {
                                        dampRefreshListener.onScrollChanged((int) (offsetY * measureDampTopValue(mChangedTopViewMarginTop) + 0.5f), mChangedTopViewMarginTop);
                                    }
                                }
                            }
                        } else {
                            //如果topView为空则只实现回弹效果
                            if (offsetY < 0) {//判断当前是否是顶部可拉动状态
                                isPullDownState = PULL_DOWN_ING;//复原下拉状态
                            }
                            if (isPullDownState == PULL_DOWN_ING) {
                                if (offsetY > 0) {
                                    setMiddleViewLayoutForPullDown(middleView, middleView.getTop(), middleView.getBottom(), -offsetY);
                                    mChangedMiddleHeight += offsetY;
                                } else {
                                    float nowOffsetY = offsetY * measureDampMiddleValueForPullDown(mChangedMiddleHeight);
                                    nowOffsetY += 0.5f;
                                    setMiddleViewLayoutForPullDown(middleView, middleView.getTop(), middleView.getBottom(), -(int) nowOffsetY);
                                    mChangedMiddleHeight += (int) nowOffsetY;
                                }
                                if (mChangedMiddleHeight > 0) {
                                    //如果MidlleView回到原位但是仍在下拉时添加此标记
                                    isPullDownState = PULL_DOWN_COMPLETE;
                                    mChangedMiddleHeight = 0;
                                }
                            }
                        }
                    } else if (isDampTopOrBottom == DAMP_BOTTOM && !canScrollVertically(1)) {
                        if (bottomView != null) {
                            if (isLoadMoreState == LOAD_MORE_OVER) {
                                if (offsetY > 0) {
                                    isUpglide = UPGLIDE_ING;
                                }
                                if (isUpglide == UPGLIDE_ING) {
                                    float nowOffsetY;
                                    if (offsetY < 0) {
                                        nowOffsetY = (float) offsetY + 0.5f;
                                    } else {
                                        nowOffsetY = offsetY * measureDampMiddleValueForUpGlide(mChangedMiddleHeight);
                                        nowOffsetY += 0.5f;
                                    }
                                    setMiddleViewLayout(middleView, middleView.getTop(), middleView.getBottom(), -(int) nowOffsetY);
                                    setBottomViewLayout(bottomView, bottomView.getTop(), bottomView.getBottom(), -(int) nowOffsetY, mInitialBottomViewHeight);
                                    mChangedMiddleHeight += (int) nowOffsetY;

                                    if (mChangedMiddleHeight < 0) {
                                        //如果MidlleView回到原位但是仍在下拉时添加此标记
                                        isUpglide = UPGLIDE_COMPLETE;
                                        mChangedMiddleHeight = 0;
                                    }

                                    if (mDampLoadMoreListeners != null) {
                                        for (DampLoadMoreListener dampLoadMoreListener : mDampLoadMoreListeners) {
                                            dampLoadMoreListener.onScrollChanged((int) nowOffsetY, mChangedMiddleHeight);
                                        }
                                    }
                                    if (mDampLoadMoreListenerInChild != null) {
                                        mDampLoadMoreListenerInChild.onScrollChanged((int) nowOffsetY, mChangedMiddleHeight);
                                    }
                                }
                            } else {
                                if (offsetY > 0 || isLoadMoreState == LOAD_MORE_ING) {//判断当前是否是底部可上滑状态
                                    isUpglide = UPGLIDE_ING;
                                    isLoadMoreState = LOAD_MORE_ING;
                                }
                                if (isUpglide == UPGLIDE_ING) {
                                    float nowOffsetY;
                                    if (offsetY < 0) {
                                        nowOffsetY = offsetY;
                                    } else {
                                        nowOffsetY = offsetY * measureDampMiddleValueForUpGlide(mChangedMiddleHeight);
                                        nowOffsetY += 0.5f;
                                    }
                                    setMiddleViewLayout(middleView, middleView.getTop(), middleView.getBottom(), -(int) nowOffsetY);
                                    setBottomViewLayout(bottomView, bottomView.getTop(), bottomView.getBottom(), -(int) nowOffsetY, mInitialBottomViewHeight);
                                    mChangedMiddleHeight += (int) nowOffsetY;

                                    if (mChangedMiddleHeight < 0) {
                                        //如果MidlleView回到原位但是仍在下拉时添加此标记
                                        isUpglide = UPGLIDE_COMPLETE;
                                        isLoadMoreState = LOAD_MORE_ING_II;
                                        mChangedMiddleHeight = 0;
                                    }

                                    if (mDampLoadMoreListeners != null) {
                                        for (DampLoadMoreListener dampLoadMoreListener : mDampLoadMoreListeners) {
                                            dampLoadMoreListener.onScrollChanged((int) nowOffsetY, mChangedMiddleHeight);
                                        }
                                    }
                                    if (mDampLoadMoreListenerInChild != null) {
                                        mDampLoadMoreListenerInChild.onScrollChanged((int) nowOffsetY, mChangedMiddleHeight);
                                    }
                                }
                            }
                        } else {
                            //没有设置bottomView时只实现Damp效果
                            if (offsetY > 0) {//判断当前是否是底部可上滑状态
                                isUpglide = UPGLIDE_ING;
                            }
                            if (isUpglide == UPGLIDE_ING) {
                                float nowOffsetY;
                                if (offsetY < 0) {
                                    nowOffsetY = offsetY;
                                } else {
                                    nowOffsetY = offsetY * measureDampMiddleValueForUpGlide(mChangedMiddleHeight);
                                    nowOffsetY += 0.5f;
                                }
                                setMiddleViewLayout(middleView, middleView.getTop(), middleView.getBottom(), -(int) nowOffsetY);
                                mChangedMiddleHeight += (int) nowOffsetY;
                                if (mChangedMiddleHeight <= 0) {
                                    //如果MidlleView回到原位但是仍在下拉时添加此标记
                                    isUpglide = UPGLIDE_COMPLETE;
                                    mChangedMiddleHeight = 0;
                                }
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isAnimationPlay) {
                    if (isDampTopOrBottom == DAMP_TOP && isPullDownState == PULL_DOWN_ING) {
                        if (topView != null) {
                            if (isRefreshState == REFRESH_CANNOT) {
                                //当下拉距离不够刷新时,执行该情景下动画，并初始化Refresh状态
                                startDampTopToHomeAnimation();
                                isRefreshState = REFRESH_PRE;
                                resetTopViewState();
                            } else if (mChangedTopViewMarginTop < 0) {
                                //为解决刷新完成状态全交由外部决定的需求，此处可以实现在刷新状态时，topView尚未完全显示的时候可以带有回弹效果
                                startDampTopToHomeAnimation();
                                resetTopViewState();
                            } else if (isRefreshState == REFRESH_READY) {
                                //当状态为即将触发刷新时，执行该情景下动画，并且矫正topView结果位置
                                startDampTopToRefreshAnimation();
                                mChangedTopViewMarginTop = 0;
                                //刷新必需步骤执行后将状态置为正在刷新
                                isRefreshState = REFRESH_ING;
                                if (mDampRefreshListenerInChild != null) {
                                    mDampRefreshListenerInChild.onRefreshing();
                                }
                                if (mDampRefreshListeners != null) {
                                    for (DampRefreshListener dampRefreshListener : mDampRefreshListeners) {
                                        dampRefreshListener.onRefreshing();
                                    }
                                }
                            } else if (isRefreshState == REFRESH_ING && middleView.getTop() >= mTopViewHeight) {
                                //为解决刷新完成状态全交由外部决定的需求，此处实现正在刷新时，topView完全显示的时候可以带有回弹效果
                                startDampTopToRefreshAnimation();
                                mChangedTopViewMarginTop = 0;
                            }
                        } else {
                            startDampMiddleForPullDown();
                            mChangedMiddleHeight = 0;
                        }
                        //重置拖动状态
                        resetState();
                    } else if (isDampTopOrBottom == DAMP_BOTTOM && isUpglide == UPGLIDE_ING) {
                        if (bottomView != null) {
                            if (isLoadMoreState == LOAD_MORE_ING) {
                                startDampMiddleAndBottomAnimationOnLoadMore();
                                isUpglide = UPGLIDE_PRE;
                                mChangedMiddleHeight = mInitialBottomViewHeight;
                            } else if (isLoadMoreState == LOAD_MORE_OVER) {
                                startDampMiddleAndBottomAnimationOnLoadOver();
                                isUpglide = UPGLIDE_PRE;
                                mChangedMiddleHeight = 0;
                            }
                        } else {
                            stratDampMiddleForUpGlide();
                            isUpglide = UPGLIDE_PRE;
                            mChangedMiddleHeight = 0;
                        }
                        resetState();
                    }
                }
                mScrollPointerId = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerId(actionIndex) == mScrollPointerId) {
                    final int newIndex = actionIndex == 0 ? 1 : 0;
                    mScrollPointerId = event.getPointerId(newIndex);
                    mInitialDownY = (int) (event.getY(newIndex) + 0.5f);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!isAnimationPlay) {
                    if (isDampTopOrBottom == DAMP_TOP && isPullDownState == PULL_DOWN_ING) {
                        if (topView != null) {
                            if (isRefreshState == REFRESH_CANNOT) {
                                //当下拉距离不够刷新时,执行该情景下动画，并初始化Refresh状态
                                startDampTopToHomeAnimation();
                                isRefreshState = REFRESH_PRE;
                                resetTopViewState();
                            } else if (mChangedTopViewMarginTop < 0) {
                                //为解决刷新完成状态全交由外部决定的需求，此处可以实现在刷新状态时，topView尚未完全显示的时候可以带有回弹效果
                                startDampTopToHomeAnimation();
                                resetTopViewState();
                            } else if (isRefreshState == REFRESH_READY) {
                                //当状态为即将触发刷新时，执行该情景下动画，并且矫正topView结果位置
                                startDampTopToRefreshAnimation();
                                mChangedTopViewMarginTop = 0;
                                //刷新必需步骤执行后将状态置为正在刷新
                                isRefreshState = REFRESH_ING;
                                if (mDampRefreshListenerInChild != null) {
                                    mDampRefreshListenerInChild.onRefreshing();
                                }
                                if (mDampRefreshListeners != null) {
                                    for (DampRefreshListener dampRefreshListener : mDampRefreshListeners) {
                                        dampRefreshListener.onRefreshing();
                                    }
                                }
                            } else if (isRefreshState == REFRESH_ING && middleView.getTop() >= mTopViewHeight) {
                                //为解决刷新完成状态全交由外部决定的需求，此处实现正在刷新时，topView完全显示的时候可以带有回弹效果
                                startDampTopToRefreshAnimation();
                                mChangedTopViewMarginTop = 0;
                            }
                        } else {
                            startDampMiddleForPullDown();
                            mChangedMiddleHeight = 0;
                        }
                        //重置拖动状态
                        resetState();
                    } else if (isDampTopOrBottom == DAMP_BOTTOM && isUpglide == UPGLIDE_ING) {
                        if (bottomView != null) {
                            if (isLoadMoreState == LOAD_MORE_ING) {
                                startDampMiddleAndBottomAnimationOnLoadMore();
                                isUpglide = UPGLIDE_PRE;
                                mChangedMiddleHeight = mInitialBottomViewHeight;
                            } else if (isLoadMoreState == LOAD_MORE_OVER) {
                                startDampMiddleAndBottomAnimationOnLoadOver();
                                isUpglide = UPGLIDE_PRE;
                                mChangedMiddleHeight = 0;
                            }
                        } else {
                            stratDampMiddleForUpGlide();
                            isUpglide = UPGLIDE_PRE;
                            mChangedMiddleHeight = 0;
                        }
                        resetState();
                    }
                }
                mScrollPointerId = 0;
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDispatchDownY = (int) (ev.getY() + 0.5f);
                break;
            case MotionEvent.ACTION_MOVE:
                mLastMoveMotionEvent = ev;
                int nowY = (int) (ev.getY() + 0.5f);
                int offsetY = mDispatchDownY - nowY;
                mDispatchDownY = nowY;

                if ((!middleView.canScrollVertically(-1) && offsetY < 0 && isPullDownState == PULL_DOWN_PRE)
                        || (!middleView.canScrollVertically(1) && offsetY > 0 && isUpglide == UPGLIDE_PRE)) {
                    //当middleView滑动到顶部或者底部，执行此方法，使得父容器可以执行拦截方法。
                    requestDisallowInterceptTouchEvent(false);
                }

                if ((!middleView.canScrollVertically(-1) && offsetY > 0 && isPullDownState == PULL_DOWN_COMPLETE)
                        || (!middleView.canScrollVertically(1) && offsetY < 0 && isUpglide == UPGLIDE_COMPLETE)) {
                    //判断上述前置条件，模拟down事件激活拦截方法，将事件交由子View
                    sendDownEvent(mLastMoveMotionEvent);
                    resetState();
                } else if (isShouldScrollMiddleView) {
                    isShouldScrollMiddleView = false;
                    sendDownEvent(mLastMoveMotionEvent);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    /**
     * @param ev 模拟cancel事件
     */
    private void sendCancelEvent(MotionEvent ev) {
        MotionEvent e = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(), ev.getMetaState());
        super.dispatchTouchEvent(e);
    }

    /**
     * @param ev 模拟down事件
     */
    private void sendDownEvent(MotionEvent ev) {
        MotionEvent e = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), ev.getMetaState());
        super.dispatchTouchEvent(e);
    }

    /**
     * @return view.height
     * 获取view的高度
     */
    private int getViewHeight(View view) {
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(0, h);
        return view.getMeasuredHeight();
    }

    /*以下是顶部View相关函数*/

    /**
     * set margintop 的方法
     */
    private void setTopMarigin(View targetView, MarginLayoutParams targetMarginParams, int mariginTopValue, int initialValue) {
        if (mariginTopValue >= initialValue) {
            targetMarginParams.setMargins(0, mariginTopValue, 0, 0);
            targetView.requestLayout();
        } else {
            targetMarginParams.setMargins(0, initialValue, 0, 0);
            targetView.requestLayout();
        }
    }

    /**
     * 顶部完全回弹时的动画
     */
    private void startDampTopToHomeAnimation() {
        preAnimationValue = mChangedTopViewMarginTop;
        final ValueAnimator animator = ValueAnimator.ofInt(mChangedTopViewMarginTop, mInitialTopViewMarginTop);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setTopMarigin(topView, topViewMarginParams, (int) animation.getAnimatedValue(), mInitialTopViewMarginTop);
                preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();
                if (mDampRefreshListenerInChild != null) {
                    mDampRefreshListenerInChild.onScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());

                }
                if (mDampRefreshListeners != null) {
                    for (DampRefreshListener dampRefreshListener : mDampRefreshListeners) {
                        dampRefreshListener.onScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());
                    }
                }
                preAnimationValue = (int) animation.getAnimatedValue();
                if (mInitialTopViewMarginTop == (int) animation.getAnimatedValue()) {
                    isAnimationPlay = false;
                }
            }
        });
        animator.start();
        isAnimationPlay = true;
    }

    /**
     * 回弹到刷新位置的动画PRE
     */
    private void startDampTopToRefreshAnimation() {
        preAnimationValue = mChangedTopViewMarginTop;
        ValueAnimator animator = ValueAnimator.ofInt(mChangedTopViewMarginTop, 0);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                setTopMarigin(topView, topViewMarginParams, (int) animation.getAnimatedValue(), mInitialTopViewMarginTop);
                preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();
                if (mDampRefreshListenerInChild != null) {
                    mDampRefreshListenerInChild.onScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());

                }
                if (mDampRefreshListeners != null) {
                    for (DampRefreshListener dampRefreshListener : mDampRefreshListeners) {
                        dampRefreshListener.onScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());
                    }
                }
                preAnimationValue = (int) animation.getAnimatedValue();
                if ((int) animation.getAnimatedValue() == 0) {
                    isAnimationPlay = false;
                }
            }
        });
        animator.start();
        isAnimationPlay = true;
    }

    /**
     * @param valueAnimator 取消Value动画
     */
    private void cancelAnimation(ValueAnimator valueAnimator) {
        valueAnimator.cancel();
    }

    /**
     * @return dampvalue
     * 计算顶部下拉时的实时阻尼值
     */
    private float measureDampTopValue(float marginValue) {
        float dampTopValue = 100;
        if (marginValue < 0f) {
            marginValue = 0f;
        }
        dampTopValue = (maxTopDampValue - marginValue) / (maxTopDampValue / 100f);
        if (dampTopValue < mPullDownDampValue) {
            dampTopValue = mPullDownDampValue;
        }
        return dampTopValue / 100f;
    }

    /*以上是顶部View相关函数*/

    /*以下是中间View和底部View相关函数*/


    /**
     * 加载时的回弹动画
     */
    private void startDampMiddleAndBottomAnimationOnLoadMore() {
        final int topMiddle = middleView.getTop();
        final int bottomMiddle = middleView.getBottom();
        final int topBottom = bottomView.getTop();
        final int bottomBottom = bottomView.getBottom();
        final int lastValue = mChangedMiddleHeight - mInitialBottomViewHeight;

        preAnimationValue = 0;

        loadAnimator = ValueAnimator.ofInt(0, mChangedMiddleHeight - mInitialBottomViewHeight);
        loadAnimator.setDuration(animationDuration);
        loadAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                middleView.layout(middleView.getLeft(), topMiddle + (int) animation.getAnimatedValue(), middleView.getRight(), bottomMiddle + (int) animation.getAnimatedValue());
                bottomView.layout(bottomView.getLeft(), topBottom + (int) animation.getAnimatedValue(), bottomView.getRight(), bottomBottom + (int) animation.getAnimatedValue());
                preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();
                if (mDampLoadMoreListenerInChild != null) {
                    mDampLoadMoreListenerInChild.onScrollChanged(preAnimationValue, getBottom() - middleView.getBottom());

                }
                if (mDampLoadMoreListeners != null) {
                    for (DampLoadMoreListener dampLoadMoreListener : mDampLoadMoreListeners) {
                        dampLoadMoreListener.onScrollChanged(preAnimationValue, getBottom() - middleView.getBottom());
                    }
                }
                preAnimationValue = (int) animation.getAnimatedValue();
                if ((int) animation.getAnimatedValue() == lastValue) {
                    isAnimationPlay = false;
                }
            }
        });
        loadAnimator.start();
        isAnimationPlay = true;
    }

    /**
     * 全部加载完成时的回弹动画
     */
    private void startDampMiddleAndBottomAnimationOnLoadOver() {
        final int topMiddle = middleView.getTop();
        final int bottomMiddle = middleView.getBottom();
        final int topBottom = bottomView.getTop();
        final int bottomBottom = bottomView.getBottom();
        final int lastValue = mChangedMiddleHeight;

        preAnimationValue = 0;
        final ValueAnimator animator = ValueAnimator.ofInt(0, mChangedMiddleHeight);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setMiddleViewLayout(middleView, topMiddle, bottomMiddle, (int) animation.getAnimatedValue());
                setBottomViewLayout(bottomView, topBottom, bottomBottom, (int) animation.getAnimatedValue(), mInitialBottomViewHeight);

                preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();
                if (mDampLoadMoreListenerInChild != null) {
                    mDampLoadMoreListenerInChild.onScrollChanged(preAnimationValue, getBottom() - middleView.getBottom());

                }
                if (mDampLoadMoreListeners != null) {
                    for (DampLoadMoreListener dampLoadMoreListener : mDampLoadMoreListeners) {
                        dampLoadMoreListener.onScrollChanged(preAnimationValue, getBottom() - middleView.getBottom());
                    }
                }
                preAnimationValue = (int) animation.getAnimatedValue();

                if ((int) animation.getAnimatedValue() == lastValue) {
                    isAnimationPlay = false;
                    if (mDampLoadMoreListenerInChild != null) {
                        mDampLoadMoreListenerInChild.onLoaded();
                    }
                }
            }
        });
        animator.start();
        isAnimationPlay = true;
    }

    /**
     * 没有bottimView时的回弹动画
     */
    private void stratDampMiddleForUpGlide() {
        final int topMiddle = middleView.getTop();
        final int bottomMiddle = middleView.getBottom();
        final int lastValue = mChangedMiddleHeight;
        final ValueAnimator animator = ValueAnimator.ofInt(0, mChangedMiddleHeight);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setMiddleViewLayout(middleView, topMiddle, bottomMiddle, (int) animation.getAnimatedValue());
                if ((int) animation.getAnimatedValue() == lastValue) {
                    isAnimationPlay = false;
                }
            }
        });
        animator.start();
        isAnimationPlay = true;
    }

    /**
     * 没有topView时的回弹动画
     */
    private void startDampMiddleForPullDown() {
        final int topMiddle = middleView.getTop();
        final int bottomMiddle = middleView.getBottom();
        final int lastValue = mChangedMiddleHeight;

        defaultTopSpringbackAnimator.setIntValues(0, mChangedMiddleHeight);
        defaultTopSpringbackAnimator.setDuration(animationDuration);
        defaultTopSpringbackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setMiddleViewLayoutForPullDown(middleView, topMiddle, bottomMiddle, (int) animation.getAnimatedValue());
                if ((int) animation.getAnimatedValue() == lastValue) {
                    isAnimationPlay = false;
                }
            }
        });
        defaultTopSpringbackAnimator.start();
        isAnimationPlay = true;
    }

    /**
     * 上滑时middleView设置布局位置的方法
     */
    private void setMiddleViewLayout(View targetView, int top, int bottom, int changedValue) {
        if (((getBottom() - getTop()) - (bottom + changedValue)) >= 0) {
            targetView.layout(targetView.getLeft(), top + changedValue, targetView.getRight(), bottom + changedValue);
        } else {
            targetView.layout(targetView.getLeft(), 0, targetView.getRight(), getBottom() - getTop());
        }
    }

    /**
     * @param changedValue 下拉时middleView设置布局位置的方法
     */
    private void setMiddleViewLayoutForPullDown(View targetView, int top, int bottom, int changedValue) {
        if ((top + changedValue) >= 0) {
            targetView.layout(targetView.getLeft(), top + changedValue, targetView.getRight(), bottom + changedValue);
        } else {
            targetView.layout(targetView.getLeft(), 0, targetView.getRight(), getBottom() - getTop());
        }
    }

    /**
     * @return changedValue
     * 测量middleView上滑的实时阻尼值
     */
    private float measureDampMiddleValueForUpGlide(float changedValue) {
        float dampValue;

        if (changedValue < 0f) {
            changedValue = 0f;
        }
        dampValue = (maxBottomDampValue - changedValue) / (maxBottomDampValue / 100f);
        if (dampValue < mUpGlideDampValue) {
            dampValue = mUpGlideDampValue;
        }
        return dampValue / 100f;
    }

    private float measureDampMiddleValueForPullDown(float changedValue) {
        float dampValue;

        if (changedValue > 0f) {
            changedValue = 0f;
        }

        dampValue = (maxTopDampValue + changedValue) / (maxTopDampValue / 100f);
        if (dampValue < mPullDownDampValue) {
            dampValue = mPullDownDampValue;
        }
        return dampValue / 100f;
    }

    /**
     * @param targetView   目标view
     * @param top          当前top
     * @param bottom       当前bottom
     * @param changedValue 改变值
     * @param initialValue bottomView的高度
     *                     bottomView设置布局位置的方法
     */
    private void setBottomViewLayout(View targetView, int top, int bottom, int changedValue, int initialValue) {
        if (((getBottom() - getTop()) - (targetView.getBottom() + changedValue)) >= (-initialValue)) {
            targetView.layout(targetView.getLeft(), top + changedValue, targetView.getRight(), bottom + changedValue);
        } else {
            targetView.layout(targetView.getLeft(), getBottom() - getTop(), targetView.getRight(), getBottom() - getTop() + initialValue);
        }
    }

    /*以上是中间View和底部View相关函数*/

    /**
     * @return px
     * 将dp转化为px
     */
    private int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 设置默认topView
     */
    @Override
    public void setTopView() {
        if (topView == null) {
            topView = new DampTopViewChild(mContext);
            try {
                mDampRefreshListenerInChild = (DampTopViewListener) topView;
                this.addView(topView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(mContext, mTopViewHeight)));
                //初始化topView相关
                topViewMarginParams = (MarginLayoutParams) topView.getLayoutParams();
                mInitialTopViewMarginTop = -dp2px(mContext, mTopViewHeight);
                mChangedTopViewMarginTop = mInitialTopViewMarginTop;
                setTopMarigin(topView, topViewMarginParams, mInitialTopViewMarginTop, mInitialTopViewMarginTop);
            } catch (Exception e) {
                Log.e(TAG, "setTopView: ", e);
            }
        }
    }

    /**
     * 添加自定义topView
     */
    @Override
    public void setTopView(View view, int topViewHeight) {
        if (topView == null) {
            topView = view;
            try {
                mDampRefreshListenerInChild = (DampTopViewListener) topView;
                mTopViewHeight = topViewHeight;
                this.addView(topView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(mContext, mTopViewHeight)));
                //初始化topView相关
                topViewMarginParams = (MarginLayoutParams) topView.getLayoutParams();
                mInitialTopViewMarginTop = -dp2px(mContext, mTopViewHeight);
                mChangedTopViewMarginTop = mInitialTopViewMarginTop;
                setTopMarigin(topView, topViewMarginParams, mInitialTopViewMarginTop, mInitialTopViewMarginTop);
            } catch (Exception e) {
                Log.e(TAG, "setTopView: ", e);
            }
        }
    }


    /**
     * 设置默认bottomView
     */
    @Override
    public void setBottomView() {
        if (bottomView == null) {
            bottomView = new DampBottomViewChild(mContext);
            try {
                mDampLoadMoreListenerInChild = (DampBottomViewListener) bottomView;
                if (topView == null) {
                    this.addView(bottomView, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(mContext, mBottomViewHeight)));
                } else {
                    this.addView(bottomView, 2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(mContext, mBottomViewHeight)));
                }
                mInitialBottomViewHeight = dp2px(mContext, mBottomViewHeight);
            } catch (Exception e) {
                Log.e("DampRecyclerViewParent", "setBottomView: ", e);
            }
        }
    }

    /**
     * 设置自定义bottomView
     */
    @Override
    public void setBottomView(View view, int bottomViewHeight) {
        if (bottomView == null) {
            this.bottomView = view;
            try {
                mDampLoadMoreListenerInChild = (DampBottomViewListener) bottomView;
                    mBottomViewHeight = bottomViewHeight;
                if (topView == null) {
                    this.addView(bottomView, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(mContext, mBottomViewHeight)));
                } else {
                    this.addView(bottomView, 2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(mContext, mBottomViewHeight)));
                }
                mInitialBottomViewHeight = dp2px(mContext, mBottomViewHeight);
            } catch (Exception e) {
                Log.e("DampRecyclerViewParent", "setBottomView: ", e);
            }
        }
    }

    /**
     * @param dampRefreshListener 添加refresh相关监听
     */
    @Override
    public void addOnDampRefreshListener(DampRefreshListener dampRefreshListener) {
        if (dampRefreshListener != null && mDampRefreshListeners != null) {
            mDampRefreshListeners.add(dampRefreshListener);
        }
    }

    /**
     * @param dampLoadMoreListener 添加loadmore相关监听
     */
    @Override
    public void addOnDampLoadMoreListener(DampLoadMoreListener dampLoadMoreListener) {
        if (dampLoadMoreListener != null && mDampLoadMoreListeners != null) {
            mDampLoadMoreListeners.add(dampLoadMoreListener);
        }
    }

    /**
     * 停止刷新
     */
    public void stopRefreshAnimation() {
        if (isRefreshState == REFRESH_ING && topView != null) {
            preAnimationValue = mChangedTopViewMarginTop;
            ValueAnimator animator = ValueAnimator.ofInt(mChangedTopViewMarginTop, mInitialTopViewMarginTop);
            animator.setDuration(animationDuration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setTopMarigin(topView, topViewMarginParams, (int) animation.getAnimatedValue(), mInitialTopViewMarginTop);

                    preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();
                    if (mDampRefreshListenerInChild != null) {
                        mDampRefreshListenerInChild.onScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());

                    }
                    if (mDampRefreshListeners != null) {
                        for (DampRefreshListener dampRefreshListener : mDampRefreshListeners) {
                            dampRefreshListener.onScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());
                        }
                    }
                    preAnimationValue = (int) animation.getAnimatedValue();

                    if ((int) animation.getAnimatedValue() == mInitialTopViewMarginTop) {
                        if (mDampRefreshListenerInChild != null) {
                            mDampRefreshListenerInChild.onStart();
                        }
                        isAnimationPlay = false;
                    }
                }
            });
            animator.start();
            if (mDampRefreshListenerInChild != null) {
                mDampRefreshListenerInChild.onComplete();
            }
            isAnimationPlay = true;
            isRefreshState = REFRESH_PRE;
            isLoadMoreState = LOAD_MORE_PRE;
            resetState();
            resetTopViewState();
        }
    }

    /**
     * 加载结束
     */
    public void stopLoadMoreAnimation() {
        if (isAnimationPlay) {
            if (loadAnimator != null) {
                loadAnimator.cancel();
                isAnimationPlay = false;
            }
            stopLoadMoreAnimation();
        } else if (isUpglide == UPGLIDE_ING || isDampTopOrBottom == DAMP_BOTTOM) {
            resetState();
            stopLoadMoreAnimation();
            isShouldScrollMiddleView = true;
        } else {
//            final int topMiddle = middleView.getTop();
//            final int bottomMiddle = middleView.getBottom();
//            final int topBottom = bottomView.getTop();
//            final int bottomBottom = bottomView.getBottom();
//            final int lastValue = mChangedMiddleHeight;
//            preAnimationValue = 0;
//            final ValueAnimator animator = ValueAnimator.ofInt(0,mChangedMiddleHeight);
//            animator.setDuration(animationDuration);
//            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    setMiddleViewLayout(middleView,topMiddle,bottomMiddle,(int)animation.getAnimatedValue());
//                    setBottomViewLayout(bottomView,topBottom,bottomBottom,(int)animation.getAnimatedValue(),mInitialBottomViewHeight);
//
//                    preAnimationValue = preAnimationValue - (int)animation.getAnimatedValue();
//                    if(mDampLoadMoreListenerInChild!=null){
//                        mDampLoadMoreListenerInChild.getScrollChanged(preAnimationValue,getBottom()-middleView.getBottom());
//
//                    }
//                    if(mDampLoadMoreListeners!=null){
//                        for(DampLoadMoreListener dampLoadMoreListener:mDampLoadMoreListeners){
//                            dampLoadMoreListener.getScrollChanged(preAnimationValue,getBottom()-middleView.getBottom());
//                        }
//                    }
//                    preAnimationValue = (int)animation.getAnimatedValue();
//
//                    if((int)animation.getAnimatedValue() == lastValue){
//                        if(mDampLoadMoreListenerInChild!=null){
//                            mDampLoadMoreListenerInChild.stopLoadMore();
//                        }
//                        mAapter.notifyDataSetChanged();
//                        isAnimationPlay = false;
//                    }
//                }
//            });
//            animator.start();
//            isAnimationPlay = true;
            middleView.layout(middleView.getLeft(), getTop(), middleView.getRight(),getBottom());
            bottomView.layout(bottomView.getLeft(), getBottom(), bottomView.getRight(), getBottom() + mInitialBottomViewHeight);

            isUpglide = UPGLIDE_PRE;
            isLoadMoreState = LOAD_MORE_PRE;

            if (mDampLoadMoreListenerInChild != null) {
                mDampLoadMoreListenerInChild.onComplete();
            }
            middleView.scrollBy(0, mChangedMiddleHeight);

            mChangedMiddleHeight = 0;
        }
    }

    public void loadOver() {
        if (isUpglide == UPGLIDE_ING) {
            resetState();
            loadOver();
        } else {
            startDampMiddleAndBottomAnimationOnLoadOver();
            mChangedMiddleHeight = 0;
            isLoadMoreState = LOAD_MORE_OVER;
        }
    }

    /**
     * @param duration 动画播放时长
     */
    @Override
    public void setAnimationDuration(int duration) {
        this.animationDuration = duration;
    }

    /**
     * @param value 阻尼最大时middleView顶部到容器顶部的距离
     */
    @Override
    public void setPullDownDampDistance(int value) {
        this.maxTopDampValue = (float) dp2px(mContext, value);
        isModidyMaxTopValue = true;
    }

    /**
     * @param value 阻尼最大时middleView底部到容器底部的距离
     */
    @Override
    public void setUpGlideDampDistance(int value) {
        this.maxBottomDampValue = (float) dp2px(mContext, value);
        isModidyMaxBottomValue = true;
    }

    /**
     * @param pullDownDampValue 下拉时最大阻尼系数
     *                          可选范围在0f~100f之间
     */
    @Override
    public void setPullDownDampValue(float pullDownDampValue) {
        if (pullDownDampValue > 100f) {
            this.mPullDownDampValue = 100f;
        } else if (pullDownDampValue < 0f) {
            this.mPullDownDampValue = 0f;
        } else {
            this.mPullDownDampValue = pullDownDampValue;
        }
    }

    /**
     * @param upGlideDampValue bottomView最大阻尼系数
     *                         可选范围在0f~100f之间
     */
    @Override
    public void setUpGlideDampValue(float upGlideDampValue) {
        if (upGlideDampValue > 100f) {
            this.mUpGlideDampValue = 100f;
        } else if (upGlideDampValue < 0f) {
            this.mUpGlideDampValue = 0f;
        } else {
            this.mUpGlideDampValue = upGlideDampValue;
        }
    }
}
