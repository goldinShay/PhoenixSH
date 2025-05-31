import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private Timer timer;

    public Scheduler() {
        timer = new Timer(true); 
    }

    public void scheduleTask(long delay, Runnable task) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, delay);
    }
}
