package com.binderator.gui;


import com.binderator.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;


import static com.binderator.gui.GUIUtils.scale;
import static com.binderator.util.Translations.translate;


public class InlineHelp extends JDialog implements HyperlinkListener {

  @Serial
  private static final long serialVersionUID = -6860392014417082928L;
  private final URL helpBaseURL;
  private final URL helpInitialURL;
  private URL currentURL = null;
  private final JEditorPane editorPane;
  private static InlineHelp instancePtr = null;

  private InlineHelp
  (JFrame parent, URL helpBaseURL)
  throws Exception
  {
    this(parent, helpBaseURL, "/main");
  }

  private InlineHelp
  (JFrame parent, URL helpBaseURL, String initialPath)
  throws Exception
  {
    super(parent, translate("inlineHelp"), false);
    this.helpBaseURL = helpBaseURL;
    editorPane = new JEditorPane();
    HTMLEditorKit kit = new HTMLEditorKit();
    editorPane.setContentType("text/html");
    editorPane.setBackground(Color.WHITE);
    editorPane.setEditorKit(kit);
    editorPane.setEditable(false);
    editorPane.addHyperlinkListener(this);
    JPanel helpPanel = (JPanel)getContentPane();
    helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
    JScrollPane jScrollPane = new JScrollPane(editorPane);
    jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    helpPanel.add(jScrollPane);
    helpPanel.add(Box.createVerticalStrut(scale(10)));
    JButton doneButton = new JButton(translate("done"));
    doneButton.addActionListener(e -> {
      setVisible(false);
    });
    JButton homeButton = new JButton(translate("home"));
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(homeButton);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(doneButton);
    buttonPanel.add(Box.createHorizontalGlue());
    helpPanel.add(buttonPanel);
    helpPanel.add(Box.createVerticalStrut(scale(10)));
    pack();
    Dimension size = new Dimension(scale(814), scale(800));
    setMinimumSize(size);
    setSize(size);
    setPreferredSize(size);
    setLocationRelativeTo(null);
    loadPath(initialPath);
    helpInitialURL = currentURL;
    homeButton.addActionListener(e -> {
      try {
        loadURL(helpInitialURL);
      } catch (Throwable t) {
        errorDialog(t);
      }
    });
  }

  private void errorDialog
  (Throwable t)
  {
    JOptionPane.showMessageDialog(this, (t.getMessage()));
    t.printStackTrace(System.err);
  }

  private URL pathToURL
  (String path)
  throws Exception
  {
    String fullPath = helpBaseURL.getPath();
    if (!path.startsWith("/")) {
      String currentPath = currentURL.getPath();
      fullPath = currentPath.substring(0, currentPath.lastIndexOf('/') + 1);
      while (path.startsWith("../")) {
        fullPath = fullPath.substring(0, fullPath.lastIndexOf('/') + 1);
        path = path.substring(3);
      }
    }
    fullPath += path;
    return new URL(helpBaseURL, fullPath);
  }

  private void loadPath
  (String path)
  throws Exception
  {
    currentURL = pathToURL(path);
    loadURL(currentURL);
  }

  private void loadURL
  (URL url)
  throws Exception
  {
    InputStream inputStream = Translations.openTranslatableURLAsStream(url);
    if (inputStream != null) {
      String content = new String(inputStream.readAllBytes());
      editorPane.setText(content);
      HTMLDocument document = (HTMLDocument)editorPane.getDocument();
      correctImageLinks(document);
      editorPane.setCaretPosition(0);
    } else {
      editorPane.setText("<html><body><em>Unable to load help URL:<br>\n" + url + "</em></body></html>");
    }
  }

  public void correctImageLinks
  (HTMLDocument document)
  throws Exception
  {
    Element rootElement = document.getDefaultRootElement();
    // Replace the src attribute of all <img> elements:
    HTMLDocument.Iterator imgElementIterator = document.getIterator(HTML.Tag.IMG);
    while (imgElementIterator.isValid()) {
      AttributeSet attributeSet = imgElementIterator.getAttributes();
      String srcAttribute = (String)attributeSet.getAttribute(HTML.Attribute.SRC);
      if (srcAttribute == null) {
        continue;
      }
      Element element = document.getElement(rootElement, HTML.Attribute.SRC, srcAttribute);
      if (element != null) {
        URL imageURL = pathToURL(srcAttribute);
        String imageURLString = imageURL.toString();
        // Detect JAR URL:
        int bangIndex = imageURLString.lastIndexOf('!');
        if (bangIndex >= 0) {
          imageURLString = "jar:" + imageURLString;
        }
        Rectangle imageSize = getImageSize(imageURL);
        System.err.print("Image: url: " + imageURL.getPath() + " width: " + imageSize.getWidth() + " height: " + imageSize.getHeight());
        float imageScale = 1.0f;
        int imageWidth = (int)(imageSize.getWidth()/imageScale);
        int imageHeight = (int)(imageSize.getHeight()/imageScale);
        System.err.println(" scaledWidth: " + imageWidth + " scaledHeight: " + imageHeight);
        if ((imageSize.getWidth() > 0) && (imageSize.getHeight() > 0)) {
          document.setOuterHTML(
            element,
            "<img src=\"" + imageURLString + "\" width=\"" + imageWidth + "\" height=\"" + imageHeight + "\">"
          );
        } else {
          document.setOuterHTML(element,  "<b>! IMG ERROR !</b>");
        }
      }
      imgElementIterator.next();
    }
  }

  public synchronized static void initialise
  (JFrame parent, URL helpBaseURL)
  throws Exception
  {
    if (instancePtr != null) {
      throw new Exception("Attempt to initialise InlineHelp singleton more than once");
    }
    instancePtr = new InlineHelp(parent, helpBaseURL);
  }

  public synchronized static InlineHelp instance
  ()
  throws Exception
  {
    if (instancePtr == null) {
      throw new Exception("Attempt to access uninitialised InlineHelp singleton");
    }
    return instancePtr;
  }

  @Override
  public void hyperlinkUpdate
  (HyperlinkEvent event)
  {
    String path;
    URL url = event.getURL();
    if (url != null) {
      path = url.getPath();
      if (path != null) {
        if (path.startsWith(helpBaseURL.getPath())) {
          path = path.substring(helpBaseURL.getPath().length());
        }
      }
    } else {
      path = event.getDescription();
    }
    if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
      Output.errorOut("HELP PATH: " + path);
    } else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        loadPath(path);
      } catch (Exception e) {
        Output.errorOut("Exception loading path \"" + path + "\": " + e.getMessage());
      }
    }
  }

  private static Rectangle getImageSize
  (URL imageURL)
  throws Exception
  {
    int bangIndex = imageURL.getPath().lastIndexOf('!');
    InputStream inputStream = null;
    try {
      if (bangIndex >= 0) {
        inputStream = Translations.class.getResourceAsStream(imageURL.getPath().substring(bangIndex + 1));
      } else {
        inputStream = imageURL.openStream();
      }
      if (inputStream != null) {
        BufferedImage image = ImageIO.read(inputStream);
        return new Rectangle(image.getWidth(), image.getHeight());
      }
    } catch (Throwable ignored) {}
    return new Rectangle(0, 0);
  }

}
