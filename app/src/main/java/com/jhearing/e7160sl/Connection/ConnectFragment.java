package com.jhearing.e7160sl.Connection;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jhearing.e7160sl.COBLE.Battery.BatteryControlFragment;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.MainActivity;
import com.jhearing.e7160sl.R;


public class ConnectFragment extends Fragment {


    public ConnectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, container, false);
        ((MainActivity) getActivity()).changeNavigationSelected(R.id.nav_connect);
        createConnectionFragments();
        createBatteryFragment();
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void createConnectionFragments() {
        getChildFragmentManager().beginTransaction().replace(R.id.Connection_left_frame, createSides(HearingAidModel.Side.Left)).
                replace(R.id.connection_right_frame, createSides(HearingAidModel.Side.Right)).commit();
    }


    private void createBatteryFragment() {
        BatteryControlFragment batteryControlFragment = new BatteryControlFragment();
        String tag = getResources().getStringArray(R.array.fragments_names)[1];
        getChildFragmentManager().beginTransaction().replace(R.id.battery_frame, batteryControlFragment, tag).addToBackStack(tag).commit();
    }

    private ConnectionHAFragmentItem createSides(HearingAidModel.Side side) {
        ConnectionHAFragmentItem connectionHAFragmentItem = new ConnectionHAFragmentItem();
        Bundle bundleL = new Bundle();
        bundleL.putString(getString(R.string.HA_SIDE), side.name());
        connectionHAFragmentItem.setArguments(bundleL);
        return connectionHAFragmentItem;
    }


}
