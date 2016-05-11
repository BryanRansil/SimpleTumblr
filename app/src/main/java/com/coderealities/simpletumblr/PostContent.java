package com.coderealities.simpletumblr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tumblr.jumblr.JumblrClient;
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
    private final Long mNoteCount;

    public PostContent(Post post, JumblrClient jumblrClient) {
        mPost = post;
        mNoteCount = post.getNoteCount();
        //loadSourceLine(jumblrClient);
        if (post.getType().equals("photo")) {
            mDrawables = loadImages(post);
        } else {
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

    public View generateView(Context context) {
        PostView postView = new PostView(context);
        postView.setNoteCount(mNoteCount);
        postView.setAuthorLine(mPost.getBlogName(), mAvatars.get(mPost.getBlogName()));
        if (mPost instanceof PhotoPost && mDrawables != null) {
            for (Drawable drawable : mDrawables) {
                ImageView imageView = new ImageView(context);
                imageView.setPadding(4, 4, 4, 4);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);
                imageView.setImageDrawable(drawable);
                postView.addContent(imageView);
            }
            addContent(postView, context, ((PhotoPost) mPost).getCaption());
        } else if (mPost instanceof TextPost) {
            addContent(postView, context, ((TextPost) mPost).getBody());
        } else if (mPost instanceof AnswerPost) {
            postView.addContent(generateAuthorLine(context, ((AnswerPost) mPost).getAskingName()));

            WebView webView = new WebView(context);
            webView.setBackgroundColor(getColor(context, R.color.post_background_color));

            webView.loadData(((AnswerPost) mPost).getQuestion(), "text/html; charset=utf-8", "utf-8");
            postView.addContent(webView);
            addContent(postView, context, ((AnswerPost) mPost).getAnswer());
        } else {
            addContent(postView, context, mPost.getType());
        }
        return postView;
    }

    private void addContent(PostView postView, Context context, String text) {
        Document doc = Jsoup.parseBodyFragment(text);
        postView.addContent(simpleRecursiveAddContent(context, doc.body()));
    }

    private View simpleRecursiveAddContent(Context context, Element element) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setPadding(0, 0, 0, 0);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        if (hasAuthorLine(element)) {
            linearLayout.addView(simpleRecursiveAddContent(context, element.child(1)));
            linearLayout.addView(generateAuthorLine(context, element.child(0).child(0).ownText()));

            if (hasAuthorLine(element.child(1))) {
                element.child(1).child(0).remove();
                element.child(1).child(0).remove();
            }
            WebView webView = new WebView(context);
            webView.setBackgroundColor(getColor(context, R.color.post_background_color));

            webView.loadData(element.child(1).html(), "text/html; charset=utf-8", "utf-8");
            linearLayout.addView(webView);
        }

        return linearLayout;
    }

    private boolean hasReply(Element element) {
        return element.children().size() > 1 && element.child(1).children().size() > 0 && hasAuthorLine(element.child(1).child(0));
    }

    private boolean hasAuthorLine(Element element) {
        return element.children().size() > 0 && element.child(0).children().size() > 0 && element.child(0).child(0).attr("class").equals("tumblr_blog");
    }

    private static int getColor(Context context, int colorId) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, colorId);
        } else {
            return context.getResources().getColor(colorId);
        }
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
            }
            return drawables;
        } catch (IOException e) {
            Log.d(TAG, "Failed to use the URL that was provided");
            e.printStackTrace();
        }
        return null;
    }

    private AuthorView generateAuthorLine(Context baseContext, String blogName) {
        AuthorView authorView = new AuthorView(baseContext);
        authorView.setAuthorLine(blogName, null);
        return authorView;
    }
}
