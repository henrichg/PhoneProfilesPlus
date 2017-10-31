package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

public class MobileCellsRegistrationDialogPreference extends DialogPreference
                                        implements SeekBar.OnSeekBarChangeListener {

    private String value;
    private final Context context;

    private final int mMin, mMax;

    private MaterialDialog mDialog;
    private TextView mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private SeekBar mSeekBarSeconds;
    private EditText mCellsName;
    private TextView mStatus;
    private TextView mRemainingTime;
    private TimeDurationPickerDialog mValueDialog;

    //private int mColor = 0;

    public MobileCellsRegistrationDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray durationDialogType = context.obtainStyledAttributes(attrs,
                R.styleable.DurationDialogPreference, 0, 0);

        mMax = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMax, 5);
        mMin = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMin, 0);

        durationDialogType.recycle();

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        //    mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);

        this.context = context;

        MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MobileCellsRegistrationService.ACTION_COUNT_DOWN_TICK);
        PhoneProfilesPreferencesNestedFragment.mobileCellsRegistrationBroadcastReceiver =
                new MobileCellsRegistrationBroadcastReceiver(this);
        context.registerReceiver(PhoneProfilesPreferencesNestedFragment.mobileCellsRegistrationBroadcastReceiver, intentFilter);

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void showDialog(Bundle state) {

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                        //.disableDefaultFonts()
                .icon(getDialogIcon())
                .positiveText(R.string.mobile_cells_registration_pref_dlg_start_button)
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.mobile_cells_registration_pref_dlg_stop_button)
                .content(getDialogMessage())
                .customView(R.layout.activity_mobile_cells_registration_pref_dialog, true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        int hours = mSeekBarHours.getProgress();
                        int minutes = mSeekBarMinutes.getProgress();
                        int seconds = mSeekBarSeconds.getProgress();

                        int iValue = (hours * 3600 + minutes * 60 + seconds);
                        if (iValue < mMin) iValue = mMin;
                        if (iValue > mMax) iValue = mMax;

                        //Log.d("MobileCellsRegistrationDialogPreference.onPositive","iValue="+iValue);

                        //Log.d("MobileCellsRegistrationDialogPreference.onPositive","is started");
                        MobileCellsRegistrationService.setMobileCellsAutoRegistrationRemainingDuration(context, iValue);
                        PhoneStateScanner.durationForAutoRegistration = iValue;
                        PhoneStateScanner.cellsNameForAutoRegistration = mCellsName.getText().toString();
                        PhoneStateScanner.enabledAutoRegistration = true;
                        MobileCellsRegistrationService.setMobileCellsAutoRegistration(context, false);
                        PhoneStateScanner.startAutoRegistration(context);

                        value = String.valueOf(iValue);
                        setSummaryDDP(0);

                        /*
                        value = String.valueOf(iValue);

                        if (callChangeListener(value)) {
                            //persistInt(mNumberPicker.getValue());
                            persistString(value);
                        }
                        */
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        MobileCellsRegistrationService.setMobileCellsAutoRegistrationRemainingDuration(context, 0);
                        //PPApplication.phoneProfilesService.phoneStateScanner.durationForAutoRegistration = 0;
                        //PPApplication.phoneProfilesService.phoneStateScanner.cellsNameForAutoRegistration = "";
                        PhoneStateScanner.enabledAutoRegistration = false;
                        MobileCellsRegistrationService.setMobileCellsAutoRegistration(context, false);
                        setSummaryDDP(0);
                        PhoneStateScanner.stopAutoRegistration(context);
                    }
                })
                ;

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        TextView mTextViewRange = layout.findViewById(R.id.duration_pref_dlg_range);
        mValue = layout.findViewById(R.id.duration_pref_dlg_value);
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
                MDButton button = mDialog.getActionButton(DialogAction.POSITIVE);
                button.setEnabled(!value.isEmpty());
            }
        });

        //mSeekBarHours.setRotation(180);
        //mSeekBarMinutes.setRotation(180);
        //mSeekBarSeconds.setRotation(180);

        // Initialize state
        int hours;
        int minutes;
        int seconds;
        hours = mMax / 3600;
        minutes = (mMax % 3600) / 60;
        seconds = mMax % 60;
        final String sMax = GlobalGUIRoutines.getDurationString(mMax);
        mSeekBarHours.setMax(hours);
        if (hours == 0)
            mSeekBarMinutes.setMax(minutes);
        else
            mSeekBarMinutes.setMax(59);
        if ((hours == 0) && (minutes == 0))
            mSeekBarSeconds.setMax(seconds);
        else
            mSeekBarSeconds.setMax(59);
        final String sMin = GlobalGUIRoutines.getDurationString(mMin);
        int iValue = Integer.valueOf(value);
        hours = iValue / 3600;
        minutes = (iValue % 3600) / 60;
        seconds = iValue % 60;
        mSeekBarHours.setProgress(hours);
        mSeekBarMinutes.setProgress(minutes);
        mSeekBarSeconds.setProgress(seconds);

        mValue.setText(GlobalGUIRoutines.getDurationString(iValue));

        mValueDialog = new TimeDurationPickerDialog(context, new TimeDurationPickerDialog.OnDurationSetListener() {
            @Override
            public void onDurationSet(TimeDurationPicker view, long duration) {
                int iValue = (int) duration / 1000;

                if (iValue < mMin)
                    iValue = mMin;
                if (iValue > mMax)
                    iValue = mMax;

                mValue.setText(GlobalGUIRoutines.getDurationString(iValue));

                int hours = iValue / 3600;
                int minutes = (iValue % 3600) / 60;
                int seconds = iValue % 60;

                mSeekBarHours.setProgress(hours);
                mSeekBarMinutes.setProgress(minutes);
                mSeekBarSeconds.setProgress(seconds);
            }
        }, iValue * 1000, TimeDurationPicker.HH_MM_SS);

        mValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int hours = mSeekBarHours.getProgress();
                    int minutes = mSeekBarMinutes.getProgress();
                    int seconds = mSeekBarSeconds.getProgress();

                    int iValue = (hours * 3600 + minutes * 60 + seconds);
                    if (iValue < mMin) iValue = mMin;
                    if (iValue > mMax) iValue = mMax;

                    mValueDialog.setDuration(iValue * 1000);
                    mValueDialog.show();
                }
            }
        );

        TextView mValueDescription = layout.findViewById(R.id.duration_pref_dlg_value_spinnerChar);
        mValueDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int hours = mSeekBarHours.getProgress();
                    int minutes = mSeekBarMinutes.getProgress();
                    int seconds = mSeekBarSeconds.getProgress();

                    int iValue = (hours * 3600 + minutes * 60 + seconds);
                    if (iValue < mMin) iValue = mMin;
                    if (iValue > mMax) iValue = mMax;

                    mValueDialog.setDuration(iValue * 1000);
                    mValueDialog.show();
                }
            }
        );

        mSeekBarHours.setOnSeekBarChangeListener(this);
        mSeekBarMinutes.setOnSeekBarChangeListener(this);
        mSeekBarSeconds.setOnSeekBarChangeListener(this);

        mTextViewRange.setText(sMin + " - " + sMax);

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);

        String value = mCellsName.getText().toString();
        MDButton button = mDialog.getActionButton(DialogAction.POSITIVE);
        button.setEnabled(!value.isEmpty());

        mDialog.show();

        updateInterface(0);

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        /*if(restoreValue)
        {
            value = getPersistedString(value);
        }
        else
        {
            value = (String)defaultValue;
            persistString(value);
        }*/

        value = Integer.toString(PhoneStateScanner.durationForAutoRegistration);

        setSummaryDDP(0);
    }

    private void setSummaryDDP(long millisUntilFinished)
    {
        String summary = "";
        boolean started = false;
        if (PhoneStateScanner.enabledAutoRegistration) {
            if (millisUntilFinished > 0) {
                summary = getContext().getString(R.string.mobile_cells_registration_pref_dlg_status_started);
                String time = getContext().getString(R.string.mobile_cells_registration_pref_dlg_status_remaining_time);
                long iValue = millisUntilFinished / 1000;
                time = time + ": " + GlobalGUIRoutines.getDurationString((int)iValue);
                summary = summary + "; " + time;
                        started = true;
            }
        }
        if (!started) {
            summary = getContext().getString(R.string.mobile_cells_registration_pref_dlg_status_stopped);
            int iValue = Integer.parseInt(value);
            summary = summary + "; " + GlobalGUIRoutines.getDurationString(iValue);
        }

        setSummary(summary);
    }

    private void updateInterface(long millisUntilFinished) {
        if ((mDialog != null) && mDialog.isShowing()) {
            boolean started = false;
            mCellsName.setText(PhoneStateScanner.cellsNameForAutoRegistration);
            if (PhoneStateScanner.enabledAutoRegistration) {
                mStatus.setText(R.string.mobile_cells_registration_pref_dlg_status_started);
                if (millisUntilFinished > 0) {
                    mRemainingTime.setVisibility(View.VISIBLE);
                    String time = getContext().getString(R.string.mobile_cells_registration_pref_dlg_status_remaining_time);
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
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int hours = mSeekBarHours.getProgress();
            int minutes = mSeekBarMinutes.getProgress();
            int seconds = mSeekBarSeconds.getProgress();

            int iValue = (hours * 3600 + minutes * 60 + seconds);
            if (iValue < mMin) iValue = mMin;
            if (iValue > mMax) iValue = mMax;

            mValue.setText(GlobalGUIRoutines.getDurationString(iValue));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public class MobileCellsRegistrationBroadcastReceiver extends BroadcastReceiver {

        final MobileCellsRegistrationDialogPreference preference;

        MobileCellsRegistrationBroadcastReceiver(MobileCellsRegistrationDialogPreference preference) {
            this.preference = preference;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("MobileCellsRegistrationBroadcastReceiver", "xxx");
            long millisUntilFinished = intent.getLongExtra(MobileCellsRegistrationService.EXTRA_COUNTDOWN, 0L);
            preference.updateInterface(millisUntilFinished);
            preference.setSummaryDDP(millisUntilFinished);
        }
    }

}