package edu.feup.busphone.terminal.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import edu.feup.busphone.BusPhone;
import edu.feup.busphone.terminal.R;
import edu.feup.busphone.terminal.util.CameraPreview;
import edu.feup.busphone.util.bluetooth.BluetoothRunnable;

public class DecodeTicketActivity extends Activity {
    private static final String TAG = "DecodeTicketActivity";

    private Camera camera_;
    private CameraPreview camera_preview_;
    private Handler auto_focus_handler_;

    private TextView scan_text_;
    private Button scan_button_;

    ImageScanner scanner_;

    private boolean barcode_scanned_ = false;
    private boolean previewing_ = true;

    private BluetoothAdapter bluetooth_adapter_;
    private Handler bluetooth_handler_;
    private Thread bluetooth_thread_;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decode_ticket_activity);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        auto_focus_handler_ = new Handler();
        camera_ = getCameraInstance();

        scanner_ = new ImageScanner();
        scanner_.setConfig(0, Config.X_DENSITY, 3);
        scanner_.setConfig(0, Config.Y_DENSITY, 3);

        camera_preview_ = new CameraPreview(this, camera_, preview_callback, auto_focus_callback);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(camera_preview_);

        scan_text_ = (TextView) findViewById(R.id.scanText);

        scan_button_ = (Button) findViewById(R.id.ScanButton);
        scan_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcode_scanned_) {
                    barcode_scanned_ = false;
                    scan_text_.setText("Scanning...");
                    camera_.setPreviewCallback(preview_callback);
                    camera_.startPreview();
                    previewing_ = true;
                    camera_.autoFocus(auto_focus_callback);
                }
            }
        });

        bluetooth_adapter_ = BluetoothAdapter.getDefaultAdapter();
        bluetooth_handler_ = new Handler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    public static Camera getCameraInstance() {
        Camera camera = null;
        camera = Camera.open();
        return camera;
    }

    private void releaseCamera() {
        if (camera_ != null) {
            previewing_ = false;
            camera_.setPreviewCallback(null);
            camera_.release();
            camera_ = null;
        }
    }

    private Runnable auto_focus_runnable = new Runnable() {
        @Override
        public void run() {
            if (previewing_) {
                camera_.autoFocus(auto_focus_callback);
            }
        }
    };

    Camera.PreviewCallback preview_callback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner_.scanImage(barcode);

            if (result != 0) {
                previewing_ = false;
                camera_.setPreviewCallback(null);
                camera_.stopPreview();

                SymbolSet symbols = scanner_.getResults();
                for (Symbol symbol : symbols) {
                    String mac_address = symbol.getData();
                    scan_text_.setText("MAC address: " + mac_address);
                    barcode_scanned_ = true;
                    bluetooth_thread_ = new Thread(new TerminalBluetoothRunnable(mac_address, bluetooth_handler_));
                    bluetooth_thread_.start();
                }
            }
        }
    };

    Camera.AutoFocusCallback auto_focus_callback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            auto_focus_handler_.postDelayed(auto_focus_runnable, 1000);
        }
    };

    public class TerminalBluetoothRunnable extends BluetoothRunnable {
        private boolean running_ = true;

        private String mac_address_;


        public TerminalBluetoothRunnable(String mac_address, Handler handler) {
            super(handler);

            BluetoothDevice bluetooth_device = bluetooth_adapter_.getRemoteDevice(mac_address);
            try {
                bluetooth_socket_ = bluetooth_device.createInsecureRfcommSocketToServiceRecord(BusPhone.Constants.VALIDATE_CHANNEL_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mac_address_ = mac_address;

        }

        @Override
        public void run() {
            Log.d(TAG, "BluetoothRunnable started running");
            String message = "BUSTO!";

            try {
                byte[] buffer = message.getBytes("UTF-8");

                if (bluetooth_socket_ != null) {
                    bluetooth_socket_.connect();

                    if (bluetooth_socket_.isConnected()) {
                        send(message);
                        handler_.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DecodeTicketActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                            }
                        });

                        final String response = receive();

                        handler_.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DecodeTicketActivity.this, response, Toast.LENGTH_SHORT).show();
                            }
                        });

                        bluetooth_socket_.close();

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.decode_ticket, menu);
        return true;
    }

}

