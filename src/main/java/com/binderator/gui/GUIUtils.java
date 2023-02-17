package com.binderator.gui;


import com.binderator.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


@SuppressWarnings("unused")
public class GUIUtils {

  private static Color changeBackgroundColour = new Color(0xccffff);

  private static float scaleFactor = 1.0f;

  @SuppressWarnings("unused")
  public static void setChangeBackgroundColour
  (Color color)
  {
    changeBackgroundColour = color;
  }

  public static void setScaleFactor
  (float scaleFactor)
  {
    GUIUtils.scaleFactor = scaleFactor;
  }

  public float getScale
  ()
  {
    return scaleFactor;
  }

  public static int scale
  (int value)
  {
    return (int) scaleFactor *value;
  }

  public static ImageIcon scaleImageIcon
  (ImageIcon icon)
  {
    if (scaleFactor == 1.0f) {
      return icon;
    }
    Image image = icon.getImage();
    Image newImage = image.getScaledInstance(
        scale(icon.getIconWidth()), scale(icon.getIconHeight()), java.awt.Image.SCALE_SMOOTH
    );
    return new ImageIcon(newImage);
  }

  public static void addBackgroundSetter
  (JTextField field)
  {
    field.addKeyListener(new KeyListener() {

      @Override
      public void keyTyped(KeyEvent e) {}

      @Override
      public void keyPressed(KeyEvent e) {
        if ((int)e.getKeyChar() == 10) {
          field.setBackground(null);
        } else {
          field.setBackground(changeBackgroundColour);
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {}

    });
  }

  public static void setTooltip
  (String tooltip, JComponent ... components)
  {
    for (JComponent component : components) {
      component.setToolTipText(tooltip);
    }
  }

  public static void setTooltipByKey
  (String tooltipKey, JComponent ... components)
  {
    for (JComponent component : components) {
      String translatedTooltip = Translations.translate(tooltipKey);
    }

  }

}
