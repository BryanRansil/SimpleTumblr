package com.coderealities.simpletumblr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tumblr.jumblr.types.AnswerPost;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Represents a tumblr post.
 * Copyright (c) 2016 coderealities.com
 */
public class PostView extends LinearLayout {
    private static final String TAG = PostView.class.getName();
    private final TextView mNoteCount;
    protected final LinearLayout mContentView;
    private final AuthorView mAuthorLine;
    protected final Post mPost;
    private final ImageView mLikeButton;
    private Boolean mIsLiked;

    public PostView(Context context, Post post, Drawable blogAvatar) {
        super(context);
        inflate(context, R.layout.post_view, this);
        mNoteCount = (TextView)findViewById(R.id.note_count);
        mContentView = (LinearLayout)findViewById(R.id.post_content_layout);
        mAuthorLine = (AuthorView)findViewById(R.id.author_line);
        mAuthorLine.setContent(post.getBlogName(), blogAvatar);
        mPost = post;
        mNoteCount.setText(post.getNoteCount() + " ");
        mLikeButton = (ImageView)findViewById(R.id.like_button);
        mIsLiked = post.isLiked();
        setupLikeButton(context);
    }

    public PostView(Context context, AnswerPost post, Drawable blogAvatar) {
        this(context, (Post) post, blogAvatar);
        addContent(new AuthorView(getContext(), post.getAskingName(), null));

        WebView webView = new WebView(getContext());
        webView.setBackgroundColor(getColor(getContext(), R.color.post_background_color));

        webView.loadData(post.getQuestion(), "text/html; charset=utf-8", "utf-8");
        addContent(webView);
        addContent(post.getAnswer());
    }

    public PostView(Context context, PhotoPost post, Drawable blogAvatar) {
        this(context, (Post) post, blogAvatar);

        List<Drawable> drawables = getPostDrawables(mPost);
        if (drawables == null) {
            return;
        }
        for (Drawable drawable : drawables) {
            ImageView imageView = new ImageView(getContext());
            imageView.setPadding(4, 4, 4, 4);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            imageView.setImageDrawable(drawable);
            mContentView.addView(imageView);
        }
        addContent(post.getCaption());
    }

    public PostView(Context context, TextPost post, Drawable blogAvatar) {
        this(context, (Post) post, blogAvatar);
        addContent(post.getBody());
    }

    private void setupLikeButton(final Context context) {
        mLikeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsLiked) {
                    mLikeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.like_button_drawable));
                    TaskThread.run(new Runnable() {
                        @Override
                        public void run() {
                            mPost.unlike();
                            mIsLiked = false;
                        }
                    });
                } else {
                    mLikeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.liked_button_drawable));
                    TaskThread.run(new Runnable() {
                        @Override
                        public void run() {
                            mPost.like();
                            mIsLiked = true;
                        }
                    });
                }
            }
        });
    }

    public void addContent(View view) {
        mContentView.addView(view);
    }

    protected void addContent(String text) {
        Document doc = Jsoup.parseBodyFragment(text);
        addContent(simpleRecursiveAddContent(doc.body()));
    }

    protected View simpleRecursiveAddContent(Element element) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setPadding(0, 0, 0, 0);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        if (hasAuthorLine(element)) {
            linearLayout.addView(simpleRecursiveAddContent(element.child(1)));
            linearLayout.addView(new AuthorView(getContext(), element.child(0).child(0).ownText(), null));

            if (hasAuthorLine(element.child(1))) {
                element.child(1).child(0).remove();
                element.child(1).child(0).remove();
            }
            WebView webView = new WebView(getContext());
            webView.setBackgroundColor(getColor(getContext(), R.color.post_background_color));

            webView.loadData(element.child(1).html(), "text/html; charset=utf-8", "utf-8");
            linearLayout.addView(webView);
        }

        return linearLayout;
    }

    private boolean hasAuthorLine(Element element) {
        return element.children().size() > 0 && element.child(0).children().size() > 0 && element.child(0).child(0).attr("class").equals("tumblr_blog");
    }

    protected static int getColor(Context context, int colorId) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, colorId);
        } else {
            return context.getResources().getColor(colorId);
        }
    }

    private List<Drawable> getPostDrawables(final Post post) {
        return TaskThread.getObject(new Callable<List<Drawable>>() {
            @Override
            public List<Drawable> call() {
                try {
                    HttpURLConnection connection;
                    List<Drawable> drawables = new LinkedList<Drawable>();
                    for (Photo photo : ((PhotoPost) post).getPhotos()) {
                        connection = (HttpURLConnection) new URL(photo.getOriginalSize().getUrl()).openConnection();
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Drawable drawable = Drawable.createFromStream(input, String.valueOf(post.getId()));
                        drawables.add(drawable);
                    }
                    return drawables;

                } catch (IOException e) {
                    Log.d(TAG, "Failed to use the URL that was provided");
                    e.printStackTrace();
                }
                return null;
            }
        });
    }
}
