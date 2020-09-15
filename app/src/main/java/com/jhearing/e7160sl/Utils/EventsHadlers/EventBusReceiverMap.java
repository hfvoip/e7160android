/*
----------------------------------------------------------------------------
Copyright (c) 2016 Semiconductor Components Industries, LLC
(d/b/a ON Semiconductor). All Rights Reserved.

This code is the property of ON Semiconductor and may not be redistributed
in any form without prior written permission from ON Semiconductor. The
terms of use and warranty for this code are covered by contractual
agreements between ON Semiconductor and the licensee.
----------------------------------------------------------------------------
EventBusReceiverMap.java
- EventBus Receiver Map 
----------------------------------------------------------------------------
$Revision: 1.1 $
$Date: 2017/03/07 16:09:27 $
----------------------------------------------------------------------------
*/
package com.jhearing.e7160sl.Utils.EventsHadlers;

import android.util.Log;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventBusReceiverMap is used to store the receivers for events.
 * Abstracting the name to receiver mapping for the event bus, this only deals
 * with the container and handling for the mapping, all sticky events and notifications
 * remain in the EventBus class.
 **/
public class EventBusReceiverMap extends ConcurrentHashMap<String, EventBusReceiverList> {

    public static final String ALL = "__all__";

    /**
     * registerReceiver is called when a receiver is registering to receive the events listed in
     * eventNames.
     *
     * @param receiver   of the event type (eg. VolumeChangeEvent).
     * @param eventNames the list of events the receiver wants to register to receive.
     **/
    public String[] registerReceiver(EventReceiver<Object> receiver, String... eventNames) {
        if (eventNames.length == 0)
            eventNames = new String[]{ALL};
        for (String action : eventNames) {
            receiversForEventName(action).put(receiver);


        }
        return eventNames;
    }

    /**
     * unregisterReceiver is called when a receiver is unregistering so as not to receive any
     * events, this will remove the receiver from all events it was previously registered for.
     *
     * @param receiver to remove from the events.
     **/
    public void unregisterReceiver(EventReceiver<Object> receiver) {
        for (String eventName : keySet()) {

            EventBusReceiverList receivers = receiversForEventName(eventName);
            receivers.remove(receiver);
            if (receivers.isEmpty())
                remove(eventName);
        }
    }

    private EventBusReceiverList receiversForEventName(String name) {

        EventBusReceiverList map = get(name);
        if (map == null) {
            map = new EventBusReceiverList();
            put(name, map);
        }
        Log.d("wsmessage","size is:"+map.size()+",name is:"+name);
        return map;
    }

    /**
     * receiversForEvent returns a hashset of receivers for a given event (specified in the name
     * parameter.
     *
     * @param name The name of the event from which to gather all receivers
     **/
    public HashSet<EventReceiver<Object>> receiversForEvent(String name) {
        HashSet<EventReceiver<Object>> receivers = new HashSet<>();
        receivers.addAll(receiversForEventName(ALL).keySet());
        receivers.addAll(receiversForEventName(name).keySet());
        return receivers;
    }

}
