package io.github.vigoo.examples.scala.dynamodb.implementations.common

trait DynamoDBNames {
  protected val serviceNameColumn = "service_name"
  protected val nameColumn = "name"
  protected val statusColumn = "status"
  protected val valueColumn = "value"

  protected val testServiceName = "X"

  protected val tableName: String = "example"
}
