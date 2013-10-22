package edu.feup.busphone.passenger.ui;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import edu.feup.busphone.passenger.R;

public class BuyTicketsActivity extends Activity {
    private static final String TAG = "BuyTicketsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_tickets_activity);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.buy_tickets, menu);
        return true;
    }
    
}
