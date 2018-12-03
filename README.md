# DampRefreshAndLoadMoreLayout使用手册
![](https://img.shields.io/badge/Version-v1.2.0-brightgreen.svg)
## DampRefreshAndLoadMoreLayout介绍
1. 列表在它里面可以更灵活的转交事件。
2. 默认实现仿IOS拖动带阻尼回弹的效果。
3. 可以实现刷新和加载更多的功能。
4. 将头部和底部与容器分离，提供接口返回相关参数，可以根据返回参数完全自定义自己想要的刷新头部和加载底部。
5. 可以为列表分组，实现黏性头部效果 [GroupItemDecoration]()

### 实现效果
```
以下是是默认的刷新加载动画，DampRefreshAndLoadMoreLayout最重要的功能是提供刷新和加载的一个平台，
可以根据自己需求自定义刷新和加载！
```
![刷新与加载](https://raw.githubusercontent.com/JzyCc/Material-library/master/forgithub/forDampRefreshAndLoadMoreLayout/DampRefreshAndLoadMoreLayoutGif_1.gif)    ![SwipeRefresh](https://raw.githubusercontent.com/JzyCc/Material-library/master/forgithub/forDampRefreshAndLoadMoreLayout/DampRefreshAndLoadMoreLayoutGif_2.gif)
## 使用
### 1. 添加依赖
#### 在project的build.gradle中添加仓库
```
allprojects {
    repositories {
    	...
    	maven { url 'https://jitpack.io' }
    }
}
```
#### 在app的build.gradle中添加依赖

```
dependencies {
    ...
    implementation 'com.github.JzyCc:DampRefreshAndLoadMoreLayout:1.2.0'
}
```

### 2. 在XML布局中加入如下代码
```
    <com.zmsoft.widget.damprefreshandloadmorelayout.layout.DampRefreshAndLoadMoreLayout
        android:id="@+id/dll_content"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <android.support.v7.widget.RecyclerView
            android:id="@+id/rv_content"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:overScrollMode="never">
        </android.support.v7.widget.RecyclerView>
    </com.zmsoft.widget.damprefreshandloadmorelayout.layout.DampRefreshAndLoadMoreLayout>
```

此时配置好RecyclerView后，运行项目可以实现基本的阻尼回弹效果。

![默认回弹](https://raw.githubusercontent.com/JzyCc/Material-library/master/forgithub/forDampRefreshAndLoadMoreLayout/DampRefreshAndLoadMoreDemo_3.gif)

### 3.添加下拉刷新和上拉加载功能

**获取DampRefreshAndLoadMoreLayout实例**
```
dampRefreshAndLoadMoreLayout = findViewById(R.id.dll_content);
```
- #### 设置下拉刷新功能 

##### 设置默认刷新
```
dampRefreshAndLoadMoreLayout.openRefresh();
```
##### 添加刷新监听

```
dampRefreshAndLoadMoreLayout.addOnDampRefreshListener(new DampRefreshAndLoadMoreLayout.DampRefreshListener() {
            @Override
            public void onScrollChanged(int dy, int topViewPosition) {
                //此处返回当前滑动距离和topView顶部到容器顶部的距离
            }

            @Override
            public void onRefresh() {
                //当刷新触发时在此处写刷新相关逻辑
            }
        });
```
或者使用Builder模式
```
dampRefreshAndLoadMoreLayout = new DampRefreshAndLoadMoreLayout.Builder()
                .attachLayout(dampRefreshAndLoadMoreLayout)//传入DampRefreshAndLoadMoreLayout实例
                .openRefresh()
                .addOnDampRefreshListener(new DampRefreshAndLoadMoreLayout.DampRefreshListener() {
                    @Override
                    public void onScrollChanged(int dy, int topViewPosition) {
                        //此处返回当前滑动距离和topView顶部到容器顶部的距离
                    }

                    @Override
                    public void onRefresh() {
                        //当刷新触发时在此处写刷新相关逻辑
                    }
                })
                .build();
```
##### 结束刷新

```
//当刷新结束后调用此方法结束刷新动画
dampRefreshAndLoadMoreLayout.stopRefreshAnimation();
```
- #### 设置加载更多功能
##### 设置默认加载底部

```
dampRefreshAndLoadMoreLayout.openLoadMore();
```
##### 添加加载更多监听

```
dampRefreshAndLoadMoreLayout.addOnDampLoadMoreListener(new DampRefreshAndLoadMoreLayout.DampLoadMoreListener() {
            @Override
            public void onScrollChanged(int dy, int bottomViewPosition) {
                //此处返回当前滑动距离和bottomView底部到容器底部的距离
            }

            @Override
            public void onLoadMore() {
                //当加载更多触发时在此处写加载相关逻辑
            }
        });
```
或者使用Builder模式
```
dampRefreshAndLoadMoreLayout = new DampRefreshAndLoadMoreLayout.Builder()
                .attachLayout(dampRefreshAndLoadMoreLayout)//传入DampRefreshAndLoadMoreLayout实例
                .openLoadMore()
                .addOnDampLoadMoreListener(new DampRefreshAndLoadMoreLayout.DampLoadMoreListener() {
                    @Override
                    public void onScrollChanged(int dy, int bottomViewPosition) {
                        //此处返回当前滑动距离和bottomView顶部到容器底部的距离
                    }

                    @Override
                    public void onLoadMore() {
                        //当加载更多触发时在此处写加载相关逻辑
                    }
                })
                .build();
```

##### 结束加载

```
//当加载完成后调用此方法结束加载动画
dampRefreshAndLoadMoreLayout.stopLoadMoreAnimation();
```
##### 所有数据加载完毕

```
//当所有数据加载完成后调用此方法
dampRefreshAndLoadMoreLayout.loadOver();
```
### 4.就这样？自定义自己的刷新和加载！
- #### 自定义刷新View
##### 新建一个Class文件,此处我继承FrameLayout，实现DampTopViewListener接口

```
public class TopViewChild extends FrameLayout implements DampTopViewListener {

    public TopViewChild(@NonNull Context context) {
        super(context);
    }

    @Override
    public void onScrollChanged(int dy, int topViewPosition) {
         //此处返回当前滑动距离和topView顶部到容器顶部的距离
    }

    @Override
    public void onComplete() {
        //此时刷新已经完成
    }

    @Override
    public void onRefreshing() {
        //此时正在刷新
    }

    @Override
    public void onReady() {
        //此时松手可以触发刷新
    }

    @Override
    public void onStart() {
        //需要初始化的步骤，此处在按下屏幕并下拉时触发
    }
    
    @Override
    public void onCancel() {
        //此时松手不可以触发刷新
    }
}
```
提供了DampTopViewListener接口来返回当前容器的刷新状态，可以通过这些来实现刷新的动画。
##### 添加自定义topView
```
dampRefreshAndLoadMoreLayout.openRefresh(new TopViewChild(context),topViewHeight);
```

**此处应当传入自定义topView的高度（单位：dp）**

- #### 自定义加载View
##### 新建一个Class文件,此处我继承FrameLayout，实现DampBottomViewListener接口

```
public class BottomViewChild extends FrameLayout implements DampBottomViewListener {
    public BottomViewChild(@NonNull Context context) {
        super(context);
    }

    @Override
    public void onLoading() {
        //此时加载被触发，初始化工作也可以在此处执行
    }

    @Override
    public void onComplete() {
        //此时加载结束
    }

    @Override
    public void onLoaded() {
        //此时所有数据已经加载完毕
    }

    @Override
    public void onScrollChanged(int dy, int bottomViewPosition) {
        //此处返回当前滑动距离和bottomView顶部到容器底部的距离
    }
}
```
##### 添加自定义bottomView
```
dampRefreshAndLoadMoreLayout.openLoadMore(new BottomViewChild(context),bottomViewHeight);
```
或者使用Builder模式
```
dampRefreshAndLoadMoreLayout = new DampRefreshAndLoadMoreLayout.Builder()
                .attachLayout(dampRefreshAndLoadMoreLayout)//传入DampRefreshAndLoadMoreLayout实例
                .openRefresh(new TopViewChild(context), topViewHeight)
                .openLoadMore(new BottomViewChild(context),bottomViewHeight)
                .build();
```

### 5.关于默认topView和bottomView适配不同背景色
##### 默认topView设置图片颜色和文本颜色

```
 DampTopViewChild dampTopViewChild = new DampTopViewChild(this);
 dampTopViewChild.setImageColorResource(color);//设置图片颜色
 dampTopViewChild.setTextColorResource(color);//设置文本颜色
 dampRefreshAndLoadMoreLayout.openRefresh(dampTopViewChild,DampTopViewChild.DAMPTOPVIEW_HEIGHT);//设置TopView
```
##### 默认bottomView设置图片和文本颜色以及文本内容

```
 DampBottomViewChild dampBottomViewChild = new DampBottomViewChild(this);
 dampBottomViewChild.setImageColorResource(color);//设置图片颜色 
 dampBottomViewChild.setTextColorResource(color);//设置文本颜色
 dampBottomViewChild.setLoadOverText(text);//设置加载完毕文本内容
 dampRefreshAndLoadMoreLayout.openLoadMore(dampBottomViewChild,DampBottomViewChild.DAMPBOTTOMVIEW_HEIGHT);
```
**此处应当传入自定义bottomView的高度（单位：dp）**


或者使用Builder模式
```
dampRefreshAndLoadMoreLayout = new DampRefreshAndLoadMoreLayout.Builder()
                .attachLayout(dampRefreshAndLoadMoreLayout)//传入DampRefreshAndLoadMoreLayout实例
                .openRefresh(new DampTopViewChild.Builder(this)
                        .setImageColorResource(color)//设置图片颜色
                        .setTextColorResource(color)//设置文本颜色
                        .build(), DampTopViewChild.DAMPTOPVIEW_HEIGHT)
                .openLoadMore(new DampBottomViewChild.Builder(this)
                        .setImageColorResource(color)//设置图片颜色 
                        .setTextColorResource(color)//设置文本颜色
                        .setLoadOverText(text)//设置加载完毕文本内容
                        .build(), DampBottomViewChild.DAMPBOTTOMVIEW_HEIGHT)
                .build();
```
### 6.其它相关API
##### 设置动画时长：
```
setAnimationDuration(int duration)
```
##### 设置最高阻尼时middleView顶部距离到父容器顶部的距离（单位：dp）：

```
setPullDownDampDistance(int value)
```
##### 设置最高阻尼时middleView底部距离到父容器底部部的距离（单位：dp）：

```
setUpGlideDampDistance(int value)
```
##### 设置下拉时最大的阻尼系数，可选范围在0f~100f之间（0f不可滑动，100f无阻尼）
```
setPullDownDampValue(float pullDownDampValue)
```
##### 设置上拉时最大的阻尼系数，可选范围在0f~100f之间（0f不可滑动，100f无阻尼）
```
setUpGlideDampValue(float pullDownDampValue)
```
### 7.可选择的topView和bottomView
1.DampTopViewChild

2.DampBottomViewChild

3.SwipeTopView
