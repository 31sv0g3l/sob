package com.binderator.gui;


import com.binderator.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;


@SuppressWarnings("unused")
public class GUIUtils {

  private static Color changeBackgroundColour = new Color(0xccffff);

  private static float scaleFactor = 1.0f;

  @SuppressWarnings("unused")
  public static void setChangeBackgroundColour(Color color)
  {
    changeBackgroundColour = color;
  }

  public static void setScaleFactor(float scaleFactor)
  {
    GUIUtils.scaleFactor = scaleFactor;
  }

  public float getScale()
  {
    return scaleFactor;
  }

  public static int scale(int value)
  {
    return (int) scaleFactor * value;
  }

  public static ImageIcon scaleImageIcon(ImageIcon icon)
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

  public static void addBackgroundSetter(JTextField field)
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

  public static void setTooltip(String tooltip, JComponent ... components)
  {
    for (JComponent component : components) {
      component.setToolTipText(tooltip);
    }
  }

  public static void setTooltipByKey(String tooltipKey, JComponent ... components)
  {
    for (JComponent component : components) {
      String translatedTooltip = Translations.translate(tooltipKey);
    }

  }

  public static void setStandardScale()
  {
    String os = System.getProperty("os.name");
    if (os.matches("Windows.*")) {
      setScaleFactor(1.0f);
    } else if (os.matches("Linux")) {
      float scaleFactor = 1.0f;
      // Check for Linux HiDPI settings
      String gtkScale = System.getenv("GDK_SCALE");
      if (gtkScale != null) {
        try {
          scaleFactor = Float.parseFloat(gtkScale);
        } catch (NumberFormatException e) {
          // fallback to default
        }
      }
      // Also check Toolkit DPI
      int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
      if (screenRes > 96) { // 96 is standard DPI
        scaleFactor = Math.max(scaleFactor, screenRes / 96.0f);
      }
      setScaleFactor(scaleFactor);
    } else {
      float scale = (float) java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
          .getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform().getScaleX();
      setScaleFactor(2.0f / scale);
    }
  }

}
