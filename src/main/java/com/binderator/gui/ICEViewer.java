package com.binderator.gui;


import org.icepdf.ri.common.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;


/**
 * Use ICEPDF to create an inline viewer of the current PDF.
 */
public class ICEViewer extends JFrame {


  public interface CloseListener {

    void onICEViewerClose
    ();

  }


  SwingController controller = new SwingController();

  JPanel viewerComponentPanel;

  public static final int DEFAULT_WIDTH = 800;
  public static final int DEFAULT_HEIGHT = 1024;

  public ICEViewer
  ()
  {
    this("PDF Viewer", DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
  }

  public ICEViewer
  (String title, int width, int height, CloseListener closeListener)
  {
    super(title);
    SwingViewBuilder factory = new SwingViewBuilder(controller);
    JPanel viewerComponentPanel = factory.buildViewerPanel();
    ComponentKeyBinding.install(controller, viewerComponentPanel);
    controller.getDocumentViewController().setAnnotationCallback(
      new org.icepdf.ri.common.MyAnnotationCallback(controller.getDocumentViewController())
    );
    getContentPane().add(viewerComponentPanel);
    setMinimumSize(new java.awt.Dimension(width, height));
    setPreferredSize(new java.awt.Dimension(width, height));
    setSize(new java.awt.Dimension(width, height));
    pack();
    addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing
      (WindowEvent e)
      {
        if (closeListener != null) {
          closeListener.onICEViewerClose();
        }
      }

    });
  }

  public void setContent
  (byte[] pdfContent)
  {
    if ((pdfContent != null) && (pdfContent.length > 0)) {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfContent);
      controller.openDocument(inputStream, "inline", "inline");
    } else {
      controller.closeDocument();
    }
  }

  private static void doMain
  (String[] args)
  throws Exception
  {
    ICEViewer viewer = new ICEViewer();
    viewer.setVisible(true);
  }

  public static void main
    (String[] args) {
    try {
      doMain(args);
    } catch (Throwable t) {
      System.err.println("Caught exception in main(): " + t.getMessage());
      t.printStackTrace(System.err);
    }
  }

}
