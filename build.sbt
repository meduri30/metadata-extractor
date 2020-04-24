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