package com.vinay.spyder;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

/**
 * Created by Vinay Sajjanapu on 8/27/2017.
 */

public class Spyder extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleAdapter() {
            @Override
            public void onActivityCreated(Activity a, Bundle savedInstanceState) {
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });
    }
}
