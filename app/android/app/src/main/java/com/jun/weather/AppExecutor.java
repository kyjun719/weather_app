package com.jun.weather;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutor {
    private final Executor repositoryIO;
    private final Executor networkIO;
    private final Executor uiIO;

    public AppExecutor(Executor repositoryIO, Executor networkIO, Executor uiIO) {
        this.repositoryIO = repositoryIO;
        this.networkIO = networkIO;
        this.uiIO = uiIO;
    }

    public AppExecutor() {
        this(Executors.newFixedThreadPool(3), Executors.newSingleThreadExecutor(),new MainThreadExecutor());
    }

    public void runInRepositoryIO(Runnable runnable) {
        repositoryIO.execute(runnable);
    }

    public void runInNetworkIO(Runnable runnable) {
        try {
            networkIO.execute(runnable);
            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runInUIIO(Runnable runnable) {
        uiIO.execute(runnable);
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
