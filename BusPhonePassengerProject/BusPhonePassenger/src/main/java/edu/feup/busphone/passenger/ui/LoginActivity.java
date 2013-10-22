package edu.feup.busphone.passenger.ui;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.util.FormTextWatcher;
import edu.feup.busphone.passenger.util.PasswordFontfaceWatcher;

public class LoginActivity extends Activity implements FormTextWatcher.FormListener {
    private static final String TAG = "LoginActivity";

    private EditText username_edit_;
    private EditText password_edit_;

    private TextView[] required_fields_;

    private Button login_button_;

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
    }

    public void login(View v) {
        final String username = username_edit_.getText().toString();
        final String password = password_edit_.getText().toString();
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
}
