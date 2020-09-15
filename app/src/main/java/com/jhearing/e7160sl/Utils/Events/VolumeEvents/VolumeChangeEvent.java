/*
----------------------------------------------------------------------------
Copyright (c) 2016 Semiconductor Components Industries, LLC
(d/b/a ON Semiconductor). All Rights Reserved.

This code is the property of ON Semiconductor and may not be redistributed
in any form without prior written permission from ON Semiconductor. The
terms of use and warranty for this code are covered by contractual
agreements between ON Semiconductor and the licensee.
----------------------------------------------------------------------------
VolumeChangedEvent.java
- Volume has changed event for volume feature
----------------------------------------------------------------------------
$Revision: 1.1 $
$Date: 2017/03/07 16:09:26 $
----------------------------------------------------------------------------
*/
package com.jhearing.e7160sl.Utils.Events.VolumeEvents;

import com.jhearing.e7160sl.Utils.Events.DeviceSpecificEvent;


/**
 * VolumeChangeEvent is used to signify a change in the volume level on the hearing aid
 **/
public class VolumeChangeEvent extends DeviceSpecificEvent {

    public final byte volume;

    /**
     * VolumeChangeEvent is called to initialize a volume change event
     *
     * @param address the address of the HA reporting its volume level.
     * @param volume  the volume level being reported from the HA device.
     **/
    public VolumeChangeEvent(String address, byte volume) {
        super(address);
        this.volume = volume;
    }

    @Override
    public String toString() {
        return super.toString() + ", VolumeChangeEvent{" +
                "volume=" + volume +
                '}';
    }
}
