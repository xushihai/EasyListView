package com.xsh.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseAdapter<E> extends RecyclerView.Adapter<BaseHolder> {
    List<E> data;

    SparseArray holderArray = new SparseArray();
    OnClickListener onClickListener;
    OnLongClickListener onLongClickListener;
    final String TAG = "EasyListView";

    Mode mode = Mode.NONE;
    SparseBooleanArray checkList = new SparseBooleanArray();
    int compoundButton = 0;
    OnSingleChoiceListener onSingleChoiceListener;
    OnMultiChoiceListener onMultiChoiceListener;
    Context mContext;

    public BaseAdapter() {
        parseInject();
    }


    /**
     * 切换模式后之前的状态会清理掉
     *
     * @param mode 切换后的模式
     */
    public void setMode(Mode mode) {
        this.mode = mode;
        checkList.clear();
    }

    public Mode getMode() {
        return mode;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (mContext == null)
            mContext = viewGroup.getContext();

        Object[] holderInfo = (Object[]) holderArray.get(viewType);
        if (holderInfo == null) {
            //  log("viewType:" + viewType + "没有找到对应的配置信息");
            log(R.string.adapter_viewtype_miss_match, viewType);
            return new BaseHolder(new View(viewGroup.getContext()));
        }

        Class holderCLass = (Class) holderInfo[0];
        int layoutId = (int) holderInfo[1];

        try {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            BaseHolder baseHolder = (BaseHolder) holderCLass.getConstructor(View.class).newInstance(view);
            if (onClickListener != null)
                baseHolder.itemView.setOnClickListener(new OnClickListenerImpl(baseHolder, onClickListener, this));
            if (onLongClickListener != null)
                baseHolder.itemView.setOnLongClickListener(new OnLongClickListenerImpl(baseHolder, onLongClickListener, this));
            return baseHolder;
        } catch (Resources.NotFoundException notFoundException) {
//            log("找不到布局文件，Resource ID：" + layoutId);
            log(R.string.adapter_layout_not_found, layoutId);
        } catch (Exception e) {
            e.printStackTrace();
//            log(holderCLass.getCanonicalName() + "不是正确的Holder");
            log(R.string.adapter_holder_error,holderCLass.getCanonicalName() );
        }
        return new BaseHolder(new View(viewGroup.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder t, final int position) {
        t.bindValues(getItem(position), position);
        injectcompoundButton(t, position);
    }

    @SuppressWarnings("unchecked")
    private void injectcompoundButton(@NonNull BaseHolder t, final int position) {
        if (compoundButton == 0) {
            Adapter adapter = getClass().getAnnotation(Adapter.class);
            if (adapter != null) {
                compoundButton = adapter.compoundButton();
            }
        }

        if (compoundButton != -1) {
            try {
                CompoundButton button = t.itemView.findViewById(compoundButton);
                if (button == null)
                    return;
                button.setVisibility(mode.mode != Mode.NONE.mode ? View.VISIBLE : View.INVISIBLE);
                button.setChecked(checkList.get(position, false));

                if (mode.mode == Mode.NONE.mode) {
                    return;
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isChecked = checkList.get(position, false);

                        switch (mode) {
                            case MULTI:
                                if (!isChecked) {
                                    checkList.put(position, true);
                                } else {
                                    checkList.delete(position);
                                }
                                notifyItemChanged(position);
                                if (onMultiChoiceListener != null)
                                    onMultiChoiceListener.onMultiChoiceChanged(checkList.size(), getItemCount());
                                break;
                            case SINGLE:
                                if (isChecked)
                                    break;

                                if (onSingleChoiceListener != null)
                                    onSingleChoiceListener.onSingleChoiceChanged(position, getItem(position));
                                if (checkList.size() == 0) {
                                    checkList.put(position, true);
                                    break;
                                }

                                int previousIndex = checkList.keyAt(0);
                                checkList.clear();
                                notifyItemChanged(previousIndex);

                                checkList.put(position, true);
                                // notifyItemChanged(position);
                                break;
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return 返回被选中的那些数据的实际值
     */
    @SuppressWarnings("unused")
    public List<E> getMulitiResult() {
        if (mode.mode != Mode.MULTI.mode) {
            log(R.string.adapter_multi_select_error);
            return null;
        }
        List<E> result = new ArrayList<>();
        for (int i = 0; i < checkList.size(); i++) {
            int key = checkList.keyAt(i);
            if (checkList.valueAt(i) && key < getItemCount() && key >= 0)
                result.add(getItem(key));
        }
        return result;
    }

    /**
     * 许多选择删除的功能需要这样的功能
     *
     * @return 返回被选中的那些的在adapter中的位置
     */
    public List<Integer> getMulitiResultIndex() {
        if (mode.mode != Mode.MULTI.mode) {
//            log("不是多选模式，不支持获取多选结果操作");
            log(R.string.adapter_multi_select_error);
            return null;
        }
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < checkList.size(); i++) {
            int key = checkList.keyAt(i);
            if (checkList.valueAt(i) && key < getItemCount() && key >= 0)
                result.add(key);
        }
        return result;
    }

    public void selectAll() {
        if (mode.mode != Mode.MULTI.mode) {
            log(R.string.adapter_select_all_error);
//            log("不是多选模式，不支持全选操作");
            return;
        }
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            checkList.put(i, true);
        }
        notifyDataSetChanged();

        if (onMultiChoiceListener != null)
            onMultiChoiceListener.onMultiChoiceChanged(checkList.size(), getItemCount());
    }

    @SuppressWarnings("unused")
    public void selectNone() {
        if (mode.mode == Mode.NONE.mode) {
            log(R.string.adapter_select_none_error);
            return;
        }
        checkList.clear();
        notifyDataSetChanged();
        if (onMultiChoiceListener != null)
            onMultiChoiceListener.onMultiChoiceChanged(checkList.size(), getItemCount());
    }

    public void setOnSingleChoiceListener(OnSingleChoiceListener onSingleChoiceListener) {
        this.onSingleChoiceListener = onSingleChoiceListener;
    }

    public void setOnMultiChoiceListener(OnMultiChoiceListener onMultiChoiceListener) {
        this.onMultiChoiceListener = onMultiChoiceListener;
    }

    @SuppressWarnings("unchecked")
    private void parseInject() {
        try {

            Field[] fields = getClass().getDeclaredFields();
            for (Field field :
                    fields) {
                if (field.isAnnotationPresent(Holder.class)) {
                    field.setAccessible(true);
                    Object[][] holders = (Object[][]) field.get(this);
                    for (Object[] obj :
                            holders) {

                        if (!(obj[0] instanceof Class)) {
                            log(R.string.adapter_annotation_argument_not_class,obj[0] );
//                            log("解析注解失败，Holder注解数据格式不对，" + obj[0] + "不是CLass类型");
                            return;
                        }

                        if (!(obj[1] instanceof Integer)) {
                            log(R.string.adapter_annotation_argument_not_int, obj[1] );
                           // log("解析注解失败，Holder注解数据格式不对，" + obj[1] + "不是int类型");
                            return;
                        }

                        int viewType = obj.length >= 3 ? obj[2].hashCode() : 0;
                        holderArray.put(viewType, obj);
                    }
                    return;
                }
            }


            Class baseAdapterCLass = Class.forName("com.xsh.BaseAdapter");
            Object baseASdaperObject = baseAdapterCLass.newInstance();
            Map<String, String> data = (Map<String, String>) baseAdapterCLass.getField("data").get(baseASdaperObject);

            String json = String.valueOf(data.get(this.getClass().getCanonicalName()));
            JSONObject jsonObject = new JSONObject(json);
            Class holderCLass = Class.forName(jsonObject.getString("holder"));
            int layoutId = jsonObject.getInt("layout");

            holderArray.put(0, new Object[]{holderCLass, layoutId});

        } catch (Exception e) {
            log(e.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public E getItem(int position) {
        return data.get(position);
    }


    /**
     * 有多视图需求的时候，通过该方法返回一个区分不同视图类型的数据。
     * 如果是单视图就没必要重写这个方法
     *
     * @param item 第i项的数据
     * @return 当前数据用于匹配视图类型的值
     */
    protected Object getViewTypeValue(E item) {
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        Object viewType = getViewTypeValue(getItem(position));
        if (viewType == null)
            return super.getItemViewType(position);
        return viewType.hashCode();
    }


    public List<E> getData() {
        return data;
    }

    public void setData(List<E> data) {
        if (data != null)
            this.data = data;
    }


    private void log(String log) {
        Log.e(TAG, log);
    }

    private void log(@StringRes int log) {
        if (mContext != null)
            Log.e(TAG, mContext.getString(log));
    }

    private void log(@StringRes int log, Object... values) {
        if (mContext != null)
            Log.e(TAG, String.format(mContext.getString(log), values));
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }


    private static class OnClickListenerImpl implements View.OnClickListener {
        RecyclerView.ViewHolder viewHolder;
        OnClickListener onClickListener;
        BaseAdapter baseAdapter;

        public OnClickListenerImpl(RecyclerView.ViewHolder viewHolder, OnClickListener onClickListener, BaseAdapter baseAdapter) {
            this.viewHolder = viewHolder;
            this.onClickListener = onClickListener;
            this.baseAdapter = baseAdapter;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onClick(View view) {
            if (onClickListener != null && baseAdapter.mode.mode == Mode.NONE.mode) {
                int position = viewHolder.getAdapterPosition();
                onClickListener.onClick(view, position, baseAdapter.getItem(position));
            }
        }
    }

    private static class OnLongClickListenerImpl implements View.OnLongClickListener {
        RecyclerView.ViewHolder viewHolder;
        OnLongClickListener onLongClickListener;
        BaseAdapter baseAdapter;

        public OnLongClickListenerImpl(RecyclerView.ViewHolder viewHolder, OnLongClickListener onLongClickListener, BaseAdapter baseAdapter) {
            this.viewHolder = viewHolder;
            this.onLongClickListener = onLongClickListener;
            this.baseAdapter = baseAdapter;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean onLongClick(View view) {
            if (onLongClickListener != null && baseAdapter.mode.mode == Mode.NONE.mode) {
                int position = viewHolder.getAdapterPosition();
                onLongClickListener.onLongClick(view, position, baseAdapter.getItem(position));
            }
            return true;
        }
    }

    public interface OnSingleChoiceListener<T> {
        void onSingleChoiceChanged(int checkPosition, T item);
    }

    public interface OnMultiChoiceListener {
        void onMultiChoiceChanged(int checkSum, int total);
    }

    public enum Mode {

        NONE(0),
        SINGLE(1),
        MULTI(2);

        public int mode;

        Mode(int mode) {
            this.mode = mode;
        }
    }
}
