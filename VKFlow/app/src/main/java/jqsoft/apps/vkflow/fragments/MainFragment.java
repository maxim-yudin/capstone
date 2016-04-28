package jqsoft.apps.vkflow.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import jqsoft.apps.vkflow.Constants;
import jqsoft.apps.vkflow.NewsAdapter;
import jqsoft.apps.vkflow.NewsAdapter.OnNewsPostClickListener;
import jqsoft.apps.vkflow.R;
import jqsoft.apps.vkflow.VkService;
import jqsoft.apps.vkflow.models.NewsPost;

public class MainFragment extends Fragment {
    /**
     * The fragment's current callback object, which is notified of news item
     * clicks and signing out.
     */
    private CallbackActions callbackActions = dummyCallbackActions;

    @Bind(R.id.rvNews) RecyclerView rvNews;
    @Bind(android.R.id.empty) TextView emptyView;
    @Bind(R.id.pbLoading) ProgressBar pbLoading;

    private static final String NEWS_FEED = "news_feed";

    private ArrayList<NewsPost> newsFeed;

    /**
     * A callback interface that allows main activity to be notified of news post
     * selection and signing out.
     */
    public interface CallbackActions {
        void onNewsPostSelected(NewsPost chosenNewsItem);

        void onSignOut();
    }

    /**
     * A dummy implementation of the {@link CallbackActions} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final CallbackActions dummyCallbackActions = new CallbackActions() {
        @Override
        public void onNewsPostSelected(NewsPost chosenNewsPost) {
        }

        @Override
        public void onSignOut() {
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof CallbackActions)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        callbackActions = (CallbackActions) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Reset the active callbacks interface to the dummy implementation.
        callbackActions = dummyCallbackActions;
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (newsFeed != null) {
            outState.putParcelableArrayList(NEWS_FEED, newsFeed);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, fragmentView);

        RecyclerView.LayoutManager newsLayoutManager = new LinearLayoutManager(getContext());
        rvNews.setLayoutManager(newsLayoutManager);

        return fragmentView;
    }

    final OnNewsPostClickListener onNewsPostClickListener = new OnNewsPostClickListener() {
        @Override
        public void onNewsPostClick(NewsPost newsItem) {
            callbackActions.onNewsPostSelected(newsItem);
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        IntentFilter newsfeedResultIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION_NEWSFEED_RESULT);

        NewsFeedReceiver newsFeedReceiver = new NewsFeedReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(newsFeedReceiver, newsfeedResultIntentFilter);

        if (savedInstanceState == null || !savedInstanceState.containsKey(NEWS_FEED)) {
            getNews();
        } else {
            newsFeed = savedInstanceState.getParcelableArrayList(NEWS_FEED);
            fillAdapter();
        }
    }

    private void fillAdapter() {
        rvNews.setAdapter(new NewsAdapter(newsFeed, onNewsPostClickListener));
        pbLoading.setVisibility(View.GONE);
        rvNews.setVisibility(View.VISIBLE);
    }

    private void getNews() {
        pbLoading.setVisibility(View.VISIBLE);
        emptyView.setText(R.string.no_news);
        emptyView.setVisibility(View.GONE);
        rvNews.setVisibility(View.GONE);
        Intent mServiceIntent = new Intent(getActivity(), VkService.class);
        mServiceIntent.setAction(VkService.ACTION_FETCH_NEWSFEED);
        getActivity().startService(mServiceIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnuSignOut) {
            callbackActions.onSignOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class NewsFeedReceiver extends BroadcastReceiver {
        public NewsFeedReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null) {
                return;
            }

            newsFeed = intent.getParcelableArrayListExtra(Constants.NEWSFEED_LIST);

            if (newsFeed == null) {
                pbLoading.setVisibility(View.GONE);
                rvNews.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(R.string.some_error);
                return;
            }
            fillAdapter();
        }
    }
}