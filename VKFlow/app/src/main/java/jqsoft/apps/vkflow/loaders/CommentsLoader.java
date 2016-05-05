package jqsoft.apps.vkflow.loaders;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import jqsoft.apps.vkflow.models.NewsPostComment;
import jqsoft.apps.vkflow.models.NewsPostComment.Contract;

/**
 * Created by maximyudin on 29.04.16.
 */
public class CommentsLoader extends CursorLoader {
    private static final String[] COMMENT_PROJECTION = new String[]{
            Contract.ID,
            Contract.OWNER_ID,
            Contract.POST_ID,
            Contract.DATE,
            Contract.TEXT,
            Contract.USER_PHOTO_URL,
            Contract.USER_NAME
    };

    // these indices must match the projection COMMENT_PROJECTION that don't use getColumnIndex() method
    public static final int INDEX_ID = 0;
    public static final int INDEX_OWNER_ID = 1;
    public static final int INDEX_POST_ID = 2;
    public static final int INDEX_DATE = 3;
    public static final int INDEX_TEXT = 4;
    public static final int INDEX_USER_PHOTO_URL = 5;
    public static final int INDEX_USER_NAME = 6;

    public CommentsLoader(Context context, String ownerId, String postId) {
        super(context, NewsPostComment.Contract.CONTENT_URI, COMMENT_PROJECTION,
                Contract.OWNER_ID + " = ? AND " + Contract.POST_ID + " = ?",
                new String[]{ownerId, postId},
                "");
    }
}
