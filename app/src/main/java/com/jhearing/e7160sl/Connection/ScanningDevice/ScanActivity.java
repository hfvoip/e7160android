package com.jhearing.e7160sl.Connection.ScanningDevice;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ark.ArkException;
import com.ark.AsyncResult;
import com.ark.CommunicationPort;
import com.ark.WirelessProgrammerType;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ScanEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ScanActivity extends AppCompatActivity {
    public static final int GPS_ENABLE_CODE = 3;
    private static final String TAG = ScanActivity.class.getSimpleName();
    private static final int DEVICE_NAME = 0;
    private static final int DEVICE_MAC = 1;
    private static final int DEVICE_RSSI = 2;
    private static final int MANUFACTURING_DATA = 3;
    private static HearingAidModel.Side side;
    ArrayList<BLEDeviceWrapper> bleDeviceList = new ArrayList<>();
    BlEAdapter bleAdaptor;
    ListView listView;

    private AsyncResult asyncResult;
    private EventReceiver<ScanEvent> scanEventEventReceiver = new EventReceiver<ScanEvent>() {
        @Override
        public void onEvent(String name, ScanEvent data) throws JSONException {

            try {
                JSONObject jsonObject = new JSONObject(data.data);
                Log.d(TAG,"l53:"+jsonObject.toString());
                JSONArray scanEvent = jsonObject.getJSONArray(getString(R.string.event_json_obj));
                JSONObject manufacturingData = scanEvent.getJSONObject(MANUFACTURING_DATA);

                String manuData = manufacturingData.getString(getString(R.string.device_manu_data_json));
                try {
                    HearingAidModel.Side advertisingSide = (manuData.substring(4, 6).equals(getString(R.string.left_advertising_data))) ? HearingAidModel.Side.Left : HearingAidModel.Side.Right;
                    if (advertisingSide == side) {
                        BLEDeviceWrapper deviceWrapper = parseJsonData(data);
                        //by yang ,7160都有给名字
                        if (!isDuplicate(deviceWrapper) && !isEmtpyName(deviceWrapper)) {
                            bleDeviceList.add(deviceWrapper);
                            bleAdaptor.notifyDataSetChanged();
                        }
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.e(TAG, e.getMessage());
                }

            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BLEDeviceWrapper parseJsonData(ScanEvent data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data.data);
        JSONArray scanEvent = jsonObject.getJSONArray(getString(R.string.event_json_obj));
        JSONObject deviceName = scanEvent.getJSONObject(DEVICE_NAME);
        JSONObject deviceMAC = scanEvent.getJSONObject(DEVICE_MAC);
        JSONObject deviceRSSI = scanEvent.getJSONObject(DEVICE_RSSI);
        JSONObject manufacturingData = scanEvent.getJSONObject(MANUFACTURING_DATA);
        return new BLEDeviceWrapper(deviceMAC.getString(getString(R.string.device_id_json)), deviceName.getString(getString(R.string.device_name_json)), deviceRSSI.getInt(getString(R.string.device_rssi_json)), manufacturingData.getString(getString(R.string.device_manu_data_json)), side);
    }

    private boolean isEmtpyName(BLEDeviceWrapper device) {
        boolean value = false;
        if ((device.getName() == "") || (device.getName() =="<unknown>"))
              return true;
        return value;
    }
    private boolean isDuplicate(BLEDeviceWrapper device) {
        boolean value = false;
        for (BLEDeviceWrapper deviceWrapper : bleDeviceList) {
            value = deviceWrapper.getAddress().equals(device.getAddress());
        }
        return value;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.registerReceiver(scanEventEventReceiver, ScanEvent.class.getSimpleName());
        startScanning();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.unregisterReceiver(scanEventEventReceiver);
        stopScanning();
    }


    private void setCommunicationPort() {
        String cSide = getIntent().getStringExtra(getString(R.string.HA_SIDE));
        if (cSide.equals(HearingAidModel.Side.Left.name())) {
            Configuration.instance().communicationPort = CommunicationPort.kLeft;
            side = HearingAidModel.Side.Left;
        } else {
            Configuration.instance().communicationPort = CommunicationPort.kRight;
            side = HearingAidModel.Side.Right;
        }
    }

    private void setToolBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeAsUpIndicator(null);
        }

    }

    private void startScanning() {
        try {
            asyncResult = Configuration.getProductManager().beginScanForWirelessDevices(WirelessProgrammerType.kPlatformDefault, "", Configuration.instance().communicationPort, "", false);
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void stopScanning() {
        try {
            Configuration.getProductManager().endScanForWirelessDevices(asyncResult);
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private void preSelectConfig(int position) {
        BLEDeviceWrapper device = bleDeviceList.get(position);
        if (device.getSide() != null) {
            autoFitting(device, side);
        }
    }

    private void autoFitting(BLEDeviceWrapper device, HearingAidModel.Side side) {
        if (!Configuration.instance().isHANotNull(side)) {
            Configuration.instance().addHearingAid(side, device);
        }
    }


    private HearingAidModel getHearingAidModel(HearingAidModel.Side side) {
        return Configuration.instance().getDescriptor(side);
    }

    private void setListView() {
        listView = (ListView) findViewById(R.id.list_view);
        bleAdaptor = new BlEAdapter(this, bleDeviceList);
        listView.setAdapter(bleAdaptor);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                preSelectConfig(position);
                stopScanning();
                startConnecting(getHearingAidModel(side));

                Intent returnIntent = new Intent();
                returnIntent.putExtra("HA_Side", side.name());
                setResult(Activity.RESULT_OK, returnIntent);

                finish();
            }
        });
    }

    private void startConnecting(HearingAidModel hearingAidModel) {
        try {
            getHearingAidModel(hearingAidModel.getSide()).communicationAdaptor.connect();
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private void checkGPSEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Configuration.instance().enableGPS();
        } else {
            Configuration.instance().showMessage(getString(R.string.gps_enabled), this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case GPS_ENABLE_CODE:
                if (requestCode != RESULT_OK) {
                    Configuration.instance().showMessage(getString(R.string.gps_exiting), this);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("HA_Side", side.name());
                    setResult(Activity.RESULT_CANCELED, returnIntent);
                    finish();
                }
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCommunicationPort();
        setContentView(R.layout.activity_scan);

        setToolBar();
        checkGPSEnabled();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.rescan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopScanning();
                startScanning();
            }
        });
        setListView();

    }
}
