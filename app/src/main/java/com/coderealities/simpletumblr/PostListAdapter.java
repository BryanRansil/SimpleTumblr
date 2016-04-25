package com.coderealities.simpletumblr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {
    private static final String TAG = PostListAdapter.class.getName();
    private List<Post> mDataset;
    private Context mParentContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mCardView;
        public LinearLayout mImageContentLayout;
        public TextView mTextContentView;
        public TextView mNoteCountView;
        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
            mTextContentView = (TextView) v.findViewById(R.id.text_content);
            mImageContentLayout = (LinearLayout) v.findViewById(R.id.image_content);
            mNoteCountView = (TextView) v.findViewById(R.id.note_count);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostListAdapter(List<Post> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        if (mParentContext == null) {
            mParentContext = parent.getContext();
        }
        // create a new view
        CardView v = (CardView) LayoutInflater.from(mParentContext)
                                              .inflate(R.layout.post_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Post post = mDataset.get(position);
        holder.mTextContentView.setText(getTextContent(post));
        holder.mNoteCountView.setText(String.valueOf(post.getNoteCount()) + " ");
        final Handler mainHandler = new Handler(mParentContext .getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Drawable> drawables;
                if (post.getType().equals("photo")) {
                    drawables = loadImages(post, holder);
                } else {
                    drawables = null;
                }

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (post.getType().equals("photo") && drawables != null) {
                            for (Drawable drawable : drawables) {
                                ImageView imageView = new ImageView(mParentContext);
                                imageView.setImageDrawable(drawable);
                                holder.mImageContentLayout.addView(imageView);
                            }
                        } else {
                            holder.mImageContentLayout.removeAllViews();
                        }
                    }
                });
            }
        }).start();
    }

    @Nullable
    private List<Drawable> loadImages(Post post, final ViewHolder holder) {
        HttpURLConnection connection = null;
        final int maxImageWidth = (int) (holder.mCardView.getMeasuredWidth() - 2 * mParentContext.getResources().getDimension(R.dimen.card_margin));

        try {
            final List<Drawable> drawables = new LinkedList<Drawable>();
            for (Photo photo : ((PhotoPost) post).getPhotos()) {
                PhotoSize photoSize = photo.getSizes().get(0);
                connection = (HttpURLConnection) new URL(photo.getOriginalSize().getUrl()).openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();

                Drawable drawable = (Drawable.createFromStream(input, String.valueOf(post.getId())));
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                drawables.add(new BitmapDrawable(mParentContext.getResources(),
                                                 Bitmap.createScaledBitmap(bitmap,
                                                                           (int) (maxImageWidth*0.9),
                                                         (int) (((maxImageWidth * 0.9) / photoSize.getWidth()) * photoSize.getHeight()),
                                                                           true)));
            }
            return drawables;
        } catch (IOException e) {
            Log.d(TAG, "Failed to use the URL that was provided");
            e.printStackTrace();
        }
        return null;
    }

    private String getTextContent(Post post) {

//        if (post.getType().equals(Post.PostType.TEXT)) {
//
//        }
        return post.getBlogName() + " " + post.getType();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
