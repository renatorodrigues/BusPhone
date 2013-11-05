package edu.feup.busphone.terminal.client;


import android.content.Context;
import android.content.SharedPreferences;

import edu.feup.busphone.BusPhone;

/**
 * Singleton
 */
public class Bus {
    private static final Bus INSTANCE = new Bus();

    public static Bus getInstance() {
        return INSTANCE;
    }

    private String id_;
    private String auth_token_ = null;

    private Bus() {
        id_ = "";
    }

    public void authenticate(String token) {
        Context context = BusPhone.getContext();
        SharedPreferences.Editor preferences_editor = context.getSharedPreferences(BusPhone.Constants.TERMINAL_PREFERENCES, Context.MODE_PRIVATE).edit();
        preferences_editor.putBoolean(BusPhone.Constants.PREF_REGISTERED, true);

        if (token != null) {
            preferences_editor.putBoolean(BusPhone.Constants.PREF_LOGGED_IN, true);
            preferences_editor.putString(BusPhone.Constants.PREF_AUTH_TOKEN, token);

            auth_token_ = token;
        }

        preferences_editor.commit();
    }

    public String getAuthToken() {
        if (auth_token_ == null) {
            Context context = BusPhone.getContext();
            SharedPreferences preferences = context.getSharedPreferences(BusPhone.Constants.TERMINAL_PREFERENCES, Context.MODE_PRIVATE);
            auth_token_ = preferences.getString(BusPhone.Constants.PREF_AUTH_TOKEN, null);
        }

        return auth_token_;
    }

    public void removeCredentials() {
        Context context = BusPhone.getContext();
        SharedPreferences.Editor preferences_editor = context.getSharedPreferences(BusPhone.Constants.TERMINAL_PREFERENCES, Context.MODE_PRIVATE).edit();
        preferences_editor.remove(BusPhone.Constants.PREF_LOGGED_IN);
        preferences_editor.remove(BusPhone.Constants.PREF_AUTH_TOKEN);
        preferences_editor.commit();

        auth_token_ = null;
    }
}
