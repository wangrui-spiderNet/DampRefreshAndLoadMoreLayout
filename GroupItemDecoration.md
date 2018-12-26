# GroupItemDecoration 使用手册 
GroupItemDecroation 可以自定义 RecyclerView 分组，并带有黏性效果，保证当前 group 的 titlebar 会被黏在容器的最顶部，
可以单独使用，也已经适配 DampRefreshAndLoadMoreLayout。


![GroupItemDecoration](https://github.com/JzyCc/Material-library/blob/master/forgithub/forDampRefreshAndLoadMoreLayout/GroupItemDecoration_demo1.gif)

## 如何使用 GroupItemDecoration
### 创建实例
```
        mGroupItemDecoration = new GroupItemDecoration(this, new GroupItemDecoration.GroupDecorationCallBack() {
            @Override
            public View onCreatGroupView(ViewGroup parent, int type) {
                //在这里你应该创建你的 GroupView
                //比如
                 if(type == 0){ 
                    return LayoutInflater.from(ListActivity.this).inflate(R.layout.decoration_default_style,parent,false);
                }else {
                    return LayoutInflater.from(ListActivity.this).inflate(R.layout.decoration_second_style,parent,false);
                }
            }

            @Override
            public void onBindGroupView(View groupView, int itemPosition, int groupIndex, int viewType) {
                //在这里你应该为你的 GroupView 绑定数据
                //比如
                TextView textView = groupView.findViewById(R.id.tv_text);
                textView.setText("this is a group");
            }
        });
```
### 直接使用
**为 GroupItemDecoration 传入父容器**
```
    mGroupItemDecoration.setParent(recyclerView);
```
**为 RecyclerView 添加 Decoration**
```
    recyclerView.addItemDecoration(mGroupItemDecoration);
```
### 搭配 DampRefreshAndLoadMoreLayout 使用
> 如果还不会使用请参考此链接
> [DampRefreshAndLoadMoreLayout使用手册](https://github.com/JzyCc/DampRefreshAndLoadMoreLayout/blob/master/README.md)

**为 DampRefreshAndLoadMoreLayout 配置 GroupItemDecoration**
```
    dampRefreshAndLoadMoreLayout.setGroupDecoration(mGroupItemDecoration);
```
**也可以使用 Builder 模式**
```
    DampRefreshAndLoadMoreLayout.builder()
                                .attachLayout(dampRefreshAndLoadMoreLayout)
                                .setGroupDecoration(mGroupItemDecoration)
                                .build();
```
如何添加Group请参考下文 添加Group。

## 熟悉GroupItemDecoration

---
#### 必须要了解的CallBack GroupItemDecoration.GroupDecorationCallBack()

- **onCreatGroupView(ViewGroup parent, int type)**

parent 参数是父容器 RecylcerView

type 参数是 GroupView 的类型，可以根据 type 的不同创建不同布局的 groupView。 

而这个回调在你使用 addGroup() 方法后触发。
- **onBindGroupView(View groupView, int itemPosition, int groupIndex, int viewType)**

groupView 参数是你在 onCreatGroupView 中返回的View实例

itemPosition 是这个 groupView 在 RecyclerView 附着的 Item 的位置

groupIndex 是关于对 group 的索引，顺序与你addGroup()的顺序一致

type 参数是groupView的类型。

#### 如何添加 group 和 移除 group
##### 添加API:
- **addGroup(int startPosition)**
- **addGroup(int viewType, int StartPosition)**
- **addGroup(GroupItem groupItem)**
##### 参数介绍
startPosition groupView 在RecyclerView中的位置

viewType 你为 groupView 设置的类型

GroupItem 它里面包含了你所需配置的所有数据，已经提供了 Map ,方便你在绑定数据时使用它。

**如何获得你需要的 GroupItem**
```
 mGroupItemDecoration.getGroupItem(gourpIndex);
```
##### 移除API:
- **removeAllGroup()**
- **removeGroup(int index)**



