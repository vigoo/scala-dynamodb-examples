package io.github.vigoo.examples.scala.dynamodb

trait Example {
  def createTable(): Unit
  def registerItem(name: String): Unit
  def updateItemStatus(name: String): Unit
  def getItemStatus(): String
}
