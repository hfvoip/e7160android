package com.jhearing.e7160sl.Utils.Events.BatteryEvents;


import com.jhearing.e7160sl.Utils.Events.DeviceSpecificEvent;

/**
 * BatteryStateChangedEvent is used to signify a change in the battery level of the hearing aid
 **/
public class BatteryStateChangedEvent extends DeviceSpecificEvent {

    public final int level;

    /**
     * Constructor
     * BatteryStateChangedEvent is called to initialize a battery state change event
     *
     * @param address the address of the remote device reporting the battery level change
     * @param level   the battery level being reported by the hearing aid device
     **/
    public BatteryStateChangedEvent(String address, int level) {
        super(address);
        this.level = level;
    }

    @Override
    public String toString() {
        return super.toString() + ", BatteryStateChangedEvent{" +
                "level=" + level +
                '}';
    }
}
