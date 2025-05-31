package com.smarthome;

public class Light extends Device {
    private boolean isOn;

    public Light(String deviceId, String name) {
        super(deviceId, name);
        this.isOn = false;
    }

    public void turnOn() {
        isOn = true;
    }
    
    public void turnOff() {
        isOn = false;
    }
    
    public boolean getStatus() {
        return isOn;
    }

    @Override
    public void simulate() {
        // TODO: Implement your simulation logic.
        
    }
}
