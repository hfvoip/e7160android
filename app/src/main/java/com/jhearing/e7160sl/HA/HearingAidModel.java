/*
----------------------------------------------------------------------------
Copyright (c) 2016 Semiconductor Components Industries, LLC
(d/b/a ON Semiconductor). All Rights Reserved.

This code is the property of ON Semiconductor and may not be redistributed
in any form without prior written permission from ON Semiconductor. The
terms of use and warranty for this code are covered by contractual
agreements between ON Semiconductor and the licensee.
----------------------------------------------------------------------------
HearingAidModel.java
- Hearing Aid Model used to support individual hearing aids
----------------------------------------------------------------------------
$Revision: 1.6 $
$Date: 2017/05/29 19:52:33 $
----------------------------------------------------------------------------
*/
package com.jhearing.e7160sl.HA;


import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.ark.ArkException;
import com.ark.CommunicationAdaptor;
import com.ark.DeviceInfo;
import com.ark.Manufacturing;
import com.ark.ParameterList;
import com.ark.ParameterMemory;
import com.ark.ParameterMemoryList;
import com.ark.ParameterSpace;
import com.ark.Product;
import com.ark.ProductDefinition;
import com.ark.ProductDefinitionList;
import com.ark.WirelessCommunicationAdaptorStateType;
import com.ark.WirelessControl;
import com.jhearing.e7160sl.Utils.Events.BatteryEvents.BatteryStateChangedEvent;
import com.jhearing.e7160sl.Utils.Events.ConnectionEvents.ConnectionStateChangedEvent;
import com.jhearing.e7160sl.Utils.Events.CurrentMemoryCharacteristicChangedEvent;
import com.jhearing.e7160sl.Utils.Events.VolumeEvents.VolumeChangeEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;

import java.util.HashMap;


/**
 * Data object defining the model of the data associated with a hearing aid.
 */
public class HearingAidModel {

    private static final String TAG = HearingAidModel.class.getName();
    static private int instanceCount = 0;

    public boolean productInitialized = false;

    public CommunicationAdaptor communicationAdaptor;

    public WirelessControl wirelessControl;

    public boolean autoConnect = Configuration.instance().autoConnect;


    public boolean isConfigured = false;

    public HashMap<String, ParameterSpace> MemoriesMap = new HashMap<>();

    /**
     * Variable indicating if this HA is connected or not
     */
    public boolean connected;

    public int numberOfMemories;

    public int connectionStatus = 1;
    /**
     * The name of the associated hearing aid
     */
    public String name;
    /**
     * The MAC address of the associated hearing aid
     */
    public String address;
    /**
     * The user defined friendly name of the hearing aid
     */
    public String friendlyName;
    /**
     * The volume of the hearing aid, we expect this to be 5 by default
     */
    public int volume = 100;
    /**
     * The current memory of the hearing aid, we expect this to be 0 by default
     */
    public int currentMemory = 0;
    /**
     * The last reported level of the battery for the hearing aid.
     */
    public int batteryLevel = -1;
    /**
     * variable that stores the manufacturer specific data information
     */
    public String manufacturerSpecificData;
    /**
     * Remember which side this particular hearing aid model is expecting.
     */
    public Side side = null;
    public Product product;
    public Manufacturing  manufacturing;
    public String serialno;
    public String macaddress;
    public String lib_id;
    public String product_id;
    public String firmware_id;
    public ProductDefinitionList productList;
    public ProductDefinition productDefinition;
    public ParameterMemoryList memories;
    public ParameterMemory memoryA;

    public ParameterMemory systemMemory;
    public ParameterList parameters;
    public ParameterList systemParameters;
    public ParameterList[] arr_parameters = new ParameterList[255];

    private boolean cleanUp = false;
    /**
     * The BluetoothDevice associated with the hearing aid.
     */
    BluetoothDevice bluetoothDevice;
    private EventReceiver<ConnectionStateChangedEvent> ConnectionStateChangedHandler
            = new EventReceiver<ConnectionStateChangedEvent>() {
        @Override
        public void onEvent(String name, ConnectionStateChangedEvent data) {
            if (data.address.equals(address)) {
                connectionStatus = data.connectionStatus;
                if (WirelessCommunicationAdaptorStateType.kDisconnected.ordinal() == data.connectionStatus) {
                    whenDisconnected();
                } else if (WirelessCommunicationAdaptorStateType.kConnected.ordinal() == data.connectionStatus) {
                    whenConnected();
                }
                issueConfigurationChangedEvent();
            }
        }
    };
    private EventReceiver<BatteryStateChangedEvent> BatteryStateChangedHandler
            = new EventReceiver<BatteryStateChangedEvent>() {
        @Override
        public void onEvent(String name, BatteryStateChangedEvent data) {
            if (data.address.equals(address)) {
                if (data.level != batteryLevel) {
                    batteryLevel = data.level;
                    issueConfigurationChangedEvent();
                }
            }

        }
    };
    private EventReceiver<CurrentMemoryCharacteristicChangedEvent> currentMemoryChangedEventHandler = new EventReceiver<CurrentMemoryCharacteristicChangedEvent>() {

        @Override
        public void onEvent(String name, CurrentMemoryCharacteristicChangedEvent data) {
            if (data.address.equals(address)) {
                if (data.currentMemory != currentMemory) {
                    currentMemory = data.currentMemory;
                    issueConfigurationChangedEvent();
                }
            }
        }
    };
    private EventReceiver<VolumeChangeEvent> volumeChangeEvent = new EventReceiver<VolumeChangeEvent>() {
        @Override
        public void onEvent(String name, VolumeChangeEvent data) {

            if (data.address.equals(address)) {
                if (volume != data.volume) {
                    Log.i(TAG, "EV volume of side " + side + "is " + data.volume);
                    volume = data.volume;
                    issueConfigurationChangedEvent();
                }
            }
        }
    };

    /**
     * Constructor for a hearing aid.
     * This method saves the side locally that it represents and registers to receive all events that
     * pertain to it (such as volume level change etc.)
     *
     * @param side The side on which the hearing aid will be worn, this is immutable
     */
    public HearingAidModel(Side side, String address) {
        this.side = side;
        this.address = address;
        instanceCount++;
        Log.i(TAG, "instance count: " + instanceCount);
        setWirelessControlAndMemoryValues();
        initParameters();
        registerReceiver();
    }

    private void whenDisconnected() {
        connected = false;
        isConfigured = false;
        // if (Configuration.instance().autoConnect && autoConnect) {
        //     try {
        //         Log.e(TAG, "Attempting Reconnect");
        //         communicationAdaptor.connect();
        //     } catch (ArkException e) {
        //         Log.e(TAG, e.getMessage());
        //     }
        // } else {
            if (cleanUp) {
                try {
                    communicationAdaptor.closeDevice();
                } catch (ArkException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
                unregisterReceivers();
            }

        // }
    }

    private void whenConnected() {
        connected = true;
        try {
            batteryLevel = wirelessControl.getBatteryLevel();

            //sync values to whats saved if not default
            if (volume != wirelessControl.getVolume()) {
                wirelessControl.setVolume(volume);
            }

            if (currentMemory != (wirelessControl.getCurrentMemory().ordinal()) - 4) {
                wirelessControl.setCurrentMemory(ParameterSpace.values()[currentMemory + 4]);
            }

        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
        setNumberOfMemories();
    }

    private void setNumberOfMemories() {
        try {
            numberOfMemories = wirelessControl.getNumberOfMemories();
            // Memory HashMap for view
            for (int i = 0; i < numberOfMemories; i++) {
                MemoriesMap.put("Memory " + (i + 1), ParameterSpace.values()[i + 4]);
            }
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void initParameters() {
        try {
            productList = Configuration.getEzairoLib().getProducts();

            manufacturing = Configuration.getProductManager().getManufacturing();

            // For the purposes of the sample code here just get the first product in the list
            // There is only one product defined in the demo library used here
            productDefinition = productList.getItem(0);

            // Create an instance of this product from its definition
            product = productDefinition.createProduct();

            // From the product get the parameter memory list
            memories = product.getMemories();

            // For the purposes of the sample code here just get the first memory in the list
            memoryA = memories.getItem(0);

            // From the product get the system memory
            systemMemory = product.getSystemMemory();
            // Get the parameter list from system memory
            systemParameters = systemMemory.getParameters();

            // Get the parameter list from the first memory
            parameters = memoryA.getParameters();
            for (int i=0;i<8;i++) {
                arr_parameters[i] = memories.getItem(i).getParameters();
            }


        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void setWirelessControlAndMemoryValues() {
        try {
            communicationAdaptor = Configuration.getProductManager().createWirelessCommunicationInterface(address);
            communicationAdaptor.setEventHandler(Configuration.getProductManager().getEventHandler());
            wirelessControl = Configuration.getProductManager().getWirelessControl();
            wirelessControl.setCommunicationAdaptor(communicationAdaptor);
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Getter for the hearing aid side.
     *
     * @return The side as an enum value Left or Right
     */
    public Side getSide() {
        return side;
    }

    private void issueConfigurationChangedEvent() {
        Configuration.instance().issueConfigurationChangedEvent(address);
    }


    /**
     * Method to register this hearing aid for all events it would be interested in such as the
     * volume level change event etc.
     */
    void registerReceiver() {
        EventBus.registerReceiver(currentMemoryChangedEventHandler, CurrentMemoryCharacteristicChangedEvent.class.getName());
        EventBus.registerReceiver(BatteryStateChangedHandler, BatteryStateChangedEvent.class.getName());
        EventBus.registerReceiver(volumeChangeEvent, VolumeChangeEvent.class.getName());
        EventBus.registerReceiver(ConnectionStateChangedHandler, ConnectionStateChangedEvent.class.getName());

    }

    /**
     * Method to register this hearing aid for all events it would be interested in such as the
     * volume level change event etc.
     */
    public void unregisterReceivers() {

        EventBus.unregisterReceiver(BatteryStateChangedHandler);
        EventBus.unregisterReceiver(ConnectionStateChangedHandler);
        EventBus.unregisterReceiver(volumeChangeEvent);
        EventBus.unregisterReceiver(currentMemoryChangedEventHandler);
    }

    public void cleanup() {
        cleanUp = true;
    }

    /**
     * Hearing aids can be worn on the left or right so define an enum to capture this.
     */
    public enum Side {
        Left, Right
    }

}