package com.coderealities.simpletumblr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tumblr.jumblr.types.AnswerPost;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
    private final Map<Drawable, PhotoSize> mDrawableSizes;
    private final Long mNoteCount;

    public PostContent(Post post) {
        mPost = post;
        mNoteCount = post.getNoteCount();
        if (post.getType().equals("photo")) {
            mDrawableSizes = new HashMap<>();
            mDrawables = loadImages(post);
        } else {
            mDrawableSizes = null;
            mDrawables = null;
        }
    }

    public View generateView(Context context, final int parentWidth) {
        PostView postView = new PostView(context);
        postView.setNoteCount(mNoteCount);
        if (mPost instanceof PhotoPost && mDrawables != null) {
            postView.textContent.setText(Html.fromHtml(((PhotoPost) mPost).getCaption()));
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
        } else {
            postView.textContent.setText(mPost.getType());
        }
        return postView;
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
