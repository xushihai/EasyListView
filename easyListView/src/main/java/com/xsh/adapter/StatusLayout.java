package com.xsh.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

public class StatusLayout extends FrameLayout {
    ImageView ivStatus;
    TextView tvStatus;
    View loadingIndicatorView;

    public StatusLayout(Context context) {
        super(context);

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        ivStatus = new ImageView(getContext());
        tvStatus = new TextView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = getResources().getDimensionPixelOffset(R.dimen.status_layout_text_margin);
        tvStatus.setLayoutParams(layoutParams);

        loadingIndicatorView = inflate(getContext(), R.layout.layout_loading_view, null);
        linearLayout.addView(loadingIndicatorView);
        linearLayout.addView(ivStatus);
        linearLayout.addView(tvStatus);
        addView(linearLayout);
    }

    public View getLoadingIndicatorView() {
        return loadingIndicatorView;
    }

    public void setIndicator(String indicatorName) {
        setIndicator(indicatorName, -1);
    }

    public void setIndicator(String indicatorName, String color) {
        setIndicator(indicatorName, Color.parseColor(color));
    }

    public void setIndicatorRes(String indicatorName, @ColorRes int color) {
        setIndicator(indicatorName, getResources().getColor(color));
    }

    public void setIndicator(String indicatorName, int color) {
        if (!(loadingIndicatorView instanceof AVLoadingIndicatorView)) {
            Log.e("EasyListView", "loadingIndicatorView不是AVLoadingIndicatorView的实例");
            return;
        }


        AVLoadingIndicatorView avLoadingIndicatorView = ((AVLoadingIndicatorView) loadingIndicatorView);
        avLoadingIndicatorView.setIndicator(indicatorName);
        avLoadingIndicatorView.setIndicator(avLoadingIndicatorView.getIndicator());
        if (color != -1)
            avLoadingIndicatorView.setIndicatorColor(color);
    }

    public void showError(String error) {
        if (getVisibility() != VISIBLE)
            setVisibility(VISIBLE);
        loadingIndicatorView.setVisibility(GONE);
        tvStatus.setText(R.string.status_connect_fail);
        tvStatus.setText(error);
        ivStatus.setImageResource(R.mipmap.ic_status_network_error);
    }

    public void showEmpty() {
        if (getVisibility() != VISIBLE)
            setVisibility(VISIBLE);
        loadingIndicatorView.setVisibility(GONE);
        tvStatus.setText(R.string.status_data_empty);
        ivStatus.setImageResource(R.mipmap.ic_status_empty);
    }

    public void showLoading() {
        if (getVisibility() != VISIBLE)
            setVisibility(VISIBLE);
        loadingIndicatorView.setVisibility(VISIBLE);
        tvStatus.setText(R.string.status_loading);
        ivStatus.setImageBitmap(null);
    }

    public boolean isLoading() {
        return loadingIndicatorView.getVisibility() == VISIBLE;
    }

    public void hide() {
        loadingIndicatorView.setVisibility(GONE);
        setVisibility(GONE);
    }
}
