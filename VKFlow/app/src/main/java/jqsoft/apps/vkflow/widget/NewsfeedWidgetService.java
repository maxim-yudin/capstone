package jqsoft.apps.vkflow.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import jqsoft.apps.vkflow.R;
import jqsoft.apps.vkflow.fragments.MainFragment;
import jqsoft.apps.vkflow.loaders.NewsfeedLoader;
import jqsoft.apps.vkflow.models.NewsPost;

@TargetApi(VERSION_CODES.JELLY_BEAN)
public class NewsfeedWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NewsfeedRemoteViewsFactory(getApplicationContext());
    }

    private static class NewsfeedRemoteViewsFactory implements RemoteViewsFactory {
        private Cursor cursor = null;

        private final Context context;

        public NewsfeedRemoteViewsFactory(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate() {
            // Nothing
        }

        @Override
        public void onDestroy() {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        @Override
        public void onDataSetChanged() {
            if (cursor != null) {
                cursor.close();
            }

            // This method is called by the app hosting the widget (e.g., the launcher)
            // However, our ContentProvider is not exported so it doesn't have access to the
            // data. Therefore we need to clear (and finally restore) the calling identity so
            // that calls use our process and permission
            final long identityToken = Binder.clearCallingIdentity();
            cursor = context.getContentResolver().query(NewsPost.Contract.CONTENT_URI, NewsfeedLoader.NEWSPOST_PROJECTION, null, null, null);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public int getCount() {
            return cursor == null ? 0 : cursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION ||
                    cursor == null || !cursor.moveToPosition(position)) {
                return null;
            }

            NewsPost post = new NewsPost();

            post.id = Integer.parseInt(cursor.getString(NewsfeedLoader.INDEX_POST_ID));
            post.source_id = Integer.parseInt(cursor.getString(NewsfeedLoader.INDEX_SOURCE_ID));
            post.date = cursor.getLong(NewsfeedLoader.INDEX_DATE);
            post.text = cursor.getString(NewsfeedLoader.INDEX_TEXT);
            post.comments_count = cursor.getString(NewsfeedLoader.INDEX_COMMENTS_COUNT);
            post.likes_count = cursor.getString(NewsfeedLoader.INDEX_LIKES_COUNT);
            post.userPhotoUrl = cursor.getString(NewsfeedLoader.INDEX_USER_PHOTO_URL);
            post.userName = cursor.getString(NewsfeedLoader.INDEX_USER_NAME);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_row_news_item);

            views.setTextViewText(R.id.tvFriendName, post.userName);
            views.setContentDescription(R.id.tvFriendName, post.userName);

            views.setTextViewText(R.id.tvNewsContent, post.text);
            views.setContentDescription(R.id.tvNewsContent, post.text);

            views.setTextViewText(R.id.tvNewsDate, post.getPostDate());
            views.setContentDescription(R.id.tvNewsDate, post.getPostDate());

            views.setTextViewText(R.id.tvCommentsCount, post.comments_count);
            views.setTextViewText(R.id.tvLikesCount, post.likes_count);

            Bitmap friendPhoto = null;
            try {
                // if we have some cache image, we try to load friend photo
                friendPhoto = Picasso.with(context).load(post.userPhotoUrl).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (friendPhoto != null) {
                views.setImageViewBitmap(R.id.ivFriendPhoto, friendPhoto);
                views.setContentDescription(R.id.ivFriendPhoto, context.getString(R.string.content_friend_photo));
                views.setViewVisibility(R.id.ivFriendPhoto, View.VISIBLE);
            } else {
                views.setViewVisibility(R.id.ivFriendPhoto, View.GONE);
            }

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(MainFragment.STATE_ACTIVATED_POSITION, position);
            views.setOnClickFillInIntent(R.id.llContent, fillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(context.getPackageName(), R.layout.appwidget_row_news_item);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (cursor.moveToPosition(position))
                return cursor.getInt(NewsfeedLoader.INDEX_POST_ID);
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}