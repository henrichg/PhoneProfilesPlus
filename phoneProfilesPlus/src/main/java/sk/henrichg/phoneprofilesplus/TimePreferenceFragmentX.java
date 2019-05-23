package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.PreferenceDialogFragmentCompat;

public class TimePreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private TimePreferenceX preference;

    private TimePicker picker;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (TimePreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_time_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        PPApplication.logE("TimePreferenceFragmentX.onBindDialogView", "preference.value="+preference.getSummary());

        picker = view.findViewById(R.id.time_pref_dlg_timePicker);
        picker.setIs24HourView(DateFormat.is24HourFormat(prefContext));

        picker.setCurrentHour(preference.value / 60);
        picker.setCurrentMinute(preference.value % 60);

        picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                preference.value = hourOfDay * 60 + minute;
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue(picker);
        }
        else {
            preference.resetSummary();
        }
        preference.fragment = null;
    }

}
