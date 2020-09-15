package com.jhearing.e7160sl.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jhearing.e7160sl.R;
import org.jetbrains.annotations.NotNull;

public class ItemAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public ItemAdapter() {
        super(R.layout.item_string);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, String s) {
        baseViewHolder.setText(R.id.textView,s);
    }
}