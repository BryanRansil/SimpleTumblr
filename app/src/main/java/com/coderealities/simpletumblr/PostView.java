package com.coderealities.simpletumblr;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tumblr.jumblr.types.AnswerPost;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

/**
 * Represents a tumblr post.
 * Copyright (c) 2016 coderealities.com
 */
public class PostView extends LinearLayout {
    private static final String TAG = PostView.class.getName();

    protected final LinearLayout mContentView;
    protected final Post mPost;

    private final PostListFragment mPostListFragment;
    private final TextView mNoteCount;
    private final AuthorView mAuthorLine;
    private final ImageView mLikeButton;
    private Boolean mIsLiked;

    public PostView(PostListFragment postListFragment, Post post) {
        super(postListFragment.getActivity());
        mPostListFragment = postListFragment;
        inflate(postListFragment.getActivity(), R.layout.post_view, this);
        mNoteCount = (TextView)findViewById(R.id.note_count);
        mContentView = (LinearLayout)findViewById(R.id.post_content_layout);
        mAuthorLine = (AuthorView)findViewById(R.id.author_line);
        mAuthorLine.setText(post.getBlogName())
                   .setLink(post.getPostUrl());
        postListFragment.fillWithAvatar(post.getBlogName(), mAuthorLine.mBlogAvatar);

        mPost = post;
        mNoteCount.setText(post.getNoteCount() + " " + post.getType());
        mLikeButton = (ImageView)findViewById(R.id.like_button);
        mIsLiked = post.isLiked();
        setupLikeButton(postListFragment.getActivity());
    }

    public PostView(PostListFragment postListFragment, AnswerPost post) {
        this(postListFragment, (Post) post);

        TextView questionView = new TextView(getContext());
        questionView.setTextColor(getColor(getContext(), R.color.black));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            questionView.setBackground(getResources().getDrawable(R.drawable.question_view_background, postListFragment.getContext().getTheme()));
        } else {
            questionView.setBackgroundDrawable(getResources().getDrawable(R.drawable.question_view_background));
        }
        questionView.setText(post.getQuestion());
        addContent(questionView);
        addContent(new AuthorView(postListFragment, post.getAskingName()));

        addContent(post.getAnswer());
    }

    public PostView(PostListFragment postListFragment, final PhotoPost post) {
        this(postListFragment, (Post) post);

        int width = mContentView.getWidth() - mContentView.getPaddingLeft();
        for (Photo photo : post.getPhotos()) {
            final ImageView imageView = new ImageView(getContext());
            imageView.setPadding(4, 4, 4, 4);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            imageView.setMinimumWidth(width);
            imageView.setMinimumHeight(width * photo.getOriginalSize().getHeight() / photo.getOriginalSize().getWidth());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setImageDrawable(postListFragment.getActivity().getDrawable(R.drawable.blank_image));
            } else {
                imageView.setImageDrawable(postListFragment.getActivity().getResources().getDrawable(R.drawable.blank_image));
            }
            mContentView.addView(imageView);
            postListFragment.fillWithImage(getOptimalSize(photo, imageView).getUrl(), String.valueOf(post.getId()), imageView);
        }
        addContent(post.getCaption());
    }

    private PhotoSize getOptimalSize(Photo photo, ImageView imageView) {
        return photo.getOriginalSize();
//        for (PhotoSize photoSize : photo.getSizes()) {
//            if (Math.abs(photoSize.getHeight() - imageView.getHeight()) < Math.abs(bestMatch.getHeight() - imageView.getHeight())
//                    && Math.abs(photoSize.getWidth() - imageView.getWidth()) < Math.abs(bestMatch.getWidth() - imageView.getWidth())) {
//                bestMatch = photoSize;
//            }
//        }
//        return bestMatch;
    }

    public PostView(PostListFragment postListFragment, TextPost post) {
        this(postListFragment, (Post) post);
        addContent(post.getBody());
    }

    private void setupLikeButton(final Context context) {
        mLikeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsLiked) {
                    mLikeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.like_button_drawable));
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            mPost.unlike();
                            mIsLiked = false;
                        }
                    });
                } else {
                    mLikeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.liked_button_drawable));
                    AsyncTask.execute(new Runnable() {
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
        if (hasAuthorLine(doc.body())) {
            addContent(simpleRecursiveAddContent(doc.body(), mPost.getBlogName()));
        } else {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            addContent(insertContentInto(linearLayout, doc.body().children(), null));
        }
    }

    protected LinearLayout insertContentInto(LinearLayout linearLayout, Elements whatThisPosterWrote, String blogName) {
        if (whatThisPosterWrote.size() > 0 && blogName != null) {
            linearLayout.addView(new AuthorView(mPostListFragment, blogName));
        }
        for (Element child : whatThisPosterWrote) {
            WebView webView = new WebView(getContext());
            webView.setBackgroundColor(getColor(getContext(), R.color.post_background_color));
            webView.setPadding(4, 4, 4, 4);
            webView.loadData(child.html(), "text/html; charset=utf-8", "utf-8");
            linearLayout.addView(webView);
        }
        return linearLayout;
    }

    protected View simpleRecursiveAddContent(Element element, String blogName) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setPadding(0, 0, 0, 0);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        Elements children = element.children();
        if (hasAuthorLine(element)) {
            linearLayout.addView(simpleRecursiveAddContent(element.child(1), element.child(0).child(0).ownText()));
            children.remove(1);
            children.remove(0);
        }
        insertContentInto(linearLayout, children, blogName);
        return linearLayout;
    }

    private boolean hasAuthorLine(Element element) {
        return element.children().size() > 1 && element.child(0).children().size() > 0 && element.child(0).child(0).attr("class").equals("tumblr_blog");
    }

    protected static int getColor(Context context, int colorId) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, colorId);
        } else {
            return context.getResources().getColor(colorId);
        }
    }
}
