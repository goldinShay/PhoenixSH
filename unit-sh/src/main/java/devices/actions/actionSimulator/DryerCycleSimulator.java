package devices.actions.actionSimulator;

import devices.actions.DryerAction;

public class DryerCycleSimulator {

    public static void simulateCycle(DryerAction action) {
        int durationInSeconds = action.getDurationMinutes(); // 1 min = 1 sec for simulation

        System.out.println("⏳ Starting " + action.getLabel() + " cycle (" + durationInSeconds + " sec)");

        Thread thread = new Thread(() -> {
            for (int i = durationInSeconds; i >= 0; i--) {
                System.out.printf("🕒 Remaining: %02d sec\r", i);
                try {
                    Thread.sleep(1000); // simulate 1 second per minute
                } catch (InterruptedException e) {
                    System.out.println("\n❌ Cycle interrupted.");
                    return;
                }
            }
            System.out.println("\n✅ Cycle complete: " + action.getLabel());
        });

        thread.start();
    }
}