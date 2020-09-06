package com.jun.weather;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.jun.weather.repository.AppRepository;
import com.jun.weather.util.CLogger;
import com.jun.weather.util.PreferenceUtils;

public class BaseApplication extends Application {
    private AppExecutor appExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
        this.appExecutor = new AppExecutor();

        setDebug();
        CLogger.init();
        PreferenceUtils.getInstance().init(getBaseContext());
    }

    public AppRepository getRepository() {
        AppRepository appRepository = AppRepository.getInstance(getBaseContext(), appExecutor);
        appRepository.init(this);
        return appRepository;
    }

    public AppExecutor getAppExecutor() {
        return this.appExecutor;
    }

    private static boolean debuggable = false;
    private void setDebug() {
        PackageManager pm = getBaseContext().getPackageManager();
        try {
            //패키지명으로 ApplicationInfo 검색
            ApplicationInfo appInfo = pm.getApplicationInfo(getBaseContext().getPackageName(), 0);
            //ApplicationInfo 의 플래그중 ApplicationInfo.FLAG_DEBUGGABLE set 여부
            debuggable = (0 != (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
    public static boolean isDebug() {
        return debuggable;
    }
}
