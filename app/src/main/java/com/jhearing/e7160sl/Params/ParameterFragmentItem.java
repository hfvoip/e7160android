package com.jhearing.e7160sl.Params;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.ark.ArkException;
import com.ark.AsyncResult;
import com.ark.CommunicationAdaptor;
import com.ark.DeviceInfo;
import com.ark.IndexedList;
import com.ark.IndexedTextList;
import com.ark.Parameter;
import com.ark.ParameterSpace;
import com.ark.Product;
import com.github.mikephil.charting.data.Entry;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.jhearing.e7160sl.widget.SingleTapLineChart;
import com.jhearing.e7160sl.widget.ThreeTapLineChart;
import com.jhearing.e7160sl.widget.TinnitusLineChart;
import com.jhearing.e7160sl.widget.WdrcLineChart;


import java.util.ArrayList;


public class ParameterFragmentItem extends Fragment {

    private static final String TAG = ParameterSpace.class.getSimpleName();
    private static final int DETECT_DEVICE = 0;
    private static final int INITIALIZE_DEVICE = 1;
    private static final int READ_DEVICE = 2;
    private static final int READ_DEVICE_SYS = 4;

    private static final int WRITE_TO_DEVICE = 3;
    private static HearingAidModel.Side side;
    private static boolean isActive = true;
    private static AsyncTask<Void, Integer, Void> initializeSDKParameters;
    private static AsyncTask<Void, Integer, Void> initializeSDKParameters2;

    private boolean isBusy = false;
    private Switch enableEQ, enableNR,enableFBC,enableTinnitus,enableAutomemory;
    private int current_memory_idx;

    private SeekBar seekBarBass, seekBarMid, seekBarTreble,seekBarWideband,seekBarllgain,SeekBarHlgain,SeekBarMensharp,SeekBarOwnvoice,seekBarSglevel;

    private TextView progressBarTextView,microphone_setting_tv,sg_centerband_tv,sg_bandwidth_tv;

    private LinearLayout optgroup_sg_ll ;
    private LinearLayout sg_enable_ll  ;
    private LinearLayout sg_bandwidth_ll  ;
    private LinearLayout sg_centerband_ll  ;
    private LinearLayout sg_sglevel_ll ;
    private   LinearLayout optgroup_eq_ll ;
    private LinearLayout left_ear_stlc_ll  ;

    private LinearLayout optgroup_wdrc_ll  ;
    private LinearLayout smallbig_wdrc_ll  ;
    private  LinearLayout voicequality_ll  ;

    private  LinearLayout optgroup_modulesetting_ll  ;
    private LinearLayout modulesetting_ll ;

    private CardView cardview_audio,cardview_eq,cardview_sg,cardview_setting,cardview_user;

    private TextView tv_hearingaid_autoec,tv_hearingaid_id,tv_hearingaid_battery,tv_hearingaid_memory;
    private TextView tv_hearingaid_noisereduction,tv_hearingaid_sgmode,tv_hearingaid_auxinput,tv_hearingaid_fbc;
    private TextView tv_hearingaid_femode;

    private String page1;
    private Button btn_voicesharp,btn_voicemen;
    private ProgressBar progressBar;
    private ProgressDialog progressDlg;
    private ArrayList<Entry> values;
    private ArrayList<Entry> values_sg;

    private int  arr_eq_dbs[];
    private int arr_wdrc_dbs[];
    private int llgain_value = 0;
    private int hlgain_value = 0;
    private int total_wdrc_channels = 0;
    private ThreeTapLineChart leftEarStlc;
    private SingleTapLineChart eqStlc;
    private WdrcLineChart wdrcStlc;
    private TinnitusLineChart sgStlc;
    private ArrayList<String> list2 = new ArrayList<>();
    private ArrayList<String> list_centerband = new ArrayList<>();
    private ArrayList<String> list_bandwidth = new ArrayList<>();
    private OptionsPickerView pv_microphone,pv_centerband,pv_bandwidth;
    private LinearLayout microphone_setting_ll ;
    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            if (data.address.equals(getHearingAidModel(side).address)) {


                if (!getHearingAidModel(side).connected) {
                    Log.d(TAG,"config changed l107");
                    isActiveView(getHearingAidModel(side).connected);
                    initializeSDKParameters.cancel(true);
                } else if (!isActive && !isBusy() && getHearingAidModel(side).connected) {
                    Log.d(TAG,"L113");
                    isActiveView(getHearingAidModel(side).connected);

                }
            }
        }
    };

    public ParameterFragmentItem() {
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
        Log.d(TAG,"L103:OnResume");
        register();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"L103:OnPause");
        unregister();
    }

    private Parameter getParameter(HearingAidModel.Side side, String id) {
        Parameter parameter = null;
        current_memory_idx = 0;

        try {


            parameter = getHearingAidModel(side).arr_parameters[current_memory_idx].getById(id);
            if (id == "X_EQ_ChannelGain_dB[10]") {
                IndexedList arr_list = parameter.getListValues();
                int count = arr_list.getCount();
                for (int i = 0; i < count; i++) {
                    arr_eq_dbs[i] = (int )arr_list.getItem(i);
                    Log.v(TAG, "" + arr_list.getItem(i));
                }
            }

        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        return parameter;
    }
    private Parameter getSysparameter(HearingAidModel.Side side, String id) {
        Parameter parameter = null;

        try {
            parameter = getHearingAidModel(side).systemParameters.getById(id);

            IndexedTextList arr_list2 = parameter.getTextListValues();
            int count = arr_list2.getCount();
            for (int i =0; i< count ;i++) {
                Log.v(TAG,arr_list2.getItem(i));
            }
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        return parameter;
    }

    private void updateViewValues(final HearingAidModel.Side side) {
        //取得所有param的值
        Parameter param_eq  = getParameter(side, getString(R.string.sdk_enableEQ_param_id));
        Parameter param_noise = getParameter(side, getString(R.string.sdk_enableNR_param_id));
        Parameter param_tinnitus = getParameter(side,"X_SG_EnableMode");
        Parameter param_fbc = getParameter(side,"X_FBC_Enable");
        Parameter param_automemory =  getSysparameter(side,"X_EC_AutomaticMemory");
        Parameter param_lf = getParameter(side, "X_EQ_ChannelGain_dB[1]");
        Parameter param_mf = getParameter(side, "X_EQ_ChannelGain_dB[4]");
        Parameter param_hf = getParameter(side, "X_EQ_ChannelGain_dB[10]");
        Parameter param_llgain = getParameter(side,"X_WDRC_LowLevelGain[0]");
        Parameter param_hlgain = getParameter(side,"X_WDRC_HighLevelGain[0]");
        Parameter param_wdrc_channels = getParameter(side,"X_WDRC_NumberOfChannels");
        Parameter param_femode = getParameter(side,"X_FE_FEMode");
        Parameter param_bands = getParameter(side,"X_SG_Bandwidth");
        Parameter param_centerband = getParameter(side,"X_SG_Centerband");
        Parameter param_sglevel = getParameter(side,"X_SG_Level");


        int lf_value = 0;
        int mf_value =0;
        int hf_value = 0;
        int wdrc_min = 0;
        int wdrc_max = 0;



        try {
         //   if (param_eq != null)
        //        enableEQ.setChecked(param_eq.getValue() == 1);
         //   if (param_noise != null)
        //        enableNR.setChecked(param_noise.getValue() == 1);
        //    if (param_tinnitus != null)
          //      enableTinnitus.setChecked(param_tinnitus.getValue() >0 );
         //   if (param_fbc != null)
         //       enableFBC.setChecked(param_fbc.getValue() == 1);
         //   if (param_automemory != null) {

         //       enableAutomemory.setChecked(param_automemory.getValue() == (1 + current_memory_idx));
          //  }
            if (param_lf != null) {
                lf_value = param_lf.getValue();
            }
            if (param_mf != null) {
                mf_value = param_mf.getValue();
            }
            if (param_hf != null) {
                hf_value = param_hf.getValue();
            }
            if (param_llgain != null) {
                llgain_value = param_llgain.getValue();
                wdrc_min = param_llgain.getMin();
                wdrc_max = param_llgain.getMax();
            }
            if (param_hlgain !=null) {
                hlgain_value = param_hlgain.getValue();
            }
            //这怎么会返回0?
        //    if (param_wdrc_channels !=null)
        //        total_wdrc_channels = param_wdrc_channels.getValue();

            total_wdrc_channels = 16;

            seekBarllgain.setMax(wdrc_max);
            SeekBarHlgain.setMax(wdrc_max);
            seekBarllgain.setProgress(llgain_value);
            SeekBarHlgain.setProgress(hlgain_value);



            if (param_femode !=null) {
                Log.d(TAG,"femode="+ param_femode.getValue());
                int list_option = param_femode.getValue();
                String textval =  list2.get(list_option);

                pv_microphone.setSelectOptions(param_femode.getValue());
                if (textval != null)
                microphone_setting_tv.setText(textval);
            }
            if (param_centerband !=null) {

                int list_option = param_centerband.getValue();
                String textval =  list_centerband.get(list_option);

                pv_centerband.setSelectOptions(param_centerband.getValue());
                if (textval != null)
                    sg_centerband_tv.setText(textval);
            }
            if (param_bands !=null) {

                int list_option = param_bands.getValue();
                String textval =  list_bandwidth.get(list_option);

                pv_bandwidth.setSelectOptions(param_bands.getValue());
                if (textval != null)
                    sg_bandwidth_tv.setText(textval);
            }

            if (param_lf !=null) {
                SeekBarOwnvoice.setProgress(param_lf.getValue());
                SeekBarOwnvoice.setMax(param_lf.getMax());
            }
            if (param_sglevel !=null) {
                seekBarSglevel.setProgress(param_sglevel.getValue());
                seekBarSglevel.setMax(param_sglevel.getMax());
            }

            //计算下men ,sharp的度，转为与10参照，如何转化
            int mensharp_degree= get_mensharp_degree();
            SeekBarMensharp.setProgress(mensharp_degree);
            SeekBarMensharp.setMax(72);


            if (param_automemory !=null) {
                int ec_id = param_automemory.getValue();
                String tmp_label ="未设置";
                if (ec_id >0)  tmp_label = "程序"+(ec_id+1);
                if (tv_hearingaid_autoec!=null)
                  tv_hearingaid_autoec.setText(tmp_label);
            }

            tv_hearingaid_memory.setText("程序"+ (current_memory_idx+1));


         if (param_noise != null) {
             String tmp_label = "关闭";
             if (param_noise.getValue() == 1)
                 tmp_label = "打开";
             tv_hearingaid_noisereduction.setText(tmp_label );

         }

            if (param_fbc != null) {
                String tmp_label = "关闭";
                if (param_fbc.getValue() == 1)
                    tmp_label = "打开";
                tv_hearingaid_fbc.setText(tmp_label );

            }

            if (param_femode !=null) {

                int list_option = param_femode.getValue();
                String textval =  list2.get(list_option);
                tv_hearingaid_femode.setText(textval);

            }

            if (param_tinnitus != null) {
                String tmp_label ="";
                if (param_tinnitus.getValue() == 0) tmp_label="关闭";
                else
                    tmp_label = "打开";

                tv_hearingaid_sgmode.setText(tmp_label);
            }
            Parameter  param_auxinput = getParameter(side,"X_AuxiliaryInput");
            if (param_auxinput !=null) {
                String tmp_label ="";
                if (param_auxinput.getValue() == 0) tmp_label="关闭";
                else
                    tmp_label = "打开";

                tv_hearingaid_auxinput.setText(tmp_label);
            }





        } catch (ArkException e) {
            e.printStackTrace();
        }

        values = new ArrayList<>();
        int lf_db = arr_eq_dbs[(int)lf_value];
        int mf_db = arr_eq_dbs[(int)mf_value];
        int  hf_db = arr_eq_dbs[(int)hf_value];


        values.add(new Entry(0, (int)lf_value ));
        values.add(new Entry(1,(int)mf_value  ));
        values.add(new Entry(2,(int)hf_value));

        wdrcStlc.setChartData(values);
        wdrcStlc.changeTouchEntry();


        wdrcStlc.setOnSingleTapListener(new WdrcLineChart.OnSingleTapListener() {
            @Override
            public void onSingleTap(int x, float y) {
                //只处理x=0,2
                Log.d(TAG,"L334: X="+x+",Y="+y);
                if (x==0) {
                    //小声
                    int bak_llgain_value = llgain_value;
                    llgain_value = (int)y;
                    int new_llgain_value = verify_and_set_wdrc_llgain(llgain_value);

                }
                if (x==2) {
                    int bak_hlgain_value = hlgain_value;
                    hlgain_value = (int)y;
                    int new_hlgain_value = verify_and_set_wdrc_hlgain(hlgain_value);
                }


            }
        });
        eqStlc.setChartData(values);


        values_sg = new ArrayList<>();
        values_sg.add(new Entry(0,0));
        for (int i=1;i<=9;i++) {
            int y = -i*10 -3;
            values_sg.add(new Entry(i, (int)y ));
        }

        sgStlc.setChartData(values_sg);


        sgStlc.changeTouchEntry();


    }

    private void onSwitchValueChanged(HearingAidModel.Side side, String id, boolean value) {
        int newval = 0;
        if (value)  newval =1;

        if (id =="X_EC_AutomaticMemory") {
            if (value)  newval = current_memory_idx+1 ;
            else  newval = 0;
            try {
                getSysparameter(side, id).setValue(newval);
                initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            try {
                getParameter(side, id).setValue(newval);
                initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void onSeekBarProgressEnd(HearingAidModel.Side side, String id, int value) {
        try {
            getParameter(side, id).setValue(value);
            initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }

    }
    private void Update_EQ(HearingAidModel.Side side, String id, int value) {
        String first_id  ="",tmp_id;
        Parameter tmp_parameter;
        int old_value ,delta,newval;
        int min,max;
        int start_freq =0 ,end_freq=49;
        if (id =="LF") {
            start_freq=0;end_freq = 4;
        }
        if (id =="MF") {
            start_freq=4;end_freq = 10;
        }
        if (id =="HF") {
            start_freq=10;end_freq = 49;
        }
        try {

            first_id ="X_EQ_ChannelGain_dB["+start_freq+"]" ;
            tmp_parameter =  getParameter(side,first_id);
            if (tmp_parameter == null)  return ;
            old_value = tmp_parameter.getValue();
            delta = value - old_value;
            for (int freq_idx=start_freq;freq_idx<end_freq;freq_idx++) {
                tmp_id = "X_EQ_ChannelGain_dB["+freq_idx+"]";
                tmp_parameter = getParameter(side,tmp_id);
                if (tmp_parameter != null) {
                    old_value = tmp_parameter.getValue();
                    min = tmp_parameter.getMin();
                    max = tmp_parameter.getMax();
                    newval =old_value + delta;
                    if (newval < min)  newval = min;
                    if (newval >max)  newval = max;
                    tmp_parameter.setValue(newval);
                }
            }
        //    initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }

    }
    private void Update_EQ_MENSHARP(HearingAidModel.Side side, String id ,int delta_val) {
        String first_id  ="",tmp_id;
        Parameter tmp_parameter;
        int old_value  ,delta,newval;
        int min,max;
        int start_freq =0 ,end_freq=49;
        int[] arr_delta = new int[49];
        if (id=="SHARP") {

            for (int i=0;i<49;i++)  arr_delta[i] = 0;
            for (int i=0;i<=4;i++)  arr_delta[i] =1*delta_val;
            for (int i=9;i<=16;i++)  arr_delta[i] =-1*delta_val;

        }
        if (id=="MEN") {

            for (int i=0;i<49;i++)  arr_delta[i] = 0;
            for (int i=0;i<=4;i++)  arr_delta[i] =-1*delta_val;
            for (int i=9;i<=16;i++)  arr_delta[i] =1*delta_val;
        }

        try {


            for (int freq_idx=start_freq;freq_idx<end_freq;freq_idx++) {
                delta = arr_delta[freq_idx];
                if ( delta == 0 ) continue;

                tmp_id = "X_EQ_ChannelGain_dB["+freq_idx+"]";
                tmp_parameter = getParameter(side,tmp_id);
                if (tmp_parameter != null) {
                    old_value = tmp_parameter.getValue();
                    min = tmp_parameter.getMin();
                    max = tmp_parameter.getMax();
                    newval =old_value + delta;
                    if (newval < min)  newval = min;
                    if (newval >max)  newval = max;
                    tmp_parameter.setValue(newval);
                }
            }
        //    initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }

    }



    private void switchListeners() {
/*        enableNR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onSwitchValueChanged(side, getString(R.string.sdk_enableNR_param_id), b);
            }
        });

        enableEQ.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onSwitchValueChanged(side, getString(R.string.sdk_enableEQ_param_id), b);
            }
        });
        enableFBC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onSwitchValueChanged(side, "X_FBC_Enable", b);
            }
        });
        enableTinnitus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               onSwitchValueChanged(side, "X_SG_EnableMode", b);

            }
        });



        enableEQ.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onSwitchValueChanged(side, getString(R.string.sdk_enableEQ_param_id), b);
            }
        });
        enableAutomemory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onSwitchValueChanged(side, "X_EC_AutomaticMemory", b);
            }
        });
        */
    }

    private void seekBarListeners() {
        // 先关注界面


        //噪音
        seekBarSglevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //   onSeekBarProgressEnd(side, getString(R.string.sdk_treble_param_id), seekBar.getProgress());
                int newval = seekBar.getProgress();

                try {

                    Parameter tmp_parameter = getParameter(side, "X_SG_Level");
                    if (tmp_parameter != null) {
                        tmp_parameter.setValue(newval);
                    }
                }catch (ArkException e) {

                }


            }
        });


    }
    private void btnListeners() {


    }
    private void init_microphonelist() {
        list2.add("单麦");
        list2.add("前置麦克风全向");
        list2.add("后置麦克风全向");
        list2.add("双麦克风全向");
        list2.add("静态方向性");
        list2.add("Wideband ADM");

        microphone_setting_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pv_microphone.show();
            }
        });

        //创建
        pv_microphone = new OptionsPickerBuilder(getActivity(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                microphone_setting_tv.setText(list2.get(options1));

                int newval = options1;
                String id="X_FE_FEMode";

                try {
                    getParameter(side, id).setValue(newval);
                //    initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (ArkException e) {
                    Log.e(TAG, e.getMessage());
                }
                catch(NullPointerException e) {
                    Log.e(TAG, e.getMessage());
                }

            }
        }).setSelectOptions(0)//设置选择第一个
                .setOutSideCancelable(false)//点击背的地方不消失
                .build();
//      把数据绑定到控件上面
        pv_microphone.setPicker(list2);

    }
    private void init_sg_centerband() {
        int count = 11750/250;
        for (int i=0; i<count;i++ )
            list_centerband.add(250*(1+i) +" Hz");


        sg_centerband_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pv_centerband.show();
            }
        });

        //创建
        pv_centerband = new OptionsPickerBuilder(getActivity(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                sg_centerband_tv.setText(list_centerband.get(options1));

                int newval = options1;
                String id="X_SG_Centerband";

                try {
                    getParameter(side, id).setValue(newval);
              //      initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (ArkException e) {
                    Log.e(TAG, e.getMessage());
                } catch(NullPointerException e) {
                    Log.e(TAG, e.getMessage());
                }

            }
        }).setSelectOptions(0)//设置选择第一个
                .setOutSideCancelable(false)//点击背的地方不消失
                .build();
//      把数据绑定到控件上面
        pv_centerband.setPicker(list_centerband);

    }
    private void init_sg_bandwidth() {
        int count = 11750/250;
        for (int i=0; i<=count;i++ ) {
            String tmp = "250+ "+ 250*i+" Hz";
            list_bandwidth.add(tmp);
        }


        sg_bandwidth_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pv_bandwidth.show();
            }
        });

        //创建
        pv_bandwidth = new OptionsPickerBuilder(getActivity(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                sg_bandwidth_tv.setText(list_bandwidth.get(options1));

                int newval = options1;
                String id="X_SG_Bandwidth";

                try {
                    getParameter(side, id).setValue(newval);
               //     initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (ArkException e) {
                    Log.e(TAG, e.getMessage());
                } catch(NullPointerException e) {
                    Log.e(TAG, e.getMessage());
                }

            }
        }).setSelectOptions(0)//设置选择第一个
                .setOutSideCancelable(false)//点击背的地方不消失
                .build();
//      把数据绑定到控件上面
        pv_bandwidth.setPicker(list_bandwidth);

    }

    private int  verify_and_set_wdrc_llgain(int newval) {
        int db_lth =0,db_uth=0,db_hlgain=0,db_llgain =0;
        Parameter tmp_parameter,llgain_parameter;
        IndexedList  arr_list;
        int tmp_val = 0;
        int delta = 0;
        int old_llgain_val=0;
        int new_llgain_val =0;
        int min=0,max=0;
        int return_value = -1;

        tmp_parameter = getParameter(side, "X_WDRC_LowLevelGain[0]");
        if (tmp_parameter == null) return -1;
        try {
            old_llgain_val = (int) tmp_parameter.getValue();
            delta = newval -old_llgain_val;
            min = tmp_parameter.getMin();
            max = tmp_parameter.getMax();

        }catch (ArkException ark) {

            return -1;
        }

        for (int ch=0;ch< 8;ch++) {
            try {
                tmp_parameter = getParameter(side, "X_WDRC_LowLevelThreshold[" + ch + "]");
                if (tmp_parameter == null) continue;
                tmp_val  = tmp_parameter.getValue();
                arr_list =  tmp_parameter.getListValues();
                db_lth =(int) arr_list.getItem(tmp_val);


                tmp_parameter = getParameter(side, "X_WDRC_HighLevelThreshold[" + ch + "]");
                if (tmp_parameter == null) continue;
                tmp_val  = tmp_parameter.getValue();
                arr_list =  tmp_parameter.getListValues();
                db_uth =(int) arr_list.getItem(tmp_val);

                tmp_parameter = getParameter(side, "X_WDRC_LowLevelGain[" + ch + "]");
                if (tmp_parameter == null) continue;
                tmp_val  = tmp_parameter.getValue();
                old_llgain_val = tmp_parameter.getValue();
                arr_list =  tmp_parameter.getListValues();
                db_llgain =(int) arr_list.getItem(tmp_val);


                llgain_parameter = tmp_parameter;


                tmp_parameter = getParameter(side, "X_WDRC_HighLevelGain[" + ch + "]");
                if (tmp_parameter == null) continue;
                tmp_val  = tmp_parameter.getValue();
                arr_list =  tmp_parameter.getListValues();
                db_hlgain =(int) arr_list.getItem(tmp_val);

                //用新的llgain_val
                new_llgain_val = old_llgain_val +delta;
                if (new_llgain_val <min)  new_llgain_val =min;
                if (new_llgain_val >max )  new_llgain_val =max;
                db_llgain = (int) arr_list.getItem(new_llgain_val);

                boolean bpass1 =  (db_hlgain <= db_llgain);
                boolean bpass2 = ( db_hlgain + db_uth) >= (db_llgain +db_lth);

                if (bpass1 && bpass2) {
                    llgain_parameter.setValue(new_llgain_val);
                    if (ch==0) return_value = new_llgain_val;
                }

            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }


        }
        return  return_value ;

    }
    private int verify_and_set_wdrc_hlgain(int newval) {
        int db_lth =0,db_uth=0,db_hlgain=0,db_llgain =0;
        Parameter tmp_parameter,hlgain_parameter;
        IndexedList  arr_list;
        int tmp_val = 0;
        int delta = 0;
        int old_hlgain_val=0;
        int new_hlgain_val =0;
        int min=0,max=0;
        int return_value = -1;

        tmp_parameter = getParameter(side, "X_WDRC_HighLevelGain[0]");
        if (tmp_parameter == null) return -1;
        try {
            old_hlgain_val = (int) tmp_parameter.getValue();
            delta = newval -old_hlgain_val;
            min = tmp_parameter.getMin();
            max = tmp_parameter.getMax();

        }catch (ArkException ark) {

            return -1;
        }
        //如果16个channel,比较慢
        for (int ch=0;ch< 8;ch++) {
            try {
                tmp_parameter = getParameter(side, "X_WDRC_LowLevelThreshold[" + ch + "]");
                if (tmp_parameter == null) continue;
                tmp_val  = tmp_parameter.getValue();
                arr_list =  tmp_parameter.getListValues();
                db_lth =(int) arr_list.getItem(tmp_val);


                tmp_parameter = getParameter(side, "X_WDRC_HighLevelThreshold[" + ch + "]");
                if (tmp_parameter == null) continue;
                tmp_val  = tmp_parameter.getValue();
                arr_list =  tmp_parameter.getListValues();
                db_uth =(int) arr_list.getItem(tmp_val);

                tmp_parameter = getParameter(side, "X_WDRC_LowLevelGain[" + ch + "]");
                if (tmp_parameter == null) continue;
                tmp_val  = tmp_parameter.getValue();
                arr_list =  tmp_parameter.getListValues();
                db_llgain =(int) arr_list.getItem(tmp_val);




                tmp_parameter = getParameter(side, "X_WDRC_HighLevelGain[" + ch + "]");
                if (tmp_parameter == null) continue;
                tmp_val  = tmp_parameter.getValue();
                hlgain_parameter = tmp_parameter;
                arr_list =  tmp_parameter.getListValues();
                db_hlgain =(int) arr_list.getItem(tmp_val);
                old_hlgain_val =  tmp_parameter.getValue();


                //用新的llgain_val
                new_hlgain_val = old_hlgain_val +delta;
                if (new_hlgain_val <min)  new_hlgain_val =min;
                if (new_hlgain_val >max )  new_hlgain_val =max;
                db_hlgain = (int) arr_list.getItem(new_hlgain_val);



                boolean bpass1 =  (db_hlgain <= db_llgain);
                boolean bpass2 = ( db_hlgain + db_uth) >= (db_llgain +db_lth);

                if (bpass1 && bpass2) {
                    hlgain_parameter.setValue(new_hlgain_val);
                    if (ch==0) return_value = new_hlgain_val;
                }

            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }


        }
        return  return_value ;

    }
    private int get_mensharp_degree() {
        //0到4的平均值
        int sum_val = 0;
        int avg_men = 0;
        int avg_sharp = 0;
        try {
            for (int i = 0; i <= 4; i++) {
                String param_name = "X_EQ_ChannelGain_dB[" + i + "]";
                Parameter tmp_param = getParameter(side, param_name);
                if (tmp_param != null) {
                    sum_val = sum_val + tmp_param.getValue();
                }
            }
            avg_sharp = sum_val/5;
            sum_val = 0;
            for (int i = 9; i <= 16; i++) {
                String param_name = "X_EQ_ChannelGain_dB[" + i + "]";
                Parameter tmp_param = getParameter(side, param_name);
                if (tmp_param != null) {
                    sum_val = sum_val + tmp_param.getValue();
                }
            }
            avg_men = sum_val/8;

        }catch (ArkException fe ) {
            Log.e(TAG,fe.getMessage());
        }
        // 如果相等，0，每相差4算一档 ， 0..100
        int delta = avg_sharp - avg_men ; //这个取值是-18到18,但现实中不会差距太大，估计-10到10左右

        delta +=36 ; //转化到0..36之间
        if (delta <0) delta=0;
        if (delta >72) delta =72;
         return delta;

    }
    private void getSide() {
        String sideHA = getArguments().getString(getString(R.string.HA_SIDE));
        if (sideHA != null)
            side = (sideHA.equals(HearingAidModel.Side.Left.name())) ? HearingAidModel.Side.Left : HearingAidModel.Side.Right;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getSide();
        Bundle bundle = getArguments();
        if (bundle !=null) {
            page1 = bundle.getString("page1");
        }
        View view =  inflater.inflate(R.layout.fragment_parameter_item_parts, container, false);

       {
            enableEQ = view.findViewById(R.id.switchEQ);
            enableNR = view.findViewById(R.id.switchNR);
            enableFBC = view.findViewById(R.id.switchFBC);
            enableTinnitus = view.findViewById(R.id.switchtinnitus);
            enableAutomemory = view.findViewById(R.id.switchAutomemory);

           tv_hearingaid_autoec = view.findViewById(R.id.tv_hearingaid_autoec);
           tv_hearingaid_id = view.findViewById(R.id.tv_hearingaid_id);

           tv_hearingaid_id.setText(getHearingAidModel(side).address);

           tv_hearingaid_battery = view.findViewById(R.id.tv_hearingaid_battery);
           tv_hearingaid_battery.setText(""+getHearingAidModel(side).batteryLevel);

           tv_hearingaid_memory = view.findViewById(R.id.tv_hearingaid_memory);

           tv_hearingaid_noisereduction =  view.findViewById(R.id.tv_hearingaid_noisereduction);

           tv_hearingaid_fbc =  view.findViewById(R.id.tv_hearingaid_fbc);

           tv_hearingaid_femode = view.findViewById(R.id.tv_hearingaid_femode);
           tv_hearingaid_sgmode = view.findViewById(R.id.tv_hearingaid_sgmode);

           tv_hearingaid_auxinput = view.findViewById(R.id.tv_hearingaid_auxinput);

            seekBarSglevel = view.findViewById(R.id.sglevel_SeekBar);


            SeekBarOwnvoice = view.findViewById(R.id.ownvoice_SeekBar);
           SeekBarMensharp = view.findViewById(R.id.men_SeekBar);



            seekBarBass = view.findViewById(R.id.bassSeekBar);
            seekBarMid = view.findViewById(R.id.midSeekBar);
            seekBarTreble = view.findViewById(R.id.trebleSeekBar);
            seekBarWideband = view.findViewById(R.id.widebandSeekBar);
            progressBar = view.findViewById(R.id.progressBar);
            progressBarTextView = view.findViewById(R.id.progressUpdateTextView);
            progressDlg = new ProgressDialog(getActivity());

            microphone_setting_tv = view.findViewById(R.id.microphone_setting_tv);
            microphone_setting_ll = view.findViewById(R.id.microphone_setting_ll);

            sg_centerband_ll = view.findViewById(R.id.sg_centerband_ll);
            sg_centerband_tv = view.findViewById(R.id.sg_centerband_tv);

            sg_bandwidth_tv = view.findViewById(R.id.sg_bandwidth_tv);
            sg_bandwidth_ll = view.findViewById(R.id.sg_bandwidth_ll);


            optgroup_sg_ll = view.findViewById(R.id.optgroup_sg_ll);
            sg_enable_ll = view.findViewById(R.id.sg_enable_ll);
            sg_bandwidth_ll = view.findViewById(R.id.sg_bandwidth_ll);
            sg_centerband_ll = view.findViewById(R.id.sg_centerband_ll);
            sg_sglevel_ll = view.findViewById(R.id.sg_sglevel_ll);

            optgroup_eq_ll = view.findViewById(R.id.optgroup_eq_ll);
            left_ear_stlc_ll = view.findViewById(R.id.left_ear_stlc_ll);




            LinearLayout optgroup_modulesetting_ll = view.findViewById(R.id.optgroup_modulesetting_ll);
            LinearLayout modulesetting_ll = view.findViewById(R.id.modulesetting_ll);
        }

        createuserpage(view);
        init_optgroup(view);

        arr_eq_dbs = new int[100];
        for (int i=0;i<100;i++ )
            arr_eq_dbs[i] = 0;

        check_parameterspace();
        init_microphonelist();
        init_sg_centerband();
        init_sg_bandwidth();

        wdrcStlc = view.findViewById(R.id.wdrc_linechart);
        leftEarStlc = view.findViewById((R.id.left_ear_stlc));
        eqStlc = view.findViewById(R.id.eq_stlc);

        sgStlc = view.findViewById(R.id.tinnitus_linechart);

       updateViewValues(side);

        switchListeners();
        seekBarListeners();
        btnListeners();


        cardview_audio =  view.findViewById(R.id.cardview_audio) ;
        cardview_audio.setVisibility(View.GONE);

        cardview_eq = view.findViewById(R.id.cardview_eq) ;
        cardview_eq.setVisibility(View.GONE);

        cardview_sg =  view.findViewById(R.id.cardview_sg) ;
        cardview_sg.setVisibility(View.GONE);

        cardview_setting = view.findViewById(R.id.cardview_setting) ;
        cardview_setting.setVisibility(View.GONE);

        cardview_user = view.findViewById(R.id.cardview_wdrc);
        cardview_user.setVisibility(View.GONE);

        if (page1 =="user")
            cardview_user.setVisibility(View.VISIBLE);

        if (page1 =="setting")
            cardview_setting.setVisibility(View.VISIBLE);

        if (page1 =="audio")
            cardview_audio.setVisibility(View.VISIBLE);

        if (page1 =="sg")
            cardview_sg.setVisibility(View.VISIBLE);


        if (Configuration.instance().isHAAvailable(side)) {
            if (!getHearingAidModel(side).isConfigured) {
                initializeSDKParameters = new InitializeSDKParameters(side, DETECT_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                initializeSDKParameters = new InitializeSDKParameters(side, READ_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        }

        return view;
    }

    private void createuserpage(View view) {

        btn_voicesharp = view.findViewById(R.id.btn_voicesharp);
        btn_voicemen = view.findViewById(R.id.btn_voicemen);

        seekBarllgain = view.findViewById(R.id.llgain_SeekBar);
        SeekBarHlgain = view.findViewById(R.id.hlgain_SeekBar);


        seekBarllgain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //  onSeekBarProgressEnd(side, getString(R.string.sdk_bass_param_id), seekBar.getProgress());
                //  Update_EQ(side,"LF",seekBar.getProgress());
                //   Configuration.instance().showMessage("llgain value is:"+ seekBar.getProgress(),getActivity());
                int bak_llgain_value = llgain_value;
                llgain_value = seekBar.getProgress();
                int new_llgain_value = verify_and_set_wdrc_llgain(llgain_value);
                try {
                //    initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    Parameter tmp_parameter = getParameter(side, "X_WDRC_LowLevelGain[0]");
                    if (tmp_parameter != null) {
                        int val = tmp_parameter.getValue();
                        seekBarllgain.setProgress(val);
                    }

                }catch (ArkException e) {

                }

            }
        });

        SeekBarHlgain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //   onSeekBarProgressEnd(side, getString(R.string.sdk_treble_param_id), seekBar.getProgress());
                // Update_EQ(side,"HF",seekBar.getProgress());
                hlgain_value = seekBar.getProgress();
                int new_hlgain_value = verify_and_set_wdrc_hlgain(hlgain_value);
                try {
                //    initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    Parameter tmp_parameter = getParameter(side, "X_WDRC_HighLevelGain[0]");
                    if (tmp_parameter != null) {
                        int val = tmp_parameter.getValue();
                        SeekBarHlgain.setProgress(val);
                    }
                }catch (ArkException e) {

                }

                //再次取到新的值


                //  Configuration.instance().showMessage(" hlgain value is:"+ seekBar.getProgress(),getActivity());
            }
        });
        //自己声音太响
        SeekBarOwnvoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //   onSeekBarProgressEnd(side, getString(R.string.sdk_treble_param_id), seekBar.getProgress());

                Update_EQ(side,"LF",seekBar.getProgress());

            }
        });

        //自己声音太响
        SeekBarMensharp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //   onSeekBarProgressEnd(side, getString(R.string.sdk_treble_param_id), seekBar.getProgress());
                //往左移

                //计算下MENSHARP的值
                int nowval = seekBar.getProgress();
                int mensharp_degree= get_mensharp_degree();
                int detla = nowval - mensharp_degree;

                //SHARP变大
                if (nowval >mensharp_degree) {
                    Update_EQ_MENSHARP(side,"SHARP",Math.abs( detla) );
                }
                if (nowval < mensharp_degree) {
                    Update_EQ_MENSHARP(side,"MEN" ,Math.abs(detla) );
                }


            }
        });


    }
    private void progressView(int visibility) {

        if (visibility == View.GONE)
            progressDlg.hide();
        else
            progressDlg.show();

        progressBar.setVisibility(View.GONE);
        progressBarTextView.setVisibility(View.GONE);

    }
    private void toggle_optgroup(String grpname) {

    }
    private void init_optgroup(View view) {



        final LinearLayout optgroup_audiodevice_ll= view.findViewById(R.id.optgroup_audiodevice_ll);
        final LinearLayout extaudio_ll = view.findViewById(R.id.extaudio_ll);
        final LinearLayout microphone_setting_ll = view.findViewById(R.id.microphone_setting_ll);



    }

    private void isActiveView(boolean isActive) {
        ParameterFragmentItem.isActive = isActive;
     //   enableEQ.setEnabled(isActive);
     //   enableNR.setEnabled(isActive);
     //   seekBarBass.setEnabled(isActive);
      //  seekBarMid.setEnabled(isActive);
       // seekBarTreble.setEnabled(isActive);
      //  seekBarWideband.setEnabled(isActive);

        cardview_user.setEnabled(isActive);
        btn_voicesharp.setEnabled(isActive);
        btn_voicemen.setEnabled(isActive);
        wdrcStlc.setEnabled(isActive);
        SeekBarOwnvoice.setEnabled(isActive);
        SeekBarMensharp.setEnabled(isActive);

        cardview_audio.setEnabled(isActive);
        cardview_sg.setEnabled(isActive);
        cardview_eq.setEnabled(isActive);
        cardview_setting.setEnabled(isActive);





    }

    private HearingAidModel getHearingAidModel(HearingAidModel.Side side) {

        return Configuration.instance().getDescriptor(side);
    }
    private void check_parameterspace() {
        try {
            Log.d(TAG, "L92:wirelesscontrol:" + getHearingAidModel(side).wirelessControl.getCurrentMemory());
            Log.d(TAG, "L92:current memory:" + getHearingAidModel(side).product.getCurrentMemory());
            ParameterSpace tmp_ps = getHearingAidModel(side).wirelessControl.getCurrentMemory();
            if (tmp_ps == ParameterSpace.kNvmMemory0) current_memory_idx = 0;
            if (tmp_ps == ParameterSpace.kNvmMemory1) current_memory_idx = 1;
            if (tmp_ps == ParameterSpace.kNvmMemory2) current_memory_idx = 2;
            if (tmp_ps == ParameterSpace.kNvmMemory3) current_memory_idx = 3;
            if (tmp_ps == ParameterSpace.kNvmMemory4) current_memory_idx = 4;
            if (tmp_ps == ParameterSpace.kNvmMemory5) current_memory_idx = 5;
            if (tmp_ps == ParameterSpace.kNvmMemory6) current_memory_idx = 6;
            if (tmp_ps == ParameterSpace.kNvmMemory7) current_memory_idx = 7;
            getHearingAidModel(side).product.setCurrentMemory(current_memory_idx);
        } catch (ArkException e ) {

        }
    }


    private class InitializeSDKParameters extends AsyncTask<Void, Integer, Void> {
        AsyncResult res;
        DeviceInfo deviceInfo;
        private HearingAidModel.Side side;
        private CommunicationAdaptor communicationAdaptor;
        private Product product;
        private int command;
        private ParameterSpace  tmp_ps ;



        InitializeSDKParameters(HearingAidModel.Side side, int command) {
            this.side = side;
            this.command = command;
            communicationAdaptor = Configuration.instance().getDescriptor(this.side).communicationAdaptor;
            product = Configuration.instance().getDescriptor(this.side).product;


        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                tmp_ps = getHearingAidModel(side).wirelessControl.getCurrentMemory();
                switch (command) {
                    case DETECT_DEVICE:
                        res = communicationAdaptor.beginDetectDevice();
                        break;
                    case INITIALIZE_DEVICE:
                        res = product.beginInitializeDevice(communicationAdaptor);
                        break;
                    case READ_DEVICE:
                        res = product.beginReadParameters(tmp_ps);
                        break;
                    case READ_DEVICE_SYS:
                        res = product.beginReadParameters(ParameterSpace.kSystemActiveMemory);
                        break;
                    case WRITE_TO_DEVICE:
                        res = product.beginWriteParameters(tmp_ps);
                        break;
                }
                isBusy = true;
                if (res ==null) {
                  Log.e(TAG,"启动异步任务失败,任务类型:"+command);
                  return null;
                }
                while (!res.isIsFinished()) {
                    publishProgress(res.getProgressValue());
                }
                res.getResult();

            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());

            }
            return null;

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isActiveView(false);
            switch (command) {
                case DETECT_DEVICE:
                //    progressBarTextView.setText(getString(R.string.msg_param_progress_detect) + " " + side.name() + ".....");
                    progressDlg.setTitle(getString(R.string.msg_param_progress_detect) + " " + side.name() + ".....");
                    progressDlg .setMessage("");
                    progressDlg.setCancelable(false);
                    progressDlg.show();
                    break;
                case INITIALIZE_DEVICE:
                //    progressBarTextView.setText(R.string.msg_param_progress_init);

                    progressDlg.setTitle(R.string.msg_param_progress_init);
                    progressDlg .setMessage("");
                    progressDlg.setCancelable(false);
                    progressDlg.show();
                    break;
                case READ_DEVICE:
                 //   progressBarTextView.setText(R.string.msg_param_progress_reading);
                    progressDlg.setTitle(R.string.msg_param_progress_reading);
                    progressDlg .setMessage("");
                    progressDlg.setCancelable(false);
                    progressDlg.show();

                    break;
                case READ_DEVICE_SYS:
                    //   progressBarTextView.setText(R.string.msg_param_progress_reading);
                    progressDlg.setTitle(R.string.msg_param_progress_reading);
                    progressDlg .setMessage("");
                    progressDlg.setCancelable(false);
                    progressDlg.show();


                    break;

                case WRITE_TO_DEVICE:
                 //   progressView(View.VISIBLE);
                   // progressBarTextView.setText("Writing to Device");
                   progressDlg.setTitle(R.string.msg_param_progress_writing);
                    progressDlg .setMessage("");
                    progressDlg.setCancelable(false);
                    progressDlg.show();

                    break;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (res == null ) {
          //      Configuration.instance().showMessage("启动异步任务失败", getActivity());
        //        progressDlg.hide();
                return ;
            }
            try {
                switch (command) {
                    case DETECT_DEVICE:
                        deviceInfo = communicationAdaptor.endDetectDevice(res);
                        initializeSDKParameters = new InitializeSDKParameters(side, INITIALIZE_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    case INITIALIZE_DEVICE:
                        product.endInitializeDevice(res);
                        initializeSDKParameters = new InitializeSDKParameters(side, READ_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    case READ_DEVICE:
                        initializeSDKParameters = new InitializeSDKParameters(side, READ_DEVICE_SYS).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                     //   Configuration.instance().showMessage("Initialization Complete", getActivity());
                      //  progressView(View.GONE);
                        isActiveView(true);
                     //   isBusy = false;
                      //  getHearingAidModel(side).productInitialized = true;
                      //  getHearingAidModel(side).isConfigured = true;
                    //    updateViewValues(side);

                     //   progressDlg.hide();

                        break;
                    case READ_DEVICE_SYS:
                        progressDlg.hide();
                        isActiveView(true);
                        isBusy = false;
                        getHearingAidModel(side).productInitialized = true;
                        getHearingAidModel(side).isConfigured = true;
                        updateViewValues(side);


                    case WRITE_TO_DEVICE:
                    //    Configuration.instance().showMessage("Write to Device Complete", getActivity());
                     //   progressView(View.GONE);
                        progressDlg.hide();
                        isBusy = false;
                        isActiveView(true);
                        break;
                }
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
       //     progressBar.setProgress(values[0]);
        }


    }


}
