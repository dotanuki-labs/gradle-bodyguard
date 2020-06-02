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

package io.plaidapp.core.util

import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Test

class TextViewExtensionTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val view = TextView(context)

    @Test
    fun doBeforeTextChanged() {
        val called = AtomicBoolean()

        view.doBeforeTextChanged { _, _, _, _ ->
            called.set(true)
        }

        view.text = "text"
        assertTrue(called.get())
    }

    @Test
    fun doOnTextChanged() {
        val called = AtomicBoolean()

        view.doOnTextChanged { _, _, _, _ ->
            called.set(true)
        }

        view.text = "text"
        assertTrue(called.get())
    }

    @Test
    fun doAfterTextChanged() {
        val called = AtomicBoolean()

        view.doAfterTextChanged { _ ->
            called.set(true)
        }

        view.text = "text"
        assertTrue(called.get())
    }
}
