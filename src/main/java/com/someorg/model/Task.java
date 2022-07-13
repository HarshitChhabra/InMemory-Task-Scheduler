package com.someorg.model;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class Task {
    private Runnable taskRunnable;
    private long initialDelay;
    private long period;
    private long scheduleTime;
    private TaskType taskType;
    private TimeUnit timeUnit;

    private Task() {
    }

    private Task(Runnable task, long initialDelay, long period, long scheduleTime, TaskType taskType, TimeUnit timeUnit) {
        this.taskRunnable = task;
        this.initialDelay = initialDelay;
        this.period = period;
        this.scheduleTime = scheduleTime;
        this.taskType = taskType;
        this.timeUnit = timeUnit;
    }

    public static Task buildTask(Runnable task, long initialDelay, long period, TaskType taskType, TimeUnit timeUnit) {
        return new Task(task, initialDelay, period, System.currentTimeMillis() + initialDelay, taskType, timeUnit);
    }
}
