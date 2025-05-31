package com.smarthome;

// Base class for all devices 
public abstract class Device implements Runnable {
    protected String deviceId;
    protected String name;

    public Device(String deviceId, String name) {
        this.deviceId = deviceId;
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }
    
    public String getName() {
        return name;
    }
    
    public abstract void simulate();

    @Override
    public void run() {
        simulate();
    }
}
