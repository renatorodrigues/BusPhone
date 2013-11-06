package edu.feup.busphone.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.feup.busphone.library.R;

public class ProgressDialogFragment extends DialogFragment {
    private static final String KEY_MESSAGE = "message";

    private static final int STYLE = DialogFragment.STYLE_NO_TITLE;
    private static final int THEME = android.R.style.Theme_Holo_Light_Dialog;

    private ProgressBar progress_bar_;
    private TextView message_text_;

    private String message_;

    public static ProgressDialogFragment newInstance(String message, boolean cancelable) {
        ProgressDialogFragment progress_fragment = new ProgressDialogFragment();

        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        progress_fragment.setArguments(args);

        progress_fragment.setCancelable(cancelable);

        return progress_fragment;
    }

    @Override
    public void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);

        Bundle args = getArguments();
        message_ = args.getString(KEY_MESSAGE);

        setStyle(STYLE, THEME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved_instance_state) {
        View v = inflater.inflate(R.layout.progress_dialog_fragment, container, false);

        progress_bar_ = (ProgressBar) v.findViewById(R.id.progress_bar);
        message_text_ = (TextView) v.findViewById(R.id.message_text);

        if (message_ == null || "".equals(message_text_)) {
            message_text_.setVisibility(View.GONE);
        } else {
            message_text_.setText(message_);
        }

        return v;
    }
}
