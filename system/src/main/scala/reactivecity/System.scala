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

package reactivecity

import com.typesafe.config.{Config, ConfigFactory}
import org.rogach.scallop.ScallopConf
import reactivecity.controller.Controller
import reactivecity.partitioner.Partitioner

class SystemConf(args: Seq[String]) extends ScallopConf(args) {
  val role = opt[String](required = true)
  val location = opt[String](required = true)
  val port = opt[Int]()
  verify()
}

object System {

  private def clusterConfigString(role: String, location: String, port: Int) = {
    s"""
       |akka.cluster.roles = [$role, $location]
       |akka.remote.netty.tcp.port = $port
     """.stripMargin
  }

  private def matcher(conf: SystemConf): Unit = {
    val clusterConfig =
      ConfigFactory
        .parseString(clusterConfigString(conf.role(), conf.location(), conf.port.getOrElse(0)))
        .withFallback(ConfigFactory.load())
    conf.role() match {
      case "controller"  => launcher(Controller, conf.location(), conf.role(), clusterConfig)
      case "partitioner" => launcher(Partitioner, conf.location(), conf.role(), clusterConfig)
      case "analytics"   =>
      case "reflector"   =>
    }
  }

  private def launcher[S <: MetricsService](s: S, location: String, role: String, config: Config): Unit = {
    s.start(location, role, config)
  }

  def main(args: Array[String]): Unit = {
    val config = new SystemConf(args)
    matcher(config)
  }
}
