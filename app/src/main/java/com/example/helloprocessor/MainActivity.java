package com.example.helloprocessor;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.xsh.EasyListView;
import com.xsh.adapter.BaseAdapter;
import com.xsh.adapter.OnClickListener;
import com.xsh.adapter.OnLongClickListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;
import rx.functions.Func1;


public class MainActivity extends Activity {

    BaseAdapter adapter;
    EasyListView easyListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initSimpleAdapter();
        easyListView.setAndStartTask(getTask(), adapter);
    }

    private void init() {
        easyListView = findViewById(R.id.easy_listview);
        easyListView.setLayoutManager(new LinearLayoutManager(this));
        easyListView.getStatusLayout().setIndicator("PacmanIndicator", Color.RED);
        easyListView.getSwipeRefreshLayout().setRefreshHeader(new BezierRadarHeader(this));
        easyListView.getSwipeRefreshLayout().setRefreshFooter(new BallPulseFooter(this));
    }

    private void initAdapter() {
        adapter.setOnClickListener(new OnClickListener<String>() {
            @Override
            public void onClick(View view, int position, String item) {
                Toast.makeText(MainActivity.this, "点击，position:" + position + " item:" + item, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnLongClickListener(new OnLongClickListener<String>() {
            @Override
            public void onLongClick(View view, int position, String item) {
                Toast.makeText(MainActivity.this, "长按：position:" + position + " item:" + item, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnMultiChoiceListener(new BaseAdapter.OnMultiChoiceListener() {
            @Override
            public void onMultiChoiceChanged(int checkSum, int total) {
                Log.e("EasyListView", "多选，选中数量：" + checkSum + "  总体数量：" + total + " 选中的数据：" + adapter.getMulitiResult() + "  " + adapter.getMulitiResultIndex());
            }
        });

        adapter.setOnSingleChoiceListener(new BaseAdapter.OnSingleChoiceListener<String>() {
            @Override
            public void onSingleChoiceChanged(int position, String item) {
                Log.e("EasyListView", "单选 position:" + position + "  item:" + item);
            }
        });
    }

    private void initSimpleAdapter() {
        adapter = new SimpleAdapter();
        initAdapter();
    }

    private void initMultiAdapter() {
        adapter = new MultiAdapter();
        initAdapter();
    }


    @SuppressWarnings("unchecked")
    private Observable<List> getTask() {
        return Observable.just(adapter.getItemCount())
                .flatMap(new Func1<Integer, Observable<List>>() {
                    @Override
                    public Observable<List> call(Integer offset) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //需要自己判断是刷新还是加载更多，第一加载算是刷新，跟刷新的操作是一样的。

                        offset = easyListView.getLoadOffset();
                        List list = new ArrayList<>();
                        for (int i = offset; i < offset + 20; i++) {
                            list.add(String.valueOf(i));
                        }
                        return Observable.just(list);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private Observable<List> getErrorTask() {
//        Observable<List> observable = Observable.create(new Observable.OnSubscribe<List>() {
//            @Override
//            public void call(Subscriber<? super List> subscriber) {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                subscriber.onError(new SocketTimeoutException());
//            }
//        });
//. 0
//        return observable;

//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();
        // .addConverterFactory(GsonConverterFactory.create(gson))
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://www.baidu.com/")
                .addConverterFactory(ScalarsConverterFactory.create())//返回原始json字符串
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build();
        return retrofit.create(APi.class).getNews("")
                .flatMap(new Func1<String, Observable<List>>() {
                    @Override
                    public Observable<List> call(String s) {
                        Log.e("EasyListView", "xxx:");
                        if (s.length() > 100) {
                            return Observable.error(new Exception("字符串长度过长"));
                        }

                        List list = new ArrayList<>();
                        list.add(s);
                        return Observable.just(list);
                    }
                });
    }

    private Observable<List> getEmptyTask() {
        return Observable.just(0)
                .flatMap(new Func1<Integer, Observable<List>>() {
                    @Override
                    public Observable<List> call(Integer count) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        List list = new ArrayList<>();
                        return Observable.just(list);
                    }
                });
    }

    @SuppressWarnings("unused")
    public void toggle(View view) {
        if (adapter.getMode().mode == BaseAdapter.Mode.NONE.mode)
            adapter.setMode(BaseAdapter.Mode.MULTI);
        else
            adapter.setMode(BaseAdapter.Mode.NONE);
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_test_empty:
                testEmpty();
                break;
            case R.id.menu_test_error:
                testLoadError();
                break;
            case R.id.menu_test_loading:
                testLoading();
                break;
            case R.id.menu_test_refresh:
                testRefresh();
                break;
            case R.id.menu_test_load_more:
                testLoadMore();
                break;
            case R.id.menu_test_multi_view:
                testMultiView();
                break;
            case R.id.menu_test_normal_single:
                testNormalSingle();
                break;
            case R.id.menu_test_normal_multi:
                testNormalMulti();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void testEmpty() {
        easyListView.setAndStartTask(getEmptyTask(), adapter);
    }

    private void testLoadError() {
        easyListView.setAndStartTask(getErrorTask(), adapter);
    }

    private void testLoading() {

    }

    private void testRefresh() {
        easyListView.setEnableRefresh(true);
    }

    private void testLoadMore() {
        easyListView.setEnableLoadMore(true);
    }

    private void testMultiView() {
        initMultiAdapter();
        easyListView.setAndStartTask(getTask(), adapter);
    }

    private void testNormalSingle() {
        if (adapter.getMode().mode != BaseAdapter.Mode.SINGLE.mode)
            adapter.setMode(BaseAdapter.Mode.SINGLE);
        else
            adapter.setMode(BaseAdapter.Mode.NONE);
        adapter.notifyDataSetChanged();
    }

    private void testNormalMulti() {

        if (adapter.getMode().mode != BaseAdapter.Mode.MULTI.mode)
            adapter.setMode(BaseAdapter.Mode.MULTI);
        else
            adapter.setMode(BaseAdapter.Mode.NONE);
        adapter.notifyDataSetChanged();
    }


    public interface APi {
        // @GET注解的作用:采用Get方法发送网络请求
        // getNews(...) = 接收网络请求数据的方法
        // 其中返回类型为Call<News>，News是接收数据的类（即上面定义的News类）
        // 如果想直接获得Responsebody中的内容，可以定义网络请求返回值为Call<ResponseBody>
        @Headers("apikey:81bf9da930c7f9825a3c3383f1d8d766")
        @GET("word/word")
        Observable<String> getNews(@Query("num") String num);
    }
}


