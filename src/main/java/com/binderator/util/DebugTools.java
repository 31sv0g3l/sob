package com.binderator.util;

public class DebugTools {

  public static void printStackTrace(String message)
  {
    if (message != null) {
      System.err.println(message);
    }
    Exception e = new Exception(message);
    e.printStackTrace(System.err);
  }

}
