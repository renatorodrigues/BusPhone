package edu.feup.busphone;

import android.app.Application;
import android.content.Context;

import java.util.UUID;

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

        // Bus
        public static final String PREF_BUS_ID = "bus_id";

        public static final String VALIDATE_CHANNEL_NAME = "validate_channel";
        public static final UUID VALIDATE_CHANNEL_UUID = UUID.fromString("575a64f6-f514-4b4d-99d3-f3b57d38f5e2");

        // Tickets durations (seconds)
        public static final int T1_DURATION = 900;
        public static final int T2_DURATION = 1800;
        public static final int T3_DURATION = 3600;

    }
}
