package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

public class MobileCellsRegistrationDialogPreference extends DialogPreference
                                        implements SeekBar.OnSeekBarChangeListener {

    private String value;
    private final Context context;

    private final int mMin, mMax;
    long event_id;

    private AlertDialog mDialog;
    private TextView mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private SeekBar mSeekBarSeconds;
    TextView mCellsName;
    private TextView mStatus;
    private TextView mRemainingTime;
    private TimeDurationPickerDialog mValueDialog;
    private Button startButton;
    private Button stopButton;
    private MobileCellNamesDialog mMobileCellNamesDialog;

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
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void showDialog(Bundle state) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(R.string.mobile_cells_registration_pref_dlg_start_button, new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Permissions.grantMobileCellsRegistrationDialogPermissions(context)) {
                    if (PhoneStateScanner.enabledAutoRegistration) {
                        if (!PhoneStateScanner.isEventAdded(event_id))
                            PhoneStateScanner.addEvent(event_id);
                        else
                            PhoneStateScanner.removeEvent(event_id);
                    }
                    else {
                        if (!PhoneStateScanner.isEventAdded(event_id))
                            PhoneStateScanner.addEvent(event_id);
                        startRegistration();
                    }
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_mobile_cells_registration_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                updateInterface(0, false);
            }
        });

        //noinspection ConstantConditions
        TextView mTextViewRange = layout.findViewById(R.id.duration_pref_dlg_range);
        //noinspection ConstantConditions
        mValue = layout.findViewById(R.id.duration_pref_dlg_value);
        //noinspection ConstantConditions
        mSeekBarHours = layout.findViewById(R.id.duration_pref_dlg_hours);
        //noinspection ConstantConditions
        mSeekBarMinutes = layout.findViewById(R.id.duration_pref_dlg_minutes);
        //noinspection ConstantConditions
        mSeekBarSeconds = layout.findViewById(R.id.duration_pref_dlg_seconds);
        //noinspection ConstantConditions
        mCellsName = layout.findViewById(R.id.mobile_cells_registration_cells_name);
        //noinspection ConstantConditions
        mStatus = layout.findViewById(R.id.mobile_cells_registration_status);
        //noinspection ConstantConditions
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
                    if (!((Activity)context).isFinishing())
                        mValueDialog.show();
                }
             }
        );

        mMobileCellNamesDialog = new MobileCellNamesDialog((Activity)context, this, false);
        mCellsName.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
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
                MobileCellsRegistrationService.setMobileCellsAutoRegistrationRemainingDuration(context, 0);
                //PPApplication.phoneProfilesService.phoneStateScanner.durationForAutoRegistration = 0;
                //PPApplication.phoneProfilesService.phoneStateScanner.cellsNameForAutoRegistration = "";
                setSummaryDDP(0);
                PhoneStateScanner.stopAutoRegistration(context);
            }
        });

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);

        if (!((Activity)context).isFinishing())
            mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mDialog != null) && mDialog.isShowing())
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

    void setSummaryDDP(long millisUntilFinished)
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
            String newCount = context.getString(R.string.mobile_cells_registration_pref_dlg_status_new_cells_count);
            long iValue = DatabaseHandler.getInstance(context.getApplicationContext()).getNewMobileCellsCount();
            newCount = newCount + " " + iValue;
            summary = summary + "; " + newCount;
        }

        setSummary(summary);
    }

    void updateInterface(long millisUntilFinished, boolean forceStop) {
        if ((mDialog != null) && mDialog.isShowing()) {
            boolean started = false;
            mCellsName.setText(PhoneStateScanner.cellsNameForAutoRegistration);
            if (PhoneStateScanner.enabledAutoRegistration && !forceStop) {
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

            mValue.setEnabled(!started);
            if (started) {
                ColorStateList colors = mValue.getHintTextColors();
                mValue.setTextColor(colors);
            }
            else
                mValue.setTextColor(ContextCompat.getColor(context, R.color.accent));
            mSeekBarHours.setEnabled(!started);
            mSeekBarMinutes.setEnabled(!started);
            mSeekBarSeconds.setEnabled(!started);
            mCellsName.setEnabled(!started);
            if (started) {
                ColorStateList colors = mCellsName.getHintTextColors();
                mCellsName.setTextColor(colors);
            }
            else
                mCellsName.setTextColor(ContextCompat.getColor(context, R.color.accent));

            String value = mCellsName.getText().toString();
            boolean enable = !value.isEmpty();
            if (started) {
                if (PhoneStateScanner.isEventAdded(event_id)) {
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

    void startRegistration() {
        if ((mDialog != null) && mDialog.isShowing()) {
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
            PPApplication.logE("MobileCellsRegistrationDialogPreference.startRegistration",
                    "cellsNameForAutoRegistration="+PhoneStateScanner.cellsNameForAutoRegistration);
            PhoneStateScanner.startAutoRegistration(context, false);

            value = String.valueOf(iValue);
            setSummaryDDP(0);

            mDialog.dismiss();
        }
    }

}