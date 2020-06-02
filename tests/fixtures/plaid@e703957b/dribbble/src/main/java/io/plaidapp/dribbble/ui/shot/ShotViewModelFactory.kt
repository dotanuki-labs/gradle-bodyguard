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

package io.plaidapp.dribbble.ui.shot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.dribbble.domain.CreateShotUiModelUseCase
import io.plaidapp.dribbble.domain.GetShareShotInfoUseCase
import javax.inject.Inject

/**
 * Factory for creating [ShotViewModel] with args.
 */
class ShotViewModelFactory @Inject constructor(
    private val shotId: Long,
    private val shotRepository: ShotsRepository,
    private val createShotUiModel: CreateShotUiModelUseCase,
    private val getShareShotInfoUseCase: GetShareShotInfoUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass != ShotViewModel::class.java) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        return ShotViewModel(
            shotId,
            shotRepository,
            createShotUiModel,
            getShareShotInfoUseCase,
            dispatcherProvider
        ) as T
    }
}
