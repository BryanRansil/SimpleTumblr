package com.coderealities.simpletumblr;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Represents a tumblr post.
 * Copyright 2016, coderealities.com
 */
public class PostView extends LinearLayout {
    protected final TextView textContent;
    protected LinearLayout imageContent;
    protected final TextView mNoteCount;
    public PostView(Context context) {
        super(context);
        inflate(context, R.layout.post_view, this);
        imageContent = (LinearLayout)findViewById(R.id.image_content);
        textContent = (TextView)findViewById(R.id.text_content);
        textContent.setText("HIIIIIII");
        mNoteCount = (TextView)findViewById(R.id.note_count);
    }

    public void setNoteCount(Long noteCount) {
        mNoteCount.setText(noteCount + " ");
    }
}
