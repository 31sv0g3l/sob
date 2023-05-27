package com.binderator.gui;


import com.binderator.persistence.*;
import com.binderator.engine.*;
import com.binderator.util.*;
import com.formdev.flatlaf.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.*;
import javax.swing.text.*;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;


import static com.binderator.util.Translations.translate;
import static com.binderator.gui.GUIUtils.scale;


public class BinderatorFrame extends JFrame
  implements ActionListener, UnsavedChangeListener, Book.StatusListener, ICEViewer.CloseListener {

  private class ViewerRenderingThread extends Worker {

    @Override
    public void task
    ()
    {
      if (viewerActive) {
        bookLock.lock();
        try {
          if (book.getPageCount() > 0) {
            Book book = new Book(getBook());
            bookLock.unlock();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            book.generatePDF(byteArrayOutputStream, book.isUsingMargins(), book.isUsingPageNumbering(), null);
            byte[] bookBytes = byteArrayOutputStream.toByteArray();
            int pageNumber = viewer.controller.getCurrentPageNumber();
            viewer.setContent(bookBytes);
            viewer.controller.showPage(pageNumber);
          } else {
            viewer.setContent(new byte[0]);
          }
        } catch (Exception e) {
          errorDialog(e);
        } finally {
          if (bookLock.isHeldByCurrentThread()) {
            bookLock.unlock();
          }
        }
      }
    }

  }


  @Serial
  private static final long serialVersionUID = -1429747105438739695L;
  public static final String VERSION = "0.2.2";
  private static BinderatorFrame singletonInstance = null;
  private boolean haveUnsavedChanges = false;
  private boolean haveViewerContentChange = false;
  private ViewerRenderingThread viewerRenderingThread = null;
  String projectPath = null;
  JPanel mainPanel;
  JPanel buttonPanel;
  JPanel statusPanel;
  JPanel statusStringFullPanel; // to be swapped in as the only child of statusPanel
  JPanel statusStringPanel;  // actual container of status string (one child, the label)
  JPanel statusProgressBarFullPanel; // to be swapped in as the only child of statusPanel
  JPanel statusProgressBarLabelPanel; // container of label for progress bar (one child)
  JPanel statusProgressBarPanel; // to be swapped in as the only child of statusPanel
  JProgressBar statusProgressBar; // actual container of progress bar (one child)
  JButton generateButton;
  JButton generateSignaturesButton;
  JButton viewerButton;
  JButton exitButton;
  JPanel projectPanel;
  JPanel documentsPanel;
  JPanel pagesPanel;
  JPanel documentsAndPagesPanel;
  JPanel transformSetsPanel;
  JPanel transformsPanel;
  ImageIcon newIcon;
  ImageIcon deleteIcon;
  ImageIcon upIcon;
  ImageIcon downIcon;
  private static final String ACTION_EXIT = "exit";
  private static final String ACTION_GENERATE = "generate";
  private static final String ACTION_GENERATE_SIGNATURES = "generateSignatures";
  private static final String ACTION_SHOW_HIDE_VIEWER = "showHideViewer";
  private boolean viewerActive = false;
  Book book = null;
  ReentrantLock bookLock = new ReentrantLock();
  JComboBox<SourceDocument> sourceDocumentsComboBox;
  DefaultComboBoxModel<SourceDocument> sourceDocumentsComboBoxModel;
  JComboBox<TransformSet> transformSetsComboBox;
  DefaultComboBoxModel<TransformSet> transformSetComboBoxModel;
  // Project Panel Widgets:
  JTextField projectNameTextField;
  JTextField projectOutputPathTextField;
  JButton projectOutputPathButton;
  JTextField projectSignaturesOutputPathTextField;
  JButton projectSignaturesOutputPathButton;
  JTextArea projectCommentTextArea;
  JCheckBox projectScaleToFitCheckBox;
  JCheckBox projectEnableMarginsCheckBox;
  JCheckBox projectEnablePageNumberingCheckBox;
  JComboBox<Pair<Rectangle, Rectangle>> pageSizePairsComboBox;
  DefaultComboBoxModel<Pair<Rectangle, Rectangle>> pageSizePairsComboBoxModel;
  JPanel projectControlsPanel;
  // Documents Panel Widgets:
  JTextField documentNameTextField = new JTextField(34);
  JTextField documentIdentifierTextField = new JTextField(8);
  JTextField documentPathTextField = new JTextField(34);
  JButton documentPathButton;
  JTextArea documentCommentTextArea = new JTextArea(5, 34);
  JTextArea pageRangesTextArea = new JTextArea(7, 34);
  // Page Operations Panel Widgets:
  JTextField transformSetNameTextField = new JTextField(34);
  JTextField transformSetPageRangesTextField = new JTextField(34);
  JTextArea transformSetCommentTextArea = new JTextArea(5, 34);
  private TransformSet selectedTransformSet = null;
  private SourceDocument selectedDocument = null;
  private JDialog optionsDialog = null;
  private JDialog inlineHelpDialog = null;
  private boolean showProgressBars = true;
  private ICEViewer viewer = null;
  private final FileFilter bdrFilter = new FileNameExtensionFilter("Son of Binderator files", "bdr");
  private String basePath = null;


  private void execute
  (CommandQueue.Command command)
  {
    execute(command, false);
  }

  private void execute
  (CommandQueue.Command command, boolean synchronous)
  {
    CommandQueue.getInstance().execute(command, synchronous);
  }

  @SuppressWarnings("unused")
  private Pair<JPanel, JButton> createScaledLabeledPathSelectionWidgetPanel
  (JTextField entryWidget, String name, boolean isDirectory)
  {
    return createScaledLabeledPathSelectionWidgetPanel(entryWidget, name, 0, 0, isDirectory);
  }

  private Pair<JPanel, JButton> createScaledLabeledPathSelectionWidgetPanel
  (JTextField entryWidget, String name, int labelHeight, int widgetHeight, boolean isDirectory)
  {
    JPanel panel = new JPanel();
    panel.setMinimumSize(new Dimension(0, scale(widgetHeight)));
    panel.setPreferredSize(new Dimension(-1, scale(widgetHeight)));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(widgetHeight)));
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(entryWidget);
    panel.add(Box.createHorizontalStrut(scale(5)));
    JButton pathButton = newNavAndControlButton(
      "new.png",
      e -> {
        File file = new File(entryWidget.getText());
        JFileChooser fileChooser = new JFileChooser(file);
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf", "PDF"));
        String existingPath = entryWidget.getText();
        if (existingPath != null && !existingPath.isEmpty()) {
          File existingFile = new File(existingPath);
          String existingDirectory = existingFile.isDirectory() ? existingPath : existingFile.getParent();
          fileChooser.setCurrentDirectory(new File(existingDirectory));
        } else if (basePath != null) {
          fileChooser.setCurrentDirectory(new File(basePath));
        }
        if (isDirectory) {
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        int fileChooserRC = fileChooser.showOpenDialog(BinderatorFrame.getInstance());
        if (fileChooserRC == JFileChooser.APPROVE_OPTION) {
          entryWidget.setText(fileChooser.getSelectedFile().getPath());
          entryWidget.postActionEvent();
        }
      }
    );
    panel.add(pathButton);
    return new Pair<>(createScaledLabeledWidgetPanel(panel, name, labelHeight, widgetHeight), pathButton);
  }

  private JPanel createScaledLabeledWidgetPanel
  (JComponent entryWidget, String name, int labelHeight, int widgetHeight)
  {
    // Top horizontal panel for the label, left justified:
    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
    labelPanel.add(Box.createHorizontalStrut(scale(5)));
    JLabel label = new JLabel(name);
    label.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(labelHeight)));
    labelPanel.add(Box.createHorizontalStrut(scale(5)));
    labelPanel.add(label);
    // Bottom horizontal panel for the widget, left justified:
    JPanel entryPanel = null;
    if (entryWidget != null) {
      entryPanel = new JPanel();
      entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.X_AXIS));
      entryWidget.setMinimumSize(new Dimension(0, scale(widgetHeight)));
      entryWidget.setPreferredSize(new Dimension(0, scale(widgetHeight)));
      entryWidget.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(widgetHeight)));
      entryPanel.add(Box.createHorizontalStrut(scale(5)));
      entryPanel.add(entryWidget);
      entryPanel.add(Box.createHorizontalGlue());
      entryPanel.add(Box.createHorizontalStrut(scale(5)));
    }
    // Combined Panel:
    JPanel combinedPanel = new JPanel();
    combinedPanel.setLayout(new BoxLayout(combinedPanel, BoxLayout.Y_AXIS));
    combinedPanel.add(labelPanel);
    if (entryWidget != null) {
      combinedPanel.add(entryPanel);
    }
    combinedPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(5 + (labelHeight + widgetHeight))));
    installContextMenu(combinedPanel);
    return combinedPanel;
  }

  protected ImageIcon createImageIcon
  (String fileName)
  {
    String path = "/icons/" + fileName;
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try (InputStream iconIn = getClass().getResourceAsStream(path)) {
      if (iconIn != null) {
        InputStream bufferedIconIn = new BufferedInputStream(iconIn);
        int i;
        while ((i = bufferedIconIn.read()) > -1) {
          byteOut.write(i);
        }
        return GUIUtils.scaleImageIcon(new ImageIcon(byteOut.toByteArray()));
      } else {
        System.err.println("Couldn't open icon resource at " + path);
        return null;
      }
    } catch (IOException e) {
      System.err.println("Couldn't read icon resource at " + path);
      return null;
    }
  }

  private JButton newNavAndControlButton
  (String iconFile, ActionListener listener)
  {
    JButton button = new JButton();
    ImageIcon icon = createImageIcon(iconFile);
    if (icon != null) {
      button.setIcon(icon);
    }
    button.addActionListener(listener);
    button.setMinimumSize(new Dimension(scale(22), scale(22)));
    button.setMaximumSize(new Dimension(scale(22), scale(22)));
    button.setPreferredSize(new Dimension(scale(22), scale(22)));
    return button;
  }

  public static BinderatorFrame getInstance
  ()
  {
    if (singletonInstance == null) {
      singletonInstance = new BinderatorFrame();
    }
    return singletonInstance;
  }

  private BinderatorFrame
  ()
  {
    setTitle(translate("windowTitle") + " " + VERSION);
    downIcon = createImageIcon("down.png");
    upIcon = createImageIcon("up.png");
    deleteIcon = createImageIcon("delete.png");
    newIcon = createImageIcon("new.png");
    setJMenuBar(buildMenuBar());
    // Main body panel:
    mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, scale(400)));
    mainPanel.setMinimumSize(new Dimension(scale(300), scale(200)));
    // Main tabbed pane:
    JTabbedPane mainTabs = new JTabbedPane();
    mainTabs.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    // Project panel:
    projectPanel = new JPanel();
    projectPanel.setLayout(new BoxLayout(projectPanel, BoxLayout.Y_AXIS));
    mainTabs.add(translate("project"), projectPanel);
    projectPanel.add(Box.createVerticalStrut(scale(5)));
    projectNameTextField = new JTextField(scale(34));
    projectNameTextField.setToolTipText(translate("projectNameTooltip"));
    projectNameTextField.addActionListener(event -> {
      try {
        getBook().setName(projectNameTextField.getText());
        registerUnsavedChange();
      } catch (Exception e) {
        errorDialog(e);
      }
    });
    GUIUtils.addBackgroundSetter(projectNameTextField);
    projectPanel.add(createScaledLabeledWidgetPanel(projectNameTextField, translate("name"), 22, 22));
    projectPanel.add(Box.createVerticalStrut(scale(5)));
    projectOutputPathTextField = new JTextField(scale(34));
    projectOutputPathTextField.addActionListener(event -> {
      try {
        getBook().setOutputPath(projectOutputPathTextField.getText());
        registerUnsavedChange();
      } catch (Exception e) {
        errorDialog(e);
      }
    });
    GUIUtils.addBackgroundSetter(projectOutputPathTextField);
    Pair<JPanel, JButton> projectOutputPanelAndButton = createScaledLabeledPathSelectionWidgetPanel(
        projectOutputPathTextField, translate("outputPath"), 22, 22, false
    );
    projectPanel.add(projectOutputPanelAndButton.getFirst());
    projectOutputPathTextField.setToolTipText(translate("projectOutputPathTooltip"));
    projectOutputPathButton = projectOutputPanelAndButton.getSecond();
    projectOutputPathButton.setToolTipText(translate("projectOutputPathButtonTooltip"));
    projectPanel.add(Box.createVerticalStrut(scale(5)));
    projectSignaturesOutputPathTextField = new JTextField(scale(34));
    projectSignaturesOutputPathTextField.addActionListener(event -> {
      try {
        getBook().setSignaturesOutputPath(projectSignaturesOutputPathTextField.getText());
        registerUnsavedChange();
      } catch (Exception e) {
        errorDialog(e);
      }
    });
    projectSignaturesOutputPathTextField.setToolTipText(translate("projectSignaturesOutputPathTooltip"));
    GUIUtils.addBackgroundSetter(projectSignaturesOutputPathTextField);
    Pair<JPanel, JButton> projectSignaturesPathAndButton = createScaledLabeledPathSelectionWidgetPanel(
      projectSignaturesOutputPathTextField, translate("signaturesOutputDirectory"), 22, 22, true
    );
    projectPanel.add(projectSignaturesPathAndButton.getFirst());
    projectSignaturesOutputPathButton = projectSignaturesPathAndButton.getSecond();
    projectSignaturesOutputPathButton.setToolTipText(translate("projectSignaturesOutputPathButtonTooltip"));
    projectScaleToFitCheckBox = new JCheckBox();
    projectScaleToFitCheckBox.setText(translate("initialScale"));
    projectScaleToFitCheckBox.addActionListener(e -> {
      execute(() -> {
        book.setScaleToFit(projectScaleToFitCheckBox.isSelected());
        registerUnsavedChange();
      });
    });
    projectScaleToFitCheckBox.setToolTipText(translate("projectScaleToFitTooltip"));
    projectEnableMarginsCheckBox = new JCheckBox();
    projectEnableMarginsCheckBox.setText(translate("margins"));
    projectEnableMarginsCheckBox.setToolTipText(translate("projectEnableMarginsTooltip"));
    projectEnableMarginsCheckBox.addActionListener(e -> {
      execute(() -> {
        book.setUsingMargins(projectEnableMarginsCheckBox.isSelected());
        registerUnsavedChange();
      });
    });
    projectEnablePageNumberingCheckBox = new JCheckBox();
    projectEnablePageNumberingCheckBox.setText(translate("pageNumbers"));
    projectEnablePageNumberingCheckBox.setToolTipText(translate("pageNumbersTooltip"));
    projectEnablePageNumberingCheckBox.addActionListener(e -> {
      execute(() -> {
        book.setUsingPageNumbering(projectEnablePageNumberingCheckBox.isSelected());
        registerUnsavedChange();
      });
    });
    JPanel projectCheckboxPanel = new JPanel();
    projectCheckboxPanel.setLayout(new BoxLayout(projectCheckboxPanel, BoxLayout.X_AXIS));
    projectCheckboxPanel.add(Box.createHorizontalStrut(scale(5)));
    projectCheckboxPanel.add(projectScaleToFitCheckBox);
    projectCheckboxPanel.add(Box.createHorizontalGlue());
    projectCheckboxPanel.add(projectEnableMarginsCheckBox);
    projectCheckboxPanel.add(Box.createHorizontalGlue());
    projectCheckboxPanel.add(projectEnablePageNumberingCheckBox);
    projectCheckboxPanel.add(Box.createHorizontalGlue());
    pageSizePairsComboBox = new JComboBox<>();
    pageSizePairsComboBox.setToolTipText(translate("pageSizeComboTooltip"));
    populatePageSizesComboBox();
    projectCheckboxPanel.add(new JLabel(translate("pageSignatureSizeColon")));
    projectCheckboxPanel.add(Box.createHorizontalStrut(scale(5)));
    projectCheckboxPanel.add(pageSizePairsComboBox);
    projectCheckboxPanel.add(Box.createHorizontalStrut(scale(5)));
    projectCheckboxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    projectPanel.add(Box.createVerticalStrut(scale(10)));
    projectPanel.add(projectCheckboxPanel);
    projectPanel.add(Box.createVerticalStrut(scale(10)));
    projectControlsPanel = new JPanel();
    projectControlsPanel.setLayout(new BoxLayout(projectControlsPanel, BoxLayout.Y_AXIS));
    projectPanel.add(projectControlsPanel);
    projectPanel.add(Box.createVerticalStrut(scale(5)));
    projectCommentTextArea = new JTextArea(scale(5), scale(34));
    projectCommentTextArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void changedUpdate(DocumentEvent e) {
        execute(() -> {
          book.setComments(projectCommentTextArea.getText());
          registerUnsavedChange();
        });
      }
      @Override public void removeUpdate(DocumentEvent e) {
        execute(() -> {
          book.setComments(projectCommentTextArea.getText());
          registerUnsavedChange();
        });
      }
      @Override public void insertUpdate(DocumentEvent e) {
        execute(() -> {
          book.setComments(projectCommentTextArea.getText());
          registerUnsavedChange();
        });
      }
    });
    projectCommentTextArea.setLineWrap(true);
    projectCommentTextArea.setWrapStyleWord(true);
    JScrollPane projectCommentScrollPane = new JScrollPane(projectCommentTextArea);
    projectPanel.add(createScaledLabeledWidgetPanel(projectCommentScrollPane, translate("comments"), 22, 70));
    projectCommentTextArea.setToolTipText(translate("projectCommentsTooltip"));
    updateProjectControlsPanel();
    projectPanel.add(Box.createVerticalGlue());
    // Documents panel:
    documentsPanel = new JPanel();
    JScrollPane documentsPane = new JScrollPane(documentsPanel);
    documentsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    documentsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    // Pages panel below documents panel:
    pagesPanel = new JPanel();
    pagesPanel.setLayout(new BoxLayout(pagesPanel, BoxLayout.X_AXIS));
    pageRangesTextArea.setLineWrap(true);
    pageRangesTextArea.setWrapStyleWord(true);
    JScrollPane pagesRangesScrollPane = new JScrollPane(pageRangesTextArea);
    pagesRangesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pagesRangesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    pagesPanel.add(createScaledLabeledWidgetPanel(pagesRangesScrollPane, translate("pageRanges"), 22, 94));
    pageRangesTextArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void changedUpdate(DocumentEvent e) {
        handlePageRangesChange(pageRangesTextArea.getText());
      }
      @Override public void removeUpdate(DocumentEvent e) {
        handlePageRangesChange(pageRangesTextArea.getText());
      }
      @Override public void insertUpdate(DocumentEvent e) {
        handlePageRangesChange(pageRangesTextArea.getText());
      }
    });
    pageRangesTextArea.setToolTipText(translate("pageRangesTooltip"));

    // Combined documents and pages panel:
    documentsAndPagesPanel = new JPanel();
    documentsAndPagesPanel.setLayout(new BoxLayout(documentsAndPagesPanel, BoxLayout.Y_AXIS));
    documentsAndPagesPanel.add(documentsPane);
    JSeparator documentsAndPagesSeparator = new JSeparator(JSeparator.HORIZONTAL);
    documentsAndPagesSeparator.setForeground(Color.BLACK);
    documentsAndPagesPanel.add(documentsAndPagesSeparator);
    documentsAndPagesPanel.add(pagesPanel);
    mainTabs.add(translate("sourceDocuments"), documentsAndPagesPanel);
    documentsPanel.setLayout(new BoxLayout(documentsPanel, BoxLayout.Y_AXIS));
    JPanel documentsNavPanel = new JPanel();
    documentsNavPanel.setLayout(new BoxLayout(documentsNavPanel, BoxLayout.X_AXIS));
    documentsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    sourceDocumentsComboBox = new JComboBox<>();
    sourceDocumentsComboBox.setToolTipText(translate("sourceDocumentsComboTooltip"));
    populateSourceDocumentsComboBox();
    documentsNavPanel.add(sourceDocumentsComboBox);
    sourceDocumentsComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    sourceDocumentsComboBox.setPreferredSize(new Dimension(scale(202), scale(22)));
    documentsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton newDocumentButton = newNavAndControlButton(
      "new.png",
      e -> newSourceDocument()
    );
    newDocumentButton.setToolTipText(translate("sourceDocumentNewButtonTooltip"));
    documentsNavPanel.add(newDocumentButton);
    documentsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton downDocumentButton = newNavAndControlButton(
      "down.png",
      e -> {
        if (sourceDocumentsComboBox.getSelectedItem() != null) {
          downSourceDocument((SourceDocument)sourceDocumentsComboBox.getSelectedItem());
        }
      }
    );
    downDocumentButton.setToolTipText(translate("sourceDocumentDownButtonTooltip"));
    documentsNavPanel.add(downDocumentButton);
    documentsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton upDocumentButton = newNavAndControlButton(
      "up.png",
      e -> {
        if (sourceDocumentsComboBox.getSelectedItem() != null) {
          upSourceDocument((SourceDocument)sourceDocumentsComboBox.getSelectedItem());
        }
      }
    );
    upDocumentButton.setToolTipText(translate("sourceDocumentUpButtonTooltip"));
    documentsNavPanel.add(upDocumentButton);
    documentsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton deleteSourceDocumentButton = newNavAndControlButton(
      "delete.png",
      e -> {
        if (sourceDocumentsComboBox.getSelectedItem() != null) {
          deleteSourceDocument((SourceDocument)sourceDocumentsComboBox.getSelectedItem());
        }
      }
    );
    deleteSourceDocumentButton.setToolTipText(translate("sourceDocumentDeleteButtonTooltip"));
    documentsNavPanel.add(deleteSourceDocumentButton);
    documentsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    documentsNavPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    documentsNavPanel.setVisible(true);
    documentsPanel.add(Box.createVerticalStrut(5));
    documentsPanel.add(documentsNavPanel);
    documentsPanel.add(Box.createVerticalStrut(5));
    documentsPanel.add(createScaledLabeledWidgetPanel(documentNameTextField, translate("name"), 22, 22));
    documentNameTextField.addActionListener(event -> {
      if (selectedDocument != null) {
        try {
          selectedDocument.setName(documentNameTextField.getText());
          registerUnsavedChange();
        } catch (Exception e) {
          errorDialog(e);
        }
      }
    });
    documentNameTextField.setToolTipText(translate("sourceDocumentNameTooltip"));
    GUIUtils.addBackgroundSetter(documentNameTextField);
    documentsPanel.add(Box.createVerticalStrut(scale(5)));
    documentsPanel.add(createScaledLabeledWidgetPanel(documentIdentifierTextField, translate("documentId"), 22, 22));
    documentIdentifierTextField.addActionListener(event -> {
      if (selectedDocument != null) {
        try {
          selectedDocument.setStringId(documentIdentifierTextField.getText());
          registerUnsavedChange();
        } catch (Exception e) {
          errorDialog(e);
        }
      }
    });
    documentIdentifierTextField.setToolTipText(translate("sourceDocumentIdTooltip"));
    documentIdentifierTextField.setMaximumSize(new Dimension(scale(130), scale(22)));
    GUIUtils.addBackgroundSetter(documentIdentifierTextField);
    documentsPanel.add(Box.createVerticalStrut(scale(5)));
    Pair<JPanel, JButton> documentPathPanelAndButton = createScaledLabeledPathSelectionWidgetPanel(
      documentPathTextField, translate("path"), 22, 22, false
    );
    documentPathTextField.setToolTipText(translate("sourceDocumentPathTooltip"));
    documentsPanel.add(documentPathPanelAndButton.getFirst());
    documentPathButton = documentPathPanelAndButton.getSecond();
    documentPathButton.setToolTipText(translate("sourceDocumentPathButtonTooltip"));
    documentPathTextField.addActionListener(event -> {
      if (selectedDocument != null) {
        try {
          selectedDocument.setPath(documentPathTextField.getText());
          registerUnsavedChange();
        } catch (Exception e) {
          errorDialog(e);
        }
      }
    });
    GUIUtils.addBackgroundSetter(documentPathTextField);
    documentsPanel.add(Box.createVerticalStrut(scale(5)));
    documentCommentTextArea.setLineWrap(true);
    documentCommentTextArea.setWrapStyleWord(true);
    JScrollPane documentCommentScrollPane = new JScrollPane(documentCommentTextArea);
    documentsPanel.add(createScaledLabeledWidgetPanel(documentCommentScrollPane, translate("comments"), 22, 70));
    documentCommentTextArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void changedUpdate(DocumentEvent e) {
        selectedDocument.setComment(documentCommentTextArea.getText());
        registerUnsavedChange();
      }
      @Override public void removeUpdate(DocumentEvent e) {
        selectedDocument.setComment(documentCommentTextArea.getText());
        registerUnsavedChange();
      }
      @Override public void insertUpdate(DocumentEvent e) {
        selectedDocument.setComment(documentCommentTextArea.getText());
        registerUnsavedChange();
      }
    });
    documentCommentTextArea.setToolTipText(translate("sourceDocumentCommentTooltip"));
    documentsPanel.add(Box.createGlue());
    documentsPanel.add(Box.createVerticalStrut(scale(5)));
    documentsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setEnabledSourceDocumentsWidgets(false);
    // Transforms panel:
    transformSetsPanel = new JPanel();
    JScrollPane transformSetsPane = new JScrollPane(transformSetsPanel);
    transformSetsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    transformSetsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    mainTabs.add(translate("transforms"), transformSetsPane);
    transformSetsPanel.setLayout(new BoxLayout(transformSetsPanel, BoxLayout.Y_AXIS));
    JPanel transformSetsNavPanel = new JPanel();
    transformSetsNavPanel.setLayout(new BoxLayout(transformSetsNavPanel, BoxLayout.X_AXIS));
    transformSetsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    transformSetsComboBox = new JComboBox<>();
    populateTransformSetComboBox();
    transformSetsNavPanel.add(transformSetsComboBox);
    transformSetsComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    transformSetsComboBox.setToolTipText(translate("transformSetsComboTooltip"));
    transformSetsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton newTransformSetButton = newNavAndControlButton("new.png", e -> newTransformSet());
    newTransformSetButton.setToolTipText(translate("transformSetsNewTooltip"));
    transformSetsNavPanel.add(newTransformSetButton);
    transformSetsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton downTransformSetButton = newNavAndControlButton(
      "down.png",
      e -> {
        if (transformSetsComboBox.getSelectedItem() != null) {
          downTransformSet((TransformSet)transformSetsComboBox.getSelectedItem());
        }
      }
    );
    transformSetsNavPanel.add(downTransformSetButton);
    downTransformSetButton.setToolTipText(translate("transformSetsDownTooltip"));
    transformSetsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton upTransformSetButton = newNavAndControlButton(
      "up.png",
      e -> {
        if (transformSetsComboBox.getSelectedItem() != null) {
          upTransformSet((TransformSet)transformSetsComboBox.getSelectedItem());
        }
      }
    );
    transformSetsNavPanel.add(upTransformSetButton);
    upTransformSetButton.setToolTipText(translate("transformSetsUpTooltip"));
    transformSetsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton deleteTransformSetButton = newNavAndControlButton(
      "delete.png",
      e -> {
        if (transformSetsComboBox.getSelectedItem() != null) {
          deleteTransformSet((TransformSet) transformSetsComboBox.getSelectedItem());
        }
      }
    );
    transformSetsNavPanel.add(deleteTransformSetButton);
    deleteTransformSetButton.setToolTipText(translate("transformSetsDeleteTooltip"));
    transformSetsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    transformSetsNavPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    transformSetsNavPanel.setVisible(true);
    transformSetsPanel.add(Box.createVerticalStrut(5));
    transformSetsPanel.add(transformSetsNavPanel);
    transformSetsPanel.add(Box.createVerticalStrut(5));
    transformSetsPanel.add(createScaledLabeledWidgetPanel(transformSetNameTextField, translate("name"), 22, 22));
    transformSetNameTextField.addActionListener(event -> {
      if ( selectedTransformSet != null) {
        try {
          selectedTransformSet.setName(transformSetNameTextField.getText());
          registerUnsavedChange();
        } catch (Exception e) {
          errorDialog(e);
        }
      }
    });
    transformSetNameTextField.setToolTipText(translate("transformSetNameTooltip"));
    GUIUtils.addBackgroundSetter(transformSetNameTextField);
    transformSetsPanel.add(createScaledLabeledWidgetPanel(
      transformSetPageRangesTextField, translate("transformSetPageRanges"), 22, 22
    ));
    transformSetPageRangesTextField.addActionListener(event -> {
      if ( selectedTransformSet != null) {
        try {
          selectedTransformSet.setPageRanges(PageRange.parsePageRanges(transformSetPageRangesTextField.getText(), true));
          registerUnsavedChange();
        } catch (Exception e) {
          errorDialog(e);
        }
      }
    });
    transformSetPageRangesTextField.setToolTipText(translate("transformSetPageRangesTooltip"));
    GUIUtils.addBackgroundSetter(transformSetPageRangesTextField);
    transformSetCommentTextArea.setLineWrap(true);
    transformSetCommentTextArea.setWrapStyleWord(true);
    transformSetCommentTextArea.setToolTipText(translate("transformSetCommentsTooltip"));
    JScrollPane transformSetCommentScrollPane = new JScrollPane(transformSetCommentTextArea);
    transformSetsPanel.add(
      createScaledLabeledWidgetPanel(transformSetCommentScrollPane, translate("comments"), 22, 70)
    );
    transformSetCommentTextArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void changedUpdate(DocumentEvent e) {
        selectedTransformSet.setComment(transformSetCommentTextArea.getText());
        registerUnsavedChange();
      }
      @Override public void removeUpdate(DocumentEvent e) {
        selectedTransformSet.setComment(transformSetCommentTextArea.getText());
        registerUnsavedChange();
      }
      @Override public void insertUpdate(DocumentEvent e) {
        selectedTransformSet.setComment(transformSetCommentTextArea.getText());
        registerUnsavedChange();
      }
    });
    transformsPanel = new JPanel();
    transformsPanel.setLayout(new BoxLayout(transformsPanel, BoxLayout.Y_AXIS));
    transformsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    transformSetsPanel.add(Box.createVerticalStrut(scale(5)));
    transformSetsPanel.add(transformsPanel);
    transformSetsPanel.add(Box.createVerticalGlue());
    transformSetsPanel.add(Box.createVerticalStrut(scale(5)));
    transformSetsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setEnabledTransformsWidgets(false);
    mainPanel.add(mainTabs);
    mainPanel.setVisible(true);
    // frameContainer.add(mainPanel);
    // Bottom button panel:
    buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setMinimumSize(new Dimension(scale(-1), scale(58)));
    buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(58)));
    buttonPanel.setVisible(true);
    mainPanel.add(Box.createGlue());
    mainPanel.add(buttonPanel);
    generateButton = new JButton(translate("generate"));
    generateButton.setActionCommand(ACTION_GENERATE);
    generateButton.addActionListener(this);
    generateButton.setToolTipText(translate("buttonTooltipGenerate"));
    buttonPanel.add(Box.createHorizontalStrut(scale(5)));
    buttonPanel.add(generateButton);
    generateSignaturesButton = new JButton(translate("generateSignatures"));
    generateSignaturesButton.setActionCommand(ACTION_GENERATE_SIGNATURES);
    generateSignaturesButton.addActionListener(this);
    generateSignaturesButton.setToolTipText(translate("buttonTooltipGenerateSignatures"));
    buttonPanel.add(Box.createHorizontalStrut(scale(10)));
    buttonPanel.add(generateSignaturesButton);
    viewerButton = new JButton(translate("viewer"));
    viewerButton.setActionCommand(ACTION_SHOW_HIDE_VIEWER);
    viewerButton.addActionListener(this);
    viewerButton.setToolTipText(translate("buttonTooltipViewer"));
    buttonPanel.add(Box.createHorizontalStrut(scale(10)));
    buttonPanel.add(viewerButton);
    exitButton = new JButton(translate("exit"));
    exitButton.setActionCommand(ACTION_EXIT);
    exitButton.addActionListener(this);
    exitButton.setToolTipText(translate("buttonTooltipExit"));
    buttonPanel.add(Box.createGlue());
    buttonPanel.add(exitButton);
    buttonPanel.add(Box.createHorizontalStrut(scale(5)));
    // Bottom button panel:
    statusPanel = new JPanel();
    statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
    statusPanel.setMinimumSize(new Dimension(scale(-1), scale(24)));
    statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(24)));
    statusPanel.setBorder(new BorderUIResource.LineBorderUIResource(Color.LIGHT_GRAY, 2));
    statusProgressBarFullPanel = new JPanel();
    statusProgressBarFullPanel.setLayout(new BoxLayout(statusProgressBarFullPanel, BoxLayout.X_AXIS));
    statusProgressBarLabelPanel = new JPanel();
    statusProgressBarLabelPanel.setLayout(new BoxLayout(statusProgressBarLabelPanel, BoxLayout.X_AXIS));
    statusProgressBarFullPanel.add(statusProgressBarLabelPanel);
    statusProgressBarPanel = new JPanel();
    statusProgressBarPanel.setLayout(new BoxLayout(statusProgressBarPanel, BoxLayout.X_AXIS));
    statusProgressBar = new JProgressBar();
    statusProgressBar.setMinimum(0);
    statusProgressBar.setMaximum(100);
    statusProgressBar.setStringPainted(true);
    statusProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(20)));
    statusProgressBar.setPreferredSize(new Dimension(scale(200), scale(20)));
    // statusProgressBar.setIndeterminate(true);
    statusProgressBar.setBackground(Color.green);
    statusProgressBarPanel.add(statusProgressBar);
    statusProgressBarFullPanel.add(statusProgressBarPanel);
    statusStringFullPanel = new JPanel();
    statusStringFullPanel.setLayout(new BoxLayout(statusStringFullPanel, BoxLayout.X_AXIS));
    statusStringPanel = new JPanel();
    statusStringPanel.setLayout(new BoxLayout(statusStringPanel, BoxLayout.X_AXIS));
    statusStringFullPanel.add(statusStringPanel);
    statusStringFullPanel.add(Box.createHorizontalGlue());
    statusPanel.setVisible(true);
    mainPanel.add(statusPanel);
    setContentPane(mainPanel);
    addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing
      (WindowEvent e)
      {
        gracefulExit();
      }

    });
    setMinimumSize(new Dimension(scale(610), scale(510)));
    setPreferredSize(new Dimension(scale(610), scale(510)));
    setBackground(new Color(230, 255, 230));
    setStatusMessage(translate("welcomeToSonOfBinderator"));
  }

  private void setEnabledSourceDocumentsWidgets
  (boolean enabled)
  {
    documentNameTextField.setEnabled(enabled);
    documentIdentifierTextField.setEnabled(enabled);
    documentPathTextField.setEnabled(enabled);
    documentPathButton.setEnabled(enabled);
    documentCommentTextArea.setEnabled(enabled);
  }

  private void setEnabledTransformsWidgets
  (boolean enabled)
  {
    transformSetNameTextField.setEnabled(enabled);
    transformSetPageRangesTextField.setEnabled(enabled);
    transformSetCommentTextArea.setEnabled(enabled);
  }

  private void setStatusMessage
  (String message)
  {
    statusPanel.setVisible(false);
    statusPanel.removeAll();
    statusStringPanel.removeAll();
    statusStringPanel.add(new JLabel(message));
    statusPanel.add(statusStringFullPanel);
    statusPanel.setVisible(true);
  }

  private void setStatusProgressLabel
  (String label)
  {
    if (showProgressBars) {
      statusProgressBar.setVisible(false);
      statusProgressBarPanel.setVisible(false);
      statusProgressBarFullPanel.setVisible(false);
      statusPanel.setVisible(false);
      statusPanel.removeAll();
      statusProgressBarLabelPanel.removeAll();
      statusProgressBarLabelPanel.add(new JLabel(label));
      statusProgressBarLabelPanel.add(Box.createHorizontalStrut(scale(10)));
      statusPanel.add(statusProgressBarFullPanel);
      statusProgressBar.setVisible(true);
      statusProgressBarPanel.setVisible(true);
      statusProgressBarFullPanel.setVisible(true);
      statusPanel.setVisible(true);
    }
  }

  private void setStatusProgress
  (float progress, float maxProgress)
  {
    if (showProgressBars) {
      float progressRatio = progress / maxProgress;
      progressRatio = Math.min(progressRatio, 1.0f);
      progressRatio = Math.max(progressRatio, 0.0f);
      int progressPercent = (int) (100.0 * progressRatio);
      statusProgressBar.setValue(progressPercent);
    }
  }

  private void installContextMenu
  (Container comp)
  {
    for (Component component : comp.getComponents()) {
      if (component instanceof JTextComponent) {
        component.addMouseListener(new MouseAdapter() {
          public void mousePressed(final MouseEvent event) {
            if (event.isPopupTrigger()) {
              final JTextComponent component = (JTextComponent)event.getComponent();
              final JPopupMenu menu = new JPopupMenu();
              JMenuItem item;
              item = new JMenuItem(new DefaultEditorKit.CopyAction());
              item.setText(translate("copy"));
              item.setEnabled(component.getSelectionStart() != component.getSelectionEnd());
              menu.add(item);
              item = new JMenuItem(new DefaultEditorKit.CutAction());
              item.setText(translate("cut"));
              item.setEnabled(component.isEditable() && component.getSelectionStart() != component.getSelectionEnd());
              menu.add(item);
              item = new JMenuItem(new DefaultEditorKit.PasteAction());
              item.setText(translate("paste"));
              item.setEnabled(component.isEditable());
              menu.add(item);
              menu.show(event.getComponent(), event.getX(), event.getY());
            }
          }
        });
      } else if (component instanceof Container)
        installContextMenu((Container) component);
    }
  }

  private void handlePageRangesChange
  (String pageRangesText)
  {
    execute(() -> {
      String errorMessage = null;
      try {
        getBook().setPageRangesSource(pageRangesText);
        // Pre-parse and colour the background to the page ranges text area red, every time we encounter an error,
        // and white whenver it's good.  This will avoid an error dialog popping up from the rendering thread when
        // the viewer is open:
        getBook().getPages();
        registerUnsavedChange();
      } catch (Throwable t) {
        errorMessage = t.getMessage();
        t.printStackTrace(System.err);
      } finally {
        if (errorMessage != null) {
          pageRangesTextArea.setBackground(new Color(255, 230, 230));
          setStatusMessage(errorMessage);
        } else {
          pageRangesTextArea.setBackground(Color.WHITE);
          setStatusMessage("Page range text is valid");
          registerUnsavedChange();
        }
      }
    });
  }

  void errorDialog
  (Throwable t)
  {
    JOptionPane.showMessageDialog(this, (t.getMessage()));
    t.printStackTrace(System.err);
  }

  private void newSourceDocument
  ()
  {
    Book book = getBook();
    SourceDocument sourceDocument = new SourceDocument("");
    sourceDocument.setName(translate("new"));
    execute(() -> {
      try {
        book.addSourceDocument(sourceDocument);
        registerUnsavedChange();
      } catch (Throwable t) {
        book.getSourceDocuments().remove(sourceDocument);
        errorDialog(t);
        return;
      }
      populateSourceDocumentsComboBox();
      setEnabledSourceDocumentsWidgets(true);
      sourceDocumentsComboBox.setSelectedItem(sourceDocument);
      selectedDocument = sourceDocument;
      populateSourceDocumentWidgets();
    });
  }

  private void upSourceDocument
  (SourceDocument document)
  {
    execute(() -> {
      getBook().upSourceDocument(document);
      registerUnsavedChange();
      populateSourceDocumentsComboBox();
      sourceDocumentsComboBox.setSelectedItem(document);
      selectedDocument = document;
      populateSourceDocumentWidgets();
    });
  }

  private void downSourceDocument
  (SourceDocument document)
  {
    execute(() -> {
      getBook().downSourceDocument(document);
      registerUnsavedChange();
      populateSourceDocumentsComboBox();
      sourceDocumentsComboBox.setSelectedItem(document);
      selectedDocument = document;
      populateSourceDocumentWidgets();
    });
  }

  private void deleteSourceDocument
  (SourceDocument document)
  {
    execute(() -> {
      getBook().removeSourceDocument(document);
      registerUnsavedChange();
      populateSourceDocumentsComboBox();
      if (sourceDocumentsComboBox.getItemCount() > 0) {
        sourceDocumentsComboBox.setSelectedItem(0);
        selectedDocument = (SourceDocument) sourceDocumentsComboBox.getSelectedItem();
        populateSourceDocumentWidgets();
      } else {
        selectedDocument = null;
        populateSourceDocumentWidgets();
        setEnabledSourceDocumentsWidgets(false);
      }
    });
  }

  private Book getBook
  ()
  {
    if (book == null) {
      book = new Book();
    }
    return book;
  }

  private void newTransformSet
  ()
  {
    execute(() -> {
      bookLock.lock();
      try {
        Book book = getBook();
        TransformSet transformSet = new TransformSet();
        transformSet.setName(translate("new"));
        book.getTransformSets().add(transformSet);
        registerUnsavedChange();
        populateTransformSetComboBox();
        setEnabledTransformsWidgets(true);
        transformSetsComboBox.setSelectedItem(transformSet);
        selectedTransformSet = transformSet;
        populateTransformSetWidgets();
        updateTransformControls(transformSet);
      } finally {
        bookLock.unlock();
      }
    });
  }

  private void upTransformSet
  (TransformSet transformSet)
  {
    execute(() -> {
      getBook().upTransformSet(transformSet);
      registerUnsavedChange();
      populateTransformSetComboBox();
      transformSetsComboBox.setSelectedItem(transformSet);
      selectedTransformSet = transformSet;
      updateTransformControls(transformSet);
    });
  }

  private void downTransformSet
  (TransformSet transformSet)
  {
    execute(() -> {
      getBook().downTransformSet(transformSet);
      registerUnsavedChange();
      populateTransformSetComboBox();
      transformSetsComboBox.setSelectedItem(transformSet);
      selectedTransformSet = transformSet;
      updateTransformControls(transformSet);
    });
  }

  private void deleteTransformSet
  (TransformSet transformSet)
  {
    execute(() -> {
      getBook().removeTransformSet(transformSet);
      registerUnsavedChange();
      populateTransformSetComboBox();
      if (transformSetsComboBox.getItemCount() > 0) {
        transformSetsComboBox.setSelectedItem(0);
        selectedTransformSet = (TransformSet) transformSetsComboBox.getSelectedItem();
        populateTransformSetWidgets();
        updateTransformControls(selectedTransformSet);
      } else {
        selectedTransformSet = null;
        setEnabledTransformsWidgets(false);
        populateTransformSetWidgets();
        updateTransformControls(null);
      }
    });
  }

  private JMenuBar buildMenuBar
  ()
  {
    JMenuBar menuBar = new JMenuBar();
    // File menu:
    JMenu fileMenu = new JMenu(translate("file"));
    fileMenu.setMnemonic(KeyEvent.VK_F);
    fileMenu.getAccessibleContext().setAccessibleDescription(translate("fileMenu"));
    menuBar.add(fileMenu);
    // File->New menu item:
    JMenuItem fileNewMenuItem = new JMenuItem(translate("New"), KeyEvent.VK_S);
    fileNewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
    fileNewMenuItem.getAccessibleContext().setAccessibleDescription(translate("newProject"));
    fileMenu.add(fileNewMenuItem);
    fileNewMenuItem.addActionListener(e -> fileNew());
    // File->Open menu item:
    JMenuItem fileOpenMenuItem = new JMenuItem(translate("open"), KeyEvent.VK_O);
    fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK));
    fileOpenMenuItem.getAccessibleContext().setAccessibleDescription(translate("openProject"));
    fileMenu.add(fileOpenMenuItem);
    fileOpenMenuItem.addActionListener(e -> fileOpen());
    // File->Save menu item:
    JMenuItem fileSaveMenuItem = new JMenuItem(translate("save"), KeyEvent.VK_S);
    fileSaveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
    fileSaveMenuItem.getAccessibleContext().setAccessibleDescription(translate("saveProject"));
    fileMenu.add(fileSaveMenuItem);
    fileSaveMenuItem.addActionListener(e -> fileSave());
    // File->SaveAs menu item:
    JMenuItem fileSaveAsMenuItem = new JMenuItem(translate("saveAs"), KeyEvent.VK_A);
    fileSaveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK));
    fileSaveAsMenuItem.getAccessibleContext().setAccessibleDescription(translate("saveProjectAs"));
    fileMenu.add(fileSaveAsMenuItem);
    fileSaveAsMenuItem.addActionListener(e -> fileSaveAs());
    // File->Exit menu item:
    JMenuItem fileExitMenuItem = new JMenuItem(translate("exit"), KeyEvent.VK_E);
    fileExitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK));
    fileExitMenuItem.getAccessibleContext().setAccessibleDescription(translate("exit"));
    fileExitMenuItem.addActionListener(e -> gracefulExit());
    fileMenu.add(fileExitMenuItem);
    // App menu:
    JMenu appMenu = new JMenu(translate("app"));
    appMenu.getAccessibleContext().setAccessibleDescription(translate("appMenu"));
    menuBar.add(appMenu);
    // App->Options menu item:
    JMenuItem optionsMenuItem = new JMenuItem(translate("options"), KeyEvent.VK_O);
    optionsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK));
    optionsMenuItem.getAccessibleContext().setAccessibleDescription(translate("options"));
    optionsMenuItem.addActionListener(e -> options());
    appMenu.add(optionsMenuItem);
    // Help menu:
    JMenu helpMenu = new JMenu(translate("help"));
    fileMenu.getAccessibleContext().setAccessibleDescription(translate("helpMenu"));
    menuBar.add(Box.createGlue());
    menuBar.add(helpMenu);
    // Help->Inline Help menu item:
    JMenuItem inlineHelpMenuItem = new JMenuItem(translate("inlineHelp"), KeyEvent.VK_H);
    inlineHelpMenuItem.getAccessibleContext().setAccessibleDescription(translate("inlineHelp"));
    inlineHelpMenuItem.addActionListener(e -> inlineHelp());
    helpMenu.add(inlineHelpMenuItem);
    // Help->About menu item:
    JMenuItem aboutSonOfBinderatorMenuItem = new JMenuItem(translate("aboutSOB"), KeyEvent.VK_S);
    aboutSonOfBinderatorMenuItem.getAccessibleContext().setAccessibleDescription(translate("aboutSOB"));
    helpMenu.add(aboutSonOfBinderatorMenuItem);
    aboutSonOfBinderatorMenuItem.addActionListener(e -> about());
    return menuBar;
  }
  
  private void fileOpen
  ()
  {
    if (checkForUnsavedChanges()) return;
    try {
      File chosenFile = new File(projectPath != null ? projectPath : "");
      JFileChooser fileChooser = new JFileChooser(chosenFile);
      if (basePath != null) {
        fileChooser.setCurrentDirectory(new File(basePath));
      }
      fileChooser.setFileFilter(bdrFilter);
      int fileChooserRC = fileChooser.showOpenDialog(this);
      if (fileChooserRC == JFileChooser.APPROVE_OPTION) {
        chosenFile = fileChooser.getSelectedFile();
        if (chosenFile != null) {
          if (!chosenFile.exists()) {
            throw new IOException(translate("chosenFile") + " \"" + chosenFile + "\" " + translate("doesNotExist"));
          }
          if (!chosenFile.canRead()) {
            throw new IOException(translate("chosenFileAtPath") + " \"" + chosenFile + "\" " + translate("isNotReadable"));
          }
          projectPath = chosenFile.getPath();
          setBook(getStore().loadBook(projectPath));
          basePath = chosenFile.getParent();
          InitFile.instance().set("projectPath", projectPath);
          setStatusMessage(translate("project") + " " + getBook().getName() + " " + translate("loadedSuccessfully"));
        }
      }
    } catch (Exception e) {
      errorDialog(e);
    }
  }

  // Returns true if operation can continue, false if the user chose cancel on unsaved changes.
  private boolean checkForUnsavedChanges
  ()
  {
    if (haveUnsavedChanges) {
      int result = JOptionPane.showConfirmDialog(
        this,
        translate("youHaveUnsavedChangesEtc"),
        translate("unsavedChanges"), JOptionPane.YES_NO_CANCEL_OPTION
      );
      if (result == JOptionPane.YES_OPTION) {
        fileSave();
      } else {
        return result == JOptionPane.CANCEL_OPTION;
      }
    }
    return false;
  }

  private BinderatorStore getStore
  ()
  {
    return new SerialBinderatorStore();
  }

  private void fileNew
  ()
  {
    setBook(new Book());
    projectPath = null;
  }

  private void fileSave
  ()
  {
    fileSave(false);
  }

  private void fileSave
  (boolean emptySchema)
  {
    Book book = getBook();
    if (projectPath != null) {
      try {
        getStore().saveBook(book, projectPath, emptySchema);
        InitFile.instance().set("projectPath", projectPath);
        haveUnsavedChanges = false;
        setStatusMessage(translate("project") + " " + book.getName() + " " + translate("successfullySaved"));
      } catch (Exception e) {
        errorDialog(e);
      }
    } else {
      fileSaveAs();
    }
  }

  private void fileSaveAs
  ()
  {
    try {
      File chosenFile = new File(projectPath != null ? projectPath : "");
      JFileChooser fileChooser = new JFileChooser(chosenFile);
      if (basePath != null) {
        fileChooser.setCurrentDirectory(new File(basePath));
      }
      fileChooser.setFileFilter(bdrFilter);
      int fileChooserRC = fileChooser.showOpenDialog(this);
      if (fileChooserRC == JFileChooser.APPROVE_OPTION) {
        chosenFile = fileChooser.getSelectedFile();
        if (!chosenFile.getPath().endsWith(".bdr")) {
          chosenFile = new File(chosenFile.getPath() + ".bdr");
        }
        if (chosenFile.exists()) {
          if (!chosenFile.delete()) {
            throw new IOException(translate("couldNotOverwrite") + " \"" + chosenFile + "\"");
          }
        }
        if (!chosenFile.createNewFile()) {
          throw new IOException(translate("couldNotCreate") + " \"" + chosenFile + "\"");
        }
        if (!chosenFile.canWrite()) {
          throw new IOException(translate("chosenFileAtPath") + " \"" + chosenFile + "\" " + translate("isNotWriteable"));
        }
        projectPath = chosenFile.getPath();
        basePath = chosenFile.getParent();
        fileSave(true);
      }
      // BinderatorDB.instance().saveProject(book, projectPathTextField.getText());
    } catch (Exception e) {
      errorDialog(e);
    }
  }

  private void about
  ()
  {
    String message = translate("aboutContent");
    message = message.replace("#VERSION#", VERSION);
    JDialog aboutDialog = new JDialog(this, translate("aboutSOB"), true);
    JPanel aboutPanel = (JPanel)aboutDialog.getContentPane();
    aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
    JButton okButton = new JButton(translate("ok"));
    okButton.addActionListener(e -> {
      aboutDialog.setVisible(false);
    });
    aboutPanel.add(Box.createVerticalStrut(scale(10)));
    JLabel messageLabel = new JLabel(message);
    messageLabel.setOpaque(true);
    JPanel messagePanel = new JPanel();
    messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
    messagePanel.add(Box.createHorizontalStrut(scale(10)));
    messagePanel.add(messageLabel);
    messagePanel.add(Box.createHorizontalStrut(scale(10)));
    messagePanel.add(Box.createHorizontalGlue());
    aboutPanel.add(messagePanel);
    aboutPanel.add(Box.createVerticalStrut(scale(10)));
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(okButton);
    buttonPanel.add(Box.createHorizontalGlue());
    aboutPanel.add(Box.createVerticalGlue());
    aboutPanel.add(buttonPanel);
    aboutPanel.add(Box.createVerticalStrut(scale(10)));
    aboutDialog.setSize(new Dimension(scale(310), scale(400)));
    aboutDialog.setPreferredSize(new Dimension(scale(310), scale(400)));
    aboutDialog.setMinimumSize(new Dimension(scale(310), scale(400)));
    aboutDialog.setLocationRelativeTo(this);
    aboutDialog.setVisible(true);
  }

  private void options
  ()
  {
    if (optionsDialog == null) {
      optionsDialog = new OptionsDialog(this) {

        @Serial
        private static final long serialVersionUID = -8031914324321766285L;

        @Override
        protected void handleException(Throwable t) {
          errorDialog(t);
        }
      };

    }
    optionsDialog.setVisible(true);
  }

  private void inlineHelp
  ()
  {
    if (inlineHelpDialog == null) {
      try {
        inlineHelpDialog = InlineHelp.instance();
      } catch (Throwable t) {
        errorDialog(t);
      }
    }
    inlineHelpDialog.setVisible(true);
  }

  private void gracefulExit
  ()
  {
    if (checkForUnsavedChanges()) return;
    try {
      InitFile.instance().save();
    } catch (Exception ignored) {};
    System.exit(0);
  }

  private JTextField createRangedFloatField
  (RangedFloat rangedFloat)
  {
    JTextField field = new JTextField();
    GUIUtils.addBackgroundSetter(field);
    field.addKeyListener(new KeyAdapter() {

      @Override
      public void keyReleased(KeyEvent ke) {
        String typed = field.getText();
        if (!typed.matches("-?[\\d]+(\\.[\\d]+)?") || typed.length() > 5) {
          return;
        }
        float value = Float.parseFloat(typed);
        rangedFloat.setValue(value);
      }

    });
    field.setText("" + rangedFloat.getValue());
    field.setMaximumSize(new Dimension(scale(100), scale(22)));
    return field;
  }

  public void setBook
  (Book book)
  {
    execute(
      () -> {
        bookLock.lock();
        try {
          this.book = book;
          book.setStatusListener(this);
          notifyViewerBookChange();
        } finally {
          bookLock.unlock();
        }
        updateProjectControlsPanel();
        updateSourceDocumentsTab();
        updateTransformSetsTab();
        haveUnsavedChanges = false;
      },
      true
    );
  }

  Pattern floatPattern = Pattern.compile("-?\\d+(\\.\\d+)?([eE][+\\-]?\\d+)?");

  private float validatedFloatFromString
  (String source, String name, float min, float max)
  throws Exception
  {
    if (floatPattern.matcher(source).matches()) {
      float value = Float.parseFloat(source);
      if ((value >= min) && (value <= max)) {
        return value;
      }
    }
    throw new Exception(name + " value \"" + source + "\" is invalid.\nMust be a number between " + min + " and " + max);
  }

  private void updateProjectControlsPanel
  ()
  {
    Book book = getBook();
    projectNameTextField.setText(book.getName());
    projectOutputPathTextField.setText(book.getOutputPath());
    projectSignaturesOutputPathTextField.setText(book.getSignaturesOutputPath());
    projectCommentTextArea.setText(book.getComments());
    projectScaleToFitCheckBox.setSelected(book.getScaleToFit());
    projectEnableMarginsCheckBox.setSelected(book.isUsingMargins());
    projectEnablePageNumberingCheckBox.setSelected(book.isUsingPageNumbering());
    setPageSizePairsComboBoxSelection();
    projectControlsPanel.removeAll();
    JPanel marginControlsPanel = new JPanel();
    marginControlsPanel.setLayout(new BoxLayout(marginControlsPanel, BoxLayout.X_AXIS));
    marginControlsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    marginControlsPanel.add(new JLabel(translate("marginRatiosColon")));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    marginControlsPanel.add(Box.createHorizontalGlue());
    marginControlsPanel.add(new JLabel(translate("leftColon")));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    JTextField leftMarginRatioField = createRangedFloatField(book.getLeftMarginRatio());
    leftMarginRatioField.addActionListener(e -> {
      execute(() -> {
        try {
          book.setLeftMarginRatio(
            validatedFloatFromString(leftMarginRatioField.getText(), "Left margin ratio", 0.0f, 0.5f)
          );
          registerUnsavedChange();
        } catch (Exception ex) {
          errorDialog(ex);
        }
      });
    });
    leftMarginRatioField.setToolTipText(translate("marginsLeftTooltip"));
    marginControlsPanel.add(leftMarginRatioField);
    marginControlsPanel.add(Box.createHorizontalGlue());
    marginControlsPanel.add(new JLabel(translate("rightColon")));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    JTextField rightMarginRatioField = createRangedFloatField(book.getRightMarginRatio());
    rightMarginRatioField.addActionListener(e -> {
      execute(() -> {
        try {
          book.setRightMarginRatio(
            validatedFloatFromString(rightMarginRatioField.getText(), "Right margin ratio", 0.0f, 0.5f)
          );
          registerUnsavedChange();
        } catch (Exception ex) {
          errorDialog(ex);
        }
      });
    });
    rightMarginRatioField.setToolTipText(translate("marginsRightTooltip"));
    marginControlsPanel.add(rightMarginRatioField);
    marginControlsPanel.add(Box.createHorizontalGlue());
    marginControlsPanel.add(new JLabel(translate("bottomColon")));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    JTextField bottomMarginRatioField = createRangedFloatField(book.getBottomMarginRatio());
    bottomMarginRatioField.addActionListener(e -> {
      execute(() -> {
        try {
          book.setBottomMarginRatio(
            validatedFloatFromString(bottomMarginRatioField.getText(), "Bottom margin ratio", 0.0f, 0.5f)
          );
          registerUnsavedChange();
        } catch (Exception ex) {
          errorDialog(ex);
        }
      });
    });
    bottomMarginRatioField.setToolTipText(translate("marginsBottomTooltip"));
    marginControlsPanel.add(bottomMarginRatioField);
    marginControlsPanel.add(Box.createHorizontalGlue());
    marginControlsPanel.add(new JLabel(translate("topColon")));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    JTextField topMarginRatioField = createRangedFloatField(book.getTopMarginRatio());
    topMarginRatioField.addActionListener(e -> {
      execute(() -> {
        try {
          book.setTopMarginRatio(
            validatedFloatFromString(topMarginRatioField.getText(), "Top margin ratio", 0.0f, 0.5f)
          );
          registerUnsavedChange();
        } catch (Exception ex) {
          errorDialog(ex);
        }
      });
    });
    topMarginRatioField.setToolTipText(translate("marginsTopTooltip"));
    marginControlsPanel.add(topMarginRatioField);
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    projectControlsPanel.add(marginControlsPanel);
    projectControlsPanel.add(Box.createVerticalStrut(scale(5)));

    JPanel signatureControlsPanel = new JPanel();
    signatureControlsPanel.setLayout(new BoxLayout(signatureControlsPanel, BoxLayout.X_AXIS));
    signatureControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    signatureControlsPanel.add(new JLabel(translate("signaturesColon")));
    signatureControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    signatureControlsPanel.add(Box.createHorizontalGlue());
    signatureControlsPanel.add(new JLabel(translate("sheetsColon")));
    signatureControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    JComboBox<Integer> signatureSheetsComboBox = new JComboBox<>();
    for (int i = 1; i <= 20; i++) {
      signatureSheetsComboBox.addItem(i);
    }
    signatureSheetsComboBox.setSelectedItem(book.getSignatureSheets());
    signatureSheetsComboBox.addActionListener(
      e -> {
        if (signatureSheetsComboBox.getSelectedItem() != null) {
          book.setSignatureSheets((Integer) signatureSheetsComboBox.getSelectedItem());
          registerUnsavedChange();
        }
      }
    );
    signatureSheetsComboBox.setMinimumSize(new Dimension(scale(58), scale(22)));
    signatureSheetsComboBox.setMaximumSize(new Dimension(scale(58), scale(22)));
    signatureSheetsComboBox.setToolTipText(translate("signatureSheetsComboTooltip"));
    signatureControlsPanel.add(signatureSheetsComboBox);
    signatureControlsPanel.add(Box.createHorizontalGlue());
    JLabel minimiseLastSignatureLabel = new JLabel(translate("minimiseLastColon"));
    signatureControlsPanel.add(minimiseLastSignatureLabel);
    JCheckBox minimiseLastSignatureCheckbox = new JCheckBox();
    String minimiseLastSignatureTooltip = translate("minimiseLastSignatureTooltip");
    minimiseLastSignatureLabel.setToolTipText(minimiseLastSignatureTooltip);
    minimiseLastSignatureCheckbox.setToolTipText(minimiseLastSignatureTooltip);
    minimiseLastSignatureCheckbox.setToolTipText(minimiseLastSignatureTooltip);
    minimiseLastSignatureCheckbox.setSelected(book.isMinimisingLastSignature());
    minimiseLastSignatureCheckbox.addActionListener(
      e -> {
        book.setMinimiseLastSignature(minimiseLastSignatureCheckbox.isSelected());
        registerUnsavedChange();
      }
    );
    signatureControlsPanel.add(minimiseLastSignatureCheckbox);
    signatureControlsPanel.add(Box.createHorizontalGlue());
    signatureControlsPanel.add(new JLabel(translate("spineOffset")));
    signatureControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    JTextField spineOffsetRatioField = createRangedFloatField(book.getSpineOffsetRatio());
    spineOffsetRatioField.setToolTipText(translate("signatureSpineOffsetTooltip"));
    signatureControlsPanel.add(spineOffsetRatioField);
    signatureControlsPanel.add(Box.createHorizontalGlue());
    signatureControlsPanel.add(new JLabel(translate("edgeOffset")));
    signatureControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    JTextField edgeOffsetRatioField = createRangedFloatField(book.getEdgeOffsetRatio());
    edgeOffsetRatioField.setToolTipText(translate("signatureEdgeOffsetTooltip"));
    signatureControlsPanel.add(edgeOffsetRatioField);
    signatureControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    projectControlsPanel.add(signatureControlsPanel);
    projectControlsPanel.add(Box.createVerticalGlue());
  }

  private void updateSourceDocumentsTab
  ()
  {
    populateSourceDocumentsComboBox();
    if (book.getSourceDocuments().size() > 0) {
      sourceDocumentsComboBox.setSelectedIndex(-1);
      sourceDocumentsComboBox.setSelectedIndex(0);
      setEnabledSourceDocumentsWidgets(true);
    } else {
      documentNameTextField.setText("");
      documentPathTextField.setText("");
      documentCommentTextArea.setText("");
      setEnabledSourceDocumentsWidgets(false);
    }
    String pageRangesSource = book.getPageRangesSource();
    if (pageRangesSource != null) {
      pageRangesTextArea.setText(pageRangesSource);
      execute(() -> {
        try {
          book.getPages();
        } catch (Throwable t) {
          errorDialog(t);
        }
      });
    }
  }

  void populateSourceDocumentsComboBox
  ()
  {
    DefaultComboBoxModel<SourceDocument> model = new DefaultComboBoxModel<>();
    Book book = getBook();
    if (book.getSourceDocuments() != null) {
      for (SourceDocument sourceDocument : book.getSourceDocuments()) {
        model.addElement(sourceDocument);
      }
    }
    sourceDocumentsComboBoxModel = model;
    sourceDocumentsComboBox.setModel(model);
    sourceDocumentsComboBox.createToolTip().setTipText(translate("sourceDocumentsComboTooltip"));
    sourceDocumentsComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        if (!Objects.equals(selectedDocument, sourceDocumentsComboBox.getSelectedItem())) {
          populateSourceDocumentWidgets();
        }
      }
    });
  }

  private void populateSourceDocumentWidgets
  ()
  {
    selectedDocument = (SourceDocument)sourceDocumentsComboBox.getSelectedItem();
    if (selectedDocument != null) {
      documentIdentifierTextField.setText(selectedDocument.getStringId());
      documentNameTextField.setText(selectedDocument.getName());
      documentPathTextField.setText(selectedDocument.getPath());
      documentCommentTextArea.setText(selectedDocument.getComment());
    } else {
      documentIdentifierTextField.setText("");
      documentNameTextField.setText("");
      documentPathTextField.setText("");
      documentCommentTextArea.setText("");
    }
  }


  private void updateTransformSetsTab
  ()
  {
    populateTransformSetComboBox();
    if (book.getTransformSets().size() > 0) {
      transformSetsComboBox.setSelectedIndex(-1);
      transformSetsComboBox.setSelectedIndex(0);
      setEnabledTransformsWidgets(true);
    } else {
      transformSetNameTextField.setText("");
      transformSetCommentTextArea.setText("");
      transformSetPageRangesTextField.setText("");
      transformsPanel.removeAll();
      setEnabledTransformsWidgets(false);
    }
  }

  private void populateTransformSetComboBox
  ()
  {
    DefaultComboBoxModel<TransformSet> model = new DefaultComboBoxModel<>();
    Book book = getBook();
    for (TransformSet transformSet : book.getTransformSets()) {
      model.addElement(transformSet);
    }
    transformSetComboBoxModel = model;
    transformSetsComboBox.setModel(model);
    transformSetsComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        if (!Objects.equals(selectedTransformSet, transformSetsComboBox.getSelectedItem())) {
          populateTransformSetWidgets();
        }
      }
    });
  }

  private void populateTransformSetWidgets
  ()
  {
    selectedTransformSet = (TransformSet)transformSetsComboBox.getSelectedItem();
    if (selectedTransformSet != null) {
      transformSetNameTextField.setText(selectedTransformSet.getName());
      transformSetPageRangesTextField.setText(selectedTransformSet.getPageRangesString());
      transformSetCommentTextArea.setText(selectedTransformSet.getComment());
      updateTransformControls(selectedTransformSet);
      setEnabledTransformsWidgets(true);
    }
  }

  @SuppressWarnings("unchecked")
  private void populatePageSizesComboBox
  ()
  {
    DefaultComboBoxModel<Pair<Rectangle, Rectangle>> model = new DefaultComboBoxModel<>();
    Book book = getBook();
    this.pageSizePairsComboBoxModel = model;
    final Object[][] pageSizeTuples = new Object[][] {
      { PageSize.A4, PageSize.A3, "A4 / A3" },
      { PageSize.A5, PageSize.A4, "A5 / A4" },
      { PageSize.HALFLETTER, PageSize.LETTER, "HalfLetter / Letter" },
      { PageSize.LETTER, PageSize.TABLOID, "Letter / Tabloid" }
    };
    for (Object[] pageSizes : pageSizeTuples) {
      model.addElement(new Pair<>((Rectangle)pageSizes[0], (Rectangle)pageSizes[1]) {

        public String toString
        ()
        {
          return (String)pageSizes[2];
        }

      });
    }
    pageSizePairsComboBox.setModel(model);
    Pair<Rectangle, Rectangle> bookSizes = new Pair<>(book.getPageSize(), book.getSignaturePageSize());
    int bookSizesIndex = pageSizePairsComboBoxModel.getIndexOf(bookSizes);
    pageSizePairsComboBox.setSelectedIndex(bookSizesIndex);
    setPageSizePairsComboBoxSelection();
    pageSizePairsComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        getBook().setPageSize(((Pair<Rectangle, Rectangle>)model.getSelectedItem()).getFirst());
        getBook().setSignaturePageSize(((Pair<Rectangle, Rectangle>)model.getSelectedItem()).getSecond());
        registerUnsavedChange();
      }
    });
  }

  private boolean equals
  (Rectangle first, Rectangle second)
  {
    return first.getLeft() == second.getLeft() &&
      first.getRight() == second.getRight() &&
      first.getBottom() == second.getBottom() &&
      first.getTop() == second.getTop();
  }

  private void setPageSizePairsComboBoxSelection
  ()
  {
    Book book = getBook();
    Pair<Rectangle, Rectangle> bookSizes = new Pair<>(book.getPageSize(), book.getSignaturePageSize());
    for (int index = 0; index < pageSizePairsComboBoxModel.getSize(); index++) {
      Pair<Rectangle, Rectangle> nextBookSizes = pageSizePairsComboBoxModel.getElementAt(index);
      if (equals(bookSizes.getFirst(), nextBookSizes.getFirst()) &&
          equals(bookSizes.getSecond(), nextBookSizes.getSecond())) {
        pageSizePairsComboBoxModel.setSelectedItem(nextBookSizes);
        return;
      }
    }
    pageSizePairsComboBox.setSelectedIndex(-1);
  }

  private void updateTransformControls
  (TransformSet transformSet)
  {
    // Replace the transform sets:
    transformsPanel.removeAll();
    if (transformSet == null) {
      transformsPanel.setVisible(false);
      transformsPanel.setVisible(true);
      return;
    }
    Collection<Transform> transforms = transformSet.getTransforms();
    for (Transform transform : transforms) {
      JPanel controlRowPanel = new JPanel();
      controlRowPanel.setLayout(new BoxLayout(controlRowPanel, BoxLayout.X_AXIS));
      controlRowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
      if (transform.hasRangedValue()) {
        JPanel rangedFloatSliderPanel = new RangedFloatSlider(transform.getRangedFloat(), this);
        controlRowPanel.add(rangedFloatSliderPanel);
      } else {
        JLabel transformLabel = new JLabel(transform.getLongDescription());
        transformLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (22)));
        controlRowPanel.add(Box.createHorizontalStrut(scale(5)));
        controlRowPanel.add(transformLabel);
      }
      controlRowPanel.add(Box.createHorizontalStrut(scale(5)));
      JButton downButton = newNavAndControlButton(
        "down.png",
        e -> {
          transformSet.moveDown(transform);
          updateTransformControls(transformSet);
          registerUnsavedChange();
        }
      );
      controlRowPanel.add(downButton);
      downButton.setToolTipText(translate("transformTooltipDownButton"));
      controlRowPanel.add(Box.createHorizontalStrut(scale(5)));
      JButton upButton = newNavAndControlButton(
        "up.png",
        e -> {
          transformSet.moveUp(transform);
          updateTransformControls(transformSet);
          registerUnsavedChange();
        }
      );
      controlRowPanel.add(upButton);
      upButton.setToolTipText(translate("transformTooltipUpButton"));
      controlRowPanel.add(Box.createHorizontalStrut(scale(5)));
      JButton deleteButton = newNavAndControlButton(
        "delete.png",
        e -> {
          transformSet.remove(transform);
          updateTransformControls(transformSet);
          registerUnsavedChange();
        }
      );
      controlRowPanel.add(deleteButton);
      deleteButton.setToolTipText(translate("transformTooltipDeleteButton"));
      controlRowPanel.add(Box.createHorizontalStrut(scale(5)));
      JButton enableDisableButton = newNavAndControlButton(
        transform.isEnabled() ? "enabled.png" : "disabled.png",
        e -> {
          transform.setEnabled(!transform.isEnabled());
          updateTransformControls(transformSet);
          registerUnsavedChange();
        }
      );
      controlRowPanel.add(enableDisableButton);
      enableDisableButton.setToolTipText(translate("transformTooltipEnableDisableButton"));
      JPanel transformRowInnerPanel;
      if (transform.hasRangedValue()) {
        transformRowInnerPanel =
          createScaledLabeledWidgetPanel(controlRowPanel, transform.toString(), 22, 22);
      } else {
        transformRowInnerPanel = new JPanel();
        transformRowInnerPanel.setLayout(new BoxLayout(transformRowInnerPanel, BoxLayout.X_AXIS));
        transformRowInnerPanel.add(Box.createHorizontalStrut(scale(5)));
        transformRowInnerPanel.add(controlRowPanel);
        transformRowInnerPanel.add(Box.createHorizontalStrut(scale(5)));
        transformRowInnerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)controlRowPanel.getMaximumSize().getHeight() + 10));
      }
      JPanel transformRowPanel = new JPanel();
      transformRowPanel.setLayout(new BoxLayout(transformRowPanel, BoxLayout.Y_AXIS));
      transformRowPanel.add(Box.createVerticalStrut(scale(5)));
      transformRowPanel.add(transformRowInnerPanel);
      transformRowPanel.add(Box.createVerticalStrut(scale(5)));
      transformsPanel.add(Box.createVerticalStrut(scale(5)));
      transformRowPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
      transformRowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)transformRowInnerPanel.getMaximumSize().getHeight() + 10));
      JPanel transformRowHWrapperPanel = new JPanel();
      transformRowHWrapperPanel.setLayout(new BoxLayout(transformRowHWrapperPanel, BoxLayout.X_AXIS));
      transformRowHWrapperPanel.add(Box.createHorizontalStrut(scale(5)));
      transformRowHWrapperPanel.add(transformRowPanel);
      transformRowHWrapperPanel.add(Box.createHorizontalStrut(scale(5)));
      transformRowHWrapperPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)transformRowPanel.getMaximumSize().getHeight() + 10));
      transformsPanel.add(transformRowHWrapperPanel);
    }
    transformsPanel.add(Box.createVerticalStrut(scale(10)));
    JComboBox<Transform.Type> newTransformCombo = new JComboBox<>();
    newTransformCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    for (Transform.Type type : Transform.Type.values()) {
      newTransformCombo.addItem(type);
    }
    JPanel newTransformRowPanel = new JPanel();
    newTransformRowPanel.setLayout(new BoxLayout(newTransformRowPanel, BoxLayout.X_AXIS));
    newTransformRowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    newTransformRowPanel.add(Box.createHorizontalStrut(scale(5)));
    newTransformRowPanel.add(newTransformCombo);
    newTransformRowPanel.add(Box.createHorizontalStrut(scale(5)));
    newTransformRowPanel.add(newNavAndControlButton(
      "new.png",
      e -> {
        Transform.Type type = (Transform.Type)newTransformCombo.getSelectedItem();
        if (type != null) {
          Transform transform = new Transform(
            type,
            Transform.getDefaultValue(type), Transform.getDefaultLowValue(type), Transform.getDefaultHighValue(type)
          );
          transformSet.add(transform);
          updateTransformControls(transformSet);
          registerUnsavedChange();
        }
      }
    ));
    newTransformRowPanel.add(Box.createHorizontalStrut(scale(5)));
    transformsPanel.add(newTransformRowPanel);
    transformsPanel.add(Box.createVerticalGlue());
    transformsPanel.add(Box.createVerticalStrut(scale(5)));
    transformsPanel.setVisible(false);
    transformsPanel.setVisible(true);
  }

  @Override
  public void registerUnsavedChange
  ()
  {
    haveUnsavedChanges = true;
    notifyViewerBookChange();
  }

  private void notifyViewerBookChange
  ()
  {
//    if (viewerActive) {
    if (viewerRenderingThread != null) {
      viewerRenderingThread.signalTask();
    }
//    }
    haveViewerContentChange = true;
  }


  @Override
  public void actionPerformed
  (ActionEvent event)
  {
    try {
      switch (event.getActionCommand()) {
        case ACTION_EXIT -> {
          gracefulExit();
        }
        case ACTION_GENERATE -> {
          if (book != null) {
            execute(() -> {
              try {
                book.generatePDF(book.getOutputPath());
              } catch (Throwable t) {
                errorDialog(t);
              }
            });
          }
        }
        case ACTION_GENERATE_SIGNATURES -> {
          if (book != null) {
            execute(() -> {
              try {
                book.generatePDFSignatures(book.getSignaturesOutputPath(), "signature_");
              } catch (Throwable t) {
                errorDialog(t);
              }
            });
          }
        }
        case ACTION_SHOW_HIDE_VIEWER -> {
          boolean newViewer = false;
          if (viewer == null) {
            viewer = new ICEViewer(translate("viewerTitle"), 800, 1024, this);
            newViewer = true;
          }
          if (viewerRenderingThread == null) {
            viewerRenderingThread = new ViewerRenderingThread();
            viewerRenderingThread.start();
          }
          if (viewerButton.isSelected()) {
            viewerButton.setSelected(false);
            viewerActive = false;
            viewer.setVisible(false);
          } else {
            viewerButton.setSelected(true);
            viewerActive = true;
            if (newViewer) {
              viewerRenderingThread.signalTask();
            }
            viewer.setVisible(true);
          }
        }
      }
    } catch (Throwable t) {
      errorDialog(t);
    }
  }

  @Override
  public void onICEViewerClose
  ()
  {
    gracefulExit();
  }

  @Override
  public void printBookStatus
  (String statusString)
  {
    setStatusMessage(statusString);
  }

  @Override
  public void handleBookException
  (Exception e)
  {
    errorDialog(e);
  }

  @Override
  public void handleBookProgressLabel
  (String progressLabel)
  {
    setStatusProgressLabel(progressLabel);
  }

  @Override
  public void handleBookProgress
  (float progress, float maxProgress)
  {
    setStatusProgress(progress, maxProgress);
  }

  @SuppressWarnings({"all"})
  public static void doMain
  (String[] args)
  throws Exception
  {
    System.err.println("Son of Binderator : Starting...");
    String userDir = System.getProperty("user.dir");
    File userDirFile = new File(userDir);
    File initFile;
    if (userDirFile.canRead() && userDirFile.isDirectory() && userDirFile.canExecute() && userDirFile.canWrite()) {
      initFile = new File(userDirFile, ".binderator");
    } else {
      initFile = new File(".binderator");
    }
    InitFile.initialise(initFile.getPath(), false);
    GUIUtils.setStandardScale();
    try {
      UIManager.setLookAndFeel(new FlatLightLaf());
    } catch (Exception e) {
      System.err.println( "Failed to initialize LaF: " + e.getMessage());
    }
    Translations.setErrorHandler(

      new Translations.ErrorHandler() {

        @Override
        public void handleError
          (String message)
        {
          JOptionPane.showConfirmDialog(null, message);
        }
      }

    );
    Translations.initialiseFromJar("/translations");
    BinderatorFrame frame = getInstance();
    frame.showProgressBars = InitFile.instance().get(OptionsDialog.OPTION_SHOW_PROGRESS, "t").toString().equals("t");
    File projectFile = null;
    if (args.length > 0) {
      projectFile = new File(args[0]);
      String workingDirectory = System.getProperty("user.dir");
      if (!projectFile.isAbsolute()) {
        projectFile = new File(workingDirectory + File.separator + projectFile);
      }
    } else {
      String lastProjectPath = (String)InitFile.instance().get("projectPath");
      if (lastProjectPath != null) {
        projectFile = new File(lastProjectPath);
      }
    }
    java.net.URL helpURL = BinderatorFrame.class.getResource("/help");
    InlineHelp.initialise(frame, helpURL);
    if ((projectFile != null) && projectFile.canRead()) {
      Book book = frame.getStore().loadBook(projectFile.getPath());
      frame.basePath = projectFile.getParent();
      frame.projectPath = projectFile.getPath();
      frame.setBook(book);
      frame.setStatusMessage(translate("project") + " " + book.getName() + " " + translate("loadedSuccessfully"));
    }
    frame.execute(() -> { frame.haveUnsavedChanges = false; });
    frame.setVisible(true);
  }

  public static void main
  (String[] args)
  {
    PrintStream errorOut = System.err;
    try {
      doMain(args);
    } catch (Throwable t) {
      errorOut.print("Caught exception in main(): " + t.getMessage() + "\n\nBacktrace:\n\n");
      t.printStackTrace(errorOut);
      System.exit(1);
    }
  }

}
