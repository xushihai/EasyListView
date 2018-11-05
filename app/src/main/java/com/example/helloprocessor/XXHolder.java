package com.example.helloprocessor;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.xsh.adapter.BaseHolder;
import com.xsh.adapter.OnClickListener;
import com.xsh.adapter.OnLongClickListener;

public class XXHolder extends BaseHolder {

    public XXHolder(@NonNull View itemView) {
        super(itemView);
    }



    @Override
    public void bindValues(Object object, int positoin) {
        super.bindValues(object, positoin);
        TextView textView = itemView.findViewById(R.id.tv_b);

        if (textView != null) {
            textView.setTextColor(Color.BLUE);
            textView.setTextSize(40);
            textView.setText("XXholder " + object);
        }
    }
}