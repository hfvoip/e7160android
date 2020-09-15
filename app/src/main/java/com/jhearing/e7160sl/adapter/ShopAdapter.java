package com.jhearing.e7160sl.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.model.shopInfo;

import org.jetbrains.annotations.NotNull;

/**
 * Create by dongli
 * Create date 2020/8/5
 * desc：
 */
public class ShopAdapter extends BaseQuickAdapter<shopInfo, BaseViewHolder>   {

    public ShopAdapter() {
        super(R.layout.item_shop_info);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, shopInfo shopinfo) {

        if (!TextUtils.isEmpty(shopinfo.getTitle())){
            baseViewHolder.setText(R.id.shop_name_tv,shopinfo.getTitle());
        }

        if (!TextUtils.isEmpty(shopinfo.getAddress())){
            baseViewHolder.setText(R.id.shop_address_tv,shopinfo.getAddress());
        }

        if (!TextUtils.isEmpty(shopinfo.getContent())){
            baseViewHolder.setText(R.id.shop_desc_tv,shopinfo.getContent());
        }


    }
}
