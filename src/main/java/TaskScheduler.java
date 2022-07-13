import com.someorg.model.Task;
import com.someorg.model.TaskType;
import com.someorg.service.TaskExecutor;
import com.someorg.service.TaskManager;
import com.someorg.service.TaskPoller;

import java.util.concurrent.*;

;

public class TaskScheduler {
    private static final int THREAD_POOL_SIZE = 10;
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager(THREAD_POOL_SIZE);
        TaskExecutor taskExecutor = new TaskExecutor(taskManager);
        TaskPoller taskPoller = new TaskPoller(taskManager);

        System.out.println("Current time: " + System.currentTimeMillis());
        new Thread(taskExecutor).start();
        new Thread(taskPoller).start();

        /* immediate execution */
        Task t1 = Task.buildTask(getDummyRunnable(1), 3000, 0, TaskType.IMMEDIATE, TimeUnit.MILLISECONDS);

        /* Periodic */
        Task t2 = Task.buildTask(getDummyRunnable(2), 0, 10, TaskType.PERIODIC, TimeUnit.MILLISECONDS);
        Task t3 = Task.buildTask(getDummyRunnable(3), 10000, 5000, TaskType.PERIODIC, TimeUnit.MILLISECONDS);

        /* Periodic post execution */
        Task t4 = Task.buildTask(getDummyRunnable(4), 10000, 3000, TaskType.PERIODIC_POST_COMPLETION, TimeUnit.MILLISECONDS);

        taskManager.addNewTask(t1);
        taskManager.addNewTask(t2);
        taskManager.addNewTask(t3);
        taskManager.addNewTask(t4);

    }

    private static Runnable getDummyRunnable(int id) {
        return new Runnable() {
            @Override
            public void run()   {
                System.out.println("Executing task " + id + " time: " + System.currentTimeMillis());
                try {
                    Thread.sleep(3000);
                }catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
        };
    }
}

