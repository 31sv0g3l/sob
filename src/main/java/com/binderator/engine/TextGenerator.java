package com.binderator.engine;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.binderator.util.Translations.translate;


public class TextGenerator implements Serializable {

  public enum Alignment {

    CENTRE(translate("textGeneratorsAlignCentre")),
    LEFT(translate("textGeneratorsAlignLeft")),
    RIGHT(translate("textGeneratorsAlignRight"));

    private final String name;

    Alignment
    (String name)
    {
      this.name = name;
    }

    public String toString
    ()
    {
      return name;
    }

  }


  public interface Component {

    String toString
    (PageRef pageRef, int absolutePageNumber);

  }


  public class StringComponent implements Component {

    final String content;

    public StringComponent
    (String content)
    {
      this.content = content;
    }

    @Override
    public String toString
    (PageRef pageRef, int absolutePageNumber)
    {
      return content;
    }

  }


  public class PageRefComponent implements Component {

    final String docId;

    final int offset;

    public PageRefComponent
    (int offset)
    {
      this(null, offset);
    }

    public PageRefComponent
    (String docId, int offset)
    {
      this.docId = docId;
      this.offset = offset;
    }

    @Override
    public String toString
    (PageRef pageRef, int absolutePageNumber)
    {
      if (docId != null) {
        if (Objects.equals(docId, pageRef.getSourceDocument().getId())) {
          return "" + (pageRef.getPageNumber() + offset);
        } else {
          String offsetString = "";
          if (offset > 0) {
            offsetString = "+" + offset;
          } else if (offset < 0) {
            offsetString = "" + offset;
          }
          return "#" + docId + offsetString + "#";
        }
      } else {
        return "" + (absolutePageNumber + offset);
      }
    }

  }


  @Serial
  private static final long serialVersionUID = 8224030971129010709L;
  private String name = null;
  private List<PageRange> pageRanges = new ArrayList<>();
  private String comment = null;
  private String initialContent = null;
  private String content = null;
  private Float horizontalOffset = null;
  private Float verticalOffset = null;
  private java.awt.Font font = null;
  private Alignment alignment = Alignment.CENTRE;
  private Float lineHeightFactor = null;
  private Integer columns = null;
  private List<Component> components = null;

  public TextGenerator
  ()
  {}

  public TextGenerator
  (TextGenerator textGenerator)
  {
    name = textGenerator.name;
    comment = textGenerator.comment;
    pageRanges = new ArrayList<>();
    pageRanges.addAll(textGenerator.getPageRanges());
    initialContent = textGenerator.initialContent;
    content = textGenerator.content;
    horizontalOffset = textGenerator.horizontalOffset;
    verticalOffset = textGenerator.verticalOffset;
    alignment = textGenerator.alignment;
    lineHeightFactor = textGenerator.lineHeightFactor;
    font = textGenerator.font;
  }

  static Pattern idOffsetPattern = Pattern.compile(
    "#\\s*([_a-zA-Z][_a-zA-Z0-9]*)?\\s*((\\+|\\-)\\s*([1-9][0-9]?[0-9]?[0-9]?))?\\s*#"
  );

  public void compile
  ()
  {
    if ((content == null) || content.isEmpty()) {
      return;
    }
    List<Component> newComponents = new ArrayList<>();
    String contentCopy = content;
    Matcher idOffsetMatcher = idOffsetPattern.matcher(contentCopy);
    while (idOffsetMatcher.find()) {
      if (idOffsetMatcher.start() > 0) {
        newComponents.add(new StringComponent(contentCopy.substring(0, idOffsetMatcher.start())));
      }
      String docId = idOffsetMatcher.group(1);
      if ((docId != null) && (docId.length() == 0)) {
        docId = null;
      }
      String plusMinus = idOffsetMatcher.group(3);
      plusMinus = plusMinus != null ? plusMinus : "";
      String offsetSource = idOffsetMatcher.group(4);
      int offset = plusMinus.length() > 0 ? Integer.parseInt(plusMinus + offsetSource) : 0;
      newComponents.add(new PageRefComponent(docId, offset));
      contentCopy = contentCopy.substring(idOffsetMatcher.end());
      idOffsetMatcher = idOffsetPattern.matcher(contentCopy);
    }
    if (contentCopy.length() > 0) {
      newComponents.add(new StringComponent(contentCopy));
    }
    components = newComponents;
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

  public void setPageRanges
  (List<PageRange> pageRanges)
  {
    this.pageRanges = pageRanges;
  }

  public Collection<PageRange> getPageRanges
  ()
  {
    return pageRanges;
  }

  public String getPageRangesString
  ()
  {
    return PageRange.toString(pageRanges);
  }

  public String getInitialContent
  ()
  {
    return initialContent;
  }

  public void setInitialContent
  (String initialContent)
  {
    this.initialContent = initialContent;
  }

  public String getContent
  ()
  {
    return content;
  }

  public String getContent
  (PageRef pageRef, int absolutePageNumber)
  {
    StringBuilder builder = new StringBuilder();
    for (Component component : components) {
      builder.append(component.toString(pageRef, absolutePageNumber));
    }
    return builder.toString();
  }

  public void setContent
  (String content)
  {
    this.content = content;
  }

  public Float getHorizontalOffset
  ()
  {
    return horizontalOffset;
  }

  public void setHorizontalOffset
  (float horizontalOffset)
  {
    this.horizontalOffset = horizontalOffset;
  }

  public Float getVerticalOffset
  ()
  {
    return verticalOffset;
  }

  public void setVerticalOffset
  (float verticalOffset)
  {
    this.verticalOffset = verticalOffset;
  }

  public java.awt.Font getFont
  ()
  {
    return font;
  }

  public void setFont
  (java.awt.Font font)
  {
    this.font = font;
  }

  public Alignment getAlignment
  ()
  {
    return alignment;
  }

  public void setAlignment
  (Alignment alignment)
  {
    this.alignment = alignment;
  }

  public float getLineHeightFactor
  ()
  {
    return lineHeightFactor != null ? lineHeightFactor : 1.0f;
  }

  public void setLineHeightFactor
  (Float lineHeightFactor)
  {
    this.lineHeightFactor = lineHeightFactor;
  }

  public Integer getColumns
  ()
  {
    return columns;
  }

  public void setColumns
  (Integer columns)
  {
    this.columns = columns;
  }

  @Override
  public String toString
  ()
  {
    if (name != null) {
      return name;
    } else {
      return "TextGenerator(\"" + name + "\")";
    }
  }

}
