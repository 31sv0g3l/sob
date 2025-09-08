package com.binderator.engine;

import com.lowagie.text.pdf.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

@SuppressWarnings("unused")
public class SourceDocument implements Serializable, Comparable<SourceDocument> {

  @Serial
  private static final long serialVersionUID = -1029037522867925664L;

  private String id;
  private String name;
  private String comment;
  private String path = null;
  private InputStream inputStream = null;
  private transient PdfReader reader = null;
  List<PageRef> sourcePages = null;
  List<PageRef> pages = null;
  private Book book = null;

  public SourceDocument(String path)
  {
    this(null, null, path);
  }

  public SourceDocument(SourceDocument sourceDocument)
  {
    id = sourceDocument.id;
    name = sourceDocument.name;
    comment = sourceDocument.comment;
    path = sourceDocument.path;
  }

  public SourceDocument(InputStream inputStream)
  {
    this(null, null, inputStream);
  }

  public SourceDocument(String stringId, String name, InputStream inputStream)
  {
    this.id = stringId;
    this.name = name;
    path = null;
    this.inputStream = inputStream;
  }

  public SourceDocument(String identifier, String name, String path)
  {
    this(identifier, name, path, null);
  }

  public SourceDocument(String stringId, String name, String path, String comment)
  {
    this.id = stringId;
    this.name = name;
    this.comment = comment;
    this.path = path;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    String oldId = this.id;
    this.id = id;
    book.changeSourceDocumentId(oldId, id, this);
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
    close();
  }

  void setBook(Book book)
  {
    this.book = book;
  }

  public PdfReader getReader()
  throws IOException
  {
    if (reader == null) {
      if (inputStream != null) {
        reader = new PdfReader(inputStream, null);
      } else if ((path != null) && !path.isBlank()) {
        reader = new PdfReader(new FileInputStream(path), null);
      }
    }
    return reader;
  }

  @Override
  public int compareTo(SourceDocument other)
  {
    int comparison;
   if ((comparison = Objects.compare(this.path, other.path, String::compareTo)) != 0) {
      return comparison;
    }
    if ((comparison = Objects.compare(this.name, other.name, String::compareTo)) != 0) {
      return comparison;
    }
    if ((comparison = Objects.compare(this.id, other.id, String::compareTo)) != 0) {
      return comparison;
    }
    return 0;
  }

  public void close()
  {
    if (reader != null) {
      try {
        reader.close();
        reader = null;
        sourcePages = null;
        pages = null;
      } catch (Exception ignored) {}
    }
  }

  static Pattern blankPagesPattern = Pattern.compile(
    "\\s*([0-9][0-9]*)\\s*:\\s*([1-9][0-9]*)\\s*"
  );

  public int getPageCount()
  {
    try {
      PdfReader reader = getReader();
      if (reader == null) {
        return 0;
      }
      return reader.getNumberOfPages();
    } catch (Throwable t) {
      // Reader likely null - path likely not set...
      return 0;
    }
  }

  public List<PageRef> getSourcePages()
  throws Exception
  {
    if ((sourcePages == null) || (sourcePages.isEmpty())) {
      sourcePages = new ArrayList<>();
      for (int i = 1; i <= getPageCount(); i++) {
        sourcePages.add(new PageRef(this, i));
      }
    }
    return sourcePages;
  }

  public List<PageRef> getPages()
  {
    return sourcePages;
  }

  @Override
  public String toString()
  {
    if (name != null) {
      return name;
    } else if (path != null) {
      File file = new File(path);
      return file.getName();
    } else {
      return "";
    }
  }

}
