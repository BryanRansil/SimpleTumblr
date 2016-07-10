package com.coderealities.simpletumblr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.AnswerPost;
import com.tumblr.jumblr.types.AudioPost;
import com.tumblr.jumblr.types.ChatPost;
import com.tumblr.jumblr.types.LinkPost;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.QuotePost;
import com.tumblr.jumblr.types.TextPost;
import com.tumblr.jumblr.types.UnknownTypePost;
import com.tumblr.jumblr.types.VideoPost;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
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

        TaskThread.run(new Runnable() {
            @Override
            public void run() {
                final Map<String, Object> params = new HashMap<String, Object>();

                final List<Post> posts = mClient.blogPosts("simplrpostexamples.tumblr.com", params);
                if (posts.size() > 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            for (Post post : posts) {
                                mPostListView.addView(createPostView(post));
                            }
                            progress.dismiss();
                        }
                    });
                }
            }
        });

    }

    private View createPostView(Post post) {
        if (post instanceof AnswerPost) {
            return new PostView(this, (AnswerPost)post);
        } else if (post instanceof AudioPost) {
            return new PostView(this, (AudioPost)post);
        } else if (post instanceof ChatPost) {
            return new PostView(this, (ChatPost)post);
        } else if (post instanceof LinkPost) {
            return new PostView(this, (LinkPost)post);
        } else if (post instanceof PhotoPost) {
            return new PostView(this, (PhotoPost)post);
        } else if (post instanceof QuotePost) {
            return new PostView(this, (QuotePost)post);
        } else if (post instanceof TextPost) {
            return new PostView(this, (TextPost)post);
        } else if (post instanceof VideoPost) {
            return new PostView(this, (VideoPost)post);
        }
        return new PostView(this, (UnknownTypePost)post);
    }

    protected void fillAvatar(final String blogName, String imageName, final ImageView blogAvatar) {
        TaskThread.run(new Runnable() {
            @Override
            public void run() {
                fillWithImage(mClient.blogInfo(blogName).avatar(), blogName, blogAvatar);
            }
        });
    }

    protected void fillWithImage(final String imageUrl, final String imageName, final ImageView imageView) {
        TaskThread.run(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                try {
                    connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                    connection.connect();
                    final InputStream input = connection.getInputStream();
                    final Drawable drawable = Drawable.createFromStream(input, imageName);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageDrawable(drawable);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
