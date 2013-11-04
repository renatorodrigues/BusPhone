package edu.feup.busphone.terminal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;

import edu.feup.busphone.BusPhone;
import edu.feup.busphone.terminal.ui.DecodeTicketActivity;
import edu.feup.busphone.terminal.ui.LoginActivity;
import edu.feup.busphone.terminal.ui.SignupActivity;

public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";

    private SharedPreferences preferences_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);

        boolean registered, logged_in;
        Class<?> cls;

        preferences_ = getSharedPreferences(BusPhone.Constants.INSPECTOR_PREFERENCES, Context.MODE_PRIVATE);

        registered = preferences_.getBoolean(BusPhone.Constants.PREF_REGISTERED, false);
        logged_in = preferences_.getBoolean(BusPhone.Constants.PREF_LOGGED_IN, false);

        registered = logged_in = true;

        if (!registered) {
            cls = SignupActivity.class;
        } else if (!logged_in) {
            cls = LoginActivity.class;
        } else {
            cls = DecodeTicketActivity.class;
        }

        startActivity(new Intent(SplashActivity.this, cls));
        finish();
    }
    
}
