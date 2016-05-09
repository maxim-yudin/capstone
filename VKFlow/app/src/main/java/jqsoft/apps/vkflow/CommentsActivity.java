package jqsoft.apps.vkflow;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import jqsoft.apps.vkflow.fragments.CommentsFragment;
import jqsoft.apps.vkflow.models.NewsPostComment;

public class CommentsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Bundle bundle = getIntent().getExtras();

        GA.sendEvent(GA.createEvent(GA.CATEGORY_USAGE, GA.EVENT_COMMENTS_RUN, bundle.getString(NewsPostComment.POST_ID)));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, CommentsFragment.newInstance(bundle.getString(NewsPostComment.OWNER_ID),
                            bundle.getString(NewsPostComment.POST_ID)), "comments_form")
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
