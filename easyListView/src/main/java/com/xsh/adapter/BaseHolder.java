package com.xsh.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;

public class BaseHolder extends RecyclerView.ViewHolder {
    Object object;
    public BaseHolder(@NonNull View itemView) {
        super(itemView);
        initView(null, itemView);
    }


    public void initView(Object object, @NonNull View itemView) {

    }

    public void bindValues(Object object, int position) {
        this.object = object;
    }

}