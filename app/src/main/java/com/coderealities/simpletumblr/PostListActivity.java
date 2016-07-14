package com.coderealities.simpletumblr;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.exceptions.JumblrException;
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
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2016 coderealities.com
 */
public class PostListActivity extends Activity {
    public static final String EXTRA_CATEGORY = "android.intent.PostListActivity.extra.CATEGORY";
    private static final String TAG = PostListActivity.class.getName();
    private LinearLayout mPostListView;
    private String mCategory;
    private CompilerRequestReceiver receiver = new CompilerRequestReceiver(new Handler());
    private JumblrClient mClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClient = new JumblrClient(getString(R.string.consumerKey), getString(R.string.consumerSecret));
        mClient.setToken(getString(R.string.oathToken), getString(R.string.oauthSecret));

        setContentView(R.layout.activity_post_list);
        mPostListView = (LinearLayout) findViewById(R.id.post_list);

        Bundle bundle = getIntent().getExtras();
        mCategory = bundle.getString(EXTRA_CATEGORY);

        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.category_file_key), Context.MODE_PRIVATE);
        final String idList = sharedPreferences.getString(mCategory, "");
        final String blogName = mCategory.split(" ")[0];

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(mCategory, "");
        editor.commit();

        if (idList.equals("")) {
            // It was empty
            return;
        }
        TaskThread.run(new Runnable() {
            @Override
            public void run() {
                for (String postId : idList.split(",")) {
                    try {
                        final Post post = mClient.blogPost(blogName + ".tumblr.com", Long.valueOf(postId));
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mPostListView.addView(createPostView(post));
                            }
                        });
                    } catch (JumblrException exception) {
                        if (!exception.getMessage().equals("Not Found")) {
                            throw exception;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
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
}
