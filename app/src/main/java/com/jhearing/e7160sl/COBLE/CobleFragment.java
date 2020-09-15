package com.jhearing.e7160sl.COBLE;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jhearing.e7160sl.COBLE.Battery.BatteryControlFragment;
import com.jhearing.e7160sl.COBLE.Memory.MemoryFragment;
import com.jhearing.e7160sl.COBLE.Volume.VolumeFragment;
import com.jhearing.e7160sl.R;


public class CobleFragment extends Fragment {

    private String tag;

    public CobleFragment() {
        // Required empty public constructor
    }

    private void createVolumeFragment() {
        VolumeFragment volumeFragment = new VolumeFragment();
        tag = getResources().getStringArray(R.array.fragments_names)[0];
        getChildFragmentManager().beginTransaction().replace(R.id.volume_frame, volumeFragment, tag).addToBackStack(tag).commit();
    }

    private void createBatteryFragment() {
        BatteryControlFragment batteryControlFragment = new BatteryControlFragment();
        tag = getResources().getStringArray(R.array.fragments_names)[1];
        getChildFragmentManager().beginTransaction().replace(R.id.battery_frame, batteryControlFragment, tag).addToBackStack(tag).commit();
    }

    private void createMemoryFragment() {
        MemoryFragment memoryFragment = new MemoryFragment();
        tag = getResources().getStringArray(R.array.fragments_names)[2];
        getChildFragmentManager().beginTransaction().replace(R.id.memory_frame, memoryFragment, tag).addToBackStack(tag).commit();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_coble_v2, container, false);

      //  createVolumeFragment();
      //  createBatteryFragment();
        createMemoryFragment();
        return rootView;
    }


}
