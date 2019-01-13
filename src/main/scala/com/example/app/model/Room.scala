package com.example.app.model

import org.scalatra._
import org.scalatra.atmosphere._

abstract class Room(val users: collection.mutable.Map[String, UserState])
  extends AtmosphereClient
