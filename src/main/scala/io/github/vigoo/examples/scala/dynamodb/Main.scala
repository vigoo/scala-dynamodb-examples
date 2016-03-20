package io.github.vigoo.examples.scala.dynamodb

import io.github.vigoo.examples.scala.dynamodb.implementations.awssdk.JavaAwsSdkExample
import io.github.vigoo.examples.scala.dynamodb.implementations.mock.MockExample

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Main extends App {

  val mockImpl = new MockExample
  runExample(mockImpl)

  val javaSdkImpl = new JavaAwsSdkExample
  runExample(javaSdkImpl)

  private def runExample(impl: Example): Unit = {
    println(s"*** Running example ${impl.title}")

    try {
      impl.createTable()

      val firstClient = Future {
        impl.registerItem("A")
        impl.updateItemStatus("A")
      }
      val secondClient = Future {
        impl.registerItem("B")
        impl.updateItemStatus("B")
      }

      Await.ready(Future.sequence(List(firstClient, secondClient)), 10.seconds)

      println(s"Result: ${impl.getItemStatus()}")
    }
    finally {
      println("***")
      println()
    }
  }
}
