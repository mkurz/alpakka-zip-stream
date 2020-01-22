name := "alpakka-zip-stream"
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)
  //.enablePlugins(PlayNettyServer).disablePlugins(PlayAkkaHttpServer) // netty needed because of bug in alpakka / UPDATE fixed in https://github.com/akka/alpakka/pull/2090

scalaVersion := "2.13.1"

libraryDependencies += guice

//libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-file" % "2.0.0-M1"
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-file" % "2.0.0-M2+24-f6eb9dca"

libraryDependencies += javaWs
