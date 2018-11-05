package com.example.helloprocessor;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.xsh.adapter.BaseHolder;

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
            textView.setText("Hello " + object );
        }

    }
}
