/*
----------------------------------------------------------------------------
Copyright (c) 2016 Semiconductor Components Industries, LLC
(d/b/a ON Semiconductor). All Rights Reserved.

This code is the property of ON Semiconductor and may not be redistributed
in any form without prior written permission from ON Semiconductor. The
terms of use and warranty for this code are covered by contractual
agreements between ON Semiconductor and the licensee.
----------------------------------------------------------------------------
BatteryFragmentItem.java
- Fragment item used to support each HA device battery level. 
----------------------------------------------------------------------------
$Revision: 1.5 $
$Date: 2017/05/25 16:39:40 $
----------------------------------------------------------------------------
*/

package com.jhearing.e7160sl.COBLE.Battery;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;


/**
 * A simple {@link Fragment} subclass. This class supports the battery monitoring feature for a single
 * hearing aid device
 */
public class BatteryFragmentItem extends Fragment {

    private static final String TAG = BatteryFragmentItem.class.getSimpleName();

    private ImageView batteryImageView;
    private TextView dataPercentage;
    private ImageView BLEImageView;
    private HearingAidModel.Side side;

    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            HearingAidModel HA = getHearingAid();
            final int NULL_BATTERY_LEVEL = -1;
            if (HA == null) {
                updateBatteryConnectionView();
                return;
            }

            if (data.address.equals(HA.address)) {
                if (HA.connected && HA.batteryLevel != NULL_BATTERY_LEVEL)
                    setBatteryView(HA.batteryLevel);

            }

            updateBatteryConnectionView();
        }
    };

    private HearingAidModel getHearingAid() {
        return Configuration.instance().getDescriptor(side);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.battery_item, container, false);
        batteryImageView = rootView.findViewById(R.id.battery_imgView);
        BLEImageView = rootView.findViewById(R.id.BLE_imgView);
        dataPercentage = rootView.findViewById(R.id.percentage_txtView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!Configuration.instance().isConfigEmpty()) {
            if (Configuration.instance().isHAAvailable(side))
                setBatteryView(getHearingAid().batteryLevel);
        }
        updateBatteryConnectionView();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregister();
    }

    private void unregister() {
        EventBus.unregisterReceiver(configurationChangedEventHandler);
    }

    private void register() {
        EventBus.registerReceiver(configurationChangedEventHandler, ConfigurationChangedEvent.class.getName());
    }

    /**
     * Method to set the side of the hearing aid model, it can be either a left or right hearing aid
     *
     * @param side Hearing aid side Right or Left
     */
    public void setState(HearingAidModel.Side side) {
        this.side = side;
    }

    private void setBatteryView(int batteryLevel) {
        setBatteryPercentage(batteryLevel);
        setBatteryLevel(batteryLevel);
    }

    private void setBatteryPercentage(int batteryPercentage) {
        dataPercentage.setText(getString(R.string.battery_percentage, batteryPercentage));
        Log.i(TAG, "Battery percentage " + getString(R.string.battery_percentage, batteryPercentage));
    }

    private void setBatteryLevel(int batteryLevel) {
        Log.i(TAG, "Battery level " + batteryLevel);
        if (batteryLevel == 100) {
            batteryImageView.setImageResource(R.drawable.battery_full);
            return;
        }
        switch ((batteryLevel * 5) / 100) {
            case 0:
            case 1:
                batteryImageView.setImageResource(R.drawable.battery_critical);
                break;
            case 2:
                batteryImageView.setImageResource(R.drawable.battery_two_dots_yellow);
                break;
            case 3:
                batteryImageView.setImageResource(R.drawable.battery_three_dots_lime);
                break;
            case 4:
                batteryImageView.setImageResource(R.drawable.battery_four_dots);
                break;
            default:
                break;
        }
    }

    private void updateBatteryConnectionView() {
        if (Configuration.instance().isHAAvailable(side)) {
            connectedBondedView();
        } else
            notConnectedView();
    }

    private void connectedBondedView() {
        BLEImageView.setImageResource(R.mipmap.ic_bluetooth_connected_black);
        BLEImageView.setColorFilter(Color.argb(255, 0, 153, 0));
    }

    private void notConnectedView() {
        batteryImageView.setImageResource(R.drawable.battery_empty);
        dataPercentage.setVisibility(View.INVISIBLE);
        BLEImageView.setImageResource(R.mipmap.ic_bluetooth_disabled_black);
        BLEImageView.setColorFilter(Color.argb(255, 204, 41, 0));
    }


}
