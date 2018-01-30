package com.lunatech.collector

import java.util.Properties

import akka.actor.Props
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

object collector extends App  {

	implicit val system = akka.actor.ActorSystem()

	val URL = "http://api.metro.net/agencies/lametro/vehicles/"
	val KafkaHost = "kafka"
	val props = new Properties()

	val streamActor = system.actorOf(Props(new DataCollector()))

	import scala.language.postfixOps
	import scala.concurrent.duration._
	import system.dispatcher

	system.scheduler.schedule(0 milliseconds, 10 seconds, streamActor, "")
}


import akka.actor.{ Actor, ActorLogging }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings }
import akka.util.ByteString

class DataCollector extends Actor
  with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)
  val URL = "http://api.metro.net/agencies/lametro/vehicles/"

  override def preStart() = {}

  def receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info("Got response, body: " + body.utf8String)
      }
    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
	  case _ => http.singleRequest(HttpRequest(uri = URL))
      .pipeTo(self)
  }
}