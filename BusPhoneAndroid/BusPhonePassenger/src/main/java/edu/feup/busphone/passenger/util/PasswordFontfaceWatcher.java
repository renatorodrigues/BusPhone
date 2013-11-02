package edu.feup.busphone.passenger.util;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

/**
 * This class watches the text input in a password field in order to toggle the field's font so that the hint text
 * appears in a normal font and the password appears as monospace.
 *
 * <p />
 * Works around an issue with the Hint typeface.
 *
 * @author jhansche
 * @see <a
 * href="http://stackoverflow.com/questions/3406534/password-hint-font-in-android">http://stackoverflow.com/questions/3406534/password-hint-font-in-android</a>
 */
public class PasswordFontfaceWatcher implements TextWatcher {
    private static final int TEXT_VARIATION_PASSWORD =
            (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
    private TextView view_;

    /**
     * Register a new watcher for this {@code TextView} to alter the fontface based on the field's contents.
     *
     * <p />
     * This is only necessary for a textPassword field that has a non-empty hint text. A view not meeting these
     * conditions will incur no side effects.
     *
     * @param view
     */
    public static void register(TextView view) {
        final CharSequence hint = view.getHint();
        final int input_type = view.getInputType();
        final boolean is_password = ((input_type & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION))
                == TEXT_VARIATION_PASSWORD);

        if (is_password && hint != null && !"".equals(hint)) {
            PasswordFontfaceWatcher obj = new PasswordFontfaceWatcher(view);
            view.addTextChangedListener(obj);

            if (view.length() > 0) {
                obj.setMonospaceFont();
            } else {
                obj.setDefaultFont();
            }
        }
    }

    public PasswordFontfaceWatcher(TextView view) {
        view_ = view;
    }

    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

    }

    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
        if (s.length() == 0 && after > 0) {
            // Input field went from empty to non-empty
            setMonospaceFont();
        }
    }

    public void afterTextChanged(final Editable s) {
        if (s.length() == 0) {
            // Input field went from non-empty to empty
            setDefaultFont();
        }
    }

    public void setDefaultFont() {
        view_.setTypeface(Typeface.DEFAULT);
    }

    public void setMonospaceFont() {
        view_.setTypeface(Typeface.MONOSPACE);
    }
}
