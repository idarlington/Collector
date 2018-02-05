package com.lunatech.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.lunatech.api.ServiceActor._
import com.lunatech.collector.{AllVehicles, JsonFormatSupport, TiledVehicle, TiledVehicles}

import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout

trait Routes extends JsonFormatSupport {

	implicit def system : ActorSystem

	lazy val log = Logging( system, classOf[ Routes ] )

	def serviceActor : ActorRef

	implicit lazy val timeout = Timeout( 3.seconds )

	lazy val vehicleRoutes : Route = {
		pathPrefix( "vehicles" / "list" ) {
			concat(
				pathEnd {
					concat(
						get {
							val vehicles : Future[ TiledVehicles ] = ( serviceActor ? GetVehicles ).mapTo[ TiledVehicles ]
              /*val vehicles_ : Future[AllVehicles] = (serviceActor ? ListVehicles).mapTo[AllVehicles]*/
							complete( vehicles )
						}
					)
				}
			)
		}
	}

}