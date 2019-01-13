package com.example.app.model

import org.scalatra.atmosphere._

case class Room(id: String, users: Seq[String], atmoClient: AtmosphereClient)
