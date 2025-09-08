package com.binderator.util;

import java.util.*;
import java.io.*;

public class InitFile {

  private TreeMap<String, Object> map = new TreeMap<>();
  File file = null;
  boolean autoSave = false;

  private static InitFile instancePtr = null;

  public static void initialise(String path, boolean autoSave)
  throws Exception
  {
    if (instancePtr != null) {
      throw new Exception("Attempt to initialise InitFile singleton more than once");
    }
    instancePtr = new InitFile(path, autoSave);
  }

  @SuppressWarnings("unchecked")
  private InitFile(String path, boolean autoSave)
  throws Exception
  {
    file = new File(path);
    if (!file.canRead()){
      if (!file.createNewFile()) {
        throw new IOException("Could not create new init file at path \"" + path + "\"");
      }
    }
    if (!file.canRead()){
      throw new IOException("Could not read init file at path \"" + path + "\"");
    }
    if (!file.canWrite()){
      throw new IOException("Could not write to init file at path \"" + path + "\"");
    }
    try {
      FileInputStream inputStream = new FileInputStream(file);
      ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
      map = (TreeMap<String, Object>) objectInputStream.readObject();
    } catch (Exception ignored) {}
    this.autoSave = autoSave;
  }

  public static InitFile instance()
  throws Exception
  {
    if (instancePtr == null ) {
      throw new Exception("Attempt to access Initfile instance without initialisation");
    }
    return instancePtr;
  }

  public void save()
  {
    try {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
      out.writeObject(map);
      out.flush();
      out.close();
    } catch (Exception e) {
      Output.errorOut("Could not write init file: " + e.getMessage());
      e.printStackTrace(Output.getErrorOut());
    }
  }

  public Object get(String key)
  {
    return get(key, null);
  }

  public Object get(String key, Object defaultValue)
  {
    Object value = map.get(key);
    return value != null ? value : defaultValue;
  }

  public void set(String key, Object value)
  {
    map.put(key, value);
    if (autoSave) {
      save();
    }
  }

}
