package io.github.vigoo.examples.scala.dynamodb.implementations.mock

import io.github.vigoo.examples.scala.dynamodb.Example
import scala.concurrent.stm._

class MockExample extends Example {
  val title = "mock implementation"

  private case class Item(name: String, status: String)
  private val item: Ref[Option[Item]] = Ref[Option[Item]](None)

  override def createTable(): Unit = {
    println("Table created")
  }

  override def updateItemStatus(name: String): Unit = {
    atomic { implicit txn =>
      item.get match {
        case Some(v) if v.name == name =>
          item.set(Some(Item(name, s"$name won")))
          println(s"$name updated the record")
        case _ =>
          println(s"$name could not update the record")
      }
    }
  }

  override def getItemStatus(): String = {
    atomic { implicit txn =>
      item.get match {
        case Some(v) => v.status
        case None => "No item were registered"
      }
    }
  }

  override def registerItem(name: String): Unit = {
    atomic { implicit txn =>
      item.get match {
        case Some(v) =>
          println(s"Not registered $name, item already existed")
        case None =>
          item.set(Some(Item(name, "Initialized")))
          println(s"Registered $name")
      }
    }
  }
}
