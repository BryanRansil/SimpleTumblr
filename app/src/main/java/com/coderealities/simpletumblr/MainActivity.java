package com.coderealities.simpletumblr;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tumblr.jumblr.JumblrClient;

import java.util.concurrent.TimeUnit;

/*
 * Copyright (c) 2016 coderealities.com
 */
public class MainActivity extends FragmentActivity implements PostListFragment.PostListFragmentHost, StreamSettingsFragment.StreamSettingsFragmentHost {
    private static final String TAG = MainActivity.class.getName();
    private static final String POST_LIST_FRAGMENT_CONTENT = "mPostListFragmentContent";
    private static final String STREAM_MENU_OPTION = "Stream";
    private static final String STREAM_SETTINGS_MENU_OPTION = "Stream Settings";
    private static final long PERIOD_BETWEEN_UPDATES_MILLIS = TimeUnit.MINUTES.toMillis(3);
    private static final String[] OPTIONS_MENU = {STREAM_MENU_OPTION, STREAM_SETTINGS_MENU_OPTION};

    private JumblrClient mClient;
    private PostListFragment mPostListFragment;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerLayout.SimpleDrawerListener mDrawerToggle;

    private PendingIntent mRepeatingUpdateIntent;

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment fragment;
            switch (OPTIONS_MENU[position]) {
                case STREAM_MENU_OPTION :
                    fragment = mPostListFragment;
                    break;
                case STREAM_SETTINGS_MENU_OPTION :
                    fragment = new StreamSettingsFragment();
                    break;
                default:
                    return;
            }

            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();

            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = new JumblrClient(getString(R.string.consumerKey), getString(R.string.consumerSecret));
        mClient.setToken(getString(R.string.oathToken), getString(R.string.oauthSecret));

        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, OPTIONS_MENU));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new DrawerLayout.SimpleDrawerListener() {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        // TODO: setDrawerListener is deprecated. Find part of the android documentation that's actually more up to date than what I used
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState != null) {
            mPostListFragment = (PostListFragment) getFragmentManager().getFragment(savedInstanceState, POST_LIST_FRAGMENT_CONTENT);
        }

        mRepeatingUpdateIntent =  PendingIntent.getBroadcast(this, CompilerAlarmReceiver.REQUEST_CODE,
                        new Intent(getApplicationContext(), CompilerAlarmReceiver.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        if (mPostListFragment.isVisible()) {
            getFragmentManager().putFragment(outState, POST_LIST_FRAGMENT_CONTENT, mPostListFragment);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelRepeatingUpdate();
        if (mPostListFragment == null) {
            mPostListFragment = new PostListFragment();
        }

        getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, mPostListFragment)
                            .commit();
    }

    @Override
    public void onPause() {
        scheduleRepeatingUpdate();
        super.onPause();
    }

    public void cancelRepeatingUpdate() {
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(mRepeatingUpdateIntent);
    }

    private void scheduleRepeatingUpdate() {
        long firstTriggerTimeMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstTriggerTimeMillis,
                                  PERIOD_BETWEEN_UPDATES_MILLIS, mRepeatingUpdateIntent);
    }

    public void nextPost(View view) {
        Log.d(TAG, "nextPost");
        mPostListFragment.nextPost();
    }

    @Override
    public JumblrClient getJumblrClient() {
        return mClient;
    }
}