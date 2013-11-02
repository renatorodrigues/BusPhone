package edu.feup.busphone;

import android.app.Application;
import android.content.Context;

public class BusPhone extends Application {
    private static Context context_;

    @Override
    public void onCreate() {
        super.onCreate();

        context_ = getApplicationContext();
    }

    public static Context getContext() {
        return context_;
    }

    public static final class Constants {
        // Shared preferences
        public static final String PASSENGER_PREFERENCES = "passenger_details";
        public static final String TERMINAL_PREFERENCES = "terminal_details";
        public static final String INSPECTOR_PREFERENCES = "inspector_preferences";

        public static final String PREF_LOGGED_IN = "logged_in";
        public static final String PREF_REGISTERED = "registered";
        public static final String PREF_AUTH_TOKEN = "auth_token";
    }
}
