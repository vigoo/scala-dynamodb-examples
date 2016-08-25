package io.github.vigoo.examples.scala.dynamodb

import java.util.concurrent.ExecutionException

import io.github.vigoo.examples.scala.dynamodb.implementations.awscala.AwscalaExample
import io.github.vigoo.examples.scala.dynamodb.implementations.awssdk.JavaAwsSdkExample
import io.github.vigoo.examples.scala.dynamodb.implementations.awswrap.AwsWrapExample
import io.github.vigoo.examples.scala.dynamodb.implementations.mock.MockExample

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Main extends App {

  val mockImpl = new MockExample
  runExample(mockImpl)

  val javaSdkImpl = new JavaAwsSdkExample
  runExample(javaSdkImpl)

  val awscalaImpl = new AwscalaExample
  runExample(awscalaImpl)

  val awswrapImpl = new AwsWrapExample
  runExample(awswrapImpl)

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

      Await.result(Future.sequence(List(firstClient, secondClient)), 10.seconds)

      println(s"Result: ${impl.getItemStatus()}")
    }
    catch {
      case boxed: ExecutionException =>
        println(s"Failed: ${boxed.getCause}")
      case ex: Exception =>
        println(s"Failed: $ex")
    }
    finally {
      println("***")
      println()
    }
  }
}
