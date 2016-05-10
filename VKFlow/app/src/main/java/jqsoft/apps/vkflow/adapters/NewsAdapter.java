package jqsoft.apps.vkflow.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import jqsoft.apps.vkflow.GA;
import jqsoft.apps.vkflow.R;
import jqsoft.apps.vkflow.loaders.NewsfeedLoader;
import jqsoft.apps.vkflow.models.NewsPost;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    public static final int INVALID_POSITION = -1;

    private final Cursor cursor;
    private boolean needPerformClickOnPost = false;
    private int activatedPosition = INVALID_POSITION;
    private int newActivatedPosition;

    private @ColorInt int cardContentPressedColor;
    private @ColorInt int cardContentDefaultColor;

    private boolean isTwoPane = false;

    public interface OnNewsPostClickListener {
        void onNewsPostClick(int sourceId, int newsPostId);
    }

    public int getActivatedPosition() {
        return activatedPosition;
    }

    private final OnNewsPostClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tvFriendName) public TextView tvFriendName;
        @Bind(R.id.ivFriendPhoto) public ImageView ivFriendPhoto;
        @Bind(R.id.pbFriendPhotoLoading) public ProgressBar pbFriendPhotoLoading;
        @Bind(R.id.tvNewsDate) public TextView tvNewsDate;
        @Bind(R.id.tvNewsContent) public TextView tvNewsContent;
        @Bind(R.id.llComments) public View llComments;
        @Bind(R.id.tvCommentsCount) public TextView tvCommentsCount;
        @Bind(R.id.tvLikesCount) public TextView tvLikesCount;
        @Bind(R.id.llCardContent) public View llCardContent;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public View getContentView() {
            return itemView;
        }
    }

    public NewsAdapter(Cursor cursor, OnNewsPostClickListener listener, boolean isTwoPane, int activatedPosition) {
        this.cursor = cursor;
        this.listener = listener;
        this.isTwoPane = isTwoPane;
        this.newActivatedPosition = activatedPosition;
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(NewsfeedLoader.INDEX_POST_ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_news_item, viewGroup, false);

        cardContentPressedColor = ContextCompat.getColor(v.getContext(), R.color.card_pressed);
        cardContentDefaultColor = ContextCompat.getColor(v.getContext(), android.R.color.transparent);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        cursor.moveToPosition(position);

        final NewsPost post = new NewsPost();

        post.id = Integer.parseInt(cursor.getString(NewsfeedLoader.INDEX_POST_ID));
        post.source_id = Integer.parseInt(cursor.getString(NewsfeedLoader.INDEX_SOURCE_ID));
        post.date = cursor.getLong(NewsfeedLoader.INDEX_DATE);
        post.text = cursor.getString(NewsfeedLoader.INDEX_TEXT);
        post.comments_count = cursor.getString(NewsfeedLoader.INDEX_COMMENTS_COUNT);
        post.likes_count = cursor.getString(NewsfeedLoader.INDEX_LIKES_COUNT);
        post.userPhotoUrl = cursor.getString(NewsfeedLoader.INDEX_USER_PHOTO_URL);
        post.userName = cursor.getString(NewsfeedLoader.INDEX_USER_NAME);

        if (isTwoPane) {
            if (activatedPosition == INVALID_POSITION) {
                activatedPosition = (newActivatedPosition == INVALID_POSITION ? 0 : newActivatedPosition);
                needPerformClickOnPost = true;
            }

            if (activatedPosition == position) {
                viewHolder.llCardContent.setBackgroundColor(cardContentPressedColor);

                if (needPerformClickOnPost) {
                    needPerformClickOnPost = false;
                    GA.sendEvent(GA.createEvent(GA.CATEGORY_USAGE, GA.EVENT_POST_CLICK, String.valueOf(post.id)));
                    listener.onNewsPostClick(post.source_id, post.id);
                }
            } else {
                viewHolder.llCardContent.setBackgroundColor(cardContentDefaultColor);
            }
        }

        View.OnClickListener newsPostClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTwoPane) {
                    notifyItemChanged(activatedPosition);
                    activatedPosition = position;
                    notifyItemChanged(position);
                }
                GA.sendEvent(GA.createEvent(GA.CATEGORY_USAGE, GA.EVENT_POST_CLICK, String.valueOf(post.id)));
                listener.onNewsPostClick(post.source_id, post.id);
            }
        };

        viewHolder.getContentView().setOnClickListener(newsPostClickListener);
        viewHolder.llComments.setOnClickListener(newsPostClickListener);
        viewHolder.tvNewsContent.setOnClickListener(newsPostClickListener);

        final Context context = viewHolder.getContentView().getContext();

        viewHolder.tvFriendName.setText(post.userName);
        viewHolder.tvNewsContent.setText(post.text);
        viewHolder.tvNewsDate.setText(post.getPostDate());
        viewHolder.tvCommentsCount.setText(post.comments_count);
        viewHolder.tvLikesCount.setText(post.likes_count);

        Picasso.with(context).load(post.userPhotoUrl).into(viewHolder.ivFriendPhoto, new Callback() {
            @Override
            public void onSuccess() {
                viewHolder.pbFriendPhotoLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                viewHolder.pbFriendPhotoLoading.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
}
