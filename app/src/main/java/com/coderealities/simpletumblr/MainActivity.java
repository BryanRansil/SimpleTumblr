package com.coderealities.simpletumblr;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private JumblrClient mClient;
    private RecyclerView mPostListView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPostListView = (RecyclerView) findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mPostListView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mPostListView.setLayoutManager(mLayoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Create a new client
        // Authenticate via OAuth
        mClient = new JumblrClient(
                "TFGB1WudJLpsDxa2B1cgYH2nE98aVxkfto8deCQFlRhxUSNjhA",
                "nXu4bZo5Qwy1Fm0TlWbWkjIlQqG1S34UEDpboUpC9hjqzMUE8B"
        );
        mClient.setToken(
                "JSgSbWUnpQjQJv2RV7OR0kpfpYQjqHtrK5Y5Nfq9LPKC2s5Tv7",
                "248VjWRvM6u2uayQchWcFD0aOwJOOdWn1ShPjgkVrr3IHu5BEk"
        );

        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = mClient.user();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("reblog_info", "true");
                List<Post> complation = mClient.userDashboard(params);

                final PostListAdapter postAdapter = new PostListAdapter(complation);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostListView.setAdapter(postAdapter);
                    }
                });
            }
        }).start();
    }
}
