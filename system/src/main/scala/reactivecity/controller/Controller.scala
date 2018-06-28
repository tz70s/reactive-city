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

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.ClusterEvent._
import akka.cluster.Cluster
import reactivecity.MetricsService

/** Singleton system for managing reactive-city-systems */
object Controller extends MetricsService {

  override def init()(implicit system: ActorSystem): Unit = {
    system.actorOf(ControllerSeed.props)
    system.actorOf(Scheduler.props(), "scheduler")
  }
}

object ControllerSeed {
  case class QueryMemberByRole(role: String)
  def props: Props = Props(new ControllerSeed)
}

class ControllerSeed extends Actor with ActorLogging {
  import ControllerSeed._
  val cluster = Cluster(context.system)

  // Subscribe membership events.
  override def preStart(): Unit = {
    cluster.subscribe(
      self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember],
      classOf[ReachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberUp(member) =>
      log.debug(s"Member $member is up!")
    case UnreachableMember(member) =>
      log.debug(s"Current member $member is unreachable, remove from list.")
    case ReachableMember(member) =>
      log.debug(s"Resumed member $member is reachable, add back to list.")
    case MemberRemoved(member, previousStatus) =>
      log.debug(s"The member $member is removed after previous status $previousStatus")
    case QueryMemberByRole(role) =>
      // Get back a list of members.
      sender() ! cluster.state.members.filter(_.hasRole(role))
  }
}
