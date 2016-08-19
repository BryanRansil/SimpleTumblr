package com.coderealities.simpletumblr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.ResultReceiver;
import android.util.Log;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.exceptions.JumblrException;
import com.tumblr.jumblr.types.AnswerPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;
import com.tumblr.jumblr.types.VideoPost;

import org.scribe.exceptions.OAuthConnectionException;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Copyright (c) 2016 coderealities.com
 */
public class CompilerService extends IntentService {
    public static final String ACTION_UPDATE_COMPLATION = CompilerService.class.getCanonicalName() + ".UPDATE_COMPLATION";
    public static final String EXTRA_RECEIVER = CompilerService.class.getCanonicalName() + ".extra.RECEIVER";
    public static final String POST_LIST = CompilerService.class.getCanonicalName() + ".POST_LIST";
    public static final String POST_LIST_DIVIDER = ";";
    public static final String POST_LIST_ENTRY_DIVIDER = ",";

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
                getString(R.string.complation_file_key), Context.MODE_PRIVATE);

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
        if (ACTION_UPDATE_COMPLATION.equals(intent.getAction()) && haveNetworkConnection()) {
            try {
                updateComplation();
            } catch (JumblrException e) {
                Log.e(TAG, "CompilerService had JumblrException");
                e.printStackTrace();
                throw e;
            } catch (OAuthConnectionException e) {
                Log.e(TAG, "CompilerService failed to connect, despite having checked that we have a network connection");
                e.printStackTrace();
                throw e;
            }
        }
    }

    private void updateComplation() {
        Long lastId = mSharedPreferences.getLong(LAST_ID_KEY, 0);
        HashMap<String, StringBuilder> categoryList = new HashMap<>(1);
        categoryList.put(POST_LIST, new StringBuilder(mSharedPreferences.getString(POST_LIST, "")));

        // give list, store updated list, and check
        try {
            Log.d(TAG, "Starting Update");
            Map<String, Object> params = new HashMap<String, Object>();
            if (lastId != 0) {
                params.put("since_id", lastId);
            } else {
                Log.d(TAG, "First time this service has run, collecting a number of posts");
                params.put("limit", 20);
            }
            params.put("reblog_info", true);
            List<Post> recentPosts = mClient.userDashboard(params);
            if (recentPosts.size() == 0) {
                Log.d(TAG, "No posts, finished");
                return;
            }
            for (ListIterator<Post> iterator = recentPosts.listIterator(recentPosts.size()); iterator.hasPrevious(); ) {
                Post post = iterator.previous();
                Log.d(TAG, "  (Update) adding a post");
                if (isImportantPost(post)) {
                    categoryList.get(POST_LIST).append(post.getId() + POST_LIST_ENTRY_DIVIDER + post.getBlogName() + POST_LIST_DIVIDER);
                }
                Log.d(TAG, "  (Update) added");
            }

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putLong(LAST_ID_KEY, recentPosts.get(0).getId());
            editor.putString(POST_LIST, categoryList.get(POST_LIST).toString());
            editor.commit();
            Log.d(TAG, "Finished Update");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private boolean isImportantPost(final Post post) {
        final String blogName = post.getBlogName();
        final boolean needsToBeOriginal = mSharedPreferences.getBoolean(blogName + StreamSettingsFragment.SETTING_PREFERENCE_DIVIDER + StreamSettingsFragment.SETTING_ORIGINAL, false);
        final boolean needsToBeVisual = mSharedPreferences.getBoolean(blogName + StreamSettingsFragment.SETTING_PREFERENCE_DIVIDER + StreamSettingsFragment.SETTING_VISUAL, false);
        if (needsToBeOriginal && (post.getRebloggedFromName() != null)) {
            return false;
        }
        if (needsToBeVisual && !visualPost(post)) {
            return false;
        }
        return true;
    }

    private boolean visualPost(Post post) {
        if (post instanceof VideoPost) {
            return true;
        } else if (post instanceof AnswerPost) {
            return ((AnswerPost) post).getAnswer().contains("<img");
        } else if (post instanceof TextPost) {
            return ((TextPost) post).getBody().contains("<img");
        }
        return false;
    }

    private boolean haveNetworkConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() == null) {
            return false;
        }
        return connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
