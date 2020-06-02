/*
 * Copyright 2018 Google LLC.
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

package io.plaidapp.dribbble.dagger

import io.plaidapp.dribbble.ui.shot.ShotActivity
import io.plaidapp.ui.coreComponent

/**
 * Inject dependencies into [ShotActivity]
 */
fun ShotActivity.inject(shotId: Long) {
    DaggerDribbbleComponent.builder()
        .coreComponent(coreComponent())
        .dribbbleModule(DribbbleModule(this, shotId))
        .build()
        .inject(this)
}
