package jqsoft.apps.vkflow;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
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
        Intent newsfeedLoadingIntent = new Intent(Constants.BROADCAST_ACTION_NEWSFEED);
        Bundle bundleLoading = new Bundle();
        bundleLoading.putBoolean(Constants.REFRESHING, true);
        newsfeedLoadingIntent.putExtras(bundleLoading);
        LocalBroadcastManager.getInstance(this).sendBroadcast(newsfeedLoadingIntent);

        if (Utils.isInternetConnected(this)) {
            try {
                VKJsonOperation getNewsFeed = new VKJsonOperation(new VKRequest("newsfeed.get", VKParameters.from(VKApiConst.FILTERS, "post")).getPreparedRequest());
                getNewsFeed.start(Executors.newSingleThreadExecutor());
                JSONObject newsFeedJson = getNewsFeed.getResponseJson().getJSONObject("response");

                final VKList<NewsPost> postList = new VKList<>(newsFeedJson.getJSONArray("items"), NewsPost.class);
                final VKList<VKApiUser> profiles = new VKList<>(newsFeedJson.getJSONArray("profiles"), VKApiUser.class);
                final VKList<VKApiCommunity> groups = new VKList<>(newsFeedJson.getJSONArray("groups"), VKApiCommunity.class);

                VKApiUser user;
                VKApiCommunity group;

                final ContentResolver contentResolver = getContentResolver();
                ArrayList<ContentValues> newsfeedValues = new ArrayList<>();

                contentResolver.delete(NewsPost.Contract.CONTENT_URI, null, null);

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

                        ContentValues newsPostValues = new ContentValues();
                        newsPostValues.put(NewsPost.Contract.POST_ID, post.getId());
                        newsPostValues.put(NewsPost.Contract.DATE, post.date);
                        newsPostValues.put(NewsPost.Contract.TEXT, post.text);
                        newsPostValues.put(NewsPost.Contract.COMMENTS_COUNT, post.comments_count);
                        newsPostValues.put(NewsPost.Contract.CAN_POST_COMMENT, post.can_post_comment);
                        newsPostValues.put(NewsPost.Contract.LIKES_COUNT, post.likes_count);
                        newsPostValues.put(NewsPost.Contract.USER_LIKES, post.user_likes);
                        newsPostValues.put(NewsPost.Contract.CAN_LIKE, post.can_like);
                        newsPostValues.put(NewsPost.Contract.USER_PHOTO_URL, post.userPhotoUrl);
                        newsPostValues.put(NewsPost.Contract.USER_NAME, post.userName);
                        newsfeedValues.add(newsPostValues);
                    }
                }

                if (newsfeedValues.size() != 0) {
                    ContentValues[] newsPostListArray = new ContentValues[newsfeedValues.size()];
                    newsfeedValues.toArray(newsPostListArray);
                    contentResolver.bulkInsert(NewsPost.Contract.CONTENT_URI, newsPostListArray);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Intent newsfeedUpdateIntent = new Intent(Constants.BROADCAST_ACTION_NEWSFEED);
        Bundle bundleUpdate = new Bundle();
        bundleUpdate.putBoolean(Constants.REFRESHING, false);
        newsfeedUpdateIntent.putExtras(bundleUpdate);
        LocalBroadcastManager.getInstance(this).sendBroadcast(newsfeedUpdateIntent);
    }
}
