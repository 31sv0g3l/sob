package com.binderator.util;


import java.util.Objects;


/**
 * A simple typed pair of objects.
 */
@SuppressWarnings("unused")
public class Pair<Type1, Type2> {
  private Type1 first;
  
  private Type2 second;

  public Pair
  (Type1 first, Type2 second)
  {
    this.first = first;
    this.second = second;
  }
  
  public Type1 getFirst
  ()
  {
    return first;
  }
  
  public void setFirst
  (Type1 first)
  {
    this.first = first;
  }
  
  public Type2 getSecond
  ()
  {
    return second;
  }

  public void setSecond
  (Type2 second)
  {
    this.second = second;
  }

  @Override
  public boolean equals
  (Object other)
  {
    if (this == other) return true;
    if (!(other instanceof Pair<?, ?> pair)) return false;
    return Objects.equals(getFirst(), pair.getFirst()) && Objects.equals(getSecond(), pair.getSecond());
  }

  @Override
  public int hashCode
  ()
  {
    return Objects.hash(getFirst(), getSecond());
  }

  @Override
  public String toString
  ()
  {
    return "Pair(" + first + ", " + second + ")";
  }

}
