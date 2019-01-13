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
    val atmoClient = new AtmosphereClient {
      def receive = {
        case Connected =>
          broadcast("Connected")
        case Disconnected(disconnector, Some(error)) =>
          broadcast(s"Disconnected $disconnector")
        case Error(Some(error)) =>
        case TextMessage(text) => broadcast("ECHO: " + text, Everyone)
        case JsonMessage(json) => broadcast(json)
      }
    }
    val room = Room(roomId, Seq(), atmoClient)

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
        val newRoom = room.copy(users = room.users :+ userId)
        RoomRepository.data.update(room.id, room)

        s"""{
          "is_success":true,
          "user_id":"$userId"
        }"""
      }
    )
  }

}
