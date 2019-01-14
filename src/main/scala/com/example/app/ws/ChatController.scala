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
    val userOpt = for {
      userId <- params.get("user_id")
      userStatus <- UserRepository.data.find(_._1 == userId).map(_._2)
    } yield (userId, userStatus)

    userOpt.fold(DenyAtmosphereClient) { case (userId, userStatus) =>
      new AtmosphereClient {
        def receive = {
          case Connected =>
            UserRepository.data.update(userId, Working(uuid))
            broadcast(s"Connected -> $userId($uuid)", Everyone)
          case TextMessage(text) => broadcast("ECHO: " + text, Everyone)
        }
      }
    }
  }

  get("/post") {
    val targets = params.get("targets").map(_.split(",").toSeq).getOrElse(Seq())
    val message = params.get("message").getOrElse("Touch")

    val filter = new ClientFilter(null) {
      def apply(v1: AtmosphereResource): Boolean = {
        val uuids = UserRepository.data.filter(targets contains _._1).values.collect {
          case Working(uuid) => uuid
        }.toSeq

        uuids.contains(v1.uuid)
      }
    }
    AtmosphereClient.broadcastAll(message, filter)
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
