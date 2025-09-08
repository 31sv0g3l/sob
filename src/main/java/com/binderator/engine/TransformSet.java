package com.binderator.engine;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class TransformSet implements Serializable {

  @Serial
  private static final long serialVersionUID = 1095643075129104426L;
  private String name = null;
  private String comment = null;
  private List<PageRange> pageRanges = new ArrayList<>();
  ArrayList<Transform> transforms = new ArrayList<>();

  public TransformSet()
  {}

  public TransformSet(TransformSet transformSet)
  {
    name = transformSet.name;
    comment = transformSet.comment;
    pageRanges = new ArrayList<>();
    pageRanges.addAll(transformSet.getPageRanges());
    transforms = new ArrayList<>();
    for (Transform transform : transformSet.getTransforms()) {
      transforms.add(new Transform(transform));
    }
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }

  public void setPageRanges(List<PageRange> pageRanges)
  {
    this.pageRanges = pageRanges;
  }

  public void addPageRange(PageRange pageRange)
  {
    if (pageRanges == null) {
      pageRanges = new ArrayList<>();
    }
    pageRanges.add(pageRange);
  }

  public Collection<PageRange> getPageRanges()
  {
    return pageRanges;
  }

  public String getPageRangesString()
  {
    return PageRange.toString(pageRanges);
  }

  /**
   * Moves the given transform up (earlier, to a lower index) in the list of transforms.
   */
  public void moveUp(Transform transform)
  {
    if (transform == null) {
      return;
    }
    int index = transforms.indexOf(transform);
    if (index < 0) {
      return;
    }
    if (index == 0) {
      return;
    }
    transforms.remove(transform);
    transforms.add(index - 1, transform);
  }

  /**
   * Moves the given transform down (later, to a lower index) in the list of transforms.
   */
  public void moveDown(Transform transform)
  {
    if (transform == null) {
      return;
    }
    int index = transforms.indexOf(transform);
    if (index < 0) {
      return;
    }
    if (index == transforms.size() - 1) {
      return;
    }
    transforms.remove(transform);
    transforms.add(index + 1, transform);
  }

  public void remove(Transform transform)
  {
    transforms.remove(transform);
  }

  public Collection<Transform> getTransforms()
  {
    return transforms;
  }

  public void add(Transform transform)
  {
    transforms.add(transform);
  }

  @Override
  public String toString()
  {
    if (name != null) {
      return name;
    } else {
      return "TransformSet(\"" + name + "\")";
    }
  }

}
