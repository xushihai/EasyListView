package com.xsh.adapter;

import android.view.View;

public interface OnLongClickListener<T> {
    void onLongClick(View view, int position, T item);
}
