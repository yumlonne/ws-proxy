package com.example.app.ws

import java.util.Date

import org.json4s.JsonDSL._
import org.json4s._
import org.scalatra._
import org.scalatra.atmosphere._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.scalatra.scalate.ScalateSupport

import com.example.app.model.repository.RoomRepository

import scala.concurrent.ExecutionContext.Implicits.global

class ChatController extends ScalatraServlet
  with ScalateSupport
  with JValueResult
  with JacksonJsonSupport
  with SessionSupport
  with AtmosphereSupport {

  implicit protected val jsonFormats: Formats = DefaultFormats

  atmosphere("/chat/:room") {
    val clientOpt = for {
      userId <- params.get("user_id")
      room   <- RoomRepository.data.get(params("room"))
      if room.users contains userId
    } yield room.atmoClient
    println(s"clientOpt => ${clientOpt.isDefined}")

    clientOpt.getOrElse(DenyAtmosphereClient)
  }

  lazy val DenyAtmosphereClient: AtmosphereClient = new AtmosphereClient {
    def receive = {
      case Connected =>
        send("access denied.")
        this.receive(Disconnected)
      case Disconnected =>
        send("called disconnected")
      case _ =>
    }
  }

}
