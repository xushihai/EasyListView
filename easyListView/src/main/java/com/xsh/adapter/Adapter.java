package com.xsh.adapter;


import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Adapter {
    Class<? extends BaseHolder> holder();

    @LayoutRes int layout();

    @IdRes int compoundButton() default -1;//多选时的view的ID
    
}
