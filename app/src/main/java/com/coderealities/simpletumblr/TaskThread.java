package com.coderealities.simpletumblr;

import android.support.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Copyright (c) 2016 coderealities.com
 */
public class TaskThread {
    private static ExecutorService sExecutor = Executors.newFixedThreadPool(5);

    @Nullable
    public static <T> T getObject(Callable<T> task) {
        Future<T> future = sExecutor.submit(task);
        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void run(final Runnable task) {
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                task.run();
            }
        });
    }
}
