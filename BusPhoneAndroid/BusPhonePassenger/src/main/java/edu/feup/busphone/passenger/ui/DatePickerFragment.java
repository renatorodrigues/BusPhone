package edu.feup.busphone.passenger.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class DatePickerFragment extends DialogFragment {
    private static final String TAG = "DatePickerFragment";

    public static final String ARG_YEAR = "year";
    public static final String ARG_MONTH = "month";
    public static final String ARG_DAY = "day";

    private DatePickerDialog.OnDateSetListener listener_;

    public static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener listener, int year, int month, int day) {
        final DatePickerFragment date_picker = new DatePickerFragment();
        date_picker.setListener(listener);

        final Bundle arguments = new Bundle();
        arguments.putInt(ARG_YEAR, year);
        arguments.putInt(ARG_MONTH, month);
        arguments.putInt(ARG_DAY, day);
        date_picker.setArguments(arguments);

        return date_picker;
    }

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        listener_ = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle saved_instance_state) {
        final Bundle arguments = getArguments();
        final int year = arguments.getInt(ARG_YEAR);
        final int month = arguments.getInt(ARG_MONTH);
        final int day = arguments.getInt(ARG_DAY);

        return new DatePickerDialog(getActivity(), listener_, year, month, day);
    }
}