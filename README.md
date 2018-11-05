# EasyListView
[![](https://jitpack.io/v/xushihai/EasyListView.svg)](https://jitpack.io/#xushihai/EasyListView)
# Adapter
  通过anotationprocessor注解技术简化RecyclerView的Adapter的许多繁琐的代码
Adapter功能:
  - @Adapter注解提供ViewHolder和对应的LayoutId
  - @Adapter注解还提供多选或单选的compoundButton的id
  - @Holder注解和getViewTypeValue方法处理多视图的数据

| setMode | 设置适配器 |
|--|--|
|  |  |


注：所有的ViewHolder都需要集成BaseHolder
1、最简单的单视图适配器
```java
public class XHolder extends BaseHolder {
    public XHolder(@NonNull View itemView) {
        super(itemView);
    }
    @Override
    public void bindValues(Object object, int positoin) {
        super.bindValues(object, positoin);
        TextView textView = itemView.findViewById(R.id.tv_a);

        if (textView != null) {
            textView.setTextColor(Color.RED);
            textView.setTextSize(18);
            textView.setText("Hello " + object + " " + getItemViewType());
        }
    }
}


@Adapter(holder = XHolder.class, layout = R.layout.item_a)
public class Xada extends BaseAdapter<String> {

}

Xada xada = new Xada();
List<String> list = new ArrayList<>();
list.add("2");
xada.setData(list);
xada.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View view, int position, Object item) {
     Toast.makeText(MainActivity.this, position + " " + item, Toast.LENGTH_SHORT).show();
    }
});
xada.setOnLongClickListener(new OnLongClickListener() {
    @Override
    public void onLongClick(View view, int position, Object item) {
    Toast.makeText(MainActivity.this, position + " ghhfhhh " + item, Toast.LENGTH_SHORT).show();
   }
});
recyclerView.setAdapter(xada);
```

2、多视图的适配器
```java
public class Xada extends BaseAdapter<String> {

    @Holder
    static Object[][] holders = {
            {XHolder.class, R.layout.item_a, "2"},//第三个就是用来匹配ViewType
            {XHolder.class, R.layout.item_a, "3"},
            {XXHolder.class, R.layout.item_b, "4"},
            {XHolder.class, R.layout.item_a, "5"},
            {XHolder.class, R.layout.item_a, "6"},
    };

  //这个方法就是用来返回具体数据用于匹配viewType的
    @Override
    protected Object getViewTypeValue(String item) {
        return item;
    }

}

xada = new Xada();
List<String> list = new ArrayList<>();
list.add("2");
list.add("3");
list.add("4");
list.add("5");
list.add("6");
xada.setData(list);
recyclerView.setAdapter(xada);
```
3、有单选或多选功能的适配器
```java
@Adapter(holder = XHolder.class, layout = R.layout.item_a,compoundButton = R.id.radio_button_a)//compoundButton指定单选或者多选的选择按钮控件ID
public class Xada extends BaseAdapter<String> {

}

Xada xada = new Xada();
List<String> list = new ArrayList<>();
list.add("2");
xada.setData(list);
xada.setMode(BaseAdapter.Mode.SINGLE);//  xada.setMode(BaseAdapter.Mode.MULTI);
xada.setOnSingleChoiceListener(new BaseAdapter.OnSingleChoiceListener() {//单选的监听器
   @Override
    public void onSingleChoiceChanged(int position, Object item) {
     Log.e("xcncbc", position + "  " + item);
    }
});
xada.setOnMultiChoiceListener(new BaseAdapter.OnMultiChoiceListener() {//多选的监听器
    @Override
    public void onMultiChoiceChanged(int checkSum, int total) {
    Log.e("xcncbc", checkSum + "  " + total);
    }
});
recyclerView.setAdapter(xada);
```


# AVLoadingIndicatorView（加载中动画）
 | 动画效果 | 动画名称 |
| :------:| :------: |
| ![AVLoadingIndicatorView](https://img-blog.csdnimg.cn/20181105103212711.gif )  |  ![AVLoadingIndicatorViewNames](https://img-blog.csdnimg.cn/20181105103320815.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2xvdmVsaXdlbnlhbjIwMTI=,size_16,color_FFFFFF,t_70 )


```java
 easyListView.getStatusLayout().setIndicator("PacmanIndicator", Color.RED);
```

# SmartRefreshLayout（上拉，刷新控件）
```java
//设置上拉动画，下拉动画
       easyListView.getSwipeRefreshLayout().setRefreshHeader(new BezierRadarHeader(this));
       easyListView.getSwipeRefreshLayout().setRefreshFooter(new BallPulseFooter(this));
```
更多详情可以进入[SmartRefreshLayout官网](https://github.com/scwang90/SmartRefreshLayout)
