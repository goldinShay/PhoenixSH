package devices.actions;

public enum DryerAction {
    ECO_DRY("EcoDry", 45),
    RAPID_DRY("RapidDry", 25),
    ANTI_CREASE("AntiCrease", 15);

    private final String label;
    private final int durationMinutes;

    DryerAction(String label, int durationMinutes) {
        this.label = label;
        this.durationMinutes = durationMinutes;
    }

    public String getLabel() {
        return label;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    @Override
    public String toString() {
        return label + " (" + durationMinutes + " mins)";
    }

    public static DryerAction fromLabel(String input) {
        for (DryerAction action : values()) {
            if (action.label.equalsIgnoreCase(input)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown DryerAction: " + input);
    }
}
