package com.coderealities.simpletumblr;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tumblr.jumblr.types.Post;

import java.util.List;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {
    private List<Post> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mCardView;
        public TextView mTextContentView;
        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
            mTextContentView = (TextView) v.findViewById(R.id.text_content);
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
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                                              .inflate(R.layout.post_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextContentView.setText(getContent(mDataset.get(position)) + " ");
    }

    private String getContent(Post post) {
        return post.getBlogName() + " " + post.getRebloggedFromName();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
