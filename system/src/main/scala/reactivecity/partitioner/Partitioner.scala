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

package reactivecity.partitioner

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, SubscribeAck}
import reactivecity.MetricsService
import reactivecity.model.Vehicle

object Partitioner extends MetricsService {
  override def init()(implicit system: ActorSystem): Unit = {
    system.actorOf(FlowPartition.props("fog-west"))
  }
}

/**
 * Actor responsible for partition flow into two flows.
 * The flow partition actor also responsible as a router for flow migration from analytics.
 * Maybe we can make the routing rr aware and use dynamic dispatching of analytics actors.
 */
class FlowPartition(val location: String) extends Actor with ActorLogging {
  private val mediator = DistributedPubSub(context.system).mediator

  val subscribed = Subscribe(s"$location-partitioner", self)
  mediator ! subscribed

  // TODO: We should first determine the possible routees.

  override def receive: Receive = {
    case v: Vehicle =>
      mediator ! Publish("analytics", v)
    case SubscribeAck(s) if s == subscribed =>
      log.debug(s"Subscribe to $s")
    case _ =>
    // handle nothing.
  }
}

object FlowPartition {
  def props(location: String): Props = Props(new FlowPartition(location))
}
