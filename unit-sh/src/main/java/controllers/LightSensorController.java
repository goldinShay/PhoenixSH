package controllers;

import devices.SmartLight;
import sensors.Sensor;

import java.util.List;
import java.util.stream.Collectors;

public class LightSensorController {

    public static void evaluateAutoOp(Sensor sensor) {
        List<SmartLight> linkedLights = sensor.getLinkedDevice().stream()
                .filter(device -> device instanceof SmartLight)
                .map(device -> (SmartLight) device)
                .collect(Collectors.toList());

        double currentValue = sensor.getCurrentReading(); // Or from Excel

        for (SmartLight light : linkedLights) {
            double threshold = light.getAutoThreshold();

            if (currentValue >= threshold && !light.isAutomationEnabled()) {
                light.setAutomationEnabled(true);
                SmartLightController.updateSmartLight(light);
                System.out.println("💡 AutoOp ON for " + light.getId());
            } else if (currentValue < threshold && light.isAutomationEnabled()) {
                light.setAutomationEnabled(false);
                SmartLightController.updateSmartLight(light);
                System.out.println("💤 AutoOp OFF for " + light.getId());
            }
        }
    }
}

