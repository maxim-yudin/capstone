package jqsoft.apps.vkflow.providers;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.tjeannin.provigen.ProviGenOpenHelper;
import com.tjeannin.provigen.ProviGenProvider;

import jqsoft.apps.vkflow.models.NewsPost;
import jqsoft.apps.vkflow.models.NewsPostComment;

public class NewsDbProvider extends ProviGenProvider {
    private static final String DB_NAME = "NewsfeedDb";
    private static final int DB_VERSION = 1;

    private static final String AUTHORITY = "content://jqsoft.apps.vkflow";

    private static final Class[] contracts = new Class[]{
            NewsPost.Contract.class, NewsPostComment.Contract.class};

    @Override
    public SQLiteOpenHelper openHelper(Context context) {
        return new ProviGenOpenHelper(context, DB_NAME, null, DB_VERSION, contracts);
    }

    @Override
    public Class[] contractClasses() {
        return contracts;
    }

    public static Uri getContentUri(String path) {
        return Uri.parse(AUTHORITY).buildUpon().appendPath(path).build();
    }
}
