package edu.feup.busphone.inspector.ui;

import android.os.Bundle;
import android.view.Menu;

import edu.feup.busphone.inspector.R;

public class InspectionActivity extends CameraActivity {

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.inspection_activity);
    }

    @Override
    protected void onRead(String data) {
        super.onRead(data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inspection, menu);
        return true;
    }
}
