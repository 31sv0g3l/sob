package com.binderator.persistence;


import com.binderator.engine.*;
import java.io.*;


public class SerialBinderatorStore implements BinderatorStore {

  @Override
  public void saveBook
  (Book book, String path, boolean emptySchema)
  throws Exception
  {
    File file = new File(path);
    File parentFile = file.getParentFile();
    if (!parentFile.exists()) {
      if (!parentFile.mkdirs()) {
        throw new Exception("Couldn't create parent directory of path \"" + path + "\"");
      }
    }
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
    objectOutputStream.writeObject(book);
    objectOutputStream.close();
  }

  @Override
  public Book loadBook
  (String path)
  throws Exception
  {
    FileInputStream fileInputStream = new FileInputStream(path);
    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
    return (Book)objectInputStream.readObject();
  }

}
