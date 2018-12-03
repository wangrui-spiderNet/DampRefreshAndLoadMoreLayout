package com.jzycc.layout.damplayoutlibrary.layout.decoration;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.jzycc.layout.damplayoutlibrary.layout.DampLayoutScrollChangedListener;
import com.jzycc.layout.damplayoutlibrary.layout.DampRefreshAndLoadMoreLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author yuzhouxu
 * date 2018/11/12.
 * <p>
 * GroupItemDecroation可以自定义RecyclerView分组，并带有黏性效果，保证当前group的titlebar会被黏在容器的最顶部，
 * 可以单独使用，也已经适配{@link DampRefreshAndLoadMoreLayout}。
 */

public class GroupItemDecoration extends RecyclerView.ItemDecoration implements DampLayoutScrollChangedListener {
    private static final String TAG = "GroupItemDecoration";

    private View groupView;
    private Map<Object, GroupView> mGroupViews;
    private View mNewGroupView;
    private GroupDecorationCallBack mDecorationCallback;

    private List<GroupItem> groupList; //用户设置的分组列表
    private Map<Object, GroupItem> groups = new HashMap<>();//保存startPosition与分组对象的对应关系
    private List<Integer> groupPositions = new ArrayList<>();//保存分组startPosition的数组
    private int positionIndex;//分组对应的startPosition在groupPositions中的索引
    private boolean isStickyHeader = true;//是否粘性头部
    private int groupViewHeight = 0;
    private int indexCache = -1;
    private boolean isUpGlide = false;
    private int mBottomViewPosition;
    private int mNowStickyGroupViewPositionIndex;
    private int preGroupViewHeight;
    public static final String KEY_RECT = "isRectClick4GroupView";//这个值随便设，只要不容易和用户设置的撞车就行
    private RecyclerView mParent;

    public GroupItemDecoration(Context context, GroupDecorationCallBack decorationCallback) {
        this.mDecorationCallback = decorationCallback;
        this.mGroupViews = new HashMap<>();
        this.groupList = new ArrayList<>();
    }

    public static class GroupView {
        private View groupView;
        private Integer groupViewHeight;

        public GroupView(View groupView) {
            this.groupView = groupView;
        }

        public View getGroupView() {
            return groupView;
        }

        public void setGroupView(View groupView) {
            this.groupView = groupView;
        }

        public Integer getGroupViewHeight() {
            return groupViewHeight;
        }

        public void setGroupViewHeight(Integer groupViewHeight) {
            this.groupViewHeight = groupViewHeight;
        }
    }

    /**
     * 开关粘性头部
     *
     * @param isStickyHeader
     */
    public void setStickyHeader(boolean isStickyHeader) {
        this.isStickyHeader = isStickyHeader;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (!isLinearAndVertical(parent)) {//若RecyclerView类型不是LinearLayoutManager.VERTICAL，跳出（下同）
            return;
        }

        int position = parent.getChildAdapterPosition(view);
        if (groups.get(position) != null) {
            if (mGroupViews.get(position).getGroupViewHeight() == null) {
                measureView(mGroupViews.get(position).getGroupView(), parent, position);
            }
            outRect.top = mGroupViews.get(position).getGroupViewHeight();//若RecyclerView中该position对应的childView之前需要绘制groupView，则为其预留空间
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (groupList.size() == 0 || !isLinearAndVertical(parent)) {
            return;
        }

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            float left = child.getLeft();
            float top = child.getTop();
            float right = child.getRight();

            int position = parent.getChildAdapterPosition(child);
            if (groups.get(position) != null) {
                groupView = mGroupViews.get(position).getGroupView();
                if (mGroupViews.get(position).getGroupViewHeight() == null) {
                    measureView(groupView, parent, position);
                }
                Rect rect = new Rect((int) left, (int) (top - mGroupViews.get(position).getGroupViewHeight()), (int) right, (int) top);
                groups.get(position).setData(KEY_RECT, rect);//用于判断点击范围
                c.save();
                c.translate(left, top - mGroupViews.get(position).getGroupViewHeight());//将画布起点移动到之前预留空间的左上角
                groupView.draw(c);
                c.restore();
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        if (groupList.size() == 0 || !isStickyHeader || !isLinearAndVertical(parent)) {
            return;
        }
        int childCount = parent.getChildCount();
        Map<Object, Object> map = new HashMap<>();

        //遍历当前可见的childView，找到当前组和下一组并保存其position索引和GroupView的top位置
        boolean isFirst = true;
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            float top = child.getTop();
            int position = parent.getChildAdapterPosition(child);
            if (groups.get(position) != null) {
                positionIndex = searchGroupIndex(groupPositions, position);
                if(positionIndex < 0){
                    Log.e(TAG, "onDrawOver: "+"can't find group position by the index");
                    return;
                }
                if (map.get("cur") == null) {
                    map.put("cur", positionIndex);
                    map.put("curTop", top);
                    map.put("curPosition", position);
                } else {
                    if (map.get("next") == null) {
                        map.put("next", positionIndex);
                        map.put("nextTop", top);
                        map.put("nextPosition", position);
                    }
                }

                if (isFirst && positionIndex >= 0) {
                    if (i == 0) {
                        mNowStickyGroupViewPositionIndex = positionIndex;
                    } else if (positionIndex > 0) {
                        mNowStickyGroupViewPositionIndex = positionIndex - 1;
                    }
                    //缓存当前第一个屏幕可见的groupview
                    groupView = mGroupViews.get(groupPositions.get(mNowStickyGroupViewPositionIndex)).getGroupView();
                    //groupViewHeight = mGroupViews.get(groupPositions.get(mNowStickyGroupViewPositionIndex)).getGroupViewHeight();
                    isFirst = false;
                }
            }
        }

        c.save();
        if (map.get("cur") != null) {//如果当前组不为空，说明RecyclerView可见部分至少有一个GroupView
            indexCache = (int) map.get("cur");
            float curTop = (float) map.get("curTop");
            float nextTop = 0f;

            if (indexCache >= 0 && indexCache < groupPositions.size()) {
                groupViewHeight = mGroupViews.get(groupPositions.get(indexCache)).getGroupViewHeight();
            }

            if (indexCache - 1 >= 0 && indexCache <= groupPositions.size()) {
                preGroupViewHeight = mGroupViews.get(groupPositions.get(indexCache - 1)).getGroupViewHeight();
            } else if (indexCache + 1 >= 0 && indexCache + 1 < groupPositions.size()) {
                if (groupPositions.size() > (indexCache + 1) && mGroupViews.get(groupPositions.get(indexCache + 1)).getGroupViewHeight() != null) {
                    //第一组特殊处理， 此时preGroupViewHeight指代下一组高度
                    preGroupViewHeight = mGroupViews.get(groupPositions.get(indexCache + 1)).getGroupViewHeight();
                } else {
                    preGroupViewHeight = groupViewHeight;
                }
            }

            int nextPosition = 0;
            int curPosition = 0;

            if (map.get("nextPosition") != null) {
                nextPosition = (int) map.get("nextPosition");
                nextTop = (float) map.get("nextTop");
                curPosition = (int) map.get("curPosition");
            }
            if (nextPosition - curPosition == 1 && (nextTop - groupViewHeight < preGroupViewHeight)) {
                curTop = nextTop - groupViewHeight - preGroupViewHeight;
            } else if (curTop - groupViewHeight < 0) {//保持当前组GroupView一直在顶部
                curTop = 0;
            } else {
                map.put("pre", (int) map.get("cur") - 1);
                int preIndex = (int) map.get("pre");
                if (preIndex >= 0) {
                    preGroupViewHeight = mGroupViews.get(groupPositions.get(preIndex)).getGroupViewHeight();
                } else {
                    preGroupViewHeight = groupViewHeight;
                }
                if (curTop - groupViewHeight < preGroupViewHeight) {//判断与上一组的碰撞，推动当前的顶部GroupView
                    curTop = curTop - preGroupViewHeight - groupViewHeight;
                } else {
                    curTop = 0;
                }
                indexCache = (int) map.get("pre");
            }
            if (isUpGlide) {
                c.translate(0, mBottomViewPosition);
            } else {
                c.translate(0, curTop);
            }

            if (map.get("pre") != null) {//判断顶部childView的分组归属，绘制对应的GroupView
                drawGroupView(c, parent, (int) map.get("pre"));
            } else {
                drawGroupView(c, parent, (int) map.get("cur"));
            }

        } else {//否则当前组为空时，通过之前缓存的索引找到上一个GroupView并绘制到顶部
            if (isUpGlide) {
                c.translate(0, mBottomViewPosition);
            } else {
                c.translate(0, 0);
            }
            drawGroupView(c, parent, indexCache);
        }
        c.restore();
    }

    /**
     * 绘制GroupView
     *
     * @param canvas
     * @param parent
     * @param index
     */
    private void drawGroupView(Canvas canvas, RecyclerView parent, int index) {
        if (index < 0 || groupView == null) {
            return;
        }
        groupView.draw(canvas);
    }

    @Override
    public void onPullDownScrollChanged(int dy, int topViewPosition) {

    }

    @Override
    public void onUpGlideScrollChanged(int dy, int bottomViewPosition) {
        if (bottomViewPosition > 0) {
            isUpGlide = true;
            mBottomViewPosition = bottomViewPosition;
        } else {
            isUpGlide = false;
            mBottomViewPosition = 0;
        }
    }

    public interface GroupDecorationCallBack {

        View onCreatGroupView(ViewGroup parent, int type);

        /**
         * 构建GroupView
         *
         * @param groupView 返回的groupView实例
         */
        void onBindGroupView(View groupView, int itemPosition, int groupIndex, int viewType);
    }

    /**
     * 查询startPosition对应分组的索引
     *
     * @param groupArrays
     * @param startPosition
     * @return
     */
    private int searchGroupIndex(List<Integer> groupArrays, int startPosition) {
        //Collections.sort(groupArrays);
        return Collections.binarySearch(groupArrays, startPosition);
    }


    /**
     * 测量View的大小和位置
     *
     * @param view
     * @param parent
     */
    private void measureView(View view, View parent, int position) {
        if (view.getLayoutParams() == null) {
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.getPaddingLeft() + parent.getPaddingRight(), view.getLayoutParams().width);

        int childHeight;
        if (view.getLayoutParams().height > 0) {
            childHeight = View.MeasureSpec.makeMeasureSpec(view.getLayoutParams().height, View.MeasureSpec.EXACTLY);
        } else {
            childHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);//未指定
        }

        view.measure(childWidth, childHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        groupViewHeight = view.getMeasuredHeight();
        mGroupViews.get(position).setGroupViewHeight(groupViewHeight);
    }

    /**
     * 判断LayoutManager类型，目前GroupItemDecoration仅支持LinearLayoutManager.VERTICAL
     *
     * @param parent
     * @return
     */
    private boolean isLinearAndVertical(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            return false;
        } else {
            if (((LinearLayoutManager) layoutManager).getOrientation()
                    != LinearLayoutManager.VERTICAL) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param startPosition group组头在recyclerview中的位置
     *                      添加group
     */
    public void addGroup(int startPosition) {
        groupList.add(new GroupItem(startPosition, 0));
        int listEnd = groupList.size() - 1;
        if (groups.get(startPosition) == null) {
            callBackGroupView(startPosition, listEnd);
            groups.put(startPosition, groupList.get(listEnd));
            groupPositions.add(startPosition);
            mGroupViews.put(startPosition, new GroupView(mNewGroupView));
        }
    }

    /**
     * @param startPosition group组头在recyclerview中的位置
     *                      添加group
     */
    public void addGroup(int viewType, int startPosition) {
        groupList.add(new GroupItem(startPosition, viewType));
        int listEnd = groupList.size() - 1;
        if (groups.get(startPosition) == null) {
            callBackGroupView(startPosition, listEnd);
            groups.put(startPosition, groupList.get(listEnd));
            groupPositions.add(startPosition);
            mGroupViews.put(startPosition, new GroupView(mNewGroupView));
        }
    }

    public void addGroup(GroupItem groupItem) {
        if (groupItem != null) {
            groupList.add(groupItem);
            int listEnd = groupList.size() - 1;
            if (groups.get(groupItem.getStartPosition()) == null) {
                callBackGroupView(groupItem.getStartPosition(), listEnd);
                groups.put(groupItem.getStartPosition(), groupList.get(listEnd));
                groupPositions.add(groupItem.getStartPosition());
                mGroupViews.put(groupItem.getStartPosition(), new GroupView(mNewGroupView));
            }
        }
    }

    /**
     * @param startPosition group组头在recyclerview中的位置
     *                      批量添加group
     */
    public void addGroupList(int... startPosition) {
        for (int aStartPosition : startPosition) {
            groupList.add(new GroupItem(aStartPosition, 0));
            int listEnd = groupList.size() - 1;
            if (groups.get(startPosition) == null) {
                groups.put(startPosition, groupList.get(listEnd));
                groupPositions.add(aStartPosition);
                callBackGroupView(aStartPosition, groupList.size() - 1);
                mGroupViews.put(aStartPosition, new GroupView(mNewGroupView));
            }
        }
    }

    /**
     * @param startPosition group组头在recyclerview中的位置
     *                      批量添加group
     */
    public void addGroupList(int viewType, int... startPosition) {
        for (int aStartPosition : startPosition) {
            groupList.add(new GroupItem(aStartPosition, viewType));
            int listEnd = groupList.size() - 1;
            if (groups.get(startPosition) == null) {
                groups.put(startPosition, groupList.get(listEnd));
                groupPositions.add(aStartPosition);
                callBackGroupView(aStartPosition, groupList.size() - 1);
                mGroupViews.put(aStartPosition, new GroupView(mNewGroupView));
            }
        }
    }

    public void addGroupList(List<GroupItem> groupItems) {
        if (groupItems != null) {
            groupList.addAll(groupItems);
            for (GroupItem groupItem : groupItems) {
                groupList.add(groupItem);
                int listEnd = groupList.size() - 1;
                if (groups.get(groupItem.getStartPosition()) == null) {
                    groups.put(groupItem.getStartPosition(), groupList.get(listEnd));
                    groupPositions.add(groupItem.getStartPosition());
                    callBackGroupView(groupItem.getStartPosition(), groupList.size() - 1);
                    mGroupViews.put(groupItem.getStartPosition(), new GroupView(mNewGroupView));
                }
            }
        }
    }

    /**
     * @param index 根据索引获取GroupItem
     * @return {@link GroupItem}
     */
    public GroupItem getGroupItem(int index) {
        return groupList.get(index);
    }

    private void callBackGroupView(int itemPosition, int groupIndex) {
        if (mDecorationCallback != null) {
            mNewGroupView = mDecorationCallback.onCreatGroupView(mParent, groupList.get(groupIndex).getType());
            mDecorationCallback.onBindGroupView(mNewGroupView, itemPosition, groupIndex, groupList.get(groupIndex).getType());
        }
    }

    /**
     * @param parent 传入父容器
     */
    public void setParent(RecyclerView parent) {
        mParent = parent;
    }

    public void removeAllGroup() {
        groupView = null;
        groupViewHeight = 0;
        groupPositions.clear();
        groupList.clear();
        groups.clear();
        mGroupViews.clear();
        indexCache = -1;
    }

    public void removeGroup(int index) {
        groupList.remove(index);
        if (groupView == mGroupViews.get(groupPositions.get(index)).getGroupView()) {
            groupView = null;
        }
        indexCache = -1;
        mGroupViews.remove(groupPositions.get(index));
        groups.remove(groupPositions.get(index));
        groupPositions.remove(index);
    }
}
