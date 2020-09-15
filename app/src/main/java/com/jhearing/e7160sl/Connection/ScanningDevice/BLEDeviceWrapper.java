/*
----------------------------------------------------------------------------
Copyright (c) 2016 Semiconductor Components Industries, LLC
(d/b/a ON Semiconductor). All Rights Reserved.

This code is the property of ON Semiconductor and may not be redistributed
in any form without prior written permission from ON Semiconductor. The
terms of use and warranty for this code are covered by contractual
agreements between ON Semiconductor and the licensee.
----------------------------------------------------------------------------
EventBus.java
- EventBus related functions 
----------------------------------------------------------------------------
$Revision: 1.6 $
$Date: 2016/04/21 04:02:29 $
----------------------------------------------------------------------------
*/

package com.jhearing.e7160sl.Connection.ScanningDevice;

import com.jhearing.e7160sl.HA.HearingAidModel;

/**
 * BLEDeviceWrapper class creates a wrapper around a BLE device.
 */
public class BLEDeviceWrapper {


    private final String address;
    private final String name;
    private final int rssi;
    private final HearingAidModel.Side side;
    //    HearingAidModel.Side side;
    String manufacturerData;
    private int bondState;

    /**
     * Constructor
     * BLEDeviceWrapper called to initialize a wrapper around a BLE device
     * //     * @param device the BLE device which represents the HA.
     *
     * @param rssi the current RSSI value for this BLE device
     *             //     * @param side the hearing aid side this device represents (left, right)
     */
    public BLEDeviceWrapper(String address, String name, int rssi, String manufacturerData, HearingAidModel.Side side) {
        this.address = address;
        this.side = side;
        this.name = name;
        this.rssi = rssi;
        this.manufacturerData = manufacturerData;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public int getRssi() {
        return rssi;
    }

    public String getManufacturerData() {
        return manufacturerData;
    }

    public void setManufacturerData(String manufacturerData) {
        this.manufacturerData = manufacturerData;
    }

    public HearingAidModel.Side getSide() {
        return side;
    }


}
