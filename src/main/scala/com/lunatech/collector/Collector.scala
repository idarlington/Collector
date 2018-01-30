package com.lunatech.collector

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import akka.actor.Props

object collector extends App  {

	implicit val system = akka.actor.ActorSystem()

	val URL = "http://api.metro.net/agencies/lametro/vehicles/"
	val KafkaHost = "kafka"
	val props = new Properties()

	val streamActor = system.actorOf(Props(new StreamActor()))

	import scala.language.postfixOps
	import scala.concurrent.duration._
	import system.dispatcher

	system.scheduler.schedule(0 milliseconds, 10 seconds, streamActor, "")
}
