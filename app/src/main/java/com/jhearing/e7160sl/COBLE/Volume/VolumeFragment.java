package com.jhearing.e7160sl.COBLE.Volume;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ark.ArkException;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;
import com.project.android.seekarclibrary.SeekArc;


/**
 * Individual view of one of the hearing aids, either the right or the left in the VolumeControlActivity activity
 * A simple {@link Fragment} subclass.
 */
public class VolumeFragment extends Fragment {

    private final String TAG = VolumeFragment.class.getName();
    private TextView volumeTextView;
    private SeekArc mSeekArc;
    private SeekBar mSeekBar;
    private int ENABLED_COLOR;
    private int DISCONNECTED_COLOR;
    private int viewVolume;
    private HearingAidModel masterHA;

    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            setDisconnectedConnectedView();
            checkConfiguration(HearingAidModel.Side.Left, data.address);
            checkConfiguration(HearingAidModel.Side.Right, data.address);
        }
    };

    public VolumeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register();
    }

    private void updateVolumeView() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
            mSeekBar.setProgress(viewVolume);
            volumeTextView.setText(String.valueOf(viewVolume));
        } else {
            mSeekArc.setProgress(viewVolume);
            volumeTextView.setText(String.valueOf(viewVolume));
        }

    }

    private void checkConfiguration(HearingAidModel.Side side, String address) {
        if (Configuration.instance().isHANotNull(side)) {
            if (address.equals(getHearingAid(side).address)) {
                if (getHearingAid(side).volume != viewVolume) {
                    viewVolume = getHearingAid(side).volume;
                    updateVolumeView();
                }
            }
        }
    }


    private HearingAidModel getHearingAid(HearingAidModel.Side side) {

        return Configuration.instance().getDescriptor(side);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_volume, container, false);
        volumeTextView = rootView.findViewById(R.id.volumeTxt);
        getResourceColors();

        this.setHasOptionsMenu(true);


        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
            setSeekBar(rootView);
        } else {
            setSeekArc(rootView);
        }


        if (Configuration.instance().isConfigEmpty())
            setNullView();

        return rootView;
    }

    private void updateCurrentVolumeValue() {
        if (!Configuration.instance().isConfigEmpty()) {
            if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Left)) {
                masterHA = getHearingAid(HearingAidModel.Side.Left);
                viewVolume = getHearingAid(HearingAidModel.Side.Left).volume;
            } else if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
                viewVolume = getHearingAid(HearingAidModel.Side.Right).volume;
                masterHA = getHearingAid(HearingAidModel.Side.Right);
            }
        }
    }

    private void getResourceColors() {
        ENABLED_COLOR = getResources().getColor(R.color.colorAccent, null);
        DISCONNECTED_COLOR = getResources().getColor(R.color.fbutton_color_pomegranate, null);
    }


    private void setSeekBar(View rootView) {
        mSeekBar = rootView.findViewById(R.id.volumeSeekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                viewVolume = progress;
                volumeTextView.setText(String.valueOf(viewVolume));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateHAsWithViewValue();
            }
        });
        syncValues();
        updateVolumeView();
    }

    private void syncValues() {
        // Making sure the hearing aids are in sync

        if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Left)) {
            masterHA = getHearingAid(HearingAidModel.Side.Left);
            viewVolume = getHearingAid(HearingAidModel.Side.Left).volume;
            // if both hearing aids are connected then (sync)
            if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
                if (viewVolume != getHearingAid(HearingAidModel.Side.Right).volume) {
                    writeToHA(HearingAidModel.Side.Right, viewVolume);
                }
            }
        } else if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
            viewVolume = getHearingAid(HearingAidModel.Side.Right).volume;
            masterHA = getHearingAid(HearingAidModel.Side.Right);
        } else
            viewVolume = Configuration.instance().HA_VOLUME_LIMIT;

    }

    private void setSeekArc(View rootView) {
        mSeekArc = rootView.findViewById(R.id.seekArcLeft);
        setSeekArcListener();

        syncValues();
        updateVolumeView();
    }


    private void setNullView() {
        updateVolumeView();
        updateVolumeLevelTextViewColor(DISCONNECTED_COLOR);
        updateSeekArc(false);
    }

    private void setDisconnectedConnectedView() {
        boolean isConnected = Configuration.instance().isHAAvailable(HearingAidModel.Side.Left) || Configuration.instance().isHAAvailable(HearingAidModel.Side.Right);
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            updateSeekBar(isConnected);
        } else {
            updateSeekArc(isConnected);
        }
        updateVolumeLevelTextViewColor(isConnected ? ENABLED_COLOR : DISCONNECTED_COLOR);
    }

    private void updateVolumeLevelTextViewColor(int color) {
        TextView textView = volumeTextView;
        textView.setTextColor(color);
    }

    @Override
    public void onResume() {
        super.onResume();
        onResumeUpdates();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregister();
    }


    private void unregister() {
        EventBus.unregisterReceiver(configurationChangedEventEventHandler);
    }

    private void register() {
        EventBus.registerReceiver(configurationChangedEventEventHandler, ConfigurationChangedEvent.class.getName());
    }


    private void onResumeUpdates() {
        updateCurrentVolumeValue();
        setDisconnectedConnectedView();
        updateVolumeView();
    }


    private void setSeekArcListener() {
        mSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                updateHAsWithViewValue();
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onProgressChanged(SeekArc seekArc, int progress,
                                          boolean fromUser) {
                viewVolume = progress;
                volumeTextView.setText(String.valueOf(viewVolume));
            }
        });
    }


    private void updateSeekBar(boolean enabled) {
        mSeekBar.setEnabled(enabled);
    }

    private void updateSeekArc(boolean enabled) {
        mSeekArc.setEnabled(enabled);
        mSeekArc.setProgressColor((enabled) ? ENABLED_COLOR : DISCONNECTED_COLOR);
        mSeekArc.setArcColor((enabled) ? ENABLED_COLOR : DISCONNECTED_COLOR);
    }

    private void writeVolumes() throws ArkException {
        writeToHA(HearingAidModel.Side.Right, viewVolume);
        writeToHA(HearingAidModel.Side.Left, viewVolume);
    }

    private void updateHAsWithViewValue() {
        volumeTextView.setText(String.valueOf(viewVolume));
        try {
            writeVolumes();
        } catch (ArkException e) {
            Log.i(TAG, e.getMessage());
        }
    }


    /**
     * This method is used to change the volume using the phone's volume buttons
     * onKeyDown does the following
     * - checks the keycode to see if the volume up/down button was pressed
     * - if volume keys were pressed then it calls the appropriate volume up/down function
     * - sets the new progress of the SeekArcVolume to show the increase/decrease of volume
     * - remove the callbacks for volume
     *
     * @param keyCode code of the volume key button pressed, captured by the main activity and passed onto the VolumeFragment fragment
     * @return always true as a call back for the onKeyDown super in the main activity
     */
    public boolean onKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) volumeKeyUp();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) volumeKeyDown();

        mSeekArc.setProgress(viewVolume);
        updateHAsWithViewValue();
        try {
            writeVolumes();
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
        return true;
    }

    private void volumeKeyUp() {
        viewVolume = (viewVolume >= mSeekArc.getMax()) ? mSeekArc.getMax() : ++viewVolume;
    }

    private void volumeKeyDown() {
        viewVolume = (viewVolume <= 0) ? 0 : --viewVolume;
    }


    private synchronized void writeToHA(HearingAidModel.Side side, int newVolume) {
        if (Configuration.instance().isHAAvailable(side)) {
            try {
                Log.i(TAG, "sdk write to volume" + side + "value : " + newVolume);
                getHearingAid(side).wirelessControl.setVolume(newVolume);
                getHearingAid(side).volume = newVolume;
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

}


