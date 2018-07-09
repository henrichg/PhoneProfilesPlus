package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.AttributeSet;

class MaterialMultiSelectListPreference extends com.afollestad.materialdialogs.prefs.MaterialMultiSelectListPreference {

    @SuppressWarnings("unused")
    public MaterialMultiSelectListPreference(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public MaterialMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public MaterialMultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public MaterialMultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /*
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
    */

}
