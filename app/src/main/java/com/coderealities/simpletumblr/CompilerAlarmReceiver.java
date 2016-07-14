package com.coderealities.simpletumblr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Copyright (c) 2016 coderealities.com
 */
public class CompilerAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 9873;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent compilerIntent = new Intent(context, CompilerService.class);
        compilerIntent.setAction(CompilerService.ACTION_UPDATE_COMPLATION);
        context.startService(compilerIntent);
    }
}
