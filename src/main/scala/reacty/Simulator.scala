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

import akka.actor.{Actor, ActorLogging, ActorSystem, DiagnosticActorLogging, Props, Timers}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}
import reacty.model.{Emergency, TrafficFactory}
import akka.event.Logging.MDC

import scala.concurrent.duration._
import scala.util.Random

object PeriodicSender {
  case class BackPressureSender(duration: Duration)
  case object TickKey
  case object Tick

  case class LogDownLatency(msg: String, latency: Long)
  def props(location: String): Props = Props(new PeriodicSender(location))
}

class PeriodicSender(val location: String) extends Actor with DiagnosticActorLogging with Timers {

  import PeriodicSender._

  val cluster = Cluster(context.system)
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberEvent])
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def mdc(msg: Any): MDC = {
    msg match {
      case LogDownLatency(_, latency) =>
        Map("latency" -> s"latency ${latency}ms")
      case _ =>
        Map()
    }
  }

  self ! LogDownLatency("Start customized log down service", 0)

  private def reviseTimer(): Unit = {
    if (cluster.state.members.exists(_.hasRole("partition"))) {
      timers.startPeriodicTimer(TickKey, Tick, 100 millis)
    } else {
      log.debug(s"no available partition now, drop out timer.")
      timers.cancel(TickKey)
    }
  }

  private val mediator = DistributedPubSub(context.system).mediator
  val subscribed = Subscribe(s"$location-reflector", self)
  mediator ! subscribed

  override def receive: Receive = {
    case Tick =>
      log.debug("Send data to the selected partition router ...")
      val msg = TrafficFactory.vehicle
      mediator ! Publish(s"$location-partition", msg)
    case e: Emergency =>
      val latency = System.currentTimeMillis() - e.vehicle.time
      val speed = if (e.speed < 10 || e.speed > 140) 50 + Random.nextDouble() * 50 else e.speed
      self ! LogDownLatency(f"Re-route ${e.vehicle.shape}-${e.vehicle.id}, expected speed: $speed%.2f", latency)
    case _: MemberEvent =>
      // Checkout whether partition exists in cluster members and set/unset timer for publishing message.
      reviseTimer()
    case logdown: LogDownLatency =>
      log.info(s"${logdown.msg}")
  }
}

object Simulator extends MetricsService {
  override def init(location: String, role: String)(implicit system: ActorSystem): Unit = {
    system.actorOf(PeriodicSender.props(location))
  }
}
