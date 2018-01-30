package com.lunatech.collector

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport


trait JsonFormatSupport extends SprayJsonSupport {
  final case class Vehicle(id: String, heading : Double, latitude: Double, longitude: Double, run_id: String,
                           route_id: String, seconds_since_report: Int, predictable: Boolean)
  final case class VehicleList(items : List[Vehicle])

  import spray.json.DefaultJsonProtocol._
  implicit val vehicle = jsonFormat8(Vehicle)
  implicit val vehicleList = jsonFormat1(VehicleList)
}