package edu.feup.busphone.inspector.ui;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.feup.busphone.client.Ticket;
import edu.feup.busphone.inspector.R;
import edu.feup.busphone.inspector.util.network.InspectorNetworkUtilities;
import edu.feup.busphone.ui.ProgressDialogFragment;
import edu.feup.busphone.util.network.WebServiceCallRunnable;

public class InspectionActivity extends CameraActivity implements ValidationResultDialogFragment.ValidationResultDialogFragmentListener {

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

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(camera_preview_);

        capture_mode_text_ = (TextView) findViewById(R.id.capture_mode_text);

        setCaptureMode(BUS_ID_READING_MODE);

        progress_dialog_fragment = ProgressDialogFragment.newInstance("Loading tickets", false);

        pattern_ = Pattern.compile(UUID_PATTERN);
    }

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

    @Override
    protected void onRead(String data) {
        super.onRead(data);

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

    private void validateTicket(String ticket_id) {
        boolean valid = false;
        Ticket ticket = getTicket(ticket_id);
        if (ticket != null) {
            int type = ticket.getType();
            String timestamp = ticket.getTimestamp();

            valid = true;
            
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
