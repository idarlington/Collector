package com.lunatech.collector

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.pattern.pipe



class StreamActor extends Actor with ActorLogging with JsonFormatSupport {

  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)
  val URL = "http://api.metro.net/agencies/lametro/vehicles/"

  def receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) => {
      Unmarshal(entity).to[VehicleList].map { jsonString =>
        println(jsonString)
      }
    }
    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
    case _ => http.singleRequest(HttpRequest(uri = URL))
      .pipeTo(self)
  }
}