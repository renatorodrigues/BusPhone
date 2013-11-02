package edu.feup.busphone.terminal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    }


    public void signup(View v) {
        final String bus_id = bus_id_edit_.getText().toString();
        final String password = password_edit_.getText().toString();

        Thread signup_thread = new Thread(new WebServiceCallRunnable(getWindow().getDecorView().getHandler()) {
            @Override
            public void run() {

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
