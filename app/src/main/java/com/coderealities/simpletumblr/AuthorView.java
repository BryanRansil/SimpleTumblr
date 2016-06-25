package com.coderealities.simpletumblr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * Copyright (c) 2016 coderealities.com
 */
public class AuthorView extends LinearLayout {
    private static final String TAG = AuthorView.class.getName();
    protected TextView mBlogName;
    protected ImageView mBlogAvatar;

    public AuthorView(Context context) {
        super(context);
        init();
    }

    public AuthorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AuthorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.author_line, this);
        mBlogName = (TextView)findViewById(R.id.author_name);
        mBlogAvatar = (ImageView)findViewById(R.id.author_avatar);
    }

    public void setAuthorLine(String blogName, Drawable blogAvatar) {
        mBlogName.setText(blogName);
        mBlogAvatar.setImageDrawable(blogAvatar);
    }
}
