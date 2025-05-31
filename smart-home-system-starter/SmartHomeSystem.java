package com.smarthome;

import java.util.ArrayList;
import java.util.List;

public class SmartHomeSystem {

    public static void main(String[] args) {
        NotificationService notificationService = new NotificationService();
        List<Thread> deviceThreads = new ArrayList<>();

        Light livingRoomLight = new Light("L001", "Living Room Light");
        Thermostat homeThermostat = new Thermostat("T001", "Home Thermostat", 20.0, 25.0, notificationService);
         
        Thread lightThread = new Thread(livingRoomLight);
        Thread thermostatThread = new Thread(homeThermostat);
        
        deviceThreads.add(lightThread);
        deviceThreads.add(thermostatThread);
        
        lightThread.start();
        thermostatThread.start();
         
        Scheduler scheduler = new Scheduler();
        scheduler.scheduleTask(10000, () -> {
            livingRoomLight.turnOff();
            System.out.println("Scheduler: Turning off " + livingRoomLight.getName());
        });

        try {
            Thread.sleep(30000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
         
        for (Thread t : deviceThreads) {
            t.interrupt();
        }
        System.out.println("Smart Home Simulation stopped.");
    }
}
