package edu.feup.busphone.inspector;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import edu.feup.busphone.inspector.ui.InspectionActivity;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);

        startActivity(new Intent(SplashActivity.this, InspectionActivity.class));
        finish();
    }
}
