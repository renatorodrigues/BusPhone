package edu.feup.busphone.passenger;

import android.app.Application;
import android.content.Context;

public class BusPhone extends Application {
    private static Context context_;
    private static String auth_token_ = null;

    @Override
    public void onCreate() {
        super.onCreate();

        context_ = getApplicationContext();
    }

    public static Context getContext() {
        return context_;
    }
}
