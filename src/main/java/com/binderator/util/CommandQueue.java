package com.binderator.util;

import java.util.*;
import java.util.concurrent.locks.*;

public class CommandQueue extends Thread {

  public interface Command {

    void invoke
    ();

  }

  private final LinkedList<Command> commands;
  private final ReentrantLock lock;
  private final Condition condition;
  private static CommandQueue singletonInstance = null;
  private static boolean running;

  public CommandQueue
  ()
  {
    super();
    commands = new LinkedList<>();
    lock = new ReentrantLock();
    condition = lock.newCondition();
    running = true;
  }

  public static CommandQueue getInstance
  ()
  {
    if (singletonInstance == null) {
      singletonInstance = new CommandQueue();
      singletonInstance.start();
    }
    return singletonInstance;
  }

  public void execute
  (Command command)
  {
    lock.lock();
    commands.addFirst(command);
    condition.signal();
    lock.unlock();
  }

  public void terminate
  ()
  {
    lock.lock();
    running = false;
    condition.signal();
    lock.unlock();
  }

  void logException
  (Throwable t)
  {
    System.err.println("Command queue caught exception: " + t.getMessage());
    t.printStackTrace(System.err);
  }

  @Override
  public void run
  ()
  {
    List<Command> newCommands = new ArrayList<>();
    lock.lock();
    while (running) {
      while (running && (commands.size() == 0)) {
        try {
          condition.await();
        } catch (InterruptedException e) {
          logException(e);
        }
      }
      if (running) {
        newCommands.addAll(commands);
        commands.clear();
        lock.unlock();
        for (Command command : newCommands) {
          command.invoke();
        }
        newCommands.clear();
        lock.lock();
      }
    }
  }

}
