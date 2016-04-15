package jqsoft.apps.vkflow.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;

import butterknife.Bind;
import butterknife.ButterKnife;
import jqsoft.apps.vkflow.NewsAdapter;
import jqsoft.apps.vkflow.NewsAdapter.OnNewsItemClickListener;
import jqsoft.apps.vkflow.R;

public class MainFragment extends Fragment {
    /**
     * The fragment's current callback object, which is notified of news item
     * clicks and signing out.
     */
    private CallbackActions callbackActions = dummyCallbackActions;

    @Bind(R.id.rvNews) RecyclerView rvNews;

    private NewsAdapter newsAdapter;
    private RecyclerView.LayoutManager newsLayoutManager;
    private String[] newsList;

    /**
     * A callback interface that allows main activity to be notified of news item
     * selection and signing out.
     */
    public interface CallbackActions {
        void onNewsItemSelected(String chosenNewsItem);

        void onSignOut();
    }

    /**
     * A dummy implementation of the {@link CallbackActions} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final CallbackActions dummyCallbackActions = new CallbackActions() {
        @Override
        public void onNewsItemSelected(String chosenNewsItem) {
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

        initNewsList();
    }

    private void initNewsList() {
        newsList = new String[20];
        for (int i = 0; i < 20; i++) {
            newsList[i] = getString(R.string.sample_text_friend_name, i);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, fragmentView);

        newsLayoutManager = new LinearLayoutManager(getContext());
        rvNews.setLayoutManager(newsLayoutManager);

        newsAdapter = new NewsAdapter(newsList, new OnNewsItemClickListener() {
            @Override
            public void onNewsItemClick(String newsItem) {
                callbackActions.onNewsItemSelected(newsItem);
            }
        });
        rvNews.setAdapter(newsAdapter);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new VKRequest("newsfeed.get", VKParameters.from(VKApiConst.FILTERS, "post")).executeWithListener(new VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                Log.i("VKFlow", response.responseString);
            }

            @Override
            public void onError(VKError error) {

            }
        });
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