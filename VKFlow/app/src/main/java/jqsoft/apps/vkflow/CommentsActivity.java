package jqsoft.apps.vkflow;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import jqsoft.apps.vkflow.fragments.CommentsFragment;
import jqsoft.apps.vkflow.models.NewsPostComment;

public class CommentsActivity extends AppCompatActivity {
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_piece);

        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        if (adView != null) {
            adView.loadAd(adRequest);
        }

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, CommentsFragment.newInstance(bundle.getString(NewsPostComment.OWNER_ID),
                            bundle.getString(NewsPostComment.POST_ID)), "comments_form")
                    .commit();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}
