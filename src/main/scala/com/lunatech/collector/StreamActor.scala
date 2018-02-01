package com.lunatech.collector

import java.util.Properties

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}


class StreamActor extends Actor with ActorLogging with JsonFormatSupport {

  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)
  val props = new Properties()

  val URL = "http://api.metro.net/agencies/lametro/vehicles/"
  val KafkaHost = "localhost:9092"

  props.put("bootstrap.servers", KafkaHost)
  props.put("client.id", "Producer")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)


  def receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) => {
      /*Unmarshal(entity).to[VehicleList].map { Vehicles =>
        log.info("Vehicle: " + Vehicles)
      }*/

      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        producer.send(new ProducerRecord[String, String]("vehicles", body.utf8String))
        log.info("Got response, body: " + body.utf8String)
      }
    }
    case resp@HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
    case _ => http.singleRequest(HttpRequest(uri = URL))
      .pipeTo(self)
  }
}