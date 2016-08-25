package io.github.vigoo.examples.scala.dynamodb.implementations.awswrap

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model.{ComparisonOperator, CreateTableRequest, ExpectedAttributeValue, PutItemRequest}
import com.github.dwhjames.awswrap.dynamodb._
import io.github.vigoo.examples.scala.dynamodb.Example

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

class AwsWrapExample extends Example {
  override def title: String = "aws-wrap implementation"

  private val testServiceName = "X"

  private case class Item(serviceName: String, name: String, status: String, value: Int)

  private object Item {
    val tableName = "example"

    object Attributes {
      val serviceName = "service_name"
      val name = "name"
      val status = "status"
      val value = "value"
    }

    val tableRequest =
      new CreateTableRequest()
        .withTableName(tableName)
        .withKeySchema(Schema.hashKey(Attributes.serviceName))
        .withAttributeDefinitions(Schema.stringAttribute(Attributes.serviceName))
        .withProvisionedThroughput(Schema.provisionedThroughput(10L, 5L))

    implicit object itemSerializer extends DynamoDBSerializer[Item] {
      override val tableName = Item.tableName
      override val hashAttributeName = Attributes.serviceName

      override def primaryKeyOf(item: Item): Map[String, AttributeValue] =
        Map(Attributes.serviceName -> item.serviceName)

      override def toAttributeMap(item: Item): Map[String, AttributeValue] =
        Map(
          Attributes.serviceName -> item.serviceName,
          Attributes.name -> item.name,
          Attributes.status -> item.status,
          Attributes.value -> item.value
        )

      override def fromAttributeMap(item: mutable.Map[String, AttributeValue]): Item =
        Item(
          serviceName = item(Attributes.serviceName),
          name = item(Attributes.name),
          status = item(Attributes.status),
          value = item(Attributes.value)
        )
    }

  }

  private lazy val sdkClient: AmazonDynamoDBAsyncClient =
    new AmazonDynamoDBAsyncClient(new BasicAWSCredentials("test", "test"))
      .withEndpoint("http://localhost:8000")

  private lazy val client = new AmazonDynamoDBScalaClient(sdkClient)
  private lazy val mapper = AmazonDynamoDBScalaMapper(client)

  override def createTable(): Unit = {
    val deleteResult = client.deleteTable(Item.tableName)
    Await.ready(deleteResult, 10.seconds)

    val futureResult = client.createTable(Item.tableRequest)

    Try { Await.result(futureResult, 10.seconds) } match {
      case Success(result) =>
        println(s"Created table ${Item.tableName}")
      case Failure(reason) =>
        println(s"Failed to create table ${Item.tableName}: $reason")
    }
  }

  override def registerItem(name: String, value: Int): Unit = {
    val item = Item(testServiceName, name, "Initialized", value)
    val futureResult = client.putItem(
      new PutItemRequest(Item.tableName, Item.itemSerializer.toAttributeMap(item).asJava)
          .withExpected(Map(
            Item.Attributes.serviceName -> new ExpectedAttributeValue().withComparisonOperator(ComparisonOperator.NULL)).asJava
          ))
    Try { Await.result(futureResult, 10.seconds) } match {
      case Success(putResult) =>
        println(putResult)
        println(s"Registered $name")
      case Failure(reason) =>
        println(s"Not registered $name: $reason")
    }
  }

  override def updateItemStatus(name: String): Unit = {
    val futureNewItem: Future[Item] = for {
      Some(item) <- mapper.loadByKey[Item](testServiceName)
      existingValue = item.value
    } yield Item(testServiceName, name, s"$name won", existingValue + 1)

    val futureResult = futureNewItem.flatMap { newItem =>
      client.putItem(
        new PutItemRequest(Item.tableName, Item.itemSerializer.toAttributeMap(newItem).asJava)
          .withExpected(Map(
            Item.Attributes.name -> new ExpectedAttributeValue(name),
            Item.Attributes.value -> new ExpectedAttributeValue(newItem.value - 1)
          ).asJava)
      )
    }

    Try { Await.result(futureResult, 10.seconds) } match {
      case Success(_) =>
        println(s"$name updated the record")
      case Failure(reason) =>
        println(s"$name could not update the record: $reason")
    }
  }

  override def getItemStatus(): (String, Int) = {
    val futureResult = for {
      Some(item) <- mapper.loadByKey[Item](testServiceName)
    } yield (item.status, item.value)

    Await.result(futureResult, 10.seconds)
  }
}
