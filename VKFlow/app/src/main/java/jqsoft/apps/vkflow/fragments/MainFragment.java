package jqsoft.apps.vkflow.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import jqsoft.apps.vkflow.Constants;
import jqsoft.apps.vkflow.adapters.NewsAdapter;
import jqsoft.apps.vkflow.adapters.NewsAdapter.OnNewsPostClickListener;
import jqsoft.apps.vkflow.loaders.NewsfeedLoader;
import jqsoft.apps.vkflow.R;
import jqsoft.apps.vkflow.Utils;
import jqsoft.apps.vkflow.VkService;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment's current callback object, which is notified of news item
     * clicks and signing out.
     */
    private CallbackActions callbackActions = dummyCallbackActions;

    @Bind(R.id.rvNews) RecyclerView rvNews;
    @Bind(android.R.id.empty) TextView emptyView;
    @Bind(R.id.pbLoading) ProgressBar pbLoading;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new NewsfeedLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        rvNews.setAdapter(new NewsAdapter(cursor, onNewsPostClickListener));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvNews.setAdapter(null);
    }

    /**
     * A callback interface that allows main activity to be notified of news post
     * selection and signing out.
     */
    public interface CallbackActions {
        void onNewsPostSelected(int chosenNewsPostSourceId, int chosenNewsPostId);

        void onSignOut();
    }

    /**
     * A dummy implementation of the {@link CallbackActions} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final CallbackActions dummyCallbackActions = new CallbackActions() {
        @Override
        public void onNewsPostSelected(int chosenNewsPostSourceId, int chosenNewsPostId) {
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
        public void onNewsPostClick(int newsPostSourceId, int newsPostId) {
            callbackActions.onNewsPostSelected(newsPostSourceId, newsPostId);
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        IntentFilter newsfeedResultIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION_NEWSFEED);

        NewsFeedUpdateReceiver newsFeedUpdateReceiver = new NewsFeedUpdateReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(newsFeedUpdateReceiver, newsfeedResultIntentFilter);

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            getNews();
        }
    }

    private void getNews() {
        Intent mServiceIntent = new Intent(getActivity(), VkService.class);
        mServiceIntent.setAction(VkService.ACTION_FETCH_NEWSFEED);
        getActivity().startService(mServiceIntent);
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

    public class NewsFeedUpdateReceiver extends BroadcastReceiver {
        public NewsFeedUpdateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null) {
                return;
            }

            if (intent.getAction().equals(Constants.BROADCAST_ACTION_NEWSFEED)) {
                boolean isRefreshing = intent.getBooleanExtra(Constants.REFRESHING_NEWSFEED, false);
                if (isRefreshing) {
                    pbLoading.setVisibility(View.VISIBLE);
                    emptyView.setText(R.string.no_news);
                    emptyView.setVisibility(View.GONE);
                    rvNews.setVisibility(View.GONE);
                } else {
                    pbLoading.setVisibility(View.GONE);
                    if (rvNews.getAdapter() == null || rvNews.getAdapter().getItemCount() == 0) {
                        rvNews.setVisibility(View.GONE);
                        emptyView.setText(Utils.isInternetConnected(getContext()) ? R.string.no_news : R.string.some_error);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        rvNews.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
}