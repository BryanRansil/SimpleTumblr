package com.coderealities.simpletumblr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
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
    protected final AuthorView mAuthorLine;
    public PostView(Context context) {
        super(context);
        inflate(context, R.layout.post_view, this);
        mNoteCount = (TextView)findViewById(R.id.note_count);
        mContentView = (LinearLayout)findViewById(R.id.post_content_layout);
        mAuthorLine = (AuthorView)findViewById(R.id.author_line);
    }

    public void setNoteCount(Long noteCount) {
        mNoteCount.setText(noteCount + " ");
    }

    public void setAuthorLine(String blogName, Drawable blogAvatar) {
        mAuthorLine.setAuthorLine(blogName, blogAvatar);
    }

    public void addContent(View view) {
        mContentView.addView(view);
    }
}
