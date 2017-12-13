package com.jcc.sukura;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

import java.util.Stack;

/**
 * Created by jincancan on 2017/12/13.
 * Description:
 */

public class JApp extends MultiDexApplication implements Thread.UncaughtExceptionHandler, Application.ActivityLifecycleCallbacks {

    private static JApp instance;
    private Stack<Activity> mActivityStack = new Stack<Activity>();

    public static JApp getIns(){
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {

    }
}
