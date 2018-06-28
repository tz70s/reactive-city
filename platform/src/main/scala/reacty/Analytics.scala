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

import akka.actor.{Actor, ActorLogging, Props, Timers}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import reacty.model.Vehicle

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

object Analytics extends MetricsService

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

object GroupByFlow {
  case object BatchTimeoutKey
  case object BatchTimeout
  def props: Props = Props(new GroupByFlow)
}

class GroupByFlow extends Actor with ActorLogging with StreamService[Vehicle] with Timers {
  import GroupByFlow._
  // The group by actor will retrieve via topic group.
  private val mediator = DistributedPubSub(context.system).mediator
  val subscribed = Subscribe("analytics", Some("unset-location"), self)
  override def preStart(): Unit = {
    mediator ! subscribed
  }

  override def receive: Receive = {
    case v: Vehicle =>
      val currentSize = batch(v)
      if (currentSize >= MAX_BATCH_SIZE) {
        val batch = drain()
        log.debug(s"Drain out a new batch, size: ${batch.length}")
      }
      timers.startPeriodicTimer(BatchTimeoutKey, BatchTimeout, BATCH_TIMEOUT)

    case BatchTimeout =>
      // Batch timeout, drain out the queue and drop out the timer.
      val batch = drain()
      log.debug(s"Drain out a new batch, size: ${batch.length}")
      timers.cancel(BatchTimeoutKey)

    case SubscribeAck(Subscribe("analytics", Some("unset-location"), `self`)) =>
      log.debug("Successfully subscribe to analytics topic")
  }
}
