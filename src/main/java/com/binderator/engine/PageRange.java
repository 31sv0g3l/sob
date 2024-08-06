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

  /**
   * Constructor for creating a PageRange object.
   * @param docId The ID of the document.
   * @throws Exception If the start page number is greater than the end page number.
   */  public PageRange
  (String docId, Integer pageNumber)
  throws Exception
  {
    this(docId, pageNumber, pageNumber);
  }

  /**
   * Constructor for creating a PageRange object.
   * @param docId The ID of the document.
   * @param startPageNumber The starting page number of the range.
   * @param endPageNumber The ending page number of the range.
   * @throws Exception If the start page number is greater than the end page number.
   */  public PageRange
  (String docId, Integer startPageNumber, Integer endPageNumber)
  throws Exception
  {
    this(docId, startPageNumber, endPageNumber, null);
  }

  /**
   * Constructor for creating a PageRange object.
   * @param docId The ID of the document.
   * @param startPageNumber The starting page number of the range.
   * @param endPageNumber The ending page number of the range.
   * @param repetitions The number of times the range is repeated.
   * @throws Exception If the start page number is greater than the end page number.
   */  public PageRange
  (String docId, Integer startPageNumber, Integer endPageNumber, Integer repetitions)
  throws Exception
  {
    this(docId, startPageNumber, endPageNumber, repetitions, null);
  }

  /**
   * Constructor for creating a PageRange object.
   * @param docId The ID of the document.
   * @param startPageNumber The starting page number of the range.
   * @param endPageNumber The ending page number of the range.
   * @param repetitions The number of times the range is repeated.
   * @param even A Boolean value indicating whether the range is even.
   * @throws Exception If the start page number is greater than the end page number.
   */
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

  // For use in transform page range specifications; must not contain repetitions:
  static Pattern allowDocIdsRangePattern = Pattern.compile(
    "\\s*(([_a-zA-Z][_a-zA-Z0-9]*):)?([0-9]+)(\\s*-\\s*([0-9]+))?(\\s*([eE][vV][eE][nN]|[oO][dD][dD]))?\\s*"
  );

  // For use in project page range specifications; can contain repetitions:
  static Pattern requireDocIdsRangePattern = Pattern.compile(
    "\\s*(([_a-zA-Z][_a-zA-Z0-9]*):)([0-9]+)(\\s*-\\s*([0-9]+))?(\\s*\\*\\s*([1-9][0-9]*))?(\\s*([eE][vV][eE][nN]|[oO][dD][dD]))?\\s*"
  );

  /**
   * This method parses a string representing page ranges into a list of PageRange objects.
   * @param source The string representing the page ranges.
   * @param requireDocIds A boolean indicating whether document IDs are required.
   * @return A list of PageRange objects.
   * @throws Exception If the string is not a valid page range.
   */
  public static List<PageRange> parsePageRanges
  (String source, boolean requireDocIds)
  throws Exception
  {
    List<PageRange> pageRanges = new ArrayList<>();
    if (source.isEmpty()) {
      return pageRanges;
    }
    String[] rangeSources = source.split("\\s*,\\s*", -1);
    PageRange blankRange = null;
    for (String rangeSource : rangeSources) {
      rangeSource = rangeSource.trim();
      if (rangeSource.isEmpty()) {
        blankRange = handleBlankRange(blankRange);
        continue;
      }
      Matcher rangeMatcher = getRangeMatcher(rangeSource, requireDocIds);
      if (rangeMatcher.matches()) {
        blankRange = handleMatchedRange(rangeMatcher, blankRange, pageRanges, requireDocIds, rangeSource);
      } else {
        throw new ParseException("String \"" + rangeSource + "\" is not a valid page range", 0);
      }
    }
    if (blankRange != null) {
      pageRanges.add(blankRange);
    }
    return pageRanges;
  }

  private static PageRange handleBlankRange
  (PageRange blankRange)
  throws Exception
  {
    if (blankRange != null) {
      blankRange.repetitions = blankRange.getRepetitions() + 1;
    } else {
      blankRange = new PageRange(null, 0, 0, 1, true);
    }
    return blankRange;
  }

  private static Matcher getRangeMatcher
  (String rangeSource, boolean requireDocIds)
  {
    return requireDocIds ? requireDocIdsRangePattern.matcher(rangeSource) : allowDocIdsRangePattern.matcher(rangeSource);
  }

  private static PageRange handleMatchedRange
  (Matcher rangeMatcher, PageRange blankRange, List<PageRange> pageRanges, boolean requireDocIds, String rangeSource)
  throws Exception
  {
    String docId = getDocId(rangeMatcher);
    Integer from = Integer.parseInt(rangeMatcher.group(3));
    Integer to = getTo(rangeMatcher, from);
    Integer repetitions = getRepetitions(rangeMatcher, requireDocIds);
    Boolean even = getEven(rangeMatcher, from, to, rangeSource, requireDocIds);
    if (blankRange != null) {
      pageRanges.add(blankRange);
      blankRange = null;
    }
    pageRanges.add(new PageRange(docId, from, to, repetitions, even));
    return blankRange;
  }

  private static String getDocId
  (Matcher rangeMatcher)
  {
    String docId = rangeMatcher.group(2);
    return docId != null && !docId.isEmpty() ? docId : null;
  }

  private static Integer getTo
  (Matcher rangeMatcher, Integer from)
  {
    Integer to = from;
    if ((rangeMatcher.group(4) != null) && !rangeMatcher.group(4).isEmpty()) {
      String toSource = rangeMatcher.group(5);
      to = (toSource != null) && !toSource.isEmpty() ? Integer.parseInt(toSource) : null;
    }
    return to;
  }

  private static Integer getRepetitions
  (Matcher rangeMatcher, boolean requireDocIds)
  {
    Integer repetitions = null;
    if (requireDocIds) {
      String repetitionsSource = rangeMatcher.group(7);
      if ((repetitionsSource != null) && !repetitionsSource.isEmpty()) {
        repetitions = Integer.parseInt(repetitionsSource);
      }
    }
    return repetitions;
  }

  private static Boolean getEven
  (Matcher rangeMatcher, Integer from, Integer to, String rangeSource, boolean requireDocIds)
  throws Exception
  {
    String evenSource = requireDocIds ? rangeMatcher.group(9) : rangeMatcher.group(7);
    Boolean even = null;
    if ((evenSource != null) && !evenSource.isEmpty()) {
      if (Objects.equals(from, to)) {
        throw new Exception("Invalid even/odd specifier for single page in range: " + rangeSource);
      }
      even = getEvenValue(evenSource, rangeSource);
    }
    return even;
  }

  private static Boolean getEvenValue
  (String evenSource, String rangeSource)
  throws Exception
  {
    if (evenSource.equalsIgnoreCase("even")) {
      return true;
    } else if (evenSource.equalsIgnoreCase("odd")) {
      return false;
    } else {
      throw new Exception ("Invalid even/odd specifier \"" + evenSource + "\" in page range: " + rangeSource);
    }
  }

  /**
   * This method converts a collection of PageRange objects into a list of PageRef objects.
   * @param pageRanges The collection of PageRange objects.
   * @param sourceDocumentsById A map of document IDs to SourceDocument objects.
   * @return A list of PageRef objects.
   * @throws Exception If a non-null document ID is supplied with a null source document map.
   */
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

  /**
   * Get a list of page references
   * @param orderedPages the total list of source pages (assumed to be an ordered sequence starting at page 1).
   *                     NOTE: will be ignored if this page range has a non-null document id, in which case the
   *                     source pages will be taken from the specific document.
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
      if (!sourceDocumentsById.containsKey(docId)) {
        throw new Exception("ERROR: source document \"" + docId + "\" not found");
      }
      orderedPages = sourceDocumentsById.get(docId).getSourcePages();
    }
    List<PageRef> result = new LinkedList<>();
    if (orderedPages.isEmpty()) {
      return result;
    }
    int startPageIndex = (startPageNumber != null) ? startPageNumber - 1 : 0;
    int endPageIndex = (endPageNumber != null) ? endPageNumber - 1 : orderedPages.size() - 1;
    if (startPageIndex < 0 || startPageIndex >= orderedPages.size() || endPageIndex < 0 || endPageIndex >= orderedPages.size()) {
      throw new Exception("Invalid page range over " + orderedPages.size() + " source pages from document " + docId);
    }
    int repetitions = this.repetitions != null ? this.repetitions : 1;
    addPageRefsToResult(orderedPages, result, repetitions, startPageIndex, endPageIndex);
    return result;
  }

  private void addPageRefsToResult
  (List<PageRef> orderedPages, List<PageRef> result, int repetitions, int startPageIndex, int endPageIndex)
  {
    for (int repetition = 0; repetition < repetitions; repetition++) {
      for (int i = startPageIndex; i <= endPageIndex; i++) {
        PageRef pageRef = orderedPages.get(i);
        if (even != null) {
          if ((docId == null ? (i + 1) % 2 == 0 : pageRef.getPageNumber() % 2 == 0) == even) {
            result.add(docId == null ? new PageRef(null, i + 1) : pageRef);
          }
        } else {
          result.add(docId == null ? new PageRef(null, i + 1) : pageRef);
        }
      }
    }
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
