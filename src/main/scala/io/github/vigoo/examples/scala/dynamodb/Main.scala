package io.github.vigoo.examples.scala.dynamodb

import io.github.vigoo.examples.scala.dynamodb.implementations.mock.MockExample
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Main extends App {

  val impl = new MockExample
  runExample(impl)

  private def runExample(impl: Example): Unit = {
    impl.createTable()

    val firstClient = Future {
      impl.registerItem("A")
      impl.updateItemStatus("A")
    }
    val secondClient = Future {
      impl.registerItem("B")
      impl.updateItemStatus("B")
    }

    firstClient.onComplete { _ =>
      secondClient.onComplete { _ =>
        println(s"Result: ${impl.getItemStatus()}")
      }
    }
  }
}
