package com.jzycc.layout.damplayoutlibrary.layout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jzycc.layout.damplayoutlibrary.bottomview.DampBottomViewChild;
import com.jzycc.layout.damplayoutlibrary.bottomview.DampBottomViewListener;
import com.jzycc.layout.damplayoutlibrary.layout.decoration.GroupItemDecoration;
import com.jzycc.layout.damplayoutlibrary.topview.DampTopViewChild;
import com.jzycc.layout.damplayoutlibrary.topview.DampTopViewListener;

import java.util.ArrayList;
import java.util.List;

/**
 * author Jzy(Xiaohuntun)
 * date 18-9-4
 *
 * DampRefreshAndLoadMoreLayout 是一个可以为列表自由配置刷新和加载以及更多可以扩展的功能的容器，并带有可越界阻尼拉动且会回弹的功能。
 *
 * 刷新和加载可以通过提供的接口来自定义。
 * 自定义刷新接口{@link DampTopViewListener} 默认刷新view {@link DampTopViewChild}
 * 自定义加载接口{@link DampBottomViewListener} 默认加载view {@link DampBottomViewChild}
 * 可以通过参考默认的刷新加载view来自定义你的刷新加载, 并且欢迎提供新的刷新和加载 view 加进 DampRefreshAndLoadMoreLayout 的材料库中。
 *
 * 具体使用可以访问gitlab: https://github.com/JzyCc/DampRefreshAndLoadMoreLayout
 * 正在不断完善，使用过程中如有发现bug或者疑问，邮箱 mJzyCc@aliyun.com
 *
 * 使用注意事项：
 * 1. DampRefreshAndLoadMoreLayout 容器中应当且只能含有一个childView(刷新和加载所需的childView容器自行处理，不包含在内)。
 * 2. DampRefreshAndLoadMoreLayout 中的child 建议将高宽设置为 match_parent,不然可能会有不可预料的事情发生, 如需固定高宽应为 DampRefreshAndLoadMoreLayout 设置。
 * 3. 使用 builder() 方法来配置本容器时需在配置链前端调用 attachLayout(DampRefreshAndLoadMoreLayout) 方法，
 *    为容器传入已经存在的 DampRefreshAndLoadMoreLayout 实例, 接下来的build将会为传入的实例进行配置。
 *    使用 builder(Context) 方法则会生成一个实例，无需调用 attachLayout(DampRefreshAndLoadMoreLayout) 方法。
 * 4. loadOver()方法会把容器状态设置为 全部加载完成， 如果此时使用非容器刷新功能刷新列表，请调用 continueLoad(boolean type) 方法重置加载状态，参数应为true。
 *    如果是使用容器带有的刷新功能则无需调用此方法。
 * 5. 容器触发加载和刷新时，无论是否成功，请及时调用 stopLoadMoreAnimation() 结束加载动画以及调用 stopRefreshAnimation()结束刷新动画。
 * 6. 请确保刷新或者加载成功后，在与Adapter绑定的数据源填充完后再调用 stopLoadMoreAnimation() 和 stopRefreshAnimation() 方法，
 *    及时通知列表刷新是良好习惯，如果不通知列表刷新，可能会有未知的问题发生，通知刷新的操作也应在调用停止动画方法前执行。
 */
public class DampRefreshAndLoadMoreLayout extends LinearLayout implements DampRefreshAndLoadMoreLayoutService{
    private static final String TAG = "DampLayout";

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
     * 加载相关操作前的状态
     */
    public static final int LOAD_MORE_PRE = 0;

    /**
     * 加载中 用于判断是否转交事件
     */
    public static final int LOAD_MORE_ING = 1;

    /**
     * 所有数据加载完成
     */
    public static final int LOAD_MORE_OVER = 2;

    /**
     * 加载中
     */
    public static final int LOAD_MORE_ING_II = 3;

    /**
     * 此时middleView处理事件
     */
    public static final int DAMP_NONE = 0;

    /**
     * 顶部可以下拉
     */
    public static final int DAMP_TOP = 1;

    /**
     * 底部可以上拉
     */
    public static final int DAMP_BOTTOM = -1;

    /**
     * 下拉前
     */
    public static final int PULL_DOWN_PRE = 0;

    /**
     * 下拉中
     */
    public static final int PULL_DOWN_ING = 1;

    /**
     * 下拉完成
     */
    public static final int PULL_DOWN_COMPLETE = 2;

    /**
     * 上滑前
     */
    public static final int UPGLIDE_PRE = 0;

    /**
     * 上滑中
     */
    public static final int UPGLIDE_ING = 1;

    /**
     * 上滑完成
     */
    public static final int UPGLIDE_COMPLETE = 2;

    private Context mContext;

    /**
     * 记录当前刷新状态
     */
    private int isRefreshState = 0;

    /**
     * 记录当前加载状态
     */
    private int isLoadMoreState = 0;

    /**
     * 手指仍在滑动时判断是否应该将事件交给middleView;
     */
    private boolean isShouldScrollMiddleView = false;

    /**
     * 当前拖动状态
     */
    private int isDampTopOrBottom = DAMP_NONE;

    /**
     * 当前下拉状态
     */
    private int isPullDownState = 0;

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
     * 底部上滑时阻尼值最大时的距离
     */
    private float maxBottomDampValue = 200f;


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

        void onRefresh();
    }

    /**
     * 用于存放监听容器刷新状态的监听器的list
     */
    private List<DampRefreshListener> mDampRefreshListeners;

    /**
     * topView必须要实现的监听，用于监听容器状态来做加载相关的操作
     */
    private DampBottomViewListener mDampLoadMoreListenerInChild;

    /**
     * 这个接口用于提供给外部监听当前容器是否触发加载
     */
    public interface DampLoadMoreListener {
        void onScrollChanged(int dy, int bottomViewPosition);

        void onLoadMore();
    }

    /**
     * 用于存放监听容器加载状态的监听器的list
     */
    private List<DampLoadMoreListener> mDampLoadMoreListeners;

    private List<DampLayoutScrollChangedListener> mDampLayoutScrollChangedListeners;

    /**
     * 加载到bottomView完全显示的动画
     */
    private ValueAnimator loadAnimator = new ValueAnimator();

    /**
     *
     */
    private ValueAnimator loadOverAnimator = new ValueAnimator();

    private ValueAnimator defaultTopSpringbackAnimator = new ValueAnimator();

    private ValueAnimator defaultBottomSpringbackAnimator = new ValueAnimator();

    /**
     * 当前追踪的手指Id
     */
    private int mScrollPointerId = 0;

    private float mPullDownDampValue = 20f;

    private float mUpGlideDampValue = 20f;

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

    /**
     * 用于构造DampRefreshAndLoadMoreLayout, 无参数时不会new出新的 {@link DampRefreshAndLoadMoreLayout} 实例，请使用
     * {@link DampRefreshAndLoadMoreLayout.Builder#attachLayout(DampRefreshAndLoadMoreLayout)}方法传进已经生成的实例。
     * @return 返回一个Builder用于构造传进的 {@link DampRefreshAndLoadMoreLayout} 实例
     */
    public static Builder builder(){
        return new Builder();
    }

    /**
     * 用于构造DampRefreshAndLoadMoreLayout, 传入{@link Context} 会new出新的{@link DampRefreshAndLoadMoreLayout}实例，
     * 无需使用{@link DampRefreshAndLoadMoreLayout.Builder#attachLayout(DampRefreshAndLoadMoreLayout)}方法传进已经生成的实例。
     * @param context 这个context用于生成新的DampRefreshAndLoadMoreLayout实例
     * @return 返回一个Builder用于构造新的DampRefreshAndLoadMoreLayout实例
     */
    public static Builder builder(Context context){
        return new Builder(context);
    }

    public static class Builder implements DampRefreshAndLoadMoreLayoutBuilderService{

        private DampRefreshAndLoadMoreLayout dampRefreshAndLoadMoreLayout;

        public Builder(Context context) {
            dampRefreshAndLoadMoreLayout = new DampRefreshAndLoadMoreLayout(context);
        }

        public Builder(){

        }

        /**
         * @param dampRefreshAndLoadMoreLayout 需要传入实例,接下来将会为传入的实例进行配置
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder attachLayout(DampRefreshAndLoadMoreLayout dampRefreshAndLoadMoreLayout){
            this.dampRefreshAndLoadMoreLayout = dampRefreshAndLoadMoreLayout;
            return this;
        }

        /**
         * 调用此方法后，容器将添加默认的刷新头部并带有刷新功能，
         * 请为容器添加刷新监听{@link DampRefreshAndLoadMoreLayout.Builder#addOnDampRefreshListener(DampRefreshListener)}
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder openRefresh() {
            dampRefreshAndLoadMoreLayout.openRefresh();
            return this;
        }


        /**
         * 调用此方法后，容器将添加传入的刷新头部并带有刷新功能，
         * 请为容器添加刷新监听{@link DampRefreshAndLoadMoreLayout.Builder#addOnDampRefreshListener(DampRefreshListener)}
         * @param view 需要传入的自定义刷新头部
         * @param viewHeight 需要传入的自定义刷新头部的高度（高度的取舍将交由使用者来决定，取舍为是否将刷新头部的高度固定和是否使用{@link View#post(Runnable)}方法获取高度）
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder openRefresh(View view, int viewHeight) {
            dampRefreshAndLoadMoreLayout.openRefresh(view,viewHeight);
            return this;
        }

        /**
         * 调用此方法后，容器将添加默认的加载底部并带有加载更多功能，
         * 请为容器添加加载监听{@link DampRefreshAndLoadMoreLayout.Builder#addOnDampLoadMoreListener(DampLoadMoreListener)}
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder openLoadMore() {
            dampRefreshAndLoadMoreLayout.openLoadMore();
            return this;
        }

        /**
         * 调用此方法后，容器将添加传入的加载底部并带有加载功能，
         * 请为容器添加加载监听{@link DampRefreshAndLoadMoreLayout.Builder#addOnDampLoadMoreListener(DampLoadMoreListener)}
         * @param view 需要传入的自定义加载底部
         * @param viewHeight 需要传入的自定义加载底部的高度（高度的取舍将交由使用者来决定，取舍为是否将加载底部的高度固定和是否使用{@link View#post(Runnable)}方法获取高度）
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder openLoadMore(View view, int viewHeight) {
            dampRefreshAndLoadMoreLayout.openLoadMore(view,viewHeight);
            return this;
        }

        /**
         * @param dampRefreshListener 添加刷新相关监听 {@link DampRefreshListener}
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder addOnDampRefreshListener(DampRefreshListener dampRefreshListener) {
            dampRefreshAndLoadMoreLayout.addOnDampRefreshListener(dampRefreshListener);
            return this;
        }

        /**
         * @param dampLoadMoreListener 添加loadmore相关监听 {@link DampLoadMoreListener}
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder addOnDampLoadMoreListener(DampLoadMoreListener dampLoadMoreListener) {
            dampRefreshAndLoadMoreLayout.addOnDampLoadMoreListener(dampLoadMoreListener);
            return this;
        }

        /**
         * @param duration 动画播放时长
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder setAnimationDuration(int duration) {
            dampRefreshAndLoadMoreLayout.setAnimationDuration(duration);
            return this;
        }

        /**
         * @param value 阻尼最大时middleView顶部到容器顶部的距离
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder setPullDownDampDistance(int value) {
            dampRefreshAndLoadMoreLayout.setPullDownDampDistance(value);
            return this;
        }

        /**
         * @param value 阻尼最大时middleView底部到容器底部的距离
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder setUpGlideDampDistance(int value) {
            dampRefreshAndLoadMoreLayout.setUpGlideDampDistance(value);
            return this;
        }

        /**
         * @param pullDownDampValue 下拉时最大阻尼系数 可选范围在0f~100f之间
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder setPullDownDampValue(float pullDownDampValue) {
            dampRefreshAndLoadMoreLayout.setPullDownDampValue(pullDownDampValue);
            return this;
        }

        /**
         * @param upGlideDampValue 上拉最大阻尼系数 可选范围在0f~100f之间
         * @return {@link DampRefreshAndLoadMoreLayout.Builder}
         */
        @Override
        public Builder setUpGlideDampValue(float upGlideDampValue) {
            dampRefreshAndLoadMoreLayout.setUpGlideDampValue(upGlideDampValue);
            return this;
        }

        /**
         * @param groupDecoration 需要传入{@link GroupItemDecoration}实例
         * 如果容器内的View是RecyclerView的话，调用此方法可以为recyclerView添加{@link GroupItemDecoration}
         */
        @Override
        public Builder setGroupDecoration(GroupItemDecoration groupDecoration) {
            dampRefreshAndLoadMoreLayout.setGroupDecoration(groupDecoration);
            return this;
        }

        /**
         * 调用此方法，返回配置好的{@link DampRefreshAndLoadMoreLayout}
         * @return {@link DampRefreshAndLoadMoreLayout}
         */
        @Override
        public DampRefreshAndLoadMoreLayout build() {
            return dampRefreshAndLoadMoreLayout;
        }
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
        initAnimation();

    }
    private void initAnimation(){
        loadAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimationPlay = false;
                if(isLoadMoreState == LOAD_MORE_OVER){
                    startDampMiddleAndBottomAnimationOnLoadOver();
                }
            }
        });
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
                        }else  {
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
                            if (isLoadMoreState == LOAD_MORE_PRE) {
                                if (mDampLoadMoreListenerInChild != null) {
                                    mDampLoadMoreListenerInChild.onLoadMore();
                                }
                                if (mDampLoadMoreListeners != null) {
                                    for (DampLoadMoreListener dampLoadMoreListener : mDampLoadMoreListeners) {
                                        dampLoadMoreListener.onLoadMore();
                                    }
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

                                notifyDampTopViewListenerScrollChanged((int) (offsetY * measureDampTopValue(mChangedTopViewMarginTop) + 0.5f), mChangedTopViewMarginTop);

                                notifyDampLayoutScrollChangedListenerPullDown((int) (offsetY * measureDampTopValue(mChangedTopViewMarginTop) + 0.5f), mChangedTopViewMarginTop);

                                notifyDampRefreshListenerScrollChanged((int) (offsetY * measureDampTopValue(mChangedTopViewMarginTop) + 0.5f), mChangedTopViewMarginTop);
                            }
                        } else {
                            //如果topView为空则只实现回弹效果
                            if (offsetY < 0) {//判断当前是否是顶部可拉动状态
                                isPullDownState = PULL_DOWN_ING;//复原下拉状态
                            }
                            if (isPullDownState == PULL_DOWN_ING) {
                                float nowOffsetY;
                                if (offsetY > 0) {
                                    nowOffsetY = offsetY;
                                    nowOffsetY += 0.5f;
                                    setMiddleViewLayoutForPullDown(middleView, middleView.getTop(), middleView.getBottom(), -offsetY);
                                    mChangedMiddleHeight += nowOffsetY;
                                } else {
                                    nowOffsetY = offsetY * measureDampMiddleValueForPullDown(mChangedMiddleHeight);
                                    nowOffsetY += 0.5f;
                                    setMiddleViewLayoutForPullDown(middleView, middleView.getTop(), middleView.getBottom(), -(int) nowOffsetY);
                                    mChangedMiddleHeight += (int) nowOffsetY;
                                }

                                notifyDampLayoutScrollChangedListenerPullDown((int) nowOffsetY, -mChangedMiddleHeight);
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

                                    if (mDampLoadMoreListenerInChild != null) {
                                        mDampLoadMoreListenerInChild.onScrollChanged((int) nowOffsetY, mChangedMiddleHeight);
                                    }

                                    notifyDampLayoutScrollChangedListenerUpGlide((int) nowOffsetY, mChangedMiddleHeight);

                                    if (mDampLoadMoreListeners != null) {
                                        for (DampLoadMoreListener dampLoadMoreListener : mDampLoadMoreListeners) {
                                            dampLoadMoreListener.onScrollChanged((int) nowOffsetY, mChangedMiddleHeight);
                                        }
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

                                    notifyDampBottomViewListenerScrollChanged((int) nowOffsetY, mChangedMiddleHeight);

                                    notifyDampLayoutScrollChangedListenerUpGlide((int) nowOffsetY, mChangedMiddleHeight);

                                    notifyDampLoadMoreListenerScrollChanged((int) nowOffsetY, mChangedMiddleHeight);
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

                                notifyDampLayoutScrollChangedListenerUpGlide((int) nowOffsetY, mChangedMiddleHeight);

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
                                    mDampRefreshListenerInChild.onRefresh();
                                }
                                if (mDampRefreshListeners != null) {
                                    for (DampRefreshListener dampRefreshListener : mDampRefreshListeners) {
                                        dampRefreshListener.onRefresh();
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
                                    mDampRefreshListenerInChild.onRefresh();
                                }

                                if (mDampRefreshListeners != null) {
                                    for (DampRefreshListener dampRefreshListener : mDampRefreshListeners) {
                                        dampRefreshListener.onRefresh();
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

    private ValueAnimator toHomeAnimation = new ValueAnimator();
    /**
     * 顶部完全回弹时的动画
     */
    private void startDampTopToHomeAnimation() {
        preAnimationValue = mChangedTopViewMarginTop;
        toHomeAnimation.setIntValues(mChangedTopViewMarginTop, mInitialTopViewMarginTop);
        toHomeAnimation.setDuration(animationDuration);
        toHomeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setTopMarigin(topView, topViewMarginParams, (int) animation.getAnimatedValue(), mInitialTopViewMarginTop);
                preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();

                notifyDampTopViewListenerScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());

                notifyDampLayoutScrollChangedListenerPullDown(preAnimationValue, (int) animation.getAnimatedValue());

                notifyDampRefreshListenerScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());

                preAnimationValue = (int) animation.getAnimatedValue();
                if (mInitialTopViewMarginTop == (int) animation.getAnimatedValue()) {
                    isAnimationPlay = false;
                }
            }
        });
        toHomeAnimation.start();
        isAnimationPlay = true;
    }

    private ValueAnimator toRefreshAnimation = new ValueAnimator();
    /**
     * 回弹到刷新位置的动画PRE
     */
    private void startDampTopToRefreshAnimation() {
        preAnimationValue = mChangedTopViewMarginTop;
        toRefreshAnimation.setIntValues(mChangedTopViewMarginTop, 0);
        toRefreshAnimation.setDuration(animationDuration);
        toRefreshAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                setTopMarigin(topView, topViewMarginParams, (int) animation.getAnimatedValue(), mInitialTopViewMarginTop);
                preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();

                notifyDampTopViewListenerScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());

                notifyDampLayoutScrollChangedListenerPullDown(preAnimationValue, (int) animation.getAnimatedValue());

                notifyDampRefreshListenerScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());

                preAnimationValue = (int) animation.getAnimatedValue();
                if ((int) animation.getAnimatedValue() == 0) {
                    isAnimationPlay = false;
                }
            }
        });
        toRefreshAnimation.start();
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
        float dampTopValue;
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

        loadAnimator.setIntValues(0, mChangedMiddleHeight - mInitialBottomViewHeight);
        loadAnimator.setDuration(animationDuration);
        loadAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                middleView.layout(middleView.getLeft(), topMiddle + (int) animation.getAnimatedValue(), middleView.getRight(), bottomMiddle + (int) animation.getAnimatedValue());
                bottomView.layout(bottomView.getLeft(), topBottom + (int) animation.getAnimatedValue(), bottomView.getRight(), bottomBottom + (int) animation.getAnimatedValue());
                preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();

                mChangedScrollByAnimation = lastValue - (int) animation.getAnimatedValue();

                notifyDampBottomViewListenerScrollChanged(preAnimationValue, getBottom() - middleView.getBottom());

                notifyDampLayoutScrollChangedListenerUpGlide(preAnimationValue, getBottom() - middleView.getBottom());

                notifyDampLoadMoreListenerScrollChanged(preAnimationValue, getBottom() - middleView.getBottom());

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
        if(mChangedMiddleHeight != 0){
            //用于判断是否预先知道数据已经加载完毕，并提前调用 loadOver() 方法
            loadOverAnimator.setIntValues(0, mChangedMiddleHeight);
            loadOverAnimator.setDuration(animationDuration);
            loadOverAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    setMiddleViewLayout(middleView, topMiddle, bottomMiddle, (int) animation.getAnimatedValue());
                    setBottomViewLayout(bottomView, topBottom, bottomBottom, (int) animation.getAnimatedValue(), mInitialBottomViewHeight);

                    preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();

                    notifyDampBottomViewListenerScrollChanged(preAnimationValue, getBottom() - middleView.getBottom());

                    notifyDampLayoutScrollChangedListenerUpGlide(preAnimationValue, getBottom() - middleView.getBottom());

                    notifyDampLoadMoreListenerScrollChanged(preAnimationValue, getBottom() - middleView.getBottom());

                    preAnimationValue = (int) animation.getAnimatedValue();

                    if ((int) animation.getAnimatedValue() == lastValue) {
                        isAnimationPlay = false;
                        if (mDampLoadMoreListenerInChild != null) {
                            mDampLoadMoreListenerInChild.onLoaded();
                        }
                    }
                }
            });
            loadOverAnimator.start();
            isAnimationPlay = true;
        }else {
            notifyDampBottomViewListenerScrollChanged(mChangedMiddleHeight, 0);

            notifyDampLayoutScrollChangedListenerUpGlide(mChangedMiddleHeight, 0);

            notifyDampLoadMoreListenerScrollChanged(mChangedMiddleHeight, 0);

            if (mDampLoadMoreListenerInChild != null) {
                mDampLoadMoreListenerInChild.onLoaded();
            }
        }
        mChangedMiddleHeight = 0;
    }

    /**
     * 没有bottimView时的回弹动画
     */
    private void stratDampMiddleForUpGlide() {
        final int topMiddle = middleView.getTop();
        final int bottomMiddle = middleView.getBottom();
        final int lastValue = mChangedMiddleHeight;
        defaultBottomSpringbackAnimator.setIntValues(0, mChangedMiddleHeight);
        defaultBottomSpringbackAnimator.setDuration(animationDuration);
        defaultBottomSpringbackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setMiddleViewLayout(middleView, topMiddle, bottomMiddle, (int) animation.getAnimatedValue());
                preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();
                notifyDampLayoutScrollChangedListenerUpGlide(preAnimationValue, getBottom() - middleView.getBottom());
                preAnimationValue = (int) animation.getAnimatedValue();
                if ((int) animation.getAnimatedValue() == lastValue) {
                    isAnimationPlay = false;
                }
            }
        });
        defaultBottomSpringbackAnimator.start();
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
                preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();
                notifyDampLayoutScrollChangedListenerPullDown(preAnimationValue, middleView.getTop());
                preAnimationValue = (int) animation.getAnimatedValue();
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
        if (getBottom() - top - changedValue > 0) {
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
    public void openRefresh() {
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
    public void openRefresh(View view, int viewHeight) {
        if (topView == null) {
            topView = view;
            try {
                mDampRefreshListenerInChild = (DampTopViewListener) topView;
                mTopViewHeight = viewHeight;
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
    public void openLoadMore() {
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
    public void openLoadMore(View view, int viewHeight) {
        if (bottomView == null) {
            this.bottomView = view;
            try {
                mDampLoadMoreListenerInChild = (DampBottomViewListener) bottomView;
                    mBottomViewHeight = viewHeight;
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
        if(mDampRefreshListeners == null){
            mDampRefreshListeners = new ArrayList<>();
        }
        if (dampRefreshListener != null) {
            mDampRefreshListeners.add(dampRefreshListener);
        }
    }

    /**
     * @param dampLoadMoreListener 添加loadmore相关监听
     */
    @Override
    public void addOnDampLoadMoreListener(DampLoadMoreListener dampLoadMoreListener) {
        if(mDampLoadMoreListeners == null){
            mDampLoadMoreListeners = new ArrayList<>();
        }
        if (dampLoadMoreListener != null) {
            mDampLoadMoreListeners.add(dampLoadMoreListener);
        }
    }

    private ValueAnimator refreshStopAnimation = new ValueAnimator();

    /**
     * 停止刷新
     */
    public void stopRefreshAnimation() {
        if (isRefreshState == REFRESH_ING && topView != null) {
            preAnimationValue = mChangedTopViewMarginTop;
            refreshStopAnimation.setIntValues(mChangedTopViewMarginTop, mInitialTopViewMarginTop);
            refreshStopAnimation.setDuration(animationDuration);
            refreshStopAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setTopMarigin(topView, topViewMarginParams, (int) animation.getAnimatedValue(), mInitialTopViewMarginTop);

                    preAnimationValue = preAnimationValue - (int) animation.getAnimatedValue();

                    notifyDampTopViewListenerScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());

                    notifyDampLayoutScrollChangedListenerPullDown(preAnimationValue, (int) animation.getAnimatedValue());

                    notifyDampRefreshListenerScrollChanged(preAnimationValue, (int) animation.getAnimatedValue());

                    preAnimationValue = (int) animation.getAnimatedValue();

                    if ((int) animation.getAnimatedValue() == mInitialTopViewMarginTop) {
                        if (mDampRefreshListenerInChild != null) {
                            mDampRefreshListenerInChild.onStart();
                        }
                        isAnimationPlay = false;
                    }
                }
            });
            refreshStopAnimation.start();
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

    private int mChangedScrollByAnimation = 0;

    public void stopLoadMoreAnimation() {
        if(bottomView != null){
            if (isAnimationPlay) {
                if (loadAnimator != null) {
                    loadAnimator.cancel();
                }
                isAnimationPlay = false;
                stopLoadMoreAnimation();
            } else if (isUpglide == UPGLIDE_ING || isDampTopOrBottom == DAMP_BOTTOM) {
                resetState();
                stopLoadMoreAnimation();
                isShouldScrollMiddleView = true;
            } else {
                notifyDampBottomViewListenerScrollChanged(mChangedMiddleHeight,0);
                notifyDampLayoutScrollChangedListenerUpGlide(mChangedMiddleHeight,0);
                notifyDampLoadMoreListenerScrollChanged(mChangedMiddleHeight,0);


                middleView.layout(middleView.getLeft(), getTop(), middleView.getRight(),getBottom());
                bottomView.layout(bottomView.getLeft(), getBottom(), bottomView.getRight(), getBottom() + mInitialBottomViewHeight);

                isUpglide = UPGLIDE_PRE;
                isLoadMoreState = LOAD_MORE_PRE;

                if (mDampLoadMoreListenerInChild != null) {
                    mDampLoadMoreListenerInChild.onComplete();
                }

                middleView.scrollBy(0, mChangedMiddleHeight + mChangedScrollByAnimation);

                mChangedScrollByAnimation = 0;

                mChangedMiddleHeight = 0;
            }
        }
    }

    private void notifyDampTopViewListenerScrollChanged(int dy, int topViewPosition){
        if (mDampRefreshListenerInChild != null) {
            mDampRefreshListenerInChild.onScrollChanged(dy, topViewPosition);
        }
    }

    private void notifyDampBottomViewListenerScrollChanged(int dy, int bottomViewPosition){
        if (mDampLoadMoreListenerInChild != null) {
            mDampLoadMoreListenerInChild.onScrollChanged(dy, bottomViewPosition);
        }
    }

    private void notifyDampLayoutScrollChangedListenerPullDown(int dy, int topViewPosition){
        if (mDampLayoutScrollChangedListeners != null) {
            for (DampLayoutScrollChangedListener dampLayoutScrollChangedListener : mDampLayoutScrollChangedListeners) {
                dampLayoutScrollChangedListener.onPullDownScrollChanged(dy, topViewPosition);
            }
        }
    }

    private void notifyDampLayoutScrollChangedListenerUpGlide(int dy, int bottomViewPosition){
        if (mDampLayoutScrollChangedListeners != null) {
            for (DampLayoutScrollChangedListener dampLayoutScrollChangedListener : mDampLayoutScrollChangedListeners) {
                dampLayoutScrollChangedListener.onUpGlideScrollChanged(dy, bottomViewPosition);
            }
        }
    }

    private void notifyDampRefreshListenerScrollChanged(int dy, int topViewPosition){
        if (mDampRefreshListeners != null) {
            for (DampRefreshListener dampRefreshListener : mDampRefreshListeners) {
                dampRefreshListener.onScrollChanged(dy, topViewPosition);
            }
        }
    }

    private void notifyDampLoadMoreListenerScrollChanged(int dy, int bottomViewPosition){
        if (mDampLoadMoreListeners != null) {
            for (DampLoadMoreListener dampLoadMoreListener : mDampLoadMoreListeners) {
                dampLoadMoreListener.onScrollChanged(dy, bottomViewPosition);
            }
        }
    }

    /**
     * 1.loadMore动画播放时使用
     * 2.容器还被拖动时使用
     * 3.loadMore动画结束时使用
     */
    public void loadOver() {
        resetState();
        if(loadAnimator.isRunning()) {
            isLoadMoreState = LOAD_MORE_OVER;
        }else{
            startDampMiddleAndBottomAnimationOnLoadOver();
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
    }

    /**
     * @param value 阻尼最大时middleView底部到容器底部的距离
     */
    @Override
    public void setUpGlideDampDistance(int value) {
        this.maxBottomDampValue = (float) dp2px(mContext, value);
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

    /**
     * @param groupDecoration 需要传入{@link GroupItemDecoration}实例
     * 使用此方法的前提是容器内的child是RecyclerView
     */
    @Override
    public void setGroupDecoration(GroupItemDecoration groupDecoration){
        try {
            RecyclerView rvView = (RecyclerView)middleView;
            groupDecoration.setParent(rvView);
            rvView.addItemDecoration(groupDecoration);
            addDampLayoutScrollChangedListener(groupDecoration);
        }catch (ClassCastException e){
            Log.e(TAG, "setGroupDecoration: ", e);
        }
    }

    private void addDampLayoutScrollChangedListener(DampLayoutScrollChangedListener dampLayoutScrollChangedListener){
        if(mDampLayoutScrollChangedListeners == null){
            mDampLayoutScrollChangedListeners = new ArrayList<>();
        }
        if(dampLayoutScrollChangedListener != null){
            mDampLayoutScrollChangedListeners.add(dampLayoutScrollChangedListener);
        }
    }

    /**
     * @param type 用于判断是否可以继续加载
     */
    public void continueLoad(boolean type){
        if(type){
            isLoadMoreState = LOAD_MORE_PRE;
        }else {
            loadOver();
        }
    }
}
