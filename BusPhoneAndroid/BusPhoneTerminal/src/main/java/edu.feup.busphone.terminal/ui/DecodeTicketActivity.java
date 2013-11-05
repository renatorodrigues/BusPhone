package edu.feup.busphone.terminal.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.io.IOException;

import edu.feup.busphone.BusPhone;
import edu.feup.busphone.terminal.R;
import edu.feup.busphone.terminal.client.Bus;
import edu.feup.busphone.terminal.util.CameraPreview;
import edu.feup.busphone.terminal.util.network.TerminalNetworkUtilities;
import edu.feup.busphone.util.bluetooth.BluetoothRunnable;

public class DecodeTicketActivity extends Activity {
    private static final String TAG = "DecodeTicketActivity";

    private Camera camera_;
    private CameraPreview camera_preview_;
    private Handler auto_focus_handler_;


    private ImageScanner scanner_;

    private boolean previewing_ = true;

    private BluetoothAdapter bluetooth_adapter_;
    private Handler bluetooth_handler_;
    private Thread bluetooth_thread_;

    private LinearLayout status_linear_layout_;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decode_ticket_activity);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        auto_focus_handler_ = new Handler();
        camera_ = Camera.open();

        scanner_ = new ImageScanner();
        scanner_.setConfig(0, Config.X_DENSITY, 3);
        scanner_.setConfig(0, Config.Y_DENSITY, 3);

        camera_preview_ = new CameraPreview(this, camera_, preview_callback_, auto_focus_callback_);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(camera_preview_);

        status_linear_layout_ = (LinearLayout) findViewById(R.id.status_linear_layout);

        bluetooth_adapter_ = BluetoothAdapter.getDefaultAdapter();
        bluetooth_handler_ = new Handler();
    }

    public void addStatus(String message) {
        TextView status_text = new TextView(DecodeTicketActivity.this);
        status_text.setText(message);
        status_linear_layout_.addView(status_text);
    }

    public void clearStatus() {
        status_linear_layout_.removeAllViews();
    }

    private void setPreviewEnabled(boolean enabled) {
        previewing_ = enabled;

        if (enabled) {
            camera_.setPreviewCallback(preview_callback_);
            camera_.startPreview();
            camera_.autoFocus(auto_focus_callback_);
        } else {
            camera_.setPreviewCallback(null);
            camera_.stopPreview();
        }
    }

    private void releaseCamera() {
        if (camera_ != null) {
            previewing_ = false;
            camera_.setPreviewCallback(null);
            camera_.release();
            camera_ = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private Runnable auto_focus_runnable_ = new Runnable() {
        @Override
        public void run() {
            if (previewing_) {
                camera_.autoFocus(auto_focus_callback_);
            }
        }
    };

    private Camera.PreviewCallback preview_callback_ = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner_.scanImage(barcode);

            if (result != 0) {
                setPreviewEnabled(false);

                SymbolSet symbols = scanner_.getResults();
                for (Symbol symbol : symbols) {
                    String scan_text = symbol.getData();
                    clearStatus();
                    if (BluetoothAdapter.checkBluetoothAddress(scan_text)) {
                        addStatus("MAC address: " + scan_text);
                        bluetooth_thread_ = new Thread(new TerminalBluetoothRunnable(scan_text, bluetooth_handler_));
                        bluetooth_thread_.start();
                    } else {
                        addStatus("Invalid input");
                        setPreviewEnabled(true);
                    }
                }
            }
        }
    };

    private Camera.AutoFocusCallback auto_focus_callback_ = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            auto_focus_handler_.postDelayed(auto_focus_runnable_, 1000);
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

            try {
                if (bluetooth_socket_ != null) {
                    bluetooth_socket_.connect();

                    if (bluetooth_socket_.isConnected()) {
                        final String ticket_id = receive();

                        handler_.post(new Runnable() {
                            @Override
                            public void run() {
                                addStatus("Validating ticket.");
                            }
                        });

                        final String result = TerminalNetworkUtilities.validate(Bus.getInstance().getAuthToken(), ticket_id);
                        if ("OK".equals(result)) {
                            send("ACK");
                        } else {
                            send("NACK");
                        }

                        handler_.post(new Runnable() {
                            @Override
                            public void run() {
                                String status = "OK".equals(result) ? "Successfully validated!" : "Error!";
                                addStatus(status);
                            }
                        });

                        bluetooth_socket_.close();
                        setPreviewEnabled(true);
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

