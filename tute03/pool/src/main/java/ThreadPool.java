import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {

  private final int nThreads;
  private final BlockingQueue<Runnable> taskQueue;
  private final List<ThreadWorker> threads;

  public ThreadPool(int nThreads) {
    this.nThreads = nThreads;
    this.taskQueue = new LinkedBlockingQueue<>();
    this.threads = new ArrayList<>();

    for (int i = 0; i < nThreads; i++) {
      threads.add(new ThreadWorker());
      threads.get(i).start();
    }
  }

  public void execute(Runnable task) {
    // add the job to the queue
    synchronized (taskQueue) {
      taskQueue.add(task);
      taskQueue.notify();
    }

  }

  private class ThreadWorker extends Thread {

    @Override
    public void run() {
      Runnable curr_task;
      while (true) {
        // get a job from the queue
        synchronized (taskQueue) {
          while (taskQueue.isEmpty()) {
            // if the queue is empty, wait until a job arrives???
            try {
              taskQueue.wait();
            } catch (InterruptedException e) {
              System.out.println("An error occurred while queue is waiting: " + e.getMessage());
            }
          }
          curr_task = taskQueue.poll();
        }

        try {
          curr_task.run();
        } catch (RuntimeException e) {
          System.out.println("Thread pool is interrupted due to an issue: " + e.getMessage());
        }
      }
    }
  }
}
