package edu.feup.busphone.passenger.ui;

import android.app.ActionBar;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;


import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.client.Passenger;
import edu.feup.busphone.passenger.client.TicketsWallet;
import edu.feup.busphone.passenger.util.NetworkUtilities;
import edu.feup.busphone.passenger.util.WebServiceCallRunnable;

public class BuyTicketsActivity extends Activity {
    private static final String TAG = "BuyTicketsActivity";

    private static final int MAX_TICKETS_PER_TYPE = 10;

    private TextView t1_counter_text_;
    private TextView t2_counter_text_;
    private TextView t3_counter_text_;

    private int[] max_tickets_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_tickets_activity);

        t1_counter_text_ = (TextView) findViewById(R.id.t1_counter_text);
        t2_counter_text_ = (TextView) findViewById(R.id.t2_counter_text);
        t3_counter_text_ = (TextView) findViewById(R.id.t3_counter_text);


        int[] passenger_tickets_count = Passenger.getInstance().getTicketsWallet().getCounts();

        max_tickets_ = new int[] {
                MAX_TICKETS_PER_TYPE - passenger_tickets_count[TicketsWallet.T1],
                MAX_TICKETS_PER_TYPE - passenger_tickets_count[TicketsWallet.T2],
                MAX_TICKETS_PER_TYPE - passenger_tickets_count[TicketsWallet.T3]
        };

        ActionBar action_bar = getActionBar();
        action_bar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.buy_tickets, menu);
        return true;
    }

    public void increment(View v) {
        TextView counter_text;
        int index;
        switch (v.getId()) {
            case R.id.t1_increment_button:
                index = 0;
                counter_text = t1_counter_text_;
                break;
            case R.id.t2_increment_button:
                index = 1;
                counter_text = t2_counter_text_;
                break;
            case R.id.t3_increment_button:
                index = 2;
                counter_text = t3_counter_text_;
                break;
            default:
                return;
        }

        int count = Integer.parseInt(counter_text.getText().toString());
        if (count < max_tickets_[index]) {
            counter_text.setText(Integer.toString(++count));
        }
    }

    public void decrement(View v) {
        TextView counter_text;
        int index;
        switch (v.getId()) {
            case R.id.t1_decrement_button:
                index = 0;
                counter_text = t1_counter_text_;
                break;
            case R.id.t2_decrement_button:
                index = 1;
                counter_text = t2_counter_text_;
                break;
            case R.id.t3_decrement_button:
                index = 2;
                counter_text = t3_counter_text_;
                break;
            default:
                return;
        }

        int count = Integer.parseInt(counter_text.getText().toString());
        if (count > 0) {
            counter_text.setText(Integer.toString(--count));
        }
    }

    public void buy(View v) {
        final String token = Passenger.getInstance().getAuthToken();
        final int t1 = Integer.parseInt(t1_counter_text_.getText().toString());
        final int t2 = Integer.parseInt(t2_counter_text_.getText().toString());
        final int t3 = Integer.parseInt(t3_counter_text_.getText().toString());

        Thread buy = new Thread(new WebServiceCallRunnable(getWindow().getDecorView().getHandler()) {
            @Override
            public void run() {
                NetworkUtilities.buy(token, t1, t2, t3);

                handler_.post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });
        buy.start();
    }

}
