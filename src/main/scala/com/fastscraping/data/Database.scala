package com.fastscraping.data

trait Database {
  /**
   * @param index The index (or table or collection) in the database
   * @param column Name of the column under which the text will be stored
   * @param text The text data itself
   * @param documentId Find the document with `documentId` and save text under
   */
  def saveText(index: String, documentId: String, column: String, text: String)
}
