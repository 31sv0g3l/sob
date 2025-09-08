package com.binderator.gui;

import com.binderator.util.*;
import javax.annotation.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;

import static com.binderator.gui.GUIUtils.scale;
import static com.binderator.util.Translations.translate;

@SuppressWarnings("unused")
public class RangedFloatSlider extends JPanel {

  @Serial
  private static final long serialVersionUID = 3762170917578342642L;
  RangedFloat rangedFloat;
  JTextField field = new JTextField();
  JTextField lowField = new JTextField();
  JTextField highField = new JTextField();
  JSlider slider;
  private boolean settingValue = false;


  public RangedFloatSlider()
  {
    this(null, scale(22), scale(70));
  }

  public RangedFloatSlider(RangedFloat rangedFloat)
  {
    this(rangedFloat, scale(22), scale(70));
  }

  public RangedFloatSlider(RangedFloat rangedFloat, UnsavedChangeListener unsavedChangeListener)
  {
    this(rangedFloat, scale(22), scale(70), unsavedChangeListener);
  }

  public RangedFloatSlider(@Nullable RangedFloat rangedFloat, int widgetHeight, int fieldWidth)
  {
    this(rangedFloat, widgetHeight, fieldWidth, null);
  }

  public RangedFloatSlider(
    @Nullable RangedFloat rangedFloat, int widgetHeight, int fieldWidth, UnsavedChangeListener unsavedChangeListener
  )
  {
    super();
    this.rangedFloat = rangedFloat;
    // Bottom horizontal panel for the widget, left justified:
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    field.setMaximumSize(new Dimension(fieldWidth, widgetHeight));
    field.setPreferredSize(new Dimension(new Dimension(fieldWidth, widgetHeight)));
    add(field);
    add(Box.createHorizontalStrut(scale(5)));
    add(new JLabel(":"));
    add(Box.createHorizontalStrut(scale(5)));
    lowField.setMaximumSize(new Dimension(fieldWidth, widgetHeight));
    lowField.setPreferredSize(new Dimension(new Dimension(fieldWidth, widgetHeight)));
    slider = new JSlider();
    highField.setMaximumSize(new Dimension(fieldWidth, widgetHeight));
    highField.setPreferredSize(new Dimension(new Dimension(fieldWidth, widgetHeight)));
    add(lowField);
    add(Box.createHorizontalStrut(scale(5)));
    add(slider);
    add(Box.createHorizontalStrut(scale(5)));
    add(highField);
    setRangedFloat(rangedFloat);
    slider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e)
      {
        if (RangedFloatSlider.this.rangedFloat != null) {
          int sliderWidth = slider.getMaximum() - slider.getMinimum();
          float lowValue = RangedFloatSlider.this.rangedFloat.getLowValue();
          float highValue = RangedFloatSlider.this.rangedFloat.getHighValue();
          float value = lowValue + (highValue - lowValue) * ((float) slider.getValue()) / (float) sliderWidth;
          if (!settingValue) {
            RangedFloatSlider.this.rangedFloat.setValue(value);
            if (unsavedChangeListener != null) unsavedChangeListener.registerUnsavedChange();
            setText();
          }
          settingValue = false;
        }
      }

    });
    field.addActionListener(event -> {
      String typed = field.getText();
      if (!typed.matches("-?[\\d]+(\\.[\\d]+)?") || typed.length() > 5) {
        return;
      }
      float value = Float.parseFloat(typed);
      settingValue = true;
      RangedFloatSlider.this.rangedFloat.setValue(value);
      if (unsavedChangeListener != null) unsavedChangeListener.registerUnsavedChange();
      resetFields();
    });
    GUIUtils.addBackgroundSetter(field);
    lowField.addActionListener(event -> {
      String typed = lowField.getText();
      if (!typed.matches("-?[\\d]+(\\.[\\d]+)?") || typed.length() > 5) {
        return;
      }
      float value = Float.parseFloat(typed);
      RangedFloatSlider.this.rangedFloat.setLowValue(value);
      if (unsavedChangeListener != null) unsavedChangeListener.registerUnsavedChange();
      resetFields();
    });
    GUIUtils.addBackgroundSetter(lowField);
    highField.addActionListener(event -> {
      String typed = highField.getText();
      if (!typed.matches("-?[\\d]+(\\.[\\d]+)?") || typed.length() > 5) {
        return;
      }
      float value = Float.parseFloat(typed);
      RangedFloatSlider.this.rangedFloat.setHighValue(value);
      if (unsavedChangeListener != null) unsavedChangeListener.registerUnsavedChange();
      resetFields();
    });
    GUIUtils.addBackgroundSetter(highField);
    resetSlider();
    field.setToolTipText(translate("rangedValueSliderTooltipValueField"));
    lowField.setToolTipText(translate("rangedValueSliderTooltipValueLowField"));
    highField.setToolTipText(translate("rangedValueSliderTooltipValueHighField"));
    slider.setToolTipText(translate("rangedValueSliderTooltipSlider"));
  }

  private void setText()
  {
    field.setText(String.format("%3.3f", rangedFloat.getValue()));
  }

  private void resetSlider()
  {
    if (rangedFloat != null) {
      float sliderWidth = slider.getMaximum() - slider.getMinimum();
      float initialRangeWidth = rangedFloat.getHighValue() - rangedFloat.getLowValue();
      int sliderValue = (int) (
        ((float)slider.getMinimum()) +
        sliderWidth * (rangedFloat.getValue() - rangedFloat.getLowValue()) / initialRangeWidth
      );
      slider.setValue(sliderValue);
    }
  }

  private void resetFields()
  {
    if (rangedFloat != null) {
      lowField.setText("" + rangedFloat.getLowValue());
      highField.setText("" + rangedFloat.getHighValue());
      setText();
      resetSlider();
    }
  }

  public void setRangedFloat(RangedFloat rangedFloat)
  {
    this.rangedFloat = rangedFloat;
    if (rangedFloat != null) {
      resetFields();
    }
  }

}
