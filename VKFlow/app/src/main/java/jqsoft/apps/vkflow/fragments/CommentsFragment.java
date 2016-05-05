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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import jqsoft.apps.vkflow.Constants;
import jqsoft.apps.vkflow.R;
import jqsoft.apps.vkflow.Utils;
import jqsoft.apps.vkflow.VkService;
import jqsoft.apps.vkflow.adapters.CommentsAdapter;
import jqsoft.apps.vkflow.loaders.CommentsLoader;
import jqsoft.apps.vkflow.models.NewsPostComment;

public class CommentsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    @Bind(R.id.rvComments) RecyclerView rvComments;
    @Bind(android.R.id.empty) TextView emptyView;
    @Bind(R.id.pbLoading) ProgressBar pbLoading;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CommentsLoader(getActivity(),
                getArguments().getString(NewsPostComment.OWNER_ID),
                getArguments().getString(NewsPostComment.POST_ID));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        rvComments.setAdapter(new CommentsAdapter(cursor));
        if (rvComments.getAdapter() != null && rvComments.getAdapter().getItemCount() == 0) {
            emptyView.setText(Utils.isInternetConnected(getContext()) ? R.string.no_comments : R.string.some_error);
        } else {
            emptyView.setText("");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvComments.setAdapter(null);
    }

    public static CommentsFragment newInstance(String ownerId, String postId) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(NewsPostComment.OWNER_ID, ownerId);
        args.putString(NewsPostComment.POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_comments, container, false);
        ButterKnife.bind(this, fragmentView);

        RecyclerView.LayoutManager commentsLayoutManager = new LinearLayoutManager(getContext());
        rvComments.setLayoutManager(commentsLayoutManager);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        IntentFilter commentsResultIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION_COMMENTS);

        CommentsUpdateReceiver commentsUpdateReceiver = new CommentsUpdateReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(commentsUpdateReceiver, commentsResultIntentFilter);

        getLoaderManager().initLoader(1, null, this);

        if (savedInstanceState == null) {
            getComments();
        }
    }

    private void getComments() {
        Intent mServiceIntent = new Intent(getActivity(), VkService.class);
        mServiceIntent.setAction(VkService.ACTION_FETCH_COMMENTS);
        mServiceIntent.putExtra(NewsPostComment.OWNER_ID, getArguments().getString(NewsPostComment.OWNER_ID));
        mServiceIntent.putExtra(NewsPostComment.POST_ID, getArguments().getString(NewsPostComment.POST_ID));
        getActivity().startService(mServiceIntent);
    }

    public class CommentsUpdateReceiver extends BroadcastReceiver {
        public CommentsUpdateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null) {
                return;
            }

            if (intent.getAction().equals(Constants.BROADCAST_ACTION_COMMENTS)) {
                boolean isRefreshing = intent.getBooleanExtra(Constants.REFRESHING_COMMENTS, false);
                if (isRefreshing) {
                    pbLoading.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    rvComments.setVisibility(View.GONE);
                } else {
                    pbLoading.setVisibility(View.GONE);
                    rvComments.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}