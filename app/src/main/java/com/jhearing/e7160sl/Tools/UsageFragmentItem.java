package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatSpinner;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ark.ArkException;
import com.ark.CommunicationAdaptor;
import com.ark.Manufacturing;
import com.ark.Parameter;
import com.ark.ParameterSpace;
import com.ark.Product;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;


public class UsageFragmentItem extends Fragment {

    private static final String TAG = UsageFragmentItem.class.getSimpleName();
    private static final int DETECT_DEVICE = 0;
    private static final int INITIALIZE_DEVICE = 1;
    private static final int READ_DEVICE = 2;
    private static final int WRITE_TO_DEVICE = 3;
    private static HearingAidModel.Side side;
    private static boolean isActive = true;

    private boolean isBusy = false;
    private boolean isPlaying = false;
    private int m_freq=0;
    private int m_duration = 0;
    private int m_volume = 0;
    private SeekBar seekBarVolume ;

    private TextView progressBarTextView,dbTextView,tvShortlog,tvLonglog;
    private ProgressBar progressBar;
    private Button  btnPlay,btnVolumeup,btnVolumedown,btnSave;
    private CommunicationAdaptor communicationAdaptor;
    private Product product ;
   private  Manufacturing manufacturing  ;


    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            if (data.address.equals(getHearingAidModel(side).address)) {
                if (!getHearingAidModel(side).connected) {
                    isActiveView(getHearingAidModel(side).connected);

                } else if (!isActive && !isBusy() && getHearingAidModel(side).connected) {
                    isActiveView(getHearingAidModel(side).connected);
                }
            }
        }
    };

    public UsageFragmentItem() {
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


    private Parameter getSysParameter(HearingAidModel.Side side, String id) {
        Parameter parameter = null;
        try {
            parameter = getHearingAidModel(side).systemParameters.getById(id);
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
        return parameter;
    }
    private void updateViewValues(HearingAidModel.Side side) {

    }

    private void onSwitchValueChanged(HearingAidModel.Side side, String id, boolean value) {

    }

    private void onSeekBarProgressEnd(HearingAidModel.Side side, String id, int value) {


    }


    private void buttnListeners() {

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                Parameter tmp_parameter = getSysParameter(side,"X_DL_Enable");
                if (tmp_parameter != null) {
                    tmp_parameter.setValue(1);
                    product.writeParameters(ParameterSpace.kSystemNvmMemory);
                    Configuration.instance().showMessage("saving parameters",getActivity());

                }} catch (ArkException e ) {
                    Log.e(TAG, e.getMessage());

                }


            }
        });
    }

    private void getSide() {
        String sideHA = getArguments().getString(getString(R.string.HA_SIDE));
        if (sideHA != null)
            side = (sideHA.equals(HearingAidModel.Side.Left.name())) ? HearingAidModel.Side.Left : HearingAidModel.Side.Right;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getSide();
        View view = inflater.inflate(R.layout.fragment_usage_item, container, false);

        seekBarVolume = view.findViewById(R.id.bassSeekBar);
        progressBar = view.findViewById(R.id.progressBar);
        progressBarTextView = view.findViewById(R.id.progressUpdateTextView);

        dbTextView = view.findViewById(R.id.txt_dbfs);
        btnPlay = view.findViewById(R.id.button_play);
        btnVolumeup = view.findViewById(R.id.button_up);
        btnVolumedown = view.findViewById(R.id.button_down);
        btnSave =  view.findViewById(R.id.button_save);


        String jsonlog ="";

       /*
        updateViewValues(side);

        buttnListeners();
        try {
            HearingAidModel ha = getHearingAidModel(side);
            communicationAdaptor = ha.communicationAdaptor;
            product = ha.product;
            communicationAdaptor.detectDevice();
            product.initializeDevice(communicationAdaptor);

            Parameter tmp_parameter = getSysParameter(side,"X_DL_Enable");
            if (tmp_parameter != null) {
                ParameterType type = tmp_parameter.getType();
                int value = tmp_parameter.getValue();



            }



            jsonlog = product.generateLog(LogType.kShort);
            tvShortlog.setText(jsonlog);
            jsonlog = product.generateLog(LogType.kLong);
            tvLonglog.setText(jsonlog);



        }catch (ArkException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        */
        return view;
    }

    private void progressView(int visibility) {
        progressBar.setVisibility(visibility);
        progressBarTextView.setVisibility(visibility);
    }

    private void isActiveView(boolean isActive) {
        UsageFragmentItem.isActive = isActive;

        seekBarVolume.setEnabled(isActive);

        btnVolumedown.setEnabled(isActive);
        btnVolumeup.setEnabled(isActive);
        btnSave.setEnabled(isActive);
        btnPlay.setEnabled(isActive);

    }

    private HearingAidModel getHearingAidModel(HearingAidModel.Side side) {

        return Configuration.instance().getDescriptor(side);
    }



}
