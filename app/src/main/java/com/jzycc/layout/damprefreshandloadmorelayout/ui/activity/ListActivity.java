package com.jzycc.layout.damprefreshandloadmorelayout.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.jzycc.layout.damplayoutlibrary.bottomview.DampBottomViewChild;
import com.jzycc.layout.damplayoutlibrary.layout.DampRefreshAndLoadMoreLayout;
import com.jzycc.layout.damplayoutlibrary.topview.DampTopViewChild;
import com.jzycc.layout.damplayoutlibrary.topview.swipetopview.SwipeTopView;
import com.jzycc.layout.damprefreshandloadmorelayout.R;
import com.jzycc.layout.damprefreshandloadmorelayout.model.Content;
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
    private int pageSize = 5;
    private boolean loadOver = false;
    private Integer type = 0;

    public static void actionStart(Context context,Integer type){
        Intent intent = new Intent(context,ListActivity.class);
        intent.putExtra("type",type);
        context.startActivity(intent);
    }

    private void getIntentData(){
        type = getIntent().getIntExtra("type",0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setZhiHuVo();
        initView();
        getIntentData();
        LinearLayoutManager layoutmanager = new LinearLayoutManager(this);
        rvContent.setLayoutManager(layoutmanager);
        mAdapter = new ListAdapter(this,mPageList);
        rvContent.setAdapter(mAdapter);

        setActivityType(type);

        loadZhiHuVo();


        if(type==1||type==3||type==4||type==5){
            dvContent.addOnDampRefreshListener(new DampRefreshAndLoadMoreLayout.DampRefreshListener() {
                @Override
                public void onScrollChanged(int i, int i1) {

                }

                @Override
                public void startRefresh() {
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
                            }catch (InterruptedException e){
                                Log.e("jzyTest", "run: ",e );
                            }
                        }
                    }).start();
                }
            });
        }

        if(type == 2||type == 3 ||type ==4){
            //dvContent.setAnimationDuration(20);
            dvContent.addOnDampLoadMoreListener(new DampRefreshAndLoadMoreLayout.DampLoadMoreListener() {
                @Override
                public void onScrollChanged(int i, int i1) {

                }

                @Override
                public void startLoadMore() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(300);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadZhiHuVo();
                                        if(!loadOver){
                                            if(!loadOver)
                                                dvContent.stopLoadMoreAnimation();
                                            //mAda  pter.notifyDataSetChanged();
                                        }else {
                                            dvContent.loadOver();
                                        }
                                    }
                                });
                            }catch (InterruptedException e){
                                Log.e("jzyTest", "run: ",e );
                            }
                        }
                    }).start();
                }
            });
        }

    }

    private void setActivityType(Integer type){
        switch (type){
            case 0:
                break;
            case 1:
                dvContent.setTopView();
                break;
            case 2:
                dvContent.setBottomView();
                break;
            case 3:
                dvContent.setTopView();
                dvContent.setBottomView();
                break;
            case 4:
                DampTopViewChild dampTopViewChild = new DampTopViewChild(this);
                DampBottomViewChild dampBottomViewChild = new DampBottomViewChild(this);
                dampBottomViewChild.setImageColorResource(getResources().getColor(R.color.colorAccent));
                dampTopViewChild.setImageColorResource(getResources().getColor(R.color.colorAccent));
                dampTopViewChild.setTextColorResource(getResources().getColor(R.color.colorAccent));
                dampBottomViewChild.setTextColorResource(getResources().getColor(R.color.colorAccent));
                dampBottomViewChild.setLoadOverText("再拉裤子要掉了");
                dvContent.setTopView(dampTopViewChild,DampTopViewChild.DAMPTOPVIEW_HEIGHT);
                dvContent.setBottomView(dampBottomViewChild,DampBottomViewChild.DAMPBOTTOMVIEW_HEIGHT);
                break;
            case 5:
                SwipeTopView swipeTopView = new SwipeTopView(this);
                dvContent.setTopView(swipeTopView,SwipeTopView.SWIPETOPVIEW_HEIGHT);
                break;
        }

    }

    private void initView(){
        dvContent = (DampRefreshAndLoadMoreLayout) findViewById(R.id.dv_content);
        rvContent = (RecyclerView)findViewById(R.id.rv_content);
    }


    private void setZhiHuVo(){
        String[] images = Content.images.split(",");
        String[] titles = Content.titles.split("&&");
        for(int i = 0 ; i < images.length ; i++){
            mList.add(new ZhiHuDto(titles[i],images[i]));
        }
    }
    private void refresh(){
        loadOver = false;
        count = 0;
        mPageList.clear();
        loadZhiHuVo();
        dvContent.stopRefreshAnimation();
        mAdapter.notifyDataSetChanged();
    }
    private void loadZhiHuVo(){
        if( count+1<=mList.size()/pageSize){
            int length = mPageList.size();
            for (int i = count*pageSize; i<count*pageSize+pageSize;i++){
                mPageList.add(mList.get(i));
            }
            if(mAdapter!=null) {
               mAdapter.notifyItemRangeInserted(length,pageSize);
            }
            count++;
        }else {
            loadOver = true;
        }
    }
}
