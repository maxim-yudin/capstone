package jqsoft.apps.vkflow.adapters;

import android.content.Context;
import android.database.Cursor;
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
import jqsoft.apps.vkflow.R;
import jqsoft.apps.vkflow.loaders.CommentsLoader;
import jqsoft.apps.vkflow.models.NewsPostComment;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    private final Cursor cursor;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tvFriendName) public TextView tvFriendName;
        @Bind(R.id.ivFriendPhoto) public ImageView ivFriendPhoto;
        @Bind(R.id.pbFriendPhotoLoading) public ProgressBar pbFriendPhotoLoading;
        @Bind(R.id.tvCommentDate) public TextView tvCommentDate;
        @Bind(R.id.tvCommentContent) public TextView tvCommentContent;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public View getContentView() {
            return itemView;
        }
    }

    public CommentsAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(CommentsLoader.INDEX_ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_comment, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        cursor.moveToPosition(position);

        final NewsPostComment comment = new NewsPostComment();

        comment.id = Integer.parseInt(cursor.getString(CommentsLoader.INDEX_ID));
        comment.from_id = Integer.parseInt(cursor.getString(CommentsLoader.INDEX_OWNER_ID));
        comment.post_id = Integer.parseInt(cursor.getString(CommentsLoader.INDEX_POST_ID));
        comment.date = cursor.getLong(CommentsLoader.INDEX_DATE);
        comment.text = cursor.getString(CommentsLoader.INDEX_TEXT);
        comment.userPhotoUrl = cursor.getString(CommentsLoader.INDEX_USER_PHOTO_URL);
        comment.userName = cursor.getString(CommentsLoader.INDEX_USER_NAME);

        final Context context = viewHolder.getContentView().getContext();

        viewHolder.tvFriendName.setText(comment.userName);
        viewHolder.tvCommentContent.setText(comment.text);
        viewHolder.tvCommentDate.setText(comment.getCommentDate());

        Picasso.with(context).load(comment.userPhotoUrl).into(viewHolder.ivFriendPhoto, new Callback() {
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
