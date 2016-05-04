package jqsoft.apps.vkflow.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import jqsoft.apps.vkflow.R;
import jqsoft.apps.vkflow.loaders.NewsfeedLoader;
import jqsoft.apps.vkflow.models.NewsPost;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private final Cursor cursor;

    public interface OnNewsPostClickListener {
        void onNewsPostClick(int sourceId, int newsPostId);
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
        @Bind(R.id.ibLikes) public ImageButton ibLikes;
        @Bind(R.id.tvLikesCount) public TextView tvLikesCount;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public View getContentView() {
            return itemView;
        }
    }

    public NewsAdapter(Cursor cursor, OnNewsPostClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
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

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        cursor.moveToPosition(position);

        final NewsPost post = new NewsPost();

        post.id = Integer.parseInt(cursor.getString(NewsfeedLoader.INDEX_POST_ID));
        post.source_id = Integer.parseInt(cursor.getString(NewsfeedLoader.INDEX_SOURCE_ID));
        post.date = cursor.getLong(NewsfeedLoader.INDEX_DATE);
        post.text = cursor.getString(NewsfeedLoader.INDEX_TEXT);
        post.comments_count = cursor.getString(NewsfeedLoader.INDEX_COMMENTS_COUNT);
        post.can_post_comment = (cursor.getInt(NewsfeedLoader.INDEX_CAN_POST_COMMENT) == 1);
        post.likes_count = cursor.getString(NewsfeedLoader.INDEX_LIKES_COUNT);
        post.user_likes = (cursor.getInt(NewsfeedLoader.INDEX_USER_LIKES) == 1);
        post.can_like = (cursor.getInt(NewsfeedLoader.INDEX_CAN_LIKE) == 1);
        post.userPhotoUrl = cursor.getString(NewsfeedLoader.INDEX_USER_PHOTO_URL);
        post.userName = cursor.getString(NewsfeedLoader.INDEX_USER_NAME);

        View.OnClickListener newsPostClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onNewsPostClick(post.source_id, post.id);
            }
        };

        viewHolder.getContentView().setOnClickListener(newsPostClickListener);
        viewHolder.llComments.setOnClickListener(newsPostClickListener);

        final Context context = viewHolder.getContentView().getContext();
        viewHolder.ibLikes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, context.getString(R.string.like_action, viewHolder.getAdapterPosition()),
                        Toast.LENGTH_SHORT).show();
            }
        });

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
