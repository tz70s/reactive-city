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

import java.time.{Duration, Instant}

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, ORMultiMapKey}
import reacty.model.Vehicle

object Reflector extends MetricsService {
  def props(location: String): Props = Props(new Reflector(location))
}

class Reflector(val location: String) extends Actor with ActorLogging with Timers {

  implicit val cluster = Cluster(context.system)
  private var queue = List[Vehicle]()
  val replicator: ActorRef = DistributedData(context.system).replicator
  val DataKey = ORMultiMapKey[(Int, Int), Double](location)

  override def receive: Receive = {
    case v: Vehicle =>
      replicator ! Get(DataKey, ReadLocal)
      queue = (v :: queue.reverse).reverse

    case g @ GetSuccess(DataKey, req) =>
      val elements = g.get(DataKey)
      log.info(s"$elements")
      val v = queue.head
      val latency = Instant.now().minusMillis(v.time).toEpochMilli
      queue = queue.tail
      log.info(s"Receive message $v, latency: ${latency}ms")
  }
}
