package com.someorg.service;

public class TaskPoller implements Runnable {

    private TaskManager taskManager;

    public TaskPoller(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void run() {
        while (true) {
            try {
                taskManager.checkForTasksToExecute();
                Thread.sleep(100L);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }
}
