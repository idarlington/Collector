name := "Collector"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies  ++= Seq (
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "org.apache.kafka" %% "kafka" % "0.11.0.2"

)