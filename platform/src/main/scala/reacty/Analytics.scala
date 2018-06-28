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
import reacty.model.Vehicle

object Analytics extends MetricsService

object GroupByFlow {
  def props: Props = Props(new GroupByFlow)
}

class GroupByFlow extends Actor with ActorLogging {

  override def receive: Receive = {
    case v: Array[Vehicle] =>
      log.info(s"receive msg : $v")
  }
}
