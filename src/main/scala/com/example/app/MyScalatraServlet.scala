package com.example.app

import org.json4s.JsonDSL._
import org.json4s._
import org.scalatra._
import org.scalatra.atmosphere._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.scalatra.scalate.ScalateSupport

import com.example.app.model.repository.RoomRepository
import com.example.app.model.Room

import scala.concurrent.ExecutionContext.Implicits.global

class MyScalatraServlet extends ScalatraServlet
  with ScalateSupport
  with JValueResult
  with JacksonJsonSupport
  with SessionSupport
  with AtmosphereSupport {
  implicit protected val jsonFormats: Formats = DefaultFormats

  get("/") {
    views.html.hello()
  }

  get("/create/room") {
    val roomId = java.util.UUID.randomUUID.toString
    val room = new Room(roomId, collection.mutable.ArrayBuffer[String]()) {
      def receive = {
        case Connected =>
          broadcast("Connected")
        case TextMessage(text) => broadcast("ECHO: " + text, Everyone)
      }
    }

    RoomRepository.data += roomId -> room

    // response
    s"""{
      "is_success":true,
      "room_id":"$roomId"
    }"""
  }

  get("/add/user") {
    val roomEither: Either[String, Room] = for {
      roomId <- params.get("room_id").toRight("room_id required")
      room   <- RoomRepository.data.get(roomId).toRight(s"not found: room $roomId")
    } yield room

    roomEither.fold(reason =>
      s"""{
        "is_success":false,
        "reason":"$reason"
      }"""
    , room => {
        val userId = java.util.UUID.randomUUID.toString
        room.users += userId

        s"""{
          "is_success":true,
          "user_id":"$userId"
        }"""
      }
    )
  }

  get("/post/:room_id") {
    val roomId = params("room_id")
    val targetsOpt = params.get("targets")
    val excludes = params.get("excludes").getOrElse(Seq[String]())

    for {
      room <- RoomRepository.data.get(roomId).toRight(s"not found: room $roomId")
      message <- params.get("message").toRight("message required")
    } {
      val filter = new Everyone
      room.broadcast(message, filter)
    }
  }

}
