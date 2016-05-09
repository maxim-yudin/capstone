package jqsoft.apps.vkflow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdk.LoginState;
import com.vk.sdk.api.VKError;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jqsoft.apps.vkflow.fragments.CommentsFragment;
import jqsoft.apps.vkflow.fragments.MainFragment;
import jqsoft.apps.vkflow.models.NewsPostComment;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class MainActivity extends AppCompatActivity implements MainFragment.CallbackActions {
    private boolean mTwoPane;
    private static final int REQUEST_NEWS_PIECE = 1;

    /**
     * Scope is set of required permissions for your application
     *
     * @see <a href="https://vk.com/dev/permissions">vk.com api permissions documentation</a>
     */
    private static final String[] scope = new String[]{
            VKScope.WALL, VKScope.FRIENDS
    };
    private boolean isResumed = false;
    private boolean isSavedInstanceState = false;

    private InterstitialAd interstitialAd;

    private View containerComments = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        containerComments = findViewById(R.id.containerComments);
        if (containerComments != null) {
            mTwoPane = true;
        }

        GA.sendEvent(GA.createEvent(GA.CATEGORY_USAGE, mTwoPane ? GA.EVENT_NEWSFEED_WITH_COMMENTS_RUN : GA.EVENT_NEWSFEED_RUN, null));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isSavedInstanceState = (savedInstanceState != null);

        if (savedInstanceState == null) {
            VKSdk.wakeUpSession(this, new VKCallback<LoginState>() {
                @Override
                public void onResult(VKSdk.LoginState res) {
                    if (isResumed) {
                        switch (res) {
                            case LoggedOut:
                                showLoginForm();
                                break;
                            case LoggedIn:
                                showMainForm();
                                break;
                            case Pending:
                                break;
                            case Unknown:
                                break;
                        }
                    }
                }

                @Override
                public void onError(VKError error) {

                }
            });
        }

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.test_interstitial_ad_unit_id));
        requestNewInterstitial();

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        interstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isResumed = true;
        if (VKSdk.isLoggedIn()) {
            showMainForm();
        } else {
            showLoginForm();
        }
        isSavedInstanceState = false;

        if (!interstitialAd.isLoaded()) {
            requestNewInterstitial();
        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEWS_PIECE) {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        } else {
            VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
                @Override
                public void onResult(VKAccessToken res) {
                    // User passed Authorization
                }

                @Override
                public void onError(VKError error) {
                    // User didn't pass Authorization
                }
            };

            if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void showMainForm() {
        if (!isSavedInstanceState) {
            if (getSupportFragmentManager().findFragmentByTag("main_form") == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, MainFragment.newInstance(mTwoPane), "main_form")
                        .commitAllowingStateLoss();
            }
        }
    }

    private void showLoginForm() {
        if (!isSavedInstanceState) {
            if (getSupportFragmentManager().findFragmentByTag("login_form") == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, LoginFragment.newInstance(), "login_form")
                        .commitAllowingStateLoss();
            }
        }
    }

    public static class LoginFragment extends Fragment {
        public static LoginFragment newInstance() {
            return new LoginFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View fragmentView = inflater.inflate(R.layout.fragment_login, container, false);
            ButterKnife.bind(this, fragmentView);
            return fragmentView;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            ButterKnife.unbind(this);
        }

        @OnClick(R.id.btnSignIn)
        public void signIn() {
            VKSdk.login(getActivity(), scope);
        }
    }

    @Override
    public void onNewsPostSelected(int chosenNewsPostSourceId, int chosenNewsPostId) {
        if (mTwoPane) {
            containerComments.setVisibility(View.VISIBLE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.containerComments, CommentsFragment.newInstance(String.valueOf(chosenNewsPostSourceId),
                            String.valueOf(chosenNewsPostId)), "comments_form")
                    .commit();
        } else {
            Intent newsActivity = new Intent(this, CommentsActivity.class);
            newsActivity.putExtra(NewsPostComment.OWNER_ID, String.valueOf(chosenNewsPostSourceId));
            newsActivity.putExtra(NewsPostComment.POST_ID, String.valueOf(chosenNewsPostId));
            startActivityForResult(newsActivity, REQUEST_NEWS_PIECE);
        }
    }

    @Override
    public void onUpdateCommentsWhetherNewsfeedListEmpty(boolean isEmpty) {
        if (mTwoPane) {
            if (isEmpty) {
                CommentsFragment commentsFragment =
                        (CommentsFragment) getSupportFragmentManager().findFragmentById(R.id.containerComments);
                if (commentsFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(commentsFragment).commit();
                }
            }
            containerComments.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onSignOut() {
        onUpdateCommentsWhetherNewsfeedListEmpty(true);
        VKSdk.logout();
        if (!VKSdk.isLoggedIn()) {
            showLoginForm();
        }
    }
}
