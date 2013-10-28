package edu.feup.busphone.passenger.ui;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import edu.feup.busphone.passenger.R;

public class ShowTicketActivity extends Activity {
    private static final String TAG = "ShowTicketActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_ticket_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_ticket, menu);
        return true;
    }
    
}
