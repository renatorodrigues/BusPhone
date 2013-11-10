package edu.feup.busphone.inspector.ui;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.feup.busphone.BusPhone;
import edu.feup.busphone.client.Ticket;
import edu.feup.busphone.hardware.camera.CameraPreview;
import edu.feup.busphone.inspector.R;
import edu.feup.busphone.inspector.util.network.InspectorNetworkUtilities;
import edu.feup.busphone.ui.ProgressDialogFragment;
import edu.feup.busphone.util.network.WebServiceCallRunnable;

public class InspectionActivity extends Activity implements ValidationResultDialogFragment.ValidationResultDialogFragmentListener {
    private static final String TAG = "InspectionActivity";

    private Camera camera_;
    private CameraPreview camera_preview_;
    private Handler auto_focus_handler_;

    private ImageScanner scanner_;

    private boolean previewing_;

    static {
        System.loadLibrary("iconv");
    }

    private FrameLayout preview_layout_;

    private static final int BUS_ID_READING_MODE = 1;
    private static final int TICKET_VALIDATION_MODE = 2;

    private int capture_mode_;

    private String bus_id_ = null;
    private ArrayList<Ticket> tickets_ = null;

    private TextView capture_mode_text_;

    private ProgressDialogFragment progress_dialog_fragment;

    private Pattern pattern_;
    private Matcher matcher_;

    private static final String UUID_PATTERN = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.inspection_activity);

        preview_layout_ = (FrameLayout) findViewById(R.id.camera_preview);

        capture_mode_text_ = (TextView) findViewById(R.id.capture_mode_text);

        setCaptureMode(BUS_ID_READING_MODE);

        progress_dialog_fragment = ProgressDialogFragment.newInstance("Loading tickets", false);

        pattern_ = Pattern.compile(UUID_PATTERN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (camera_ == null) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                auto_focus_handler_ = new Handler();
                camera_ = getCameraInstance();
                previewing_ = true;
                scanner_ = new ImageScanner();
                scanner_.setConfig(0, Config.X_DENSITY, 3);
                scanner_.setConfig(0, Config.Y_DENSITY, 3);

                camera_preview_ = new CameraPreview(this, camera_, preview_callback_, auto_focus_callback_);
                preview_layout_.addView(camera_preview_);
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        preview_layout_.removeView(camera_preview_);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }

    protected void releaseCamera() {
        if (camera_ != null) {
            previewing_ = false;
            camera_.setPreviewCallback(null);
            camera_.release();
            camera_ = null;
        }
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

    private void setCaptureMode(int mode) {
        capture_mode_ = mode;

        String mode_title;
        switch (capture_mode_) {
            case BUS_ID_READING_MODE:
                mode_title = "Scanning Bus ID";
                break;
            case TICKET_VALIDATION_MODE:
                mode_title = "Inspecting tickets";
                break;
            default:
                return;
        }

        capture_mode_text_.setText(mode_title);
    }

    private boolean isUUID(String data) {
        matcher_ = pattern_.matcher(data);
        return matcher_.matches();
    }

    private void onRead(String data) {

        if (!isUUID(data)) {
            Toast.makeText(InspectionActivity.this, "Bad scan", Toast.LENGTH_SHORT).show();
            setPreviewEnabled(true);
            return;
        }

        switch (capture_mode_) {
            case BUS_ID_READING_MODE:
                getValidatedTickets(data);
                break;
            case TICKET_VALIDATION_MODE:
                validateTicket(data);
                break;
            default:
        }
    }

    private void getValidatedTickets(final String bus_id) {
        progress_dialog_fragment.show(getFragmentManager(), "tickets_progress");

        Handler handler = getWindow().getDecorView().getHandler();
        Thread validated_tickets = new Thread(new WebServiceCallRunnable(handler) {
            @Override
            public void run() {
                final ArrayList<Ticket> tickets = InspectorNetworkUtilities.inspect(bus_id);

                handler_.post(new Runnable() {
                    @Override
                    public void run() {
                        progress_dialog_fragment.dismiss();

                        if (tickets == null) {
                            Toast.makeText(InspectionActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                        } else {
                            bus_id_ = bus_id;
                            tickets_ = tickets;

                            Log.d(TAG, "tickets size: " + tickets.size());

                            setCaptureMode(TICKET_VALIDATION_MODE);
                            setPreviewEnabled(true);
                        }
                    }
                });
            }
        });
        validated_tickets.start();
    }

    private void popUpValidationResult(boolean valid) {
        ValidationResultDialogFragment.newInstance(valid).show(getFragmentManager(), "validation_result_fragment");
    }

    @Override
    public void onValidationResultDialogFragmentDismiss() {
        setPreviewEnabled(true);
    }

    public long timeLeft(Ticket ticket) {
        Calendar now = Calendar.getInstance();

        Timestamp timestamp = Timestamp.valueOf(ticket.getTimestamp());
        Calendar validation = Calendar.getInstance();
        validation.setTimeInMillis(timestamp.getTime());
        int ticket_duration = 0;
        switch (ticket.getType()) {
            case Ticket.T1:
                ticket_duration = BusPhone.Constants.T1_DURATION;
                break;
            case Ticket.T2:
                ticket_duration = BusPhone.Constants.T2_DURATION;
                break;
            case Ticket.T3:
                ticket_duration = BusPhone.Constants.T3_DURATION;
                break;
        }

        validation.add(Calendar.SECOND, ticket_duration);

        return validation.getTime().getTime() - now.getTime().getTime();
    }

    private void validateTicket(String ticket_id) {
        boolean valid = false;
        Ticket ticket = getTicket(ticket_id);
        if (ticket != null) {
            int type = ticket.getType();
            String timestamp = ticket.getTimestamp();

            valid = timeLeft(ticket) > 0;
        }

        popUpValidationResult(valid);
    }

    private Ticket getTicket(String id) {
        for (Ticket t : tickets_) {
            if (id.equals(t.getId())) {
                return t;
            }
        }

        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inspection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan_bus_id:
                setCaptureMode(BUS_ID_READING_MODE);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}
