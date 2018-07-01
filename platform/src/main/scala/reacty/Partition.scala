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

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Timers}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.routing._
import reacty.model.Vehicle

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

object Partition extends MetricsService {
  override def init()(implicit system: ActorSystem): Unit = {
    system.actorOf(FlowPartition.props("fog-west"))
  }
}

trait StreamService[T] {
  import scala.reflect.ClassTag

  val MAX_BATCH_SIZE = 32
  val BATCH_TIMEOUT = 250 millis

  protected lazy val buffer: ArrayBuffer[T] = ArrayBuffer[T]()

  /** batch elements into buffer */
  protected def batch(item: T): Int = {
    buffer += item
    buffer.length
  }

  /** drain out buffer and take an array out. */
  protected def drain()(implicit c: ClassTag[T]): Array[T] = {
    val array = buffer.toArray
    buffer.clear()
    array
  }
}

/**
 * Actor responsible for partition flow into two flows.
 * The flow partition actor also responsible as a router for flow migration from analytics.
 * Maybe we can make the routing rr aware and use dynamic dispatching of analytics actors.
 */
class FlowPartition(val location: String) extends Actor with ActorLogging with StreamService[Vehicle] with Timers {
  import FlowPartition._
  private val mediator = DistributedPubSub(context.system).mediator

  val subscribed = Subscribe(s"$location-partition", self)
  mediator ! subscribed

  val router = context.actorOf(
    ClusterRouterPool(
      RoundRobinPool(100),
      ClusterRouterPoolSettings(
        totalInstances = 100,
        maxInstancesPerNode = 30,
        allowLocalRoutees = false,
        useRoles = Set("analytics"))).props(GroupByFlow.props(location)),
    name = "router")

  val routeToReflector = context.actorOf(
    ClusterRouterPool(
      RoundRobinPool(10),
      ClusterRouterPoolSettings(
        totalInstances = 10,
        maxInstancesPerNode = 5,
        allowLocalRoutees = false,
        useRoles = Set("reflector"))).props(Reflector.props(location)),
    name = "route-reflector")

  override def receive: Receive = {
    case v: Vehicle =>
      if (v.shape == "ambulance") {
        routeToReflector ! v
      }
      val currentSize = batch(v)
      if (currentSize >= MAX_BATCH_SIZE) {
        val batch = drain()
        router ! batch
        log.debug(s"Drain out a new batch, size: ${batch.length}")
      }
      timers.startPeriodicTimer(BatchTimeoutKey, BatchTimeout, BATCH_TIMEOUT)

    case BatchTimeout =>
      // Batch timeout, drain out the queue and drop out the timer.
      val batch = drain()

      router ! batch
      timers.cancel(BatchTimeoutKey)
      log.debug(s"Drain out a new batch, size: ${batch.length}")

    case SubscribeAck(s) if s == subscribed =>
      log.debug(s"Subscribe to $s")

    case "scale" =>
    case _       =>
    // handle nothing.
  }
}

object FlowPartition {
  case object BatchTimeoutKey
  case object BatchTimeout
  def props(location: String): Props = Props(new FlowPartition(location))
}
