package com.jhearing.e7160sl.Utils.Events.VolumeEvents;

import com.jhearing.e7160sl.Utils.Events.DeviceSpecificEvent;


/**
 * ConfigurationChangedEvent is used to signify a change in the configuration has happened
 **/
public class CurrentMemoryCharacteristicChangedEvent extends DeviceSpecificEvent {

    public final byte currentMemory;

    /**
     * CurrentMemoryCharacteristicsChangedEvent is called to initialize a volume change event
     *
     * @param address       the address of the HA reporting its volume level.
     * @param currentMemory current memory value
     **/
    public CurrentMemoryCharacteristicChangedEvent(String address, byte currentMemory) {
        super(address);
        this.currentMemory = currentMemory;
    }

    @Override
    public String toString() {
        return super.toString() + ", CurrentMemoryValueChanged{" +
                "Memory=" + currentMemory +
                '}';
    }
}
