package pl.karoldronia.artefacts.scheduler;

import java.time.Duration;

public interface Scheduler {

    void sync(Runnable task);

    void async(Runnable task);

    void laterSync(Runnable task, Duration delay);

    void laterAsync(Runnable task, Duration delay);

    void timerSync(Runnable task, Duration delay, Duration period);

    void timerAsync(Runnable task, Duration delay, Duration period);

    void repeatSync(Runnable task, Duration delayBetween, int times);

    void repeatAsync(Runnable task, Duration delayBetween, int times);


}