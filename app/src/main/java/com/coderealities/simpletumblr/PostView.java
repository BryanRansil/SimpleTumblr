package com.coderealities.simpletumblr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Represents a tumblr post.
 * Copyright 2016, coderealities.com
 */
public class PostView extends LinearLayout {
    protected final TextView mNoteCount;
    protected final LinearLayout mContentView;
    protected final TextView mBlogName;
    protected final ImageView mBlogAvatar;
    public PostView(Context context) {
        super(context);
        inflate(context, R.layout.post_view, this);
        mNoteCount = (TextView)findViewById(R.id.note_count);
        mContentView = (LinearLayout)findViewById(R.id.post_content_layout);
        mBlogName = (TextView)findViewById(R.id.post_blog_name);
        mBlogAvatar = (ImageView)findViewById(R.id.post_blog_avatar);
    }

    public void setNoteCount(Long noteCount) {
        mNoteCount.setText(noteCount + " ");
    }

    public void setPosterLine(String blogName, Drawable blogAvatar) {
        mBlogName.setText(blogName);
        mBlogAvatar.setImageDrawable(blogAvatar);
    }
}
