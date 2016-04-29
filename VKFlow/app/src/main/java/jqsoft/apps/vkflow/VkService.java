package jqsoft.apps.vkflow;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.httpClient.VKJsonOperation;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import jqsoft.apps.vkflow.models.NewsPost;

public class VkService extends IntentService {
    public static final String ACTION_FETCH_NEWSFEED = "jqsoft.apps.vkflow.action.FETCH_NEWSFEED";

    public VkService() {
        super("VkService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_NEWSFEED.equals(action)) {
                handleActionFetchNewsfeed();
            }
        }
    }

    private void handleActionFetchNewsfeed() {
        ArrayList<NewsPost> newsFeed = null;
        if (Utils.isInternetConnected(this)) {
            try {
                VKJsonOperation getNewsFeed = new VKJsonOperation(new VKRequest("newsfeed.get", VKParameters.from(VKApiConst.FILTERS, "post")).getPreparedRequest());
                getNewsFeed.start(Executors.newSingleThreadExecutor());
                JSONObject newsFeedJson = getNewsFeed.getResponseJson().getJSONObject("response");

                VKList<NewsPost> postList = new VKList<>(newsFeedJson.getJSONArray("items"), NewsPost.class);
                VKList<VKApiUser> profiles = new VKList<>(newsFeedJson.getJSONArray("profiles"), VKApiUser.class);
                VKList<VKApiCommunity> groups = new VKList<>(newsFeedJson.getJSONArray("groups"), VKApiCommunity.class);

                VKApiUser user;
                VKApiCommunity group;

                newsFeed = new ArrayList<>();

                for (NewsPost post : postList) {
                    if (!TextUtils.isEmpty(post.text)) {
                        StringBuilder sbName = new StringBuilder();
                        String photoUrl;
                        if (post.source_id >= 0) {
                            // it's an user
                            user = profiles.getById(post.source_id);
                            if (user.first_name != null) {
                                sbName.append(user.first_name);
                                sbName.append(" ");
                            }
                            if (user.last_name != null) {
                                sbName.append(user.last_name);
                            }
                            photoUrl = user.photo_100;
                        } else {
                            // it's a community
                            group = groups.getById(Math.abs(post.source_id));
                            sbName.append(group.name);
                            photoUrl = group.photo_100;
                        }
                        post.userName = sbName.toString();
                        post.userPhotoUrl = photoUrl;

                        newsFeed.add(post);
                    }
                }
            } catch (Exception e) {
                newsFeed = null;
            }
            // Save newsfeed in our db, we will use them when we have no internet
            NewsfeedStorage.updateNewsfeed(getApplicationContext(), newsFeed);
        } else {
            // When we have no internet, we use offline newsfeed
            newsFeed = NewsfeedStorage.getNewsfeed(getApplicationContext());
        }

        Intent newsfeedResultIntent = new Intent(Constants.BROADCAST_ACTION_NEWSFEED_RESULT);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.NEWSFEED_LIST, newsFeed);
        newsfeedResultIntent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(newsfeedResultIntent);
    }
}
