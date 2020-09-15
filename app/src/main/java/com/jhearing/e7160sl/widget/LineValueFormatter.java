package com.jhearing.e7160sl.widget;

/**
 * Create by dongli
 * Create date 2020/8/10
 * desc：
 */

import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;

/**
 * 自定义折线点上的文字,否则仅显示数字:29.00
 */
public class LineValueFormatter extends ValueFormatter {

    private final List<Entry> mLabels;

    /**
     * 构造方法，把数据源传进来
     * @param labels
     */
    public LineValueFormatter(List<Entry> labels) {
        mLabels = labels;
    }

    @Override
    public String getFormattedValue(float value) {

        int left = (int) (Math.floor(value)*5-10);
        if (left <= -10){
            left = -10;
        }else if (left >= 120){
            left = 120;
        }else {
            left = (int) (Math.floor(value)*5-10);
        }
        Log.i("left", "getFormattedValue: " + left);
        return String.valueOf(left);
    }
}