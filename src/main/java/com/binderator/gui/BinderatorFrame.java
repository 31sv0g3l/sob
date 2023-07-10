package com.binderator.gui;


import com.binderator.ext.gui.JFontChooser;
import com.binderator.persistence.*;
import com.binderator.engine.*;
import com.binderator.util.*;
import com.formdev.flatlaf.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.concurrent.locks.*;
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
      if (viewerActive || signaturesViewerActive) {
        bookLock.lock();
        try {
          if (book.getPageCount() > 0) {
            Book book = new Book(getBook());
            bookLock.unlock();
            if (viewerActive) {
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
              book.generatePDF(byteArrayOutputStream, book.isUsingMargins(), book.isUsingPageNumbering(), null);
              byte[] bookBytes = byteArrayOutputStream.toByteArray();
              int pageNumber = viewer.controller.getCurrentPageNumber();
              viewer.setContent(bookBytes);
              viewer.controller.showPage(pageNumber);
            }
            if (signaturesViewerActive) {
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
              book.generatePDFSignatures(byteArrayOutputStream, null, null, true);
              byte[] signaturesBytes = byteArrayOutputStream.toByteArray();
              int pageNumber = signaturesViewer.controller.getCurrentPageNumber();
              signaturesViewer.setContent(signaturesBytes, false);
              signaturesViewer.controller.showPage(pageNumber);
            }
          } else {
            if (viewer != null) {
              viewer.setContent(new byte[0]);
            }
            if (signaturesViewer != null) {
              signaturesViewer.setContent(new byte[0]);
            }
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
  public static final String VERSION = "0.3.0";
  private static BinderatorFrame singletonInstance = null;
  private boolean haveUnsavedChanges = false;
  private static final FileFilter binderatorFileFilter =
    new FileNameExtensionFilter("Son of Binderator files","sob", "SOB", "bdr");
  private static final FileFilter pdfFileFilter =
    new FileNameExtensionFilter("PDF files","pdf", "PDF");

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
  JButton signaturesViewerButton;
  JButton exitButton;
  JPanel projectPanel;
  JPanel documentsPanel;
  JPanel pagesPanel;
  JPanel documentsAndPagesPanel;
  JPanel transformSetsPanel;
  JPanel transformsPanel;
  JPanel textGeneratorsPanel;
  ImageIcon newIcon;
  ImageIcon deleteIcon;
  ImageIcon upIcon;
  ImageIcon downIcon;
  private static final String ACTION_EXIT = "exit";
  private static final String ACTION_GENERATE = "generate";
  private static final String ACTION_GENERATE_SIGNATURES = "generateSignatures";
  private static final String ACTION_SHOW_HIDE_VIEWER = "showHideViewer";
  private static final String ACTION_SHOW_HIDE_SIGNATURES_VIEWER = "showHideSignaturesViewer";
  private boolean viewerActive = false;
  private boolean signaturesViewerActive = false;
  Book book = null;
  ReentrantLock bookLock = new ReentrantLock();
  JComboBox<SourceDocument> sourceDocumentsComboBox;
  DefaultComboBoxModel<SourceDocument> sourceDocumentsComboBoxModel;
  JComboBox<TransformSet> transformSetsComboBox;
  DefaultComboBoxModel<TransformSet> transformSetComboBoxModel;
  JComboBox<TextGenerator> textGeneratorsComboBox;
  DefaultComboBoxModel<TextGenerator> textGeneratorsComboBoxModel;
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

  // Documents Panel Widgets:
  JTextField documentNameTextField = new JTextField(34);
  JTextField documentIdentifierTextField = new JTextField(8);
  JTextField documentPathTextField = new JTextField(34);
  JButton documentPathButton;
  JTextArea documentCommentTextArea = new JTextArea(5, 34);
  JTextArea pageRangesTextArea = new JTextArea(7, 34);
  boolean pageRangesTextValid = false;
  JButton pageRangesApplyButton = new JButton();
  final Color pageRangesApplyButtonDefaultColor = pageRangesApplyButton.getBackground();

  // Page Operations Panel Widgets:
  private TransformSet selectedTransformSet = null;
  JTextField transformSetNameTextField = new JTextField(34);
  JTextField transformSetPageRangesTextField = new JTextField(34);
  JTextArea transformSetCommentTextArea = new JTextArea(5, 34);
  private TextGenerator selectedTextGenerator = null;
  JTextField textGeneratorNameTextField = new JTextField(34);
  JTextField textGeneratorPageRangesTextField = new JTextField(34);
  JTextArea textGeneratorCommentTextArea = new JTextArea(5, 34);
  JTextField textGeneratorHorizontalOffsetField = new JTextField(6);
  JTextField textGeneratorVerticalOffsetField = new JTextField(6);
  JTextField textGeneratorLineHeightField = new JTextField(6);
  JTextArea textGeneratorContentTextArea = new JTextArea(10, 34);
  JComboBox<TextGenerator.Alignment> textGeneratorAlignmentComboBox;
  DefaultComboBoxModel<TextGenerator.Alignment> textGeneratorAlignmentComboBoxModel;
  JLabel textGeneratorFontLabel = new JLabel("");
  private SourceDocument selectedDocument = null;
  private JDialog optionsDialog = null;
  private JDialog inlineHelpDialog = null;
  private boolean showProgressBars = true;
  private ICEViewer viewer = null;
  private ICEViewer signaturesViewer = null;
  private String basePath = null;

  JPanel projectControlsPanel;
  JPanel marginControlsPanel;
  JTextField leftMarginRatioField;
  JTextField rightMarginRatioField;
  JTextField bottomMarginRatioField;
  JTextField topMarginRatioField;
  JPanel signatureControlsPanel;
  JComboBox<Integer> signatureSheetsComboBox;
  JCheckBox minimiseLastSignatureCheckbox;
  JTextField spineOffsetRatioField;
  JTextField edgeOffsetRatioField;
  JComboBox<Book.TrimLinesType> signatureTrimLinesComboBox;
  private static final String TRIM_LINES_NONE = translate("trimLinesNone");
  private static final String TRIM_LINES_DEFAULT = translate("trimLinesDefault");
  private static final String TRIM_LINES_CUSTOM = translate("trimLinesCustom");
  JTextField signatureTrimLinesVerticalRatioField;
  JTextField signatureTrimLinesHorizontalRatioField;


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

    // ******************************************************************************************************
    // Project tab:
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
    projectScaleToFitCheckBox.addActionListener(e -> execute(() -> {
      getBook().setScaleToFit(projectScaleToFitCheckBox.isSelected());
      registerUnsavedChange();
    }));
    projectScaleToFitCheckBox.setToolTipText(translate("projectScaleToFitTooltip"));
    projectEnableMarginsCheckBox = new JCheckBox();
    projectEnableMarginsCheckBox.setText(translate("margins"));
    projectEnableMarginsCheckBox.setToolTipText(translate("projectEnableMarginsTooltip"));
    projectEnableMarginsCheckBox.addActionListener(e -> execute(() -> {
      getBook().setUsingMargins(projectEnableMarginsCheckBox.isSelected());
      registerUnsavedChange();
    }));
    projectEnablePageNumberingCheckBox = new JCheckBox();
    projectEnablePageNumberingCheckBox.setText(translate("pageNumbers"));
    projectEnablePageNumberingCheckBox.setToolTipText(translate("pageNumbersTooltip"));
    projectEnablePageNumberingCheckBox.addActionListener(e -> execute(() -> {
      getBook().setUsingPageNumbering(projectEnablePageNumberingCheckBox.isSelected());
      registerUnsavedChange();
    }));
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
          getBook().setComments(projectCommentTextArea.getText());
          registerUnsavedChange();
        });
      }

      @Override public void removeUpdate(DocumentEvent e) {
        execute(() -> {
          getBook().setComments(projectCommentTextArea.getText());
          registerUnsavedChange();
        });
      }

      @Override public void insertUpdate(DocumentEvent e) {
        execute(() -> {
          getBook().setComments(projectCommentTextArea.getText());
          registerUnsavedChange();
        });
      }

    });
    projectCommentTextArea.setLineWrap(true);
    projectCommentTextArea.setWrapStyleWord(true);
    JScrollPane projectCommentScrollPane = new JScrollPane(projectCommentTextArea);
    projectPanel.add(createScaledLabeledWidgetPanel(projectCommentScrollPane, translate("comments"), 22, 70));
    projectCommentTextArea.setToolTipText(translate("projectCommentsTooltip"));
    JPanel marginControlsPanel = new JPanel();
    marginControlsPanel.setLayout(new BoxLayout(marginControlsPanel, BoxLayout.X_AXIS));
    marginControlsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    marginControlsPanel.add(new JLabel(translate("marginRatiosColon")));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    marginControlsPanel.add(Box.createHorizontalGlue());
    marginControlsPanel.add(new JLabel(translate("leftColon")));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    leftMarginRatioField = createRangedFloatField(
      () -> { return getBook().getLeftMarginRatio(); },
      f -> {
        getBook().setLeftMarginRatio(validatedFloatFromString(f.getText(), "Left margin ratio", 0.0f, 0.5f));
      }
    );
    leftMarginRatioField.setToolTipText(translate("marginsLeftTooltip"));
    marginControlsPanel.add(leftMarginRatioField);
    marginControlsPanel.add(Box.createHorizontalGlue());
    marginControlsPanel.add(new JLabel(translate("rightColon")));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    rightMarginRatioField = createRangedFloatField(
      () -> { return getBook().getRightMarginRatio(); },
      f -> { getBook().setRightMarginRatio(validatedFloatFromString(f.getText(), "Right margin ratio", 0.0f, 0.5f)); }
    );
    rightMarginRatioField.setToolTipText(translate("marginsRightTooltip"));
    marginControlsPanel.add(rightMarginRatioField);
    marginControlsPanel.add(Box.createHorizontalGlue());
    marginControlsPanel.add(new JLabel(translate("bottomColon")));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    bottomMarginRatioField = createRangedFloatField(
      () -> { return getBook().getBottomMarginRatio(); },
      f -> { getBook().setBottomMarginRatio(validatedFloatFromString(f.getText(), "Bottom margin ratio", 0.0f, 0.5f)); }
    );
    bottomMarginRatioField.setToolTipText(translate("marginsBottomTooltip"));
    marginControlsPanel.add(bottomMarginRatioField);
    marginControlsPanel.add(Box.createHorizontalGlue());
    marginControlsPanel.add(new JLabel(translate("topColon")));
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    topMarginRatioField = createRangedFloatField(
      () -> { return getBook().getTopMarginRatio(); },
      f -> { getBook().setTopMarginRatio(validatedFloatFromString(f.getText(), "Top margin ratio", 0.0f, 0.5f)); }
    );
    topMarginRatioField.setToolTipText(translate("marginsTopTooltip"));
    marginControlsPanel.add(topMarginRatioField);
    marginControlsPanel.add(Box.createHorizontalStrut(scale(5)));
    projectControlsPanel.add(marginControlsPanel);
    projectControlsPanel.add(Box.createVerticalStrut(scale(5)));
    JPanel signatureControlsTopPanel = new JPanel();
    signatureControlsTopPanel.setLayout(new BoxLayout(signatureControlsTopPanel, BoxLayout.X_AXIS));
    signatureControlsTopPanel.add(Box.createHorizontalStrut(scale(5)));
    signatureControlsTopPanel.add(new JLabel(translate("signaturesColon")));
    signatureControlsTopPanel.add(Box.createHorizontalStrut(scale(5)));
    signatureControlsTopPanel.add(Box.createHorizontalGlue());
    signatureControlsTopPanel.add(new JLabel(translate("sheetsColon")));
    signatureControlsTopPanel.add(Box.createHorizontalStrut(scale(5)));
    signatureSheetsComboBox = new JComboBox<>();
    for (int i = 1; i <= 20; i++) {
      signatureSheetsComboBox.addItem(i);
    }
    signatureSheetsComboBox.setSelectedItem(book.getSignatureSheets());
    signatureSheetsComboBox.addActionListener(
      e -> {
        if (signatureSheetsComboBox.getSelectedItem() != null) {
          getBook().setSignatureSheets((Integer) signatureSheetsComboBox.getSelectedItem());
          registerUnsavedChange();
        }
      }
    );
    signatureSheetsComboBox.setMinimumSize(new Dimension(scale(58), scale(22)));
    signatureSheetsComboBox.setMaximumSize(new Dimension(scale(58), scale(22)));
    signatureSheetsComboBox.setToolTipText(translate("signatureSheetsComboTooltip"));
    signatureControlsTopPanel.add(signatureSheetsComboBox);
    signatureControlsTopPanel.add(Box.createHorizontalGlue());
    JLabel minimiseLastSignatureLabel = new JLabel(translate("minimiseLastColon"));
    signatureControlsTopPanel.add(minimiseLastSignatureLabel);
    minimiseLastSignatureCheckbox = new JCheckBox();
    String minimiseLastSignatureTooltip = translate("minimiseLastSignatureTooltip");
    minimiseLastSignatureLabel.setToolTipText(minimiseLastSignatureTooltip);
    minimiseLastSignatureCheckbox.setToolTipText(minimiseLastSignatureTooltip);
    minimiseLastSignatureCheckbox.setToolTipText(minimiseLastSignatureTooltip);
    minimiseLastSignatureCheckbox.setSelected(book.isMinimisingLastSignature());
    minimiseLastSignatureCheckbox.addActionListener(
      e -> {
        getBook().setMinimiseLastSignature(minimiseLastSignatureCheckbox.isSelected());
        registerUnsavedChange();
      }
    );
    signatureControlsTopPanel.add(minimiseLastSignatureCheckbox);
    signatureControlsTopPanel.add(Box.createHorizontalGlue());
    signatureControlsTopPanel.add(new JLabel(translate("spineOffset")));
    signatureControlsTopPanel.add(Box.createHorizontalStrut(scale(5)));
    spineOffsetRatioField = createRangedFloatField(
      () -> { return getBook().getSpineOffsetRatio(); },
      f -> { getBook().setSpineOffsetRatio(validatedFloatFromString(f.getText(), translate("spineOffsetRatio"), 0.0f, 0.9f));}
    );
    spineOffsetRatioField.setToolTipText(translate("signatureSpineOffsetTooltip"));
    signatureControlsTopPanel.add(spineOffsetRatioField);
    signatureControlsTopPanel.add(Box.createHorizontalGlue());
    signatureControlsTopPanel.add(new JLabel(translate("edgeOffset")));
    signatureControlsTopPanel.add(Box.createHorizontalStrut(scale(5)));
    edgeOffsetRatioField = createRangedFloatField(
      () -> { return getBook().getEdgeOffsetRatio(); },
      f -> { getBook().setEdgeOffsetRatio(validatedFloatFromString(f.getText(), translate("edgeOffsetRatio"), 0.0f, 0.9f));}
    );
    edgeOffsetRatioField.setToolTipText(translate("signatureEdgeOffsetTooltip"));
    signatureControlsTopPanel.add(edgeOffsetRatioField);
    signatureControlsTopPanel.add(Box.createHorizontalStrut(scale(5)));
    JPanel signatureControlsBottomPanel = new JPanel();
    signatureControlsBottomPanel.setLayout(new BoxLayout(signatureControlsBottomPanel, BoxLayout.X_AXIS));
    signatureControlsBottomPanel.add(Box.createHorizontalStrut(scale(106)));
    signatureTrimLinesComboBox = new JComboBox<>();
    signatureTrimLinesComboBox.addItem(Book.TrimLinesType.NONE);
    signatureTrimLinesComboBox.addItem(Book.TrimLinesType.DEFAULT);
    signatureTrimLinesComboBox.addItem(Book.TrimLinesType.CUSTOM);
    signatureTrimLinesComboBox.setMinimumSize(new Dimension(scale(58), scale(22)));
    signatureTrimLinesComboBox.setMaximumSize(new Dimension(scale(58), scale(22)));
    signatureTrimLinesComboBox.setToolTipText(translate("trimLinesComboTooltip"));
    signatureControlsBottomPanel.add(new JLabel(translate("trimLinesColon")));
    signatureControlsBottomPanel.add(signatureTrimLinesComboBox);
    signatureControlsBottomPanel.add(Box.createHorizontalGlue());
    final JLabel signatureTrimLinesHorizontalColonLabel = new JLabel(translate("trimLinesHorizontalColon"));
    signatureTrimLinesHorizontalColonLabel.setVisible(false);
    signatureControlsBottomPanel.add(signatureTrimLinesHorizontalColonLabel);
    signatureTrimLinesHorizontalRatioField = createRangedFloatField(
      () -> { return getBook().getTrimLinesHorizontalRatio(); },
      f -> { getBook().getTrimLinesHorizontalRatio().setValue(validatedFloatFromString(f.getText(), translate("trimLinesHorizontalRatio"), 0.0f, 0.5f));}
    );
    signatureControlsBottomPanel.add(signatureTrimLinesHorizontalRatioField);
    signatureControlsBottomPanel.add(Box.createHorizontalGlue());
    final JLabel signatureTrimLinesVerticalColonLabel = new JLabel(translate("trimLinesVerticalColon"));
    signatureTrimLinesVerticalColonLabel.setVisible(false);
    signatureControlsBottomPanel.add(signatureTrimLinesVerticalColonLabel);
    signatureTrimLinesVerticalRatioField = createRangedFloatField(
      () -> { return getBook().getTrimLinesVerticalRatio(); },
      f -> { getBook().getTrimLinesVerticalRatio().setValue(validatedFloatFromString(f.getText(), translate("trimLinesVerticalRatio"), 0.0f, 0.5f));}
    );
    signatureControlsBottomPanel.add(signatureTrimLinesVerticalRatioField);
    signatureTrimLinesComboBox.addActionListener(
      e -> {
        if (signatureTrimLinesComboBox.getSelectedItem() != null) {
          Book.TrimLinesType selectedItem = (Book.TrimLinesType)signatureTrimLinesComboBox.getSelectedItem();
          getBook().setTrimLinesType(selectedItem);
          if (selectedItem.equals(Book.TrimLinesType.CUSTOM)) {
            signatureTrimLinesHorizontalColonLabel.setVisible(true);
            signatureTrimLinesVerticalColonLabel.setVisible(true);
            signatureTrimLinesHorizontalRatioField.setVisible(true);
            signatureTrimLinesVerticalRatioField.setVisible(true);
          } else {
            signatureTrimLinesHorizontalColonLabel.setVisible(false);
            signatureTrimLinesVerticalColonLabel.setVisible(false);
            signatureTrimLinesHorizontalRatioField.setVisible(false);
            signatureTrimLinesVerticalRatioField.setVisible(false);
          }
          registerUnsavedChange();
        }
      }
    );
    signatureTrimLinesComboBox.setSelectedItem(getBook().getTrimLinesType());
    signatureControlsBottomPanel.add(Box.createHorizontalStrut(scale(5)));
    JPanel signatureControlsPanel = new JPanel();
    signatureControlsPanel.setLayout(new BoxLayout(signatureControlsPanel, BoxLayout.Y_AXIS));
    signatureControlsPanel.add(signatureControlsTopPanel);
    signatureControlsPanel.add(signatureControlsBottomPanel);
    projectControlsPanel.add(signatureControlsPanel);
    projectControlsPanel.add(Box.createVerticalGlue());
    updateProjectControlsPanel();
    projectPanel.add(Box.createVerticalGlue());

    // ******************************************************************************************************
    // Source Documents and page ranges tab:
    documentsPanel = new JPanel();
    JScrollPane documentsPane = new JScrollPane(documentsPanel);
    documentsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    documentsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    // Pages panel below documents panel:
    pagesPanel = new JPanel();
    pagesPanel.setLayout(new BoxLayout(pagesPanel, BoxLayout.X_AXIS));
    pageRangesTextArea.setLineWrap(true);
    pageRangesTextArea.setWrapStyleWord(true);
    JScrollPane pageRangesScrollPane = new JScrollPane(pageRangesTextArea);
    pageRangesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pageRangesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    JPanel pageRangesPanel = new JPanel();
    pageRangesPanel.setLayout(new BoxLayout(pageRangesPanel, BoxLayout.X_AXIS));
    pageRangesPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 100));
    pageRangesPanel.add(pageRangesScrollPane);
    pageRangesApplyButton.setPreferredSize(new Dimension(34, Integer.MAX_VALUE));
    pageRangesApplyButton.setMaximumSize(new Dimension(34, Integer.MAX_VALUE));
    pageRangesPanel.add(pageRangesApplyButton);
    pageRangesApplyButton.addActionListener(
      e -> execute(() -> {
        if (pageRangesTextValid) {
          getBook().setPageRangesSource(pageRangesTextArea.getText());
          pageRangesApplyButton.setBackground(pageRangesApplyButtonDefaultColor);
          registerUnsavedChange();
        } else {
          messageDialog(translate("pageRangesErrors"));
        }
      })
    );
    pagesPanel.add(createScaledLabeledWidgetPanel(pageRangesPanel, translate("pageRanges"), 22, 94));
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
          selectedDocument.setId(documentIdentifierTextField.getText());
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
        if (selectedDocument != null) {
          selectedDocument.setComment(documentCommentTextArea.getText());
          registerUnsavedChange();
        }
      }
      @Override public void removeUpdate(DocumentEvent e) {
        if (selectedDocument != null) {
          selectedDocument.setComment(documentCommentTextArea.getText());
          registerUnsavedChange();
        }
      }
      @Override public void insertUpdate(DocumentEvent e) {
        if (selectedDocument != null) {
          selectedDocument.setComment(documentCommentTextArea.getText());
          registerUnsavedChange();
        }
      }
    });
    documentCommentTextArea.setToolTipText(translate("sourceDocumentCommentTooltip"));
    documentsPanel.add(Box.createGlue());
    documentsPanel.add(Box.createVerticalStrut(scale(5)));
    documentsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setEnabledSourceDocumentsWidgets(false);

    // ******************************************************************************************************
    // Transforms tab:
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
    JButton newTransformSetForCurrentPageButton = newNavAndControlButton("new_for_current_page.png", e -> newTransformSetForCurrentPage());
    newTransformSetForCurrentPageButton.setToolTipText(translate("transformSetsNewForCurrentPageTooltip"));
    transformSetsNavPanel.add(newTransformSetForCurrentPageButton);
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
      if (selectedTransformSet != null) {
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
      if (selectedTransformSet != null) {
        try {
          selectedTransformSet.setPageRanges(PageRange.parsePageRanges(transformSetPageRangesTextField.getText(), false));
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
        if (selectedTransformSet != null) {
          selectedTransformSet.setComment(transformSetCommentTextArea.getText());
          registerUnsavedChange();
        }
      }

      @Override public void removeUpdate(DocumentEvent e) {
        if (selectedTransformSet != null) {
          selectedTransformSet.setComment(transformSetCommentTextArea.getText());
          registerUnsavedChange();
        }
      }
      @Override public void insertUpdate(DocumentEvent e) {
        if (selectedTransformSet != null) {
          selectedTransformSet.setComment(transformSetCommentTextArea.getText());
          registerUnsavedChange();
        }
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

    // ******************************************************************************************************
    // Text Generators tab:
    textGeneratorsPanel = new JPanel();
    JScrollPane textGeneratorsPane = new JScrollPane(textGeneratorsPanel);
    textGeneratorsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    textGeneratorsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    mainTabs.add(translate("textGenerators"), textGeneratorsPane);
    textGeneratorsPanel.setLayout(new BoxLayout(textGeneratorsPanel, BoxLayout.Y_AXIS));
    JPanel textGeneratorsNavPanel = new JPanel();
    textGeneratorsNavPanel.setLayout(new BoxLayout(textGeneratorsNavPanel, BoxLayout.X_AXIS));
    textGeneratorsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorsComboBox = new JComboBox<>();
    populateTextGeneratorsComboBox();
    textGeneratorsNavPanel.add(textGeneratorsComboBox);
    textGeneratorsComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    textGeneratorsComboBox.setToolTipText(translate("textGeneratorsComboTooltip"));
    textGeneratorsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton newTextGeneratorButton = newNavAndControlButton("new.png", e -> newTextGenerator());
    newTextGeneratorButton.setToolTipText(translate("textGeneratorsNewTooltip"));
    textGeneratorsNavPanel.add(newTextGeneratorButton);
    textGeneratorsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton downTextGeneratorButton = newNavAndControlButton(
      "down.png",
      e -> {
        if (textGeneratorsComboBox.getSelectedItem() != null) {
          downTextGenerator((TextGenerator) textGeneratorsComboBox.getSelectedItem());
        }
      }
    );
    textGeneratorsNavPanel.add(downTextGeneratorButton);
    downTextGeneratorButton.setToolTipText(translate("textGeneratorsDownTooltip"));
    textGeneratorsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton upTextGeneratorsButton = newNavAndControlButton(
      "up.png",
      e -> {
        if (textGeneratorsComboBox.getSelectedItem() != null) {
          upTextGenerator((TextGenerator) textGeneratorsComboBox.getSelectedItem());
        }
      }
    );
    textGeneratorsNavPanel.add(upTextGeneratorsButton);
    upTextGeneratorsButton.setToolTipText(translate("textGeneratorsUpTooltip"));
    textGeneratorsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    JButton deleteTextGeneratorsButton = newNavAndControlButton(
      "delete.png",
      e -> {
        if (textGeneratorsComboBox.getSelectedItem() != null) {
          deleteTextGenerator((TextGenerator) textGeneratorsComboBox.getSelectedItem());
        }
      }
    );
    textGeneratorsNavPanel.add(deleteTextGeneratorsButton);
    deleteTextGeneratorsButton.setToolTipText(translate("textGeneratorsDeleteTooltip"));
    textGeneratorsNavPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorsNavPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    textGeneratorsNavPanel.setVisible(true);
    textGeneratorsPanel.add(Box.createVerticalStrut(5));
    textGeneratorsPanel.add(textGeneratorsNavPanel);
    textGeneratorsPanel.add(Box.createVerticalStrut(5));
    textGeneratorsPanel.add(createScaledLabeledWidgetPanel(textGeneratorNameTextField, translate("name"), 22, 22));
    textGeneratorNameTextField.addActionListener(event -> {
      if ( selectedTextGenerator != null) {
        try {
          selectedTextGenerator.setName(textGeneratorNameTextField.getText());
          registerUnsavedChange();
        } catch (Exception e) {
          errorDialog(e);
        }
      }
    });
    textGeneratorNameTextField.setToolTipText(translate("textGeneratorsNameTooltip"));
    GUIUtils.addBackgroundSetter(textGeneratorNameTextField);
    textGeneratorsPanel.add(createScaledLabeledWidgetPanel(
      textGeneratorPageRangesTextField, translate("textGeneratorsPageRanges"), 22, 22
    ));
    textGeneratorPageRangesTextField.addActionListener(event -> {
      if ( selectedTextGenerator != null) {
        try {
          selectedTextGenerator.setPageRanges(PageRange.parsePageRanges(textGeneratorPageRangesTextField.getText(), false));
          registerUnsavedChange();
        } catch (Exception e) {
          errorDialog(e);
        }
      }
    });
    textGeneratorPageRangesTextField.setToolTipText(translate("textGeneratorsPageRangesTooltip"));
    GUIUtils.addBackgroundSetter(textGeneratorPageRangesTextField);
    textGeneratorCommentTextArea.setLineWrap(true);
    textGeneratorCommentTextArea.setWrapStyleWord(true);
    textGeneratorCommentTextArea.setToolTipText(translate("textGeneratorsCommentsTooltip"));
    JScrollPane textGeneratorCommentScrollPane = new JScrollPane(textGeneratorCommentTextArea);
    textGeneratorsPanel.add(
      createScaledLabeledWidgetPanel(textGeneratorCommentScrollPane, translate("textGeneratorsComments"), 22, 70)
    );
    textGeneratorCommentTextArea.getDocument().addDocumentListener(new DocumentListener() {

      @Override public void changedUpdate(DocumentEvent e) {
        if (selectedTextGenerator != null) {
          selectedTextGenerator.setComment(textGeneratorCommentTextArea.getText());
          registerUnsavedChange();
        }
      }

      @Override public void removeUpdate(DocumentEvent e) {
        if (selectedTextGenerator != null) {
          selectedTextGenerator.setComment(textGeneratorCommentTextArea.getText());
          registerUnsavedChange();
        }
      }

      @Override public void insertUpdate(DocumentEvent e) {
        if (selectedTextGenerator != null) {
          selectedTextGenerator.setComment(textGeneratorCommentTextArea.getText());
          registerUnsavedChange();
        }
      }

    });
    JPanel textGeneratorsXYAlignmentPanel = new JPanel();
    textGeneratorsXYAlignmentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    textGeneratorsXYAlignmentPanel.setLayout(new BoxLayout(textGeneratorsXYAlignmentPanel, BoxLayout.X_AXIS));
    textGeneratorsXYAlignmentPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorsXYAlignmentPanel.add(new JLabel(translate("textGeneratorsHorizontalOffset") + ":"));
    textGeneratorsXYAlignmentPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorHorizontalOffsetField.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    textGeneratorHorizontalOffsetField.addActionListener(event -> {
      if ( selectedTextGenerator != null) {
        try {
          selectedTextGenerator.setHorizontalOffset(validatedFloatFromString(
            textGeneratorHorizontalOffsetField.getText(), translate("textGeneratorsHorizontalOffset") + ":", 0.0f, 1.0f
          ));
          registerUnsavedChange();
        } catch (Exception e) {
          errorDialog(e);
        }
      }
    });
    GUIUtils.addBackgroundSetter(textGeneratorHorizontalOffsetField);
    textGeneratorsXYAlignmentPanel.add(textGeneratorHorizontalOffsetField);
    textGeneratorsXYAlignmentPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorsXYAlignmentPanel.add(new JLabel(translate("textGeneratorsVerticalOffset") + ":"));
    textGeneratorsXYAlignmentPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorVerticalOffsetField.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    textGeneratorVerticalOffsetField.addActionListener(event -> {
      if (selectedTextGenerator != null) {
        try {
          selectedTextGenerator.setVerticalOffset(validatedFloatFromString(
            textGeneratorVerticalOffsetField.getText(), translate("textGeneratorsVerticalOffset") + ":", 0.0f, 1.0f
          ));
          registerUnsavedChange();
        } catch (Exception e) {
          errorDialog(e);
        }
      }
    });
    GUIUtils.addBackgroundSetter(textGeneratorVerticalOffsetField);
    textGeneratorsXYAlignmentPanel.add(textGeneratorVerticalOffsetField);
    textGeneratorsXYAlignmentPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorsXYAlignmentPanel.add(new JLabel(translate("textGeneratorsAlign") + ":"));
    textGeneratorsXYAlignmentPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorAlignmentComboBox = new JComboBox<>();
    textGeneratorAlignmentComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    textGeneratorAlignmentComboBoxModel = new DefaultComboBoxModel<>(TextGenerator.Alignment.values());
    textGeneratorAlignmentComboBox.setModel(textGeneratorAlignmentComboBoxModel);
    textGeneratorAlignmentComboBox.addActionListener(
      e -> {
        if (textGeneratorAlignmentComboBox.getSelectedItem() != null) {
          if (selectedTextGenerator != null) {
            selectedTextGenerator.setAlignment((TextGenerator.Alignment) textGeneratorAlignmentComboBox.getSelectedItem());
          }
          registerUnsavedChange();
        }
      }
    );
    textGeneratorsXYAlignmentPanel.add(textGeneratorAlignmentComboBox);
    textGeneratorsXYAlignmentPanel.add(Box.createVerticalStrut(scale(5)));
    textGeneratorsXYAlignmentPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorsPanel.add(Box.createVerticalStrut(scale(5)));
    textGeneratorsPanel.add(textGeneratorsXYAlignmentPanel);
    textGeneratorsPanel.add(Box.createVerticalStrut(scale(5)));
    JPanel textGeneratorsFontPanel = new JPanel();
    textGeneratorsFontPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    textGeneratorsFontPanel.setLayout(new BoxLayout(textGeneratorsFontPanel, BoxLayout.X_AXIS));
    JFontChooser fontChooser = new JFontChooser();
    JButton fontChooserButton = new JButton(translate("chooseFont"));
    fontChooserButton.addActionListener(
      e -> execute(() -> {
        if (selectedTextGenerator != null) {
          java.awt.Font initialFont = selectedTextGenerator.getFont();
          if (initialFont != null) {
            fontChooser.setSelectedFont(initialFont);
          }
          fontChooser.showDialog(this);
          java.awt.Font font = fontChooser.getSelectedFont();
          if (!Objects.equals(initialFont, font)) {
            selectedTextGenerator.setFont(font);
            textGeneratorFontLabel.setText(font.getFontName() + " " + font.getSize());
            registerUnsavedChange();
          }
        }
      })
    );
    textGeneratorsFontPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorsFontPanel.add(fontChooserButton);
    textGeneratorsFontPanel.add(Box.createHorizontalStrut(scale(10)));
    textGeneratorsFontPanel.add(textGeneratorFontLabel);
    textGeneratorsFontPanel.add(Box.createHorizontalGlue());
    textGeneratorsFontPanel.add(Box.createHorizontalStrut(scale(10)));
    textGeneratorsFontPanel.add(new JLabel(translate("textGeneratorsLineHeight") + ":"));
    textGeneratorsFontPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorLineHeightField.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(22)));
    textGeneratorLineHeightField.addActionListener(event -> {
      if (selectedTextGenerator != null) {
        try {
          selectedTextGenerator.setLineHeightFactor(validatedFloatFromString(
            textGeneratorLineHeightField.getText(), translate("textGeneratorsLineHeight") + ":", 0.5f, 5.0f
          ));
          registerUnsavedChange();
        } catch (Exception e) {
          errorDialog(e);
        }
      }
    });
    GUIUtils.addBackgroundSetter(textGeneratorLineHeightField);
    textGeneratorsFontPanel.add(textGeneratorLineHeightField);
    textGeneratorsFontPanel.add(Box.createHorizontalStrut(scale(5)));
    textGeneratorsPanel.add(textGeneratorsFontPanel);
    textGeneratorsPanel.add(Box.createVerticalStrut(scale(5)));
    textGeneratorContentTextArea.setLineWrap(true);
    textGeneratorContentTextArea.setWrapStyleWord(true);
    textGeneratorContentTextArea.setToolTipText(translate("textGeneratorsContentTooltip"));
    JScrollPane textGeneratorContentScrollPane = new JScrollPane(textGeneratorContentTextArea);
    textGeneratorsPanel.add(
      createScaledLabeledWidgetPanel(textGeneratorContentScrollPane, translate("textGeneratorsContent"), 22, 70)
    );
    textGeneratorContentTextArea.getDocument().addDocumentListener(new DocumentListener() {

      @Override public void changedUpdate(DocumentEvent e) {
        if (selectedTextGenerator != null) {
          selectedTextGenerator.setContent(textGeneratorContentTextArea.getText());
          registerUnsavedChange();
        }
      }

      @Override public void removeUpdate(DocumentEvent e) {
        if (selectedTextGenerator != null) {
          selectedTextGenerator.setContent(textGeneratorContentTextArea.getText());
          registerUnsavedChange();
        }
      }

      @Override public void insertUpdate(DocumentEvent e) {
        if (selectedTextGenerator != null) {
          selectedTextGenerator.setContent(textGeneratorContentTextArea.getText());
          registerUnsavedChange();
        }
      }

    });
    textGeneratorsPanel.add(Box.createVerticalGlue());
    textGeneratorsPanel.add(Box.createVerticalStrut(scale(5)));
    textGeneratorsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    setEnabledTextGeneratorWidgets(false);

    // ******************************************************************************************************
    // Main panel:
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
    signaturesViewerButton = new JButton(translate("signaturesViewer"));
    signaturesViewerButton.setActionCommand(ACTION_SHOW_HIDE_SIGNATURES_VIEWER);
    signaturesViewerButton.addActionListener(this);
    signaturesViewerButton.setToolTipText(translate("buttonTooltipSignaturesViewer"));
    buttonPanel.add(Box.createHorizontalStrut(scale(10)));
    buttonPanel.add(signaturesViewerButton);
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
    statusPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, scale(24)));
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
    if (!enabled) {
      documentNameTextField.setText("");
      documentIdentifierTextField.setText("");
      documentPathTextField.setText("");
      documentCommentTextArea.setText("");
    }
  }

  private void setEnabledTransformsWidgets
  (boolean enabled)
  {
    transformSetNameTextField.setEnabled(enabled);
    transformSetPageRangesTextField.setEnabled(enabled);
    transformSetCommentTextArea.setEnabled(enabled);
    if (!enabled) {
      transformSetNameTextField.setText("");
      transformSetPageRangesTextField.setText("");
      transformSetCommentTextArea.setText("");
    }
  }

  private void setEnabledTextGeneratorWidgets
  (boolean enabled)
  {
    textGeneratorNameTextField.setEnabled(enabled);
    textGeneratorPageRangesTextField.setEnabled(enabled);
    textGeneratorCommentTextArea.setEnabled(enabled);
    textGeneratorHorizontalOffsetField.setEnabled(enabled);
    textGeneratorVerticalOffsetField.setEnabled(enabled);
    textGeneratorContentTextArea.setEnabled(enabled);
    if (!enabled) {
      textGeneratorNameTextField.setText("");
      textGeneratorPageRangesTextField.setText("");
      textGeneratorCommentTextArea.setText("");
      textGeneratorHorizontalOffsetField.setText("");
      textGeneratorVerticalOffsetField.setText("");
      textGeneratorContentTextArea.setText("");
    }
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

  private void resetStatusMessage
  ()
  {
    setStatusMessage("");
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

  @Override
  public void handlePageRangesChange
  (String pageRangesText)
  {
    execute(() -> {
      String errorMessage = null;
      try {
        // Pre-parse and colour the background to the page ranges text area red, every time we encounter an error,
        // and white whenever it's good.  This will avoid an error dialog popping up from the rendering thread when
        // the viewer is open:
        getBook().getPages(pageRangesText);
      } catch (Throwable t) {
        errorMessage = t.getMessage();
        t.printStackTrace(System.err);
      } finally {
        if (errorMessage != null) {
          pageRangesTextArea.setBackground(Color.PINK);
          pageRangesApplyButton.setBackground(Color.PINK);
          pageRangesTextValid = false;
          setStatusMessage(errorMessage);
        } else {
          pageRangesTextArea.setBackground(Color.WHITE);
          pageRangesApplyButton.setBackground(Color.CYAN);
          pageRangesTextValid = true;
          setStatusMessage("Page range text is valid");
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

  void messageDialog
  (String message)
  {
    JOptionPane.showMessageDialog(this, message);
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
        transformSetsComboBox.setSelectedItem(transformSet);
        selectedTransformSet = transformSet;
        populateTransformSetWidgets();
        updateTransformControls(transformSet);
      } finally {
        bookLock.unlock();
      }
    });
  }

  private void newTransformSetForCurrentPage
  ()
  {
    execute(() -> {
      bookLock.lock();
      try {
        Book book = getBook();
        if (viewerActive) {
          PageRef pageRef = book.getPageRef(viewer.getCurrentPageNumber());
          PageRange pageRange = pageRef.getSinglePageRange();
          String name = translate("auto") + " " + pageRef.getSourceDocument().getId() + ":" + pageRef.getPageNumber();
          TransformSet transformSet = book.findTransformSet(name);
          if (transformSet == null) {
            transformSet = new TransformSet();
            transformSet.setName(name);
            transformSet.addPageRange(pageRange);
            transformSet.setComment(translate("autoGeneratedTransform"));
            book.getTransformSets().add(transformSet);
            registerUnsavedChange();
            populateTransformSetComboBox();
          }
          transformSetsComboBox.setSelectedItem(transformSet);
          selectedTransformSet = transformSet;
          populateTransformSetWidgets();
          updateTransformControls(transformSet);
        }
      } catch (Exception e) {
        errorDialog(e);
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
        populateTransformSetWidgets();
        updateTransformControls(null);
      }
    });
  }

  private void newTextGenerator
  ()
  {
    execute(() -> {
      bookLock.lock();
      try {
        Book book = getBook();
        TextGenerator textGenerator = new TextGenerator();
        textGenerator.setName(translate("new"));
        book.getTextGenerators().add(textGenerator);
        registerUnsavedChange();
        populateTextGeneratorsComboBox();
        textGeneratorsComboBox.setSelectedItem(textGenerator);
        selectedTextGenerator = textGenerator;
        populateTextGeneratorWidgets();
      } finally {
        bookLock.unlock();
      }
    });
  }

  private void upTextGenerator
  (TextGenerator textGenerator)
  {
    execute(() -> {
      getBook().upTextGenerator(textGenerator);
      registerUnsavedChange();
      populateTextGeneratorsComboBox();
      textGeneratorsComboBox.setSelectedItem(textGenerator);
      selectedTextGenerator = textGenerator;
    });
  }

  private void downTextGenerator
  (TextGenerator textGenerator)
  {
    execute(() -> {
      getBook().downTextGenerator(textGenerator);
      registerUnsavedChange();
      populateTextGeneratorsComboBox();
      textGeneratorsComboBox.setSelectedItem(textGenerator);
      selectedTextGenerator = textGenerator;
    });
  }

  private void deleteTextGenerator
  (TextGenerator textGenerator)
  {
    execute(() -> {
      getBook().removeTextGenerator(textGenerator);
      registerUnsavedChange();
      populateTextGeneratorsComboBox();
      if (textGeneratorsComboBox.getItemCount() > 0) {
        textGeneratorsComboBox.setSelectedItem(0);
        selectedTextGenerator = (TextGenerator) textGeneratorsComboBox.getSelectedItem();
        populateTextGeneratorWidgets();
      } else {
        selectedTextGenerator = null;
        setEnabledTextGeneratorWidgets(false);
        populateTextGeneratorWidgets();
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
        fileChooser.setFileFilter(binderatorFileFilter);
      }
      fileChooser.setFileFilter(binderatorFileFilter);
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
    } finally {
      postLoadCleanup();
    }
  }

  private void postLoadCleanup
  ()
  {
    execute(this::deregisterUnsavedChanges);
    // Add any code to finalise / clean up initialisation of the new Book here...
    // Explicitly set the page ranges source again, since it can be updated to an empty string out of order by
    // the deserialisation of a source document id:
    execute(() -> {
      getBook().setPageRangesSource(pageRangesTextArea.getText());
      pageRangesApplyButton.setBackground(pageRangesApplyButtonDefaultColor);
    });
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
        fileChooser.setFileFilter(binderatorFileFilter);
      }
      fileChooser.setFileFilter(binderatorFileFilter);
      int fileChooserRC = fileChooser.showOpenDialog(this);
      if (fileChooserRC == JFileChooser.APPROVE_OPTION) {
        chosenFile = fileChooser.getSelectedFile();
        if (!chosenFile.getPath().endsWith(".sob")) {
          chosenFile = new File(chosenFile.getPath() + ".sob");
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
    okButton.addActionListener(e -> aboutDialog.setVisible(false));
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
    } catch (Exception ignored) {}
    System.exit(0);
  }

  private interface RangedFloatAccessor {

    RangedFloat get
    ();

  }

  private JTextField createRangedFloatField
  (RangedFloatAccessor rangedFloatAccessor, ThrowingConsumer<JTextField, Exception> action)
  {
    JTextField field = new JTextField();
    GUIUtils.addBackgroundSetter(field);
    field.addKeyListener(new KeyAdapter() {

      @Override
      public void keyReleased(KeyEvent ke) {
        String typed = field.getText();
        if (!typed.matches("-?\\d+(\\.\\d+)?") || typed.length() > 5) {
          setStatusMessage(rangedFloatAccessor.get().getName() + " value \"" + typed +"\" is invalid");
          field.setBackground(Color.PINK);
          return;
        }
        float value = Float.parseFloat(typed);
        rangedFloatAccessor.get().setValue(value);
        resetStatusMessage();
      }

    });
    field.setText("" + rangedFloatAccessor.get().getValue());
    field.setMaximumSize(new Dimension(scale(100), scale(22)));
    field.addActionListener(e -> execute(() -> {
      try {
        action.apply(field);
        registerUnsavedChange();
      } catch (Exception ex) {
        errorDialog(ex);
      }
    }));
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
        updateTextGeneratorsTab();
        haveUnsavedChanges = false;
        notifyViewerBookChange();
      },
      true
    );
  }

  Pattern floatPattern = Pattern.compile("-?\\d+(\\.\\d+)?([eE][+\\-]?\\d+)?");
  Pattern intPattern = Pattern.compile("-?\\d+");

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

  private int validatedIntFromString
  (String source, String name, int min, int max)
  throws Exception
  {
    if (intPattern.matcher(source).matches()) {
      int value = Integer.parseInt(source);
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
    leftMarginRatioField.setText("" + book.getLeftMarginRatio().getValue());
    rightMarginRatioField.setText("" + book.getRightMarginRatio().getValue());
    bottomMarginRatioField.setText("" + book.getBottomMarginRatio().getValue());
    topMarginRatioField.setText("" + book.getTopMarginRatio().getValue());
    signatureSheetsComboBox.setSelectedItem(book.getSignatureSheets());
    minimiseLastSignatureCheckbox.setSelected(book.isMinimisingLastSignature());
    spineOffsetRatioField.setText("" + book.getSpineOffsetRatio().getValue());
    edgeOffsetRatioField.setText("" + book.getEdgeOffsetRatio().getValue());
    double trimLinesHorizontalRatio = book.getTrimLinesHorizontalRatio() != null ? book.getTrimLinesHorizontalRatio().getValue() : 0.0f;
    double trimLinesVerticalRatio = book.getTrimLinesVerticalRatio() != null ? book.getTrimLinesVerticalRatio().getValue() : 0.0f;
    signatureTrimLinesHorizontalRatioField.setText("" + trimLinesHorizontalRatio);
    signatureTrimLinesVerticalRatioField.setText("" + trimLinesVerticalRatio);
    signatureTrimLinesComboBox.setSelectedItem(book.getTrimLinesType());
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
      documentIdentifierTextField.setText("");
      setEnabledSourceDocumentsWidgets(false);
    }
    String pageRangesSource = book.getPageRangesSource();
    if (pageRangesSource != null) {
      pageRangesTextArea.setText(pageRangesSource);
      execute(() -> {
        try {
          book.getPages();
        } catch (Throwable t) {
          t.printStackTrace(System.err);
          errorDialog(t);
        }
      });
    } else {
      pageRangesTextArea.setText("");
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
      documentIdentifierTextField.setText(selectedDocument.getId());
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
    } else {
      setEnabledTransformsWidgets(false);
    }
  }

  private void updateTextGeneratorsTab
  ()
  {
    populateTextGeneratorsComboBox();
    if (book.getTextGenerators().size() > 0) {
      textGeneratorsComboBox.setSelectedIndex(-1);
      textGeneratorsComboBox.setSelectedIndex(0);
      setEnabledTextGeneratorWidgets(true);
    } else {
      textGeneratorNameTextField.setText("");
      textGeneratorCommentTextArea.setText("");
      textGeneratorContentTextArea.setText("");
      textGeneratorPageRangesTextField.setText("");
      textGeneratorHorizontalOffsetField.setText("");
      textGeneratorVerticalOffsetField.setText("");
      textGeneratorAlignmentComboBox.setSelectedIndex(0);
      textGeneratorFontLabel.setText("");
      setEnabledTextGeneratorWidgets(false);
    }
  }

  private void populateTextGeneratorWidgets
  ()
  {
    selectedTextGenerator = (TextGenerator)textGeneratorsComboBox.getSelectedItem();
    if (selectedTextGenerator != null) {
      textGeneratorNameTextField.setText(selectedTextGenerator.getName());
      textGeneratorPageRangesTextField.setText(selectedTextGenerator.getPageRangesString());
      textGeneratorCommentTextArea.setText(selectedTextGenerator.getComment());
      Float horizontalOffset = selectedTextGenerator.getHorizontalOffset();
      textGeneratorHorizontalOffsetField.setText("" + (horizontalOffset != null ? horizontalOffset : ""));
      Float verticalOffset = selectedTextGenerator.getVerticalOffset();
      textGeneratorVerticalOffsetField.setText("" + (verticalOffset != null ? verticalOffset : ""));
      textGeneratorAlignmentComboBox.setSelectedItem(selectedTextGenerator.getAlignment());
      textGeneratorContentTextArea.setText(selectedTextGenerator.getContent());
      if (selectedTextGenerator.getFont() != null) {
        textGeneratorFontLabel.setText(
          selectedTextGenerator.getFont().getFontName() + " " + selectedTextGenerator.getFont().getSize()
        );
      } else {
        textGeneratorFontLabel.setText("");
      }
      setEnabledTextGeneratorWidgets(true);
    } else {
      setEnabledTextGeneratorWidgets(false);
    }
  }

  private void populateTextGeneratorsComboBox
  ()
  {
    DefaultComboBoxModel<TextGenerator> model = new DefaultComboBoxModel<>();
    Book book = getBook();
    for (TextGenerator textGenerator : book.getTextGenerators()) {
      model.addElement(textGenerator);
    }
    textGeneratorsComboBoxModel = model;
    textGeneratorsComboBox.setModel(model);
    textGeneratorsComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        if (!Objects.equals(selectedTextGenerator, textGeneratorsComboBox.getSelectedItem())) {
          populateTextGeneratorWidgets();
        }
      }
    });
  }

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

  private void deregisterUnsavedChanges
  ()
  {
    haveUnsavedChanges = false;
  }

  private void notifyViewerBookChange
  ()
  {
    if (viewerRenderingThread != null) {
      viewerRenderingThread.signalTask();
    }
  }


  @Override
  public void actionPerformed
  (ActionEvent event)
  {
    try {
      switch (event.getActionCommand()) {
        case ACTION_EXIT -> gracefulExit();
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
        case ACTION_SHOW_HIDE_SIGNATURES_VIEWER -> {
          boolean newViewer = false;
          if (signaturesViewer == null) {
            signaturesViewer = new ICEViewer(translate("signaturesViewerTitle"), 1400, 1024, this);
            newViewer = true;
          }
          if (viewerRenderingThread == null) {
            viewerRenderingThread = new ViewerRenderingThread();
            viewerRenderingThread.start();
          }
          if (signaturesViewerButton.isSelected()) {
            signaturesViewerButton.setSelected(false);
            signaturesViewerActive = false;
            signaturesViewer.setVisible(false);
          } else {
            signaturesViewerButton.setSelected(true);
            signaturesViewerActive = true;
            if (newViewer) {
              viewerRenderingThread.signalTask();
            }
            signaturesViewer.setVisible(true);
          }
        }
      }
    } catch (Throwable t) {
      errorDialog(t);
    }
  }

  @Override
  public void onICEViewerClose
  (ICEViewer instance)
  {
    if (instance == viewer) {
      if (viewerButton.isSelected()) {
        viewerButton.setSelected(false);
      }
    } else if (instance == signaturesViewer) {
      if (signaturesViewerButton.isSelected()) {
        signaturesViewerButton.setSelected(false);
      }
    }
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
      frame.postLoadCleanup();
    } else {
      frame.execute(() -> { frame.haveUnsavedChanges = false; });
    }
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
