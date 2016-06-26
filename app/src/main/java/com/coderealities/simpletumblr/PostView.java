package com.coderealities.simpletumblr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tumblr.jumblr.types.Post;

/**
 * Represents a tumblr post.
 * Copyright (c) 2016 coderealities.com
 */
public class PostView extends LinearLayout {
    protected final TextView mNoteCount;
    protected final LinearLayout mContentView;
    protected final AuthorView mAuthorLine;
    private final Post mPost;
    private final ImageView mLikeButton;
    private Boolean mIsLiked;

    public PostView(final Context context, Post post) {
        super(context);
        inflate(context, R.layout.post_view, this);
        mNoteCount = (TextView)findViewById(R.id.note_count);
        mContentView = (LinearLayout)findViewById(R.id.post_content_layout);
        mAuthorLine = (AuthorView)findViewById(R.id.author_line);
        mPost = post;
        mNoteCount.setText(mPost.getNoteCount() + " ");
        mLikeButton = (ImageView)findViewById(R.id.like_button);
        mIsLiked = mPost.isLiked();
        setupLikeButton(context);
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

    public void setAuthorLine(String blogName, Drawable blogAvatar) {
        mAuthorLine.setAuthorLine(blogName, blogAvatar);
    }

    public void addContent(View view) {
        mContentView.addView(view);
    }
}
