package com.coderealities.simpletumblr;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copyright (c) 2016 coderealities.com
 */
public class PostListFragment extends ListFragment {
    private static final String TAG = PostListFragment.class.getName();
    private static final String STATE_CURRENT_POST_INFO = "currentPostInfo";

    private JumblrClient mClient;
    private PostListFragmentHost mHost;
    private PostArrayAdapter mPostListAdapter;
    private AtomicBoolean mLoadingPost = new AtomicBoolean(false);
    private String mPostInfoString;
    private TextView mMessageView;

    public interface PostListFragmentHost {
        JumblrClient getJumblrClient();
    }

    private class PostArrayAdapter<T> extends ArrayAdapter<Post> {
        public PostArrayAdapter(Context context, List<Post> sourceList) {
            super(context, 0, sourceList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createPostView(getItem(position));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mHost = (PostListFragmentHost) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement PostListFragmentHost");
        }
        mClient = mHost.getJumblrClient();

        ArrayList<Post> postList = new ArrayList<>();
        mPostListAdapter = new PostArrayAdapter<Post>(getActivity().getApplicationContext(), postList);
        setListAdapter(mPostListAdapter);

        // get the last one
        if (savedInstanceState != null) {
            mPostInfoString = savedInstanceState.getString(STATE_CURRENT_POST_INFO);
        }

        if (mPostInfoString != null) {
            final String postInfoString = mPostInfoString;
            getPost(postInfoString, null);
        } else {
            nextPost();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_CURRENT_POST_INFO, mPostInfoString);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_list_fragment, container, false);
        return view;
    }

    public void nextPost() {
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.complation_file_key), Context.MODE_PRIVATE);

        final String postInfoListString = sharedPreferences.getString(CompilerService.POST_LIST, "");
        if (postInfoListString.equals("")) {
            Log.d(TAG, "PostInfoList... was empty");
            Toast.makeText(getActivity(), "No more entries", Toast.LENGTH_LONG).show();
            return;
        }

        final String[] postInfoList = postInfoListString.split(CompilerService.POST_LIST_DIVIDER);
        final String postInfo = postInfoList[0];
        getPost(postInfo, postInfoListString.substring(postInfo.length() + CompilerService.POST_LIST_DIVIDER.length()));
        return;
    }

    private void getPost(@NonNull final String postInfoString, @Nullable final String updatedStorage) {
        if (mLoadingPost.getAndSet(true)) {
            Log.d(TAG, "Already loading a post");
            return;
        }
        mPostListAdapter.clear();
        TaskThread.run(new Runnable() {
            @Override
            public void run() {
                String[] postInfo = postInfoString.split(CompilerService.POST_LIST_ENTRY_DIVIDER);
                try {
                    final Post post = mClient.blogPost(postInfo[1] + ".tumblr.com", Long.valueOf(postInfo[0]));
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "  have the post.");
                            mPostListAdapter.add(post);
                            if (getListView().getVisibility() != View.VISIBLE) {
                                mMessageView.setVisibility(View.GONE);
                                getListView().setVisibility(View.VISIBLE);
                            }
                            cleanup();
                        }
                    });
                } catch (final JumblrException exception) {
                    Log.e(TAG, "JumblrException, " + exception.getMessage());
                    cleanup();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Exception", Toast.LENGTH_LONG).show();
                        }
                    });
                    if (!exception.getMessage().equals("Not Found")) {
                        throw exception;
                    }
                }
            }

            private void cleanup() {
                if (updatedStorage != null) {
                    final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                            getString(R.string.complation_file_key), Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(CompilerService.POST_LIST, updatedStorage);
                    editor.commit();
                }
                mLoadingPost.set(false);
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

    public void fillWithAvatar(final String blogName, final ImageView avatar) {
        TaskThread.run(new Runnable() {
            @Override
            public void run() {
                if (!blogName.equals("Anonymous")) {
                    fillWithImage("https://api.tumblr.com/v2/blog/" + blogName + ".tumblr.com/avatar/96", blogName, avatar);
                }
            }
        });
    }
}
