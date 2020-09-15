package com.jhearing.e7160sl.Utils.Events;

/**
 * ConfigurationChangedEvent is used to signify a change in the configuration has happened
 **/
public class ConfigurationChangedEvent extends DeviceSpecificEvent {

    /**
     * Constructor
     * ConfigurationChangedEvent is called to initialize a configuration change event
     *
     * @param address the address of the HA reporting its configuration change.
     **/
    public ConfigurationChangedEvent(String address) {
        super(address);
    }

    @Override
    public String toString() {
        return super.toString() + "ConfigurationChangedEvent{" + address + "}";
    }


}
