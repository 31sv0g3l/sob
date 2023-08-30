package com.binderator.engine;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Color;


import static com.binderator.util.Translations.translate;


public class ContentGenerator implements Serializable {

  public enum Alignment {

    CENTRE(translate("contentGeneratorsAlignCentre")),
    LEFT(translate("contentGeneratorsAlignLeft")),
    RIGHT(translate("contentGeneratorsAlignRight"));

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


  public interface TextComponent extends Serializable {

    String toString
    (PageRef pageRef, int absolutePageNumber);

  }


  public class StringTextComponent implements TextComponent {

    final String content;

    public StringTextComponent
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


  public class PageRefTextComponent implements TextComponent {

    final String docId;

    final int offset;

    public PageRefTextComponent
    (int offset)
    {
      this(null, offset);
    }

    public PageRefTextComponent
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
  private List<TextComponent> textComponents = null;
  private boolean useFrame = false;
  private Float width = null;
  private Float height = null;
  private Float borderWidth = null;
  private Color backgroundColor = null;
  private Color textColor = null;
  private Color borderColor = null;

  public ContentGenerator
  ()
  {}

  public ContentGenerator
  (ContentGenerator contentGenerator)
  {
    name = contentGenerator.name;
    comment = contentGenerator.comment;
    pageRanges = new ArrayList<>();
    pageRanges.addAll(contentGenerator.getPageRanges());
    initialContent = contentGenerator.initialContent;
    content = contentGenerator.content;
    horizontalOffset = contentGenerator.horizontalOffset;
    verticalOffset = contentGenerator.verticalOffset;
    alignment = contentGenerator.alignment;
    lineHeightFactor = contentGenerator.lineHeightFactor;
    font = contentGenerator.font;
    useFrame = contentGenerator.useFrame;
    width = contentGenerator.width;
    height = contentGenerator.height;
    borderWidth = contentGenerator.borderWidth;
    backgroundColor = contentGenerator.backgroundColor;
    textColor = contentGenerator.textColor;
    borderColor = contentGenerator.borderColor;
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
    List<TextComponent> newTextComponents = new ArrayList<>();
    String contentCopy = content;
    Matcher idOffsetMatcher = idOffsetPattern.matcher(contentCopy);
    while (idOffsetMatcher.find()) {
      if (idOffsetMatcher.start() > 0) {
        newTextComponents.add(new StringTextComponent(contentCopy.substring(0, idOffsetMatcher.start())));
      }
      String docId = idOffsetMatcher.group(1);
      if ((docId != null) && (docId.length() == 0)) {
        docId = null;
      }
      String plusMinus = idOffsetMatcher.group(3);
      plusMinus = plusMinus != null ? plusMinus : "";
      String offsetSource = idOffsetMatcher.group(4);
      int offset = plusMinus.length() > 0 ? Integer.parseInt(plusMinus + offsetSource) : 0;
      newTextComponents.add(new PageRefTextComponent(docId, offset));
      contentCopy = contentCopy.substring(idOffsetMatcher.end());
      idOffsetMatcher = idOffsetPattern.matcher(contentCopy);
    }
    if (contentCopy.length() > 0) {
      newTextComponents.add(new StringTextComponent(contentCopy));
    }
    textComponents = newTextComponents;
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
    for (TextComponent textComponent : textComponents) {
      builder.append(textComponent.toString(pageRef, absolutePageNumber));
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
