name := "metadata-extractor"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += "jitpack".at("https://jitpack.io")

libraryDependencies += "io.circe" %% "circe-json-schema" % "0.1.0"

// https://mvnrepository.com/artifact/io.circe/circe-literal
libraryDependencies += "io.circe" %% "circe-literal" % "0.12.3"
libraryDependencies += "io.circe" %% "circe-parser" % "0.12.3"
libraryDependencies += "io.circe" %% "circe-generic" % "0.12.3"

// dependencies for http clients
libraryDependencies ++= Seq("com.softwaremill.sttp.client" %% "core" % "2.0.0-RC1")

// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.2"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.2"

// File Watcher
val AkkaVersion = "2.5.31"
libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % "2.0.0",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion
)