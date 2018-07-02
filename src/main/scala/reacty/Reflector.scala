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

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, ORMultiMapKey}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import reacty.model.{Emergency, Vehicle}

object Reflector extends MetricsService {
  def props(location: String): Props = Props(new Reflector(location))
}

class Reflector(val location: String) extends Actor with ActorLogging with Timers {

  implicit val cluster = Cluster(context.system)
  private var queue = List[Vehicle]()
  val replicator: ActorRef = DistributedData(context.system).replicator
  val DataKey = ORMultiMapKey[(Int, Int), Double](location)
  private val mediator = DistributedPubSub(context.system).mediator

  private def shortestPath(current: Int, dst: Int, graph: Array[Array[Double]], visited: Array[Boolean]): Double = {
    visited(current) = true
    if (current == dst) {
      0
    } else {
      val length = for (i <- graph(current).indices; if !visited(i) && (graph(current)(i) > 0)) yield {
        graph(current)(i) + shortestPath(i, dst, graph, visited)
      }
      length.min
    }
  }

  private def convertToArray(elements: Map[(Int, Int), Set[Double]]): Array[Array[Double]] = {
    val result = Array.ofDim[Double](3, 3)
    for ((lane, speed) <- elements) {
      result(lane._1)(lane._2) = speed.head
    }
    result
  }

  override def receive: Receive = {
    case v: Vehicle =>
      replicator ! Get(DataKey, ReadLocal)
      queue = (v :: queue.reverse).reverse

    case g @ GetSuccess(DataKey, req) =>
      val elements = g.get(DataKey)
      val graph = convertToArray(elements.entries)
      val v = queue.head
      queue = queue.tail
      val result = shortestPath(v.lane._1, 2, graph, Array.fill(3)(false))
      val latency = System.currentTimeMillis() - v.time
      // Data is only effective when latency < 1 sec
      if (latency < 1000) {
        mediator ! Publish(s"$location-reflector", Emergency(v, result))
      }
  }
}
