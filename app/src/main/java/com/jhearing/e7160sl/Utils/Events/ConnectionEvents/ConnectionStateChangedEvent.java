package com.jhearing.e7160sl.Utils.Events.ConnectionEvents;


import com.jhearing.e7160sl.Utils.Events.DeviceSpecificEvent;

/**
 * ConnectionStateChangedEvent is used to signify a change in the connection state of the hearing aid
 **/
public class ConnectionStateChangedEvent extends DeviceSpecificEvent {

    public final int connectionStatus;

    /**
     * Constructor
     * ConnectionStateChangedEvent is called to initialize a bond state change event
     *
     * @param address          the address of the remote device reporting the connection state change
     * @param connectionStatus is the new connection state value sent back in the event the value is
     *                         true for connected and false for not connected.
     **/
    public ConnectionStateChangedEvent(String address, int connectionStatus) {
        super(address);
        this.connectionStatus = connectionStatus;
    }

    @Override
    public String toString() {
        return super.toString() + ", ConnectionStateChangedEvent{" +
                "connectionStatus=" + connectionStatus +
                '}';
    }
}
