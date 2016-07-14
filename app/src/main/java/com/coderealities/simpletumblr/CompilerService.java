package com.coderealities.simpletumblr;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2016 coderealities.com
 */
public class CompilerService extends IntentService {
    public static final String ACTION_UPDATE_COMPLATION = CompilerService.class.getCanonicalName() + ".UPDATE_COMPLATION";
    public static final String EXTRA_RECEIVER = CompilerService.class.getCanonicalName() + ".extra.RECEIVER";
    public static final String ORIGINAL_SUFFIX = " Originals";
    public static final String REBLOG_SUFFIX = " Reblogs";
    public static final String BLOG_NAMES = "BLOG_NAMES";

    private static final String TAG = CompilerService.class.getSimpleName();
    private static final String LAST_ID_KEY = CompilerService.class.getCanonicalName() + ".LAST_ID_KEY";

    private JumblrClient mClient;
    private SharedPreferences mSharedPreferences;

    public CompilerService() {
        super("CompilerService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = getSharedPreferences(
                getString(R.string.category_file_key), Context.MODE_PRIVATE);

        mClient = new JumblrClient(getString(R.string.consumerKey), getString(R.string.consumerSecret));
        mClient.setToken(getString(R.string.oathToken), getString(R.string.oauthSecret));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver resultReceiver = null;

        if (intent.hasExtra(EXTRA_RECEIVER)) {
            // Extract the receiver passed into the service
            resultReceiver = intent.getParcelableExtra(EXTRA_RECEIVER);
        }
        if (ACTION_UPDATE_COMPLATION.equals(intent.getAction())) {
            updateComplation();
        }
    }

    private void updateComplation() {
        Long lastId = mSharedPreferences.getLong(LAST_ID_KEY, 0);
        ArrayList<Blog> blogsUserFollows = getBlogsUserFollows();
        HashMap<String, StringBuilder> categories = new HashMap<>(blogsUserFollows.size());
        for (Blog blog : blogsUserFollows) {
            String originals = blog.getName() + ORIGINAL_SUFFIX;
            categories.put(originals, new StringBuilder(mSharedPreferences.getString(originals, "")));
            String reblogs = blog.getName() + REBLOG_SUFFIX;
            categories.put(reblogs, new StringBuilder(mSharedPreferences.getString(reblogs, "")));
        }

        // give list, store updated list, and check
        try {
            Log.d(TAG, "Starting Update");
            Map<String, Object> params = new HashMap<String, Object>();
            if (lastId != 0) {
                params.put("since_id", lastId);
            } else {
                params.put("limit", 100);
            }
            params.put("reblog_info", true);
            List<Post> recentPosts = mClient.userDashboard(params);
            if (recentPosts.size() == 0) {
                return;
            }
            for (ListIterator<Post> iterator = recentPosts.listIterator(recentPosts.size()); iterator.hasPrevious(); ) {
                Post post = iterator.previous();
                if (post.getRebloggedFromName() == null) {
                    categories.get(post.getBlogName() + ORIGINAL_SUFFIX).append(post.getId() + ",");
                } else {
                    categories.get(post.getBlogName() + REBLOG_SUFFIX).append(post.getId() + ",");
                }
            }

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putLong(LAST_ID_KEY, recentPosts.get(0).getId());
            Set<String> blogNames = new HashSet<>(blogsUserFollows.size());
            for (Blog blog : blogsUserFollows) {
                String originals = blog.getName() + ORIGINAL_SUFFIX;
                editor.putString(originals, categories.get(originals).toString());
                String reblogs = blog.getName() + REBLOG_SUFFIX;
                editor.putString(reblogs, categories.get(reblogs).toString());
                blogNames.add(blog.getName());
            }
            editor.putStringSet(BLOG_NAMES, blogNames);
            editor.commit();
            Log.d(TAG, "Finished Update");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Blog> getBlogsUserFollows() {
        Map<String, Object> userFollowingParams = new HashMap<String, Object>();
        int offset = 0;
        List<Blog> blogs = mClient.userFollowing();
        ArrayList<Blog> finalList = new ArrayList<>();
        while (blogs.size() > 0) {
            finalList.addAll(blogs);
            offset += 20;
            userFollowingParams.put("offset", offset);
            blogs = mClient.userFollowing(userFollowingParams);
        }
        return finalList;
    }
}
