package com.projecki.gyro.processor;

import java.util.Timer;
import java.util.TimerTask;

public abstract class Processor {

    private final Timer timer;
    private final TimerTask task;
    private final int delay;
    private final int interval;

    public Processor(String name, int delay, int interval) {
        this.timer = new Timer(name);
        this.task = new TimerTask() {
            @Override
            public void run() {
                runnable();
            }
        };
        this.delay = delay;
        this.interval = interval;
    }

    public abstract void runnable();

    public final void start() {
        this.timer.schedule(task, delay, interval);
    }

    public final void stop() {
        this.task.cancel();
    }
}
