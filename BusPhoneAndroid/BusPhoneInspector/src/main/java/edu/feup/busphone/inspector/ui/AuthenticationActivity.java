package edu.feup.busphone.inspector.ui;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import edu.feup.busphone.inspector.R;

public class AuthenticationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.authentication, menu);
        return true;
    }
    
}
