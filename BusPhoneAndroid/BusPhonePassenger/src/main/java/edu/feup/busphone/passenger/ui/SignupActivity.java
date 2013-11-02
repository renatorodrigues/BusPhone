package edu.feup.busphone.passenger.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.client.Passenger;
import edu.feup.busphone.passenger.util.FormTextWatcher;
import edu.feup.busphone.passenger.util.NetworkUtilities;
import edu.feup.busphone.passenger.util.PasswordFontfaceWatcher;
import edu.feup.busphone.passenger.util.WebServiceCallRunnable;

public class SignupActivity extends Activity implements FormTextWatcher.FormListener {
    private static final String TAG = "SignupActivity";

    private EditText name_edit_;
    private EditText username_edit_;
    private EditText password_edit_;

    private EditText card_number_edit;
    private Spinner expiry_year_spinner_;
    private Spinner expiry_month_spinner_;
    private EditText cv2_edit_;

    private TextView[] required_fields_;

    private Button signup_button_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.signup_activity);

        name_edit_ = (EditText) findViewById(R.id.name_edit);
        username_edit_ = (EditText) findViewById(R.id.username_edit);
        password_edit_ = (EditText) findViewById(R.id.password_edit);

        card_number_edit = (EditText) findViewById(R.id.card_number_edit);
        expiry_year_spinner_ = (Spinner) findViewById(R.id.expiry_year_spinner);
        expiry_month_spinner_ = (Spinner) findViewById(R.id.expiry_month_spinner);
        cv2_edit_ = (EditText) findViewById(R.id.cv2_edit);

        signup_button_ = (Button) findViewById(R.id.signup_button);

        required_fields_ = new TextView[] {name_edit_, username_edit_, password_edit_, card_number_edit, cv2_edit_};

        AdapterView.OnItemSelectedListener expiry_listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateSubmitButton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };

        Resources resources = getResources();

        ArrayAdapter<String> years_adapter = new ArrayAdapterHint<String>(this, android.R.layout.simple_spinner_item);
        Calendar calendar = Calendar.getInstance();
        int current_year = calendar.get(Calendar.YEAR);
        String year;
        for (int i = 0; i < 13; ++i) {
            year = "" + (current_year + i);
            years_adapter.add(year);
        }
        years_adapter.add(resources.getString(R.string.year)); // hint
        years_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expiry_year_spinner_.setAdapter(years_adapter);
        expiry_year_spinner_.setSelection(years_adapter.getCount());
        expiry_year_spinner_.setOnItemSelectedListener(expiry_listener);

        ArrayAdapter<String> months_adapter = new ArrayAdapterHint<String>(this, android.R.layout.simple_spinner_item);
        months_adapter.addAll(resources.getStringArray(R.array.months));
        months_adapter.add(resources.getString(R.string.month)); // hint
        months_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expiry_month_spinner_.setAdapter(months_adapter);
        expiry_month_spinner_.setSelection(months_adapter.getCount());
        expiry_month_spinner_.setOnItemSelectedListener(expiry_listener);

        PasswordFontfaceWatcher.register(password_edit_);
        FormTextWatcher.register(required_fields_, this);
    }

    public void signup(View v) {
        final String name = name_edit_.getText().toString();
        final String username = username_edit_.getText().toString();
        final String password = password_edit_.getText().toString();

        final String card_number = card_number_edit.getText().toString();
        final String expiry_year = expiry_year_spinner_.getSelectedItem().toString();
        final String expiry_month = expiry_month_spinner_.getSelectedItem().toString();
        final String cv2 = cv2_edit_.getText().toString();

        Thread signup_thread = new Thread(new WebServiceCallRunnable(getWindow().getDecorView().getHandler()) {
            @Override
            public void run() {
                final String registration_response = NetworkUtilities.userRegister(name, username, password, card_number);
                final boolean registration_success = "OK".equals(registration_response);
                final HashMap<String, String> login_response = registration_success == true ? NetworkUtilities.userLogin(username, password) : null;

                handler_.post(new Runnable() {
                    @Override
                    public void run() {
                        if (registration_success) {
                            String token = null;
                            if (login_response.containsKey("token")) {
                                token = login_response.get("token");
                            }
                            Passenger.getInstance().authenticateUser(token);

                            Class<?> cls = token != null ? ViewTicketsActivity.class : LoginActivity.class;
                            Intent intent = new Intent(SignupActivity.this, cls);
                            startActivity(intent);
                            finish();
                        } else {
                            int message_id;
                            if ("already in use".equals(registration_response)) {
                                message_id = R.string.username_already_in_use;
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

        if (enabled && (expiry_year_spinner_.getSelectedItemPosition() == expiry_year_spinner_.getCount()
                || expiry_month_spinner_.getSelectedItemPosition() == expiry_month_spinner_.getCount())) {
            enabled = false;
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
                Intent intent = new Intent(this, LoginActivity.class);
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
        name_edit_.setText("Renato Rodrigues");
        username_edit_.setText("renatorodrigues");
        password_edit_.setText("qwerty");

        card_number_edit.setText("4186360089268307");
        expiry_year_spinner_.setSelection(0);
        expiry_month_spinner_.setSelection(11);
        cv2_edit_.setText("245");
    }

    private static class ArrayAdapterHint<String> extends ArrayAdapter<String> {

        public ArrayAdapterHint(Context context, int text_view_resource_id) {
            super(context, text_view_resource_id);
        }

        @Override
        public View getView(int position, View convert_view, ViewGroup parent) {
            View v = super.getView(position, convert_view, parent);
            if (position == getCount()) {
                TextView text_view = (TextView) v.findViewById(android.R.id.text1);
                text_view.setText("");
                text_view.setHint((CharSequence) getItem(getCount()));
            }

            return v;
        }

        @Override
        public int getCount() {
            return super.getCount() - 1;
        }
    }
}
