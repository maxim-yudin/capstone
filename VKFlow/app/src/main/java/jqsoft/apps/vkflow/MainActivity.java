package jqsoft.apps.vkflow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import com.vk.sdk.api.model.VKApiPost;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jqsoft.apps.vkflow.fragments.MainFragment;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class MainActivity extends AppCompatActivity implements MainFragment.CallbackActions {
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

    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitAllowingStateLoss();
    }

    private void showLoginForm() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, LoginFragment.newInstance())
                .commitAllowingStateLoss();
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
    public void onNewsItemSelected(VKApiPost chosenNewsItem) {
        Intent newsActivity = new Intent(this, NewsPieceActivity.class);
        startActivityForResult(newsActivity, REQUEST_NEWS_PIECE);
    }

    @Override
    public void onSignOut() {
        VKSdk.logout();
        if (!VKSdk.isLoggedIn()) {
            showLoginForm();
        }
    }
}
