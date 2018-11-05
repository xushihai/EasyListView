package com.xsh;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xsh.adapter.BaseAdapter;
import com.xsh.adapter.R;
import com.xsh.adapter.StatusLayout;

import java.util.List;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EasyListView extends FrameLayout {
    RecyclerView recyclerView;
    SmartRefreshLayout swipeRefreshLayout;
    StatusLayout statusLayout;
    OnClickListener onRetryListener;
    Observable<List> observable;
    BaseAdapter baseAdapter;

    public EasyListView(Context context) {
        super(context);
        init(context, null);
    }

    public EasyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @SuppressWarnings("unused")
    private void init(Context context, AttributeSet attrs) {
        swipeRefreshLayout = new SmartRefreshLayout(getContext());
        recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        swipeRefreshLayout.addView(recyclerView);
        addView(swipeRefreshLayout);
        swipeRefreshLayout.setEnableRefresh(false);
        swipeRefreshLayout.setEnableLoadMore(false);
        swipeRefreshLayout.setRefreshHeader(new BezierRadarHeader(context).setEnableHorizontalDrag(true));
        //设置 Footer 为 球脉冲 样式
        swipeRefreshLayout.setRefreshFooter(new BallPulseFooter(context).setSpinnerStyle(SpinnerStyle.Scale));
        statusLayout = new StatusLayout(getContext());
        statusLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        statusLayout.setClickable(true);//是为了防止在显示状态界面时，上拉下拉能操作到，逻辑就太不对了
        statusLayout.setVisibility(GONE);
        addView(statusLayout);
    }

    @SuppressWarnings("unused")
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public SmartRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    @SuppressWarnings("unused")
    public StatusLayout getStatusLayout() {
        return statusLayout;
    }

    public void setAdapter(final RecyclerView.Adapter adapter) {
        if (adapter == null) {
            recyclerView.setAdapter(null);
            return;
        }
        recyclerView.setAdapter(adapter);
        RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (adapter.getItemCount() == 0) {
                    statusLayout.showEmpty();
                }
            }
        };
        adapter.registerAdapterDataObserver(adapterDataObserver);
        adapterDataObserver.onChanged();
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        recyclerView.setLayoutManager(layoutManager);
    }

    @SuppressWarnings("unused")
    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        recyclerView.addItemDecoration(itemDecoration);
    }


    @SuppressWarnings("unused")
    public void setEnableLoadMore(boolean enabled) {
        swipeRefreshLayout.setEnableLoadMore(true);

    }

    @SuppressWarnings("unused")
    public void setEnableRefresh(boolean enabled) {
        swipeRefreshLayout.setEnableRefresh(true);
    }

    @SuppressWarnings("unchecked")
    protected void startTask() {
        //只要不是通过刷新控件来执行的任务时都显示加载动画
        if (getState() == RefreshState.None) {
            statusLayout.setVisibility(VISIBLE);
            statusLayout.showLoading();
        }
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List>() {
            @Override
            public void onCompleted() {
                //需要处理当Retriofit使用Observable返回的是null导致不会执行onError从而不会正常的结束刷新状态的情况
                switch (swipeRefreshLayout.getState()) {
                    case Loading:
                        swipeRefreshLayout.finishLoadMore();
                        break;
                    case Refreshing:
                        swipeRefreshLayout.finishRefresh();
                        break;
                    case None:
                        if (statusLayout.isLoading()) {
                            String error = getContext().getString(R.string.reason_load_fail);
                            statusLayout.showError(error);
                            setOnRetryListener();
                        }
                        break;
                }
            }

            @Override
            public void onError(Throwable e) {
                doOnError(e);
            }

            @Override
            public void onNext(List data) {
                switch (swipeRefreshLayout.getState()) {
                    case Loading:
                        swipeRefreshLayout.finishLoadMore();
                        if (data == null)
                            break;
                        int previousCount = baseAdapter.getItemCount();
                        baseAdapter.getData().addAll(data);
                        baseAdapter.notifyItemRangeChanged(previousCount, data.size());
                        break;
                    case Refreshing:
                        swipeRefreshLayout.finishRefresh();
                        if (data == null)
                            break;
                        baseAdapter.getData().clear();
                        baseAdapter.setData(data);
                        baseAdapter.notifyDataSetChanged();
                        break;
                    case None:
                        if (data == null) {
                            doOnError(new NullPointerException("返回的数据列表为null"));
                            break;
                        }
                        statusLayout.hide();
                        baseAdapter.setData(data);
                        setAdapter(baseAdapter);
                        break;
                }
            }
        });
    }

    protected void doOnError(Throwable e) {
        String error;
        switch (e.getClass().getSimpleName()) {
            case "HttpException":
                HttpException httpException = (HttpException) e;
                //"网络连接失败，请设置网络"
                int code = httpException.code();
                error = httpException.message();
                Log.e("xxx", code + ":  " + error);
                break;

            case "SocketException":
                error = getContext().getString(R.string.reason_socket_disconnect);
                setOnRetryListener();
                break;

            case "SocketTimeoutException":
                error = getContext().getString(R.string.reason_connect_timeout);
                setOnRetryListener();
                break;

            case "ConnectException":
                if (!isNetAvailable(getContext())) {
                    error = getContext().getString(R.string.reason_network_unaviable);
                } else {
                    error = getContext().getString(R.string.reason_connect_fail);
                }
                setOnRetryListener();
                break;
            case "Exception":
                error = e.getMessage();
                break;
            default:
                error = getContext().getString(R.string.reason_load_fail);
                setOnRetryListener();
                break;
        }

        switch (swipeRefreshLayout.getState()) {
            case Loading:
                swipeRefreshLayout.finishLoadMore(false);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                return;
            case Refreshing:
                swipeRefreshLayout.finishRefresh(false);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                return;
            case None:
                statusLayout.showError(error);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    public void setAndStartTask(Observable<List> observable, BaseAdapter baseAdapter) {
        this.observable = observable;
        this.baseAdapter = baseAdapter;
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                startTask();
            }
        });

        swipeRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                startTask();
            }
        });
        recyclerView.setAdapter(null);
        startTask();
    }

    public RefreshState getState() {
        return swipeRefreshLayout.getState();
    }

    protected void setOnRetryListener() {
        if (swipeRefreshLayout.getState() != RefreshState.None)
            return;

        if (onRetryListener == null)
            onRetryListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    statusLayout.setOnClickListener(null);
                    startTask();
                }
            };
        statusLayout.setOnClickListener(onRetryListener);
    }

    /**
     * 多数情况下，第一次加载跟刷新的逻辑一样，应该偏移量都是从0开始加载，加载更多是从当前数量开始
     * @return 加载偏移量
     */
    public int getLoadOffset(){
        //需要自己判断是刷新还是加载更多，第一加载算是刷新，跟刷新的操作是一样的。
        int offset = getRecyclerView().getAdapter() != null ? getRecyclerView().getAdapter().getItemCount() : 0;
        if (getState() == RefreshState.Refreshing)
            offset = 0;
        return offset;
    }

    @SuppressWarnings("all")
    public static boolean isNetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = null;

        try {
            info = connectivityManager.getActiveNetworkInfo();
        } catch (Exception var4) {

        }
        return info != null && info.isAvailable();
    }
}