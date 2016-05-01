package com.coderealities.simpletumblr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
            addTextContent(postView, context, ((PhotoPost) mPost).getCaption());
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

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                Log.d(TAG, span + " clicked!");
                // Do something with span.getURL() to handle the link click...
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    private void addTextContent(PostView postView, Context context, String text) {
        TextView textView = new TextView(context);
        CharSequence sequence = Html.fromHtml(text);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        textView.setText(strBuilder);
        textView.setLinksClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        addToContentView(postView, textView);
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
