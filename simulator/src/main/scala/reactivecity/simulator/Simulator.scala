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

package reactivecity.simulator

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, RootActorPath, Timers}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.{MemberExited, MemberUp}
import reactivecity.model.Vehicle

import scala.concurrent.duration._

object PeriodicSender {
  case class BackPressureSender(duration: Duration)
  case object TickKey
  case object Tick

  def props(location: String): Props = Props(new PeriodicSender(location))
}

class PeriodicSender(val location: String) extends Actor with ActorLogging with Timers {

  import PeriodicSender._

  val cluster = Cluster(context.system)
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  private var knownPartitioners = Set[Member]()

  override def receive: Receive = {
    case MemberUp(member) =>
      if (member.hasRole("partitioner")) {
        // Wait until partitioner is up.
        // Register into local states.
        timers.startPeriodicTimer(TickKey, Tick, 1 seconds)
        knownPartitioners += member
      }
    case MemberExited(member) =>
      knownPartitioners -= member
      if (knownPartitioners.isEmpty) {
        // drop the timer.
        timers.cancel(TickKey)
      }
    case Tick =>
      log.debug("Send data to the selected partitioner ...")
      // TODO: remove this hard-coded way.
      // Let the exception thrown and restart this is nice.
      val partitioner = knownPartitioners.head
      val selected = RootActorPath(partitioner.address) / "user" / "partitioner-fog-west"

      context.actorSelection(selected) ! Vehicle("emergency", "test-vehicle", 2.5, "test-lane", List("test-lane"))
  }
}

object Simulator {

  def main(args: Array[String]): Unit = {
    val preferLocation = if (args.length > 0) args(0) else "unset"
    // Use the randomly assigned port for us.
    val system = ActorSystem("reactive-city-system")

    system.actorOf(PeriodicSender.props(preferLocation))
  }
}
