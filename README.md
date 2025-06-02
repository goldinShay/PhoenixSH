🚀 UNIT Smart Home System — Project Summary
UNIT is a Java-based Smart Home simulation system that allows users to interact with a variety of devices such as lights, sensors, and security elements in a modular and testable environment. The project is designed with clean architecture, extensibility, and testability in mind, making it an ideal foundation for both academic and personal IoT simulation projects.

✅ Features Implemented So Far
Device System Architecture

Support for core device types: Light, Camera, Sensor, etc.

Each device has a unique ID, location, and descriptive name.

Devices can be turned on/off, tested, and extended with specific behaviors.

Modular Menus and Interactions

Main Menu system to navigate between core features.

Device Test Menu for selecting and testing individual devices.

Devices added manually and through the DeviceStorage mechanism for extensibility.

Device Testing Mechanism

Supports interactive testing of devices.

Filters devices based on custom criteria (e.g., only isOn == true).

Visual confirmation using console output for active test devices.

Notification Support

A NotificationService (basic version) enables user-facing messages for actions like testing and status updates.

Smart Logging

Console logs confirm actions like turning on a device or triggering a test, making debugging easier.

Manual Control

Manual overrides implemented (e.g., turning on Light|L001|Front Door) to help with development and testing.

🛠️ Technologies Used
Java 17 (can be upgraded to Java 21)

Object-Oriented Design

IntelliJ IDEA

Maven (for dependency management)

JavaFX (optional/for future GUI)
