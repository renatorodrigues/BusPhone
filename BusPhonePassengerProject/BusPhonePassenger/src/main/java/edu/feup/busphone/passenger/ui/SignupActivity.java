package edu.feup.busphone.passenger.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import edu.feup.busphone.passenger.R;
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

    private class SignupRunnable extends WebServiceCallRunnable {
        SignupRunnable(Handler handler) {
            super(handler);
        }

        @Override
        public void run() {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("identifier", "bus1"));
            params.add(new BasicNameValuePair("password", "bus1"));
            JSONObject response = NetworkUtilities.post("http://172.30.2.49/loginBus", params);
            try {
                Log.d(TAG, response.toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }


            handler_.post(new Runnable() {
                @Override
                public void run() {

                    //Toast.makeText(getBaseContext(), "Olá Renato!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void signup(View v) {
        final String name = name_edit_.getText().toString();
        final String username = username_edit_.getText().toString();
        final String password = password_edit_.getText().toString();

        final String card_number = card_number_edit.getText().toString();
        final String expiry_year = expiry_year_spinner_.getSelectedItem().toString();
        final String expiry_month = expiry_month_spinner_.getSelectedItem().toString();
        final String cv2 = cv2_edit_.getText().toString();

        Thread signup_thread = new Thread(new SignupRunnable(getWindow().getDecorView().getHandler()));
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
