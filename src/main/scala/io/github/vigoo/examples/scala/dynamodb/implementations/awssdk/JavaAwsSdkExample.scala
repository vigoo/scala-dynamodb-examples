package io.github.vigoo.examples.scala.dynamodb.implementations.awssdk

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Expected, Item, Table}
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClient}
import io.github.vigoo.examples.scala.dynamodb.Example
import io.github.vigoo.examples.scala.dynamodb.implementations.common.DynamoDBNames

class JavaAwsSdkExample extends Example with DynamoDBNames {
  override val title = "java aws sdk implementation"

  private lazy val client: AmazonDynamoDB = {
    val result = new AmazonDynamoDBClient(new BasicAWSCredentials("test", "test"))
    result.withEndpoint("http://localhost:8000")
  }
  private lazy val db: DynamoDB = new DynamoDB(client)

  override def createTable(): Unit = {
    val existingTable = getTable
    try {
      if (existingTable.describe().getTableStatus == TableStatus.ACTIVE.toString) {
        println(s"Deleting existing table $tableName")
        existingTable.delete()
      }
    }
    catch {
      case _: ResourceNotFoundException => // ignore
    }

    val attributeDefs = List(
      new AttributeDefinition().withAttributeName(serviceNameColumn).withAttributeType(ScalarAttributeType.S)

      // NOTE: Only attributes used by the key schema must be specified
      // new AttributeDefinition().withAttributeName(nameColumn).withAttributeType(ScalarAttributeType.S),
      // new AttributeDefinition().withAttributeName(statusColumn).withAttributeType(ScalarAttributeType.S)
    )

    val keySchema = List(
      new KeySchemaElement().withAttributeName(serviceNameColumn).withKeyType(KeyType.HASH)
    )

    val request = new CreateTableRequest()
      .withTableName(tableName)
      .withKeySchema(keySchema : _*)
      .withAttributeDefinitions(attributeDefs : _*)
      .withProvisionedThroughput(
        new ProvisionedThroughput()
          .withReadCapacityUnits(5L)
          .withWriteCapacityUnits(5L)
      )
    db.createTable(request)
    println(s"Created table $tableName")
  }

  override def updateItemStatus(name: String): Unit = {
    val table = getTable

    val existingItem = table.getItem(serviceNameColumn, testServiceName)
    val existingValue = existingItem.getInt(valueColumn)

    val spec = new PutItemSpec()
      .withItem(new Item()
        .withString(serviceNameColumn, testServiceName)
        .withString(nameColumn, name)
        .withString(statusColumn, s"$name won")
        .withInt(valueColumn, existingValue + 1))
      .withExpected(
        new Expected(nameColumn).eq(name),
        new Expected(valueColumn).eq(existingValue))
    try {
      table.putItem(spec)
      println(s"$name updated the record")
    }
    catch {
      case _: ConditionalCheckFailedException =>
        println(s"$name could not update the record")
    }
  }

  override def getItemStatus(): (String, Int) = {
    val table = getTable
    val item = table.getItem(serviceNameColumn, testServiceName)
    (item.getString(statusColumn), item.getInt(valueColumn))
  }

  override def registerItem(name: String, value: Int): Unit = {
    val table = getTable
    val spec = new PutItemSpec()
        .withItem(new Item()
          .withString(serviceNameColumn, testServiceName)
          .withString(nameColumn, name)
          .withString(statusColumn, "Initialized")
          .withInt(valueColumn, value))
        .withExpected(new Expected(statusColumn).notExist())
    try {
      table.putItem(spec)
      println(s"Registered $name")
    }
    catch {
      case _: ConditionalCheckFailedException =>
        println(s"Not registered $name, item already existed")
    }
  }

  private def getTable: Table = {
    db.getTable(tableName)
  }
}
