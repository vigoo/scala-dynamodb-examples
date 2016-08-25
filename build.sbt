name := "scala-dynamodb-examples"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += Resolver.bintrayRepo("dwhjames", "maven")

libraryDependencies ++= Seq(
  "org.scala-stm" %% "scala-stm" % "0.7",
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.10.62",
  "com.github.seratch" %% "awscala" % "0.5.+",
  "io.atlassian.aws-scala" %% "aws-scala-dynamodb"  % "6.0.0",
  "com.github.dwhjames" %% "aws-wrap" % "0.8.0"
)
