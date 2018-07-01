/*
 * Copyright 2018 Tzu-Chiao Yeh.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

name := "reacty"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

lazy val commonSettings = Seq(
  version := "0.1.0",
  scalaVersion := "2.12.6"
)

val akkaVersion = "2.5.13"

lazy val libraries = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
  "org.rogach" %% "scallop" % "3.1.2",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "io.spray" %%  "spray-json" % "1.3.4",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "de.aktey.akka.visualmailbox" %% "collector" % "1.1.0"
)

lazy val app = (project in file("."))
  .settings(
    commonSettings,
    libraryDependencies ++= libraries
  )

dockerBaseImage := "openjdk:jre"
maintainer := "Tzu-Chiao Yeh <su3g4284zo6y7@gmail.com>"
packageSummary := "Reactive CQRS system for traffic congestion control and emergency services."
packageName := "reactive-city"
dockerExposedPorts := Seq(2552)
dockerUsername := Some("tz70s")