/*
----------------------------------------------------------------------------
Copyright (c) 2016 Semiconductor Components Industries, LLC
(d/b/a ON Semiconductor). All Rights Reserved.

This code is the property of ON Semiconductor and may not be redistributed
in any form without prior written permission from ON Semiconductor. The
terms of use and warranty for this code are covered by contractual
agreements between ON Semiconductor and the licensee.
----------------------------------------------------------------------------
DeviceSpecificEvent.java
- A device specific Event for device related features
----------------------------------------------------------------------------
$Revision: 1.1 $
$Date: 2017/03/07 16:09:27 $
----------------------------------------------------------------------------
*/
package com.jhearing.e7160sl.Utils.Events;

/**
 * DeviceSpecificEvent is used to signify a change in a device has occurred.
 **/
public  class WssendreqEvent extends DeviceSpecificEvent  {

    public final String msgbody;

    /**
     * Constructor
     * DeviceSpecificEvent is called to initialize a device specific event
     *
     * @param msgbody the address of the device initiating the event.
     **/

    public WssendreqEvent(String address,String msgbody) {
        super(address);
        this.msgbody = msgbody;
    }

    @Override
    public String toString() {
        return "WssendreqEvent{" +
                "msgbody='" + msgbody + '\'' +
                '}';
    }
}
