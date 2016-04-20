package jqsoft.apps.vkflow.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.httpClient.VKJsonOperation;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import org.json.JSONObject;

import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import jqsoft.apps.vkflow.NewsAdapter;
import jqsoft.apps.vkflow.NewsAdapter.OnNewsItemClickListener;
import jqsoft.apps.vkflow.R;
import jqsoft.apps.vkflow.Utils;
import jqsoft.apps.vkflow.models.NewsFeed;

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

    private NewsFeed newsFeed;

    /**
     * A callback interface that allows main activity to be notified of news item
     * selection and signing out.
     */
    public interface CallbackActions {
        void onNewsItemSelected(VKApiPost chosenNewsItem);

        void onSignOut();
    }

    /**
     * A dummy implementation of the {@link CallbackActions} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final CallbackActions dummyCallbackActions = new CallbackActions() {
        @Override
        public void onNewsItemSelected(VKApiPost chosenNewsItem) {
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
            outState.putParcelable(NEWS_FEED, newsFeed);
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

    final OnNewsItemClickListener onNewsItemClickListener = new OnNewsItemClickListener() {
        @Override
        public void onNewsItemClick(VKApiPost newsItem) {
            callbackActions.onNewsItemSelected(newsItem);
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null || !savedInstanceState.containsKey(NEWS_FEED)) {
            getNews();
        } else {
            newsFeed = savedInstanceState.getParcelable(NEWS_FEED);
            fillAdapter();
        }
    }

    private void fillAdapter() {
        rvNews.setAdapter(new NewsAdapter(newsFeed, onNewsItemClickListener));
        pbLoading.setVisibility(View.GONE);
        rvNews.setVisibility(View.VISIBLE);
    }

    private void getNews() {
        new GetNewsListTask().execute();
    }

    private class GetNewsListTask extends AsyncTask<Void, Void, NewsFeed> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbLoading.setVisibility(View.VISIBLE);
            emptyView.setText(R.string.no_news);
            emptyView.setVisibility(View.GONE);
            rvNews.setVisibility(View.GONE);
        }

        protected NewsFeed doInBackground(Void... params) {
            try {
                if (!Utils.isInternetConnected(getContext())) {
                    return null;
                }

                VKJsonOperation getNewsFeed = new VKJsonOperation(new VKRequest("newsfeed.get", VKParameters.from(VKApiConst.FILTERS, "post")).getPreparedRequest());
                getNewsFeed.start(Executors.newSingleThreadExecutor());
                JSONObject newsFeedJson = getNewsFeed.getResponseJson().getJSONObject("response");

                NewsFeed newsFeed = new NewsFeed();
                newsFeed.items = new VKList<>(newsFeedJson.getJSONArray("items"), VKApiPost.class);
                newsFeed.profiles = new VKList<>(newsFeedJson.getJSONArray("profiles"), VKApiUser.class);
                newsFeed.groups = new VKList<>(newsFeedJson.getJSONArray("groups"), VKApiCommunity.class);
                return newsFeed;
            } catch (Exception e) {
                // if some errors occurs, e.g. no internet
                return null;
            }
        }

        protected void onPostExecute(NewsFeed result) {
            if (getActivity() == null) {
                return;
            }

            newsFeed = result;

            if (result == null) {
                pbLoading.setVisibility(View.GONE);
                rvNews.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(R.string.some_error);
                return;
            }
            fillAdapter();
        }
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
}