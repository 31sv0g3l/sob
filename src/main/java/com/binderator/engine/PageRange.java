package com.binderator.engine;


import com.binderator.util.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;


@SuppressWarnings("unused")
public class PageRange implements Comparable<PageRange>, Serializable {

  @Serial
  private static final long serialVersionUID = 6651080896479199101L;
  private final String docId;
  private final Integer startPageNumber;
  private final Integer endPageNumber;
  private Integer repetitions;
  private final Boolean even;

  public String getDocId
  ()
  {
    return docId;
  }

  public Integer getStartPageNumber
  ()
  {
    return startPageNumber;
  }

  public Integer getEndPageNumber
  ()
  {
    return endPageNumber;
  }

  public Boolean getEven
  ()
  {
    return even;
  }

  public boolean isBlank
  ()
  {
    return docId == null;
  }

  public int getRepetitions
  ()
  {
    return repetitions != null ? repetitions : 0;
  }

  public PageRange
  (String docId, Integer startPageNumber, Integer endPageNumber, Integer repetitions, Boolean even)
  throws Exception
  {
    if ((startPageNumber != null) && (endPageNumber != null) && (startPageNumber > endPageNumber)) {
      throw new Exception(
        "Start page number in range " + startPageNumber + " is greater than end " + endPageNumber
      );
    }
    this.docId = docId;
    this.startPageNumber = startPageNumber;
    this.endPageNumber = endPageNumber;
    this.repetitions = repetitions;
    this.even = even;
  }

  static Pattern docIdRangePattern = Pattern.compile(
    "\\s*(([_a-zA-Z][_a-zA-Z0-9]*):)?([0-9]+)(\\s*-\\s*([0-9]+))?(\\s*([eE][vV][eE][nN]|[oO][dD][dD]))?\\s*"
  );

  static Pattern onlyDocIdRangePattern = Pattern.compile(
    "\\s*(([_a-zA-Z][_a-zA-Z0-9]*):)([0-9]+)(\\s*-\\s*([0-9]+))?(\\s*([eE][vV][eE][nN]|[oO][dD][dD]))?\\s*"
  );

  // For use in source document specifications; can contain repetitions:
  static Pattern rangePattern = Pattern.compile(
    "\\s*([0-9]+)(\\s*-\\s*([0-9]+))?(\\s*\\*\\s*([1-9][0-9]*))?(\\s*([eE][vV][eE][nN]|[oO][dD][dD]))?\\s*"
  );

  public static List<PageRange> parsePageRanges
  (String source, boolean useDocIds)
  throws Exception
  {
    return parsePageRanges(source, useDocIds, false);
  }

  public static List<PageRange> parsePageRanges
  (String source, boolean allowDocIds, boolean requireDocIds)
  throws Exception
  {
    List<PageRange> pageRanges = new ArrayList<>();
    if (source.isEmpty()) {
      return pageRanges;
    }
    String[] rangeSources = source.split("\\s*,\\s*", -1);
    Integer repetitions = null;
    // A cumulative blank range:
    PageRange blankRange = null;
    for (String rangeSource : rangeSources) {
      rangeSource = rangeSource.trim();
      if (rangeSource.isEmpty()) {
        // A blank page:
        if (blankRange != null) {
          blankRange.repetitions = blankRange.getRepetitions() + 1;
        } else {
          blankRange = new PageRange(null, 0, 0, 1, true);
        }
        continue;
      }
      Matcher rangeMatcher =
        allowDocIds
          ? (requireDocIds ? onlyDocIdRangePattern.matcher(rangeSource) : docIdRangePattern.matcher(rangeSource))
          : rangePattern.matcher(rangeSource);
      if (rangeMatcher.matches()) {
        String docId = null;
        if (allowDocIds) {
          docId = rangeMatcher.group(2);
          docId = docId != null && !docId.isEmpty() ? docId : null;
        }
        String fromSource = rangeMatcher.group(allowDocIds ? 3 : 1);
        Integer from = Integer.parseInt(fromSource);
        Integer to = from;
        if ((rangeMatcher.group(allowDocIds ? 4 : 2) != null) && !rangeMatcher.group(allowDocIds ? 4 : 2).isEmpty()) {
          String toSource = rangeMatcher.group(allowDocIds ? 5 : 3);
          to = (toSource != null) && !toSource.isEmpty() ? Integer.parseInt(toSource) : null;
        }
        if (!allowDocIds) {
          String repetitionsSource = rangeMatcher.group(5);
          if ((repetitionsSource != null) && !repetitionsSource.isEmpty()) {
            repetitions = Integer.parseInt(repetitionsSource);
          }
        }
        String evenSource = rangeMatcher.group(7);
        Boolean even = null;
        if ((evenSource != null) && !evenSource.isEmpty()) {
          if (Objects.equals(from, to)) {
            throw new Exception("Invalid even/odd specifier for single page in range: " + rangeSource);
          }
          if (evenSource.equalsIgnoreCase("even")) {
            even = true;
          } else if (evenSource.equalsIgnoreCase("odd")) {
            even = false;
          } else {
            throw new Exception ("Invalid even/odd specifier \"" + evenSource + "\" in page range: " + rangeSource);
          }
        }
        if (blankRange != null) {
          pageRanges.add(blankRange);
          blankRange = null;
        }
        pageRanges.add(new PageRange(docId, from, to, repetitions, even));
      } else {
        throw new ParseException("String \"" + rangeSource + "\" is not a valid page range", 0);
      }
    }
    if (blankRange != null) {
      pageRanges.add(blankRange);
    }
    return pageRanges;
  }

  public static List<PageRef> toPageRefs
  (Collection<PageRange> pageRanges, Map<String, SourceDocument> sourceDocumentsById)
  throws Exception
  {
    List<PageRef> pageRefs = new ArrayList<>();
    for (PageRange pageRange : pageRanges) {
      if (pageRange.isBlank()) {
        for (int i = 0; i < pageRange.getRepetitions(); i++) {
          pageRefs.add(new PageRef(null, 0));
        }
        continue;
      }
      pageRefs.addAll(pageRange.getPageRefs(null, sourceDocumentsById));
    }
    return pageRefs;
  }

  public static String toString
  (Collection<PageRange> pageRanges)
  {
    if (pageRanges != null) {
      return StringUtils.join(pageRanges, ", ");
    } else {
      return "NIL";
    }
  }

  public String toString
  ()
  {
    String range = (docId != null) ? docId + ":" : "";
    range += (endPageNumber == null) || (startPageNumber.equals(endPageNumber))
      ? "" + startPageNumber : startPageNumber + "-" + endPageNumber;
    if (repetitions != null) {
      range = range + '*' + repetitions;
    }
    if (even != null) {
      return range + (even ? " even" : " odd");
    } else {
      return range;
    }
  }

  public Collection<PageRef> getPageRefs
  (List<PageRef> pages)
  throws Exception
  {
    return getPageRefs(pages, null);
  }

  /**
   * Get a list of page references
   * @param orderedPages the total list of source pages (assumed to be an ordered sequence starting at page 1)
   * @param sourceDocumentsById a nullable map of source document ids to source documents.  If this PageRange has a
   *                            non-null docId, allPages will be ignored and the pages from the associated source
   *                            document will be used.
   * @return a collection of page refs derived from this page range and the given source page refs
   */
  public Collection<PageRef> getPageRefs
  (List<PageRef> orderedPages, Map<String, SourceDocument> sourceDocumentsById)
  throws Exception
  {
    if (docId != null) {
      if (sourceDocumentsById == null) {
        throw new Exception("INTERNAL ERROR: null source document map supplied with non-null docId");
      }
      SourceDocument sourceDocument = sourceDocumentsById.get(docId);
      if (sourceDocument == null) {
        throw new Exception("ERROR: source document \"" + docId + "\" not found");
      }
      orderedPages = sourceDocument.getSourcePages();
    }
    List<PageRef> result = new LinkedList<>();
    if (orderedPages.size() == 0) {
      return result;
    }
    int startPageIndex = (startPageNumber != null) ? startPageNumber - 1 : 0;
    if ((startPageIndex < 0) || (startPageIndex >= orderedPages.size())) {
      throw new Exception(
        "Invalid start page " + (startPageIndex + 1) + " in page range over " + orderedPages.size() +
        " source pages from document " + docId
      );
    }
    int endPageIndex = (endPageNumber != null) ? endPageNumber - 1 : orderedPages.size() - 1;
    if ((endPageIndex < 0) || (endPageIndex >= orderedPages.size())) {
      throw new Exception(
        "Invalid end page " + (endPageIndex + 1) + " in page range over " + orderedPages.size() +
        " source pages from document " + docId
      );
    }
    int repetitions = this.repetitions != null ? this.repetitions : 1;
    for (int repetition = 0; repetition < repetitions; repetition++ ) {
      for (int i = startPageIndex; i <= endPageIndex; i++) {
        PageRef pageRef = orderedPages.get(i);
        if (even != null) {
          if ((even && (pageRef.getPageNumber() % 2 == 0)) || ((!even) && (pageRef.getPageNumber() % 2 != 0))) {
            result.add(pageRef);
          }
        } else {
          result.add(pageRef);
        }
      }
    }
    return result;
  }

  @Override
  public int compareTo
  (PageRange other)
  {
    int comparison;
    if ((comparison = Objects.compare(docId, other.docId, CharSequence::compare)) != 0) {
      return comparison;
    }
    if ((comparison = Objects.compare(startPageNumber, other.startPageNumber, Integer::compare)) != 0) {
      return comparison;
    }
    if ((comparison = Objects.compare(endPageNumber, other.endPageNumber, Integer::compare)) != 0) {
      return comparison;
    }
    return Objects.compare(even, other.even, Boolean::compare);
  }

}
