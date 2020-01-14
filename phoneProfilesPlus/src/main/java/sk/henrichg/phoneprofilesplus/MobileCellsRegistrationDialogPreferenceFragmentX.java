package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

@SuppressWarnings("WeakerAccess")
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
        dialogBuilder.setPositiveButton(R.string.mobile_cells_registration_pref_dlg_start_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Permissions.grantMobileCellsRegistrationDialogPermissions(prefContext)) {
                    if (PhoneStateScanner.enabledAutoRegistration) {
                        if (!PhoneStateScanner.isEventAdded(preference.event_id))
                            PhoneStateScanner.addEvent(preference.event_id);
                        else
                            PhoneStateScanner.removeEvent(preference.event_id);
                    }
                    else {
                        if (!PhoneStateScanner.isEventAdded(preference.event_id))
                            PhoneStateScanner.addEvent(preference.event_id);
                        preference.startRegistration();
                    }
                }
            }
        });

        LayoutInflater inflater = ((Activity)prefContext).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_mobile_cells_registration_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                preference.updateInterface(0, false);
            }
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
        int iValue = Integer.valueOf(preference.value);
        hours = iValue / 3600;
        minutes = (iValue % 3600) / 60;
        seconds = iValue % 60;
        mSeekBarHours.setProgress(hours);
        mSeekBarMinutes.setProgress(minutes);
        mSeekBarSeconds.setProgress(seconds);

        mValue.setText(GlobalGUIRoutines.getDurationString(iValue));

        mValueDialog = new TimeDurationPickerDialog(prefContext, new TimeDurationPickerDialog.OnDurationSetListener() {
            @Override
            public void onDurationSet(TimeDurationPicker view, long duration) {
                int iValue = (int) duration / 1000;

                if (iValue < preference.mMin)
                    iValue = preference.mMin;
                if (iValue > preference.mMax)
                    iValue = preference.mMax;

                preference.value = String.valueOf(iValue);

                mValue.setText(GlobalGUIRoutines.getDurationString(iValue));

                int hours = iValue / 3600;
                int minutes = (iValue % 3600) / 60;
                int seconds = iValue % 60;

                mSeekBarHours.setProgress(hours);
                mSeekBarMinutes.setProgress(minutes);
                mSeekBarSeconds.setProgress(seconds);
            }
        }, iValue * 1000, TimeDurationPicker.HH_MM_SS);
        GlobalGUIRoutines.setThemeTimeDurationPickerDisplay(mValueDialog.getDurationInput(), getActivity());
        mValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int hours = mSeekBarHours.getProgress();
                    int minutes = mSeekBarMinutes.getProgress();
                    int seconds = mSeekBarSeconds.getProgress();

                    int iValue = (hours * 3600 + minutes * 60 + seconds);
                    if (iValue < preference.mMin) iValue = preference.mMin;
                    if (iValue > preference.mMax) iValue = preference.mMax;

                    preference.value = String.valueOf(iValue);

                    mValueDialog.setDuration(iValue * 1000);
                    if (!((Activity)prefContext).isFinishing())
                        mValueDialog.show();
                }
            }
        );

        mMobileCellNamesDialog = new MobileCellNamesDialogX((Activity)prefContext, preference, false);
        mCellsName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!((Activity)prefContext).isFinishing())
                        mMobileCellNamesDialog.show();
                }
            }
        );

        mSeekBarHours.setOnSeekBarChangeListener(this);
        mSeekBarMinutes.setOnSeekBarChangeListener(this);
        mSeekBarSeconds.setOnSeekBarChangeListener(this);

        mTextViewRange.setText(sMin + " - " + sMax);

        startButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);

        stopButton = layout.findViewById(R.id.mobile_cells_registration_stop_button);
        //stopButton.setAllCaps(false);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInterface(0, true);
                MobileCellsRegistrationService.setMobileCellsAutoRegistrationRemainingDuration(prefContext, 0);
                //PPApplication.phoneProfilesService.phoneStateScanner.durationForAutoRegistration = 0;
                //PPApplication.phoneProfilesService.phoneStateScanner.cellsNameForAutoRegistration = "";
                preference.setSummaryDDP(0);
                PhoneStateScanner.stopAutoRegistration(prefContext);
            }
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
                mCellsName.setText(PhoneStateScanner.cellsNameForAutoRegistration);
            else
                mCellsName.setText(preference.cellName);
            if (PhoneStateScanner.enabledAutoRegistration && !forceStop) {
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
                if (PhoneStateScanner.isEventAdded(preference.event_id)) {
                    if (PhoneStateScanner.getEventCount() == 1) {
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
            PhoneStateScanner.durationForAutoRegistration = iValue;
            PhoneStateScanner.cellsNameForAutoRegistration = mCellsName.getText().toString();
            //PPApplication.logE("MobileCellsRegistrationDialogPreferenceFragmentX.startRegistration",
            //        "cellsNameForAutoRegistration="+PhoneStateScanner.cellsNameForAutoRegistration);
            PhoneStateScanner.startAutoRegistration(prefContext, false);

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
