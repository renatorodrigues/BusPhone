package edu.feup.busphone.passenger.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.io.InputStream;

import edu.feup.busphone.BusPhone;
import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.util.qrcode.Contents;
import edu.feup.busphone.passenger.util.qrcode.QRCodeEncoder;

public class ShowTicketActivity extends Activity {
    private static final String TAG = "ShowTicketActivity";

    public static final String EXTRA_TICKET_TYPE = "ticket_type";

    private ImageView qr_code_image_;

    private BluetoothAdapter bluetooth_adapter_;

    private Handler bluetooth_handler_;
    private Thread bluetooth_listener_thread_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_ticket_activity);

        WindowManager window_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = window_manager.getDefaultDisplay();
        int display_width = display.getWidth();
        int display_height = display.getHeight();

        qr_code_image_  = (ImageView) findViewById(R.id.qr_code_image);

        int ticket_type = getIntent().getIntExtra(EXTRA_TICKET_TYPE, -1);
        if (ticket_type == -1) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        // Bluetooth listener background thread
        bluetooth_listener_thread_ = new Thread(new BluetoothListenerRunnable(bluetooth_handler_));

        bluetooth_listener_thread_.start();
    }

    public class BluetoothListenerRunnable implements Runnable {
        private static final int TIMEOUT = 100000;

        private boolean running_ = true;

        private Handler handler_;
        private BluetoothSocket client_;
        private BluetoothServerSocket bluetooth_socket_;

        public BluetoothListenerRunnable(Handler handler) {
            handler_ = handler;
        }

        @Override
        public void run() {
            try {
                bluetooth_socket_ = bluetooth_adapter_.listenUsingInsecureRfcommWithServiceRecord(BusPhone.Constants.VALIDATE_CHANNEL_NAME, BusPhone.Constants.VALIDATE_CHANNEL_UUID);

                int timeout;
                int max_timeout;
                int available;

                byte[] buffer;

                while (running_) {
                    client_ = bluetooth_socket_.accept(TIMEOUT);

                    timeout = 0;
                    max_timeout = 32;
                    available = 0;

                    InputStream input_stream = client_.getInputStream();

                    while (running_ && (available = input_stream.available()) == 0 && timeout < max_timeout) {
                        ++timeout;

                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (available > 0) {
                        buffer = new byte[available];
                        available = input_stream.read(buffer);

                        final String reply = new String(buffer);

                        handler_.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ShowTicketActivity.this, reply, Toast.LENGTH_SHORT).show();
                            }
                        });

                        stop();
                    } else {
                        client_.close();

                        handler_.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ShowTicketActivity.this, "TIMEOUT", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            running_ = false;

            try {
                client_.close();
                bluetooth_socket_.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_ticket, menu);
        return true;
    }

}
