/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.core.util.customtabs;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Empty service used by the custom tab to bind to, raising the application's importance.
 *
 * Adapted from github.com/GoogleChrome/custom-tabs-client
 */
public class KeepAliveService extends Service {
    private static final Binder sBinder = new Binder();

    @Override
    public IBinder onBind(Intent intent) {
        return sBinder;
    }
}
