package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatSpinner;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ark.ArkException;
import com.ark.Parameter;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.github.mikephil.charting.data.Entry;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.Events.WsmessageEvent;

import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;
import com.jhearing.e7160sl.Utils.ParseUrl;
import com.jhearing.e7160sl.model.PatientRecord;
import com.jhearing.e7160sl.widget.SingleTapLineChart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AgFragmentItem<Wsmessage> extends Fragment {

    private static final String TAG = AgFragmentItem.class.getSimpleName();

    private static final int DETECT_DEVICE = 0;
    private static final int INITIALIZE_DEVICE = 1;
    private static final int READ_DEVICE = 2;
    private static final int WRITE_TO_DEVICE = 3;
    private static HearingAidModel.Side side;
    private static boolean isActive = true;
    private static AsyncTask<Void, Integer, Void> initializeSDKParameters;
    private boolean isBusy = false;
    private int m_freq=0;
    private int m_duration = 0;
    private int m_volume = 0;
    private SeekBar seekBarVolume ;
    private AppCompatSpinner  arr_agspinners[];
    private int arr_freq_agvalues[];
    private TextView arr_agtv[];
    private LinearLayout  arr_aglayout[];

    private PatientRecord  m_patient = new PatientRecord();

    private TextView progressBarTextView,dbTextView;

    private ProgressBar progressBar;
    private ArrayList<String> list = new ArrayList<>();

    private ArrayList<Entry> values;

    private String flag = "";
    private SingleTapLineChart leftEarStlc ;
    private OptionsPickerView pvOptions;
    private final int total_ag = 9;




    private View.OnClickListener layoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int ag_idx = 0;
            switch (view.getId()) {
                case R.id.left_Hz_250_ll:
                    ag_idx=0;
                    flag = "left_Hz_250";
                    break;
                case R.id.left_Hz_500_ll:
                    ag_idx=1;
                    flag = "left_Hz_500";
                    break;
                case R.id.left_Hz_750_ll:
                    ag_idx=2;
                    flag = "left_Hz_750";
                    break;
                case R.id.left_Hz_1000_ll:
                    ag_idx=3;
                    flag = "left_Hz_1000";
                    break;
                case R.id.left_Hz_1500_ll:
                    ag_idx=4;
                    flag = "left_Hz_1500";
                    break;
                case R.id.left_Hz_2000_ll:
                    ag_idx=5;
                    flag = "left_Hz_2000";
                    break;
                case R.id.left_Hz_4000_ll:
                    ag_idx=6;
                    flag = "left_Hz_4000";
                    break;
                case R.id.left_Hz_6000_ll:
                    ag_idx=7;
                    flag = "left_Hz_6000";
                    break;
                case R.id.left_Hz_8000_ll:
                    ag_idx=8;
                    flag = "left_Hz_8000";
                    break;
            }
            String trim2 = arr_agtv[ag_idx].getText().toString().trim();
            if (!trim2.equals("")) {
                pvOptions.setSelectOptions(getPosition(trim2));
            } else {
                pvOptions.setSelectOptions(0);
            }
            pvOptions.show();
        }
    };
    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            if (data.address.equals(getHearingAidModel(side).address)) {
                if (!getHearingAidModel(side).connected) {
                    isActiveView(getHearingAidModel(side).connected);
                    initializeSDKParameters.cancel(true);
                } else if (!isActive && !isBusy() && getHearingAidModel(side).connected) {
                    isActiveView(getHearingAidModel(side).connected);
                }
            }
        }
    };



    public AgFragmentItem() {
        // Required empty public constructor
    }

    public boolean isBusy() {
        return isBusy;
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
        //保存下ag
        List<String> list = new ArrayList<String>();
        for (int i =0; i<total_ag;i++)
         list.add(arr_freq_agvalues[i]+"");

        String str_ag = TextUtils.join(",",list );
        m_patient.set_audiogram(side,"ac",str_ag);
        m_patient.save_record_sp(getActivity());
    }

    private Parameter getParameter(HearingAidModel.Side side, String id) {
        Parameter parameter = null;
        try {
            parameter = getHearingAidModel(side).parameters.getById(id);
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
        return parameter;
    }

    private void updateViewValues(HearingAidModel.Side side) {


    }



    private void seekBarListeners() {


    }


    private void getSide() {
        String sideHA = getArguments().getString(getString(R.string.HA_SIDE));
        if (sideHA != null)
            side = (sideHA.equals(HearingAidModel.Side.Left.name())) ? HearingAidModel.Side.Left : HearingAidModel.Side.Right;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getSide();
        View view = inflater.inflate(R.layout.fragment_ag_item, container, false);



        arr_freq_agvalues = new int[total_ag];
        arr_agtv = new TextView[total_ag];
        arr_aglayout = new LinearLayout[total_ag];

        arr_agtv[0] = view.findViewById(R.id.left_Hz_250_tv);

        arr_agtv[1] = view.findViewById(R.id.left_Hz_500_tv);

        arr_agtv[2] = view.findViewById(R.id.left_Hz_750_tv);
        arr_agtv[3] = view.findViewById(R.id.left_Hz_1000_tv);
        arr_agtv[4] = view.findViewById(R.id.left_Hz_1500_tv);
        arr_agtv[5] = view.findViewById(R.id.left_Hz_2000_tv);
        arr_agtv[6] = view.findViewById(R.id.left_Hz_4000_tv);
        arr_agtv[7] = view.findViewById(R.id.left_Hz_6000_tv);
        arr_agtv[8] = view.findViewById(R.id.left_Hz_8000_tv);

        arr_aglayout[0] =  view.findViewById(R.id.left_Hz_250_ll);
        arr_aglayout[1] =  view.findViewById(R.id.left_Hz_500_ll);
        arr_aglayout[2] =  view.findViewById(R.id.left_Hz_750_ll);
        arr_aglayout[3] =  view.findViewById(R.id.left_Hz_1000_ll);
        arr_aglayout[4] =  view.findViewById(R.id.left_Hz_1500_ll);
        arr_aglayout[5] =  view.findViewById(R.id.left_Hz_2000_ll);
        arr_aglayout[6] =  view.findViewById(R.id.left_Hz_4000_ll);
        arr_aglayout[7] =  view.findViewById(R.id.left_Hz_6000_ll);
        arr_aglayout[8] =  view.findViewById(R.id.left_Hz_8000_ll);

        leftEarStlc = view.findViewById(R.id.left_ear_stlc);
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



        for (int i=0;i<total_ag;i++) {
            arr_aglayout[i].setOnClickListener(layoutListener);

            arr_agtv[i].setText(arr_freq_agvalues[i]+" dB");



        }

        initDb();

        values = new ArrayList<>();
        for (int i =0;i<total_ag;i++) {
            //从 -10 间隔5, -10,-5,0,5,10,15,20,....
            int axis_y = (arr_freq_agvalues[i] -(-10)) /5;
            //在singletaplinechart 中规定了axis_x 和values 的下标一样，也就是从0开始

            int axis_x = i ;

            Entry new_entry =new Entry(axis_x,axis_y);
            values.add(new_entry);
        }


        leftEarStlc.setChartData(values);


        leftEarStlc.setOnSingleTapListener(new SingleTapLineChart.OnSingleTapListener() {
            @Override
            public void onSingleTap(int x, float y) {
                Log.i(TAG, "x = " + x + " y = " + Math.floor(y));
                int left = (int) (Math.floor(y)*5-10);
                if (left <= -10){
                    left = -10;
                }else if (left >= 120){
                    left = 120;
                }else {
                    left = (int) (Math.floor(y)*5-10);;
                }
                if (x>=0) {
                    arr_agtv[x ].setText(String.valueOf(left) + "dB");
                    arr_freq_agvalues[x] = left;
                }



            }
        });



        return view;
    }

    private void progressView(int visibility) {

    }

    private void isActiveView(boolean isActive) {
        AgFragmentItem.isActive = isActive;

    }
    private int  OptionSelecttoDb(String str) {
        String str2 = str.replace(" dB","");
        int tmp_i = 20;
        try {
            tmp_i = Integer.parseInt(str2);
        }catch (Exception e) {

        }
        return tmp_i;
    }
    private void initDb() {
        for (int i = -10; i <= 120; i += 5) {
            list.add(i+" dB");
        }


        //创建
        pvOptions = new OptionsPickerBuilder(getActivity(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                int ag_idx = 0;
                if (flag.equals("left_Hz_250")) {
                    ag_idx =0;

                } else if (flag.equals("left_Hz_500")) {
                    ag_idx =1;

                }else if (flag.equals("left_Hz_750")) {
                    ag_idx =2;

                }else if (flag.equals("left_Hz_1000")) {
                    ag_idx =3;

                }else if (flag.equals("left_Hz_1500")) {
                    ag_idx =4;

                }else if (flag.equals("left_Hz_2000")) {
                    ag_idx =5;

                }else if (flag.equals("left_Hz_4000")) {
                    ag_idx =6;

                }else if (flag.equals("left_Hz_6000")) {
                    ag_idx =7;

                }else if (flag.equals("left_Hz_8000")) {
                    ag_idx =8;


                }

                arr_freq_agvalues[ag_idx] =  OptionSelecttoDb(list.get(options1) );
                arr_agtv[ag_idx].setText(list.get(options1));
                Entry entry = values.get(ag_idx);
                entry.setY(options1);
                values.set(ag_idx,entry);
                leftEarStlc.changeTouchEntry();


            }
        }).setSelectOptions(0)//设置选择第一个
                .setOutSideCancelable(false)//点击背的地方不消失
                .build();


//      把数据绑定到控件上面
//      把数据绑定到控件上面
        pvOptions.setPicker(list);
     //   pvOptions2.setPicker(list);
    }

    public int getPosition(String s) {
        int flag = 0;
        for (int i = 0; i < list.size(); i++) {
            if (s.equals(list.get(i))) {
                flag = i;
                return flag;
            }

        }
        return flag;
    }

    private HearingAidModel getHearingAidModel(HearingAidModel.Side side) {

        return Configuration.instance().getDescriptor(side);
    }




}
