package com.jhearing.e7160sl.COBLE.Battery;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.R;


/*
 * Use the {@link BatteryControlFragment#} factory method to
 * create an instance of this fragment.
 */
public class BatteryControlFragment extends Fragment {

    public BatteryControlFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_battery_control, container, false);
        createBatteryFragments();
        return rootView;
    }

    private void createBatteryFragments() {

        BatteryFragmentItem LeftBattery = new BatteryFragmentItem();
        BatteryFragmentItem RightBattery = new BatteryFragmentItem();

        LeftBattery.setState(HearingAidModel.Side.Left);
        RightBattery.setState(HearingAidModel.Side.Right);

        getChildFragmentManager().beginTransaction().replace(R.id.L_batteryContainer, LeftBattery).add(R.id.R_batteryContainer, RightBattery).commit();
    }


}
