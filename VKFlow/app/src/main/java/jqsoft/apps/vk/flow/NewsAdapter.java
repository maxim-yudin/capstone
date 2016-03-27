package jqsoft.apps.vk.flow;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    public interface OnNewsItemClickListener {
        void onNewsItemClick(String newsItem);
    }

    private final OnNewsItemClickListener listener;

    private final String[] newsSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tvNewsTitle) TextView tvNewsTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public TextView getNewsTitleView() {
            return tvNewsTitle;
        }

        public View getContentView() {
            return itemView;
        }

    }

    public NewsAdapter(String[] dataSet, OnNewsItemClickListener listener) {
        newsSet = dataSet;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_news_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getContentView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onNewsItemClick(newsSet[position]);
            }
        });
        viewHolder.getNewsTitleView().setText(newsSet[position]);
    }

    @Override
    public int getItemCount() {
        return newsSet.length;
    }
}
