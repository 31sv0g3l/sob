package com.binderator.util;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

@SuppressWarnings("unused")
public class Translations {

  public interface ErrorHandler {

    void handleError(String message);

  }

  public enum Language {
    EN, FR, DE, CN, JP
  };

  public record LanguageInfo(Language language, String englishName, String name)
  {}

  static Map<Language, LanguageInfo> languageInfo = new HashMap<>() {{
    put(Language.EN, new LanguageInfo(Language.EN, "English", "English"));
    put(Language.FR, new LanguageInfo(Language.FR, "French", "Francais"));
    put(Language.DE, new LanguageInfo(Language.DE, "German", "Deutsch"));
    put(Language.CN, new LanguageInfo(Language.CN, "Chinese", "中文"));
    put(Language.JP, new LanguageInfo(Language.JP, "Japanese", "日本"));
  }};

 private static final Map<Language, Map<String, String>> translationsMap = new HashMap<>();

  private static Language currentLanguage = Language.EN;
  private static ErrorHandler errorHandler = null;

  public static void setCurrentLanguageKey(Language language)
  {
    currentLanguage = language;
  }

  public static void setErrorHandler(ErrorHandler errorHandler)
  {
    Translations.errorHandler = errorHandler;
  }

  public static void initialise(String translationsPath)
  throws Exception
  {
    translationsMap.clear();
    File translationsDirectory = new File(translationsPath);
    if (!translationsDirectory.canExecute() || !translationsDirectory.canRead()) {
      throw new Exception("Can't read translations directory at path \"" + translationsPath + "\"");
    }
    for (Language language : Language.values()) {
      File languageTranslationsPath = new File(translationsDirectory, language.name());
      if (languageTranslationsPath.canRead()) {
        translationsMap.put(language, readTranslationsFile(languageTranslationsPath));
      }
    }
  }

  public static void initialiseFromJar(String translationsPath)
  throws Exception
  {
    translationsMap.clear();
    File translationsDirectory = new File(translationsPath);
    for (Language language : Language.values()) {
      File languageTranslationsPath = new File(translationsDirectory, language.name());
      String path = languageTranslationsPath.getAbsolutePath();
      // On mswindows, the path gets prepended with a drive id:
      if (path.matches("[a-zA-Z]:.*")) {
        path = path.substring(2);
      }
      // On mswindows, the path has broken backslash separators:
      path = path.replaceAll("\\\\", "/");
      InputStream inputStream = Translations.class.getResourceAsStream(path);
      if (inputStream != null) {
        translationsMap.put(language, readTranslationsFromStream(path, inputStream));
      }
    }
  }

  public static InputStream openTranslatableURLAsStream(java.net.URL url)
  throws Exception
  {
    return openTranslatableURLAsStream(url, currentLanguage);
  }

  public static InputStream openTranslatableURLAsStream(java.net.URL url, Language language)
  throws IOException
  {
    java.net.URL translationURL;
    if (url.getPath().endsWith("/")) {
      translationURL = new URL(url, language.name());
    } else {
      translationURL = new URL(url.toString() + "/" + language.name());
    }
    InputStream inputStream;
    // Detect JAR URL:
    int bangIndex = translationURL.getPath().lastIndexOf('!');
    try {
      if (bangIndex >= 0) {
        inputStream = Translations.class.getResourceAsStream(translationURL.getPath().substring(bangIndex + 1));
      } else {
        inputStream = translationURL.openStream();
      }
    } catch (Exception e) {
      inputStream = null;
    }
    if (inputStream != null) {
      return inputStream;
    }
    // Translation path didn't exist - try treating the url as a file instead of a translations directory:
    try {
      if (bangIndex >= 0) {
        return Translations.class.getResourceAsStream(url.getPath().substring(bangIndex + 1));
      } else {
        return url.openStream();
      }
    } catch (Exception e) {
      return null;
    }
  }

  private static final Pattern translationsPattern = Pattern.compile(
    "(\\s*([_a-zA-Z0-9]+)\\s*:\\s*\\{%(([^%]|%[^}])*)%}\\s*)*"
  );

  private static void throwParseException(String path, int lineCount, int columnCount, char character)
  throws Exception {
    throw new Exception(
      "Invalid char at line " + lineCount + ", column " + columnCount + " in translations file at path " + path
    );
  }

  private static void throwEOFException(String path)
  throws Exception
  {
    throw new Exception("Unexpected end of translations file at path " + path);
  }

  // Parse a translations file with the syntax:
  //
  // someAlphanumericKey {
  //    blah blah value blah blah
  // }
  //
  // some.OtherKey { blah value blah }
  //

  public static Map<String, String> readTranslationsFileFromJar(String path)
  throws Exception
  {
    try (InputStream inputStream = Translations.class.getResourceAsStream(path)) {
      if (inputStream != null) {
        return readTranslationsFromStream(path, inputStream);
      } else {
        System.err.println("Couldn't open translations resource from jar at " + path);
        return null;
      }
    } catch (IOException e) {
      System.err.println("Couldn't read translations resource from jar at " + path);
      return null;
    }

  }

  private static Map<String, String> readTranslationsFile(File path)
  throws Exception
  {
    if (!path.canRead()) {
      throw new Exception("Translations file at path " + path + " is not readable");
    }
    return readTranslationsFromStream(path.getPath(), new FileInputStream(path));
  }

  private static Map<String, String> readTranslationsFromStream(String path, InputStream inputStream)
  throws Exception
  {
    String fileString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    Map<String, String> translations = new HashMap<>();
    StringBuilder builder = new StringBuilder();
    String key = "";
    String value;
    char lastCharacter = 0;
    int lineCount = 1;
    int columnCount = 1;
    final int STATE_START = 0;
    final int STATE_IN_KEY = 1;
    final int STATE_AFTER_KEY = 2;
    final int STATE_IN_VALUE = 3;
    final int STATE_IN_COMMENT = 4;
    int state = STATE_START;
    char[] fileCharacters = fileString.toCharArray();
    for (char character : fileCharacters) {
      switch (state) {
        case STATE_START -> {
          if (character == '\n') {
            lineCount++;
            columnCount = 1;
          } else if (character == '#') {
            columnCount++;
            state = STATE_IN_COMMENT;
          } else if (((character >= 'a') && (character <= 'z')) ||
              ((character >= 'A') && (character <= 'Z')) ||
              ((character >= '0') && (character <= '9')) ||
              (character == '_') ||
              (character == '.')) {
            builder.setLength(0);
            builder.append(character);
            state = STATE_IN_KEY;
            columnCount++;
          } else if ((character == ' ') || (character == '\t') || (character == '\r')) {
            columnCount++;
          } else {
            throwParseException(path, lineCount, columnCount, character);
          }
        }
        case STATE_IN_KEY -> {
          if (character == '\n') {
            lineCount++;
            columnCount = 1;
            state = STATE_AFTER_KEY;
            key = builder.toString();
          } else if (((character >= 'a') && (character <= 'z')) ||
              ((character >= 'A') && (character <= 'Z')) ||
              ((character >= '0') && (character <= '9')) ||
              (character == '_') ||
              (character == '.')) {
            builder.append(character);
            columnCount++;
          } else if ((character == ' ') || (character == '\t') || (character == '\r')) {
            columnCount++;
            state = STATE_AFTER_KEY;
            key = builder.toString();
          } else {
            throwParseException(path, lineCount, columnCount, character);
          }
        }
        case STATE_AFTER_KEY -> {
          if (character == '\n') {
            lineCount++;
            columnCount = 1;
          } else if ((character == ' ') || (character == '\t') || (character == '\r')) {
            columnCount++;
          } else if ((character == '{')) {
            columnCount++;
            state = STATE_IN_VALUE;
            builder.setLength(0);
          } else {
            throwParseException(path, lineCount, columnCount, character);
          }
        }
        case STATE_IN_VALUE -> {
          if (character == '\n') {
            lineCount++;
            columnCount = 1;
            builder.append(character);
          } else if ((character == ' ') || (character == '\t') || (character == '\r')) {
            columnCount++;
            builder.append(character);
          } else if (character == '}') {
            columnCount++;
            translations.put(key, builder.toString().trim());
            builder.setLength(0);
            state = STATE_START;
          } else {
            builder.append(character);
            columnCount++;
          }
        }
        case STATE_IN_COMMENT -> {
          if (character == '\n') {
            lineCount++;
            columnCount = 1;
            state = STATE_START;
          } else {
            columnCount++;
          }
        }
      }
    }
    if (state != STATE_START) {
      throwEOFException(path);
    }
    return translations;
  }

  public static String translate(Language language, String key)
  {
    Map<String, String> translations = translationsMap.get(language);
    if (translations != null) {
      return translations.get(key);
    }
    return null;
  }

  public static String translate(String key)
  {
    String translation = translate(currentLanguage, key);
    if (translation == null) {
      if (errorHandler != null) {
        errorHandler.handleError("translation for key \"" + key + "\" is missing");
      }
    }
    return translation;
  }

}
