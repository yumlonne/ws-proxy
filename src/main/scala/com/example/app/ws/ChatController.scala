package com.example.app.ws

import java.util.Date

import org.json4s.JsonDSL._
import org.json4s._
import org.scalatra._
import org.scalatra.atmosphere._
import org.atmosphere.cpr.AtmosphereResource
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.scalatra.scalate.ScalateSupport

import com.example.app.model.repository.UserRepository
import com.example.app.model._

import scala.concurrent.ExecutionContext.Implicits.global

class ChatController extends ScalatraServlet
  with ScalateSupport
  with JValueResult
  with JacksonJsonSupport
  with SessionSupport
  with AtmosphereSupport {

  implicit protected val jsonFormats: Formats = DefaultFormats

  atmosphere("/chat") {
    val roomOpt = for {
      userId <- params.get("user_id")
      room   <- Some(RoomClient)
      if UserRepository.data.find(_ == (userId -> Ready)).isDefined
    } yield room
    println(s"clientOpt => ${roomOpt.isDefined}")

    roomOpt.fold(DenyAtmosphereClient) { room =>
      val userId = params("user_id")
      var next = true
      while (next) {
        val cnt = UserRepository.data.count(_._2 == Waiting)
        if (cnt == 0) {
          UserRepository.data.update(userId, Waiting)
          next = false
        }
        Thread.sleep(100)
      }
      room
    }
  }

  get("/post") {
    val targets = params.get("targets").map(_.split(",").toSeq).getOrElse(Seq())
    val message = params.get("message").getOrElse("Touch")

    println(targets)

    // FIXME: filter
    val filter = new ClientFilter(null) {
      def apply(v1: AtmosphereResource): Boolean = {
        val uuids = UserRepository.data.filter(targets contains _._1).values.collect {
          case Working(uuid) => uuid
        }.toSeq
        println(uuids)
        println(v1.uuid)

        val res = uuids.contains(v1.uuid)
        println(res)
        res
      }
    }
    RoomClient.broadcast(message, filter)
  }

  lazy val RoomClient = new AtmosphereClient {
    def receive = {
      case Connected =>
        val Some((userId, status)) = UserRepository.data.find(_._2 == Waiting)
        val newStatus = Working(uuid)
        UserRepository.data.update(userId, newStatus)
        broadcast(s"Connected -> $uuid", Everyone)
      case TextMessage(text) => broadcast("ECHO: " + text, Everyone)
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
