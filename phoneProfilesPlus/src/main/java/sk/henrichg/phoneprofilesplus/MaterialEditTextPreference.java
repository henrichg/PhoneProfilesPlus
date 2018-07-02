package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;

class MaterialEditTextPreference extends com.afollestad.materialdialogs.prefs.MaterialEditTextPreference {

    @SuppressWarnings("unused")
    public MaterialEditTextPreference(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public MaterialEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public MaterialEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public MaterialEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        MaterialDialog dialog = (MaterialDialog)getDialog();
        if (dialog != null) {
            MDButton positive = dialog.getActionButton(DialogAction.POSITIVE);
            if (positive != null) positive.setAllCaps(false);
            MDButton negative = dialog.getActionButton(DialogAction.NEGATIVE);
            if (negative != null) negative.setAllCaps(false);
        }
    }

}
