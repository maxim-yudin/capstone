package jqsoft.apps.vkflow;

import android.app.IntentService;
import android.content.ContentProviderOperation;
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
import jqsoft.apps.vkflow.models.NewsPostComment;

public class VkService extends IntentService {
    public static final String ACTION_FETCH_NEWSFEED = "jqsoft.apps.vkflow.action.FETCH_NEWSFEED";
    public static final String ACTION_FETCH_COMMENTS = "jqsoft.apps.vkflow.action.FETCH_COMMENTS";

    public VkService() {
        super("VkService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_NEWSFEED.equals(action)) {
                handleActionFetchNewsfeed();
            } else if (ACTION_FETCH_COMMENTS.equals(action)) {
                handleActionFetchComments(intent.getStringExtra(NewsPostComment.OWNER_ID),
                        intent.getStringExtra(NewsPostComment.POST_ID));
            }
        }
    }

    private void handleActionFetchNewsfeed() {
        Intent newsfeedLoadingIntent = new Intent(Constants.BROADCAST_ACTION_NEWSFEED);
        Bundle bundleLoading = new Bundle();
        bundleLoading.putBoolean(Constants.REFRESHING_NEWSFEED, true);
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
                        newsPostValues.put(NewsPost.Contract.SOURCE_ID, post.source_id);
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
                    ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                    ops.add(ContentProviderOperation.newDelete(NewsPost.Contract.CONTENT_URI).build());
                    for (ContentValues newPost : newsfeedValues) {
                        ops.add(ContentProviderOperation.newInsert(NewsPost.Contract.CONTENT_URI).withValues(newPost).build());
                    }
                    contentResolver.applyBatch(NewsPost.Contract.CONTENT_URI.getAuthority(), ops);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Intent newsfeedUpdateIntent = new Intent(Constants.BROADCAST_ACTION_NEWSFEED);
        Bundle bundleUpdate = new Bundle();
        bundleUpdate.putBoolean(Constants.REFRESHING_NEWSFEED, false);
        newsfeedUpdateIntent.putExtras(bundleUpdate);
        LocalBroadcastManager.getInstance(this).sendBroadcast(newsfeedUpdateIntent);
    }

    private void handleActionFetchComments(String ownerId, String postId) {
        Intent commentsLoadingIntent = new Intent(Constants.BROADCAST_ACTION_COMMENTS);
        Bundle bundleLoading = new Bundle();
        bundleLoading.putBoolean(Constants.REFRESHING_COMMENTS, true);
        commentsLoadingIntent.putExtras(bundleLoading);
        LocalBroadcastManager.getInstance(this).sendBroadcast(commentsLoadingIntent);

        if (Utils.isInternetConnected(this)) {
            try {
                VKJsonOperation getComments = new VKJsonOperation(new VKRequest("wall.getComments",
                        VKParameters.from("owner_id", ownerId, "post_id", postId, "extended", "1")).getPreparedRequest());
                getComments.start(Executors.newSingleThreadExecutor());
                JSONObject commentsJson = getComments.getResponseJson().getJSONObject("response");

                final VKList<NewsPostComment> commentList = new VKList<>(commentsJson.getJSONArray("items"), NewsPostComment.class);
                final VKList<VKApiUser> profiles = new VKList<>(commentsJson.getJSONArray("profiles"), VKApiUser.class);
                final VKList<VKApiCommunity> groups = new VKList<>(commentsJson.getJSONArray("groups"), VKApiCommunity.class);

                VKApiUser user;
                VKApiCommunity group;

                final ContentResolver contentResolver = getContentResolver();
                ArrayList<ContentValues> commentsValues = new ArrayList<>();

                for (NewsPostComment postComment : commentList) {
                    if (!TextUtils.isEmpty(postComment.text)) {
                        StringBuilder sbName = new StringBuilder();
                        String photoUrl;
                        if (postComment.from_id >= 0) {
                            // it's an user
                            user = profiles.getById(postComment.from_id);
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
                            group = groups.getById(Math.abs(postComment.from_id));
                            sbName.append(group.name);
                            photoUrl = group.photo_100;
                        }
                        postComment.userName = sbName.toString();
                        postComment.userPhotoUrl = photoUrl;

                        ContentValues newsPostCommentValues = new ContentValues();
                        newsPostCommentValues.put(NewsPostComment.Contract.ID, postComment.getId());
                        newsPostCommentValues.put(NewsPostComment.Contract.OWNER_ID, ownerId);
                        newsPostCommentValues.put(NewsPostComment.Contract.POST_ID, postId);
                        newsPostCommentValues.put(NewsPostComment.Contract.DATE, postComment.date);
                        newsPostCommentValues.put(NewsPostComment.Contract.TEXT, postComment.text);
                        newsPostCommentValues.put(NewsPostComment.Contract.USER_PHOTO_URL, postComment.userPhotoUrl);
                        newsPostCommentValues.put(NewsPostComment.Contract.USER_NAME, postComment.userName);
                        commentsValues.add(newsPostCommentValues);
                    }
                }

                if (commentsValues.size() != 0) {
                    ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                    ops.add(ContentProviderOperation.newDelete(NewsPostComment.Contract.CONTENT_URI).withSelection(
                            NewsPostComment.Contract.OWNER_ID + " = ? AND " + NewsPostComment.Contract.POST_ID + " = ?",
                            new String[]{ownerId, postId}).build());
                    for (ContentValues newComment : commentsValues) {
                        ops.add(ContentProviderOperation.newInsert(NewsPostComment.Contract.CONTENT_URI).withValues(newComment).build());
                    }
                    contentResolver.applyBatch(NewsPostComment.Contract.CONTENT_URI.getAuthority(), ops);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Intent commentsUpdateIntent = new Intent(Constants.BROADCAST_ACTION_COMMENTS);
        Bundle bundleUpdate = new Bundle();
        bundleUpdate.putBoolean(Constants.REFRESHING_COMMENTS, false);
        commentsUpdateIntent.putExtras(bundleUpdate);
        LocalBroadcastManager.getInstance(this).sendBroadcast(commentsUpdateIntent);
    }
}
