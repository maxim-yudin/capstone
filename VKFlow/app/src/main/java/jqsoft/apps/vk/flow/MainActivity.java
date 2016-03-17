package jqsoft.apps.vk.flow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdk.LoginState;
import com.vk.sdk.api.VKError;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Scope is set of required permissions for your application
     *
     * @see <a href="https://vk.com/dev/permissions">vk.com api permissions documentation</a>
     */
    private static final String[] scope = new String[]{
            VKScope.WALL, VKScope.FRIENDS
    };
    private boolean isResumed = false;

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
                            showLogoutForm();
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

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        if (VKSdk.isLoggedIn()) {
            showLogoutForm();
        } else {
            showLoginForm();
        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // User passed Authorization
                startMainActivity();
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

    private void showLogoutForm() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LogoutFragment())
                .commitAllowingStateLoss();
    }

    private void showLoginForm() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commitAllowingStateLoss();
    }

    public static class LoginFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View fragmentView = inflater.inflate(R.layout.fragment_sign_in, container, false);
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

    public static class LogoutFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View fragmentView = inflater.inflate(R.layout.fragment_sign_out, container, false);
            ButterKnife.bind(this, fragmentView);
            return fragmentView;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            ButterKnife.unbind(this);
        }

        @OnClick(R.id.btnRunMainForm)
        public void runMainForm() {
            ((MainActivity) getActivity()).startMainActivity();
        }

        @OnClick(R.id.btnSignOut)
        public void signOut() {
            VKSdk.logout();
            if (!VKSdk.isLoggedIn()) {
                ((MainActivity) getActivity()).showLoginForm();
            }
        }
    }

    private void startMainActivity() {
        Toast.makeText(this, R.string.run_main_form, Toast.LENGTH_SHORT).show();
    }
}
