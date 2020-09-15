package com.jhearing.e7160sl.Connection;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.ark.AsyncResult;
import com.ark.CommunicationAdaptor;
import com.ark.DeviceInfo;
import com.ark.ParameterSpace;
import com.ark.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.GravityCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ark.ArkException;
import com.jhearing.e7160sl.Connection.ScanningDevice.ScanActivity;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.MainActivity;

import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;


public class ConnectionHAFragmentItem extends Fragment {

    private static final String TAG = ConnectionHAFragmentItem.class.getSimpleName();
    private static final int DISCONNECTING = 0;
    private static final int DISCONNECTED = 1;
    private static final int CONNECTING = 2;
    private static final int CONNECTED = 3;
    private static final int ERROR = 4;
    private static final int SEARCH_FOR_RESULTS = 89;

    private static final int DETECT_DEVICE = 0;
    private static final int INITIALIZE_DEVICE = 1;
    private static final int READ_DEVICE = 2;
    private static final int WRITE_TO_DEVICE = 3;

    TextView deviceAddress,
            deviceName,
            connectionStatus,
            sdkVersion,
            nordicVersion,
            firmwareVersion;
    FloatingActionButton fabDisconnect, fabReconnect, fabRemove;
    LinearLayout deviceInfoLayout;



    Intent intent;
    private HearingAidModel.Side side;
    private ImageView earImage;
    private View view;


    private static boolean isActive = true;
    private static AsyncTask<Void, Integer, Void> initializeSDKParameters;
    private boolean isBusy = false;

    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            onConfigurationChanged(side, data.address);
        }
    };

    public ConnectionHAFragmentItem() {

        // Required empty public constructor
    }

    private void onConfigurationChanged(HearingAidModel.Side side, String address) {
        if (Configuration.instance().isHANotNull(side)) {
            if (address.equals(getHearingAid().address)) {
                Log.i(TAG, "Connection Status " + getHearingAid().connectionStatus);
                connectedView();
            }
        }
    }


    private void startScanningActivity(HearingAidModel.Side side) {
        intent = new Intent(getContext(), ScanActivity.class);
        intent.putExtra(getString(R.string.HA_SIDE), side.name());
        startActivityForResult(intent, SEARCH_FOR_RESULTS);
    }


    private void connectedView() {

        view.invalidate();

        int imageResDis = (side == HearingAidModel.Side.Left) ? R.mipmap.ear_l_red : R.mipmap.ear_r_red;
        int imageResConnecting = (side == HearingAidModel.Side.Left) ? R.mipmap.ear_l_orange : R.mipmap.ear_r_orange;
        int imageResConn = (side == HearingAidModel.Side.Left) ? R.mipmap.ear_l_green : R.mipmap.ear_r_green;

        deviceName.setText(getHearingAid().name);
        deviceAddress.setText(getHearingAid().address);
        try {
            firmwareVersion.setText(getHearingAid().productDefinition.getUpdateFirmwareVersion());
        } catch (ArkException e) {
            e.printStackTrace();
        }

        switch (getHearingAid().connectionStatus) {
            case DISCONNECTING:
                connectionStatus.setText(getResources().getStringArray(R.array.connection_state)[DISCONNECTING]);
                break;
            case DISCONNECTED:
              //  fabDisconnect.setVisibility(View.GONE);
                fabDisconnect.hide();
               //  fabRemove.setVisibility(View.GONE);
                fabRemove.hide();
             //    fabReconnect.setVisibility(View.VISIBLE);
                fabReconnect.show();
                earImage.setImageResource(imageResDis);

                connectionStatus.setText(getResources().getStringArray(R.array.connection_state)[DISCONNECTED]);
            //    Toast.makeText(getContext(), " Disconnected from " + getHearingAid().name, Toast.LENGTH_SHORT).show();
                break;
            case CONNECTING:
                 fabDisconnect.show( );
                fabRemove.show( );
               fabReconnect.hide( );
              //  deviceName.setVisibility(View.VISIBLE);

                earImage.setImageResource(imageResConnecting);
                connectionStatus.setText(getResources().getStringArray(R.array.connection_state)[CONNECTING]);
                break;
            case CONNECTED:
                 fabDisconnect.show();
                 fabReconnect.hide( );
                fabRemove.show( );

                earImage.setImageResource(imageResConn);
                connectionStatus.setText(getResources().getStringArray(R.array.connection_state)[CONNECTED]);
             //   initializeSDKParameters = new InitializeSDKParameters(side, DETECT_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case ERROR:
                connectionStatus.setText(getResources().getStringArray(R.array.connection_state)[ERROR]);
                break;
        }
    }

    private HearingAidModel getHearingAid() {
        return Configuration.instance().getDescriptor(side);
    }

    private HearingAidModel getHearingAidModel(HearingAidModel.Side side) {

        return Configuration.instance().getDescriptor(side);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.registerReceiver(configurationChangedEventHandler, ConfigurationChangedEvent.class.getName());
        generalViewUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.unregisterReceiver(configurationChangedEventHandler);
    }

    private void generalViewUpdate() {

        if (Configuration.instance().isHANotNull(side)) {

            Log.i(TAG, "Connection Status" + getHearingAid().connectionStatus);
            deviceInfoLayout.setVisibility(View.VISIBLE);
            connectedView();
        } else
            emptyDescriptorView();

    }

    private void emptyDescriptorView() {

        fabDisconnect.hide();
      fabReconnect.hide();
         fabRemove.hide();
        deviceInfoLayout.setVisibility(View.INVISIBLE);


        int imageResEmptyConfig = (side == HearingAidModel.Side.Left) ? R.mipmap.ear_l_grey : R.mipmap.ear_r_grey;

        earImage.setImageResource(imageResEmptyConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == SEARCH_FOR_RESULTS) {
            connectedView();
        }

    }

    private void getSide() {
        String sideHA = getArguments().getString(getString(R.string.HA_SIDE));
        if (sideHA != null)
            side = (sideHA.equals(HearingAidModel.Side.Left.name())) ? HearingAidModel.Side.Left : HearingAidModel.Side.Right;
    }


    private void isActiveView(boolean isActive) {
        ConnectionHAFragmentItem.isActive = isActive;

    }
    public void buildAlertDialog(String title, String message, final Runnable runnable) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runnable.run();
            }
        }).setNegativeButton(R.string.popup_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getSide();

        view = inflater.inflate(R.layout.connection_fragment_item, container, false);
        deviceInfoLayout = view.findViewById(R.id.device_info_layout);
        earImage = view.findViewById(R.id.ear_image);
        deviceName = view.findViewById(R.id.Dname_textView);
        deviceAddress = view.findViewById(R.id.DAddress_textview);
        connectionStatus = view.findViewById(R.id.connection_status_textView);
        firmwareVersion = view.findViewById(R.id.DFirmware_textview);



        fabDisconnect = view.findViewById(R.id.fab_disconnect);
        fabReconnect = view.findViewById(R.id.fab_reconnect_ha);
        fabRemove = view.findViewById(R.id.fab_remove_ha);



        fabDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getHearingAid().connectionStatus == CONNECTED || getHearingAid().connectionStatus == CONNECTING) {
                    Runnable r = new Runnable() {
                        public void run() {
                            getHearingAid().autoConnect = false;
                            getHearingAid().connectionStatus = DISCONNECTING;
                            Configuration.instance().issueConfigurationChangedEvent(getHearingAid().address);
                            Configuration.instance().disconnectHA(side);
                        }
                    };
                    buildAlertDialog( getActivity().getString(R.string.disconnect_device ), getActivity().getString(R.string.disconnect_device_text) +" " + getHearingAid().name, r);

                }
            }
        });

        fabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Configuration.instance().isHANotNull(side)) {
                    Runnable r = new Runnable() {
                        public void run() {
                            Configuration.instance().removeHearingAid(side);
                            emptyDescriptorView();
                        }
                    };
                    buildAlertDialog(getActivity().getString(R.string.remove_device ), getActivity().getString(R.string.remove_device_text) +" " + getHearingAid().name, r);

                }
            }
        });

        fabReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (getHearingAid().connectionStatus != CONNECTING)
                    Configuration.instance().connectHA(side);

            }
        });

        earImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Configuration.instance().isHANotNull(side))
                    startScanningActivity(side);
                else {
                     Configuration.instance().alertDialog(getActivity().getString(R.string.remove_device_first)  , getActivity());

                }

            }
        });



        return view;
    }

    private class InitializeSDKParameters extends AsyncTask<Void, Integer, Void> {
        AsyncResult res;
        DeviceInfo deviceInfo;
        private HearingAidModel.Side side;
        private CommunicationAdaptor communicationAdaptor;
        private Product product;
        private int command;
        private ParameterSpace tmp_ps ;



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
                    case WRITE_TO_DEVICE:
                        res = product.beginWriteParameters(tmp_ps);
                        break;
                }
                isBusy = true;
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
            try {
                switch (command) {
                    case DETECT_DEVICE:
                        deviceInfo = communicationAdaptor.endDetectDevice(res);
                        initializeSDKParameters = new  InitializeSDKParameters(side, INITIALIZE_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    case INITIALIZE_DEVICE:
                        product.endInitializeDevice(res);

                        initializeSDKParameters = new  InitializeSDKParameters(side, READ_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    case READ_DEVICE:
                   //     Configuration.instance().showMessage(getActivity().getString(R.string.device_init_completed), getActivity());

                        //  progressView(View.GONE);
                        isActiveView(true);
                        isBusy = false;
                        getHearingAidModel(side).productInitialized = true;
                        getHearingAidModel(side).isConfigured = true;
                     //   updateViewValues(side);

                   //     progressDlg.hide();

                        break;
                    case WRITE_TO_DEVICE:
                        //    Configuration.instance().showMessage("Write to Device Complete", getActivity());
                        //   progressView(View.GONE);
                      //  progressDlg.hide();
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
