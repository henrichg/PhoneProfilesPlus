package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.redmadrobot.inputmask.MaskedTextChangedListener;

public class DurationDialogPreference2 extends DialogPreference
                                        implements SeekBar.OnSeekBarChangeListener {

    private String value;

    private int mMin, mMax;

    private MaterialDialog mDialog;
    private EditText mValue;
    private SeekBar mSeekBarHours;
    private SeekBar mSeekBarMinutes;
    private SeekBar mSeekBarSeconds;

    //private int mColor = 0;

    public DurationDialogPreference2(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray durationDialogType = context.obtainStyledAttributes(attrs,
                R.styleable.DurationDialogPreference, 0, 0);

        mMax = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMax, 5);
        mMin = durationDialogType.getInt(R.styleable.DurationDialogPreference_dMin, 0);

        durationDialogType.recycle();

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        //    mColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
    }

    @Override
    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                        //.disableDefaultFonts()
                .icon(getDialogIcon())
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .content(getDialogMessage())
                .customView(R.layout.activity_duration_pref_dialog2, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        int hours = mSeekBarHours.getProgress();
                        int minutes = mSeekBarMinutes.getProgress();
                        int seconds = mSeekBarSeconds.getProgress();

                        int iValue = (hours * 3600 + minutes * 60 + seconds);
                        if (iValue < mMin) iValue = mMin;
                        if (iValue > mMax) iValue = mMax;

                        value = String.valueOf(iValue);

                        if (callChangeListener(value)) {
                            //persistInt(mNumberPicker.getValue());
                            persistString(value);
                            setSummaryDDP();
                        }
                    }
                });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        TextView mTextViewRange = (TextView) layout.findViewById(R.id.duration_pref_dlg_range);
        mValue = (EditText) layout.findViewById(R.id.duration_pref_dlg_value);
        mSeekBarHours = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_hours);
        mSeekBarMinutes = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_minutes);
        mSeekBarSeconds = (SeekBar) layout.findViewById(R.id.duration_pref_dlg_seconds);

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

        final MaskedTextChangedListener listener = new MaskedTextChangedListener(
                "[00]{:}[00]{:}[00]",
                true,
                mValue,
                null,
                new MaskedTextChangedListener.ValueListener() {
                    @Override
                    public void onTextChanged(boolean maskFilled, @NonNull final String extractedValue) {
                        //Log.d(DurationDialogPreference2.class.getSimpleName(), extractedValue);
                        //Log.d(DurationDialogPreference2.class.getSimpleName(), String.valueOf(maskFilled));

                        int hours = 0;
                        int minutes = 0;
                        int seconds = 0;
                        String[] splits = extractedValue.split(":");
                        try {
                            hours = Integer.parseInt(splits[0].replaceFirst("\\s+$", ""));
                        } catch (Exception ignored) {
                        }
                        try {
                            minutes = Integer.parseInt(splits[1].replaceFirst("\\s+$", ""));
                        } catch (Exception ignored) {
                        }
                        try {
                            seconds = Integer.parseInt(splits[2].replaceFirst("\\s+$", ""));
                        } catch (Exception ignored) {
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
                }
        );
        mValue.addTextChangedListener(listener);
        mValue.setOnFocusChangeListener(listener);
        mValue.setHint(listener.placeholder());

        mSeekBarHours.setOnSeekBarChangeListener(this);
        mSeekBarMinutes.setOnSeekBarChangeListener(this);
        mSeekBarSeconds.setOnSeekBarChangeListener(this);

        mTextViewRange.setText(sMin + " - " + sMax);

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
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

        if(restoreValue)
        {
            value = getPersistedString(value);
        }
        else
        {
            value = (String)defaultValue;
            persistString(value);
        }
        setSummaryDDP();
    }

    @SuppressLint("DefaultLocale")
    private void setSummaryDDP()
    {
        setSummary(GlobalGUIRoutines.getDurationString(Integer.parseInt(value)));
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

}