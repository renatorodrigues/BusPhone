package edu.feup.busphone.terminal.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.io.IOException;

import edu.feup.busphone.BusPhone;
import edu.feup.busphone.hardware.camera.CameraPreview;
import edu.feup.busphone.terminal.R;
import edu.feup.busphone.terminal.client.Bus;
import edu.feup.busphone.terminal.util.network.TerminalNetworkUtilities;
import edu.feup.busphone.ui.ProgressDialogFragment;
import edu.feup.busphone.hardware.bluetooth.BluetoothRunnable;

public class DecodeTicketActivity extends Activity {
    private static final String TAG = "DecodeTicketActivity";

    protected Camera camera_;
    protected CameraPreview camera_preview_;
    private Handler auto_focus_handler_;

    protected ImageScanner scanner_;

    protected boolean previewing_;

    static {
        System.loadLibrary("iconv");
    }

    private BluetoothAdapter bluetooth_adapter_;
    private Handler bluetooth_handler_;
    private Thread bluetooth_thread_;

    private LinearLayout status_linear_layout_;

    private ProgressDialogFragment progress_dialog_fragment_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.decode_ticket_activity);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        auto_focus_handler_ = new Handler();
        //boolean camera_opened = safeCameraOpen();
        camera_ = Camera.open();
        //Log.d(TAG, Boolean.toString(camera_opened));

        previewing_ = true;

        scanner_ = new ImageScanner();
        scanner_.setConfig(0, Config.X_DENSITY, 3);
        scanner_.setConfig(0, Config.Y_DENSITY, 3);

        camera_preview_ = new CameraPreview(this, camera_, preview_callback_, auto_focus_callback_);

        //TODO: start from here

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(camera_preview_);

        status_linear_layout_ = (LinearLayout) findViewById(R.id.status_linear_layout);

        bluetooth_adapter_ = BluetoothAdapter.getDefaultAdapter();
        bluetooth_handler_ = new Handler();

        addStatus("Scanning...");
    }

    private boolean safeCameraOpen() {
        boolean opened = false;

        try {
            releaseCameraAndPreview();
            camera_ = Camera.open();
            opened = camera_ != null;
        } catch (Exception e) {
            Log.e(TAG, "Failed to open Camera");
            e.printStackTrace();
        }

        return opened;
    }

    private void releaseCameraAndPreview() {
        previewing_ = false;
        if (camera_ != null) {
            camera_.setPreviewCallback(null);
            camera_.release();
        }

        camera_ = null;
    }

    protected void setPreviewEnabled(boolean enabled) {
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

    protected void releaseCamera() {
        if (camera_ != null) {
            previewing_ = false;
            camera_.setPreviewCallback(null);
            camera_.release();
            camera_ = null;
        }
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

                    onRead(scan_text);
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

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
    }

    public void addStatus(String message) {
        TextView status_text = new TextView(DecodeTicketActivity.this);
        status_text.setTextColor(getResources().getColor(android.R.color.white));
        status_text.setText(message);
        status_linear_layout_.addView(status_text);
    }

    public void clearStatus() {
        status_linear_layout_.removeAllViews();
    }

    protected void onRead(String data) {

        clearStatus();
        if (BluetoothAdapter.checkBluetoothAddress(data)) {
            addStatus("MAC address: " + data);
            bluetooth_thread_ = new Thread(new TerminalBluetoothRunnable(data, bluetooth_handler_));
            bluetooth_thread_.start();
        } else {
            addStatus("Scanning...");
            setPreviewEnabled(true);
        }
    }

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
                                progress_dialog_fragment_ = ProgressDialogFragment.newInstance("Validating ticket", false);
                                progress_dialog_fragment_.show(getFragmentManager(), "decode_progress_fragment");
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
                                progress_dialog_fragment_.dismiss();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bus_id:
                startActivity(new Intent(DecodeTicketActivity.this, IdentificationActivity.class));
                break;
            case R.id.action_logout:
                Bus.getInstance().removeCredentials();
                startActivity(new Intent(DecodeTicketActivity.this, LoginActivity.class));
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}

