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

package reactivecity.model

import org.scalatest._

class SerdeSpec extends FlatSpec with Matchers {

  behavior of "vehicle-serde"

  it should "be a valid serde" in {
    val vehicle =
      Vehicle("emergency", "test-vehicle-id", 2.5, "test-lane-id-0", List("test-lane-id-0", "test-lane-id-1"))

    val rawVehicle =
      """
        |{
        |  "shape": "emergency",
        |  "id": "test-vehicle-id",
        |  "speed": 2.5,
        |  "lane": "test-lane-id-0",
        |  "route": ["test-lane-id-0", "test-lane-id-1"]
        |}
      """.stripMargin

    import ReactiveCityJsonProtocol._
    import spray.json._
    rawVehicle.parseJson.convertTo should be(vehicle)
  }
}
