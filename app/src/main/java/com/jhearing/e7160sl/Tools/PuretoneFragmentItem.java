package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ark.ArkException;
import com.ark.CommunicationAdaptor;
import com.ark.LogType;
import com.ark.Manufacturing;
import com.ark.Parameter;
import com.ark.Product;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;

import java.util.Objects;


public class PuretoneFragmentItem extends Fragment {

    private static final String TAG = PuretoneFragmentItem.class.getSimpleName();
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
    private int m_volume = -80;
    private SeekBar seekBarVolume ;
    private AppCompatSpinner  freqSpinner,durationSpinner ;
    private TextView progressBarTextView,dbTextView;
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

    public PuretoneFragmentItem() {
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
        try {
            parameter = getHearingAidModel(side).parameters.getById(id);
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

    private void spinnerListeners() {
        freqSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int arr_freqs[] = {250,500,1000,1500,2000,2500,3000,3500,4000,4500,5000,5500,6000,6500,7000,7500,8000};
                m_freq = arr_freqs[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int arr_durations[] = {30,60,90,120};
                m_duration = arr_durations[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    private void seekBarListeners() {
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                m_volume = i -80;
                dbTextView.setText(String.valueOf(m_volume)+" dBFS");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
              //  dbTextView.setText(String.valueOf(seekBar.getProgress()));

            }
        });




    }
    private void buttnListeners() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isBusy = true;
                progressView(View.VISIBLE);
                progressBarTextView.setText("Hold on ..");


                if (isPlaying == true) {
                    //stop
                    try {
                        manufacturing.stopTone();
                        progressBarTextView.setText("Stopped Play ..");
                        manufacturing.prepareDeviceForModeling(false);
                        progressView(View.GONE);
                        btnPlay.setText("Play");
                        isPlaying = false;

                    } catch (ArkException e) {
                        Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    }
                } else {

                    try {

                        isPlaying = true;
                        btnPlay.setText("Stop");

                        manufacturing.prepareDeviceForModeling(true);
                        progressBarTextView.setText("Playing ..");

                        manufacturing.generateTone(m_freq, m_volume);

                    }   catch (ArkException e) {
                        Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    }


                    progressView(View.GONE);
                }
                isBusy = false;

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jsonlog ="";
                isBusy = true;
                progressView(View.VISIBLE);
                progressBarTextView.setText("Hold on ..");
                try {
                    jsonlog = product.generateLog(LogType.kShort);
                }catch (ArkException e) {
                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                }
                Configuration.instance().showMessage(jsonlog,getActivity());

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
        View view = inflater.inflate(R.layout.fragment_puretone_item, container, false);

        seekBarVolume = view.findViewById(R.id.bassSeekBar);
        progressBar = view.findViewById(R.id.progressBar);
        progressBarTextView = view.findViewById(R.id.progressUpdateTextView);
        durationSpinner = view.findViewById(R.id.durationspinner);
        freqSpinner = view.findViewById(R.id.freqspinner);
        dbTextView = view.findViewById(R.id.txt_dbfs);
        btnPlay = view.findViewById(R.id.button_play);
        btnVolumeup = view.findViewById(R.id.button_up);
        btnVolumedown = view.findViewById(R.id.button_down);
        btnSave =  view.findViewById(R.id.button_save);



        updateViewValues(side);

        spinnerListeners();
        seekBarListeners();
        buttnListeners();
        try {
            HearingAidModel ha = getHearingAidModel(side);
            communicationAdaptor = ha.communicationAdaptor;
            product = ha.product;
            manufacturing = ha.manufacturing;
            communicationAdaptor.detectDevice();
            manufacturing.setProduct(product);

            manufacturing.initializeDevice(communicationAdaptor);
        }catch (ArkException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }


        return view;
    }

    private void progressView(int visibility) {
        progressBar.setVisibility(visibility);
        progressBarTextView.setVisibility(visibility);
    }

    private void isActiveView(boolean isActive) {
        PuretoneFragmentItem.isActive = isActive;

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
