package com.binderator.util;


@SuppressWarnings("unused")
public class StringUtils {

  public static String[] toLines
  (String source)
  {
    return toLines(source, true);
  }

  public static String[] toLines
  (String source, boolean retainNewlines)
  {
    String[] lines = source.split("\\n");
    if (retainNewlines) {
      for (int i = 0; i < lines.length - 1; i++) {
        lines[i] = lines[i] + "\n";
      }
    }
    return lines;
  }

  public static String escape
  (String source)
  {
    char[] chars = source.toCharArray();
    StringBuilder result = new StringBuilder();
    for (char thisChar : chars) {
      switch (thisChar) {
        case '\\' -> result.append('\\');
        case '\t' -> result.append("\\t");
        case '\r' -> result.append("\\r");
        case '\n' -> result.append("\\n");
        default -> result.append(thisChar);
      }
    }
    return result.toString();
  }

  public static String[] toJavaLiteralLines
  (String source)
  {
    String[] lines = StringUtils.toLines(source);
    for (int i = 0; i < lines.length; i++) {
      lines[i] = "\"" + escape(lines[i]) + "\"";  
    }
    return lines;
  }
  
  
  public interface JoinedPrinter {
    
    String toString
    (Object joined);
    
  }
  

  public static String join
  (Iterable<?> elements, String filler, JoinedPrinter printer)
  {
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (Object element : elements) {
      if (!first) {
        result.append(filler);
      }
      first = false;
      if (printer != null) {
        result.append(printer.toString(element));
      } else {
        result.append(element);
      }
    }
    return result.toString();
  }
  
  public static String join
  (Iterable<?> elements, String filler)
  {
    JoinedPrinter printer = Object::toString;
    return join(elements, filler, printer);
  }

  public static <T> String join
  (T[] elements, String filler)
  {
    StringBuilder buffer = new StringBuilder();
    boolean first = true;
    for (T element : elements) {
      if (!first) {
        buffer.append(filler);
      }
      first = false;
      buffer.append(element);
    }
    return buffer.toString();
  }
  
  public static boolean isIn
  (char needle, char[] haystack)
  {
    for (char c : haystack) {
      if (needle == c) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean isWhitespace
  (char character)
  {
    return character == ' ' || character == '\t' || character == '\r' || character == 'n';
  }
  
  public static String capitalise
  (String string)
  {
    char[] chars = string.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (((i == 0)||isWhitespace(chars[i - 1]))&&((chars[i] >= 'a')&&(chars[i] <= 'z'))) {
        chars[i] = (char)('A' + ((int)chars[i] - (int)'a'));
      }
    }
    return new String(chars);
  }
  
  public static String uppercase
  (String string)
  {
    char[] chars = string.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if ((chars[i] >= 'a')&&(chars[i] <= 'z')) {
        chars[i] = (char)('A' + ((int)chars[i] - (int)'a'));
      }
    }
    return new String(chars);
  }
  
  public static String lowercase
  (String string)
  {
    char[] chars = string.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if ((chars[i] >= 'A')&&(chars[i] <= 'Z')) {
        chars[i] = (char)('a' + ((int)chars[i] - (int)'A'));
      }
    }
    return new String(chars);
  }
  
}
