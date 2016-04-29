package jqsoft.apps.vkflow;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import jqsoft.apps.vkflow.models.NewsPost;
import jqsoft.apps.vkflow.models.NewsPost.Contract;

/**
 * Created by maximyudin on 29.04.16.
 */
public class NewsfeedStorage {
    private static final String[] NEWSPOST_PROJECTION = new String[]{
            NewsPost.Contract.POST_ID,
            NewsPost.Contract.DATE,
            NewsPost.Contract.TEXT,
            NewsPost.Contract.COMMENTS_COUNT,
            NewsPost.Contract.CAN_POST_COMMENT,
            NewsPost.Contract.LIKES_COUNT,
            NewsPost.Contract.USER_LIKES,
            NewsPost.Contract.CAN_LIKE,
            NewsPost.Contract.USER_PHOTO_URL,
            NewsPost.Contract.USER_NAME
    };

    // these indices must match the projection NEWSPOST_PROJECTION that don't use getColumnIndex() method
    private static final int INDEX_POST_ID = 0;
    private static final int INDEX_DATE = 1;
    private static final int INDEX_TEXT = 2;
    private static final int INDEX_COMMENTS_COUNT = 3;
    private static final int INDEX_CAN_POST_COMMENT = 4;
    private static final int INDEX_LIKES_COUNT = 5;
    private static final int INDEX_USER_LIKES = 6;
    private static final int INDEX_CAN_LIKE = 7;
    private static final int INDEX_USER_PHOTO_URL = 8;
    private static final int INDEX_USER_NAME = 9;

    public static void updateNewsfeed(Context context, ArrayList<NewsPost> newsFeed) {
        final ContentResolver contentResolver = context.getContentResolver();

        if (newsFeed != null && newsFeed.size() != 0) {
            ArrayList<ContentValues> newsfeedValues = new ArrayList<>();
            for (NewsPost newsPost : newsFeed) {
                contentResolver.delete(NewsPost.Contract.CONTENT_URI,
                        Contract.POST_ID + "= ?", new String[]{String.valueOf(newsPost.getId())});
                ContentValues newsPostValues = new ContentValues();
                newsPostValues.put(NewsPost.Contract.POST_ID, newsPost.getId());
                newsPostValues.put(NewsPost.Contract.DATE, newsPost.date);
                newsPostValues.put(NewsPost.Contract.TEXT, newsPost.text);
                newsPostValues.put(NewsPost.Contract.COMMENTS_COUNT, newsPost.comments_count);
                newsPostValues.put(NewsPost.Contract.CAN_POST_COMMENT, newsPost.can_post_comment);
                newsPostValues.put(NewsPost.Contract.LIKES_COUNT, newsPost.likes_count);
                newsPostValues.put(NewsPost.Contract.USER_LIKES, newsPost.user_likes);
                newsPostValues.put(NewsPost.Contract.CAN_LIKE, newsPost.can_like);
                newsPostValues.put(NewsPost.Contract.USER_PHOTO_URL, newsPost.userPhotoUrl);
                newsPostValues.put(NewsPost.Contract.USER_NAME, newsPost.userName);
                newsfeedValues.add(newsPostValues);
            }
            if (newsfeedValues.size() != 0) {
                ContentValues[] newsPostListArray = new ContentValues[newsfeedValues.size()];
                newsfeedValues.toArray(newsPostListArray);
                contentResolver.bulkInsert(NewsPost.Contract.CONTENT_URI, newsPostListArray);
            }
        }
    }

    public static ArrayList<NewsPost> getNewsfeed(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();

        ArrayList<NewsPost> newsFeed = new ArrayList<>();

        final Cursor cursor = contentResolver.query(NewsPost.Contract.CONTENT_URI, NEWSPOST_PROJECTION, null, null, "");

        if (cursor == null) {
            return newsFeed;
        } else if (cursor.getCount() < 1) {
            cursor.close();
            return newsFeed;
        }

        NewsPost newsPost;
        while (cursor.moveToNext()) {
            newsPost = new NewsPost();
            newsPost.id = Integer.parseInt(cursor.getString(INDEX_POST_ID));
            newsPost.date = cursor.getLong(INDEX_DATE);
            newsPost.text = cursor.getString(INDEX_TEXT);
            newsPost.comments_count = cursor.getString(INDEX_COMMENTS_COUNT);
            newsPost.can_post_comment = (cursor.getInt(INDEX_CAN_POST_COMMENT) == 1);
            newsPost.likes_count = cursor.getString(INDEX_LIKES_COUNT);
            newsPost.user_likes = (cursor.getInt(INDEX_USER_LIKES) == 1);
            newsPost.can_like = (cursor.getInt(INDEX_CAN_LIKE) == 1);
            newsPost.userPhotoUrl = cursor.getString(INDEX_USER_PHOTO_URL);
            newsPost.userName = cursor.getString(INDEX_USER_NAME);
            newsFeed.add(newsPost);
        }
        cursor.close();

        return newsFeed;
    }
}
