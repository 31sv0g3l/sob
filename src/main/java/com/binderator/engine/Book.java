package com.binderator.engine;


import com.binderator.util.*;
import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;


import static com.binderator.util.Translations.translate;


@SuppressWarnings("unused")
public class Book implements Serializable {

  @Serial
  private static final long serialVersionUID = 8229737525528675891L;

  public interface StatusListener {

    void printBookStatus
    (String statusString);

    void handleBookException
    (Exception e);

    void handleBookProgressLabel
    (String progressLabel);

    void handleBookProgress
    (float progress, float maxProgress);

    void handlePageRangesChange
    (String pageRangesText);

  }

  public enum PaperSize {
    LETTER, NOTE, LEGAL, TABLOID, EXECUTIVE, POSTCARD, A0, A1, A3, A4, A5, A6, A7, A8, A9, B0, B1, B2, B3,
    B4, B6, B7, B8, B9, B10, ARCH_E, ARCH_C, ARCH_B, ARCH_A, FLSA, FLSE, HALF_LETTER, _11X17, ID_1, ID_2,
    ID_3, LEDGER, CROWN_QUARTO, LARGE_CROWN_QUARTO, DEMY_QUARTO, ROYAL_QUARTO, CROWN_OCTAVO, LARGE_CROWN_OCTAVO,
    DEMY_OCTAVO, ROYAL_OCTAVO, SMALL_PAPERBACK, PENGUIN_SMALL_PAPERBACK, PENGUIN_LARGE_PAPERBACK
  }

  private static class SerializableRectangle implements Serializable {

    @Serial
    private static final long serialVersionUID = -612651010568345409L;
    float llx, lly, urx, ury;

    public SerializableRectangle
    (float llx, float lly, float urx, float ury)
    {
      this.llx = llx;
      this.lly = lly;
      this.urx = urx;
      this.ury = ury;
    }

    public SerializableRectangle
    (SerializableRectangle rectangle)
    {
      this.llx = rectangle.llx;
      this.lly = rectangle.lly;
      this.urx = rectangle.urx;
      this.ury = rectangle.ury;
    }

    public SerializableRectangle
    (Rectangle rectangle)
    {
      this.llx = rectangle.getLeft();
      this.lly = rectangle.getBottom();
      this.urx = rectangle.getRight();
      this.ury = rectangle.getTop();
    }

    public com.lowagie.text.Rectangle getRectangle
    ()
    {
      return new Rectangle(llx, lly, urx, ury);
    }

    public String toString
    ()
    {
      return "Rectangle(" + llx + ", " + lly + ", " + urx + ", " + ury + ")";
    }

  }


  private static final Map<PaperSize, Float[]> paperSizes;

  static {
    paperSizes = new HashMap<>();
    paperSizes.put(PaperSize.LETTER, new Float[]{612f, 792f});
    paperSizes.put(PaperSize.NOTE, new Float[]{540f, 720f});
    paperSizes.put(PaperSize.LEGAL, new Float[]{612f, 1008f});
    paperSizes.put(PaperSize.TABLOID, new Float[]{792f, 1224f});
    paperSizes.put(PaperSize.EXECUTIVE, new Float[]{522f, 756f});
    paperSizes.put(PaperSize.POSTCARD, new Float[]{283f, 416f});
    paperSizes.put(PaperSize.A0, new Float[]{2384f, 3370f});
    paperSizes.put(PaperSize.A1, new Float[]{1684f, 2384f});
    paperSizes.put(PaperSize.A3, new Float[]{842f, 1191f});
    paperSizes.put(PaperSize.A4, new Float[]{595f, 842f});
    paperSizes.put(PaperSize.A5, new Float[]{420f, 595f});
    paperSizes.put(PaperSize.A6, new Float[]{297f, 420f});
    paperSizes.put(PaperSize.A7, new Float[]{210f, 297f});
    paperSizes.put(PaperSize.A8, new Float[]{148f, 210f});
    paperSizes.put(PaperSize.A9, new Float[]{105f, 148f});
    paperSizes.put(PaperSize.B0, new Float[]{2834f, 4008f});
    paperSizes.put(PaperSize.B1, new Float[]{2004f, 2834f});
    paperSizes.put(PaperSize.B2, new Float[]{1417f, 2004f});
    paperSizes.put(PaperSize.B3, new Float[]{1000f, 1417f});
    paperSizes.put(PaperSize.B4, new Float[]{708f, 1000f});
    paperSizes.put(PaperSize.B6, new Float[]{54f, 498f});
    paperSizes.put(PaperSize.B7, new Float[]{249f, 354f});
    paperSizes.put(PaperSize.B8, new Float[]{175f, 249f});
    paperSizes.put(PaperSize.B9, new Float[]{124f, 175f});
    paperSizes.put(PaperSize.B10, new Float[]{87f, 124f});
    paperSizes.put(PaperSize.ARCH_E, new Float[]{2592f, 3456f});
    paperSizes.put(PaperSize.ARCH_C, new Float[]{1296f, 1728f});
    paperSizes.put(PaperSize.ARCH_B, new Float[]{864f, 1296f});
    paperSizes.put(PaperSize.ARCH_A, new Float[]{648f, 864f});
    paperSizes.put(PaperSize.FLSA, new Float[]{612f, 936f});
    paperSizes.put(PaperSize.FLSE, new Float[]{648f, 936f});
    paperSizes.put(PaperSize.HALF_LETTER, new Float[]{396f, 612f});
    paperSizes.put(PaperSize._11X17, new Float[]{792f, 1224f});
    paperSizes.put(PaperSize.ID_1, new Float[]{242.65f, 153f});
    paperSizes.put(PaperSize.ID_2, new Float[]{297f, 210f});
    paperSizes.put(PaperSize.ID_3, new Float[]{354f, 249f});
    paperSizes.put(PaperSize.LEDGER, new Float[]{1224f, 792f});
    paperSizes.put(PaperSize.CROWN_QUARTO, new Float[]{535f, 697f});
    paperSizes.put(PaperSize.LARGE_CROWN_QUARTO, new Float[]{569f, 731f});
    paperSizes.put(PaperSize.DEMY_QUARTO, new Float[]{620f, 782f});
    paperSizes.put(PaperSize.ROYAL_QUARTO, new Float[]{671f, 884f});
    paperSizes.put(PaperSize.CROWN_OCTAVO, new Float[]{348f, 527f});
    paperSizes.put(PaperSize.LARGE_CROWN_OCTAVO, new Float[]{365f, 561f});
    paperSizes.put(PaperSize.DEMY_OCTAVO, new Float[]{391f, 612f});
    paperSizes.put(PaperSize.ROYAL_OCTAVO, new Float[]{442f, 663f});
    paperSizes.put(PaperSize.SMALL_PAPERBACK, new Float[]{314f, 504f});
    paperSizes.put(PaperSize.PENGUIN_SMALL_PAPERBACK, new Float[]{314f, 513f});
    paperSizes.put(PaperSize.PENGUIN_LARGE_PAPERBACK, new Float[]{365f, 561f});
  }

  enum SignatureFormat {
    STANDARD
  }

  //   2 standard paperback sizes and full paper size
  private static final Map<String, Float[]> targetBookSize = new HashMap<>();

  static {
    targetBookSize.put("120mmx180mm", new Float[]{314.5f, 502.0f});
    targetBookSize.put("150mmx205mm", new Float[]{368.5f, 558.5f});
    targetBookSize.put("Full paper size", new Float[]{null, null});
  }

  public enum TrimLinesType {

    NONE(translate("trimLinesNone")),
    DEFAULT(translate("trimLinesDefault")),
    CUSTOM(translate("trimLinesCustom"));

    private final String label;

    TrimLinesType
    (String label)
    {
      this.label = label;
    }

    public String toString
    ()
    {
      return label;
    }

  }

  private transient InputStream input;
  private List<SourceDocument> sourceDocuments;

  SerializableRectangle pageSize = new SerializableRectangle(PageSize.A4);
  SerializableRectangle signaturePageSize = new SerializableRectangle(PageSize.A3);
  boolean scaleToFit = true;
  private String pageRangesSource = null;
  private List<PageRef> pages = null;
  private int signatureSheets = 8; // 32 pages

  // Spine and edge offsets are for signature generation only:
  private final RangedFloat spineOffsetRatio = new RangedFloat("spine offset", 0.05f, 0f, 0.5f);
  private final RangedFloat edgeOffsetRatio = new RangedFloat("edge offset", 0f, 0f, 0.5f);
  private TrimLinesType trimLinesType = TrimLinesType.NONE;
  private final RangedFloat trimLinesHorizontalRatio = new RangedFloat("trim lines horizontal ratio", 0f, 0f, 0.5f);
  private final RangedFloat trimLinesVerticalRatio = new RangedFloat("trim lines vertical ratio", 0f, 0f, 0.5f);
  List<TransformSet> transformSets = new ArrayList<>();
  List<ContentGenerator> contentGenerators = new ArrayList<>();
  Map<Integer, List<Transform>> effectiveTransformsByPage = new HashMap<>();
  Map<Integer, List<ContentGenerator>> contentGeneratorsByPage = new HashMap<>();
  private String outputPath = null;
  private String signaturesOutputPath = null;
  private String name = null;
  private String comments = null;
  private String path = null;
  private boolean pdfOutputIsValid = false;
  private boolean pdfSignatureOutputIsValid = false;
  private static FontMapper fontMapper = generateFontMapper();

  RangedFloat leftMarginRatio = new RangedFloat("left margin", 0.05f, 0.0f, 0.5f);
  RangedFloat rightMarginRatio = new RangedFloat("right margin", 0.05f, 0.0f, 0.5f);
  RangedFloat bottomMarginRatio = new RangedFloat("bottom margin", 0.05f, 0.0f, 0.5f);
  RangedFloat topMarginRatio = new RangedFloat("top margin", 0.05f, 0.0f, 0.5f);
  private boolean usingMargins = false;
  private boolean usingPageNumbering = false;
  private final Map<String, SourceDocument> sourceDocumentsById = new HashMap<>();
  private transient StatusListener statusListener = null;
  ProjectMetaData metaData = new ProjectMetaData();

  private static final float RADIANS_PER_DEGREE = 3.14159265359f / 180.f;
  private static final float DEGREES_PER_RADIAN = 180.f / 3.14159265359f;

  public Book
  ()
  {}

  public Book
  (Book book)
  throws Exception
  {
    sourceDocuments = new ArrayList<>();
    sourceDocuments.addAll(book.sourceDocuments);
    pageSize = new SerializableRectangle(book.pageSize);
    signaturePageSize = new SerializableRectangle(book.signaturePageSize);
    scaleToFit = book.scaleToFit;
    pageRangesSource = book.pageRangesSource;
    pages = new ArrayList<>();
    if (book.pages != null) {
      pages.addAll(book.pages);
    }
    signatureSheets = book.signatureSheets;
    minimiseLastSignature = book.minimiseLastSignature;
    spineOffsetRatio.copy(book.spineOffsetRatio);
    edgeOffsetRatio.copy(book.edgeOffsetRatio);
    trimLinesType = book.trimLinesType;
    trimLinesHorizontalRatio.copy(book.trimLinesHorizontalRatio);
    trimLinesVerticalRatio.copy(book.trimLinesVerticalRatio);
    transformSets = new ArrayList<>();
    for (TransformSet transformSet : book.transformSets) {
      if (transformSet != null) {
        transformSets.add(new TransformSet(transformSet));
      }
    }
    for (Map.Entry<String, SourceDocument> sourceDocumentByIdEntry : book.sourceDocumentsById.entrySet()) {
      SourceDocument sourceDocument = new SourceDocument(sourceDocumentByIdEntry.getValue());
      sourceDocument.setBook(this);
      sourceDocumentsById.put(sourceDocumentByIdEntry.getKey(), sourceDocument);
    }
    Map<PageRef, List<Integer>> pageRefsToPageNumberLists = getPageRefsToPageNumberLists();
    computeEffectiveTransformSetsByPage(pageRefsToPageNumberLists);
    contentGenerators = new ArrayList<>();
    for (ContentGenerator contentGenerator : book.contentGenerators) {
      if (contentGenerator != null) {
        contentGenerators.add(new ContentGenerator(contentGenerator));
      }
    }
    computeContentGeneratorsByPage(pageRefsToPageNumberLists);
    outputPath = book.outputPath;
    signaturesOutputPath = book.signaturesOutputPath;
    name = book.name;
    comments = book.comments;
    path = book.path;
    leftMarginRatio = new RangedFloat(book.leftMarginRatio);
    rightMarginRatio = new RangedFloat(book.rightMarginRatio);
    bottomMarginRatio = new RangedFloat(book.bottomMarginRatio);
    topMarginRatio = new RangedFloat(book.topMarginRatio);
    usingMargins = book.usingMargins;
    usingPageNumbering = book.usingPageNumbering;
  }

  public ProjectMetaData getMetaData
  ()
  {
    if (metaData == null) {
      metaData = new ProjectMetaData();
    }
    return metaData;
  }

  private static FontMapper generateFontMapper
  ()
  {
    try {
      return new OSFontMapper();
    } catch (Throwable t) {
      System.err.println("Error generating font mapper: " + t.getMessage());
      System.exit(1);
    }
    return null;
  }

  public boolean getScaleToFit
  ()
  {
    return scaleToFit;
  }

  public void setScaleToFit
  (boolean scaleToFit)
  {
    this.scaleToFit = scaleToFit;
  }

  public RangedFloat getLeftMarginRatio
  ()
  {
    return leftMarginRatio;
  }

  public void setLeftMarginRatio
  (float leftMarginRatio)
  {
    this.leftMarginRatio.setValue(leftMarginRatio);
  }

  public RangedFloat getRightMarginRatio
  ()
  {
    return rightMarginRatio;
  }

  public void setRightMarginRatio
  (float rightMarginRatio)
  {
    this.rightMarginRatio.setValue(rightMarginRatio);
  }

  public RangedFloat getBottomMarginRatio
  ()
  {
    return bottomMarginRatio;
  }

  public void setBottomMarginRatio
  (float bottomMarginRatio)
  {
    this.bottomMarginRatio.setValue(bottomMarginRatio);
  }

  public RangedFloat getTopMarginRatio
  ()
  {
    return topMarginRatio;
  }

  public void setTopMarginRatio
  (float topMarginRatio)
  {
    this.topMarginRatio.setValue(topMarginRatio);
  }

  public Rectangle getPageSize
  ()
  {
    return pageSize.getRectangle();
  }

  public void setPageSize
  (Rectangle pageSize)
  {
    this.pageSize = new SerializableRectangle(pageSize);
  }

  public Rectangle getSignaturePageSize
  ()
  {
    return signaturePageSize.getRectangle();
  }

  public void setSignaturePageSize
  (Rectangle signaturePageSize)
  {
    this.signaturePageSize = new SerializableRectangle(signaturePageSize);
  }

  public String getPath
  ()
  {
    return path;
  }

  public void setPath
  (String path)
  {
    this.path = path;
  }

  public boolean isUsingMargins
  ()
  {
    return usingMargins;
  }

  public void setUsingMargins
  (boolean usingMargins)
  {
    this.usingMargins = usingMargins;
  }

  public boolean isUsingPageNumbering
  ()
  {
    return usingPageNumbering;
  }

  public void setUsingPageNumbering
  (boolean usingPageNumbering)
  {
    this.usingPageNumbering = usingPageNumbering;
  }

  private boolean minimiseLastSignature = false;

  public boolean isMinimisingLastSignature
  ()
  {
    return minimiseLastSignature;
  }

  public void setMinimiseLastSignature
  (boolean minimiseLastSignature)
  {
    this.minimiseLastSignature = minimiseLastSignature;
  }

  public void setSourceDocuments
  (Collection<SourceDocument> sourceDocuments)
  throws Exception
  {
    this.sourceDocuments = new ArrayList<>();
    for (SourceDocument sourceDocument : sourceDocuments) {
      addSourceDocument(sourceDocument);
    }
  }

  public void upSourceDocument
  (SourceDocument sourceDocument)
  {
    int index = sourceDocuments.indexOf(sourceDocument);
    if (index <= 0) {
      return;
    }
    sourceDocuments.remove(sourceDocument);
    sourceDocuments.add(index - 1, sourceDocument);
  }

  public void downSourceDocument
  (SourceDocument sourceDocument)
  {
    int index = sourceDocuments.indexOf(sourceDocument);
    if (index >= sourceDocuments.size() - 1) {
      return;
    }
    sourceDocuments.remove(sourceDocument);
    sourceDocuments.add(index + 1, sourceDocument);
  }

  public void removeSourceDocument
  (SourceDocument sourceDocument)
  {
    sourceDocuments.remove(sourceDocument);
  }

  public List<TransformSet> getTransformSets
  ()
  {
    if (transformSets == null) {
      transformSets = new ArrayList<>();
    }
    return transformSets;
  }

  public TransformSet findTransformSet
  (String name)
  {
    if (transformSets != null) {
      for (TransformSet transformSet : transformSets) {
        if (transformSet.getName().equals(name)) {
          return transformSet;
        }
      }
    }
    return null;
  }

  public void setTransformSets
  (Collection<TransformSet> transformSets)
  {
    this.transformSets = new ArrayList<>();
    this.transformSets.addAll(transformSets);
  }

  public void upTransformSet
  (TransformSet transformSet)
  {
    List<TransformSet> transformSets = getTransformSets();
    int index = transformSets.indexOf(transformSet);
    if (index <= 0) {
      return;
    }
    transformSets.remove(transformSet);
    transformSets.add(index - 1, transformSet);
  }

  public void downTransformSet
  (TransformSet transformSet)
  {
    List<TransformSet> transformSets = getTransformSets();
    int index = transformSets.indexOf(transformSet);
    if (index >= transformSets.size() - 1) {
      return;
    }
    transformSets.remove(transformSet);
    transformSets.add(index + 1, transformSet);
  }

  public void removeTransformSet
  (TransformSet transformSet)
  {
    transformSets.remove(transformSet);
  }

  public List<ContentGenerator> getContentGenerators
  ()
  {
    if (contentGenerators == null) {
      contentGenerators = new ArrayList<>();
    }
    return contentGenerators;
  }

  public void setContentGenerators
  (Collection<ContentGenerator> contentGenerators)
  {
    this.contentGenerators = new ArrayList<>();
    this.contentGenerators.addAll(contentGenerators);
  }

  public void upContentGenerator
  (ContentGenerator contentGenerator)
  {
    List<ContentGenerator> contentGenerators = getContentGenerators();
    int index = contentGenerators.indexOf(contentGenerator);
    if (index <= 0) {
      return;
    }
    contentGenerators.remove(contentGenerator);
    contentGenerators.add(index - 1, contentGenerator);
  }

  public void downContentGenerator
  (ContentGenerator contentGenerator)
  {
    List<ContentGenerator> contentGenerators = getContentGenerators();
    int index = contentGenerators.indexOf(contentGenerator);
    if (index >= contentGenerators.size() - 1) {
      return;
    }
    contentGenerators.remove(contentGenerator);
    contentGenerators.add(index + 1, contentGenerator);
  }

  public void removeContentGenerator
  (ContentGenerator contentGenerator)
  {
    contentGenerators.remove(contentGenerator);
  }

  public String getOutputPath
  ()
  {
    return outputPath;
  }

  public void setOutputPath
  (String outputPath)
  {
    this.outputPath = outputPath;
  }

  public String getSignaturesOutputPath
  ()
  {
    return signaturesOutputPath;
  }

  public void setSignaturesOutputPath
  (String signaturesOutputPath)
  {
    this.signaturesOutputPath = signaturesOutputPath;
  }

  public String getComments
  ()
  {
    return comments;
  }

  public String getName
  ()
  {
    return name;
  }

  public void setName
  (String name)
  {
    this.name = name;
  }

  public void setComments
  (String comments)
  {
    this.comments = comments;
  }

  private void generateSourceDocumentsById
  ()
  {
    sourceDocumentsById.clear();
    for (SourceDocument document : sourceDocuments) {
      if ((document.getId() != null) && !document.getId().isEmpty()) {
        sourceDocumentsById.put(document.getId(), document);
      }
    }
  }

  public void computeEffectiveTransformSetsByPage
  (Map<PageRef, List<Integer>> pageRefsToPageNumberLists)
  {
    try {
      generateSourceDocumentsById();
      effectiveTransformsByPage.clear();
      for (TransformSet transformSet : transformSets) {
        Set<PageRef> transformPages = new TreeSet<>();
        if (transformSet.getPageRanges() != null) {
          for (PageRange pageRange : transformSet.getPageRanges()) {
            transformPages.addAll(pageRange.getPageRefs(getPages(), sourceDocumentsById));
          }
        }
        for (PageRef transformPage : transformPages) {
          List<Integer> transformPagePageNumbers = getPageNumberList(transformPage, pageRefsToPageNumberLists);
          if (transformPagePageNumbers != null) {
            for (Integer transformPagePageNumber : transformPagePageNumbers) {
              List<Transform> pageTransforms =
                effectiveTransformsByPage.computeIfAbsent(transformPagePageNumber, k -> new ArrayList<>());
              pageTransforms.addAll(transformSet.getTransforms());
            }
          }
        }
      }
    } catch (Exception e) {
      handleException(e);
    }
  }

  public void computeContentGeneratorsByPage
  (Map<PageRef, List<Integer>> pageRefsToPageNumberLists)
  {
    try {
      generateSourceDocumentsById();
      contentGeneratorsByPage = new HashMap<>();
      for (ContentGenerator contentGenerator : contentGenerators) {
        Set<PageRef> contentGeneratorPages = new TreeSet<>();
        Collection<PageRange> pageRanges = contentGenerator.getPageRanges();
        if (pageRanges != null) {
          for (PageRange pageRange : pageRanges) {
            contentGeneratorPages.addAll(pageRange.getPageRefs(getPages(), sourceDocumentsById));
          }
        }
        for (PageRef contentGeneratorPage : contentGeneratorPages) {
          List<Integer> contentGeneratorPagePageNumbers = getPageNumberList(contentGeneratorPage, pageRefsToPageNumberLists);
          if (contentGeneratorPagePageNumbers != null) {
            for (Integer contentGeneratorPagePageNumber : contentGeneratorPagePageNumbers) {
              List<ContentGenerator> pageContentGenerators =
                contentGeneratorsByPage.computeIfAbsent(contentGeneratorPagePageNumber, k -> new ArrayList<>());
              pageContentGenerators.add(contentGenerator);
            }
          }
        }
      }
    } catch (Exception e) {
      handleException(e);
    }
  }

  public RangedFloat getSpineOffsetRatio
  ()
  {
    return spineOffsetRatio;
  }

  public RangedFloat getEdgeOffsetRatio
  ()
  {
    return edgeOffsetRatio;
  }

  public TrimLinesType getTrimLinesType
  ()
  {
    return trimLinesType;
  }

  public void setTrimLinesType
  (TrimLinesType trimLinesType)
  {
    this.trimLinesType = trimLinesType;
  }

  public RangedFloat getTrimLinesHorizontalRatio
  ()
  {
    return trimLinesHorizontalRatio;
  }

  public RangedFloat getTrimLinesVerticalRatio
  ()
  {
    return trimLinesVerticalRatio;
  }

  private static Rectangle getPaperSizeRectangle
  (PaperSize paperSize)
  {
    Float[] dimensions = paperSizes.get(paperSize);
    return new Rectangle(0f, 0f, dimensions[0], dimensions[1]);
  }

  public void setSpineOffsetRatio
  (float spineOffsetRatio)
  {
    this.spineOffsetRatio.setValue(spineOffsetRatio);
  }

  public void setEdgeOffsetRatio
  (Float edgeOffsetRatio)
  {
    this.edgeOffsetRatio.setValue(edgeOffsetRatio);
  }

  private void logException
  (String where, Throwable t)
  {
    getErrorOut().print("Caught exception " + where + ".\n\nBacktrace:\n\n");
    t.printStackTrace(getErrorOut());
  }

  public void setStatusListener
  (StatusListener statusListener)
  {
    this.statusListener = statusListener;
  }

  public void printStatus
  (String statusMessage)
  {
    if (statusListener != null) {
      statusListener.printBookStatus(statusMessage);
    }
  }

  public void setProgressLabel
  (String progressLabel)
  {
    if (statusListener != null) {
      statusListener.handleBookProgressLabel(progressLabel);
    }
  }

  public void setProgress
  (float progress, float maxProgress)
  {
    if (statusListener != null) {
      statusListener.handleBookProgress(progress, maxProgress);
    }
  }

  public void handleException
  (Exception e)
  {
    if (statusListener != null) {
      statusListener.handleBookException(e);
    }
    e.printStackTrace(System.err);
  }

  public PrintStream getErrorOut
  ()
  {
    return System.err;
  }

  public List<SourceDocument> getSourceDocuments
  ()
  {
    if (sourceDocuments == null) {
      sourceDocuments = new ArrayList<>();
    }
    for (SourceDocument sourceDocument : sourceDocuments)
    {
      sourceDocument.setBook(this);
    }
    return List.copyOf(sourceDocuments);
  }

  public void addSourceDocument
  (SourceDocument sourceDocument)
  throws Exception
  {
    sourceDocuments.add(sourceDocument);
    sourceDocument.setBook(this);
    String stringId = sourceDocument.getId();
    if (stringId != null) {
      if (sourceDocumentsById.containsKey(stringId)) {
        throw new Exception("A source document with id \"" + stringId + "\" is already defined");
      }
      sourceDocumentsById.put(stringId, sourceDocument);
    }
  }

  void  changeSourceDocumentId
  (String oldId, String newId, SourceDocument sourceDocument)
  {
    sourceDocument.setBook(this);
    if (oldId != null) {
      sourceDocumentsById.remove(oldId);
    }
    if (newId != null) {
      sourceDocumentsById.put(newId, sourceDocument);
    }
    statusListener.handlePageRangesChange(pageRangesSource);
  }

  private void clearPages
  ()
  {
    pages = null;
  }

  public String getPageRangesSource
  ()
  {
    return pageRangesSource;
  }

  public void setPageRangesSource
  (String pageRangesSource)
  {
    this.pageRangesSource = pageRangesSource;
    pages = null;
  }

  public void setPages
  (Collection<PageRef> pages)
  {
    this.pages = new ArrayList<>();
    this.pages.addAll(pages);
  }

  public List<PageRef> getPages
  (String pageRangesSource)
  throws Exception
  {
    if (pageRangesSource == null) {
      pages = new ArrayList<>();
    } else {
      pages = PageRange.toPageRefs(PageRange.parsePageRanges(pageRangesSource, true), sourceDocumentsById);
    }
    return pages;
  }

  public List<PageRef> getPages
  ()
  throws Exception
  {
    return getPages(pageRangesSource);
  }

  private Map<PageRef, List<Integer>> getPageRefsToPageNumberLists
  ()
  throws Exception
  {
    Map<PageRef, List<Integer>> pageRefsToPageNumberLists = new HashMap<>();
    List<PageRef> pages = getPages();
    for (PageRef pageRef : pages) {
      if (pageRef.getPageNumber() > 0) {
        List<Integer> pageNumbers = pageRefsToPageNumberLists.get(pageRef);
        if (pageNumbers == null) {
          pageNumbers = new ArrayList<>();
          pageRefsToPageNumberLists.put(pageRef, pageNumbers);
          if (pageRef.getSourceDocument() != null) {
            // This is a ref to a source document - add the page numbers of all occurrences:
            int searchPageNumber = 0;
            for (PageRef searchPageRef : pages) {
              searchPageNumber++;
              if (searchPageRef.equals(pageRef)) {
                pageNumbers.add(searchPageNumber);
              }
            }
          }
        }
      }
    }
    return pageRefsToPageNumberLists;
  }

  private List<Integer> getPageNumberList
  (PageRef pageRef, Map<PageRef, List<Integer>> pageRefsToPageNumberLists)
  throws Exception
  {
    if (pageRef.getSourceDocument() != null) {
      return pageRefsToPageNumberLists.get(pageRef);
    } else {
      return List.of(pageRef.getPageNumber());
    }
  }

  public void setSourceDocumentsByPath
  (String ... sourceDocumentPaths)
  throws Exception
  {
    sourceDocuments = new ArrayList<>();
    for (String sourceDocumentPath : sourceDocumentPaths) {
      addSourceDocument(new SourceDocument(sourceDocumentPath));
    }
  }

  public boolean closePDFs
  ()
  {
    try {
      clearPages();
      for (SourceDocument currentDoc : sourceDocuments) {
        try {
          if (currentDoc.getReader() != null) {
            currentDoc.getReader().close();
          }
        } catch (Throwable ignored) {}
      }
      return true;
    } catch (Exception e) {
      logException("closing PDF", e);
      return false;
    }
  }

  public int getSignatureSheets
  ()
  {
    return signatureSheets;
  }

  public void setSignatureSheets
  (int sheets)
  {
    this.signatureSheets = sheets;
  }

  public int getPageCount
  ()
  throws Exception
  {
    return getPages() != null ? pages.size() : 0;
  }

  public static float mm2Points
  (float mm)
  {
    // millimetres to points = 2.83464567
    // points to millimetres = 0.352777778
    return mm*2.83464567f;
  }

  public static float points2MM
  (float points)
  {
    return points * 0.352777778f;
  }

  private record ScaleToFitRecord(AffineTransform affineTransform, float width, float height) {}

  // Initially scale a source page image to a minimal fit in the target page size.
  private ScaleToFitRecord scaleToFit
  (Image pageImage, Rectangle pageSize)
  {
    float scaleFactor;
    float xOffset = 0.0f;
    float yOffset = 0.0f;
    if ((pageSize.getHeight() / pageSize.getWidth()) > (pageImage.getHeight() / pageImage.getWidth())) {
      scaleFactor = pageSize.getWidth() / pageImage.getWidth();
      yOffset = (pageSize.getHeight() - scaleFactor * pageImage.getHeight()) / 2.0f;
    } else {
      scaleFactor = pageSize.getHeight() / pageImage.getHeight();
      xOffset = (pageSize.getWidth() - scaleFactor * pageImage.getWidth()) / 2.0f;
    }
    AffineTransform affineTransform = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
    float width = pageImage.getWidth() * scaleFactor;
    float height = pageImage.getHeight() * scaleFactor;
    affineTransform.concatenate(AffineTransform.getTranslateInstance(xOffset, yOffset));
    return new ScaleToFitRecord(affineTransform, width, height);
  }

  public enum CropType { LEFT_CROP, RIGHT_CROP, BOTTOM_CROP, TOP_CROP, ALL_CROP, MARGINS_CROP }

  private static class CropOp {

    public CropType cropType;
    AffineTransform transform;
    Float cropRatio;

    public CropOp
    (CropType cropType, AffineTransform transform, Float cropRatio)
    {
      this.cropType = cropType;
      this.transform = transform;
      this.cropRatio = cropRatio;
    }

  }

  private static class TransformedImage {

    public Image image;
    public AffineTransform transform;
    public List<CropOp> cropOps;

    public TransformedImage
      (Image image, AffineTransform transform, List<CropOp> cropOps) {
      this.image = image;
      this.transform = transform;
      this.cropOps = cropOps;
    }

  }

  private TransformedImage generateTransformedPageImage
  (PdfWriter writer, Integer totalPageNumber, Rectangle unitSize, boolean even)
  throws Exception
  {
    PageRef pageRef = getPageRef(totalPageNumber);
    int pageNumber = pageRef.getPageNumber();
    if (pageNumber == 0) {
      writer.setPageEmpty(false);
      writer.newPage();
      return null;
    }
    PdfImportedPage page = writer.getImportedPage(pageRef.getPdfReader(), pageRef.getPageNumber());
    List<Transform> transforms = effectiveTransformsByPage.get(totalPageNumber);
    Image pageImage = Image.getInstance(page);
    List<CropOp> cropOps = new ArrayList<>();
    TransformedImage returnValue = new TransformedImage(pageImage, null, cropOps);
    pageImage.setAbsolutePosition(0f, 0f);
    AffineTransform affineTransform = null;
    float initialWidth = pageImage.getScaledWidth();
    float initialHeight = pageImage.getScaledHeight();
    if (scaleToFit) {
      ScaleToFitRecord scaleToFitRecord = scaleToFit(pageImage, unitSize);
      affineTransform = scaleToFitRecord.affineTransform();
      initialWidth = scaleToFitRecord.width();
      initialHeight = scaleToFitRecord.height();
    }
    if (transforms != null) {
      for (Transform transform : transforms) {
        if (!transform.isEnabled()) {
          continue;
        }
        switch (transform.getType()) {
          case ROTATION_IN_DEGREES -> {
            // Translate to the origin:
            AffineTransform newTransform = AffineTransform.getTranslateInstance(-initialWidth/2.0f, -initialHeight/2.0f);
            affineTransform = concatenate(newTransform, affineTransform);
            concatenate(newTransform, returnValue.cropOps);
            // Rotate:
            newTransform = AffineTransform.getRotateInstance(RADIANS_PER_DEGREE * transform.getRangedFloat().getValue());
            affineTransform = concatenate(newTransform, affineTransform);
            concatenate(newTransform, returnValue.cropOps);
            // Translate back:
            newTransform = AffineTransform.getTranslateInstance(initialWidth/2.0f, initialHeight/2.0f);
            affineTransform = concatenate(newTransform, affineTransform);
            concatenate(newTransform, returnValue.cropOps);
          }
          case SCALE -> {
            float scaleFactor = transform.getRangedFloat().getValue();
            affineTransform = concatenate(
              AffineTransform.getScaleInstance(scaleFactor, scaleFactor), affineTransform
            );
            float xOffset = -(scaleFactor - 1.0f) * initialWidth / 2.0f;
            float yOffset = -(scaleFactor - 1.0f) * initialHeight / 2.0f;
            AffineTransform newTransform = AffineTransform.getTranslateInstance(xOffset, yOffset);
            affineTransform = concatenate(newTransform, affineTransform);
            concatenate(newTransform, returnValue.cropOps);
          }
          case SCALE_X -> {
            float scaleFactor = transform.getRangedFloat().getValue();
            AffineTransform newTransform = AffineTransform.getScaleInstance(scaleFactor, 1.0f);
            affineTransform = concatenate(newTransform, affineTransform);
            concatenate(newTransform, returnValue.cropOps);
            float xOffset = -(scaleFactor - 1.0f) * initialWidth / 2.0f;
            float yOffset = 0.0f;
            newTransform = AffineTransform.getTranslateInstance(xOffset, yOffset);
            affineTransform = concatenate(newTransform, affineTransform);
            concatenate(newTransform, returnValue.cropOps);
          }
          case SCALE_Y -> {
            float scaleFactor = transform.getRangedFloat().getValue();
            AffineTransform newTransform = AffineTransform.getScaleInstance(1.0f, scaleFactor);
            affineTransform = concatenate(newTransform, affineTransform);
            concatenate(newTransform, returnValue.cropOps);
            float xOffset = 0.0f;
            float yOffset = -(scaleFactor - 1.0f) * initialHeight / 2.0f;
            newTransform = AffineTransform.getTranslateInstance(xOffset, yOffset);
            affineTransform = concatenate(newTransform, affineTransform);
            concatenate(newTransform, returnValue.cropOps);
          }
          case X_TRANSLATION -> {
            float xOffset = initialWidth * transform.getRangedFloat().getValue();
            AffineTransform newTransform = AffineTransform.getTranslateInstance(xOffset, 0.0f);
            affineTransform = concatenate(newTransform, affineTransform);
            concatenate(newTransform, returnValue.cropOps);
          }
          case Y_TRANSLATION -> {
            float yOffset = initialWidth * transform.getRangedFloat().getValue();
            AffineTransform newTransform = AffineTransform.getTranslateInstance(0.0f, yOffset);
            affineTransform = concatenate(newTransform, affineTransform);
            concatenate(newTransform, returnValue.cropOps);
          }
          case LEFT_CROP, RIGHT_CROP, BOTTOM_CROP, TOP_CROP, ALL_CROP -> {
            switch (transform.getType()) {
              case LEFT_CROP -> returnValue.cropOps.add(
                new CropOp(CropType.LEFT_CROP, null, transform.getRangedFloat().getValue())
              );
              case RIGHT_CROP -> returnValue.cropOps.add(
                new CropOp(CropType.RIGHT_CROP, null, transform.getRangedFloat().getValue())
              );
              case BOTTOM_CROP -> returnValue.cropOps.add(
                new CropOp(CropType.BOTTOM_CROP, null, transform.getRangedFloat().getValue())
              );
              case TOP_CROP -> returnValue.cropOps.add(
                new CropOp(CropType.TOP_CROP, null, transform.getRangedFloat().getValue())
              );
              case ALL_CROP -> returnValue.cropOps.add(
                new CropOp(CropType.ALL_CROP, null, transform.getRangedFloat().getValue())
              );
            }
          }
          case MARGINS_CROP -> returnValue.cropOps.add(
            new CropOp(CropType.MARGINS_CROP, null, null)
          );
        }
      }
    }
    returnValue.transform = affineTransform;
    return returnValue;
  }

  private AffineTransform concatenate
  (AffineTransform newTransform, AffineTransform existingTransform)
  {
    AffineTransform transformCopy = (AffineTransform)newTransform.clone();
    if (existingTransform == null) {
      return transformCopy;
    }
    transformCopy.concatenate(existingTransform);
    return transformCopy;
  }

  private void concatenate
  (AffineTransform newTransform, List<CropOp> cropOps)
  {
    for (CropOp cropOp : cropOps) {
      cropOp.transform = concatenate(newTransform, cropOp.transform);
    }
  }

  /**
   * Generate PDF with modifications applied
   * @param destinationPath path of the generated PDF
   */
  public void generatePDF
  (String destinationPath)
  {
    generatePDF(destinationPath, usingMargins, usingPageNumbering);
  }

  public void generatePDF
  (String destinationPath, boolean usingMargins, boolean usingPageNumbering)
  {
    try {
      if (destinationPath == null) {
        throw new Exception("Output path not set");
      }
      FileOutputStream out = new FileOutputStream(destinationPath);
      generatePDF(out, usingMargins, usingPageNumbering, null);
    } catch (Exception e) {
      handleException(e);
      logException("PDF Generation", e);
    }
  }

  /**
   * Generate PDF with modifications applied
   * @param out OutputStream to generate PDF to
   * @param usingMargins if true, draw a margin overlay
   * @param usingPageNumbering if true, draw a page numbering overlay
   * @param targetPages if non-null, generate only the given pages
   */
  public void generatePDF
  (OutputStream out, boolean usingMargins, boolean usingPageNumbering, Collection<PageRef> targetPages)
  {
    try {
      List<PageRef> pages = getPages();
      if (pages.isEmpty() || ((pages.size() == 1) && pages.get(0).getSourceDocument() == null)) {
        return;
      }
      Map<PageRef, List<Integer>> pageRefsToPageNumberLists = getPageRefsToPageNumberLists();
      computeEffectiveTransformSetsByPage(pageRefsToPageNumberLists);
      computeContentGeneratorsByPage(pageRefsToPageNumberLists);
      Document document = new Document(pageSize.getRectangle(), 0, 0, 0, 0);
      PdfWriter writer = PdfWriter.getInstance(document, out);
      document.open();
      PdfContentByte cb = writer.getDirectContent();
      PdfImportedPage page;
      float pageWidth = pageSize.getRectangle().getWidth();
      float pageHeight = pageSize.getRectangle().getHeight();
      setProgressLabel(translate("documentGenerationColon"));
      for (ContentGenerator contentGenerator : contentGenerators) {
        contentGenerator.compile();
      }
      for (int totalPageNumber = 1; totalPageNumber <= getPages().size(); totalPageNumber++) {
        PageRef pageRef = getPages().get(totalPageNumber - 1);
        if ((!pageRef.isBlankPage()) && ((targetPages != null) && !targetPages.contains(pageRef))) {
          continue;
        }
        document.newPage();
        writer.setPageEmpty(false);
        if ((!pageRef.isBlankPage()) && (pageRef.getPdfReader() != null)) {
          AffineTransform affineTransform = null;
          TransformedImage transformedImage =
            generateTransformedPageImage(writer, totalPageNumber, pageSize.getRectangle(), totalPageNumber % 2 == 0);
          if (transformedImage != null) {
            // First we add the image with the total transform:
            Image pageImage = transformedImage.image;
            AffineTransform transform = null;
            if (transformedImage.transform != null) {
              transform = transformedImage.transform;
              cb.transform(transform);
            }
            cb.addImage(transformedImage.image);
            // Next we add the cropping rectangles, each with the transform corresponding to their position in the
            // list of transforms:
            cb.setColorFill(Color.WHITE);
            cb.setColorStroke(Color.WHITE);
            cb.setLineWidth(0);
            for (CropOp cropOp : transformedImage.cropOps) {
              if (transform != null) {
                cb.transform(transform.createInverse());
                transform = null;
              }
              if (cropOp.transform != null) {
                transform = cropOp.transform;
                cb.transform(transform);
              }
              switch (cropOp.cropType) {
                // Crop to a full page width / height outside the page in three directions:
                // META refactor so this code isn't repeated for ALL_CROP:
                case LEFT_CROP -> cb.rectangle(
                  -pageWidth, -pageHeight, pageWidth * (1.0f + cropOp.cropRatio), 3.0f * pageHeight
                );
                case RIGHT_CROP -> cb.rectangle(
                  pageWidth * (1.0f - cropOp.cropRatio), -pageHeight, pageWidth * (1.0f + cropOp.cropRatio),
                  3.0f * pageHeight
                );
                case BOTTOM_CROP -> cb.rectangle(
                  -pageWidth, -pageHeight, 3.0f * pageWidth, pageHeight * (1.0f + cropOp.cropRatio)
                );
                case TOP_CROP -> cb.rectangle(
                  -pageWidth, pageHeight * (1.0f - cropOp.cropRatio), 3.0f * pageWidth,
                  pageHeight * (1.0f + cropOp.cropRatio)
                );
                case ALL_CROP -> {
                  cb.rectangle(
                    -pageWidth, -pageHeight, pageWidth * (1.0f + cropOp.cropRatio), 3.0f * pageHeight
                  );
                  cb.rectangle(
                    pageWidth * (1.0f - cropOp.cropRatio), -pageHeight, pageWidth * (1.0f + cropOp.cropRatio),
                    3.0f * pageHeight
                  );
                  cb.rectangle(
                    -pageWidth, -pageHeight, 3.0f * pageWidth, pageHeight * (1.0f + cropOp.cropRatio)
                  );
                  cb.rectangle(
                    -pageWidth, pageHeight * (1.0f - cropOp.cropRatio), 3.0f * pageWidth,
                    pageHeight * (1.0f + cropOp.cropRatio)
                  );
                }
                case MARGINS_CROP -> {
                  // TODO - use marginLeftX etc.... same crop patches as above for ALL_CROP...
                  cb.rectangle(
                    -pageWidth, -pageHeight, pageWidth * (1.0f + leftMarginRatio.getValue()), 3.0f * pageHeight
                  );
                  cb.rectangle(
                    pageWidth * (1.0f - rightMarginRatio.getValue()), -pageHeight, pageWidth * (1.0f + rightMarginRatio.getValue()),
                    3.0f * pageHeight
                  );
                  cb.rectangle(
                    -pageWidth, -pageHeight, 3.0f * pageWidth, pageHeight * (1.0f + bottomMarginRatio.getValue())
                  );
                  cb.rectangle(
                    -pageWidth, pageHeight * (1.0f - topMarginRatio.getValue()), 3.0f * pageWidth,
                    pageHeight * (1.0f + topMarginRatio.getValue())
                  );
                }
              }
              cb.fillStroke();
            }
            // Next we invert any existing transform from the above, and possibly draw the margins and page numbers:
            if (transform != null) {
              cb.transform(transform.createInverse());
            }
          }
          List<ContentGenerator> pageContentGenerators = contentGeneratorsByPage.get(totalPageNumber);
          cb.setColorFill(Color.BLACK);
          cb.setColorStroke(Color.BLACK);
          if (pageContentGenerators != null) {
            for (ContentGenerator contentGenerator : pageContentGenerators) {
              // DefaultFontMapper fontMapper = new DefaultFontMapper();
              if ((contentGenerator.getHorizontalOffset() != null) && (contentGenerator.getVerticalOffset() != null)) {
                float horizontalOffset = contentGenerator.getHorizontalOffset() * pageSize.getRectangle().getWidth();
                float verticalOffset = contentGenerator.getVerticalOffset() * pageSize.getRectangle().getHeight();
                Rectangle frameRectangle = null;
                if (contentGenerator.isUsingFrame() &&
                    (contentGenerator.getWidth() != null) && (contentGenerator.getXYRatio() != null)) {
                  float frameWidth = contentGenerator.getWidth() * pageWidth;
                  float frameHeight = frameWidth / contentGenerator.getXYRatio();
                  frameRectangle = new Rectangle(
                    horizontalOffset  - (frameWidth / 2.0f), verticalOffset - (frameHeight / 2.0f),
                    horizontalOffset + (frameWidth / 2.0f), verticalOffset + (frameHeight / 2.0f)
                  );
                  if (contentGenerator.isUsingBorder() && (contentGenerator.getBorderWidth() != null)) {
                    float frameBorderWidth = contentGenerator.getBorderWidth();
                    if (frameBorderWidth > 0.0f) {
                      frameRectangle.setBorder(Rectangle.BOTTOM | Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT);
                      frameRectangle.setBorderWidth(frameBorderWidth);
                      frameRectangle.setBorderColor(contentGenerator.getBorderColor());
                    } else {
                      frameRectangle.setBorder(Rectangle.NO_BORDER);
                    }
                  }
                  if (contentGenerator.getBackgroundType() == ContentGenerator.BackgroundType.COLOR) {
                    frameRectangle.setBackgroundColor(contentGenerator.getBackgroundColor());
                  } else if (contentGenerator.getBackgroundType() == ContentGenerator.BackgroundType.IMAGE) {
                    String imagePath = contentGenerator.getBackgroundImagePath();
                    if ((imagePath != null) && !imagePath.isBlank()) {
                      Image backgroundImage = Image.getInstance(contentGenerator.getBackgroundImagePath());
                      backgroundImage.setAbsolutePosition(horizontalOffset - (frameWidth / 2.0f), verticalOffset - (frameHeight / 2.0f));
                      backgroundImage.scaleAbsolute(frameRectangle.getWidth(), frameRectangle.getHeight());
                      cb.addImage(backgroundImage);
                    }
                  }
                  cb.rectangle(frameRectangle);
                  cb.stroke();
                }
                BaseFont baseFont = fontMapper.awtToPdf(contentGenerator.getFont());
                float fontSize = contentGenerator.getFont().getSize();
                float lineHeight = contentGenerator.getLineHeightFactor();
                float lineOffset = contentGenerator.getLineOffsetFactor();
                Color fontColor = contentGenerator.getTextColor();
                cb.setFontAndSize(baseFont, fontSize);
                cb.setColorFill(fontColor);
                cb.setColorStroke(fontColor);
                String content = contentGenerator.getContent(pageRef, totalPageNumber);
                String[] contentLines = StringUtils.toLines(content);
                int contentLineCount = 0;
                for (String contentLine : contentLines) {
                  float contentWidth = cb.getEffectiveStringWidth(contentLine, true);
                  switch (contentGenerator.getAlignment()) {
                    case CENTRE:
                      horizontalOffset -= contentWidth / 2.0f;
                      break;
                    case RIGHT:
                      horizontalOffset -= contentWidth;
                      break;
                  }
                  cb.beginText();
                  cb.moveText(
                    horizontalOffset,
                    verticalOffset - ((lineOffset + contentLineCount++) * lineHeight * fontSize)
                  );
                  cb.showText(contentLine);
                  cb.endText();
                }
                cb.stroke();
              }
            }
          }
          float leftMarginX = leftMarginRatio.getValue() * pageSize.getRectangle().getWidth();
          float rightMarginX = pageSize.getRectangle().getWidth() * (1.0f - rightMarginRatio.getValue());
          float bottomMarginY = bottomMarginRatio.getValue() * pageSize.getRectangle().getHeight();
          float topMarginY = pageSize.getRectangle().getHeight() * (1.0f - topMarginRatio.getValue());
          if (usingMargins) {
            cb.setColorStroke((Color)InitFile.instance().get("marginColour", Color.BLUE));
            cb.setLineWidth((Float)InitFile.instance().get("marginLineThickness", 1.5f));
            int xDivisions = (Integer)InitFile.instance().get("marginHorizontalDivisions", 2);
            int yDivisions = (Integer)InitFile.instance().get("marginVerticalDivisions", 2);
            float divisionWidth = (rightMarginX - leftMarginX) / (float)xDivisions;
            float divisionHeight = (topMarginY - bottomMarginY) / (float)yDivisions;
            for (int i = 0; i <= xDivisions; i++) {
              cb.moveTo(leftMarginX + ((float)i) * divisionWidth, bottomMarginY);
              cb.lineTo(leftMarginX + ((float)i) * divisionWidth, topMarginY);
            }
            for (int i = 0; i <= yDivisions; i++) {
              cb.moveTo(leftMarginX, bottomMarginY + ((float)i) * divisionHeight);
              cb.lineTo(rightMarginX, bottomMarginY + ((float)i) * divisionHeight);
            }
            cb.stroke();
          }
          if (usingPageNumbering) {
            String pageNumberText = "" + pageRef.getPageNumberText(); // ORIGINAL page number
            String totalPageNumberText = "" + totalPageNumber; // TOTAL (result doc) page number
            cb.setLineWidth((Float)InitFile.instance().get("pageNumbersLineThickness", 0.0f));
            cb.beginText();
            cb.setColorStroke((Color)InitFile.instance().get("pageNumbersColour", Color.BLUE));
            cb.setColorFill((Color)InitFile.instance().get("pageNumbersColour", Color.BLUE));
            float fontHeight = pageHeight / 10.0f;
            cb.setFontAndSize(FontFactory.getFont("courier").getBaseFont(), fontHeight);
            cb.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);
            float stringWidth = cb.getEffectiveStringWidth(pageNumberText, false);
            float fontScaleFactor = 1.0f;
            float effectivePageWidth = 0.90f * (rightMarginX - leftMarginX);
            if (stringWidth > effectivePageWidth) {
              fontScaleFactor = 1.0f - ((stringWidth - effectivePageWidth) / stringWidth);
              cb.setFontAndSize(FontFactory.getFont("courier").getBaseFont(), fontScaleFactor * fontHeight);
            }
            cb.moveText(
              (leftMarginX + rightMarginX) / 2.0f - (fontScaleFactor * stringWidth / 2.0f),
              (bottomMarginY + topMarginY) / 2.0f + fontScaleFactor * fontHeight / 2.0f
            );
            cb.showText(pageNumberText);
            cb.endText();
            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont("courier").getBaseFont(), fontHeight);
            stringWidth = cb.getEffectiveStringWidth(totalPageNumberText, false);
            cb.moveText(
              (leftMarginX + rightMarginX) / 2.0f - (stringWidth / 2.0f),
              (bottomMarginY + topMarginY) / 2.0f - fontHeight
            );
            cb.showText(totalPageNumberText);
            cb.endText();
          }
        }
        setProgress(totalPageNumber, getPages().size());
      }
      document.close();
      printStatus(translate("generated") + " " + getPages().size() + " " + translate("pages") + ".");
      pdfOutputIsValid = true;
    } catch (Exception e) {
      handleException(e);
      pdfOutputIsValid = false;
      pdfSignatureOutputIsValid = false;
    }
  }

  /**
   * Generation of page number 4-tuples for pages printed on both sides of signature sheets.
   * @param pageCount the total number of pages to compile into signatures
   * @param sheetsPerSignature the number of physical (unfolded) sheets of paper in a signature
   * @return an array of [signatureIndex][sheetIndex][pageIndex] page numbers
   */
  static int[][][] generateSignaturePageNumbers
  (int pageCount, int sheetsPerSignature, boolean minimiseLastSignature)
  {
    int signaturePages = sheetsPerSignature*4;
    int signatureCount = (int)Math.ceil(((float)pageCount)/(float)(sheetsPerSignature*4));
    int[][][] result = new int[signatureCount][sheetsPerSignature][4];
    for (int signatureIndex = 0; signatureIndex < signatureCount; signatureIndex++) {
      int thisSignatureSheetCount = sheetsPerSignature;
      for (int signatureSheetIndex = 0; signatureSheetIndex < sheetsPerSignature; signatureSheetIndex++) {
        for (int sheetPageIndex = 0; sheetPageIndex < 4; sheetPageIndex++) {
          result[signatureIndex][signatureSheetIndex][sheetPageIndex] = 0;
        }
      }
      if (minimiseLastSignature && (signatureIndex == (signatureCount - 1))) {
        int thisSignaturePageCount = pageCount - ((signatureCount - 1) * signaturePages);
        thisSignatureSheetCount = thisSignaturePageCount / 4;
        thisSignatureSheetCount = thisSignatureSheetCount * 4 >= thisSignaturePageCount
          ? thisSignatureSheetCount : thisSignatureSheetCount + 1;
        signaturePages = 4 * thisSignatureSheetCount;
      }
      int signatureStartPage = signatureIndex * sheetsPerSignature * 4 + 1;
      for (int signatureSheetIndex = 0; signatureSheetIndex < thisSignatureSheetCount; signatureSheetIndex++) {
        // The page numbers in an individual "signature" (leaf array) are front left, front right, (short flip)
        // back left, back right, where the "front" is looking at the spine:
        int frontLeftPage = signatureStartPage + signaturePages - 2 * signatureSheetIndex - 1;
        frontLeftPage = frontLeftPage <= pageCount ? frontLeftPage : 0;
        result[signatureIndex][signatureSheetIndex][0] = frontLeftPage;
        int frontRightPage = signatureStartPage + 2*signatureSheetIndex;
        frontRightPage = frontRightPage <= pageCount ? frontRightPage : 0;
        result[signatureIndex][signatureSheetIndex][1] = frontRightPage;
        int backLeftPage = signatureStartPage + 2*signatureSheetIndex + 1;
        backLeftPage = backLeftPage <= pageCount ? backLeftPage : 0;
        result[signatureIndex][signatureSheetIndex][2] = backLeftPage;
        int backRightPage = signatureStartPage + signaturePages - 2*signatureSheetIndex - 2;
        backRightPage = backRightPage <= pageCount ? backRightPage : 0;
        result[signatureIndex][signatureSheetIndex][3] = backRightPage;
      }
    }
    return result;
  }

  /**
   * Generate PDF signature files based on the current settings of this Book.
   * @param destinationDirectoryPath path to the directory in which to generate signature files
   * @param destinationSignaturePrefix filename prefix for individaul signature files
   * @param signatureNumbers if non-null, an array of 1-based signature numbers to generate
   */
  public void generatePDFSignatures
  (String destinationDirectoryPath, String destinationSignaturePrefix, int ... signatureNumbers)
  {
    generatePDFSignatures(null, destinationDirectoryPath, destinationSignaturePrefix, false, signatureNumbers);
  }

  /**
   * Generate PDF signature files based on the current settings of this Book.
   * @param out if non-null, an output stream to which <em>all</em> signatures will be generated, ignoring the values
   *        of destinationDirectoryPath and destinationSignaturePrefix.
   * @param destinationDirectoryPath path to the directory in which to generate signature files.  This parameter will
   *        be ignored if out is non-null.
   * @param addSpineImage if true, a simulated spine-create image will be drawn in the middle of signatures.
   * @param destinationSignaturePrefix filename prefix for individaul signature files.  This parameter will be ignored
   *        if out is non-null.
   * @param signatureNumbers if non-null, an array of 1-based signature numbers to generate
   */
  public void generatePDFSignatures
  (
    OutputStream out, String destinationDirectoryPath, String destinationSignaturePrefix, boolean addSpineImage,
    int ... signatureNumbers
  )
  {
    try {
      // We are generating signatures from the output:
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      generatePDF(byteArrayOutputStream, isUsingMargins(), isUsingPageNumbering(), null);
      byte[] bookBytes = byteArrayOutputStream.toByteArray();
      InputStream bookStream = new ByteArrayInputStream(bookBytes);
      SourceDocument outputSourceDocument = new SourceDocument(bookStream);
      List<PageRef> outputPages = outputSourceDocument.getSourcePages();
      if (getPageCount() <= 0) {
        throw new Exception("Document not loaded or empty");
      }
      int[][][] signatures = generateSignaturePageNumbers(getPageCount(), signatureSheets, minimiseLastSignature);
      int[] signatureIndices;
      if (signatureNumbers.length > 0) {
        // Make an array of indices from 0 to signatureNumbers.length, where each index is the signature number - 1:
        signatureIndices = new int[signatureNumbers.length];
        for (int signatureIndexIndex = 0; signatureIndexIndex < signatureNumbers.length; signatureIndexIndex++) {
          int signatureIndex = signatureNumbers[signatureIndexIndex] - 1;
          if ((signatureIndex >= signatures.length) || (signatureIndex < 0)) {
            throw new Exception(
              "Invalid signature number " + signatureNumbers[signatureIndexIndex] + " for " + signatures.length +
              " signatures"
            );
          }
          signatureIndices[signatureIndexIndex] = signatureIndex;
        }
      } else {
        // Just make an array of signatures.length indices from 0 to signatures.length - 1:
        signatureIndices = new int[signatures.length];
        for (int signatureIndex = 0; signatureIndex < signatures.length; signatureIndex++) {
          signatureIndices[signatureIndex] = signatureIndex;
        }
      }
      int totalSignaturePageCount = signatureSheets * 4 * signatures.length;
      int generatedPageCount = 0;
      setProgressLabel(translate("signatureGenerationColon"));
      setProgress(0, getPageCount());
      PdfWriter writer = null;
      Document document = null;
      for (int signatureIndex : signatureIndices) {
        String signatureFileName = destinationSignaturePrefix + "_sig_" + (signatureIndex + 1) + ".pdf";
        File signatureDirectory = (out == null) ? new File(destinationDirectoryPath) : null;
        if (signatureDirectory != null) {
          if (!(signatureDirectory.canExecute() && signatureDirectory.canWrite())) {
            if (!signatureDirectory.mkdirs()) {
              throw new Exception("Couldn't access / create signatures directory \"" + destinationDirectoryPath + "\"");
            }
          }
        }
        File signatureFile = new File(destinationDirectoryPath, signatureFileName);
        Rectangle halfSize = new Rectangle(
          signaturePageSize.getRectangle().getHeight() / 2.0f,
          signaturePageSize.getRectangle().getWidth()
        );
        OutputStream signatureStream = (out != null) ? out : new FileOutputStream(signatureFile);
        if (out == null || document == null) {
          document = new Document(signaturePageSize.getRectangle().rotate(), 0, 0, 0, 0);
          document.setMargins(0.0f, 0.0f, 0.0f, 0.0f);
        }
        if (out == null || writer == null) {
            writer = PdfWriter.getInstance(document, signatureStream);
        }
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        float pageRatio = 1.0f - spineOffsetRatio.getValue() - edgeOffsetRatio.getValue();
        float spineOffset = spineOffsetRatio.getValue() * halfSize.getWidth();
        float edgeOffset = edgeOffsetRatio.getValue() * halfSize.getWidth();
        float yOffset = halfSize.getHeight() * (1.0f - pageRatio) * 0.5f;
        boolean usingTrimLines = trimLinesType != null && !trimLinesType.equals(TrimLinesType.NONE);
        float horizontalTrimRatio = 0.0f;
        float verticalTrimRatio = 0.0f;
        float TRIM_FACTOR = 0.9f;
        if (trimLinesType == TrimLinesType.DEFAULT) {
          horizontalTrimRatio = TRIM_FACTOR * edgeOffsetRatio.getValue();
          verticalTrimRatio = TRIM_FACTOR * (spineOffsetRatio.getValue() + edgeOffsetRatio.getValue()) / 2.0f;
        } else {
          if ((trimLinesHorizontalRatio != null) && (trimLinesVerticalRatio != null)) {
            horizontalTrimRatio = trimLinesHorizontalRatio.getValue();
            verticalTrimRatio = trimLinesVerticalRatio.getValue();
          }
        }
        for (int signatureSheetIndex = 0; signatureSheetIndex < signatureSheets; signatureSheetIndex++) {
          int[] sheetSourcePageNumbers = signatures[signatureIndex][signatureSheetIndex];
          for (int signatureSheetPageIndex = 0; signatureSheetPageIndex < 4; signatureSheetPageIndex++) {
            generatedPageCount++;
            // We assume that, if generating to a given stream, it is for a viewer, and we don't want to render
            // every other signature page upside down:
            boolean upsideDown = (out == null) && signatureSheetPageIndex > 1;
            boolean even = signatureSheetPageIndex % 2 == 0;
            if (even) {
              document.newPage();
            }
            int totalPageNumber = sheetSourcePageNumbers[signatureSheetPageIndex];
            if (totalPageNumber > 0) {
              PageRef pageRef = outputPages.get(totalPageNumber - 1);
              if (pageRef.getPdfReader() != null) {
                PdfImportedPage importedPage = writer.getImportedPage(pageRef.getPdfReader(), pageRef.getPageNumber());
                Image pageImage = Image.getInstance(importedPage);
                pageImage.setAbsolutePosition(0f, 0f);
                pageImage.scaleAbsolute(
                  pageRatio * halfSize.getWidth(), pageRatio * halfSize.getHeight()
                );
                if (upsideDown) {
                  // Rotate by Tau/2, then translate by page.width, page.height
                  pageImage.setRotation((float) Math.PI);
                  pageImage.setAbsolutePosition(even ? spineOffset + halfSize.getWidth() : edgeOffset, yOffset);
                } else {
                  pageImage.setAbsolutePosition(even ? edgeOffset : spineOffset + halfSize.getWidth(), yOffset);
                }
                cb.addImage(pageImage);
              } else {
                document.newPage();
              }
            }
            if (even) {
              if (addSpineImage) {
                addSpineImage(cb);
              }
              if (usingTrimLines) {
                addTrimLines(cb, halfSize, horizontalTrimRatio, verticalTrimRatio);
              }
            }
            setProgress(generatedPageCount, totalSignaturePageCount);
          }
        }
        if (out == null) {
          document.close();
        }
      }
      if ((out != null) && (document != null)) {
        document.close();
      }
      printStatus(translate("generated") + " " + signatures.length + " " + translate("signatures") + ".");
      pdfSignatureOutputIsValid = true;
    } catch (Exception e) {
      handleException(e);
      pdfSignatureOutputIsValid = false;
    }
  }

  public boolean hasValidPDFOutput
  ()
  {
    return pdfOutputIsValid;
  }

  public boolean hasValidPDFSignatureOutput
  ()
  {
    return pdfSignatureOutputIsValid;
  }

  public PageRef getPageRef
  (int absolutePageNumber)
  throws Exception
  {
    List<PageRef> pages = getPages();
    if ((absolutePageNumber < 1) || (absolutePageNumber > pages.size())) {
      throw new Exception("Invalid page number " + absolutePageNumber + " for book of " + pages.size() + " pages");
    }
    return pages.get(absolutePageNumber - 1);
  }

  private void addTrimLines
  (PdfContentByte cb, Rectangle halfSize, float horizontalTrimRatio, float verticalTrimRatio)
  {
    cb.setLineWidth(1.0f);
    cb.setColorStroke(Color.BLACK);
    float pageWidth = halfSize.getWidth() * 2.0f;
    float pageHeight = halfSize.getHeight();
    float trimWidth = halfSize.getWidth() * horizontalTrimRatio;
    float trimHeight = halfSize.getHeight() * verticalTrimRatio;
    cb.moveTo(0.0f, pageHeight - trimHeight);
    cb.lineTo(pageWidth, pageHeight - trimHeight);
    cb.moveTo(0.0f, trimHeight);
    cb.lineTo(pageWidth, trimHeight);
    cb.moveTo(trimWidth, 0.0f);
    cb.lineTo(trimWidth, pageHeight);
    cb.moveTo(pageWidth - trimWidth, 0.0f);
    cb.lineTo(pageWidth - trimWidth, pageHeight);
    cb.stroke();
  }

  private void addSpineImage
  (PdfContentByte cb)
  {
    int nLines = 34;
    // An approximation based on observation with a standard library case
    // binding and "Double-A" 80gsm paper:
    float pointsWidth = 34f + 68f * (((float)pages.size()) / 750f);
    float signatureMidX = signaturePageSize.getRectangle().getHeight() / 2.0f;
    float signatureHeight = signaturePageSize.getRectangle().getWidth();
    float lineWidth = pointsWidth/(float)nLines;
    cb.setLineWidth(lineWidth);
    cb.saveState();
    PdfGState newGState = new PdfGState();
    newGState.setStrokeOpacity(0.5f);
    cb.setGState(newGState);
    for (int i = 0; i < nLines / 2; i++) {
      float ratio = 0.3f  + 0.7f * (((float)i) / ((float)nLines / 2.0f));
      if (ratio > 1.0f) {
        ratio = 1.0f;
      }
      Color color = null;
      try {
        color = new Color(ratio, ratio, ratio);
      } catch (Exception e) {
        System.err.println("ratio: " + ratio);
      }
      cb.setColorStroke(color);
      cb.moveTo(signatureMidX - i * lineWidth, 0.0f);
      cb.lineTo(signatureMidX - i * lineWidth, signatureHeight);
      cb.moveTo(signatureMidX + i * lineWidth, 0.0f);
      cb.lineTo(signatureMidX + i * lineWidth, signatureHeight);
      cb.stroke();
    }
    cb.restoreState();
    cb.setColorStroke(Color.BLACK);
    cb.moveTo(signatureMidX, 0.0f);
    cb.lineTo(signatureMidX, signatureHeight);
    cb.stroke();
  }

}
