package com.binderator.engine;


import java.io.*;


public class ProjectMetaData implements Serializable {


  public static class BasePathHolder implements Serializable {

    public File basePath = null;

  }


  BasePathHolder projectOutputBasePathHolder = new BasePathHolder();
  BasePathHolder signaturesBasePathHolder = new BasePathHolder();
  BasePathHolder documentsBasePathHolder = new BasePathHolder();
  BasePathHolder contentGeneratorsImageBasePathHolder = new BasePathHolder();

  public BasePathHolder getProjectOutputBasePathHolder
  ()
  {
    return projectOutputBasePathHolder;
  }

  public BasePathHolder getSignaturesBasePathHolder
  ()
  {
    return signaturesBasePathHolder;
  }

  public BasePathHolder getDocumentsBasePathHolder
  ()
  {
    return documentsBasePathHolder;
  }

  public BasePathHolder getContentGeneratorsImageBasePathHolder
  ()
  {
    return contentGeneratorsImageBasePathHolder;
  }

}
