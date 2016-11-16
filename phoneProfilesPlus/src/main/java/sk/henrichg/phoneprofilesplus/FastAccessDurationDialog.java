package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.github.pinball83.maskededittext.MaskedEditText;

import java.util.Arrays;

class FastAccessDurationDialog implements SeekBar.OnSeekBarChangeListener{

    private int mMin, mMax;
    private Profile mProfile;
    private int mAfterDo;

    private DataWrapper mDataWrapper;
    private int mStartupSource;
    private boolean mInteractive;
    private Activity mActivity;
    private boolean mLog;
    private String[] afterDoValues;

    //Context mContext;

    MaterialDialog mDialog;
    private MaskedEditText mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private SeekBar mSeekBarSeconds;

    //private int mColor = 0;

    FastAccessDurationDialog(Activity activity, Profile profile, DataWrapper dataWrapper, int startupSource,
                                    boolean interactive, boolean log) {

        mMax = 86400;
        mMin = 0;
        mAfterDo = -1;

        mActivity = activity;
        //mContext = activity.getBaseContext();
        mProfile = profile;
        mDataWrapper = dataWrapper;
        mStartupSource = startupSource;
        mInteractive = interactive;
        mLog = log;

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
            */


        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title(mActivity.getString(R.string.profile_preferences_duration))
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .customView(R.layout.activity_fast_access_duration_dialog, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
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
                        mDataWrapper._activateProfile(mProfile, false, mStartupSource, mInteractive, mActivity, mLog);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        mDataWrapper.finishActivity(mStartupSource, false, mActivity);
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mDataWrapper.finishActivity(mStartupSource, false, mActivity);
                    }
                });


        mDialog = mBuilder.build();

        View layout = mDialog.getCustomView();

        TextView mTextViewRange = (TextView) layout.findViewById(R.id.duration_pref_dlg_range);
        mValue = (MaskedEditText) layout.findViewById(R.id.duration_pref_dlg_value);
        mSeekBarHours = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_hours);
        mSeekBarMinutes = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_minutes);
        mSeekBarSeconds = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_seconds);

        mValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = mValue.getText().toString();
                int hours = 0;
                int minutes = 0;
                int seconds = 0;
                String[] splits = value.split(":");
                try {
                    hours = Integer.parseInt(splits[0].replaceFirst("\\s+$", ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    minutes = Integer.parseInt(splits[1].replaceFirst("\\s+$", ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    seconds = Integer.parseInt(splits[2].replaceFirst("\\s+$", ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int iValue = (hours * 3600 + minutes * 60 + seconds);

                boolean badText = false;
                if (iValue < mMin) {
                    iValue = mMin;
                    badText = true;
                }
                if (iValue > mMax) {
                    iValue = mMax;
                    badText = true;
                }

                if (mDialog != null) {
                    MDButton button = mDialog.getActionButton(DialogAction.POSITIVE);
                    button.setEnabled(!badText);
                }

                hours = iValue / 3600;
                minutes = (iValue % 3600) / 60;
                seconds = iValue % 60;

                mSeekBarHours.setProgress(hours);
                mSeekBarMinutes.setProgress(minutes);
                mSeekBarSeconds.setProgress(seconds);
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
        final String sMax = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        mSeekBarHours.setMax(hours);
        if (hours == 0)
            mSeekBarMinutes.setMax(minutes);
        else
            mSeekBarMinutes.setMax(59);
        if ((hours == 0) && (minutes == 0))
            mSeekBarSeconds.setMax(seconds);
        else
            mSeekBarSeconds.setMax(59);
        hours = mMin / 3600;
        minutes = (mMin % 3600) / 60;
        seconds = mMin % 60;
        final String sMin = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        int iValue = mProfile._duration;
        hours = iValue / 3600;
        minutes = (iValue % 3600) / 60;
        seconds = iValue % 60;
        mSeekBarHours.setProgress(hours);
        mSeekBarMinutes.setProgress(minutes);
        mSeekBarSeconds.setProgress(seconds);

        mValue.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        mSeekBarHours.setOnSeekBarChangeListener(this);
        mSeekBarMinutes.setOnSeekBarChangeListener(this);
        mSeekBarSeconds.setOnSeekBarChangeListener(this);

        mTextViewRange.setText(sMin + " - " + sMax);

        Spinner afterDoSpinner = (Spinner) layout.findViewById(R.id.fast_access_duration_dlg_after_do_spinner);
        afterDoValues = mActivity.getResources().getStringArray(R.array.afterProfileDurationDoValues);
        afterDoSpinner.setSelection(Arrays.asList(afterDoValues).indexOf(String.valueOf(mProfile._afterDurationDo)));
        afterDoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAfterDo = Integer.valueOf(afterDoValues[position]);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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

            hours = iValue / 3600;
            minutes = (iValue % 3600) / 60;
            seconds = iValue % 60;

            mValue.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
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
