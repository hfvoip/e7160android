package com.jhearing.e7160sl.widget;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.jhearing.e7160sl.R;

import java.util.ArrayList;

/**
 * Created by xiaoniu on 2017/7/12.
 */

public class TinnitusLineChart extends LineChart implements OnChartGestureListener, OnChartValueSelectedListener {

    private static final String TAG = TinnitusLineChart.class.getSimpleName();;

    private OnSingleTapListener onSingleTapListener;

    private float yTouchPostion;
    private double xValuePos;
    private double yValuePos;
    private int iEntry;
    private float valEntry;
    String arr_freqs[] = {"250","500","750","1khz","1.5k","2khz","4khz","6khz","8khz"};

    private ArrayList<Entry> values;

    public TinnitusLineChart(Context context) {
        super(context);
        initSingleTapLineChart();
    }

    public TinnitusLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSingleTapLineChart();
    }

    public TinnitusLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initSingleTapLineChart();
    }

    private void initSingleTapLineChart() {

        values = new ArrayList<>();

        this.getDescription().setEnabled(false);
        this.setTouchEnabled(false);
        this.setDragEnabled(false);
        this.setScaleEnabled(false);
        this.setPinchZoom(false);
        //是否显示边界
        this.setDrawBorders(false);
        // 设置是否可以触摸
        this.setTouchEnabled(true);
        // 监听触摸事件
        this.setOnChartGestureListener(this);
        this.setOnChartValueSelectedListener(this);
        // 是否可以拖拽
        this.setDragEnabled(true);
        // 是否可以缩放
        this.setScaleEnabled(false);

    }

    public void setChartData(ArrayList<Entry> values) {
        this.values = values;


        //得到X轴
        XAxis xAxis = this.getXAxis();
        //设置X轴的位置（默认在上方)
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置X轴坐标之间的最小间隔
        xAxis.setGranularity(1f);
        //设置X轴的刻度数量，第二个参数为true,将会画出明确数量（带有小数点），但是可能值导致不均匀，默认（6，false）
        //  xAxis.setLabelCount(list.size() / 6, false);
        //设置X轴的值（最小值、最大值、然后会根据设置的刻度数量自动分配刻度显示）
        xAxis.setAxisMinimum(1f);
        xAxis.setAxisMaximum(9f);
        //不显示网格线
        xAxis.setDrawGridLines(false);
        // 标签倾斜
        xAxis.setLabelRotationAngle(-45);
        //设置X轴值为字符串
        xAxis.setValueFormatter(new ValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value) {
                                        int ivalue = (int) value; //1-9
                                        int index = ivalue  -1;
                                        if (index>=0 && index< arr_freqs.length)
                                            return arr_freqs[index] ;
                                        else
                                            return "" ;
                                    }
                                }
        );
        YAxis leftAxis = this.getAxisLeft();
        leftAxis.setEnabled(false); //右侧Y轴不显示
        leftAxis.removeAllLimitLines();
        leftAxis.setAxisMaximum(128);
        leftAxis.setAxisMinimum(0);
        leftAxis.setGranularity(1f);
        leftAxis.enableGridDashedLine(10f, 0f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(false);

        this.getAxisRight().setEnabled(false);

        Legend l = this.getLegend();
        l.setForm(Legend.LegendForm.NONE);


        LineDataSet set1 = new LineDataSet(values, "");

        set1.enableDashedLine(10f, 0f, 0f);
        set1.enableDashedHighlightLine(10f, 0f, 0f);
        set1.setColor(getResources().getColor(R.color.colorAccent));
        set1.setDrawCircles(true);
        set1.setDrawCircleHole(false);//设置曲线值的圆点是实心还是空心
        set1.setCircleRadius(3f);
        set1.setDrawValues(true);
        set1.setCircleColor(getResources().getColor(R.color.colorAccent));
        set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set1.setLineWidth(3f);


        set1.setValueTextSize(9f);
        set1.setDrawFilled(false);
        set1.setFormLineWidth(1f);
        set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set1.setFormSize(15.f);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(dataSets);

        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value ) {
                int eq_value = (int)value ;
                eq_value = eq_value - 128;
                //eq_value  在onsingletap中进行校验

                return  eq_value +"dB";
            }
        });
        //最后调用MPAndroidChart提供的setData方法
        this.setData(data);
    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        this.highlightValues(null);
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i(TAG, "onChartGestureEnd, lastGesture: " + lastPerformedGesture);
        if (lastPerformedGesture == ChartTouchListener.ChartGesture.SINGLE_TAP) {
            Log.i(TAG, "SingleTapped");
            yTouchPostion = me.getY();
            changeTouchEntry();
        }
        this.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        // 获取Entry
        iEntry = (int) e.getX();
        valEntry = e.getY();
        Log.i(TAG, "e.getX() = " + iEntry + "     e.getY() = " + valEntry);
        // 获取选中value的坐标
        MPPointD p = this.getPixelForValues(e.getX(), e.getY(), YAxis.AxisDependency.LEFT);
        xValuePos = Math.round(p.x);
        yValuePos = Math.round(p.y);
        Log.i(TAG, "xValuePos = " + xValuePos + "     yValuePos = " + yValuePos);
    }

    @Override
    public void onNothingSelected() {
    }

    public void changeTouchEntry() {
        // 获取X轴和Y轴的0坐标的pixel值
        MPPointD p = this.getPixelForValues(0, 0, YAxis.AxisDependency.LEFT);
        double yAixs0 = p.y;
        // 修改TouchEntry的y的值
        Log.i(TAG, "计算过程");
        Log.i(TAG, "yAixs0: " + yAixs0);
        double y1 = yValuePos - yAixs0;
        double y2 = yTouchPostion - yAixs0;
        Log.i(TAG, "原来的y值所在的坐标减0点");
        Log.i(TAG, "yValuePos - yAixs0: " + y1);
        Log.i(TAG, "点击的y值所在的坐标减0点");
        Log.i(TAG, "yTouchPostion - yAixs0: " + y2);
        valEntry = (float) (valEntry * (y2 / y1));
        valEntry =(int) Math.round(valEntry);
    //    if (valEntry == 0) valEntry = 1;
       // if (valEntry >=0 ) valEntry = -1;
       // if (valEntry <-128) valEntry = -128;

        Log.i(TAG, "value");
        Log.i(TAG, "X: " + iEntry + " , Y: " + valEntry);
        values.set(iEntry, new Entry(iEntry, valEntry));


        MPPointD tapped = this.getPixelForValues(iEntry, valEntry, YAxis.AxisDependency.LEFT);
        xValuePos = tapped.x;
        yValuePos = tapped.y;

        this.notifyDataSetChanged();
        this.invalidate();

        if (onSingleTapListener != null){
            onSingleTapListener.onSingleTap(iEntry,valEntry);
        }
    }

    public ArrayList<Entry> getValues(){
        return values;
    }

    public void setOnSingleTapListener(OnSingleTapListener onSingleTapListener){
        this.onSingleTapListener = onSingleTapListener;
    }

    public interface OnSingleTapListener{
        void onSingleTap(int x, float y);
    }
}
