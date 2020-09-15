package com.jhearing.e7160sl.Params;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ark.ArkException;
import com.ark.AsyncResult;
import com.ark.CommunicationAdaptor;
import com.ark.DeviceInfo;
import com.ark.IndexedList;
import com.ark.IndexedTextList;
import com.ark.Parameter;
import com.ark.ParameterList;
import com.ark.ParameterSpace;
import com.ark.ParameterType;
import com.ark.Product;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.github.mikephil.charting.data.Entry;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;
import com.jhearing.e7160sl.adapter.ItemAdapter;
import com.jhearing.e7160sl.widget.DialogInput;
import com.jhearing.e7160sl.widget.ThreeTapLineChart;

import java.util.ArrayList;


public class ParameterFragmentHackItem extends Fragment {

    private static final String TAG = ParameterSpace.class.getSimpleName();
    private static final int DETECT_DEVICE = 0;
    private static final int INITIALIZE_DEVICE = 1;
    private static final int READ_DEVICE = 2;
    private static final int READ_DEVICE_SYS = 4;

    private static final int WRITE_TO_DEVICE = 3;
    private static HearingAidModel.Side side;
    private static boolean isActive = true;
    private static AsyncTask<Void, Integer, Void> initializeSDKParameters;

    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<String> list1 = new ArrayList<>();
    private ArrayList<String> list2 = new ArrayList<>();
    private OptionsPickerView pvOptions1;
    private OptionsPickerView pvOptions2;

    private boolean isBusy = false;

    private int current_memory_idx;

    private TextView progressBarTextView;
    private ProgressBar progressBar;
    private ProgressDialog progressDlg;
    private RecyclerView parameterSettingRv;
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

    public ParameterFragmentHackItem() {
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
        current_memory_idx = 0;

        try {


            parameter = getHearingAidModel(side).arr_parameters[current_memory_idx].getById(id);


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
        list2.clear();
        ParameterList arr_list = getHearingAidModel(side).arr_parameters[current_memory_idx];
        ParameterList syslist = getHearingAidModel(side).systemParameters;
        try {
            int count = arr_list.getCount();
            for (int i = 0; i <count ; i++) {
                Parameter tmp_param = arr_list.getItem(i);
                String title = tmp_param.getName();
                String unit = tmp_param.getUnits();
                ParameterType type = tmp_param.getType();
                int value ;
                String strres = "";
                if (type == ParameterType.kBoolean) {
                    value = (int)tmp_param.getValue();
                    strres = value+"";
                }
                else if (type == ParameterType.kIndexedList) {
                    value = tmp_param.getValue();
                    IndexedList arr_listvalues = tmp_param.getListValues();
                    double  dvalue = arr_listvalues.getItem(value);
                    strres  = dvalue +"";
                }
                else if (type == ParameterType.kIndexedTextList) {
                    value = tmp_param.getValue();
                    IndexedTextList arr_list2 = tmp_param.getTextListValues();
                    String textvalue = arr_list2.getItem(value);
                    strres = textvalue ;
                } else if (type == ParameterType.kDouble) {
                     double dvalue = tmp_param.getDoubleValue();
                     strres = dvalue +"";
                } else {
                    value = tmp_param.getValue();
                    strres = value +"";
                }
                list2.add(title+" = "+strres+unit);


            }
            count = syslist.getCount();
            for (int i = 0; i < count ; i++) {
                Parameter tmp_param = syslist.getItem(i);
                String title = tmp_param.getName();
                String unit = tmp_param.getUnits();
                ParameterType type = tmp_param.getType();
                int value ;
                String strres = "";
                if (type == ParameterType.kBoolean) {
                    value = (int)tmp_param.getValue();
                    strres = value+"";
                }
                else if (type == ParameterType.kIndexedList) {
                    value = tmp_param.getValue();
                    IndexedList arr_listvalues = tmp_param.getListValues();
                    double  dvalue = arr_listvalues.getItem(value);
                    strres  = dvalue +"";
                }
                else if (type == ParameterType.kIndexedTextList) {
                    value = tmp_param.getValue();
                    IndexedTextList arr_list2 = tmp_param.getTextListValues();
                    String textvalue = arr_list2.getItem(value);
                    strres = textvalue ;
                } else if (type == ParameterType.kDouble) {
                    double dvalue = tmp_param.getDoubleValue();
                    strres = dvalue +"";
                } else {
                    value = tmp_param.getValue();
                    strres = value +"";
                }
                list2.add(title+" = "+strres+unit);


            }
;
        } catch (ArkException e) {
                Log.e(TAG,e.getMessage());
        }
        ItemAdapter itemAdapter =(ItemAdapter) parameterSettingRv.getAdapter();
        itemAdapter.setList(list2);
        itemAdapter.notifyDataSetChanged();

    }

    private void getSide() {
        String sideHA = getArguments().getString(getString(R.string.HA_SIDE));
        if (sideHA != null)
            side = (sideHA.equals(HearingAidModel.Side.Left.name())) ? HearingAidModel.Side.Left : HearingAidModel.Side.Right;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getSide();
        View view = inflater.inflate(R.layout.fragment_parameter_item_hack, container, false);


        parameterSettingRv = view.findViewById(R.id.parameter_setting_rv);
        progressBar = view.findViewById(R.id.progressBar);
        progressBarTextView = view.findViewById(R.id.progressUpdateTextView);
        progressDlg = new ProgressDialog(getActivity());


        parameterSettingRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        parameterSettingRv.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));

        ItemAdapter itemAdapter = new ItemAdapter();

        parameterSettingRv.setAdapter(itemAdapter);


        itemAdapter.addData(list2);
        itemAdapter.notifyDataSetChanged();
    //    updateViewValues(side);


        final DialogInput.Builder builder = new DialogInput.Builder(getActivity());
        builder.setLeftClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setRightClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builder.setRangeListener(new DialogInput.Builder.OnRangeListener() {
                    @Override
                    public void getRange(String value) {
                        Configuration.instance().showMessage(value,getActivity());
                    }
                });
            }
        });

        itemAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
               //builder.create().show();
                String msg = getString(R.string.invalid_operation);
                Configuration.instance().alertDialog(msg, getActivity());



            }
        });
        check_parameterspace();



        if (Configuration.instance().isHAAvailable(side)) {
            if (!getHearingAidModel(side).isConfigured) {
                initializeSDKParameters = new InitializeSDKParameters(side, DETECT_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                initializeSDKParameters = new InitializeSDKParameters(side, READ_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        }

        return view;
    }

    private void progressView(int visibility) {

        if (visibility == View.GONE)
            progressDlg.hide();
        else
            progressDlg.show();

        progressBar.setVisibility(View.GONE);
        progressBarTextView.setVisibility(View.GONE);

    }

    private void isActiveView(boolean isActive) {
        ParameterFragmentHackItem.isActive = isActive;

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
                Configuration.instance().showMessage("启动异步任务失败", getActivity());
                progressDlg.hide();
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
