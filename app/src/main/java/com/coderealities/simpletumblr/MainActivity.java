package com.coderealities.simpletumblr;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import java.util.List;

public class MainActivity extends Activity {

    private JumblrClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        // Write the user's name
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = mClient.user();
                System.out.println(user.getName());
            }
        }).start();
//        refreshList();
    }

    private void refreshList() {
        List<Post> posts = mClient.userDashboard();
        // only include posts user hasn't loaded on this device (cut off via last post time)
        // evaluate each post to see whether it's interesting:
            // Is it an image or video? then yes
            // does it have any tags on the white list associated with the particular blog?
            // is it an ask, submission, or reblog? filter appropriately.
            // add it to appropriate category for optional filters
            // if it passes any of the previous tests, add it to the current list.
    }
}
