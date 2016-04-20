package jqsoft.apps.vkflow;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.api.model.VKApiPost;

import butterknife.Bind;
import butterknife.ButterKnife;
import jqsoft.apps.vkflow.models.NewsFeed;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    public interface OnNewsItemClickListener {
        void onNewsItemClick(VKApiPost newsItem);
    }

    private final OnNewsItemClickListener listener;

    private final NewsFeed newsFeed;

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

    public NewsAdapter(NewsFeed newsFeed, OnNewsItemClickListener listener) {
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
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final VKApiPost post = newsFeed.items.get(position);

        View.OnClickListener newsItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onNewsItemClick(post);
            }
        };

        viewHolder.getContentView().setOnClickListener(newsItemClickListener);
        viewHolder.llComments.setOnClickListener(newsItemClickListener);

        final Context context = viewHolder.getContentView().getContext();
        viewHolder.ibLikes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, context.getString(R.string.like_action, position), Toast.LENGTH_SHORT).show();
            }
        });

        viewHolder.tvNewsContent.setVisibility(TextUtils.isEmpty(post.text) ? View.GONE : View.VISIBLE);
        viewHolder.tvNewsContent.setText(post.text);
        viewHolder.tvCommentsCount.setText(String.valueOf(post.comments_count));
        viewHolder.tvLikesCount.setText(String.valueOf(post.likes_count));
    }

    @Override
    public int getItemCount() {
        return newsFeed.items.size();
    }
}
