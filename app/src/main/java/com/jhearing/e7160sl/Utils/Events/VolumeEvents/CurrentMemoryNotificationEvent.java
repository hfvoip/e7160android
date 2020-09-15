package com.jhearing.e7160sl.Utils.Events.VolumeEvents;


import com.jhearing.e7160sl.Utils.Events.DeviceSpecificEvent;

/**
 * ConfigurationChangedEvent is used to signify a change in the configuration has happened
 **/
public class CurrentMemoryNotificationEvent extends DeviceSpecificEvent {

    public final byte currentMemory;

    /**
     * VolumeChangeEvent is called to initialize a volume change event
     *
     * @param address       the address of the HA reporting its volume level.
     * @param currentMemory the currentMemory level being reported from the HA device.
     **/
    public CurrentMemoryNotificationEvent(String address, byte currentMemory) {
        super(address);
        this.currentMemory = currentMemory;
    }

    @Override
    public String toString() {
        return super.toString() + ", CurrentMemoryNotificationChange{" +
                "CurrentMemory=" + currentMemory +
                '}';
    }
}
