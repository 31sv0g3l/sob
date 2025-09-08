package com.binderator.util;


public interface ThrowingConsumer<T, E extends Exception> {

  void apply(T t) throws E;

}
