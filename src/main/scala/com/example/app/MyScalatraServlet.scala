package com.example.app

import org.json4s.JsonDSL._
import org.json4s._
import org.scalatra._
import org.scalatra.atmosphere._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.scalatra.scalate.ScalateSupport

import com.example.app.model.repository.UserRepository
import com.example.app.model._

import scala.concurrent.ExecutionContext.Implicits.global

class MyScalatraServlet extends ScalatraServlet {

  get("/") {
    views.html.hello()
  }

  get("/new") {
    val userId = java.util.UUID.randomUUID.toString
    UserRepository.data += (userId -> Ready)
    s"""{
      "is_success":true,
      "user_id":"$userId"
    }"""
  }

}
