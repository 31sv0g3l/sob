package com.binderator.gui;

import com.binderator.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import static com.binderator.util.Translations.translate;
import static com.binderator.gui.GUIUtils.scale;

@SuppressWarnings({"unused", "SameParameterValue"})
public class OptionsDialog extends JDialog {

  @Serial
  private static final long serialVersionUID = -6419775665367426353L;
  public static final String OPTION_SHOW_PROGRESS = "showProgress";

  public OptionsDialog(JFrame parent)
  {
    super(parent, translate("options"), true);
    JPanel optionsPanel = (JPanel) getContentPane();
    optionsPanel.setBorder(new EmptyBorder(scale(5), scale(5), scale(5), scale(5)));
    optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

    // *******************************
    // Begin app-specific options code
    // *******************************

    // Interface options
    JPanel interfaceOptionsPanel = new JPanel();
    interfaceOptionsPanel.setBorder(BorderFactory.createTitledBorder(
      BorderUIResource.getBlackLineBorderUIResource(), translate("interfaceOptions")
    ));
    interfaceOptionsPanel.setLayout(new BoxLayout(interfaceOptionsPanel, BoxLayout.Y_AXIS));
    optionsPanel.add(interfaceOptionsPanel);
    // Row 1
    JPanel interfaceOptionsRow1 = new JPanel();
    interfaceOptionsRow1.setLayout(new BoxLayout(interfaceOptionsRow1, BoxLayout.X_AXIS));
    interfaceOptionsRow1.add(Box.createHorizontalGlue());
    interfaceOptionsRow1.add(createOptionCheckBox(OPTION_SHOW_PROGRESS, true));
    interfaceOptionsRow1.add(Box.createHorizontalStrut(scale(10)));
    interfaceOptionsRow1.add(Box.createHorizontalGlue());
    interfaceOptionsPanel.add(interfaceOptionsRow1);
    // Row 2
    JPanel interfaceOptionsRow2 = new JPanel();
    interfaceOptionsRow2.setLayout(new BoxLayout(interfaceOptionsRow2, BoxLayout.X_AXIS));
    interfaceOptionsRow2.add(Box.createHorizontalGlue());
    interfaceOptionsRow2.add(Box.createHorizontalGlue());
    interfaceOptionsPanel.add(interfaceOptionsRow2);
    interfaceOptionsPanel.add(Box.createVerticalGlue());

    // Overlay options
    JPanel overlayOptionsPanel = new JPanel();
    overlayOptionsPanel.setBorder(BorderFactory.createTitledBorder(
      BorderUIResource.getBlackLineBorderUIResource(), translate("overlayOptions")
    ));
    overlayOptionsPanel.setLayout(new BoxLayout(overlayOptionsPanel, BoxLayout.Y_AXIS));
    optionsPanel.add(overlayOptionsPanel);

    // Overlay Options Row 1
    JPanel overlayOptionsRow1 = new JPanel();
    overlayOptionsPanel.add(overlayOptionsRow1);
    overlayOptionsRow1.setLayout(new BoxLayout(overlayOptionsRow1, BoxLayout.X_AXIS));

    overlayOptionsRow1.add(Box.createHorizontalGlue());
    overlayOptionsRow1.add(new JLabel(translate("marginColour")));
    overlayOptionsRow1.add(Box.createHorizontalStrut(scale(5)));
    JButton marginColorChooserButton =
      createOptionColourChooser("marginColour", Color.BLUE, translate("marginColour"));
    marginColorChooserButton.setVisible(true);
    overlayOptionsRow1.add(marginColorChooserButton);

    overlayOptionsRow1.add(Box.createHorizontalGlue());
    overlayOptionsRow1.add(Box.createHorizontalStrut(scale(10)));
    overlayOptionsRow1.add(new JLabel(translate("pageNumbersColour")));
    overlayOptionsRow1.add(Box.createHorizontalStrut(scale(5)));
    JButton pageNumbersColorChooserButton =
      createOptionColourChooser("pageNumbersColour", Color.BLUE, translate("pageNumbersColour"));
    pageNumbersColorChooserButton.setVisible(true);
    overlayOptionsRow1.add(pageNumbersColorChooserButton);

    overlayOptionsRow1.add(Box.createHorizontalGlue());

    // Overlay Options Row 2
    JPanel overlayOptionsRow2 = new JPanel();
    overlayOptionsPanel.add(overlayOptionsRow2);
    overlayOptionsRow2.setLayout(new BoxLayout(overlayOptionsRow2, BoxLayout.X_AXIS));

    overlayOptionsRow2.add(Box.createHorizontalGlue());
    overlayOptionsRow2.add(Box.createHorizontalStrut(scale(10)));
    overlayOptionsRow2.add(new JLabel(translate("marginLineThickness")));
    overlayOptionsRow2.add(Box.createHorizontalStrut(scale(5)));
    overlayOptionsRow2.add(createOptionFloatField("marginLineThickness", 4, 1.0f, 20.0f, 1.5f));

    overlayOptionsRow2.add(Box.createHorizontalGlue());
    overlayOptionsRow2.add(Box.createHorizontalStrut(scale(10)));
    overlayOptionsRow2.add(new JLabel(translate("pageNumbersLineThickness")));
    overlayOptionsRow2.add(Box.createHorizontalStrut(scale(5)));
    overlayOptionsRow2.add(createOptionFloatField("pageNumbersLineThickness", 4, 1.0f, 20.0f, 1.5f));

    overlayOptionsRow2.add(Box.createHorizontalGlue());

    // Overlay Options Row 3
    JPanel overlayOptionsRow3 = new JPanel();
    overlayOptionsPanel.add(overlayOptionsRow3);
    overlayOptionsRow3.setLayout(new BoxLayout(overlayOptionsRow3, BoxLayout.X_AXIS));

    overlayOptionsRow3.add(Box.createHorizontalGlue());
    overlayOptionsRow3.add(Box.createHorizontalStrut(scale(10)));
    overlayOptionsRow3.add(new JLabel(translate("marginHorizontalDivisions")));
    overlayOptionsRow3.add(Box.createHorizontalStrut(scale(5)));
    overlayOptionsRow3.add(createOptionIntField("marginHorizontalDivisions", 4, 1, 100, 2));

    overlayOptionsRow3.add(Box.createHorizontalGlue());
    overlayOptionsRow3.add(Box.createHorizontalStrut(scale(10)));
    overlayOptionsRow3.add(new JLabel(translate("marginVerticalDivisions")));
    overlayOptionsRow3.add(Box.createHorizontalStrut(scale(5)));
    overlayOptionsRow3.add(createOptionIntField("marginVerticalDivisions", 4, 1, 100, 2));

    overlayOptionsRow3.add(Box.createHorizontalGlue());

    // *****************************
    // End app-specific options code
    // *****************************

    optionsPanel.add(Box.createVerticalGlue());
    optionsPanel.add(Box.createVerticalStrut(scale(10)));
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(Box.createHorizontalGlue());
    JButton okButton = new JButton(translate("done"));
    okButton.addActionListener(e -> setVisible(false));
    buttonPanel.add(okButton);
    buttonPanel.add(Box.createHorizontalGlue());
    optionsPanel.add(Box.createVerticalGlue());
    optionsPanel.add(buttonPanel);
    optionsPanel.add(Box.createVerticalStrut(scale(10)));
    setSize(new Dimension(scale(500), scale(400)));
    setPreferredSize(new Dimension(scale(500), scale(400)));
    setMinimumSize(new Dimension(scale(500), scale(400)));
    setLocationRelativeTo(this);
  }

  protected void handleException(Throwable t)
  {
    System.err.println("Exception in OptionsDialog: " + t.getMessage());
  }

  private JCheckBox createOptionCheckBox(String optionKey, boolean defaultValue)
  {
    // JCheckBox checkBox = new JCheckBox(translate(optionKey));
    JCheckBox checkBox = new JCheckBox();
    checkBox.setAction(new AbstractAction() {

      @Serial
      private static final long serialVersionUID = 1799173232543533939L;

      @Override
      public void actionPerformed
      (ActionEvent event)
      {
        JCheckBox checkBox = (JCheckBox)event.getSource();
        try {
          if (checkBox.isSelected()) {
            InitFile.instance().set(optionKey, "t");
          } else {
            InitFile.instance().set(optionKey, "f");
          }
        } catch (Exception e) {
          handleException(e);
        }
      }

    });
    checkBox.setText(translate(optionKey));
    try {
      String initValue = (String) InitFile.instance().get(optionKey);
      checkBox.setSelected(initValue != null ? initValue.equals("t") : defaultValue);
    } catch (Exception e) {
      handleException(e);
    }
    return checkBox;
  }

  private interface TextConverter {

    Object convertText(String text) throws Exception;

  }

  private JTextField createOptionTextField(String optionKey, int height)
  {
    return createOptionTextField(optionKey, height, null, "");
  }

  private JTextField createOptionTextField
  (String optionKey, int columns, TextConverter converter, String defaultValue)
  {
    JTextField textField = new JTextField(columns);
    textField.setColumns(columns);
    textField.addActionListener(event -> {
      try {
        String text = textField.getText();
        Object value = (converter != null) ? converter.convertText(text) : text;
        InitFile.instance().set(optionKey, value);
      } catch (Exception e) {
        handleException(e);
      }
    });
    textField.setSize(new Dimension(-1, 24));
    textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, scale(24)));
    GUIUtils.addBackgroundSetter(textField);
    try {
      Object initValueObject = InitFile.instance().get(optionKey);
      String initValue = initValueObject != null ? initValueObject.toString() : defaultValue;
      textField.setText(initValue);
    } catch (Exception e) {
      handleException(e);
    }
    return textField;
  }

  private JTextField createOptionIntField(
    String optionKey, int columns, Integer minValue, Integer maxValue, Integer defaultValue
  )
  {
    TextConverter converter = text -> {
      int value = Integer.parseInt(text);
      if ((minValue != null) && (value < minValue)) {
        throw new Exception("Integer field value (" + value + ") less than\nminimum value (" + minValue + ")");
      }
      if ((maxValue != null) && (value > maxValue)) {
        throw new Exception("Integer field value (" + value + ") greater than\nmaximum value (" + maxValue + ")");
      }
      return value;
    };
    return createOptionTextField(
      optionKey, columns, converter, "" + (defaultValue != null ? defaultValue : "")
    );
  }

  private JTextField createOptionFloatField(
    String optionKey, int columns, Float minValue, Float maxValue, Float defaultValue
  )
  {
    TextConverter converter = text -> {
      float value = Float.parseFloat(text);
      if ((minValue != null) && (value < minValue)) {
        throw new Exception("Floating point value (" + value + ") less than\nminimum value (" + minValue + ")");
      }
      if ((maxValue != null) && (value > maxValue)) {
        throw new Exception("Floating point value (" + value + ") greater than\nmaximum value (" + maxValue + ")");
      }
      return value;
    };
    return createOptionTextField(optionKey, columns, converter, (defaultValue != null ? "" + defaultValue : ""));
  }

  private JButton createOptionColourChooser(String optionKey, Color defaultColor, String title)
  {
    JColorChooser chooser = new JColorChooser();
    JButton button = new JButton();
    chooser.setColor(defaultColor);
    button.setBackground(defaultColor);
    chooser.getSelectionModel().addChangeListener(event -> {
      Color color = chooser.getColor();
      try {
        InitFile.instance().set(optionKey, color);
        button.setBackground(color);
      } catch (Exception e) {
        handleException(e);
      }
      System.out.println(color);
    });
    button.setMaximumSize(new Dimension(22, 22));
    button.setPreferredSize(new Dimension(22, 22));
    button.addActionListener(e -> {
      JDialog dialog = JColorChooser.createDialog(null, "Color Chooser",
        true, chooser, null, null);
      dialog.setVisible(true);  chooser.setVisible(true);
    });
    try {
      Object initValueObject = InitFile.instance().get(optionKey);
      Color initColorValue = initValueObject != null ? (Color)initValueObject : defaultColor;
      button.setBackground(initColorValue);
    } catch (Exception e) {
      handleException(e);
    }
    return button;
  }

}
