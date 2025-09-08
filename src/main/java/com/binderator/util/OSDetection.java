package com.binderator.util;

import java.util.regex.*;

public class OSDetection {

  public enum Type { MAC, LINUX, WINDOWS, OTHER }

  private static Pattern macPattern = Pattern.compile(".*[mM][aA][cC].*");
  private static Pattern windowsPattern = Pattern.compile(".*[wW][iI][nN][dD][oO][wW][sS].*");
  private static Pattern linuxPattern = Pattern.compile(".*[lL][iI][nN][uU][xX].*");

  public static Type detectOS()
  {
    String os = System.getProperty("os.name");
    if (macPattern.matcher(os).matches()) {
      return Type.MAC;
    } else if (linuxPattern.matcher(os).matches()) {
      return Type.LINUX;
    } else if (windowsPattern.matcher(os).matches()) {
      return Type.WINDOWS;
    }
    return Type.OTHER;
  }

  public static void main(String[] args)
  {
    System.out.print("\nOS detected: " + detectOS() + "\n\n");
  }

}
