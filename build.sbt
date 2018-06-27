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

name := "reactivecity"

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.12.6"
)

val akkaVersion = "2.5.13"

lazy val libraries = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
)

lazy val serde = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "io.spray" %%  "spray-json" % "1.3.4",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

lazy val common = (project in file("common"))
  .settings(
    commonSettings,
    libraryDependencies ++= serde
  )

lazy val controller = (project in file("controller"))
  .settings(
    commonSettings,
    libraryDependencies ++= libraries
  )
  .dependsOn(common)

lazy val partitioner = (project in file("partitioner"))
  .settings(
    commonSettings,
    libraryDependencies ++= libraries
  )
  .dependsOn(common)

lazy val analytics = (project in file("analytics"))
  .settings(
    commonSettings,
    libraryDependencies ++= libraries
  )

lazy val reflector = (project in file("reflector"))
  .settings(
    commonSettings,
    libraryDependencies ++= libraries
  )

lazy val simulator = (project in file("simulator"))
  .settings(
    commonSettings,
    libraryDependencies ++= libraries
  )
  .dependsOn(common)

lazy val root = project in file(".")
