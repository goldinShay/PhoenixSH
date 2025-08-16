package autoOp;

import devices.Device;
import sensors.Sensor;

public class AutoOpEngine {

    // 🔧 Core ops
    public void initialize() {
        // Load mappings, refresh memory links, etc.
    }

    public void link(Device linkedDevice, Sensor master) {
        // Delegate to AutoOpMemoryRestorer + ExcelSync
    }

    public void unlink(Device linkedDevice) {
        // Call to AutoOpExcelSync.removeLink(...)
    }

    public void reevaluate() {
        // Trigger AutoOpEvaluator
    }

    // 🧠 Diagnostics
    public void logSnapshot() {
        // Use AutoOpDiagnostics to print current map
    }
}
