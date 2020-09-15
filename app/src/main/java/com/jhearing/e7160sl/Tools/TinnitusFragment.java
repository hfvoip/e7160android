package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.MainActivity;
import com.jhearing.e7160sl.Params.ParameterFragmentItem;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;


public class TinnitusFragment extends Fragment {

    private static final String TAG = TinnitusFragment.class.getSimpleName();

    private static final int DISCONNECTING = 0;
    private static final int DISCONNECTED = 1;
    private static final int CONNECTING = 2;
    private static final int CONNECTED = 3;
    private TinnitusFragmentItem TinnitusFragmentItem;
    private ImageView imageRight, imageLeft;
    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            onConfigurationChanged(HearingAidModel.Side.Left, data.address);
            onConfigurationChanged(HearingAidModel.Side.Right, data.address);
        }
    };

    public TinnitusFragment() {
        // Required empty public constructor
    }

    private void onConfigurationChanged(HearingAidModel.Side side, String address) {
        if (Configuration.instance().isHANotNull(side)) {
            if (address.equals(getHearingAid(side).address)) {
                Log.i(TAG, "Connection Status" + getHearingAid(side).connectionStatus);
                connectedView(side);
            }
        }
    }

    private HearingAidModel getHearingAid(HearingAidModel.Side side) {
        return Configuration.instance().getDescriptor(side);
    }

    private void connectedView(HearingAidModel.Side side) {

        ImageView imageView = (side == HearingAidModel.Side.Left) ? imageLeft : imageRight;
        int imageResDis = (side == HearingAidModel.Side.Left) ? R.mipmap.ear_l_red : R.mipmap.ear_r_red;
        int imageResConn = (side == HearingAidModel.Side.Left) ? R.mipmap.ear_l_green : R.mipmap.ear_r_green;
        int imageResConnecting = (side == HearingAidModel.Side.Left) ? R.mipmap.ear_l_orange : R.mipmap.ear_r_orange;

        switch (getHearingAid(side).connectionStatus) {
            case DISCONNECTING:
                imageView.setImageResource(imageResDis);
                break;
            case DISCONNECTED:
                imageView.setImageResource(imageResDis);
                break;
            case CONNECTING:
                imageView.setImageResource(imageResConnecting);
                break;
            case CONNECTED:
                imageView.setImageResource(imageResConn);
                break;
        }
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


    private void createTinnitusFragment(HearingAidModel.Side side) {
        getChildFragmentManager().beginTransaction().replace(R.id.param_frame, createSide(side), "param" + side.name()).commit();
    }


    private TinnitusFragmentItem createSide(HearingAidModel.Side side) {
        TinnitusFragmentItem = new TinnitusFragmentItem();
        Bundle bundleL = new Bundle();
        bundleL.putString(getString(R.string.HA_SIDE), side.name());
        TinnitusFragmentItem.setArguments(bundleL);
        return TinnitusFragmentItem;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_parameter, container, false);

        imageLeft = view.findViewById(R.id.ear_left);
        imageRight = view.findViewById(R.id.ear_right);
        ((MainActivity) getActivity()).changeNavigationSelected(R.id.nav_Param);

        if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Left)) {
            imageLeft.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
            createTinnitusFragment(HearingAidModel.Side.Left);
            imageRight.setBackgroundColor(getResources().getColor(R.color.ap_gray, null));
        } else if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
            imageRight.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
            createTinnitusFragment(HearingAidModel.Side.Right);
            imageLeft.setBackgroundColor(getResources().getColor(R.color.ap_gray, null));
        }

        imageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageTabPressed(HearingAidModel.Side.Left);

            }
        });

        imageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageTabPressed(HearingAidModel.Side.Right);
            }
        });

        if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Left))
            connectedView(HearingAidModel.Side.Left);

        if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Right))
            connectedView(HearingAidModel.Side.Right);

        return view;
    }

    private void imageTabPressed(HearingAidModel.Side side) {

        ImageView image = (side == HearingAidModel.Side.Left) ? imageLeft : imageRight;
        ImageView opposite = (side == HearingAidModel.Side.Left) ? imageRight : imageLeft;

        if (!TinnitusFragmentItem.getTag().equals("param" + side.name())) {
            if (!TinnitusFragmentItem.isBusy()) {
                if (Configuration.instance().isHAAvailable(side)) {
                    createTinnitusFragment(side);
                    image.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
                    opposite.setBackgroundColor(getResources().getColor(R.color.ap_gray, null));
                }
            } else {
                Configuration.instance().alertDialog("Please wait until the current process is complete", getActivity());
            }
        }
    }

}
