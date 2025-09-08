package com.binderator.util;

import java.util.concurrent.locks.*;

public abstract class Worker extends Thread {

  private final ReentrantLock lock;
  private final Condition condition;
  private boolean running;
  private boolean haveATask;

  public Worker()
  {
    lock = new ReentrantLock();
    condition = lock.newCondition();
    haveATask = false;
    running = false;
  }

  public void signalTask()
  {
    lock.lock();
    haveATask = true;
    condition.signal();
    lock.unlock();
  }

  public void terminate()
  {
    lock.lock();
    running = false;
    condition.signal();
    lock.unlock();
  }

  @Override
  public void run()
  {
    lock.lock();
    running = true;
    try {
      while (running) {
        if (!haveATask) {
          try {
            condition.await();
          } catch (InterruptedException ignored) {
          }
        }
        if (running && haveATask) {
          haveATask = false;
          lock.unlock();
          task();
          lock.lock();
        }
      }
    } finally {
      try {
        lock.unlock();
      } catch (Exception ignored) {};
    }
  }

  abstract public void task();

}
