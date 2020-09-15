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
$Revision: 1.1 $
$Date: 2017/03/07 16:09:27 $
----------------------------------------------------------------------------
*/

/**
 * Copyright 2015 Alex Yanchenko
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jhearing.e7160sl.Utils.EventsHadlers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;


/**
 * EventBus class supports sending events to registered event listeners without having
 * to use the broadcast receivers.
 **/
public class EventBus {

    private static final Object NULL = new Object();
    private static final EventBusReceiverMap eventNameToReceivers = new EventBusReceiverMap();
    private static final ConcurrentHashMap<String, Object> stickyEvents = new ConcurrentHashMap<>();
    private static Handler handler;

    public static EventBusReceiverMap getEventNameToReceivers() {
        return eventNameToReceivers;
    }

    /**
     * postEvent is called when an event needs to be sent and is posted for sending.
     *
     * @param name of the event type (eg. VolumeChangeEvent).
     **/
    public static void postEvent(String name) {
        postEvent(name, null);
    }

    /**
     * postEvent is called when an event needs to be sent and is posted for sending.
     *
     * @param name of the event type (eg. VolumeChangeEvent).
     * @param data the data being sent for the event (eg. Volume level).
     **/
    public static void postEvent(String name, Object data) {
        Log.d("EVBUS", "Posting event: " + name + ", " + data.toString());
        runOnUiThread(new PostEventRunnable(name, data));
    }

    /**
     * postEventSticky is called when an event needs to be sent and is posted for sending.
     *
     * @param name of the event type (eg. VolumeChangeEvent).
     **/
    public static void postEventSticky(String name) {
        postEventSticky(name, null);
    }

    /**
     * postEventSticky is called when an event needs to be sent and and is posted for sending.
     *
     * @param name of the event type (eg. VolumeChangeEvent).
     * @param data the data being sent for the event (eg. Volume level).
     **/
    public static void postEventSticky(String name, Object data) {
        stickyEvents.put(name, (data == null) ? NULL : data);
        postEvent(name, data);
    }

    /**
     * clearStickyEvents is called when an event or all events need to be cleared from the sticky hash set.
     *
     * @param eventNames: list of events to be removed from the hash set, if zero all events are removed).
     **/
    public static void clearStickyEvents(String... eventNames) {
        if (eventNames.length == 0) {
            stickyEvents.clear();
        } else {
            HashSet<String> nameSet = new HashSet<>(Arrays.asList(eventNames));
            for (String eventName : stickyEvents.keySet())
                if (nameSet.contains(eventName))
                    stickyEvents.remove(eventName);
        }
    }

    /**
     * registerReceiver is called when a receiver is to be registered for an event(s).
     *
     * @param receiver   Event receiver to remove (unregister) from receiving events.
     * @param eventNames list of event type (eg. VolumeChangeEvent).
     **/
    public static void registerReceiver(EventReceiver<?> receiver, String... eventNames) {
        EventReceiver<Object> objectReceiver = (EventReceiver<Object>) receiver;
        Log.d("EVBUS", "Registering : " + receiver.getClass().getName() + " of event: " + eventNames[0]);
        String[] actualEventNames = eventNameToReceivers.registerReceiver(objectReceiver, eventNames);
        for (String name : actualEventNames) {
            Object data = stickyEvents.get(name);
            if (data != null)
                runOnUiThread(new StickyEventRunnable(objectReceiver, name, (data == NULL) ? null : data));
        }
    }

    /**
     * unregisterReceiver is called when a receiver no longer wants to receive events and is
     * unregistering for those event(s).
     *
     * @param receiver: The receiver to remove (unregister) from events.
     **/
    public static void unregisterReceiver(EventReceiver<?> receiver) {
        eventNameToReceivers.unregisterReceiver((EventReceiver<Object>) receiver);
        Log.d("EVBUS", "Unregistering: " + receiver.getClass().getName());
    }

    private static void notifyReceiver(EventReceiver<Object> receiver, String event, Object data) {
        try {
            Log.d("EVBUS", "Notifying: " + receiver.getClass().getName() + " of event: " + event + ", " + data.toString());
            receiver.onEvent(event, data);
        } catch (IllegalArgumentException e) {
            Log.w("EVBUS", format("Failed to deliver event %s to %s: %s.", event, receiver.getClass().getName(), e.getMessage()));
        } catch (Exception e) {
            Log.w("EVBUS", e);
            Log.w("EVBUS", "Receiver unregistered.");
            unregisterReceiver(receiver);
        }

    }

    private static void runOnUiThread(Runnable r) {
        // hack to make sure the runner is posted, keep trying until success
        do {
            if (handler == null)
                handler = new Handler(Looper.getMainLooper());
            handler = handler.post(r) ? handler : null;
        } while (handler == null);
    }

    private static class PostEventRunnable implements Runnable {

        private final String name;
        private final Object data;

        public PostEventRunnable(String name, Object data) {
            this.name = name;
            this.data = data;
        }

        @Override
        public void run() {
            HashSet<EventReceiver<Object>> receivers = eventNameToReceivers.receiversForEvent(name);
            for (EventReceiver<Object> receiver : receivers)
                notifyReceiver(receiver, name, data);
        }
    }

    private static class StickyEventRunnable implements Runnable {

        private final EventReceiver<Object> receiver;
        private final String name;
        private final Object data;

        public StickyEventRunnable(EventReceiver<Object> receiver, String name, Object data) {
            this.receiver = receiver;
            this.name = name;
            this.data = data;
        }

        @Override
        public void run() {
            notifyReceiver(receiver, name, data);
        }
    }

}