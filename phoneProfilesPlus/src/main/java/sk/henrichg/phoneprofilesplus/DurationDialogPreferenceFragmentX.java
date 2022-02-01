package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

public class DurationDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat
                                implements SeekBar.OnSeekBarChangeListener {

    private TextView mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private SeekBar mSeekBarSeconds;
    private TimeDurationPickerDialog mValueDialog;

    private Context context;
    private DurationDialogPreferenceX preference;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        this.context = context;
        preference = (DurationDialogPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_duration_preference, null, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(@NonNull View view)
    {
        super.onBindDialogView(view);

        TextView mTextViewRange = view.findViewById(R.id.duration_pref_dlg_range);

        mValue = view.findViewById(R.id.duration_pref_dlg_value);
        TooltipCompat.setTooltipText(mValue, getString(R.string.duration_pref_dlg_edit_duration_tooltip));

        mSeekBarHours = view.findViewById(R.id.duration_pref_dlg_hours);
        mSeekBarMinutes = view.findViewById(R.id.duration_pref_dlg_minutes);
        mSeekBarSeconds = view.findViewById(R.id.duration_pref_dlg_seconds);

        //mSeekBarHours.setRotation(180);
        //mSeekBarMinutes.setRotation(180);
        //mSeekBarSeconds.setRotation(180);

        // Initialize state
        int hours;
        int minutes;
        int seconds;
        hours = preference.mMax / 3600;
        minutes = (preference.mMax % 3600) / 60;
        seconds = preference.mMax % 60;
        final String sMax = GlobalGUIRoutines.getDurationString(preference.mMax);
        mSeekBarHours.setMax(hours);
        if (hours == 0)
            mSeekBarMinutes.setMax(minutes);
        else
            mSeekBarMinutes.setMax(59);
        if ((hours == 0) && (minutes == 0))
            mSeekBarSeconds.setMax(seconds);
        else
            mSeekBarSeconds.setMax(59);
        final String sMin = GlobalGUIRoutines.getDurationString(preference.mMin);
        int iValue = Integer.parseInt(preference.value);
        hours = iValue / 3600;
        minutes = (iValue % 3600) / 60;
        seconds = iValue % 60;
        mSeekBarHours.setProgress(hours);
        mSeekBarMinutes.setProgress(minutes);
        mSeekBarSeconds.setProgress(seconds);

        mValue.setText(GlobalGUIRoutines.getDurationString(iValue));

        mValueDialog = new TimeDurationPickerDialog(context, (view1, duration) -> {
            int iValue1 = (int) duration / 1000;

            if (iValue1 < preference.mMin)
                iValue1 = preference.mMin;
            if (iValue1 > preference.mMax)
                iValue1 = preference.mMax;

            preference.value = String.valueOf(iValue1);

            mValue.setText(GlobalGUIRoutines.getDurationString(iValue1));

            int hours1 = iValue1 / 3600;
            int minutes1 = (iValue1 % 3600) / 60;
            int seconds1 = iValue1 % 60;

            mSeekBarHours.setProgress(hours1);
            mSeekBarMinutes.setProgress(minutes1);
            mSeekBarSeconds.setProgress(seconds1);
        }, iValue * 1000L, TimeDurationPicker.HH_MM_SS);
        GlobalGUIRoutines.setThemeTimeDurationPickerDisplay(mValueDialog.getDurationInput(), getActivity());
        mValue.setOnClickListener(view12 -> {
                int hours12 = mSeekBarHours.getProgress();
                int minutes12 = mSeekBarMinutes.getProgress();
                int seconds12 = mSeekBarSeconds.getProgress();

                int iValue12 = (hours12 * 3600 + minutes12 * 60 + seconds12);
                if (iValue12 < preference.mMin) iValue12 = preference.mMin;
                if (iValue12 > preference.mMax) iValue12 = preference.mMax;

                mValueDialog.setDuration(iValue12 * 1000L);
                if (getActivity() != null)
                    if (!getActivity().isFinishing())
                        mValueDialog.show();
            }
        );

        mSeekBarHours.setOnSeekBarChangeListener(this);
        mSeekBarMinutes.setOnSeekBarChangeListener(this);
        mSeekBarSeconds.setOnSeekBarChangeListener(this);

        mTextViewRange.setText(sMin + " - " + sMax);

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int hours = mSeekBarHours.getProgress();
            int minutes = mSeekBarMinutes.getProgress();
            int seconds = mSeekBarSeconds.getProgress();

            int iValue = (hours * 3600 + minutes * 60 + seconds);
            if (iValue < preference.mMin) iValue = preference.mMin;
            if (iValue > preference.mMax) iValue = preference.mMax;

            preference.value = String.valueOf(iValue);

            if (preference.callChangeListener(preference.value)) {
                preference.persistValue(preference.value);
                preference.setSummaryDDP();
            }
        }
        else {
            preference.resetSummary();
        }

        preference.fragment = null;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int hours = mSeekBarHours.getProgress();
            int minutes = mSeekBarMinutes.getProgress();
            int seconds = mSeekBarSeconds.getProgress();

            int iValue = (hours * 3600 + minutes * 60 + seconds);
            if (iValue < preference.mMin) iValue = preference.mMin;
            if (iValue > preference.mMax) iValue = preference.mMax;

            preference.value = String.valueOf(iValue);

            mValue.setText(GlobalGUIRoutines.getDurationString(iValue));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
