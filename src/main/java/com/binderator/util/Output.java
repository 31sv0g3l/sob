package com.binderator.util;


import java.io.*;


@SuppressWarnings("unused")
public class Output {

  private static PrintStream errorStream = System.err;
  private static PrintStream outputStream = System.out;

  public static void setErrorStream(PrintStream errorStream)
  {
    Output.errorStream = errorStream;
  }

  public static void setOutputStream(PrintStream outputStream)
  {
    Output.outputStream = outputStream;
  }

  public static void errorOut(String message)
  {
    errorStream.println(message);
  }

  public static PrintStream getErrorOut()
  {
    return errorStream;
  }

  public static void out(String message)
  {
    outputStream.println(message);
  }

  public static PrintStream getOut()
  {
    return outputStream;
  }

}
