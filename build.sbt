enablePlugins(ScalaJSPlugin)

name := "react4s-todomvc"
organization := "com.github.ahnfelt"
version := "0.1-SNAPSHOT"

resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies += "com.github.ahnfelt" %%% "react4s" % "0.9.24-SNAPSHOT"
libraryDependencies += "com.github.werk" %%% "router4s" % "0.1.0-SNAPSHOT"
libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.4.4"

scalaVersion := "2.12.4"
