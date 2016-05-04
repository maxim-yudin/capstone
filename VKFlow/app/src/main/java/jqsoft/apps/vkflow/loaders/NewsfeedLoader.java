package jqsoft.apps.vkflow.loaders;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import jqsoft.apps.vkflow.models.NewsPost;
import jqsoft.apps.vkflow.models.NewsPost.Contract;

/**
 * Created by maximyudin on 29.04.16.
 */
public class NewsfeedLoader extends CursorLoader {
    private static final String[] NEWSPOST_PROJECTION = new String[]{
            Contract.POST_ID,
            Contract.SOURCE_ID,
            Contract.DATE,
            Contract.TEXT,
            Contract.COMMENTS_COUNT,
            Contract.CAN_POST_COMMENT,
            Contract.LIKES_COUNT,
            Contract.USER_LIKES,
            Contract.CAN_LIKE,
            Contract.USER_PHOTO_URL,
            Contract.USER_NAME
    };

    // these indices must match the projection NEWSPOST_PROJECTION that don't use getColumnIndex() method
    public static final int INDEX_POST_ID = 0;
    public static final int INDEX_SOURCE_ID = 1;
    public static final int INDEX_DATE = 2;
    public static final int INDEX_TEXT = 3;
    public static final int INDEX_COMMENTS_COUNT = 4;
    public static final int INDEX_CAN_POST_COMMENT = 5;
    public static final int INDEX_LIKES_COUNT = 6;
    public static final int INDEX_USER_LIKES = 7;
    public static final int INDEX_CAN_LIKE = 8;
    public static final int INDEX_USER_PHOTO_URL = 9;
    public static final int INDEX_USER_NAME = 10;

    public NewsfeedLoader(Context context) {
        super(context, NewsPost.Contract.CONTENT_URI, NEWSPOST_PROJECTION, null, null, "");
    }
}
