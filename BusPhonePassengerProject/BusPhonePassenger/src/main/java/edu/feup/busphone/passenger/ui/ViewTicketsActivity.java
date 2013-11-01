package edu.feup.busphone.passenger.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.client.Passenger;
import edu.feup.busphone.passenger.client.TicketsWallet;
import edu.feup.busphone.passenger.util.NetworkUtilities;
import edu.feup.busphone.passenger.util.WebServiceCallRunnable;

public class ViewTicketsActivity extends Activity {
    private static final String TAG = "ViewTicketsActivity";

    private ListView active_tickets_list_;

    private ArrayAdapter<String> adapter_;

    private TextView t1_counter_text_;
    private TextView t2_counter_text_;
    private TextView t3_counter_text_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.view_tickets_activity);

        active_tickets_list_ = (ListView) findViewById(R.id.active_tickets_list);

        adapter_ = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        active_tickets_list_.setAdapter(adapter_);

        t1_counter_text_ = (TextView) findViewById(R.id.t1_counter_text);
        t2_counter_text_ = (TextView) findViewById(R.id.t2_counter_text);
        t3_counter_text_ = (TextView) findViewById(R.id.t3_counter_text);

        final View progress_overlay = ((ViewStub) findViewById(R.id.progress_stub)).inflate();

        Thread tickets = new Thread(new WebServiceCallRunnable(new Handler()) {
            @Override
            public void run() {
                final TicketsWallet tickets_wallet = NetworkUtilities.tickets(Passenger.getInstance().getAuthToken());
                final boolean success = tickets_wallet != null;

                if (success) {
                    Passenger.getInstance().setTicketsWallet(tickets_wallet);
                }

                handler_.post(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            populateView();
                        } else {
                            Toast.makeText(ViewTicketsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }

                        progress_overlay.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
        tickets.start();
    }

    private void populateView() {
        TicketsWallet wallet = Passenger.getInstance().getTicketsWallet();

        int[] counts = wallet.getCounts();
        t1_counter_text_.setText(Integer.toString(counts[TicketsWallet.T1]));
        t2_counter_text_.setText(Integer.toString(counts[TicketsWallet.T2]));
        t3_counter_text_.setText(Integer.toString(counts[TicketsWallet.T3]));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_tickets, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                Passenger.getInstance().removeCredentials();
                startActivity(new Intent(ViewTicketsActivity.this, LoginActivity.class));
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void showTicket(View v) {
        int index = -1;
        switch (v.getId()) {
            case R.id.t1_counter_wrapper:
                index = 0;
                break;
            case R.id.t2_counter_wrapper:
                index = 1;
                break;
            case R.id.t3_counter_wrapper:
                index = 2;
                break;
        }

        Toast.makeText(this, "" + index, Toast.LENGTH_SHORT).show();
    }

    public void addTickets(View v) {
        Intent intent = new Intent(ViewTicketsActivity.this, BuyTicketsActivity.class);
        startActivity(intent);
    }
}
