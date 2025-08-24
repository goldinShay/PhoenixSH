package devices.actions.advancedActions;

import devices.actions.DryerAction;

import java.util.List;

public class DryerActionsBoschSeries6 {

    public static boolean isCompatible(String brand, String model) {
        return "Bosch".equalsIgnoreCase(brand) && "Series 6".equalsIgnoreCase(model);
    }

    public static List<DryerAction> getAvailablePrograms() {
        return List.of(
                DryerAction.ECO_DRY,
                DryerAction.RAPID_DRY,
                DryerAction.ANTI_CREASE
        );
    }

    public static void printAvailablePrograms() {
        System.out.println("🌀 Bosch Series 6 Advanced Programs:");
        for (DryerAction action : getAvailablePrograms()) {
            System.out.println(" - " + action);
        }
    }

    public static boolean supportsAdvancedPrograms() {
        return true;
    }
}