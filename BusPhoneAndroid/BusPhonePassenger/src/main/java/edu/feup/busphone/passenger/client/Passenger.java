package edu.feup.busphone.passenger.client;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import edu.feup.busphone.BusPhone;

/**
 * Singleton
 */
public class Passenger {
    private static final Passenger INSTANCE = new Passenger();

    public static Passenger getInstance() {
        return INSTANCE;
    }

    private String auth_token_ = null;

    private String name_;
    private String card_number_;
    private TicketsWallet tickets_wallet_;


    private Passenger() {
        name_ = "";
        card_number_ = "";
        tickets_wallet_ = new TicketsWallet();
    }

    public void authenticateUser(String token) {
        Context context = BusPhone.getContext();
        SharedPreferences.Editor preferences_editor = context.getSharedPreferences(BusPhone.Constants.PASSENGER_PREFERENCES, Context.MODE_PRIVATE).edit();
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
            SharedPreferences preferences = context.getSharedPreferences(BusPhone.Constants.PASSENGER_PREFERENCES, Context.MODE_PRIVATE);
            auth_token_ = preferences.getString(BusPhone.Constants.PREF_AUTH_TOKEN, null);
        }

        return auth_token_;
    }

    public void removeCredentials() {
        Context context = BusPhone.getContext();
        SharedPreferences.Editor preferences_editor = context.getSharedPreferences(BusPhone.Constants.PASSENGER_PREFERENCES, Context.MODE_PRIVATE).edit();
        preferences_editor.remove(BusPhone.Constants.PREF_LOGGED_IN);
        preferences_editor.remove(BusPhone.Constants.PREF_AUTH_TOKEN);
        preferences_editor.commit();

        auth_token_ = null;
    }

    public void setTicketsWallet(TicketsWallet tickets_wallet) {
        tickets_wallet_ = tickets_wallet;
    }

    public TicketsWallet getTicketsWallet() {
        return tickets_wallet_;
    }

    public void setInfo(JSONObject response) {

    }

    public void loadCachedData() {
        Context context = BusPhone.getContext();
        SharedPreferences preferences = context.getSharedPreferences(BusPhone.Constants.PASSENGER_PREFERENCES, Context.MODE_PRIVATE);

    }
}
