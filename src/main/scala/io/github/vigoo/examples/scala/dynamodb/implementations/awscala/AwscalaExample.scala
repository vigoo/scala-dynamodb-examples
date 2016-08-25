package io.github.vigoo.examples.scala.dynamodb.implementations.awscala

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import io.github.vigoo.examples.scala.dynamodb.Example

import awscala._, dynamodbv2._
import io.github.vigoo.examples.scala.dynamodb.implementations.common.DynamoDBNames

class AwscalaExample extends Example with DynamoDBNames {
  override val title = "awscala implementation"

  implicit private val dynamoDB = DynamoDB.local()
  private val exp = DynamoDBExpectedAttributeValue

  override def createTable(): Unit = {
    dynamoDB.table(tableName) match {
      case Some(table) =>
        println(s"Deleting existing table $tableName")
        dynamoDB.delete(table)
      case None =>
    }

    dynamoDB.create(
      Table(
        name = tableName,
        hashPK = serviceNameColumn,
        rangePK = None,
        attributes = Seq(
          AttributeDefinition(serviceNameColumn, AttributeType.String)
//          AttributeDefinition(nameColumn, AttributeType.String),
//          AttributeDefinition(statusColumn, AttributeType.String),
//          AttributeDefinition(valueColumn, AttributeType.Number)
        ),
        localSecondaryIndexes = Seq.empty))

    println(s"Table $tableName created.")
  }

  override def updateItemStatus(name: String): Unit = {
    val r = for (
      table <- dynamoDB.table(tableName);
      existingItem <- table.get(testServiceName);
      valueAttribute <- existingItem.attributes.find(_.name == valueColumn);
      valueStr <- valueAttribute.value.n;
      existingValue = valueStr.toInt
    ) yield existingValue

    r match {
      case Some(existingValue) =>
        try {
          dynamoDB.putConditional(
            tableName,
            serviceNameColumn -> testServiceName,
            nameColumn -> name,
            statusColumn -> s"$name won",
            valueColumn -> (existingValue + 1)
          )(Seq(
            nameColumn -> exp.eq(name),
            valueColumn -> exp.eq(existingValue)
          ))
          println(s"$name updated the record")
        }
        catch {
          case _: ConditionalCheckFailedException =>
            println(s"$name could not update the record")
        }
      case None =>
        println("Failed to get existing value")
    }
  }

  override def registerItem(name: String, value: Int): Unit = {
    try {
      dynamoDB.putConditional(
        tableName,
        serviceNameColumn -> testServiceName,
        nameColumn -> name,
        statusColumn -> "Initialized",
        valueColumn -> value
      )(Seq(
        statusColumn -> exp.isNull
      ))
      println(s"Registered $name")
    }
    catch {
      case _: ConditionalCheckFailedException =>
        println(s"Not registered $name, item already existed")
    }
  }

  override def getItemStatus(): (String, Int) = {
    dynamoDB.table(tableName) match {
      case Some(table) =>
        table.get(testServiceName) match {
          case Some(item) =>
            (for (
              statusAttribute <- item.attributes.find(_.name == statusColumn);
              valueAttribute <- item.attributes.find(_.name == valueColumn);
              status <- statusAttribute.value.s;
              valueStr <- valueAttribute.value.n;
              value = valueStr.toInt)
              yield (status, value)) match {
                case Some(result) => result
                case None => ("Could not read attributes", 0)
              }
          case None =>
            (s"Item $testServiceName not found", 0)
        }
      case None =>
        (s"Table $tableName not found", 0)

    }
  }
}
