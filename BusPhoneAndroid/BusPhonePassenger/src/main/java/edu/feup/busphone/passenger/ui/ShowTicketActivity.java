package edu.feup.busphone.passenger.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.IOException;

import edu.feup.busphone.BusPhone;
import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.client.Passenger;
import edu.feup.busphone.passenger.client.TicketsWallet;
import edu.feup.busphone.passenger.util.qrcode.Contents;
import edu.feup.busphone.passenger.util.qrcode.QRCodeEncoder;
import edu.feup.busphone.ui.ProgressDialogFragment;
import edu.feup.busphone.hardware.bluetooth.BluetoothRunnable;

public class ShowTicketActivity extends Activity {
    private static final String TAG = "ShowTicketActivity";

    public static final String EXTRA_TICKET_TYPE = "ticket_type";

    public static final String EXTRA_VALIDATED_TICKET = "validated_ticket";

    private BluetoothAdapter bluetooth_adapter_;

    private Handler bluetooth_handler_;
    private PassengerBluetoothRunnable passenger_bluetooth_runnable_;
    private Thread bluetooth_listener_thread_;

    private ImageView qr_code_image_;
    private LinearLayout status_linear_layout_;

    private ProgressDialogFragment progress_dialog_fragment_;

    private int ticket_type_;
    private boolean validated_ = false;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.show_ticket_activity);

        WindowManager window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = window_manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int display_width = size.x;
        int display_height = size.y;

        qr_code_image_  = (ImageView) findViewById(R.id.qr_code_image);
        status_linear_layout_ = (LinearLayout) findViewById(R.id.status_linear_layout);

        ticket_type_ = getIntent().getIntExtra(EXTRA_TICKET_TYPE, -1);
        if (ticket_type_ == -1) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TicketsWallet.Ticket ticket = Passenger.getInstance().getTicketsWallet().getTicket(ticket_type_, 0);
        Log.d(TAG, "Ticket UUID: " + ticket.getId());

        bluetooth_adapter_ = BluetoothAdapter.getDefaultAdapter();

        String data = bluetooth_adapter_.getAddress();
        Log.d(TAG, "Bluetooth MAC Address: " + data);

        int dimension = display_width < display_height ? display_width : display_height; /* smaller dimension */
        String type = Contents.Type.TEXT;
        String format = BarcodeFormat.QR_CODE.toString();

        try {
            QRCodeEncoder qr_code_encoder = new QRCodeEncoder(data, null, type, format, dimension);

            Bitmap bitmap = qr_code_encoder.encodeAsBitmap();
            qr_code_image_.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        bluetooth_handler_ = new Handler();
        passenger_bluetooth_runnable_ = new PassengerBluetoothRunnable(ticket.getId(), bluetooth_handler_);
        bluetooth_listener_thread_ = new Thread(passenger_bluetooth_runnable_);
        bluetooth_listener_thread_.start();
    }

    public class PassengerBluetoothRunnable extends BluetoothRunnable {
        private static final int TIMEOUT = 100000;

        private boolean running_ = true;

        private String ticket_id_;

        private BluetoothServerSocket bluetooth_server_socket_;

        public PassengerBluetoothRunnable(String ticket_id, Handler handler) {
            super(handler);
            ticket_id_ = ticket_id;
        }

        @Override
        public void run() {
            try {
                bluetooth_server_socket_ = bluetooth_adapter_.listenUsingInsecureRfcommWithServiceRecord(BusPhone.Constants.VALIDATE_CHANNEL_NAME, BusPhone.Constants.VALIDATE_CHANNEL_UUID);

                while (running_) {
                    bluetooth_socket_ = bluetooth_server_socket_.accept(TIMEOUT);

                    send(ticket_id_);

                    handler_.post(new Runnable() {
                        @Override
                        public void run() {
                            progress_dialog_fragment_ = ProgressDialogFragment.newInstance("Awaiting validation", true);
                            progress_dialog_fragment_.show(getFragmentManager(), "show_ticket_progress");
                        }
                    });

                    final String response = receive();
                    final boolean success = "ACK".equals(response);
                    final String message = success ? "Ticket successfully validated." : "Invalid ticket.";
                    handler_.post(new Runnable() {
                        @Override
                        public void run() {
                            progress_dialog_fragment_.dismiss();
                            addStatus(message);
                            if (success) {
                                validated_ = true;
                            }
                        }
                    });

                    stop();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            running_ = false;

            try {
                if (bluetooth_socket_ != null) {
                    bluetooth_socket_.close();
                }

                if (bluetooth_server_socket_ != null) {
                    bluetooth_server_socket_.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        passenger_bluetooth_runnable_.stop();
        bluetooth_listener_thread_.interrupt();
        super.onDestroy();
    }

    public void addStatus(String message) {
        TextView status_text = new TextView(ShowTicketActivity.this);
        status_text.setText(message);
        status_linear_layout_.addView(status_text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_ticket, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_manual_validation:
                validated_ = true;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void returnToParentActivity(boolean validate_ticket) {
        Intent return_intent = new Intent();
        return_intent.putExtra(EXTRA_VALIDATED_TICKET, validate_ticket);
        if (validate_ticket) {
            return_intent.putExtra(EXTRA_TICKET_TYPE, ticket_type_);
        }
        setResult(RESULT_OK, return_intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (validated_) {
            returnToParentActivity(true);
        } else {
            super.onBackPressed();
        }
    }
}
