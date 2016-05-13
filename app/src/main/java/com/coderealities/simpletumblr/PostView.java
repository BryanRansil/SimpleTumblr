package com.coderealities.simpletumblr;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Represents a tumblr post.
 * Copyright 2016, coderealities.com
 */
public class PostView extends LinearLayout {
    protected final TextView mNoteCount;
    protected final LinearLayout mContentView;
    public PostView(Context context) {
        super(context);
        inflate(context, R.layout.post_view, this);
        mNoteCount = (TextView)findViewById(R.id.note_count);
        mContentView = (LinearLayout)findViewById(R.id.post_content_layout);
    }

    public void setNoteCount(Long noteCount) {
        mNoteCount.setText(noteCount + " ");
    }
}
