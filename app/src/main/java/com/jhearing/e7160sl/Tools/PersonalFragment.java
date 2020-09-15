package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bryant.selectorlibrary.DSelectorPopup;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.MainActivity;
import com.jhearing.e7160sl.Tools.AgActivity;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;
import com.jhearing.e7160sl.adapter.CheckAssistantAdapter;
import com.jhearing.e7160sl.model.AssistantInfo;
import com.jhearing.e7160sl.model.PatientRecord;
import com.jhearing.e7160sl.widget.LineValueFormatter;
import com.jhearing.e7160sl.widget.SingleTapLineChart;

import java.util.ArrayList;
import java.util.List;

public class PersonalFragment extends Fragment   {

    private static final String TAG = PersonalFragment.class.getSimpleName();

    TextView ageTv;

    LinearLayout ageLl;

    TextView genderTv;
    TextView illtype_tv;

    LinearLayout genderLl;

    LinearLayout listeningLl;
    LinearLayout illtypeL1;

    LinearLayout verificationRecordLl;
    EditText  et_lastname,et_firstname,et_phone;

    ArrayList<String> list_age = new ArrayList<>();
    ArrayList<String> list_gender = new ArrayList<>();
    ArrayList<String> list_illtype = new ArrayList<>();

    private OptionsPickerView pv_age,pv_gender,pv_illtype;
    private LineChart lineChart ;

    private HearingAidModel.Side side ;
    String arr_freqs[] = {"250","500","750","1khz","1.5k","2khz","4khz","6khz","8khz"};
    private int total_ag=13;
    private int arr_freq_agvalues[];
    private ArrayList<Entry> values;

    private float yTouchPostion;
    private double xValuePos;
    private double yValuePos;
    private int iEntry;
    private float valEntry;


    private PatientRecord  m_patient = new PatientRecord();



    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            onConfigurationChanged(HearingAidModel.Side.Left, data.address);
            onConfigurationChanged(HearingAidModel.Side.Right, data.address);
        }
    };

    public PersonalFragment() {
        // Required empty public constructor

    }

    private void onConfigurationChanged(HearingAidModel.Side side, String address) {
        if (Configuration.instance().isHANotNull(side)) {
            if (address.equals(getHearingAid(side).address)) {
                Log.i(TAG, "Connection Status" + getHearingAid(side).connectionStatus);
                connectedView(side);
            }
        }
    }

    private HearingAidModel getHearingAid(HearingAidModel.Side side) {
        return Configuration.instance().getDescriptor(side);
    }

    private void connectedView(HearingAidModel.Side side) {


    }

    private void unregister() {
        EventBus.unregisterReceiver(configurationChangedEventEventHandler);
    }

    private void register() {
        EventBus.registerReceiver(configurationChangedEventEventHandler, ConfigurationChangedEvent.class.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        register();

    }

    @Override
    public void onPause() {
        super.onPause();
        unregister();
        //保存进sp

        //保存下ag
        List<String> list = new ArrayList<String>();
        for (int i =0; i<total_ag;i++)
            list.add(arr_freq_agvalues[i]+"");

        String str_ag = TextUtils.join(",",list );
        m_patient.set_audiogram(side,"ac",str_ag);

        m_patient.save_record_sp(getActivity());

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_personal_data, container, false);
        ageTv = view.findViewById(R.id.age_tv);
        ageLl = view.findViewById(R.id.age_ll);
        genderTv = view.findViewById(R.id.gender_tv);
        genderLl = view.findViewById(R.id.gender_ll);
        illtype_tv = view.findViewById(R.id.illtype_tv);
        illtypeL1 = view.findViewById(R.id.illtype_ll);
        et_lastname  = view.findViewById(R.id.et_lastname_content);
        et_firstname  = view.findViewById(R.id.et_firstname_content);
        et_phone = view.findViewById(R.id.et_phone);

        verificationRecordLl = view.findViewById(R.id.verification_record_ll);
        lineChart = (LineChart) view.findViewById(R.id.left_ear_stlc);
        side = HearingAidModel.Side.Left;

        arr_freq_agvalues = new int[total_ag];
        m_patient.load_record_sp(getActivity());


        initGender();
        initAge();
        initIlltype();
        initFirstname();
        initLastname();
        initPhone();

        loadData();

        return view;
    }


    public void  loadData(){



        m_patient.load_record_sp(getActivity());

        String str_ag =   m_patient.get_audiogram(side,"ac");
        String[] arr_temp_db  = str_ag.split(",");
        int len_arr = arr_temp_db.length;

        for (int i =0;i<total_ag;i++) {
            int tmp_i = 20;
            if (i< len_arr ) {
                try {
                    tmp_i = Integer.parseInt(arr_temp_db[i]);
                } catch( Exception e) {
                    tmp_i = 20;
                }

            } else
                tmp_i = 20;

            arr_freq_agvalues[i] = tmp_i;

        }




        values = new ArrayList<>();
        for (int i =0;i<total_ag;i++) {
            //从 -10 间隔5, -10,-5,0,5,10,15,20,....
            int axis_y = (arr_freq_agvalues[i] -(-10)) /5;
            //在singletaplinechart 中规定了axis_x 和values 的下标一样，也就是从0开始

            int axis_x = i ;

            Entry new_entry =new Entry(axis_x,axis_y);
            values.add(new_entry);
        }


        //leftEarStlc.setChartData(values);

        // 设置是否可以触摸
        lineChart.setTouchEnabled(true);
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                iEntry = (int) e.getX();
                valEntry = e.getY();
                Log.i(TAG, "e.getX() = " + iEntry + "     e.getY() = " + valEntry);
                // 获取选中value的坐标
                MPPointD p = lineChart.getPixelForValues(e.getX(), e.getY(), YAxis.AxisDependency.LEFT);
                xValuePos = p.x;
                yValuePos = p.y;
                Log.i(TAG, "xValuePos = " + xValuePos + "     yValuePos = " + yValuePos);
            }

            @Override
            public void onNothingSelected() {

            }
        });
        // 监听触摸事件
        lineChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                Log.i(TAG, "onChartGestureEnd, lastGesture: " + lastPerformedGesture);
                if (lastPerformedGesture == ChartTouchListener.ChartGesture.SINGLE_TAP) {
                    Log.i(TAG, "SingleTapped");
                    double yTouchPostion = me.getY();
                    ViewPortHandler handler = lineChart.getViewPortHandler();

                    MPPointD topLeft = lineChart.getValuesByTouchPoint(handler.contentLeft(), handler.contentTop(), YAxis.AxisDependency.LEFT);
                    MPPointD bottomRight = lineChart.getValuesByTouchPoint(handler.contentRight(), handler.contentBottom(), YAxis.AxisDependency.LEFT);

                    MPPointD p = lineChart.getPixelForValues(0, 0, YAxis.AxisDependency.LEFT);
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
                    Log.i(TAG, "value");
                    Log.i(TAG, "X: " + iEntry + "     Y: " + valEntry);
                    values.set(iEntry, new Entry(iEntry, valEntry));



                    MPPointD tapped = lineChart.getPixelForValues(iEntry, valEntry, YAxis.AxisDependency.LEFT);

                    xValuePos = tapped.x;
                    yValuePos = tapped.y;

                    lineChart.notifyDataSetChanged();
                    lineChart.invalidate();

                    //保存到 arr_freq_agvalues

                    int left = (int) (Math.floor(valEntry)*5-10);
                    if (left <= -10){
                        left = -10;
                    }else if (left >= 120){
                        left = 120;
                    }else {
                        left = (int) (Math.floor(valEntry)*5-10);;
                    }
                    arr_freq_agvalues[iEntry] = left;

                }
                lineChart.highlightValues(null);
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
        });


        //显示边界
        lineChart.setDrawBorders(false);

        //一个LineDataSet就是一条线
        LineDataSet lineDataSet = new LineDataSet(values, "");
        //线颜色
        lineDataSet.setColor(Color.parseColor("#336699"));
        //线宽度
        lineDataSet.setLineWidth(1.6f);
        //不显示圆点
        lineDataSet.setDrawCircles(true);
        lineDataSet.setCircleColor(Color.parseColor("#336699"));
        lineDataSet.setDrawCircleHole(false);//设置曲线值的圆点是实心还是空心
        lineDataSet.setCircleRadius(8f);
        lineDataSet.setValueFormatter(new LineValueFormatter(values));
        //线条平滑
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        //设置折线图填充
//        lineDataSet.setDrawFilled(true);
        LineData data = new LineData(lineDataSet);
        //无数据时显示的文字
        lineChart.setNoDataText("暂无数据");
        //折线图不显示数值
        data.setDrawValues(true);
        //得到X轴
        XAxis xAxis = lineChart.getXAxis();
        //设置X轴的位置（默认在上方)
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置X轴坐标之间的最小间隔
        xAxis.setGranularity(1f);
        //设置X轴的刻度数量，第二个参数为true,将会画出明确数量（带有小数点），但是可能值导致不均匀，默认（6，false）
      //  xAxis.setLabelCount(list.size() / 6, false);
        //设置X轴的值（最小值、最大值、然后会根据设置的刻度数量自动分配刻度显示）
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(8f);
        //不显示网格线
        xAxis.setDrawGridLines(false);
        // 标签倾斜
        xAxis.setLabelRotationAngle(-45);
        //设置X轴值为字符串
        xAxis.setValueFormatter(new ValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value) {
                                        int ivalue = (int) value;
                                         if (ivalue>=0 && ivalue< arr_freqs.length)
                                           return arr_freqs[ivalue] ;
                                        else
                                            return "" ;
                                    }
                                }
        );


        //得到Y轴
        YAxis yAxis = lineChart.getAxisLeft();
        YAxis rightYAxis = lineChart.getAxisRight();
        //设置Y轴是否显示
        rightYAxis.setEnabled(false); //右侧Y轴不显示
        //设置y轴坐标之间的最小间隔
        //不显示网格线
        yAxis.setDrawGridLines(false);
        //设置Y轴坐标之间的最小间隔
        yAxis.setGranularity(1);
        //设置y轴的刻度数量
        //+2：最大值n就有n+1个刻度，在加上y轴多一个单位长度，为了好看，so+2
        yAxis.setLabelCount(8, false);
        //设置从Y轴值
        yAxis.setAxisMinimum(-1f);
        //+1:y轴多一个单位长度，为了好看
        yAxis.setAxisMaximum(29f);

        //y轴
        yAxis.setValueFormatter(new ValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value) {
                                        int ivalue = (int) value;
                                        int newval =  (ivalue *5 -10);
                                        return ""+newval;

                                    }
                                }
        );


        //图例：得到Lengend
        Legend legend = lineChart.getLegend();
        //隐藏Lengend
        legend.setEnabled(false);
        //隐藏描述
        Description description = new Description();
        description.setEnabled(false);
        description.setText("听力图");

        lineChart.setDescription(description);


        //设置数据
        lineChart.setData(data);
        //图标刷新
        lineChart.invalidate();


    }
    private void initFirstname() {
        et_firstname.setText(m_patient.getFirstname());
        et_firstname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                    String firstname   = s.toString();
                    m_patient.setFirstname(firstname);
            }
        });


    }
    private void initLastname() {
        et_lastname.setText(m_patient.getLastname());
        et_lastname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String lastname   = s.toString();
                m_patient.setLastname(lastname);
            }
        });


    }
    private void initPhone() {
        et_phone.setText(m_patient.getContactphone());
        et_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String phone   = s.toString();
                m_patient.setContactphone(phone);
            }
        });


    }
    private void initGender() {


        genderLl.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
              //  pv_gender.show();
            }
        });

        list_gender.add("男");
        list_gender.add( "女");
        pv_gender = new OptionsPickerBuilder(getActivity(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                genderTv.setText(list_gender.get(options1));
                //options1 和 gender 相差了1
                int sex = options1+1;
                m_patient.setSex(sex);


            }
        }).setSelectOptions(0)//设置选择第一个
                .setOutSideCancelable(false)//点击背的地方不消失
                .build();
//      把数据绑定到控件上面
        pv_gender.setPicker(list_gender);

        int sex = m_patient.getSex();
        if (sex <1)  sex=1;
        if (sex>2)  sex=2;
        pv_gender.setSelectOptions(sex-1);
        String textval = list_gender.get(sex-1);
        if (textval != null)
            genderTv.setText(textval);

    }

    private void initAge() {
        ageLl.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            //    pv_age.show();
            }
        });
        for (int i = 1; i <= 100; i++) {
            list_age.add(""+i);
        }
        pv_age = new OptionsPickerBuilder(getActivity(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                ageTv.setText(list_age.get(options1));
                int age = options1+1;
                m_patient.setSex(age);


            }
        }).setSelectOptions(0)//设置选择第一个
                .setOutSideCancelable(false)//点击背的地方不消失
                .build();
//      把数据绑定到控件上面
        pv_age.setPicker(list_age);

        int age = m_patient.getAge();
        if (age <1)  age=1;
        if (age >100) age=100;

        pv_age.setSelectOptions(age-1);
        String textval = list_age.get(age-1);
        if (textval != null)
            ageTv.setText(textval);

    }
    private void initIlltype() {

        illtypeL1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            //    pv_illtype.show();
            }
        });

        list_illtype.add("传导性");
        list_illtype.add("神经性");
        list_illtype.add("混合型");

        pv_illtype = new OptionsPickerBuilder(getActivity(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                illtype_tv.setText(list_illtype.get(options1));
                int illtype = options1 ;
                m_patient.setIlltype(illtype);


            }
        }).setSelectOptions(0)//设置选择第一个
                .setOutSideCancelable(false)//点击背的地方不消失
                .build();
//      把数据绑定到控件上面
        pv_illtype.setPicker(list_illtype);

        int illtype = m_patient.getIlltype();
        if (illtype<0)  illtype=0;
        if (illtype>2)   illtype=2;

        pv_illtype.setSelectOptions(illtype);
        String textval = list_illtype.get( illtype);
        if (textval != null)
            illtype_tv.setText(textval);




    }



}
