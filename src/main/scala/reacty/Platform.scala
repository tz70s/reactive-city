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

package reacty

import com.typesafe.config.{Config, ConfigFactory}
import org.rogach.scallop.ScallopConf

class PlatformConf(args: Seq[String]) extends ScallopConf(args) {
  val role = opt[String](required = true)
  val location = opt[String](required = true)
  val port = opt[Int]()
  verify()
}

object Platform {

  private def clusterConfigString(role: String, location: String, port: Int) = {
    s"""
       |akka.cluster.roles = [$role, $location]
     """.stripMargin
  }

  private def matcher(conf: PlatformConf): Unit = {
    val clusterConfig =
      ConfigFactory
        .parseString(clusterConfigString(conf.role(), conf.location(), conf.port.getOrElse(0)))
        .withFallback(ConfigFactory.load())
    val service: MetricsService = conf.role() match {
      case "controller" => Controller
      case "partition"  => Partition
      case "analytics"  => Analytics
      case "reflector"  => Reflector
      case "simulator"  => Simulator
    }
    launcher(service, conf.location(), conf.role(), clusterConfig)
  }

  private def launcher[S <: MetricsService](s: S, location: String, role: String, config: Config): Unit = {
    s.start(location, role, config)
  }

  def main(args: Array[String]): Unit = {
    val config = new PlatformConf(args)
    matcher(config)
  }
}
