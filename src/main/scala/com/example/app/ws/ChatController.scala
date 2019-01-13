package com.example.app.ws

import java.util.Date

import org.json4s.JsonDSL._
import org.json4s._
import org.scalatra._
import org.scalatra.atmosphere._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.scalatra.scalate.ScalateSupport

import com.example.app.model.repository.RoomRepository
import com.example.app.model._

import scala.concurrent.ExecutionContext.Implicits.global

class ChatController extends ScalatraServlet
  with ScalateSupport
  with JValueResult
  with JacksonJsonSupport
  with SessionSupport
  with AtmosphereSupport {

  implicit protected val jsonFormats: Formats = DefaultFormats

  atmosphere("/chat/:room") {
    println("userId:" + params.get("user_id"))
    println("roomId:" + params("room"))
    println("room: +" + RoomRepository.data.get(params("room")))
    val roomOpt = for {
      userId <- params.get("user_id")
      room   <- RoomRepository.data.get(params("room"))
      _ <- Some(println(s"users ->${room.users}"))
      if room.users.find(_ == (userId -> Ready)).isDefined
    } yield room
    println(s"clientOpt => ${roomOpt.isDefined}")

    roomOpt.fold(DenyAtmosphereClient) { room =>
      val userId = params("user_id")
      var next = true
      while (next) {
        val cnt = room.users.count(_._2 == Waiting)
        if (cnt == 0) {
          room.users.update(userId, Waiting)
          next = false
        }
        Thread.sleep(100)
      }
      room
    }
  }

  lazy val DenyAtmosphereClient: AtmosphereClient = new AtmosphereClient {
    def receive = {
      case Connected =>
        broadcast("access denied.", Everyone)
      case Disconnected(disconnector, _) =>
        broadcast("called disconnected", Everyone)
      case _ =>
        broadcast("access denied.", Everyone)
    }
  }

}
