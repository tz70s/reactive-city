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

import akka.cluster.Cluster
import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Timers}
import com.typesafe.config.Config
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.cluster.pubsub.DistributedPubSub

import scala.concurrent.duration._

object Metrics {

  case class Metric(location: String, role: String)
  case object TickMetricKey
  case object Tick
  private val period = 500 millis

  def props(location: String, role: String): Props = Props(new Metrics(location, role))
}

/**
 * Metrics actor is responsible for reporting metrics to controller.
 * It'll re-schedule the logic based on the reporting metrics.
 */
class Metrics(private val location: String, private val role: String) extends Actor with ActorLogging with Timers {
  import reactivecity.Metrics._
  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    timers.startPeriodicTimer(TickMetricKey, Tick, period)
  }

  def report: Receive = {
    case Tick =>
      mediator ! Publish("metrics", Metric(location, role))
  }

  override def receive: Receive = report
}

trait MetricsService {

  /** Additional init function. */
  def init()(implicit system: ActorSystem): Unit

  def start(location: String, role: String, config: Config): Unit = {
    implicit val system: ActorSystem = ActorSystem("reactive-city-system", config)
    // Create the metrics actor
    system.actorOf(Metrics.props(location, role))
    init()
  }
}
