package edu.feup.busphone.passenger.ui;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;

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

        Log.d(TAG, "t1=" + t1 + " t2=" + t2 + " t3=" + t3);

        Handler handler = getWindow().getDecorView().getHandler();
        Thread buy = new Thread(new WebServiceCallRunnable(handler) {
            @Override
            public void run() {
                final HashMap<String, String> response = NetworkUtilities.buy(token, t1, t2, t3, false);

                handler_.post(new Runnable() {
                    @Override
                    public void run() {
                        if ("OK".equals(response.get("info"))) {
                            showPurchaseDialog(t1, t2, t3, Double.parseDouble(response.get("cost")));
                        } else {
                            Toast.makeText(BuyTicketsActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        buy.start();
    }

    public void showPurchaseDialog(int t1, int t2, int t3, double cost) {
        PurchaseDialogFragment.newInstance(t1, t2, t3, cost).show(getFragmentManager(), "purchase_dialog");
    }

    public static class PurchaseDialogFragment extends DialogFragment {
        public interface PurchaseDialogFragmentListener {
            public void onConfirmation();
        }

        private static final String KEY_T1_COUNT = "t1_count";
        private static final String KEY_T2_COUNT = "t2_count";
        private static final String KEY_T3_COUNT = "t3_count";
        private static final String KEY_EXTRA = "extra";
        private static final String KEY_COST = "cost";

        private Handler handler_;

        private TextView t1_counter_text_;
        private TextView t2_counter_text_;
        private TextView t3_counter_text_;
        private TextView cost_text_;

        private Button confirm_button_;

        private int t1_;
        private int t2_;
        private int t3_;
        private double cost_;

        private String extra_ = null;

        static PurchaseDialogFragment newInstance(int t1, int t2, int t3, double cost) {
            PurchaseDialogFragment purchase_fragment = new PurchaseDialogFragment();

            Bundle args = new Bundle();
            args.putInt(KEY_T1_COUNT, t1);
            args.putInt(KEY_T2_COUNT, t2);
            args.putInt(KEY_T3_COUNT, t3);
            args.putDouble(KEY_COST, cost);
            purchase_fragment.setArguments(args);

            return purchase_fragment;
        }

        @Override
        public void onCreate(Bundle saved_instance_state) {
            super.onCreate(saved_instance_state);

            Bundle args = getArguments();
            t1_ = args.getInt(KEY_T1_COUNT);
            t2_ = args.getInt(KEY_T2_COUNT);
            t3_ = args.getInt(KEY_T3_COUNT);
            cost_ = Math.round(args.getDouble(KEY_COST) * 100.0) / 100.0;

            if (args.containsKey(KEY_EXTRA)) {
                extra_ = args.getString(KEY_EXTRA);
            }

            handler_ = getActivity().getWindow().getDecorView().getHandler();

            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved_instance_state) {
            View v = inflater.inflate(R.layout.purchase_fragment, container, false);

            t1_counter_text_ = (TextView) v.findViewById(R.id.t1_counter_text);
            t2_counter_text_ = (TextView) v.findViewById(R.id.t2_counter_text);
            t3_counter_text_ = (TextView) v.findViewById(R.id.t3_counter_text);
            cost_text_ = (TextView) v.findViewById(R.id.cost_text);

            t1_counter_text_.setText(Integer.toString(t1_));
            t2_counter_text_.setText(Integer.toString(t2_));
            t3_counter_text_.setText(Integer.toString(t3_));

            cost_text_.setText(Double.toString(cost_) + " " + getResources().getString(R.string.euro));

            confirm_button_ = (Button) v.findViewById(R.id.confirm_button);
            confirm_button_.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Thread purchase = new Thread(new WebServiceCallRunnable(handler_) {
                        @Override
                        public void run() {
                            final HashMap<String, String> response = NetworkUtilities.buy(Passenger.getInstance().getAuthToken(), t1_, t2_, t3_, true);

                            handler_.post(new Runnable() {
                                @Override
                                public void run() {
                                    getActivity().finish();
                                }
                            });
                        }
                    });
                    purchase.start();
                }
            });

            return v;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
        }
    }

}
