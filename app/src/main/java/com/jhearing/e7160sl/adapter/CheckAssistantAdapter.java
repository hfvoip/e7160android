package com.jhearing.e7160sl.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.model.AssistantInfo;

import org.jetbrains.annotations.NotNull;

/**
 * Create by dongli
 * Create date 2020/8/5
 * desc：
 */
public class CheckAssistantAdapter extends BaseQuickAdapter<AssistantInfo, BaseViewHolder> {


    public CheckAssistantAdapter() {
        super(R.layout.item_check_assistant);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, AssistantInfo assistantInfo) {
        if (!TextUtils.isEmpty(assistantInfo.getTitle())){
            baseViewHolder.setText(R.id.title_tv,assistantInfo.getTitle());
        }
    }
}
