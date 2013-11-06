package edu.feup.busphone.inspector.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import edu.feup.busphone.inspector.R;

public class ValidationResultDialogFragment extends DialogFragment {
    public interface ValidationResultDialogFragmentListener {
        public void onValidationResultDialogFragmentDismiss();
    }

    private static final String EXTRA_VALIDATION_RESULT = "result";

    private static final int STYLE = DialogFragment.STYLE_NO_TITLE;
    private static final int THEME = android.R.style.Theme_Holo_Light_Dialog;

    private boolean valid_;

    private ImageView result_icon_image_;
    private TextView result_message_text_;

    private ValidationResultDialogFragmentListener listener_;

    public static ValidationResultDialogFragment newInstance(boolean valid) {
        ValidationResultDialogFragment validation_fragment = new ValidationResultDialogFragment();

        Bundle args = new Bundle();
        args.putBoolean(EXTRA_VALIDATION_RESULT, valid);
        validation_fragment.setArguments(args);

        return validation_fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener_ = (ValidationResultDialogFragmentListener) activity;
    }

    @Override
    public void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);

        Bundle args = getArguments();
        valid_ = args.getBoolean(EXTRA_VALIDATION_RESULT);

        setStyle(STYLE, THEME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved_instance_state) {
        View v = inflater.inflate(R.layout.validation_result_dialog_fragment, container, false);

        result_icon_image_ = (ImageView) v.findViewById(R.id.result_icon_image);
        result_message_text_ = (TextView) v.findViewById(R.id.result_message_text);

        String message = valid_ ? "Valid" : "Invalid";

        int color = getResources().getColor(valid_ ? android.R.color.holo_green_dark : android.R.color.holo_red_dark);
        v.setBackgroundColor(color);

        result_message_text_.setText(message);

        return v;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        listener_.onValidationResultDialogFragmentDismiss();
    }
}