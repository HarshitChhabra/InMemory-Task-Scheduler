package com.someorg.service;

import com.someorg.model.Task;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskManager {

    private final int threadPoolSize;
    private ThreadPoolExecutor threadPoolExecutor;
    private PriorityQueue<Task> taskQueue;
    private Queue<Task> scheduledTasks;
    private Lock anyTaskAvailableLock = new ReentrantLock();
    private Condition anyTaskAvailable = anyTaskAvailableLock.newCondition();

    private Lock anyScheduledTaskAvailableLock = new ReentrantLock();
    private Condition anyScheduledTaskAvailable = anyScheduledTaskAvailableLock.newCondition();

    public TaskManager(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
        this.taskQueue = new PriorityQueue<Task>(threadPoolSize, Comparator.comparingLong(Task::getScheduleTime));
        this.scheduledTasks = new LinkedList<>();
    }

    public void addNewTask(Task task) {
        anyTaskAvailableLock.lock();
        try {
            taskQueue.add(task);
            anyTaskAvailable.signalAll();
        } finally {
            anyTaskAvailableLock.unlock();
        }
    }

    public void checkForTasksToExecute() throws InterruptedException {
        anyTaskAvailableLock.lock();
        try {
            if (taskQueue.isEmpty())
                anyTaskAvailable.await();

            long currentTime = System.currentTimeMillis();
            if (currentTime < taskQueue.peek().getScheduleTime())
                return;

            anyScheduledTaskAvailableLock.lock();
            try {
                while (!taskQueue.isEmpty() && currentTime >= taskQueue.peek().getScheduleTime()) {
                    scheduledTasks.add(taskQueue.poll());
                }
                anyScheduledTaskAvailable.signalAll();
            } finally {
                anyScheduledTaskAvailableLock.unlock();
            }
        } finally {
            anyTaskAvailableLock.unlock();
        }
    }

    public void executeAvailableTasks() throws InterruptedException {
        anyScheduledTaskAvailableLock.lock();
        try {
            if (scheduledTasks.isEmpty())
                anyScheduledTaskAvailable.await();
            Task task = scheduledTasks.poll();
            switch (task.getTaskType()) {
                case IMMEDIATE:
                    executeOnce(task);
                    break;
                case PERIODIC:
                    executePeriodic(task);
                    break;
                case PERIODIC_POST_COMPLETION:
                    executePeriodicPostCompletion(task);
                    break;
            }

        } finally {
            anyScheduledTaskAvailableLock.unlock();
        }
    }

    private void executeOnce(Task task) {
        threadPoolExecutor.submit(task.getTaskRunnable());
    }

    private void executePeriodic(Task task) {
        long newSchedule = System.currentTimeMillis() + task.getPeriod();
        threadPoolExecutor.submit(task.getTaskRunnable());
        task.setScheduleTime(newSchedule);
        addNewTask(task);
    }

    private void executePeriodicPostCompletion(Task task) {
        CompletableFuture.runAsync(task.getTaskRunnable(), threadPoolExecutor)
                .thenRun(() -> {
                   task.setScheduleTime(System.currentTimeMillis() + task.getPeriod());
                   addNewTask(task);
                });
    }
}

