package com.coderealities.simpletumblr;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2016 coderealities.com
 *
 * Preferences: Each blog you follow can select originals or all, visuals (video or images) or all. I don't know how
 * you can do this in preferences like the ... you have in most apps. I've gotten rid of the bar that's typically on,
 * but I'm guessing I can launch that. I can also ask for that list when I get into here first, that will help reduce
 * lag time.
 */
public class StreamSettingsFragment extends Fragment {
    public static final String SETTING_ORIGINAL = "ORIGINAL";
    public static final String SETTING_VISUAL = "VISUAL";
    public static final String SETTING_PREFERENCE_DIVIDER = ";";
    private JumblrClient mClient;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private StreamSettingsFragmentHost mHost;

    private final Callable<List<Blog>> mCallUserList = new Callable<List<Blog>>() {
        private Comparator<? super Blog> mBlogNameComparator = new Comparator<Blog>() {
            @Override
            public int compare(Blog o1, Blog o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };

        @Override
        public List<Blog> call() throws Exception {
            Map<String, Object> userFollowingParams = new HashMap<String, Object>();
            int offset = 0;
            List<Blog> blogs = mClient.userFollowing();
            ArrayList<Blog> finalList = new ArrayList<>();
            while (blogs.size() > 0) {
                finalList.addAll(blogs);
                offset += 20;
                userFollowingParams.put("offset", offset);
                blogs = mClient.userFollowing(userFollowingParams);
            }
            Collections.sort(finalList, mBlogNameComparator);
            return finalList;
        }
    };

    public interface StreamSettingsFragmentHost {
        JumblrClient getJumblrClient();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mHost = (StreamSettingsFragmentHost) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement PostListFragmentHost");
        }
        mClient = mHost.getJumblrClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.stream_settings_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSharedPreferences = getActivity().getSharedPreferences(getString(R.string.blog_stream_settings_file_key), Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        // Put up the loading UI
        List<Blog> userFollowing = TaskThread.getObject(mCallUserList);
        ViewGroup settings = (ViewGroup) getActivity().findViewById(R.id.blog_stream_setting_list);

        for (Blog blog : userFollowing) {
            settings.addView(streamSettingBoxFor(blog.getName()));
        }
    }

    @Override
    public void onPause() {
        TableLayout settingsTable = (TableLayout) getActivity().findViewById(R.id.blog_stream_setting_list);
        for (int rowIndex = 1; rowIndex < settingsTable.getChildCount(); rowIndex++) {
            TableRow row = (TableRow) settingsTable.getChildAt(rowIndex);
            final String blogName = String.valueOf(((TextView)row.findViewById(R.id.blog_stream_setting_name)).getText());
            mEditor.putBoolean(blogName + SETTING_PREFERENCE_DIVIDER + SETTING_ORIGINAL,
                               ((CheckBox)row.findViewById(R.id.blog_stream_setting_original_checkbox)).isChecked());
            mEditor.putBoolean(blogName + SETTING_PREFERENCE_DIVIDER + SETTING_VISUAL,
                               ((CheckBox)row.findViewById(R.id.blog_stream_setting_visual_checkbox)).isChecked());
        }
        mEditor.commit();
        super.onPause();
    }

    private View streamSettingBoxFor(String blogName) {
        View settingBox = LayoutInflater.from(getActivity()).inflate(R.layout.stream_setting_view, null);
        ((TextView)settingBox.findViewById(R.id.blog_stream_setting_name)).setText(blogName);
        checkCheckbox(settingBox, R.id.blog_stream_setting_original_checkbox, blogName, SETTING_ORIGINAL);
        checkCheckbox(settingBox, R.id.blog_stream_setting_visual_checkbox, blogName, SETTING_VISUAL);
        return settingBox;
    }

    private void checkCheckbox(View settingBox, int checkboxId, String blogName, String settingName) {
        final String preference = blogName + SETTING_PREFERENCE_DIVIDER + settingName;
        ((CheckBox)settingBox.findViewById(checkboxId)).setChecked(mSharedPreferences.getBoolean(preference, false));
    }
}
