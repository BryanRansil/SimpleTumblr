package com.coderealities.simpletumblr;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/*
 * Copyright (c) 2016 coderealities.com
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    private ArrayList<String> blogCategoryList = new ArrayList<String>();
    private ArrayAdapter<String> blogButtonAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView blogList = (ListView) findViewById(R.id.blog_list);
        blogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = (String) parent.getItemAtPosition(position);
                String category = title.substring(0, title.lastIndexOf(" "));
                Intent intent = new Intent(MainActivity.this, PostListActivity.class);
                intent.putExtra(PostListActivity.EXTRA_CATEGORY, category);
                startActivity(intent);
            }
        });
        blogButtonAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, blogCategoryList);
        blogList.setAdapter(blogButtonAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelAlarm();
        blogCategoryList.clear();
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.category_file_key), Context.MODE_PRIVATE);
        Set<String> blogs = sharedPreferences.getStringSet(CompilerService.BLOG_NAMES, new HashSet<String>(0));
        for (String blog : blogs) {
            if (getCountOf(blog + CompilerService.ORIGINAL_SUFFIX) > 0) {
                blogCategoryList.add(blog + CompilerService.ORIGINAL_SUFFIX + " (" + getCountOf(blog + CompilerService.ORIGINAL_SUFFIX) + ")");
            }
            if (getCountOf(blog + CompilerService.REBLOG_SUFFIX) > 0) {
                blogCategoryList.add(blog + CompilerService.REBLOG_SUFFIX + " (" + getCountOf(blog + CompilerService.REBLOG_SUFFIX) + ")");
            }
        }
        blogButtonAdapter.notifyDataSetChanged();
    }

    private Integer getCountOf(String categoryName) {
        SharedPreferences mSharedPreferences = getSharedPreferences(
                getString(R.string.category_file_key), Context.MODE_PRIVATE);;
        String postPairings = mSharedPreferences.getString(categoryName, "");
        if ("".equals(postPairings)) {
            return 0;
        }
        return postPairings.split(",").length;
    }

    @Override
    public void onPause() {
        scheduleAlarm();
        super.onPause();
    }

    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), CompilerAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, CompilerAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    private void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), CompilerAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, CompilerAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        long experimentalPeriodMs = 4 * 1000;
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                experimentalPeriodMs, pIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
