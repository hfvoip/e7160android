/*
----------------------------------------------------------------------------
Copyright (c) 2016 Semiconductor Components Industries, LLC
(d/b/a ON Semiconductor). All Rights Reserved.

This code is the property of ON Semiconductor and may not be redistributed
in any form without prior written permission from ON Semiconductor. The
terms of use and warranty for this code are covered by contractual
agreements between ON Semiconductor and the licensee.
----------------------------------------------------------------------------
EventBusReceiverList.java
- EventBus Receiver list 
----------------------------------------------------------------------------
$Revision: 1.1 $
$Date: 2017/03/07 16:09:27 $
----------------------------------------------------------------------------
*/
package com.jhearing.e7160sl.Utils.EventsHadlers;

import java.util.concurrent.ConcurrentHashMap;

/**
 * EventBusReceiverList is a list of all receivers for events
 **/
public class EventBusReceiverList extends ConcurrentHashMap<EventReceiver<Object>, Boolean> {

    /**
     * put is called when a receiver is registered this will place the receiver in the receiver list.
     *
     * @param receiver the receiver object to place in the list.
     **/
    public void put(EventReceiver<Object> receiver) {
        put(receiver, Boolean.FALSE);
    }

}
