package com.example.app.model

sealed trait UserState

case object Ready extends UserState
case object Waiting extends UserState
case class Working(uuid: String) extends UserState
case object Destroyed extends UserState
