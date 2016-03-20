# scala-dynamodb-examples

This repository contains a simple DynamoDB example and tries out various libraries to see how the example scenario can be implemented with them in Scala.

## The task
All implementations will perform the following (oversimplified and not very realistic) task:

1. Create a DynamoDB table with a primary key `service_name`
2. Execute two parallel threads, one called `A` the other called `B`. In each:
 - Try to add an item to the table with `service_name` `X`, `name` `A` or `B` and `status` `Initialized` but only if the entry for `X` does not exist yet.
 - Try to update the item's status to `x won` if `host` is `x` 
3. Get the item and print the status

## Skeleton
The first implementation does not use *DynamoDB* at all, but defines a skeleton for all the other implementations.


## AWS Java API
The first 