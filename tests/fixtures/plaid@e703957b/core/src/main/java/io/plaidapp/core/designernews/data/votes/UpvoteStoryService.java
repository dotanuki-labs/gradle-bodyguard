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

package io.plaidapp.core.designernews.data.votes;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.plaidapp.core.dagger.designernews.Injector;
import io.plaidapp.core.designernews.data.api.DesignerNewsService;
import io.plaidapp.core.designernews.data.login.LoginRepository;
import io.plaidapp.core.designernews.data.stories.model.Story;
import retrofit2.Call;
import retrofit2.Response;

import javax.inject.Inject;

public class UpvoteStoryService extends IntentService {

    public static final String ACTION_UPVOTE = "ACTION_UPVOTE";
    public static final String EXTRA_STORY_ID = "EXTRA_STORY_ID";

    public UpvoteStoryService() {
        super("UpvoteStoryService");
    }

    @Inject DesignerNewsService service;
    @Inject LoginRepository repository;

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Injector.inject(this);
    }

    public static void startActionUpvote(@NonNull Context context, long storyId) {
        final Intent intent = new Intent(context, UpvoteStoryService.class);
        intent.setAction(ACTION_UPVOTE);
        intent.putExtra(EXTRA_STORY_ID, storyId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPVOTE.equals(action)) {
                handleActionUpvote(intent.getLongExtra(EXTRA_STORY_ID, 0L));
            }
        }
    }

    private void handleActionUpvote(long storyId) {
        if (storyId == 0L) return;

        if (!repository.isLoggedIn()) {
            // TODO prompt for login
            return;
        }

        final Call<Story> upvoteStoryCall = service.upvoteStory(storyId);
        try {
            final Response<Story> response = upvoteStoryCall.execute();
            // TODO report success
        } catch (Exception e) {
            // TODO report failure
        }
    }
}
