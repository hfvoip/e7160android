package com.jhearing.e7160sl.Utils.Events.VolumeEvents;


import com.jhearing.e7160sl.Utils.Events.DeviceSpecificEvent;

/**
 * ConfigurationChangedEvent is used to signify a change in the configuration has happened
 **/
public class VolumeNotificationEvent extends DeviceSpecificEvent {

    public final byte volume;

    /**
     * VolumeChangeEvent is called to initialize a volume change event
     *
     * @param address the address of the HA reporting its volume level.
     * @param volume  the volume level being reported from the HA device.
     **/
    public VolumeNotificationEvent(String address, byte volume) {
        super(address);
        this.volume = volume;
    }

    @Override
    public String toString() {
        return super.toString() + ", VolumeChangedNotificationEvent{" +
                "volume=" + volume +
                '}';
    }
}
