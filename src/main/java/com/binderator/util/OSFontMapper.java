package com.binderator.util;


import com.lowagie.text.pdf.*;


public class OSFontMapper extends DefaultFontMapper {

  public OSFontMapper
  ()
  {
    super();
    switch (OSDetection.detectOS()) {
      case MAC -> { insertMacDirectories(); }
      case LINUX -> { insertLinuxDirectories(); }
      case WINDOWS -> { insertWindowsDirectories(); }
      case OTHER -> {
        // "Guess":
        insertMacDirectories();
        insertLinuxDirectories();
        insertWindowsDirectories();
      }
    }
  }

  private void insertMacDirectories
  ()
  {
    try {
      insertDirectory("/Library/Fonts");
      insertDirectory("/System/Library/Fonts");
      insertDirectory(System.getProperty("user.home") + "/Library/Fonts");
    } catch (Throwable ignored) {}
  }

  private void insertLinuxDirectories
  ()
  {
    try {
    } catch (Throwable ignored) {}
  }

  private void insertWindowsDirectories
  ()
  {
    try {
    } catch (Throwable ignored) {}
  }

}
