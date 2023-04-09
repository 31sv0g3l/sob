package com.binderator.util;


import java.io.*;


@SuppressWarnings({"unused", "Math"})
public class RangedFloat implements Serializable {

  @Serial
  private static final long serialVersionUID = 4122610805003869575L;
  private String name;
  private float value;
  private float lowValue;
  private float highValue;

  public RangedFloat
  (String name, float value, float lowValue, float highValue)
  {
    this.name = name;
    this.value = value;
    this.lowValue = lowValue;
    this.highValue = highValue;
  }

  public RangedFloat
  (RangedFloat rangedFloat)
  {
    name = rangedFloat.name;
    value = rangedFloat.value;
    lowValue = rangedFloat.lowValue;
    highValue = rangedFloat.highValue;
  }

  public void copy
  (RangedFloat rangedFloat)
  {
    name = rangedFloat.name;
    value = rangedFloat.value;
    lowValue = rangedFloat.lowValue;
    highValue = rangedFloat.highValue;
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

  public float getValue
  ()
  {
    return value;
  }

  public void setValue
  (float value)
  {
    if (value < lowValue) {
      this.value = lowValue;
    } else if (value > highValue) {
      this.value = highValue;
    } else {
      this.value = value;
    }
  }

  public float getLowValue
  ()
  {
    return lowValue;
  }

  public void setLowValue
  (float lowValue)
  {
    this.lowValue = lowValue;
    if (value < lowValue) {
      this.value = lowValue;
    }
    if (highValue < value) {
      highValue = value;
    }
  }

  public float getHighValue
  ()
  {
    return highValue;
  }

  public void setHighValue
  (float highValue)
  {
    this.highValue = highValue;
    if (value > highValue) {
      value = highValue;
    }
    if (lowValue > value) {
      lowValue = value;
    }
  }

  public String toString
  ()
  {
    return name + "(" + value + ":[" + lowValue + '-' + highValue + "])";
  }

}
