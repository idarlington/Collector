package com.lunatech.collector

import java.util.Properties

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import spray.json.JsonParser

import scala.concurrent.duration.DurationInt

case class TiledVehicle(
	id : String,
	heading : Double,
	latitude : Double,
	longitude : Double,
	run_id : String,
	route_id : String,
	seconds_since_report : Int,
	predictable : Boolean,
	tile : (Int, Int)
) extends VehicleBuilder(
	id : String,
	heading,
	latitude,
	longitude,
	run_id,
	route_id,
	seconds_since_report,
	predictable
)

class StreamActor extends Actor with ActorLogging with JsonFormatSupport {

	import context.dispatcher

	final implicit val materializer : ActorMaterializer = ActorMaterializer(
		ActorMaterializerSettings( context.system )
	)

  val tileSystem = new TileSystem()
	val http = Http( context.system )
	val props = new Properties()
  val levelOfDetail = 15 //TODO move to config file

	val URL = "http://api.metro.net/agencies/lametro/vehicles/"
	val KafkaHost = "localhost:9092"

	props.put( "bootstrap.servers", KafkaHost )
	props.put( "client.id", "Producer" )
	props.put(
		ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
		"org.apache.kafka.common.serialization.StringSerializer"
	)
	props.put(
		ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
		"org.apache.kafka.common.serialization.StringSerializer"
	)

	val producer = new KafkaProducer[ String, String ]( props )

	def receive = {
		case HttpResponse( StatusCodes.OK, headers, entity, _ ) ⇒ {
      /*entity.toStrict(10.seconds).foreach { strict =>
        val r = strict.data.toArray
        JsonParser(r).convertTo[VehicleList]
        producer.send(
          new ProducerRecord[ String, String ]( "vehicles", r )
        )
      }*/

      import spray.json._

			Unmarshal(entity).to[VehicleList].map { vehicleList =>
        /*log.info("Vehicle: " + vehicleList.items)*/
        /*println()*/
        vehicleList.items.foreach{ vehicle =>
          val id = vehicle.id
					val heading = vehicle.heading
          val latitude = vehicle.latitude
          val longitude = vehicle.longitude
					val run_id = vehicle.run_id
					val route_id = vehicle.route_id
					val seconds_since_report = vehicle.seconds_since_report
					val predictable = vehicle.predictable
          val tile = tileSystem.latLongToTileXY(latitude,longitude,levelOfDetail)
          /*log.info(tile.toString())*/

          val tiledVehicle = TiledVehicle(id, heading, latitude, longitude, run_id, route_id, seconds_since_report, predictable, tile)
					log.info(tiledVehicle.toString)
        }
        /*val jsonString  = vehicleList.items.head.toJson*/
        /*log.info(jsonString.toString())*/
      }

			/*entity.dataBytes.runFold( ByteString( "" ) )( _ ++ _ ).foreach { body ⇒
				producer.send(
					new ProducerRecord[ String, String ]( "vehicles", body.utf8String )
				)
				log.info( "Got response, body: "+body.utf8String )
			}*/
		}
		case resp @ HttpResponse( code, _, _, _ ) ⇒
			log.info( "Request failed, response code: "+code )
			resp.discardEntityBytes()
		case _ ⇒
			http
				.singleRequest( HttpRequest( uri = URL ) )
				.pipeTo( self )
	}
}
