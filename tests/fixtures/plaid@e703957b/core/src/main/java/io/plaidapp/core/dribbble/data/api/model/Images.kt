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

package io.plaidapp.core.dribbble.data.api.model

import android.os.Parcelable
import io.plaidapp.core.dribbble.data.api.model.Images.ImageSize.NORMAL_IMAGE_SIZE
import io.plaidapp.core.dribbble.data.api.model.Images.ImageSize.TWO_X_IMAGE_SIZE
import kotlinx.android.parcel.Parcelize

/**
 * Models links to the various quality of images of a shot. One of [hidpi] or [normal] must be
 * non-null.
 */
data class Images(
    val hidpi: String? = null,
    val normal: String? = null,
    val teaser: String? = null
) {

    fun best(): String {
        return hidpi ?: normal!!
    }

    fun bestSize(): ImageSize {
        return if (hidpi != null) TWO_X_IMAGE_SIZE else NORMAL_IMAGE_SIZE
    }

    @Parcelize
    enum class ImageSize(val width: Int, val height: Int) : Parcelable {
        NORMAL_IMAGE_SIZE(400, 300),
        TWO_X_IMAGE_SIZE(800, 600);

        operator fun component1() = width
        operator fun component2() = height
    }
}
