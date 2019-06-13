package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;
import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

public class TimePickerPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private TimePickerPreferenceX preference;

    TimeDurationPickerDialog mDialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preference = (TimePickerPreferenceX) getPreference();
        preference.fragment = this;

        final long _duration = preference.value * 60 * 1000;
        mDialog = new TimeDurationPickerDialog(preference._context, new TimeDurationPickerDialog.OnDurationSetListener() {
            @Override
            public void onDurationSet(TimeDurationPicker view, long duration) {
                preference.persistValue(duration);

                /*int iValue = (int) duration / 1000;

                if (iValue < 0)
                    iValue = 0;
                if (iValue > (24 * 60 * 60)-1)
                    iValue = (24 * 60 * 60)-1;

                mDelayValue.setText(GlobalGUIRoutines.getDurationString(iValue));

                startApplicationDelay = iValue;*/
            }
        }, _duration, TimeDurationPicker.HH_MM);
        GlobalGUIRoutines.setThemeTimeDurationPickerDisplay(mDialog.getDurationInput(), (Activity)preference._context);

        return mDialog;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();

        preference.fragment = null;
    }
}
