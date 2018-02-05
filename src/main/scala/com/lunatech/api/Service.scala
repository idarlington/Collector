package com.lunatech.api

import com.lunatech.collector.{TiledVehicle, TiledVehicles}
import akka.actor.{Actor, ActorLogging, Props}

object ServiceActor {
	final case class GetVehicles()

	def props: Props = Props[ServiceActor]()
}

class ServiceActor extends Actor with ActorLogging{

  import ServiceActor._

	def receive: Receive = {
		case GetVehicles => {
			log.info("got request")
			sender() ! TiledVehicles(List(TiledVehicle("9423",180.0,33.945698,-118.291954,"204_106_1","204",159,true,(5616,13095))))
		}
	}
}
