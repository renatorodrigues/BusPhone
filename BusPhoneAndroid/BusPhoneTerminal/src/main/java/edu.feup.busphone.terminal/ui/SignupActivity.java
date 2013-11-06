package edu.feup.busphone.terminal.ui;

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

import edu.feup.busphone.terminal.client.Bus;
import edu.feup.busphone.terminal.util.network.TerminalNetworkUtilities;
import edu.feup.busphone.ui.ProgressDialogFragment;
import edu.feup.busphone.util.network.WebServiceCallRunnable;
import edu.feup.busphone.util.text.FormTextWatcher;
import edu.feup.busphone.util.text.PasswordFontfaceWatcher;

import edu.feup.busphone.terminal.R;

public class SignupActivity extends Activity implements FormTextWatcher.FormListener {
    private static final String TAG = "SignupActivity";

    private EditText bus_id_edit_;
    private EditText password_edit_;

    private TextView[] required_fields_;

    private Button signup_button_;

    private ProgressDialogFragment progress_dialog_fragment_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.signup_activity);

        bus_id_edit_ = (EditText) findViewById(R.id.bus_id_edit);
        password_edit_ = (EditText) findViewById(R.id.password_edit);

        signup_button_ = (Button) findViewById(R.id.signup_button);

        required_fields_ = new TextView[] {bus_id_edit_, password_edit_};

        PasswordFontfaceWatcher.register(password_edit_);
        FormTextWatcher.register(required_fields_, this);

        progress_dialog_fragment_ = ProgressDialogFragment.newInstance(null, false);
    }


    public void signup(View v) {
        final String bus_id = bus_id_edit_.getText().toString();
        final String password = password_edit_.getText().toString();

        progress_dialog_fragment_.show(getFragmentManager(), "signup_progress");

        Thread signup_thread = new Thread(new WebServiceCallRunnable(getWindow().getDecorView().getHandler()) {
            @Override
            public void run() {
                final String registration_response = TerminalNetworkUtilities.register(bus_id, password);
                final boolean registration_success = "OK".equals(registration_response);
                final HashMap<String, String> login_response = registration_success ? TerminalNetworkUtilities.login(bus_id, password) : null;

                handler_.post(new Runnable() {
                    @Override
                    public void run() {
                        progress_dialog_fragment_.dismiss();

                        if (registration_success) {
                            String token = null;
                            if (login_response.containsKey("token")) {
                                token = login_response.get("token");
                            }
                            Bus.getInstance().authenticate(token, bus_id);

                            Class<?> cls = token != null ? DecodeTicketActivity.class : LoginActivity.class;
                            startActivity(new Intent(SignupActivity.this, cls));
                            finish();
                        } else {
                            int message_id;
                            if ("already in use".equals(registration_response)) {
                                message_id = R.string.bus_id_already_in_use;
                            } else {
                                message_id = R.string.unknown_error;
                            }

                            Toast.makeText(SignupActivity.this, message_id, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        signup_thread.start();
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

        signup_button_.setEnabled(enabled);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
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
