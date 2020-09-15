package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.ark.ArkException;
import com.ark.AsyncResult;
import com.ark.CommunicationAdaptor;
import com.ark.DeviceInfo;
import com.ark.Parameter;
import com.ark.ParameterSpace;
import com.ark.Product;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;


public class TinnitusFragmentItem extends Fragment {

    private static final String TAG = ParameterSpace.class.getSimpleName();
    private static final int DETECT_DEVICE = 0;
    private static final int INITIALIZE_DEVICE = 1;
    private static final int READ_DEVICE = 2;
    private static final int WRITE_TO_DEVICE = 3;
    private static final int READ_DEVICE_SYS = 4;
    private static HearingAidModel.Side side;
    private static boolean isActive = true;
    private static AsyncTask<Void, Integer, Void> initializeSDKParameters;
    private boolean isBusy = false;
    private Switch enableEQ, enableNR;
    private SeekBar seekBarBass, seekBarMid, seekBarTreble,seekBarWideband;
    private TextView progressBarTextView;
    private ProgressBar progressBar;
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

    public TinnitusFragmentItem() {
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
    }

    private Parameter getParameter(HearingAidModel.Side side, String id) {
        Parameter parameter = null;
        int mem_idx = 0;
        try {
            Log.d(TAG,"L92:wirelesscontrol:"+ getHearingAidModel(side).wirelessControl.getCurrentMemory());
            Log.d(TAG,"L92:current memory:"+ getHearingAidModel(side).product.getCurrentMemory());
            ParameterSpace tmp_ps = getHearingAidModel(side).wirelessControl.getCurrentMemory();
            if (tmp_ps == ParameterSpace.kNvmMemory0)  mem_idx =0;
            if (tmp_ps == ParameterSpace.kNvmMemory1)  mem_idx =1;
            if (tmp_ps == ParameterSpace.kNvmMemory2)  mem_idx =2;
            if (tmp_ps == ParameterSpace.kNvmMemory3)  mem_idx =3;
            if (tmp_ps == ParameterSpace.kNvmMemory4)  mem_idx =4;
            if (tmp_ps == ParameterSpace.kNvmMemory5)  mem_idx =5;
            if (tmp_ps == ParameterSpace.kNvmMemory6)  mem_idx =6;
            if (tmp_ps == ParameterSpace.kNvmMemory7)  mem_idx =7;
            getHearingAidModel(side).product.setCurrentMemory(mem_idx);

            parameter = getHearingAidModel(side).arr_parameters[mem_idx].getById(id);

        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
        return parameter;
    }

    private void updateViewValues(HearingAidModel.Side side) {
        try {
            enableEQ.setChecked(getParameter(side, getString(R.string.sdk_enableEQ_param_id)).getValue() == 1);
            enableNR.setChecked(getParameter(side, getString(R.string.sdk_enableNR_param_id)).getValue() == 1);


        } catch (ArkException e) {
            e.printStackTrace();
        }
    }

    private void onSwitchValueChanged(HearingAidModel.Side side, String id, boolean value) {
        try {
            getParameter(side, id).setValue((value) ? 1 : 0);
            initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
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
        try {
            if (id =="LF") {
                for (int freq_idx=0;freq_idx<4;freq_idx++) {
                    String tmp_id = "X_EQ_ChannelGain_dB["+freq_idx+"]";
                    Parameter  tmp_parameter = getParameter(side,tmp_id);
                    if (tmp_parameter != null) {
                        tmp_parameter.setValue(value);
                    }
                }


            }
            if (id =="MF") {
                for (int freq_idx=4;freq_idx<10;freq_idx++) {
                    String tmp_id = "X_EQ_ChannelGain_dB["+freq_idx+"]";
                    Parameter  tmp_parameter = getParameter(side,tmp_id);
                    if (tmp_parameter != null) {
                        tmp_parameter.setValue(value);
                    }
                }

            }
            if (id =="HF") {
                for (int freq_idx=10;freq_idx<49;freq_idx++) {
                    String tmp_id = "X_EQ_ChannelGain_dB["+freq_idx+"]";
                    Parameter  tmp_parameter = getParameter(side,tmp_id);
                    if (tmp_parameter != null) {
                        tmp_parameter.setValue(value);
                    }
                }


            }
            initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    private void switchListeners() {
        enableNR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        View view = inflater.inflate(R.layout.fragment_tinnitus_item, container, false);


        updateViewValues(side);

        switchListeners();
        seekBarListeners();

        if (Configuration.instance().isHAAvailable(side)) {
            if (!getHearingAidModel(side).isConfigured) {
                initializeSDKParameters = new InitializeSDKParameters(side, DETECT_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                initializeSDKParameters = new InitializeSDKParameters(side, READ_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
               // progressView(View.GONE);
            }
        }

        return view;
    }

    private void progressView(int visibility) {
        progressBar.setVisibility(visibility);
        progressBarTextView.setVisibility(visibility);
    }

    private void isActiveView(boolean isActive) {
        TinnitusFragmentItem.isActive = isActive;
        enableEQ.setEnabled(isActive);
        enableNR.setEnabled(isActive);

    }

    private HearingAidModel getHearingAidModel(HearingAidModel.Side side) {

        return Configuration.instance().getDescriptor(side);
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

                    break;
                case INITIALIZE_DEVICE:
                    //    progressBarTextView.setText(R.string.msg_param_progress_init);


                    break;
                case READ_DEVICE:
                    //   progressBarTextView.setText(R.string.msg_param_progress_reading);


                    break;
                case READ_DEVICE_SYS:
                    //   progressBarTextView.setText(R.string.msg_param_progress_reading);



                    break;

                case WRITE_TO_DEVICE:
                    //   progressView(View.VISIBLE);
                    // progressBarTextView.setText("Writing to Device");


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
                        //   isActiveView(true);
                        //   isBusy = false;
                        //  getHearingAidModel(side).productInitialized = true;
                        //  getHearingAidModel(side).isConfigured = true;
                        //    updateViewValues(side);

                        //   progressDlg.hide();

                        break;
                    case READ_DEVICE_SYS:

                        isActiveView(true);
                        isBusy = false;
                        getHearingAidModel(side).productInitialized = true;
                        getHearingAidModel(side).isConfigured = true;
                        updateViewValues(side);


                    case WRITE_TO_DEVICE:
                        //    Configuration.instance().showMessage("Write to Device Complete", getActivity());
                        //   progressView(View.GONE);
                     //   progressDlg.hide();
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

        }


    }





}
