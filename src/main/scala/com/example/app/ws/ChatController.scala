package com.example.app.ws

import java.util.Date

import org.json4s.JsonDSL._
import org.json4s._
import org.scalatra._
import org.scalatra.atmosphere._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.scalatra.scalate.ScalateSupport

import scala.concurrent.ExecutionContext.Implicits.global

class ChatController extends ScalatraServlet
  with ScalateSupport
  with JValueResult
  with JacksonJsonSupport
  with SessionSupport
  with AtmosphereSupport {

  implicit protected val jsonFormats: Formats = DefaultFormats

  atmosphere("/the-chat") {
    new AtmosphereClient {
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
  }
}
