package edu.feup.busphone.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.client.Passenger;
import edu.feup.busphone.passenger.util.network.PassengerNetworkUtilities;
import edu.feup.busphone.ui.ProgressDialogFragment;
import edu.feup.busphone.util.text.FormTextWatcher;
import edu.feup.busphone.util.text.PasswordFontfaceWatcher;
import edu.feup.busphone.util.network.WebServiceCallRunnable;

public class LoginActivity extends Activity implements FormTextWatcher.FormListener {
    private static final String TAG = "LoginActivity";

    private EditText username_edit_;
    private EditText password_edit_;

    private TextView[] required_fields_;

    private Button login_button_;

    private ProgressDialogFragment progress_dialog_fragment_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.login_activity);

        username_edit_ = (EditText) findViewById(R.id.username_edit);
        password_edit_ = (EditText) findViewById(R.id.password_edit);

        required_fields_ = new TextView[]{username_edit_, password_edit_};

        login_button_ = (Button) findViewById(R.id.login_button);

        PasswordFontfaceWatcher.register(password_edit_);
        FormTextWatcher.register(required_fields_, this);

        progress_dialog_fragment_ = ProgressDialogFragment.newInstance(null, false);
    }

    public void login(View v) {
        final String username = username_edit_.getText().toString();
        final String password = password_edit_.getText().toString();

        progress_dialog_fragment_.show(getFragmentManager(), "login_progress");

        Thread login_thread = new Thread(new WebServiceCallRunnable(getWindow().getDecorView().getHandler()) {
            @Override
            public void run() {
                final HashMap<String, String> response = PassengerNetworkUtilities.login(username, password);

                handler_.post(new Runnable() {
                    @Override
                    public void run() {
                        progress_dialog_fragment_.dismiss();
                        if (response.containsKey("token")) {
                            Passenger.getInstance().authenticateUser(response.get("token"));

                            startActivity(new Intent(LoginActivity.this, ViewTicketsActivity.class));
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
                Intent intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
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
        username_edit_.setText("renatorodrigues");
        password_edit_.setText("qwerty");
    }
}
