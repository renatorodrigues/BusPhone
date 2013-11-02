package edu.feup.busphone.passenger.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class FormTextWatcher implements TextWatcher {
    private static final String TAG = "FormTextWatcher";

    public interface FormListener {
        void updateSubmitButton();
    }

    private TextView view_;
    private FormListener listener_;

    public static void register(TextView view, FormListener listener) {
        FormTextWatcher obj = new FormTextWatcher(view, listener);
        view.addTextChangedListener(obj);
    }

    public static void register(TextView[] views, FormListener listener) {
        for (TextView view : views) {
            FormTextWatcher obj = new FormTextWatcher(view, listener);
            view.addTextChangedListener(obj);
        }
    }

    private FormTextWatcher(TextView view, FormListener listener) {
        view_ = view;
        listener_ = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        listener_.updateSubmitButton();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
