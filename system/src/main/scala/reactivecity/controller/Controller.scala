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

import akka.actor.ActorSystem
import reactivecity.MetricsService

/** Singleton system for managing reactive-city-systems */
object Controller extends MetricsService {

  override def init()(implicit system: ActorSystem): Unit = {
    system.actorOf(ControllerSeed.props)
    system.actorOf(MetricsManager.props(), "metrics-manager")
  }
}
