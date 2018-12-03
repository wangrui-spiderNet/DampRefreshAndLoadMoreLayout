package com.jzycc.layout.damprefreshandloadmorelayout.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jzycc.layout.damprefreshandloadmorelayout.R;
import com.jzycc.layout.damprefreshandloadmorelayout.ui.adapter.MainAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvContent;
    private List<String> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();

        rvContent.setLayoutManager(new GridLayoutManager(this,3));
        MainAdapter mAdapter = new MainAdapter(mList);
        rvContent.setAdapter(mAdapter);
        mAdapter.setOnClickItemListener(new MainAdapter.OnClickItemListener() {
            @Override
            public void click(int position) {
                switch (position){
                    default:
                        ListActivity.actionStart(MainActivity.this,position);
                }
            }
        });
    }

    private void initView(){
        rvContent = findViewById(R.id.rv_content);
    }

    private void initData(){
        mList.add("默认回弹");
        mList.add("默认刷新");
        mList.add("默认加载");
        mList.add("默认刷新与加载");
        mList.add("自定义刷新与加载");
        mList.add("SwipeRefresh");
    }
}
