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

package reactivecity.controller

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

/** Singleton system for managing reactive-city-systems */
object Controller {

  def main(args: Array[String]): Unit = {
    val config = if (args.length > 0) {
      ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(0)}").withFallback(ConfigFactory.load())
    } else {
      throw new IllegalArgumentException("error occurred on parsing config.")
    }

    implicit val system = ActorSystem("reactive-city-system", config)
    system.log.info("Create an reactive-city-controller-system")
    // Spawn a seed listener actors.
    val seed = system.actorOf(ControllerSeed.props)
  }
}
