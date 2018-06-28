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

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import reacty.Metrics.Metric

object Scheduler {
  def props(): Props = Props(new Scheduler)
}

class Scheduler extends Actor with ActorLogging {

  val mediator = DistributedPubSub(context.system).mediator

  private var systemStates = Map[String, Metric]()

  override def preStart(): Unit = {
    mediator ! Subscribe("metrics", self)
  }

  override def receive: Receive = {
    case m: Metric =>
      val index = s"${m.role}-${m.location}"
      log.debug(s"Got metrics report from $index")
      systemStates += (index -> m)
    case SubscribeAck(Subscribe("metrics", None, `self`)) =>
      log.info(s"Successfully to subscribe the metric topic.")
  }
}
