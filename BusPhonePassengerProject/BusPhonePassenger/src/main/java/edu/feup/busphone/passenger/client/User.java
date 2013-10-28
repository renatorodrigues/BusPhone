package edu.feup.busphone.passenger.client;

import android.content.Context;
import android.content.SharedPreferences;

import edu.feup.busphone.passenger.BusPhone;
import edu.feup.busphone.passenger.util.Constants;

/**
 * Singleton
 */
public class User {
    private static final User INSTANCE = new User();

    public static User getInstance() {
        return INSTANCE;
    }

    private User() {

    }

    private String auth_token_ = null;

    public void authenticateUser(String token) {
        Context context = BusPhone.getContext();
        SharedPreferences.Editor preferences_editor = context.getSharedPreferences(Constants.PASSENGER_PREFERENCES, Context.MODE_PRIVATE).edit();
        preferences_editor.putBoolean(Constants.PREF_REGISTERED, true);

        if (token != null) {
            preferences_editor.putBoolean(Constants.PREF_LOGGED_IN, true);
            preferences_editor.putString(Constants.PREF_AUTH_TOKEN, token);

            auth_token_ = token;
        }

        preferences_editor.commit();
    }

    public String getAuthToken() {
        if (auth_token_ == null) {
            Context context = BusPhone.getContext();
            SharedPreferences preferences = context.getSharedPreferences(Constants.PASSENGER_PREFERENCES, Context.MODE_PRIVATE);
            auth_token_ = preferences.getString(Constants.PREF_AUTH_TOKEN, null);
        }

        return auth_token_;
    }

    public void removeCredentials() {
        Context context = BusPhone.getContext();
        SharedPreferences.Editor preferences_editor = context.getSharedPreferences(Constants.PASSENGER_PREFERENCES, Context.MODE_PRIVATE).edit();
        preferences_editor.remove(Constants.PREF_LOGGED_IN);
        preferences_editor.remove(Constants.PREF_AUTH_TOKEN);
        preferences_editor.commit();

        auth_token_ = null;
    }
}
