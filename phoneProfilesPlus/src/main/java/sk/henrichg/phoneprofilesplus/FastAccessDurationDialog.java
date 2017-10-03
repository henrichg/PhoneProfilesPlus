package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

class FastAccessDurationDialog implements SeekBar.OnSeekBarChangeListener{

    private int mMin, mMax;
    private Profile mProfile;
    private int mAfterDo;

    private Context context;

    private DataWrapper mDataWrapper;
    private boolean mMonochrome;
    private int mMonochromeValue;
    private int mStartupSource;
    private boolean mInteractive;
    private Activity mActivity;
    //private boolean mLog;
    private String[] afterDoValues;

    //Context mContext;

    private MaterialDialog mDialog;
    private TextView mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private SeekBar mSeekBarSeconds;
    private TextView mEnds;
    private TimeDurationPickerDialog mValueDialog;

    private volatile Timer updateEndsTimer = null;

    //private int mColor = 0;

    FastAccessDurationDialog(Activity activity, Profile profile, DataWrapper dataWrapper,
                             boolean monochrome, int monochromeValue,
                             int startupSource, boolean interactive) {

        mMax = 86400;
        mMin = 0;
        mAfterDo = -1;

        mActivity = activity;
        //mContext = activity.getBaseContext();
        mProfile = profile;
        mDataWrapper = dataWrapper;
        mMonochrome = monochrome;
        mMonochromeValue = monochromeValue;
        mStartupSource = startupSource;
        mInteractive = interactive;

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
            */


        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title(mActivity.getString(R.string.profile_preferences_duration))
                .positiveText(R.string.fast_access_duration_activate_with_button)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.fast_access_duration_activate_without_button)
                .customView(R.layout.activity_fast_access_duration_dialog, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        updateEndsTimer = null;

                        int hours = mSeekBarHours.getProgress();
                        int minutes = mSeekBarMinutes.getProgress();
                        int seconds = mSeekBarSeconds.getProgress();

                        int iValue = (hours * 3600 + minutes * 60 + seconds);
                        if (iValue < mMin) iValue = mMin;
                        if (iValue > mMax) iValue = mMax;

                        mProfile._duration = iValue;
                        if (mAfterDo != -1)
                            mProfile._afterDurationDo = mAfterDo;
                        mDataWrapper.getDatabaseHandler().updateProfile(mProfile);

                        if (Permissions.grantProfilePermissions(mActivity, mProfile, false, false,
                                true, mMonochrome, mMonochromeValue,
                                mStartupSource, mInteractive, mActivity, true))
                            mDataWrapper._activateProfile(mProfile, false, mStartupSource, mInteractive, mActivity);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        updateEndsTimer = null;
                        mDataWrapper.finishActivity(mStartupSource, false, mActivity);
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        updateEndsTimer = null;

                        mProfile._duration = 0;
                        mDataWrapper.getDatabaseHandler().updateProfile(mProfile);

                        if (Permissions.grantProfilePermissions(mActivity, mProfile, false, false,
                                true, mMonochrome, mMonochromeValue,
                                mStartupSource, mInteractive, mActivity, true))
                            mDataWrapper._activateProfile(mProfile, false, mStartupSource, mInteractive, mActivity);
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        updateEndsTimer = null;
                        mDataWrapper.finishActivity(mStartupSource, false, mActivity);
                    }
                });


        mDialog = mBuilder.build();

        View layout = mDialog.getCustomView();

        TextView mTextViewRange = layout.findViewById(R.id.duration_pref_dlg_range);

        mValue = layout.findViewById(R.id.duration_pref_dlg_value);
        mSeekBarHours = layout.findViewById(R.id.duration_pref_dlg_hours);
        mSeekBarMinutes = layout.findViewById(R.id.duration_pref_dlg_minutes);
        mSeekBarSeconds = layout.findViewById(R.id.duration_pref_dlg_seconds);
        mEnds = layout.findViewById(R.id.duration_pref_dlg_ends);

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
        int iValue = mProfile._duration;
        hours = iValue / 3600;
        minutes = (iValue % 3600) / 60;
        seconds = iValue % 60;
        mSeekBarHours.setProgress(hours);
        mSeekBarMinutes.setProgress(minutes);
        mSeekBarSeconds.setProgress(seconds);

        mValue.setText(GlobalGUIRoutines.getDurationString(iValue));
        mEnds.setText(GlobalGUIRoutines.getEndsAtString(iValue));

        mValueDialog = new TimeDurationPickerDialog(activity, new TimeDurationPickerDialog.OnDurationSetListener() {
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

                updateTextFields(false);
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

        Spinner afterDoSpinner = layout.findViewById(R.id.fast_access_duration_dlg_after_do_spinner);
        afterDoValues = mActivity.getResources().getStringArray(R.array.afterProfileDurationDoValues);
        afterDoSpinner.setSelection(Arrays.asList(afterDoValues).indexOf(String.valueOf(mProfile._afterDurationDo)));
        afterDoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAfterDo = Integer.valueOf(afterDoValues[position]);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        updateEndsTimer = new Timer();
        updateEndsTimer.schedule(new TimerTask() {
            private Activity activity;
            private TimerTask init(Activity a) {
                activity = a;
                return this;
            }

            @Override
            public void run() {
                if(updateEndsTimer != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(updateEndsTimer != null) {
                                updateTextFields(false);
                            }
                        }
                    });
                } else {
                    this.cancel();
                }
            }
        }.init(activity), 250, 250);

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            updateTextFields(true);
        }
    }

    private void updateTextFields(boolean updateValueField) {
        int hours = mSeekBarHours.getProgress();
        int minutes = mSeekBarMinutes.getProgress();
        int seconds = mSeekBarSeconds.getProgress();

        int iValue = (hours * 3600 + minutes * 60 + seconds);
        if (iValue < mMin) iValue = mMin;
        if (iValue > mMax) iValue = mMax;

        if(mDialog!=null && mDialog.getActionButton(DialogAction.POSITIVE).isEnabled()) {
            mEnds.setText(GlobalGUIRoutines.getEndsAtString(iValue));
        } else {
            mEnds.setText("--");
        }

        if(updateValueField) {
            mValue.setText(GlobalGUIRoutines.getDurationString(iValue));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void show() {
        mDialog.show();
    }

}
