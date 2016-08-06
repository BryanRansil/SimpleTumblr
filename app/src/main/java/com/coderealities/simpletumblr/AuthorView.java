package com.coderealities.simpletumblr;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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

    public AuthorView(Context context, String blogName, Drawable blogAvatar) {
        this(context);
        setContent(blogName, blogAvatar);
    }

    public AuthorView(Context context) {
        this(context, null);
    }

    public AuthorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AuthorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(getContext(), R.layout.author_line, this);
        mBlogName = (TextView)findViewById(R.id.author_name);
        mBlogAvatar = (ImageView)findViewById(R.id.author_avatar);
    }

    public void setContent(String blogName, Drawable blogAvatar) {
        mBlogName.setText(blogName);
        mBlogAvatar.setImageDrawable(blogAvatar);
    }

    public void setText(String text) {
        mBlogName.setText(text);
    }

    public void setLink(String postUrl) {
        Uri linkUri = Uri.parse(postUrl);
        final Intent linkIntent = new Intent(Intent.ACTION_VIEW, linkUri);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(linkIntent);
            }
        });
    }
}
