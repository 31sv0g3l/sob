package com.binderator.engine;


import com.lowagie.text.pdf.*;
import java.io.*;
import java.util.*;


@SuppressWarnings("unused")
public class PageRef implements Comparable<PageRef>, Serializable {

  @Serial
  private static final long serialVersionUID = -5625130648518860618L;
  private static int nextHashCode = 0;
  private final int hashCode = nextHashCode++;
  private final SourceDocument sourceDocument;
  private int pageNumber; // 1-based

  /**
   * Blank page ref constructor
   */
  public PageRef
  (SourceDocument sourceDocument)
  {
    this(sourceDocument, 0);
  }

  public PageRef
  (SourceDocument sourceDocument, int pageNumber)
  {
    this.sourceDocument = sourceDocument;
    this.pageNumber = pageNumber;
  }

  public PageRange getSinglePageRange
  ()
  throws Exception
  {
    return new PageRange(sourceDocument.getId(), pageNumber);
  }

  public SourceDocument getSourceDocument
  ()
  {
    return sourceDocument;
  }

  public boolean isBlankPage
  ()
  {
    return pageNumber == 0;
  }

  public PdfReader getPdfReader
  ()
  throws IOException
  {
    return sourceDocument != null ? sourceDocument.getReader() : null;
  }

  public int getPageNumber
  ()
  {
    return pageNumber;
  }

  public String getPageNumberText
  ()
  {
    String docId = sourceDocument != null ? sourceDocument.getId() : null;
    return docId != null ? docId + ':' + pageNumber : "" + pageNumber;
  }

  public void setPageNumber
  (int pageNumber)
  {
    this.pageNumber = pageNumber;
  }

  public int hashCode
  ()
  {
    return this.hashCode;
  }

  @Override
  public int compareTo
  (PageRef other)
  {
    if (sourceDocument == null) {
      if (other.sourceDocument != null) {
        return -1;
      }
    } else if (other.sourceDocument == null) {
      return 1;
    } else {
      int comparison = Objects.compare(sourceDocument, other.sourceDocument, SourceDocument::compareTo);
      if (comparison != 0) {
        return comparison;
      }
    }
    return Integer.compare(pageNumber, other.pageNumber);
  }

}
