package jqsoft.apps.vkflow;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import jqsoft.apps.vkflow.models.NewsPost;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    public interface OnNewsPostClickListener {
        void onNewsPostClick(NewsPost newsItem);
    }

    private final OnNewsPostClickListener listener;

    private final ArrayList<NewsPost> newsFeed;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tvFriendName) public TextView tvFriendName;
        @Bind(R.id.ivFriendPhoto) public ImageView ivPhoto;
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

    public NewsAdapter(ArrayList<NewsPost> newsFeed, OnNewsPostClickListener listener) {
        this.newsFeed = newsFeed;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_news_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final NewsPost post = newsFeed.get(position);

        View.OnClickListener newsPostClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onNewsPostClick(post);
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

        viewHolder.tvNewsContent.setText(post.text);
        viewHolder.tvNewsDate.setText(post.getPostDate());
        viewHolder.tvCommentsCount.setText(post.commentsCount);
        viewHolder.tvLikesCount.setText(post.likesCount);
    }

    @Override
    public int getItemCount() {
        return newsFeed.size();
    }
}
