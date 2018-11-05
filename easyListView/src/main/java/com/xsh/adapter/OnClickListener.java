package com.xsh.adapter;

import android.view.View;

public interface OnClickListener<T> {
    void onClick(View view, int position, T item);
}
