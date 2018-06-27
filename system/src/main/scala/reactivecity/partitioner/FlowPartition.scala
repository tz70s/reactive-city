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

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import reactivecity.model.Vehicle

/**
 * Actor responsible for partition flow into two flows.
 * The flow partition actor also responsible as a router for flow migration from analytics.
 * Maybe we can make the routing rr aware and use dynamic dispatching of analytics actors.
 */
class FlowPartition(val location: String) extends Actor with ActorLogging {
  log.info(s"Spawn a flow partitioner, path ${self.path}")
  private val mediator = DistributedPubSub(context.system).mediator
  val subscribed = Subscribe(s"$location-partitioner", self)
  mediator ! subscribed

  // TODO: Flow partition and router creation.
  override def receive: Receive = {
    case v: Vehicle =>
      log.info(s"Receiving vehicle data $v")
    case SubscribeAck(s) if s == subscribed =>
      log.info(s"Subscribe to $s")
  }
}

object FlowPartition {
  def props(location: String): Props = Props(new FlowPartition(location))
}
