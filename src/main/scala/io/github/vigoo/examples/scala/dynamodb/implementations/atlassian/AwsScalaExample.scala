package io.github.vigoo.examples.scala.dynamodb.implementations.atlassian

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import io.atlassian.aws.{AmazonClient, AmazonClientConnectionDef, Credential}
import io.github.vigoo.examples.scala.dynamodb.Example
import io.github.vigoo.examples.scala.dynamodb.implementations.common.DynamoDBNames
import io.atlassian.aws.dynamodb._

import scalaz.{-\/, \/-}

class AwsScalaExample extends Example with DynamoDBNames {
  override val title = "aws-scala implementation"

  object table extends Table {

  }

  implicit private  val dynamoClient = AmazonClient.withClientConfiguration[AmazonDynamoDBClient](
    AmazonClientConnectionDef.default.copy(
      endpointUrl = Some("http://localhost:8000"),
      credential = Some(Credential.static("test", "test"))
    ),
    None,
    None
  )

  override def createTable(): Unit = {
    ???
  }

  override def updateItemStatus(name: String): Unit = ???

  override def getItemStatus(): (String, Int) = ???

  override def registerItem(name: String): Unit = ???
}
