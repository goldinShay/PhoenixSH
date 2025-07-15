package storage.xlc.sheetsCommand;

public enum DeviceSheetCommand {
    TYPE,
    ID,
    NAME,
    BRAND,
    MODEL,
    AUTO_ENABLED,
    AUTO_ON,
    AUTO_OFF,
    ACTIONS,
    ADDED_TS,
    UPDATED_TS,
    REMOVED_TS,
    COLOR_MODE,
    RGB_R,
    RGB_G,
    RGB_B,
    ACTIVE_MODE;

    public String label() {
        return this.name(); // Can be customized if you prefer lowercase or spaced
    }
}
