package edu.feup.busphone.passenger.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.client.Passenger;
import edu.feup.busphone.passenger.client.TicketsWallet;
import edu.feup.busphone.passenger.util.network.PassengerNetworkUtilities;
import edu.feup.busphone.util.network.WebServiceCallRunnable;

public class ViewTicketsActivity extends Activity {
    private static final String TAG = "ViewTicketsActivity";

    public static final int BUY_TICKETS_REQUEST = 1;

    private ArrayAdapter<String> adapter_;

    private TextView t1_counter_text_;
    private TextView t2_counter_text_;
    private TextView t3_counter_text_;

    // Validated tickets elements
    private TextView validated_ticket_text_;
    private LinearLayout validated_ticket_wrapper_;
    private TextView validated_ticket_type_text_;
    private TextView validated_ticket_time_left_text_;

    private Button buy_button_;

    private View progress_overlay_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.view_tickets_activity);

        validated_ticket_text_ = (TextView) findViewById(R.id.validated_ticket_text);
        validated_ticket_wrapper_ = (LinearLayout) findViewById(R.id.validated_ticket_wrapper);
        validated_ticket_type_text_ = (TextView) findViewById(R.id.validated_ticket_type_text);
        validated_ticket_time_left_text_ = (TextView) findViewById(R.id.validated_ticket_time_left_text);

        t1_counter_text_ = (TextView) findViewById(R.id.t1_counter_text);
        t2_counter_text_ = (TextView) findViewById(R.id.t2_counter_text);
        t3_counter_text_ = (TextView) findViewById(R.id.t3_counter_text);

        buy_button_ = (Button) findViewById(R.id.add_tickets_button);

        progress_overlay_ = ((ViewStub) findViewById(R.id.progress_stub)).inflate();

        setValidatedTicketVisible(false);

        Passenger.getInstance().loadLocalTicketsWallet();

        refreshTickets();
    }

    public void refreshTickets() {
        Thread tickets = new Thread(new WebServiceCallRunnable(new Handler()) {
            @Override
            public void run() {
                final TicketsWallet tickets_wallet = PassengerNetworkUtilities.tickets(Passenger.getInstance().getAuthToken());
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
                            Toast.makeText(ViewTicketsActivity.this, "Couldn't get your tickets", Toast.LENGTH_SHORT).show();
                        }

                        progress_overlay_.setVisibility(View.INVISIBLE);
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

        buy_button_.setEnabled(wallet.getTotal() < 30);
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

    public void setValidatedTicketVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;

        validated_ticket_text_.setVisibility(visibility);
        validated_ticket_wrapper_.setVisibility(visibility);
    }

    public void showTicket(View v) {
        int ticket_type;
        switch (v.getId()) {
            case R.id.t1_counter_wrapper:
                ticket_type = 0;
                break;
            case R.id.t2_counter_wrapper:
                ticket_type = 1;
                break;
            case R.id.t3_counter_wrapper:
                ticket_type = 2;
                break;
            default:
                return;
        }

        int tickets_count = Passenger.getInstance().getTicketsWallet().getCount(ticket_type);
        if (tickets_count == 0) {
            Toast.makeText(ViewTicketsActivity.this, R.string.need_ticket, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(ViewTicketsActivity.this, ShowTicketActivity.class);
            intent.putExtra(ShowTicketActivity.EXTRA_TICKET_TYPE, ticket_type);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int request_code, int result_code, Intent data) {
        if (request_code == BUY_TICKETS_REQUEST) {
            if (result_code == BuyTicketsActivity.RESULT_OK) {
                boolean new_tickets = data.getBooleanExtra(BuyTicketsActivity.EXTRA_NEW_TICKETS, false);
                if (new_tickets) {
                    refreshTickets();
                    Toast.makeText(this, "New tickets added", Toast.LENGTH_SHORT).show();

                }
            }
        }

    }

    public void buyTickets(View v) {
        Intent intent = new Intent(ViewTicketsActivity.this, BuyTicketsActivity.class);
        startActivityForResult(intent, BUY_TICKETS_REQUEST);
    }
}
