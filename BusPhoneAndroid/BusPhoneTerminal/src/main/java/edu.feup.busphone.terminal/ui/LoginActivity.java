package edu.feup.busphone.terminal.ui;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import edu.feup.busphone.terminal.R;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.login_activity);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }


    
}
