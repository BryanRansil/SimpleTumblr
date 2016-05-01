package com.coderealities.simpletumblr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.AnswerPost;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Copyright coderealities.com 4/29/2016.
 */
public class PostContent {
    private static final String TAG = PostContent.class.getName();
    private final Post mPost;
    private final List<Drawable> mDrawables;
    private final Map<String, Drawable> mAvatars;
    private final Map<Drawable, PhotoSize> mDrawableSizes;
    private final Long mNoteCount;

    public PostContent(Post post, JumblrClient jumblrClient) {
        mPost = post;
        mNoteCount = post.getNoteCount();
        //loadSourceLine(jumblrClient);
        if (post.getType().equals("photo")) {
            mDrawableSizes = new HashMap<>();
            mDrawables = loadImages(post);
        } else {
            mDrawableSizes = null;
            mDrawables = null;
        }
        mAvatars = new HashMap<>();
    }

    private void loadSourceLine(JumblrClient jumblrClient) {
        HttpURLConnection connection = null;
        try {
            String blogAvatarUrl = jumblrClient.blogInfo(mPost.getBlogName()).avatar();

            connection = (HttpURLConnection) new URL(blogAvatarUrl).openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();
            Drawable drawable = Drawable.createFromStream(input, String.valueOf(mPost.getBlogName()));
            mAvatars.put(mPost.getBlogName(), drawable);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public View generateView(Context context, final int parentWidth) {
        PostView postView = new PostView(context);
        postView.setNoteCount(mNoteCount);
        postView.setPosterLine(mPost.getBlogName(), mAvatars.get(mPost.getBlogName()));
        if (mPost instanceof PhotoPost && mDrawables != null) {
            for (Drawable drawable : mDrawables) {
                ImageView imageView = new ImageView(context);
                Log.d(TAG, drawable.getBounds().width() + " " + drawable.getBounds().height());
                Drawable scaledDrawable = new BitmapDrawable(context.getResources(),
                        Bitmap.createScaledBitmap(((BitmapDrawable)drawable).getBitmap(),
                                (int) (parentWidth * 0.9),
                                (int) (((parentWidth * 0.9) / mDrawableSizes.get(drawable).getWidth()) * mDrawableSizes.get(drawable).getHeight()),
                                true));
                ((BitmapDrawable)drawable).getBitmap().recycle();

                imageView.setImageDrawable(scaledDrawable);
                addToContentView(postView, imageView);
            }
            addTextContent(postView, context, ((PhotoPost) mPost).getCaption());
        } else if (mPost instanceof TextPost) {
            addTextContent(postView, context, ((TextPost) mPost).getBody());
        } else if (mPost instanceof AnswerPost) {
            addTextContent(postView, context, ((AnswerPost) mPost).getQuestion());
            addTextContent(postView, context, ((AnswerPost) mPost).getAnswer());
        } else {
            addTextContent(postView, context, mPost.getType());
        }
        return postView;
    }

    private void addTextContent(PostView postView, Context context, String text) {
        WebView webView = new WebView(context);
        webView.setBackgroundColor(getColor(context, R.color.post_background_color));

        webView.loadData(text, "text/html; charset=utf-8", "utf-8");
        addToContentView(postView, webView);
    }

    private static int getColor(Context context, int colorId) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, colorId);
        } else {
            return context.getResources().getColor(colorId);
        }
    }


    private static void addToContentView(PostView postView, View view) {
        ((LinearLayout)postView.findViewById(R.id.post_content_layout)).addView(view);
    }

    @Nullable
    private List<Drawable> loadImages(Post post) {
        HttpURLConnection connection = null;

        try {
            final List<Drawable> drawables = new LinkedList<Drawable>();
            for (Photo photo : ((PhotoPost) post).getPhotos()) {
                connection = (HttpURLConnection) new URL(photo.getOriginalSize().getUrl()).openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                Drawable drawable = Drawable.createFromStream(input, String.valueOf(post.getId()));
                drawables.add(drawable);
                mDrawableSizes.put(drawable, photo.getOriginalSize());
            }
            return drawables;
        } catch (IOException e) {
            Log.d(TAG, "Failed to use the URL that was provided");
            e.printStackTrace();
        }
        return null;
    }
}
