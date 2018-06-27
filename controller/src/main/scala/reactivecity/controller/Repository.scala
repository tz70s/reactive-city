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

package reactivecity.controller

import akka.actor.{Actor, ActorLogging, ActorPath}
import akka.cluster.Member
import reactivecity.control.Repo

class Repository extends Actor with ActorLogging {
  import Repo._

  private var repo = Map[String, ActorPath]()

  override def receive: Receive = {
    case Registration(c) =>
      repo += (c -> sender().path)
    // TODO: should guarantee delivery here.
    case Retrieve(c) =>
      sender() ! repo(c)
  }
}
