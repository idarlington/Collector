name := "Collector"

version := "0.1"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "org.apache.kafka" %% "kafka" % "0.11.0.2",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0-RC1"
)
