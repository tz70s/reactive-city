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

package reacty.model

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.util.Random

object ReactiveCityJsonProtocol extends DefaultJsonProtocol {
  implicit val vehicleFormat: RootJsonFormat[Vehicle] = jsonFormat4(Vehicle)
}

object TrafficFactory {
  private val shapes = Array("ambulance", "truck", "bmw", "benz", "toyota", "nissan", "volvo", "porsche")
  private val rand = new Random()

  def vehicle: Vehicle = {
    val vertical = rand.nextInt(3)
    val horizontal = rand.nextInt(3)
    val lane = (vertical, horizontal)
    val id = rand.nextInt(10000).toString
    val speed = rand.nextDouble() * 100
    val shape = shapes(rand.nextInt(shapes.length))
    Vehicle(shape, id, speed, lane)
  }
}

case class Vehicle(shape: String, id: String, speed: Double, lane: (Int, Int))
