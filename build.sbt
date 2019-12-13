name := "alpakka-zip-stream"
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)
  .enablePlugins(PlayNettyServer).disablePlugins(PlayAkkaHttpServer) // netty needed because of bug in alpakka

scalaVersion := "2.13.1"

libraryDependencies += guice

libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-file" % "2.0.0-M1"

libraryDependencies += javaWs
