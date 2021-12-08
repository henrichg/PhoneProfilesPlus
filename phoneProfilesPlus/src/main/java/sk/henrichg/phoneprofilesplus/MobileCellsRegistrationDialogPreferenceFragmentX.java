package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

public class MobileCellsRegistrationDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat
                                    implements SeekBar.OnSeekBarChangeListener {

    private Context prefContext;
    private MobileCellsRegistrationDialogPreferenceX preference;

    private AlertDialog mDialog;
    private TextView mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private SeekBar mSeekBarSeconds;
    private TextView mCellsName;
    private TextView mStatus;
    private TextView mRemainingTime;
    private TimeDurationPickerDialog mValueDialog;
    private Button startButton;
    private Button stopButton;
    private MobileCellNamesDialogX mMobileCellNamesDialog;


    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //PPApplication.logE("MobileCellsRegistrationDialogPreferenceFragmentX.onCreateDialog", "xxx");

        preference = (MobileCellsRegistrationDialogPreferenceX)getPreference();
        prefContext = preference.getContext();
        preference.fragment = this;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(prefContext);
        dialogBuilder.setTitle(preference.getTitle());
        dialogBuilder.setIcon(preference.getIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setPositiveButton(R.string.mobile_cells_registration_pref_dlg_start_button, (dialog, which) -> {
            if (Permissions.grantMobileCellsRegistrationDialogPermissions(prefContext)) {
                if (MobileCellsScanner.enabledAutoRegistration) {
                    if (!MobileCellsScanner.isEventAdded(preference.event_id))
                        MobileCellsScanner.addEvent(preference.event_id);
                    else
                        MobileCellsScanner.removeEvent(preference.event_id);
                }
                else {
                    if (!MobileCellsScanner.isEventAdded(preference.event_id))
                        MobileCellsScanner.addEvent(preference.event_id);
                    preference.startRegistration();
                }
            }
        });

        LayoutInflater inflater = ((Activity)prefContext).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.dialog_mobile_cells_registration_preference, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);

            preference.updateInterface(0, false);
        });

        TextView mTextViewRange = layout.findViewById(R.id.duration_pref_dlg_range);
        mValue = layout.findViewById(R.id.duration_pref_dlg_value);
        TooltipCompat.setTooltipText(mValue, getString(R.string.duration_pref_dlg_edit_duration_tooltip));
        mSeekBarHours = layout.findViewById(R.id.duration_pref_dlg_hours);
        mSeekBarMinutes = layout.findViewById(R.id.duration_pref_dlg_minutes);
        mSeekBarSeconds = layout.findViewById(R.id.duration_pref_dlg_seconds);
        mCellsName = layout.findViewById(R.id.mobile_cells_registration_cells_name);
        mStatus = layout.findViewById(R.id.mobile_cells_registration_status);
        mRemainingTime = layout.findViewById(R.id.mobile_cells_registration_remaining_time);

        mCellsName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = mCellsName.getText().toString();
                startButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                startButton.setEnabled(!value.isEmpty());
            }
        });

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

        mValueDialog = new TimeDurationPickerDialog(prefContext, (view, duration) -> {
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
        mValue.setOnClickListener(view -> {
            int hours12 = mSeekBarHours.getProgress();
            int minutes12 = mSeekBarMinutes.getProgress();
            int seconds12 = mSeekBarSeconds.getProgress();

            int iValue12 = (hours12 * 3600 + minutes12 * 60 + seconds12);
            if (iValue12 < preference.mMin) iValue12 = preference.mMin;
            if (iValue12 > preference.mMax) iValue12 = preference.mMax;

            preference.value = String.valueOf(iValue12);

            mValueDialog.setDuration(iValue12 * 1000L);
            if (preference.fragment.getActivity() != null)
                if (!preference.fragment.getActivity().isFinishing())
                    mValueDialog.show();
        }
        );

        mMobileCellNamesDialog = new MobileCellNamesDialogX((Activity)prefContext, preference, false);
        mCellsName.setOnClickListener(view -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing())
                    mMobileCellNamesDialog.show();
        }
        );

        mSeekBarHours.setOnSeekBarChangeListener(this);
        mSeekBarMinutes.setOnSeekBarChangeListener(this);
        mSeekBarSeconds.setOnSeekBarChangeListener(this);

        mTextViewRange.setText(sMin + " - " + sMax);

        startButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);

        stopButton = layout.findViewById(R.id.mobile_cells_registration_stop_button);
        stopButton.setOnClickListener(v -> {
            updateInterface(0, true);
            //PPApplication.phoneProfilesService.mobileCellsScanner.durationForAutoRegistration = 0;
            //PPApplication.phoneProfilesService.mobileCellsScanner.cellsNameForAutoRegistration = "";
            preference.setSummaryDDP(0);

            //MobileCellsRegistrationService.setMobileCellsAutoRegistrationRemainingDuration(prefContext, 0);
            //MobileCellsScanner.stopAutoRegistration(prefContext.getApplicationContext());

            Intent intent5 = new Intent(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_STOP_BUTTON);
            prefContext.sendBroadcast(intent5);
        });

        return mDialog;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();

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

    void updateInterface(long millisUntilFinished, boolean forceStop) {
        if ((mDialog != null) && mDialog.isShowing()) {
            boolean started = false;
            if ((preference.cellName == null) || preference.cellName.isEmpty())
                mCellsName.setText(MobileCellsScanner.cellsNameForAutoRegistration);
            else
                mCellsName.setText(preference.cellName);
            if (MobileCellsScanner.enabledAutoRegistration && !forceStop) {
                mStatus.setText(R.string.mobile_cells_registration_pref_dlg_status_started);
                if (millisUntilFinished > 0) {
                    mRemainingTime.setVisibility(View.VISIBLE);
                    String time = getString(R.string.mobile_cells_registration_pref_dlg_status_remaining_time);
                    long iValue = millisUntilFinished / 1000;
                    time = time + ": " + GlobalGUIRoutines.getDurationString((int)iValue);
                    mRemainingTime.setText(time);
                    started = true;
                }
            }
            if (!started) {
                mStatus.setText(R.string.mobile_cells_registration_pref_dlg_status_stopped);
                mRemainingTime.setVisibility(View.GONE);
            }

            mValue.setEnabled(!started);
            if (started) {
                ColorStateList colors = mValue.getHintTextColors();
                mValue.setTextColor(colors);
            }
            else
                mValue.setTextColor(GlobalGUIRoutines.getThemeAccentColor(prefContext));
            mSeekBarHours.setEnabled(!started);
            mSeekBarMinutes.setEnabled(!started);
            mSeekBarSeconds.setEnabled(!started);
            mCellsName.setEnabled(!started);
            if (started) {
                ColorStateList colors = mCellsName.getHintTextColors();
                mCellsName.setTextColor(colors);
            }
            else
                mCellsName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(prefContext));

            String value = mCellsName.getText().toString();
            boolean enable = !value.isEmpty();
            if (started) {
                if (MobileCellsScanner.isEventAdded(preference.event_id)) {
                    if (MobileCellsScanner.getEventCount() == 1) {
                        startButton.setText(R.string.mobile_cells_registration_pref_dlg_start_button);
                        enable = false;
                    }
                    else
                        startButton.setText(R.string.mobile_cells_registration_pref_dlg_remove_event_button);
                }
                else
                    startButton.setText(R.string.mobile_cells_registration_pref_dlg_add_event_button);
            }
            else
                startButton.setText(R.string.mobile_cells_registration_pref_dlg_start_button);
            startButton.setEnabled(enable);

            stopButton.setEnabled(started);
        }
    }

    void startRegistration() {
        if ((mDialog != null) && mDialog.isShowing()) {
            int hours = mSeekBarHours.getProgress();
            int minutes = mSeekBarMinutes.getProgress();
            int seconds = mSeekBarSeconds.getProgress();

            int iValue = (hours * 3600 + minutes * 60 + seconds);
            if (iValue < preference.mMin) iValue = preference.mMin;
            if (iValue > preference.mMax) iValue = preference.mMax;

            //Log.d("MobileCellsRegistrationDialogPreference.onPositive","iValue="+iValue);

            //Log.d("MobileCellsRegistrationDialogPreference.onPositive","is started");
            MobileCellsRegistrationService.setMobileCellsAutoRegistrationRemainingDuration(prefContext, iValue);
            MobileCellsScanner.durationForAutoRegistration = iValue;
            MobileCellsScanner.cellsNameForAutoRegistration = mCellsName.getText().toString();
            //PPApplication.logE("MobileCellsRegistrationDialogPreferenceFragmentX.startRegistration",
            //        "cellsNameForAutoRegistration="+MobileCellsScanner.cellsNameForAutoRegistration);
            MobileCellsScanner.startAutoRegistration(prefContext.getApplicationContext(), false);

            preference.value = String.valueOf(iValue);
            preference.setSummaryDDP(0);

            mDialog.dismiss();
        }
    }

    void setCellNameText(String text) {
        //PPApplication.logE("MobileCellsRegistrationDialogPreferenceFragmentX.setCellNameText", "text="+text);
        mCellsName.setText(text);
    }

    String getCellNameText() {
        return mCellsName.getText().toString();
    }

}
