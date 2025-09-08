package com.binderator.persistence;

import com.binderator.engine.Book;

public interface BinderatorStore {

  void saveBook(Book book, String path, boolean emptySchema)
  throws Exception;

  Book loadBook(String path)
  throws Exception;

}
