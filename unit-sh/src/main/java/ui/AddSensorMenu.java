package ui;

import sensors.Sensor;
import sensors.SensorFactory;
import sensors.SensorType;
import sensors.MeasurementUnit;
import storage.SensorStorage;
import storage.XlCreator;

import java.time.Clock;
import java.util.Scanner;

import static ui.Menu.capitalize;

public class AddSensorMenu {

    private static final Clock clock = Clock.systemDefaultZone();

    public static void run(String name) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== Sensor Wizard ===");

        // 1️⃣ Select Sensor Type
        SensorType sensorType = chooseSensorType(scanner);
        if (sensorType == null) {
            System.out.println("❌ Sensor creation cancelled.");
            return;
        }

        // 2️⃣ Choose Measurement Unit
        MeasurementUnit unit = chooseMeasurementUnit(scanner);
        if (unit == MeasurementUnit.UNKNOWN) {
            System.out.println("❌ Invalid unit selection. Sensor creation cancelled.");
            return;
        }

        // 3️⃣ Enter current value
        System.out.print("Enter default value for the sensor: ");
        double currentValue;
        try {
            currentValue = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid default value.");
            return;
        }

        // 4️⃣ Create and store the sensor
        try {
            Sensor sensor = SensorFactory.createSensor(sensorType, null, name, unit, currentValue, clock);
            String id = sensor.getSensorId();

            SensorStorage.getSensors().put(id, sensor);
            XlCreator.writeSensorToExcel(sensor);

            System.out.printf("✅ Sensor '%s' (%s) created successfully!%n", name, id);
        } catch (Exception e) {
            System.out.println("❌ Failed to create sensor: " + e.getMessage());
        }
    }


    private static SensorType chooseSensorType(Scanner scanner) {
        System.out.println("\nSelect Sensor Type:");
        SensorType[] types = SensorType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%d - %s%n", i + 1, capitalize(types[i].toString()));
        }

        System.out.print("Enter your choice: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > types.length) return null;
            return types[choice - 1];
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static MeasurementUnit chooseMeasurementUnit(Scanner scanner) {
        System.out.println("\nSelect Measurement Unit:");
        MeasurementUnit[] units = MeasurementUnit.values();
        for (int i = 0; i < units.length; i++) {
            System.out.printf("%d - %s%n", i + 1, units[i].getDisplay());
        }

        System.out.print("Enter your choice: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice < 1 || choice > units.length) return MeasurementUnit.UNKNOWN;
            return units[choice - 1];
        } catch (NumberFormatException e) {
            return MeasurementUnit.UNKNOWN;
        }
    }
}
