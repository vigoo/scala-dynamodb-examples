name := "scala-dynamodb-examples"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-stm" %% "scala-stm" % "0.7",
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.10.62",
  "com.github.seratch" %% "awscala" % "0.5.+"
)
