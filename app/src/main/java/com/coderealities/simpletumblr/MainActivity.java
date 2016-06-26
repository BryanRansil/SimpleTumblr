package com.coderealities.simpletumblr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Post;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/*
 * Copyright (c) 2016 coderealities.com
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    private static final long LOAD_REFRESH_TIME_MS = TimeUnit.MINUTES.toMillis(15);
    private JumblrClient mClient;
    private LinearLayout mPostListView;
    private long mLastLoadTimeMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPostListView = (LinearLayout) findViewById(R.id.post_list_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Create a new client
        // Authenticate via OAuth
        mClient = new JumblrClient(
                "TFGB1WudJLpsDxa2B1cgYH2nE98aVxkfto8deCQFlRhxUSNjhA",
                "nXu4bZo5Qwy1Fm0TlWbWkjIlQqG1S34UEDpboUpC9hjqzMUE8B"
        );
        mClient.setToken(
                "JSgSbWUnpQjQJv2RV7OR0kpfpYQjqHtrK5Y5Nfq9LPKC2s5Tv7",
                "248VjWRvM6u2uayQchWcFD0aOwJOOdWn1ShPjgkVrr3IHu5BEk"
        );

        if (mPostListView.getChildCount() > 0 && loadedRecently()) {
            return;
        } else if (mPostListView.getChildCount() > 0) {
            mPostListView.removeAllViews();
        }

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Loading Posts...");
        progress.show();

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("reblog_info", "true");

        final List<PostContent> compilation = TaskThread.getObject(new Callable<List<PostContent>>() {
            @Override
            public List<PostContent> call() throws Exception {
                List<PostContent> compile = new LinkedList<PostContent>();
                for (Post post : mClient.blogPosts("simplrpostexamples.tumblr.com", params)) {
                    compile.add(new PostContent(post, mClient));
                }
                return compile;
            }
        });

        mLastLoadTimeMs = System.currentTimeMillis();
        for (PostContent postContent : compilation) {
            progress.dismiss();
            mPostListView.addView(postContent.generateView(getBaseContext()));
        }
    }

    private boolean loadedRecently() {
        return mLastLoadTimeMs + LOAD_REFRESH_TIME_MS > System.currentTimeMillis();
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
