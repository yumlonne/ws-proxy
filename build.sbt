val ScalatraVersion = "2.6.4"

organization := "com.example"

name := "WS Proxy"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.6"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "container",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "org.json4s"                  %% "json4s-jackson"      % "3.6.3",
  "org.scalatra"                %% "scalatra-scalate"    % "2.6.4",
  "org.scalatra"                %% "scalatra-specs2"     % "2.6.4"  % "test",
  "org.scalatra"                %% "scalatra-atmosphere" % "2.6.4",
  "ch.qos.logback"              %  "logback-classic"     % "1.1.7"   % "runtime",
  "org.eclipse.jetty"           %  "jetty-plus"          % "9.2.17.v20160517"     % "container;provided",
  "org.eclipse.jetty"           %  "jetty-webapp"        % "9.2.17.v20160517"     % "container",
  "org.eclipse.jetty.websocket" %  "websocket-server"    % "9.2.17.v20160517"     % "container;provided"
)

enablePlugins(SbtTwirl)
enablePlugins(ScalatraPlugin)
