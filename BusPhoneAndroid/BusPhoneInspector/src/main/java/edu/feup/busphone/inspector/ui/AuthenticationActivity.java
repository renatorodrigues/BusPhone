package edu.feup.busphone.inspector.ui;

import android.os.Bundle;
import android.view.Menu;

import edu.feup.busphone.inspector.R;

public class AuthenticationActivity extends CameraActivity {

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.authentication_activity);
    }

    @Override
    protected void onRead(String data) {
        super.onRead(data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.authentication, menu);
        return true;
    }
    
}
