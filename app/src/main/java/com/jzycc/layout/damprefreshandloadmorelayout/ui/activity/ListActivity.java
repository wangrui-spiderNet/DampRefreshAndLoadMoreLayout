package com.jzycc.layout.damprefreshandloadmorelayout.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jzycc.layout.damplayoutlibrary.bottomview.DampBottomViewChild;
import com.jzycc.layout.damplayoutlibrary.layout.DampRefreshAndLoadMoreLayout;
import com.jzycc.layout.damplayoutlibrary.layout.decoration.GroupItemDecoration;
import com.jzycc.layout.damplayoutlibrary.topview.DampTopViewChild;
import com.jzycc.layout.damplayoutlibrary.topview.swipetopview.SwipeTopView;
import com.jzycc.layout.damprefreshandloadmorelayout.Content;
import com.jzycc.layout.damprefreshandloadmorelayout.R;
import com.jzycc.layout.damprefreshandloadmorelayout.model.ZhiHuDto;
import com.jzycc.layout.damprefreshandloadmorelayout.ui.adapter.ListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * author Jzy(Xiaohuntun)
 * date 18-9-17
 */
public class ListActivity extends AppCompatActivity {
    private RecyclerView rvContent;
    private DampRefreshAndLoadMoreLayout dvContent;
    private List<ZhiHuDto> mList = new ArrayList<>();
    private List<ZhiHuDto> mPageList = new ArrayList<>();
    private ListAdapter mAdapter;
    private int count = 0;
    private static final int pageSize = 5;
    private boolean loadOver = false;
    private Integer type = 0;
    private int length;

    public static void actionStart(Context context, Integer type) {
        Intent intent = new Intent(context, ListActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    private void getIntentData() {
        type = getIntent().getIntExtra("type", 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setZhiHuVo();
        initView();
        getIntentData();
        LinearLayoutManager layoutmanager = new LinearLayoutManager(this);
        setActivityType(type);
        rvContent.setLayoutManager(layoutmanager);
        mAdapter = new ListAdapter(this, mPageList);
        rvContent.setAdapter(mAdapter);
        loadZhiHuVo();


        if (type == 1 || type == 3 || type == 4 || type == 5) {
            dvContent.addOnDampRefreshListener(new DampRefreshAndLoadMoreLayout.DampRefreshListener() {
                @Override
                public void onScrollChanged(int i, int i1) {

                }

                @Override
                public void onRefresh() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refresh();
                                    }
                                });
                            } catch (InterruptedException e) {
                                Log.e("jzyTest", "run: ", e);
                            }
                        }
                    }).start();
                }
            });
        }

        if (type == 2 || type == 3 || type == 4) {
            dvContent.addOnDampLoadMoreListener(new DampRefreshAndLoadMoreLayout.DampLoadMoreListener() {
                @Override
                public void onScrollChanged(int i, int i1) {

                }

                @Override
                public void onLoadMore() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadZhiHuVo();
                                        if (!loadOver) {
                                            dvContent.stopLoadMoreAnimation();
                                        } else {
                                            dvContent.stopLoadMoreAnimation();
                                            dvContent.loadOver();
                                        }
                                    }
                                });
                            } catch (InterruptedException e) {
                                Log.e("jzyTest", "run: ", e);
                            }
                        }
                    }).start();
                }
            });
        }
    }

    private void setActivityType(Integer type) {
        switch (type) {
            case 0:
                break;
            case 1:
                dvContent.openRefresh();
                break;
            case 2:
                dvContent.openLoadMore();
                break;
            case 3:
                dvContent.openRefresh();
                dvContent.openLoadMore();
                break;
            case 4:
                GroupItemDecoration groupItemDecoration = setDecoration();
                dvContent.setGroupDecoration(groupItemDecoration);
                dvContent.openRefresh(new DampTopViewChild.Builder(this)
                        .setImageColorResource(R.color.colorAccent)
                        .setTextColorResource(R.color.colorAccent)
                        .build(), DampTopViewChild.DAMPTOPVIEW_HEIGHT);
                dvContent.openLoadMore(new DampBottomViewChild.Builder(this)
                        .setImageColorResource(R.color.colorAccent)
                        .setTextColorResource(R.color.colorAccent)
                        .setLoadOverText("再拉裤子要掉了")
                        .build(), DampBottomViewChild.DAMPBOTTOMVIEW_HEIGHT);
                break;
            case 5:
                SwipeTopView swipeTopView = new SwipeTopView(this);
                dvContent.openRefresh(swipeTopView, SwipeTopView.SWIPETOPVIEW_HEIGHT);
                break;
        }

    }

    private void initView() {
        dvContent = findViewById(R.id.dv_content);
        rvContent = findViewById(R.id.rv_content);
    }


    private void setZhiHuVo() {
        String[] images = Content.images.split(",");
        String[] titles = Content.titles.split("&&");
        for (int i = 0; i < images.length; i++) {
            mList.add(new ZhiHuDto(titles[i], images[i]));
        }
    }

    private void refresh() {
        loadOver = false;
        count = 0;
        mPageList.clear();
        if (mGroupItemDecoration != null) {
            mGroupItemDecoration.removeAllGroup();
        }
        loadZhiHuVo();
        dvContent.stopRefreshAnimation();
        mAdapter.notifyDataSetChanged();
    }

    private void loadZhiHuVo() {
        if (count + 1 <= mList.size() / pageSize) {
            length = mPageList.size();
            for (int i = count * pageSize; i < count * pageSize + pageSize; i++) {
                mPageList.add(mList.get(i));
                if (mGroupItemDecoration != null && i % 5 == 0) {
                    mGroupItemDecoration.addGroup(i%2, mPageList.size() - 1);
                }
            }
            if (mAdapter != null) {
                mAdapter.notifyItemRangeInserted(length, pageSize);
            }
            count++;
            //loadOver = true;
        } else {
            loadOver = true;
        }
    }

    GroupItemDecoration mGroupItemDecoration;
    int num = 1;

    private GroupItemDecoration setDecoration() {

        mGroupItemDecoration = new GroupItemDecoration(this, new GroupItemDecoration.GroupDecorationCallBack() {
            @Override
            public View onCreatGroupView(ViewGroup parent, int type) {
                if (type == 0) {
                    return LayoutInflater.from(ListActivity.this).inflate(R.layout.decoration_default_style, parent, false);
                } else {
                    return LayoutInflater.from(ListActivity.this).inflate(R.layout.decoration_second_style, parent, false);
                }

            }

            @Override
            public void onBindGroupView(View groupView, int itemPosition, int groupIndex, int viewType) {
                TextView textView = groupView.findViewById(R.id.tv_text);
                textView.setText("group " + (groupIndex + 1));
            }
        });
        mGroupItemDecoration.setStickyHeader(true);
        return mGroupItemDecoration;
    }
}
