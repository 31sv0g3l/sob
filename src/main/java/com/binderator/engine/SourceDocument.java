package com.binderator.engine;


import com.binderator.util.*;
import com.lowagie.text.pdf.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;


@SuppressWarnings("unused")
public class SourceDocument implements Serializable, Comparable<SourceDocument> {

  @Serial
  private static final long serialVersionUID = -1029037522867925664L;

  public record BlankPages(int afterPageNumber, int pageCount) implements Serializable {

    @Override
    public String toString
    ()
    {
      return afterPageNumber + ":" + pageCount;
    }

  }

  private String stringId;
  private String name;
  private String comment;
  private String path;
  private transient PdfReader reader = null;
  private List<PageRange> pageRanges = new ArrayList<>();
  private List<BlankPages> blankPages = new ArrayList<>();

  public SourceDocument
  (String path)
  {
    this(null, null, path);
  }

  public SourceDocument
  (SourceDocument sourceDocument)
  {
    stringId = sourceDocument.stringId;
    name = sourceDocument.name;
    comment = sourceDocument.comment;
    path = sourceDocument.path;
  }

  public SourceDocument
  (String identifier, String name, String path)
  {
    this(identifier, name, path, null);
  }

  public SourceDocument
  (String stringId, String name, String path, String comment)
  {
    this.stringId = stringId;
    this.name = name;
    this.comment = comment;
    this.path = path;
  }

  public String getStringId
  ()
  {
    return stringId;
  }

  public void setStringId
  (String stringId)
  {
    this.stringId = stringId;
  }

  public String getName
  ()
  {
    return name;
  }

  public void setName
  (String name)
  {
    this.name = name;
  }

  public String getComment
  ()
  {
    return comment;
  }

  public void setComment
  (String comment)
  {
    this.comment = comment;
  }

  public String getPath
  ()
  {
    return path;
  }

  public void setPath
  (String path)
  {
    this.path = path;
    close();
  }

  public PdfReader getReader
  ()
  throws IOException
  {
    if (reader == null) {
      reader = new PdfReader(new FileInputStream(path), null);
    }
    return reader;
  }

  @Override
  public int compareTo
  (SourceDocument other)
  {
    int comparison;
    if ((comparison = Objects.compare(this.path, other.path, String::compareTo)) != 0) {
      return comparison;
    }
    if ((comparison = Objects.compare(this.name, other.name, String::compareTo)) != 0) {
      return comparison;
    }
    if ((comparison = Objects.compare(this.stringId, other.stringId, String::compareTo)) != 0) {
      return comparison;
    }
    return 0;
  }

  public void close
  ()
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

  public List<PageRange> getPageRanges
  ()
  {
    return pageRanges;
  }

  public void setPageRanges
  (List<PageRange> pageRanges)
  {
    this.pageRanges = pageRanges;
    pages = null;
  }

  public void addPageRange
  (PageRange pageRange)
  {
    if (pageRanges == null) {
      pageRanges = new ArrayList<>();
    }
    pageRanges.add(pageRange);
    pages = null;
  }

  public Collection<BlankPages> getBlankPages
  ()
  {
    return blankPages;
  }

  public void setBlankPages
  (List<BlankPages> blankPages)
  {
    this.blankPages = blankPages;
    pages = null;
  }

  public void addBlankPages
  (BlankPages blankPages)
  {
    this.blankPages.add(blankPages);
    pages = null;
  }

  static Pattern blankPagesPattern = Pattern.compile(
    "\\s*([0-9][0-9]*)\\s*:\\s*([1-9][0-9]*)\\s*"
  );

  public static List<BlankPages> parseBlankPages
  (String source)
  throws Exception
  {
    List<BlankPages> result = new ArrayList<>();
    String[] blankPagesSources = source.split("\\s*,\\s*");
    for (String blankPagesSource : blankPagesSources) {
      if (blankPagesSource.isEmpty()) {
        continue;
      }
      System.err.println("Blank pages source: \"" + blankPagesSource + "\"");
      Matcher blankPagesMatcher = blankPagesPattern.matcher(blankPagesSource);
      if (blankPagesMatcher.matches()) {
        String afterPageNumberSource = blankPagesMatcher.group(1);
        int afterPageNumber = Integer.parseInt(afterPageNumberSource);
        String countSource = blankPagesMatcher.group(2);
        int count = Integer.parseInt(countSource);
        result.add(new BlankPages(afterPageNumber, count));
      } else {
        throw new ParseException("String \"" + source + "\" is not a valid (page:count) blank pages specifier", 0);
      }
    }
    return result;
  }

  public String getPageRangesString
  ()
  {
    return PageRange.toString(pageRanges);
  }

  public int getPageCount
  ()
  throws Exception
  {
    return getReader().getNumberOfPages();
  }

  List<PageRef> sourcePages = null;
  List<PageRef> pages = null;

  public List<PageRef> getSourcePages
  ()
  throws Exception
  {
    if (sourcePages == null) {
      sourcePages = new ArrayList<>();
      for (int i = 1; i <= getPageCount(); i++) {
        sourcePages.add(new PageRef(this, i));
      }
    }
    return sourcePages;
  }

  public List<PageRef> getPages
  ()
  throws Exception
  {
    if (pages == null) {
      List<PageRef> sourcePages = getSourcePages();
      pages = new ArrayList<>();
      if ((pageRanges != null) && !pageRanges.isEmpty()) {
        List<PageRef> pageRangePages = new LinkedList<>();
        for (PageRange pageRange : pageRanges) {
          pageRangePages.addAll(pageRange.getPageRefs(sourcePages));
        }
        pages.addAll(pageRangePages);
      } else {
        pages.addAll(sourcePages);
      }
      for (BlankPages blankPageInsertion : this.blankPages) {
        boolean foundPageBeforeBlanks = false;
        if (blankPageInsertion.afterPageNumber() == 0) {
          for (int blankPageCount = 0; blankPageCount < blankPageInsertion.pageCount(); blankPageCount++) {
            pages.add(0, new PageRef(this));
          }
          foundPageBeforeBlanks = true;
        } else {
          for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getPageNumber() == blankPageInsertion.afterPageNumber()) {
              foundPageBeforeBlanks = true;
              for (int blankPageCount = 0; blankPageCount < blankPageInsertion.pageCount(); blankPageCount++) {
                pages.add(i + 1, new PageRef(this));
              }
              break;
            }
          }
        }
        if (!foundPageBeforeBlanks) {
          throw new Exception(
            "Blank page(s) specified after page " + blankPageInsertion.afterPageNumber() +
            ", which is not in the specified source page range set of document at path \"" + path + "\""
          );
        }
      }
    }
    return pages;
  }

  public String getBlankPagesString
  ()
  {
    return StringUtils.join(blankPages, ", ");
  }

  @Override
  public String toString
  ()
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
