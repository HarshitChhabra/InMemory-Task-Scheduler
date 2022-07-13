package com.someorg.service;

public class TaskExecutor implements Runnable {

    private TaskManager taskManager;

    public TaskExecutor(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void run() {
        while (true) {
            try {
                taskManager.executeAvailableTasks();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }
}
