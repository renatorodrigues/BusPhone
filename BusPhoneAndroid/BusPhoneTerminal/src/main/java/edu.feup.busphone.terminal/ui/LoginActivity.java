package edu.feup.busphone.terminal.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import edu.feup.busphone.terminal.R;
import edu.feup.busphone.terminal.client.Bus;
import edu.feup.busphone.terminal.util.network.TerminalNetworkUtilities;
import edu.feup.busphone.ui.ProgressDialogFragment;
import edu.feup.busphone.util.network.WebServiceCallRunnable;
import edu.feup.busphone.util.text.FormTextWatcher;
import edu.feup.busphone.util.text.PasswordFontfaceWatcher;

public class LoginActivity extends Activity implements FormTextWatcher.FormListener {
    private static final String TAG = "LoginActivity";

    private EditText bus_id_edit_;
    private EditText password_edit_;

    private TextView[] required_fields_;

    private Button login_button_;

    private ProgressDialogFragment progress_dialog_fragment_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.login_activity);

        bus_id_edit_ = (EditText) findViewById(R.id.bus_id_edit);
        password_edit_ = (EditText) findViewById(R.id.password_edit);

        required_fields_ = new TextView[]{bus_id_edit_, password_edit_};

        login_button_ = (Button) findViewById(R.id.login_button);

        PasswordFontfaceWatcher.register(password_edit_);
        FormTextWatcher.register(required_fields_, this);

        progress_dialog_fragment_ = ProgressDialogFragment.newInstance(null, false);
    }

    public void login(View v) {
        final String bus_id = bus_id_edit_.getText().toString();
        final String password = password_edit_.getText().toString();

        progress_dialog_fragment_.show(getFragmentManager(), "login_progress");

        Thread login_thread = new Thread(new WebServiceCallRunnable(getWindow().getDecorView().getHandler()) {
            @Override
            public void run() {
                final HashMap<String, String> response = TerminalNetworkUtilities.login(bus_id, password);

                handler_.post(new Runnable() {
                    @Override
                    public void run() {
                        progress_dialog_fragment_.dismiss();

                        if (response.containsKey("token")) {
                            Log.d(TAG, "BUS ID: " + bus_id);
                            Log.d(TAG, "BUS TOKEN: " + response.get("token"));
                            Bus.getInstance().authenticate(response.get("token"), bus_id);

                            startActivity(new Intent(LoginActivity.this, DecodeTicketActivity.class));
                            finish();
                        } else {
                            int message_id;
                            if ("bad username/password".equals(response.get("info"))) {
                                message_id = R.string.login_unsuccessful;
                            } else {
                                message_id = R.string.unknown_error;
                            }

                            Toast.makeText(LoginActivity.this, message_id, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        login_thread.start();
    }

    @Override
    public void updateSubmitButton() {
        boolean enabled = true;

        for (TextView field : required_fields_) {
            if (field.getText().length() == 0) {
                enabled = false;
                break;
            }
        }

        login_button_.setEnabled(enabled);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signup:
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                finish();
                break;
            case R.id.action_fill:
                fillForm();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void fillForm() {
        bus_id_edit_.setText("fa6fcfc0-43ff-11e3-8f96-0800200c9a66");
        password_edit_.setText("qwerty");
    }
    
}
