package com.example.helloprocessor;

import com.xsh.adapter.Adapter;
import com.xsh.adapter.BaseAdapter;
import com.xsh.adapter.Holder;


@Adapter(holder = XHolder.class, layout = R.layout.item_a,compoundButton = R.id.radio_button_a)
public class MultiAdapter extends BaseAdapter<String> {

    @SuppressWarnings("unused")
    @Holder
    static Object[][] holders = {
            {XHolder.class, R.layout.item_a, "1"},
            {XHolder.class, R.layout.item_a, "2"},
            {XXHolder.class, R.layout.item_b, "3"},
            {XHolder.class, R.layout.item_a, "4"},
            {XHolder.class, R.layout.item_a, "5"},
    };


    @Override
    protected Object getViewTypeValue(String item) {
        return String.valueOf(Integer.parseInt(item)%5+1);
//        return item;
    }

}