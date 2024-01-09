package com.binderator.engine;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;


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


  public static class StringTextComponent implements TextComponent {

    private static final long serialVersionUID = -8091184665938914725L;

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


  public static class PageRefTextComponent implements TextComponent {

    private static final long serialVersionUID = 7504405027534443051L;

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
  private Float lineOffsetFactor = null;
  private Integer columns = null;
  private List<TextComponent> textComponents = null;
  private boolean useFrame = false;
  private Float width = null;
  private Float xyRatio = null;
  private boolean useBorder = false;
  private Float borderWidth = null;
  private Color borderColor = null;
  public enum BackgroundType { NONE, COLOR, IMAGE }
  private BackgroundType backgroundType;
  private Color backgroundColor = null;
  private String backgroundImagePath = null;
  private Color textColor = null;

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
    lineOffsetFactor = contentGenerator.lineOffsetFactor;
    font = contentGenerator.font;
    useFrame = contentGenerator.useFrame;
    width = contentGenerator.width;
    xyRatio = contentGenerator.xyRatio;
    useBorder = contentGenerator.useBorder;
    borderWidth = contentGenerator.borderWidth;
    borderColor = contentGenerator.borderColor;
    backgroundType = contentGenerator.backgroundType;
    backgroundColor = contentGenerator.backgroundColor;
    backgroundImagePath = contentGenerator.backgroundImagePath;
    textColor = contentGenerator.textColor;
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
  (PageRange ... pageRanges)
  {
    this.pageRanges = new ArrayList<>();
    Collections.addAll(this.pageRanges, pageRanges);
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
    return content != null ? content: "";
  }

  public String getContent
  (PageRef pageRef, int absolutePageNumber)
  {
    StringBuilder builder = new StringBuilder();
    if (textComponents != null) {
      for (TextComponent textComponent : textComponents) {
        builder.append(textComponent.toString(pageRef, absolutePageNumber));
      }
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
    return horizontalOffset != null ? horizontalOffset : 0.0f;
  }

  public void setHorizontalOffset
  (float horizontalOffset)
  {
    this.horizontalOffset = horizontalOffset;
  }

  public Float getVerticalOffset
  ()
  {
    return verticalOffset != null ? verticalOffset : 0.0f;
  }

  public void setVerticalOffset
  (float verticalOffset)
  {
    this.verticalOffset = verticalOffset;
  }

  public java.awt.Font getFont
  ()
  {
    return font != null ? font : new JLabel().getFont();
  }

  public void setFont
  (java.awt.Font font)
  {
    this.font = font;
  }

  public java.awt.Color getTextColor
  ()
  {
    return textColor !=  null ? textColor : Color.BLACK;
  }

  public void setTextColor
  (java.awt.Color color)
  {
    textColor = color;
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

  public void setUsingFrame
  (boolean useFrame)
  {
    this.useFrame = useFrame;
  }

  public boolean isUsingFrame
  ()
  {
    return useFrame;
  }

  public Float getWidth
  ()
  {
    return width != null ? width : 0.0f;
  }

  public void setWidth
  (Float width)
  {
    this.width = width;
  }

  public Float getXYRatio
  ()
  {
    return xyRatio != null ? xyRatio : 1.0f;
  }

  public void setXYRatio
  (Float xyRatio)
  {
    this.xyRatio = xyRatio;
  }

  public void setUsingBorder
  (boolean usingBorder)
  {
    this.useBorder = usingBorder;
  }

  public boolean isUsingBorder
  ()
  {
    return useBorder;
  }

  public void setBorderWidth
  (Float borderWidth)
  {
    this.borderWidth = borderWidth;
  }

  public Float getBorderWidth
  ()
  {
    return borderWidth != null ? borderWidth : 0.0f;
  }

  public void setBorderColor
  (Color color)
  {
    this.borderColor = color;
  }

  public Color getBorderColor
  ()
  {
    return borderColor;
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

  public float getLineOffsetFactor
  ()
  {
    return lineOffsetFactor != null ? lineOffsetFactor : 0.0f;
  }

  public void setLineOffsetFactor
  (Float lineOffsetFactor)
  {
    this.lineOffsetFactor = lineOffsetFactor;
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

  public void setBackgroundType
  (BackgroundType backgroundType)
  {
    this.backgroundType = backgroundType;
  }

  public BackgroundType getBackgroundType
  ()
  {
    return backgroundType != null ? backgroundType : BackgroundType.NONE;
  }

  public void setBackgroundColor
  (Color backgroundColor)
  {
    this.backgroundColor = backgroundColor;
  }

  public Color getBackgroundColor
  ()
  {
    return backgroundColor;
  }

  public void setBackgroundImagePath
  (String path)
  {
    this.backgroundImagePath = path;
  }

  public String getBackgroundImagePath
  ()
  {
    return backgroundImagePath;
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

  public void copy
  (ContentGenerator other)
  {
    this.name = other.name;
    this.pageRanges = new ArrayList<>();
    this.pageRanges.addAll(other.pageRanges);
    this.comment = other.comment;
    this.initialContent = other.initialContent;
    this.content = other.content;
    this.horizontalOffset = other.horizontalOffset;
    this.verticalOffset = other.verticalOffset;
    this.font = other.font;
    this.alignment = other.alignment;
    this.lineHeightFactor = other.lineHeightFactor;
    this.lineOffsetFactor = other.lineOffsetFactor;
    this.columns = other.columns;
    this.textComponents = other.textComponents;
    this.useFrame = other.useFrame;
    this.width = other.width;
    this.xyRatio = other.xyRatio;
    this.useBorder = other.useBorder;
    this.borderWidth = other.borderWidth;
    this.borderColor = other.borderColor;
    this.backgroundType = other.backgroundType;
    this.backgroundColor = other.backgroundColor;
    this.backgroundImagePath = other.backgroundImagePath;
    this.textColor = other.textColor;
  }

}
